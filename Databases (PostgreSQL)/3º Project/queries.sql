/* Query 1 */
SELECT nome, latitude, longitude
FROM local_publico
    NATURAL JOIN incidencia
    NATURAL JOIN item
GROUP BY latitude, longitude
HAVING count(anomalia_id) >= ALL (
        SELECT count(anomalia_id)
        FROM local_publico
            NATURAL JOIN incidencia
            NATURAL JOIN item
        GROUP BY latitude, longitude);

/* Query 2 */
SELECT email
FROM incidencia
    NATURAL JOIN anomalia
    NATURAL JOIN anomalia_traducao
    NATURAL JOIN utilizador_regular
WHERE ts BETWEEN '2019-01-01' AND '2019-07-01'
GROUP BY email
HAVING count(anomalia_id) >= ALL (
    SELECT count(anomalia_id)
    FROM incidencia
        NATURAL JOIN anomalia
        NATURAL JOIN anomalia_traducao
        NATURAL JOIN utilizador_regular
    WHERE ts BETWEEN '2019-01-01' AND '2019-07-01'
    GROUP BY email);

/* Query 3 */
SELECT email
FROM incidencia
    NATURAL JOIN item
    NATURAL JOIN anomalia
WHERE latitude > 39.336775 AND date_part('year', ts) = '2019'
GROUP BY email
HAVING count(DISTINCT (latitude, longitude)) = (
    SELECT count(*)
    FROM local_publico
    WHERE latitude > 39.336775);

/* Query 4 */
SELECT DISTINCT email
FROM incidencia
    NATURAL JOIN utilizador_qualificado
    NATURAL JOIN item
    NATURAL JOIN anomalia
WHERE latitude < 39.336775 AND
      date_part('year', ts) = date_part('year', localtimestamp)
EXCEPT (
    SELECT email
        FROM ((
    SELECT email, count(anomalia_id)
    FROM incidencia
        NATURAL JOIN utilizador_qualificado
        NATURAL JOIN item
        NATURAL JOIN anomalia
    WHERE latitude < 39.336775 AND
        date_part('year', ts) = date_part('year', localtimestamp)
    GROUP BY email
    ) AS email_anomaly_count
        NATURAL JOIN (
    SELECT email, count(DISTINCT (texto, data_hora))
    FROM incidencia
        NATURAL JOIN utilizador_qualificado
        NATURAL JOIN item
        NATURAL JOIN anomalia
        NATURAL JOIN proposta_de_correcao
        NATURAL JOIN correcao
    WHERE latitude < 39.336775 AND
        date_part('year', ts) = date_part('year', localtimestamp)
    GROUP BY email
    ) AS email_anomaly_filter_count));
