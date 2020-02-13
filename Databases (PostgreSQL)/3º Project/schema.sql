DROP TABLE correcao CASCADE;
DROP TABLE proposta_de_correcao CASCADE;
DROP TABLE incidencia CASCADE;
DROP TABLE utilizador_regular CASCADE;
DROP TABLE utilizador_qualificado CASCADE;
DROP TABLE utilizador CASCADE;
DROP TABLE duplicado CASCADE;
DROP TABLE anomalia_traducao CASCADE;
DROP TABLE anomalia CASCADE;
DROP TABLE item CASCADE;
DROP TABLE local_publico CASCADE;

CREATE TABLE local_publico (
    latitude decimal(8, 6) not null,
    longitude decimal(9, 6) not null,
    nome varchar(200) not null,
    primary key(latitude, longitude)
);

CREATE TABLE item (
    item_id integer not null,
    item_descricao text not null,
    localizacao varchar(255) not null,
    latitude decimal(8, 6) not null,
    longitude decimal(9, 6) not null,
    primary key(item_id),
    foreign key(latitude, longitude)
        references local_publico(latitude, longitude) ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE anomalia (
    anomalia_id integer not null,
    zona box not null,
    imagem varchar(2083) not null,
    lingua char(3) not null,
    ts timestamp without time zone not null,
    anomalia_descricao text not null,
    tem_anomalia_redacao boolean not null,
    primary key(anomalia_id)
);

CREATE TABLE anomalia_traducao (
    anomalia_id integer not null,
    zona2 box not null,
    lingua2 char(3) not null,
    primary key(anomalia_id),
    foreign key(anomalia_id)
        references anomalia(anomalia_id) ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE duplicado (
    item1_id integer not null,
    item2_id integer not null,
    primary key(item1_id, item2_id),
    foreign key(item1_id)
        references item(item_id) ON UPDATE CASCADE ON DELETE CASCADE,
    foreign key(item2_id)
        references item(item_id) ON UPDATE CASCADE ON DELETE CASCADE,
    check(item1_id < item2_id)
);

CREATE TABLE utilizador (
    email varchar(254) not null,
    password varchar(40) not null,
    primary key(email)
);

CREATE TABLE utilizador_qualificado (
    email varchar(254) not null,
    primary key(email),
    foreign key(email)
        references utilizador(email) ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE utilizador_regular (
    email varchar(254) not null,
    primary key(email),
    foreign key(email)
        references utilizador(email) ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE incidencia (
    anomalia_id integer not null,
    item_id integer not null,
    email varchar(254) not null,
    primary key(anomalia_id),
    foreign key(anomalia_id)
        references anomalia(anomalia_id) ON UPDATE CASCADE ON DELETE CASCADE,
    foreign key(item_id)
        references item(item_id) ON UPDATE CASCADE ON DELETE CASCADE,
    foreign key(email)
        references utilizador(email) ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE proposta_de_correcao (
    email varchar(254) not null,
    nro integer not null,
    data_hora timestamp without time zone not null,
    texto text not null,
    primary key(email, nro),
    foreign key(email)
        references utilizador_qualificado(email) ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE correcao (
    email varchar(254) not null,
    nro integer not null,
    anomalia_id integer not null,
    primary key(email, nro, anomalia_id),
    foreign key(email, nro)
        references proposta_de_correcao(email, nro) ON UPDATE CASCADE ON DELETE CASCADE,
    foreign key(anomalia_id)
        references incidencia(anomalia_id) ON UPDATE CASCADE ON DELETE CASCADE
);
