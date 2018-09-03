; **********************************************************************
; * Grupo 01:                                                          *
; * Ana David (90702)                                                  *
; * Manuel Mascarenhas (90751)                                         *
; * Rafael Figueiredo (90770)                                          *
; **********************************************************************
PIXSCRN   EQU 8000H    ; Endereço onde comeca o Pixel Screen
DISPLAYS   EQU 0A000H  ; Endereço dos displays de 7 segmentos
TEC_LIN    EQU 0C000H  ; Endereço das linhas do teclado
TEC_COL    EQU 0E000H  ; Endereço das colunas do teclado
Pixeis_Pares   EQU 01010101b   ; Linhas pares para colocar no display
Pixeis_Impares EQU 10101010b   ; Linhas impares para colocar no display
Pixeis_apagados EQU 00000000b  ; Linhas para apagar no display

PLACE       1000H
pilha:      TABLE 100H      ; Espaço reservado para a pilha 
SP_inicial:               
tab:        WORD    pr_ar_int ; Interrupcão das prendas e das armas liga ao clock 0 
            WORD    nin_int   ; Interrupção dos ninjas ligado ao clock 
;;; Linhas e colunas de cada um dos ninjas
linha_nin1: STRING 12
linha_nin2: STRING 12
linha_nin3: STRING 12
linha_nin4: STRING 12
colun_nin1: STRING 0
colun_nin2: STRING 4
colun_nin3: STRING 8
colun_nin4: STRING 12

;;; Local onde as linhas e colunas, do ninja, da arma e da prenda estão memorizados
lin_i_n: STRING 0         ; Linha inicial do ninja
col_i_n: STRING 0          ; Coluna inicial do ninja
lin_i_c: STRING 7         ; Linha inicial de cima
col_i_c: STRING 29         ; Coluna inicial de cima
lin_i_b: STRING 21         ; Linha inicial de baixo
col_i_b: STRING 29         ; Coluna inicial de baixo

;;; Variaveis que na rotina liga pixel, permitem pintar o ecra, e apaga-lo
var_pinta_ecra: STRING 0    ; Utilizada para ativar o pixel_screen final ou inicial
var_apag_ecra:  STRING 0    ; Utilizada para apagar tudo o que exteja no pixel screen

;;; Diferentes variaveries que ajudam a guardar a informação necessaria para que o jogo funcione
var_contador:   STRING 0      ; Contador que vai ser utilizado para indicar as posiçoes dos objectos no pixel screen e se é prenda ou amra
var_contador_p_a: STRING 0    ; Contador que vai dizer a disposicao da prenda se em cima ou em baixo
;;; Os var_ints são usados como garantia de que passa pelas interrupções
var_int_n:         STRING 0       ; Verifica que passou pela interrupcao1
var_int_a_p:       STRING 0       ; Verifica que passou pela interrupcao0

;;; Quando activados permitem apagar os pixeis e não colocar pixeis
var_apagar_nin:       STRING 0     ; quando ligada permite que o pixel_screen apague os pixeis e não coloque o ninja
var_apagar_cima:     STRING 0      ; quando ligada permite que o pixel_screen apague os pixeis e não coloque do objecto de cima
var_apagar_baixo:       STRING 0   ; quando ligada permite que o pixel_screen apague os pixeis e não coloque do objecto de baixo

;;; Verificam se o ninja, o objecto de cima e o objecto de baixo estão ativos ou estão desativos
var_ninja:      STRING 0      ; 0 - ninja ativo, 1 - ninja inativo
var_baixo:       STRING 0      ; 0 - objecto baixo inativo, 1 - objecto de cima ativo
var_cima:     STRING 0      ; 0 - objecto de baixo inativo, 1 - objecto de cima ativo

;;; Diz-nos se do contador, foi escolhida uma arma ou uma prenda
alea_a:     STRING 0      ; 1 - escolhe a arma da aleatoriedade     
alea_p:     STRING 0      ; 1 - escolhe a prenda da aleatoriedade

;;;  Liga as permissoes para apagar ou colocar os pixeis no pixel_screen dos objectos de cima ou de baixo
var_liga_cima: STRING 0   
var_liga_baixo: STRING 0

;;; variaveis que apagam pela ultima vez o ninja, a arma e a prenda em actual 
var_apg_nin_colid: STRING 0
var_destruir: STRING 0

;;; Variaveis que ativam o jogo e desativam, e param o jogo
var_jogo_ativado: STRING 0
var_pausa_jogo: STRING 0

;;; Variaveis extras importantes para o funcionamento do jogo
var_valor_display: STRING 0    ; Valor guardado que está no display
var_teclado: STRING 0		  ; Valor indicado pelo teclado	
var_reset_display: STRING 0	      ; Quando ligado faz reset do display		
var_qual_ninja: STRING 0        ; Indica qual o ninja que vai subir ou descer com o teclado
var_sobe_desce: STRING 0          ; Indica se o ninja sobe ou desce
var_sobe_ninja: STRING 0          ; Ativa a subida do ninja
var_colo_desc_nin: STRING 0       ; Quando está a 1 - coloca no ecra, a 2 - desce o ninja

;;; Forma da Prenda,Arma,Ninja
Prenda: STRING 3,3
        STRING 0,1,0
		STRING 1,1,1
		STRING 0,1,0
Arma:   STRING 3,3
        STRING 1,0,1
		STRING 0,1,0
		STRING 1,0,1
Ninja:  STRING 3,4
        STRING 0,1,0
        STRING 1,1,1
        STRING 0,1,0
        String 1,0,1
place 0000H
;;;Inicializaçoes
principio:
	MOV SP, SP_inicial      ; inicializa SP para a palavra a seguir
                            ; à última da pilha
	MOV BTE, tab            ; incializa BTE

;;; Ciclo principal do programa
Ciclo:
    CALL keyboard
    CALL dec_nin            
	CALL esc_arm_nin        
	JMP Ciclo


; ***************************************************************************
; * Rotina que primeiramente vai verificar se o jogo esta ativo, e apenas   *
; * sai da rotina se estiver, e verifica se alguma das teclas estao primidas*
; ***************************************************************************
keyboard:              ;
    PUSH R0
	PUSH R1
	PUSH R2
	PUSH R3
	PUSH R4
	PUSH R5
	PUSH R6
	PUSH R7
	PUSH R8
	PUSH R9
	PUSH R10
inicio:                 
    MOV  R1, 1          ; R1 - linha 1
    MOV  R7, 0          ; R7 - contador de linha
    MOV  R5, 0	        ; R5 - contador de coluna
	
tecla:				    ; Scan da linha e coluna
    MOV  R2, TEC_LIN
	MOV  R3, TEC_COL
    MOVB [R2], R1       ; Atribui o numero da linha ao periferico de linha  
	MOVB R0, [R3]       ; Adquire a coluna do periferico de coluna
	MOV  R4, 0Fh
	AND  R0,R4
	CMP  R0, 0          ; Sem valor?
	JNZ  ha_tecla       ; Guarda ate desprimir a tecla
	SHL  R0, 1          ; Coluna seguinte
	MOV  R4, 8          ; R4 - linha 8
	CMP  R1 , R4        ; Completou as linhas?
	JNZ  aument_linha
	MOV	 R1, 1          ; Reinicia linha
	MOV R8, var_pausa_jogo
	MOVB R9, [R8]       ; R9 - verifica se a pausa está ativada, se tiver permanece no teclado até nao estar
	CMP  R9, R1           
	JZ  tecla
	MOV R8, var_jogo_ativado
	MOVB R9, [R8]      ; R9 - verifica se o jogo ja esta ativado, se nao extiver permanece no teclado
	CMP R9,0
	JZ tecla
	JMP  fim_tec	    ; Se nao foi clicado, então sai da rotina do teclado	
	
aument_linha:		    ; Scan da linha seguinte 
    SHL  R1,1           ; Linha seguinte
	JMP  tecla          ; Volta a reiniciar
	
ha_tecla:               ;Segurar tecla
    MOVB  [R2],R1       ; A linha e adicionada ao periferico da linha
    MOVB  R6,[R3]       ; R6 - adquire o valor do periferico da coluna
    MOV   R4, 0Fh
	AND   R6,R4
	CMP   R6,0          ; Botao largado
	JNZ   ha_tecla     
	JMP   num
	
num:
	CMP  R0,1           ; Verifica se R0 esta na primeira coluna
	JNZ  adici          ; Vai guardar um bit da coluna
	CMP  R1,1           ; Verifica se ja ta no 1
    JNZ  adici_2        ; Vai guardar um bit da linha
	JMP  valr           ; Se ja tiverem os dois obtem-se o numero do teclado
	
adici:
	SHR  R0,1           ; Anda um bit para o lado da coluna
	ADD  R5,1           ; R5 - contador de colunas 
	JMP  num
	
adici_2:
	SHR  R1,1           ; Um bit para o lado da linha
	ADD  R7,1           ; R7 - contador de linhas
	JMP  num

valr:	                ;binario para Hexa
    MOV  R4,4
	MUL  R7,R4          
	ADD  R7,R5			; R7 - valor do teclado da linha e coluna "clicada"
    MOV R8, var_teclado
	MOVB [R8],R7

op_tec:                 ;operacoes das teclas
   MOV R9, R7
   MOV R10,12            
   CMP R9,R10            
   JZ  preparar_jogo    ; Clicou na tecla c
   MOV R8, var_jogo_ativado
   MOVB R9, [R8]
   CMP R9,1             ; Vai manter-se no teclado até o jogo estiver ativado
   JNZ inicio
   MOV R9,R7
   MOV R10,13            
   CMP R9,R10
   JZ  loop_pausa       ; Clicou na tecla d
   MOV R8, var_pausa_jogo 
   MOVB R9, [R8]
   CMP R9,1             ; Se estiver em pausa vai se manter até a variavel pausa se desligar             
   JZ inicio
   MOV R9, R7
   MOV R10,14           ; Clicou na tecla d
   CMP R9,R10            
   JZ  terminar_jogo    
   CALL verificar_ninjas ; Vai verificada se foi clicada alguma das teclas que sobem ou descem o ninja
   JMP fim_tec
   
preparar_jogo:      
   Call apagar_display   ; Rotina que apaga o display
   Call pixel_inic_fin   ; Rotina que coloca o ecra inicial
   CALL contar_inicio    ; Faz uma contagem antes de apagar
   
continua_jogo:           ;Ativacao das diversas variaveis e colocacao dos ninjas no pixel screen
   CALL apagar_display   ; Apaga o display
   MOV R8, var_jogo_ativado
   MOV R9,1
   MOVB [R8], R9         ; Jogo ativado
   MOV R8,var_reset_display
   MOVB [R8], R9         ; Ativa reset do display
   CALL display          ; Chama a rotina que faz o reset do display, colocando-o a zero
   MOV R8,var_reset_display
   MOV R9,0
   MOVB [R8], R9         ; Desativa o reset do display
   MOV R8, var_ninja
   MOV R9,0              
   MOVB [R8], R9         ; Ativa a colocacao dos ninjas
   MOV R8, var_colo_desc_nin
   MOV R9,1    
   MOVB [R8], R9         ; Coloca a 1 a variavel que indica que vai colocar o ninja e nao descer na rotina seguinte
   CALL colocar_nin
   MOV R9,0
   MOVB [R8],R9          ; Desativa
   MOV R9,1
   MOV R8, var_ninja      
   MOVB [R8], R9         ; Desativa a colocacao dos ninja
   JMP fim_tec

terminar_jogo:
   MOV R8, var_jogo_ativado ; Desativa jogo
   MOV R9, 0
   MOVB [R8],R9             
   CALL fimjogo             ; Chama rotina que faz o fim do jogo
   JMP inicio

loop_pausa:
   MOV R8, var_pausa_jogo 
   MOVB R9, [R8]
   ADD R9,1                ;Adiciona um a pausa do jogo, se for 2 significa que ja tinha clicado na pausa logo esta a sair da pausa
   CMP R9,2
   JZ voltar_jogo

cont_pausa:                
   MOV R10,1
   MOVB [R8],R10           ; Coloca 1 na pausa
   JMP inicio

voltar_jogo:
   MOV R10,0 
   MOVB [R8],R10          ; Reset da variavel 

fim_tec:
    MOV R7,0
	MOV R8, var_teclado
	MOVB [R8],R7   
    POP R10
	POP R9
    POP R8
	POP R7
	POP R6
	POP R5
	POP R4
	POP R3
	POP R2
	POP R1
	POP R0
    RET

	
contar_inicio:	 ; Apenas faz tempo para apagar o pixel_screen inicial
   PUSH R0
   PUSH R1
   MOV R8,0
   MOV R9,10000
contar:
   CMP R8,R9
   JZ final_contar
   ADD R8, 1
   JMP contar
final_contar:
   POP R1
   POP R0
   RET


;;; Rotina que faz descer o ninja segundo a interrupção	
dec_nin: ;interrupçao 1 trata do ninja    
	PUSH R0
	PUSH R1
	MOV R0, var_contador        
    MOVB R1,[R0]            ; R1 - contador aleatorio
   	ADD R1,1                ; Adiciona 1 ao contador, que irá assim provocar a aleatoriedade
	MOVB [R0],R1
    EI1                     ; Ativa interrupcao 1
	EI
	MOV R0, var_int_n       
	MOVB R1, [R0]           ; R1 = 0 não passou pela interrupçao R1=1 passou pela interrupçao       
	CMP R1,0                ; R1 = 0 vai esperar que passe pela interrupçao R1=1 vai para o passo seguinte
	JZ nin_ret
	MOV R0, var_ninja       
	MOV R1,0                
	MOVB [R0],R1             ; Ativa o ninja
	MOV R0, var_colo_desc_nin 
	MOV R1,2
	MOVB [R0], R1            ; Provoca a descida automatica
    CALL colocar_nin
	MOV R1,0
	MOVB [R0], R1           ; Guarda em memoria o novo valor
	MOV R0, var_ninja
	MOV R1,1
	MOVB [R0], R1           ; Desativa o ninja
	MOV R0, var_int_n       ;
	MOV R1,0                ; 
	MOVB [R0], R1   
	
nin_ret:
    POP R1
	POP R0
	RET

	
;;; Rotina que escolhe por aleatoriedade a prenda ou a arma e coloca-os no pixel_screen numa dada posição tambem aleatoria e, trata ainda do seu movimento
esc_arm_nin:
    PUSH R0
    PUSH R1	
	EI0                     ; Ativa interrupcao 0
	EI
	MOV R0, var_contador_p_a        
    MOVB R1,[R0]            ; R1 - contador aleatorio que nos vai dizer onde dispor a prenda
   	ADD R1,3                ; Adiciona 3 ao contador, que irá assim provocar a aleatoriedade
	MOVB [R0],R1
	MOV R0, var_int_a_p
	MOVB R1, [R0]           ; R1 = 0 não passou pela interrupçao R1=1 passou pela interrupçao
	CMP R1,0                ; R1 = 0 vai esperar que passe pela interrupçao R1=1 vai para o passo seguinte
	JZ pr_ar_ret
	MOV R0, var_ninja      
	MOV R1,1                
	MOVB [R0],R1            ; Ninja desativado
    MOV R0, var_baixo       
	MOVB R1, [R0]
	CMP R1,0                ; Se for 0 vai buscar um novo valor
	JNZ ecra_cim_baix
	MOV R0, var_cima
	MOVB R1, [R0]
	CMP R1,0                ; Se os dois objectos tao em jogo entao passa a frente a escolha de um objecto
	JNZ ecra_cim_baix
	CALL arma_prenda        ; Vai identificar se vai adicionar ao pixel_screen uma arma ou uma prenda 
	CALL onde_ar_pr         ; Indica onde vai ser disposta essa arma ou prenda em cima ou em baixo a não ser que ja esteja em jogo

ecra_cim_baix:	
	CALL mov_nap_pixel      ; Vai provocar o movimento dos objectos que estejam ativos       
	CALL colisoes           ; Verifica se o ninja colide com algum objecto
	CALL fimjogo            ; Verifica se o jogo já acabou
	MOV R0,var_int_a_p       
	MOV R1,0                       
	MOVB [R0], R1           ; Volta a esperar que passe pela interrupção

pr_ar_ret:
    POP R1
	POP R0
    RET

	
nin_int:; ;Interrupçao 1
    PUSH R0
	PUSH R1
	MOV R0, var_int_n    
	MOV R1,1
	MOVB [R0],R1           ; Ativa a variavel da passagem pela interrupcao
	POP R1
	POP R0
    RFE
	
	
pr_ar_int:;interrupcao 0
    PUSH R0
	PUSH R1
    MOV R0, var_int_a_p        
	MOV R1, 1
	MOVB [R0], R1          ; Ativa a variavel da passagem pela interrupcao
	POP R1
	POP R0
	RFE


;;;Vai usar o contador aleatorio para ligar ou uma prenda ou uma arma	
arma_prenda:
    PUSH R0
	PUSH R1
	PUSH R2
	MOV R0, var_contador  
    MOVB R1,[R0]           ; Acesso ao contador pela memoria
	MOV R2,3               
	AND R1, R2             ; le os dois ultimos bits
	MOV R2, R1             ; Se esse valor for 0 = prenda, se nao for dá arma (75% Arma, 25%Prenda)
	CMP R2, 0            
	JZ  prend_var 

arma_var:;Escolheu arma	
    MOV R0, alea_a
	MOV R1,1               
	MOVB [R0], R1          ; Liga a variavel que indica que escolheu arma
	JMP fim_armpr

prend_var:;Escolheu prenda
	MOV R0, alea_p       
	MOV R1,1
	MOVB [R0], R1          ; Liga a variavel que indica que escolheu prenda

fim_armpr:
    POP R2
	POP R1
	POP R0
	RET

;;; Verifica se ja existe algum objecto de cima ou algum objecto de baixo em jogo e se não existir onde as colocar	
onde_ar_pr:
    PUSH R0
	PUSH R1
	PUSH R2
	PUSH R3
	PUSH R4
	PUSH R5

baixo_var_c:; verifica se existe um objecto de baixo em jogo
    MOV R3, var_baixo
	MOVB R1, [R3]      
	MOV R2, 0 
	CMP R1, R2              ; Se R1 = 1, entao ja existe uma arma ativa no jogo, se R1=2 então ja existe uma prenda ativa,se R1 = 0 nao existe vai verificar se foi escolhido uma arma para a ativar
	JZ col_ar

cima_var_c:; verifica se existe um objecto de cima em jogo	
	MOV R4,1
	MOV R3, var_cima
	MOVB R1, [R3]          
	MOV R2, 0 
	CMP R1, R2              ; Se R1 = 1, entao ja existe uma arma ativa no jogo, se R1=2 então ja existe uma prenda ativa,se R1 = 0 nao existe vai verificar se foi escolhido uma arma para a ativar
	JZ col_ar
	JMP fin_call            ; fim da rotina

col_ar:;verifica se foi escolhido uma arma na aleatoriedade
    MOV R0, alea_a
    MOVB R1,[R0]            ; verifica se na rotina anterior foi escolhido uma arma
 	MOV R2, 1
	CMP R1,R2
	JZ incr_valor           ; se foi escolhida uma arma entao vai se ativar a arma, se nao vai verificar a prenda

col_bar:;verifica se foi escolhido uma prenda na aleatoriedade
    MOV R0, alea_p
    MOVB R1,[R0]            ; verifica se na rotina anterior foi escolhida uma prenda
    MOV R2, 1
	CMP R1,R2               ; se foi escolhida uma prenda entao vai se ativar a prenda, se nao vai sair da rotina
	JNZ fin_call

inc_var:;vai ativar a prenda
	MOV R0, var_contador_p_a  ; Contador aleatorio 2
	MOVB R1, [R0]
	MOV R2, 1
	AND R1, R2                ; (0 ou 1)
	CMP R1,R4
	JZ incr_valor             ; Vai provocar aleatoriedade onde a prenda é disposta se em cima ou em baixo
	MOV R1,2
	MOVB [R3],R1
	CMP R4,1
	JZ liga_cima

liga_baixo:;Coloca em baixo
	MOV R0, var_liga_baixo
	MOV R1,1
	MOVB [R0],R1
	JMP rotinas

liga_cima:;Coloca em cima
	MOV R0, var_liga_cima
	MOV R1,1
	MOVB [R0],R1
	JMP rotinas

incr_valor:; vai ativar a arma
	MOV R1,1
	MOVB [R3],R1
    CMP R4,1
	JZ liga_cima
	JMP liga_baixo

rotinas:;Colocar prenda ou arma em jogo ou em cima ou em baixo
    CALL onde_obj           ; indica-nos onde colocar os objectos de cima ou de baixo
	CALL n_a_p_pixel        ; apaga e desce o pixel
	MOV R0, var_liga_baixo  ; desativa o apaga e desce o pixel em baixo
	MOV R1,0
	MOVB [R0],R1
	MOV R0, var_liga_cima   ; desativa o apaga e desce o pixel em cima
	MOV R1,0
	MOVB [R0],R1
	JMP cima_var_c

fin_call:
	POP R5
	POP R4
	POP R3
	POP R2
	POP R1
	POP R0
    RET

	
;;;Onde colocar o objecto de cima ou onde colocar o objecto de baixo
onde_obj:
    PUSH R0
	PUSH R1
	PUSH R2
	PUSH R3
	PUSH R4
	MOV R0, var_contador   ; endereco do contador 
    MOVB R1,[R0]           ; R1 - valor do contador
	MOV R2,7               ; Primeiros 3 bits
	AND R1,R2              ; Retira um valor de 0 a 7
	MOV R2, R1             ; Valor a adicionar ou a subtrair da linha
	Bit R1,1               ; Se o bit 1 for 0 jumpa para next
	JZ next
    MOV R4,1               ; R4 - indica que vai diminuir 

next:; funcao que vai verificar se e a o objecto de cima ou o objecto de baixo que sao geradas
	MOV R0, var_cima       ; Verifica se o objecto de cima vai ser adicionada
	MOVB R1, [R0]
	MOV R3,0
	CMP R1,R3
	JNZ  cima_linha
    MOV R0, var_baixo      ; Verifica se o objecto de baixo 
	MOVB R1, [R0]
	MOV R3,0
	CMP R1,R3
	JZ final_col

baixo_linha:; adiciona o endereco da linha de baixo
	MOV R0, lin_i_b
	MOVB R1, [R0]
	JMP addi

cima_linha:; adiciona o endereco da linha da prenda
    MOV R0, lin_i_c
	MOVB R1, [R0]

addi:;Adiciona ao 7 ou ao 21 que esta na linha
    CMP R4,1
	JZ nega                 ; Decrementa
	ADD R1,R2               ; Encrementa
	MOVB [R0], R1
	JMP final_col

nega:; Subtrai ao 7 ou ao 21 que esta na linha
    SUB R1,R2               ; Decrementa
	MOVB [R0], R1

final_col: 	
    POP R4
	POP R3
	POP R2
	POP R1
	POP R0
	RET
	
colisoes:;Verifica se colidiram
   PUSH R0
   PUSH R1
   PUSH R2
   PUSH R3
   PUSH R4
   PUSH R5
   PUSH R6
   PUSH R7
   PUSH R8
   PUSH R9
   PUSH R10
   PUSH R11
   MOV R0, lin_i_b          
   MOVB R2, [R0]              ; R2 - linha onde esta o objecto de baixo 
   MOV R0, lin_i_c
   MOVB R3, [R0]              ; R3 - linha onde esta o objecto de cima
   MOV R0, col_i_b
   MOVB R6, [R0]              ; R6 - coluna onde esta o objecto de baixo
   MOV R0, col_i_c
   MOVB R7, [R0]              ; R7 - coluna onde esta o objecto de cima

; ***************************************************************************
; * Igual em todos os ninjas                                                *
; *  R11 - endereco da linha do ninja                                       *
; *  R1 - linha guardada do ninja                                           *
; *  Verifica se o ninja ja morreu, se sim passa para o outro ninja         *
; *  R5 - Coluna guardada do ninja                                          *
; *  Chama a rotina colidir                                                 *
; *************************************************************************** 

ninja_1_coli:
   MOV R11, linha_nin1
   MOVB R1, [R11]    
   MOV R9, 28
   CMP R1,R9
   JZ ninja_2_coli   
   MOV R0, colun_nin1
   MOVB R5, [R0]
   CALL colidir
   
ninja_2_coli:
   MOV R11, linha_nin2
   MOVB R1, [R11]
   MOV R9, 28
   CMP R1,R9
   JZ ninja_3_coli 
   MOV R0, colun_nin2
   MOVB R5, [R0] 
   CALL colidir
   
ninja_3_coli:
   MOV R11, linha_nin3
   MOVB R1, [R11]
   MOV R9, 28
   CMP R1,R9
   JZ ninja_4_coli 
   MOV R0, colun_nin3
   MOVB R5, [R0]
   CALL colidir
   
ninja_4_coli:
   MOV R11, linha_nin4
   MOVB R1, [R11]
   MOV R9, 28
   CMP R1,R9
   JZ fim_coli
   MOV R0, colun_nin4
   MOVB R5, [R0]
   CALL colidir
   
fim_coli:
   POP R11
   POP R10
   POP R9
   POP R8
   POP R7
   POP R6
   POP R5
   POP R4
   POP R3
   POP R2
   POP R1
   POP R0
   RET
   
   
colidir:
  
baixo_col:
   MOV R9,R6
   ADD R9, 2
   CMP R9,R5
   JLT cima_col    ; Se a coluna final do objecto de baixo for menor que a primeira coluna do ninja entao nao esta a tocar 
   MOV R8,R5
   ADD R8,2
   CMP R6,R8
   JGT cima_col    ; Se a coluna inicial do objecto de baixo for maior que a coluna final do ninja entao nao esta a tocar
   
colisao_baixo_linha: 
   MOV R10,R2
   ADD R10,2
   CMP R10,R1
   JLT cima_col    ; Verifica se a ultima linha do objecto de baixo é menor que a linha inicial do ninja, se for entao nao esta a tocar
   MOV R4, R1
   ADD R4,3
   CMP R2,R4
   JGT cima_col    ; Se a linha inicial do objecto de baixo for maior que a linha final do ninja entao nao esta a tocar

colisao_baixo:     ; Ninja colidiu com o objecto de baixo
   MOV R0, var_destruir
   MOV R9,1
   MOVB [R0], R9   ; Ativa a destruicao do ninja em que o apaga pela ultima vez
   CALL mov_nap_pixel
   MOV R0, lin_i_b
   MOV R9, 21      
   MOVB [R0], R9   ; Reset da linha da arma de baixo
   MOV R0, col_i_b
   MOV R9, 29      ; Reset da coluna da arma de baixo
   MOVB [R0], R9
   MOV R0, var_baixo
   MOVB R9, [R0]
   MOV R4,0        ; Desativa a variavel de baixo
   MOVB [R0],R4
   CMP R9,2        ; Se a variavel era 2, entao era prenda e vai adicionar valor ao display
   JZ  add_display
   CMP R9,1        ; Se a variavel era 1, entao era arma e o ninja com que colidiu vai morrer
   JZ  morrer

cima_col:
   MOV R9,R7 
   ADD R9,2          
   CMP R9, R5
   JLT final_colis    ; Se a coluna final do objecto de cima for menor que a primeira coluna do ninja entao nao esta a colidir
   MOV R8,R5
   ADD R8,2
   CMP R7,R8   
   JGT final_colis    ; Se a coluna inicial do objecto de cima for maior que a ultima coluna do ninja entao nao esta a colidir

colisao_cima_linha:
   MOV R10,R3
   ADD R10,2
   CMP R10,R1
   JLT final_colis    ; Se a linha final do objecto de cima for menor que a linha inicial do ninja entao nao esta a colidir
   MOV R4, R1
   ADD R4,3
   CMP R3,R4
   JGT final_colis    ; Se a linha inicial do objecto de cima for maior que a linha final do ninja entao nao esta a colidir

colisao_cima:         ; Objecto de cima colidiu com o ninja
   MOV R0, var_destruir
   MOV R1,2
   MOVB [R0], R1      ; Vai destruir o objecto de cima
   CALL mov_nap_pixel
   MOV R0, lin_i_c
   MOV R1, 7
   MOVB [R0], R1      ; Reset da linha do objecto de cima
   MOV R0, col_i_c
   MOV R1, 29
   MOVB [R0], R1      ; Reset da coluna do objecto de cima
   MOV R0, var_cima
   MOVB R7, [R0]      ; Se R7 =2  significa que era prenda e vai adicionar pontos ao display se nao era arma e mata o ninja
   MOV R8,0
   MOVB [R0],R8       ; Desativa a variavel do objecto de cima
   CMP R7,2
   JZ  add_display
   CMP R7,1
   JZ  morrer
   JMP final_colis

add_display:
   CALL display       ; Adiciona valor ao display
   JMP final_colis

morrer:
   MOV R0, var_ninja
   MOV R1,0
   MOVB [R0], R1     ; Ativa modificacoes do ninja
   MOV R0, var_apg_nin_colid
   MOV R1,1
   MOVB [R0], R1     ; Permite apagar pela ultima vez o ninja matando - o
   MOVB R1, [R11]    
   MOV R0, lin_i_n
   MOVB [R0], R1     ; Colocar a linha do ninja no determinate do ninja
   MOV R0, col_i_n
   MOVB [R0], R5     ; Colocar a coluna do ninja no determinante do ninja
   CALL mov_nap_pixel
   MOV R2,28
   MOVB [R11],R2     ; Coloca o ninja no final do ecra como morto
   MOV R0, var_ninja
   MOV R1,1
   MOVB [R0], R1     ; Desativa modificacoes do ninja
   JMP final_colis

final_colis:
   RET

; ******************************************************************************************************************************
; * Fim do jogo                                                                                                                *
; * Se todas os ninjas estiverem mortos, entao fim do jogo, ou  se a tecla d foi carregada então fim do jogo                   *
; * Caso aconteca, vai apagar o display e fazer reset a todas as variaveis, e depois coloca o pixel_screen final               *
; * Caso não tenha desativado, pelo botao do teclado, faz-se o reset da variavel para 0 de modo a que o jogo esteja desativado *
; ******************************************************************************************************************************
fimjogo:
  PUSH R0
  PUSH R1
  MOV R0, linha_nin1
  MOVB R1, [R0]
  MOV R2,28
  CMP  R1, R2
  JNZ volta_jogo
  MOV R0, linha_nin2
  MOVB R1, [R0]
  MOV R2,28
  CMP  R1, R2
  JNZ volta_jogo
  MOV R0, linha_nin3
  MOVB R1, [R0]
  MOV R2,28
  CMP  R1, R2
  JNZ volta_jogo
  MOV R0, linha_nin4
  MOVB R1, [R0]
  MOV R2,28
  CMP  R1, R2
  JNZ volta_jogo

final_jogo:
  CALL apagar_display
  CALL pixel_inic_fin
  MOV R0, var_jogo_ativado
  MOV R1,0
  MOVB [R0],R1
  MOV R0,linha_nin1
  MOV R1,12
  MOVB [R0],R1
  MOV R0,linha_nin2
  MOVB [R0],R1
  MOV R0, linha_nin3
  MOVB [R0],R1
  MOV R0, linha_nin4
  MOVB [R0],R1  
  MOV R0, colun_nin1
  MOV R1,0
  MOVB [R0],R1
  MOV R0, colun_nin2
  MOV R1,4
  MOVB [R0],R1
  MOV R0, colun_nin3
  MOV R1,8
  MOVB [R0],R1
  MOV R0, colun_nin4
  MOV R1,12
  MOVB [R0],R1
  MOV R0, lin_i_c
  MOV R1,7
  MOVB [R0],R1
  MOV R0, col_i_c
  MOV R1,29
  MOVB [R0],R1
  MOV R0, lin_i_b
  MOV R1,21
  MOVB [R0],R1
  MOV R0, col_i_b
  MOV R1,29
  MOVB [R0],R1
  MOV R0, var_cima
  MOV R1, 0
  MOVB [R0], R1
  MOV R0, var_baixo
  MOV R1, 0
  MOVB [R0], R1
  MOV R0,var_jogo_ativado
  MOV R1,0
  MOVB [R8],R9
  JMP fim_game

volta_jogo:
  MOV R0, var_jogo_ativado
  MOVB R1, [R0]
  CMP R1,0
  JZ final_jogo

fim_game:
  POP R1
  POP R0
  RET
 
	
display:
   PUSH R0
   PUSH R1
   PUSH R2
   PUSH R3
   PUSH R4
   PUSH R5
   MOV R0, var_reset_display
   MOVB R1, [R0]
   MOV R2,1
   CMP R1,R2
   JZ  display_apagar   ; Caso esteja ativa, faz o reset do display

adicionar_presente:     ; Soma 3 ao valor do display 
    MOV  R0,var_valor_display
	MOVB R3,[R0]
    MOV  R4,3           ; R4 - Valor a ser somado
	Add  R3, R4         ; Adiciona ao valor no display
	JMP  condic 
	
condic:                 ; Condicionantes ao valor do display
	MOV  R4,99          ; R4 - numero maximo que o display pode atingir
	CMP  R3,R4          ; É maior ou igual que 99?
    JGT  disp_fin1
	MOV  R4,0           ; R4 - numero minimo que o display pode atingir
	CMP  R3,R4          ; É maior ou igual que 0?
	JLT  disp_fin2
	JMP  dec_1

dec_1:                  ; Passar o numero a decimal
    MOV R0, var_valor_display
	MOVB [R0],R3
	MOV  R5, R3         
	Mov  R1, R3
    MOV  R4,10	        ; R4 - Usado para obter o valor em decimal
	DIV  R5,R4          ; R5 - Parte inteira em decimal
	MOD  R1,R4          ; R1 - Resto em decimal
	JMP  disp
	
disp:					; Colocar valor no display
    SHL  R5, 4          ; R5 - parte inteira no nibble high(dezenas)
	OR   R5,R1          ; R1 - resto no nibble low(unidades)

addicionar_valor:
	MOV R4, DISPLAYS
	MOVB [R4], R5       ; Valores importados para o display, na memoria(R4)
	JMP  fim_dis
	
disp_fin1:			    ; Valor maximo do display
	MOV  R3,R4          ; R3 fica com o valor 99
	JMP  dec_1
	
disp_fin2:		    	; Valor minimo do display
	MOV  R3,R4          ; R3 fica com o valor 0
	JMP  dec_1
	
display_apagar:
   MOV R5,0            ; Reset do display
   JMP addicionar_valor

fim_dis:
   MOV R1,0
   MOV R0, var_reset_display
   MOVB [R0],R1        ; Desativa o reset do display
   POP R5
   POP R4
   POP R3
   POP R2
   POP R1
   POP R0
   RET
 
;*********************************************************************************************  
; Rotinas essenciais para o funcionamento do programa, usadas em varias partes do programa   *
;*********************************************************************************************

pixel_inic_fin:    ; Coloca no pixel_screen o ecra inicial e o final
   PUSH R0
   PUSH R1
   MOV R0,var_pinta_ecra 
   MOV R1,1
   MOVB [R0],R1   ; Ativa variavel que permite pintar o ecra
   CALL Lig_pixel ; Chama o ligar pixel onde vai colocar
   MOV R0, var_pinta_ecra
   MOV R1,0
   MOVB [R0],R1   ; Desativa variavel que permite pintar o ecra
   POP R1
   POP R0
   RET
   
   
apagar_display:    ; Apaga tudo o que esteja no pixel screen
   PUSH R0
   PUSH R1
   MOV R0,var_apag_ecra
   MOV R1,1
   MOVB [R0],R1    ; Ativa a variavel que permite apagar o ecra 
   CALL Lig_pixel
   MOV R0, var_apag_ecra
   MOV R1,0
   MOVB [R0],R1    ; Desativa a variavel que permite apagar o ecra
   POP R1
   POP R0
   RET

   
verificar_ninjas:
   PUSH R0
   PUSH R1
   PUSH R2
   MOV R0, var_teclado
   MOVB R3, [R0]     ; R3 - valor do teclado
   MOV R2,0
   CMP R3,R2         ; Clicou na tecla 0
   JZ ativa_sobninj1
   MOV R2,1 
   CMP R3,R2         ; Clicou na tecla 1
   JZ ativa_sobninj2
   MOV R2,2
   CMP R3,R2         ; Clicou na tecla 2
   JZ ativa_sobninj3
   MOV R2,3
   CMP R3,R2         ; Clicou na tecla 3
   JZ ativa_sobninj4
   MOV R2,4
   CMP R3,R2         ; Clicou  na tecla 4
   JZ ativa_desninj1
   MOV R2,5
   CMP R3,R2         ; Clicou na tecla 5
   JZ ativa_desninj2
   MOV R2,6
   CMP R3,R2         ; Clicou na tecla 6
   JZ ativa_desninj3
   MOV R2,7          
   CMP R3,R2         ; Clicou na telca 7
   JZ ativa_desninj4
   JMP fim_ninj
   
ativa_sobninj1:      ; Vai subir o ninja 1
   MOV R0, var_ninja
   MOV R1, 0
   MOVB [R0], R1     ; Ativa a modificacao do ninja
   MOV R0, var_qual_ninja
   MOV R1, 1
   MOVB [R0], R1     ;  Ativa ninja 1
   MOV R0, var_sobe_desce
   MOV R1, 1
   MOVB [R0], R1     ; Ativa subida 
   CALL colocar_nin  ; Vai subir o ninja
   CALL reset_atvn   ; Reset das variavei utilizadas
   JMP fim_ninj
   
ativa_sobninj2:     ; Vai subir o ninja 2
   MOV R0, var_ninja  
   MOV R1, 0
   MOVB [R0], R1    ; Ativa a modificacao do ninja
   MOV R0, var_qual_ninja
   MOV R1, 2
   MOVB [R0], R1    ; Ativa ninja 2
   MOV R0, var_sobe_desce
   MOV R1, 1
   MOVB [R0], R1    ; Ativa a variavel subir
   CALL colocar_nin
   CALL reset_atvn  ; Faz reset as variaveis utilizadas
   JMP fim_ninj
   
ativa_sobninj3:     ; Vai subir o ninja 3
   MOV R0, var_ninja
   MOV R1, 0
   MOVB [R0], R1    ; Ativa ninja
   MOV R0, var_qual_ninja
   MOV R1, 3
   MOVB [R0], R1   ; Ativa ninja 3
   MOV R0, var_sobe_desce
   MOV R1, 1
   MOVB [R0], R1  ; Ativa subida
   CALL colocar_nin
   CALL reset_atvn ; Reset das variaveis
   JMP fim_ninj
   
ativa_sobninj4:    ; Ativa subida de ninja 4
   MOV R0, var_ninja 
   MOV R1, 0
   MOVB [R0], R1   ; Ativa modificacao de ninjas
   MOV R0, var_qual_ninja
   MOV R1, 4
   MOVB [R0], R1   ; Ativa ninja 4
   MOV R0, var_sobe_desce
   MOV R1, 1
   MOVB [R0], R1   ; Ativa subida e descida
   CALL colocar_nin
   CALL reset_atvn ; Reset de todas as variaveis
   JMP fim_ninj
   
ativa_desninj1:    ; Ativa a descida do ninja 1
   MOV R0, var_ninja
   MOV R1, 0
   MOVB [R0], R1   ; Ativa mudificacoes do ninja
   MOV R0, var_qual_ninja
   MOV R1, 1
   MOVB [R0], R1   ; Ativa ninja 1
   MOV R0, var_sobe_desce
   MOV R1, 2
   MOVB [R0], R1   ; Ativa a descida
   CALL colocar_nin
   CALL reset_atvn ; Reset das variaveis
   JMP fim_ninj
   
ativa_desninj2: ; Ativa a descida do ninja 2
   MOV R0, var_ninja
   MOV R1, 0
   MOVB [R0], R1  ; Ativa modificacoes do ninja 
   MOV R0, var_qual_ninja
   MOV R1, 2
   MOVB [R0], R1  ; Ativa ninja 2
   MOV R0, var_sobe_desce
   MOV R1, 2
   MOVB [R0], R1  ; Ativa descida do ninja 2
   CALL colocar_nin
   CALL reset_atvn ; Faz reset das variaveis usadas
   JMP fim_ninj
   
ativa_desninj3: ; Ativa a descida do ninja 3
   MOV R0, var_ninja
   MOV R1, 0
   MOVB [R0], R1   ; Ativa modificacoes do ninja
   MOV R0, var_qual_ninja
   MOV R1, 3
   MOVB [R0], R1   ; Ativa ninja 3
   MOV R0, var_sobe_desce
   MOV R1, 2
   MOVB [R0], R1   ; Ativa descida do ninja
   CALL colocar_nin
   CALL reset_atvn ; Faz reset das variaveis utilizadas
   JMP fim_ninj
   
ativa_desninj4: ; Ativa a descida do ninja 4
   MOV R0, var_ninja
   MOV R1, 0
   MOVB [R0], R1 ; Ativa a modificacao do ninja
   MOV R0, var_qual_ninja
   MOV R1, 4
   MOVB [R0], R1 ; Ativa ninja 4
   MOV R0, var_sobe_desce
   MOV R1, 2
   MOVB [R0], R1 ; Ativa descida
   CALL colocar_nin
   CALL reset_atvn  ; Reset das variaveis utilizadas
   
fim_ninj:
    POP R2
	POP R1
    POP R0
	RET 
	
colocar_nin:
  PUSH R0
  PUSH R1
  PUSH R2
  PUSH R3
  PUSH R4
  PUSH R5
  PUSH R6
  PUSH R7
  MOV R0, var_qual_ninja   ; Verifica qual o ninja a ser escolhido, se vier do teclado, caso contrario faz todos
  MOVB R1, [R0]
  CMP R1,1
  JZ  ninja_1
  CMP R1,2
  JZ  ninja_2
  CMP R1,3
  JZ  ninja_3
  CMP R1,4
  JZ  ninja_4

; ***************************************************************************
; * Igual em todos os ninjas                                                *
; *  R1 - linha guardada do ninja                                           *
; *  R2 - adiciona ao determinador da linha do ninja, o ninja a colocar     *
; *  R4 - coluna guardada do ninja                                          *
; *  R5 - adiciona ao determinador de coluna do ninja o ninja a colocar     *
; *  Chama rotina que escolhe as variantes possiveis                        *
; *  Desativa a variavel que sobe ou desce o ninja                          *
; *************************************************************************** 

ninja_1:
  MOV R0, linha_nin1
  MOVB R1, [R0]             
  MOV R2, lin_i_n
  MOVB [R2], R1             
  MOV R3, colun_nin1
  MOVB R4, [R3]              
  MOV  R5, col_i_n
  MOVB [R5], R4		         
  CALL escolhe_variantes
  MOV R0, var_sobe_desce
  MOVB R1, [R0]
  CMP R1,0
  JNZ fim_voluntario

ninja_2:
  MOV R0, linha_nin2
  MOVB R1, [R0]
  MOV R2, lin_i_n
  MOVB [R2], R1
  MOV R3, colun_nin2
  MOVB R4, [R3]
  MOV  R5, col_i_n
  MOVB [R5], R4		
  CALL escolhe_variantes
  MOV R0, var_sobe_desce
  MOVB R1, [R0]
  CMP R1,0
  JNZ fim_voluntario

ninja_3:
  MOV R0, linha_nin3
  MOVB R1, [R0]
  MOV R2, lin_i_n
  MOVB [R2], R1
  MOV R3, colun_nin3
  MOVB R4, [R3]
  MOV  R5, col_i_n
  MOVB [R5], R4		
  CALL escolhe_variantes
  MOV R0, var_sobe_desce
  MOVB R1, [R0]
  CMP R1,0
  JNZ fim_voluntario

ninja_4:
  MOV R0, linha_nin4
  MOVB R1, [R0]
  MOV R2, lin_i_n
  MOVB [R2], R1
  MOV R3, colun_nin4
  MOVB R4, [R3]
  MOV  R5, col_i_n
  MOVB [R5], R4		
  CALL escolhe_variantes

fim_voluntario:
  POP R7
  POP R6
  POP R5
  POP R4
  POP R3
  POP R2
  POP R1
  POP R0
  RET
  
  
escolhe_variantes: 
  PUSH R6
  PUSH R7  
  MOV R6, var_colo_desc_nin 
  MOVB R7, [R6]
  CMP R7,1                   ; Caso esta variavel esteja a 1, vai colocar o ninja no ecra
  JZ coloc_pix
  CMP R7,2                   ; Vai  descer o ninja
  JZ descida_aut
  MOV R6, var_sobe_desce
  MOVB R7, [R6] 
  CMP R7,1                   ; Verifica se o ninja vai subir ou descer pelo teclado, se estiver a 1 - sobe
  JZ sob_pix
  CMP R7,2                   ; Se estiver a 2 - desce
  JZ descida_aut

sob_pix:
  MOV R6, lin_i_n
  MOVB R7, [R6]              
  CMP R7, 0                 ; Caso o ninja a subir ja esteja na primeira linha nao faz nada 
  JZ fim_call
  MOV R6,28 
  CMP R7,R6
  JZ fim_call               ; Caso esteja na ultima linha significa que esta morto
  MOV R6, var_sobe_ninja
  MOV R7,1
  MOVB [R6], R7             ; Ativa a variavel que faz subir o ninja
  CALL mov_nap_pixel
  MOV R6, var_sobe_ninja    
  MOV R7,0
  MOVB [R6], R7             ; Desativa a variavel que faz subir o ninja
  CALL insert_ninja         ; Coloca as novas coordenadas nos respetivos sitios em memoria
  JMP fim_call

coloc_pix:
  CALL n_a_p_pixel          ; Vai colocar o ninja
  CALL insert_ninja         ; Coloca as novas coordenadas nos respetivos sitios em memoria
  JMP fim_call

descida_aut:
  CALL mov_nap_pixel       ; Desce o ninja
  CALL insert_ninja        ; Coloca as novas coordenadas nos respetivos sitios em memoria
  
fim_call:
  POP R7
  POP R6
  RET

  
insert_ninja: ;Guarda os novos valores nas variaveis inicializadas no colocar_nin
  MOVB R1, [R2]
  MOVB [R0], R1
  MOVB R1, [R5]
  MOVB [R3], R1
  RET
 
 
reset_atvn:       ; Reset de todas as variaveis usadas
   PUSH R0
   PUSH R1
   MOV R0, var_ninja
   MOV R1, 1
   MOVB [R0], R1   ; Desativa o ninja
   MOV R0, var_qual_ninja
   MOV R1, 0
   MOVB [R0], R1   ; Desativa qual o ninja a colocar
   MOV R0, var_sobe_desce
   MOV R1, 0
   MOVB [R0], R1   ; Desativa o sobe e desce
   POP R1
   POP R0
   RET
   
;;; Rotina que escolhe se é ninja, objecto de cima ou objecto de baixo e verifica as suas linhas e colunas
n_a_p_pixel:
    PUSH R0
	PUSH R1
    PUSH R2
	PUSH R3
	PUSH R4
	PUSH R5
	PUSH R6
	PUSH R7
	PUSH R8
	PUSH R9
	PUSH R10
	PUSH R11
	MOV R0, var_ninja       
	MOVB R1, [R0]
	CMP R1,0                ; Verifica se o ninja está ativo
	JZ nin
    MOV R0,var_liga_cima
	MOVB R1, [R0]           ; Verifica se a variavel que liga o objecto de cima está ativada
	CMP R1, 1
	JZ CIMA
    MOV R0,var_liga_baixo
	MOVB R1, [R0]           ; Verifica se a variavel que liga o objecto de baixo está ativada
	CMP R1, 1
	JZ BAIXO
	
CIMA:
	MOV R0, var_cima   
	MOVB R1, [R0]
	CMP R1,0                ; Verifica se a variavel do objecto está ativa, se estiver vai verificar se é arma
	JNZ var_c_arma

BAIXO:
	MOV R0, var_baixo  
	MOVB R1, [R0]
	CMP R1,0                ; Verifica se a variavel do objecto está ativa, se estiver vai verificar se é arma
	JZ fim_ninja

var_b_arma:
    CMP R1,1                ; Se R1 = 1 entao é arma, que vai ser adicionada na parte de baixo, se R1 = 2, é prenda
	JZ arm_b
	JMP prenda_b

var_c_arma:
    CMP R1,1
	JZ baixo_linhac              ; Se R1 = 1 entao é arma, que vai ser adicionada na parte de cima, se R1 = 2, é prenda
	JMP cima_var_cc    

;;; Vai retirar a linha de cima , a coluna de cima, e a forma da prenda, para registos, vai tambem colocar num registo se é para apagar a prenda ou para colocar
cima_var_cc:
    MOV R2, lin_i_c         
	MOV R5, col_i_c
	MOV R6, Prenda
	MOV R0, var_apagar_cima
	MOVB R9, [R0]
	JMP Coord_n_a_p

;;; Vai retirar a linha de baixo , a coluna de baixo, e a forma da prenda, para registos, vai tambem colocar num registo se é para apagar a prenda ou para colocar
prenda_b:
    MOV R2, lin_i_b         
	MOV R5, col_i_b
	MOV R6, Prenda
	MOV R0, var_apagar_baixo
	MOVB R9, [R0]
	JMP Coord_n_a_p

;;; Vai retirar os endereços da linha de baixo, da coluna de baixo, e da forma da armaa, para registos, vai tambem colocar num registo se é para apagar a arma ou para a colocar no pixel screen
arm_b:
    MOV R2, lin_i_b
	MOV R5, col_i_b
	MOV R6, Arma
    MOV R0, var_apagar_baixo
	MOVB R9, [R0]
	JMP Coord_n_a_p

;;; Vai retirar os endereços da linha de cima, da coluna de cima, e da forma da arma, para registos, vai tambem colocar num registo se é para apagar a arma ou para a colocar no pixel screen
baixo_linhac:
    MOV R2, lin_i_c
	MOV R5, col_i_c
	MOV R6, Arma
    MOV R0, var_apagar_cima
	MOVB R9, [R0]
	JMP Coord_n_a_p
	
;;; Vai retirar a linha, a coluna, e a forma do ninja , para registos, vai tambem colocar num registo se é para apagar o ninja ou para colocar
nin:
    MOV R2, lin_i_n
	MOV R6, Ninja
	MOV R5, col_i_n
    MOV R0, var_apagar_nin
	MOVB R9, [R0]

;;; Obtem os valores e coloca-os em registos
Coord_n_a_p:
	MOVB R0, [R2]          ; R0 - numero da linha
	MOVB R1, [R5]          ; R1 - numero da coluna
	MOV R8, R1             ; R8 - Garantia da linha inicial
	MOVB R3, [R6]	       ; R3 - largura do objecto
	ADD R6, 1              ; R6 - segundo endereço do objecto
	MOVB R4, [R6]          ; R4 - tamanho do objecto
    MOV R11, 1             ; R11 - contador de colunas
    MOV R10, 1	           ; R10 - contador de Linhas
	
;;;  Verifica se o o bit é 0 ou 1 de forma a saber se vai pintar ou não esse pixel
PIXEL_LINHA:
    ADD R6,1               ; R6 - Proximo endereco do objecto (parte para colocar no pixel screen)
	MOVB R7,[R6]           ; Adiciona o bit do ninja ao R7
    CMP R7,0               ; Se R7 = 0 então passa para o proximo, ou seja nao vai pintar o pixel
	JZ muda_col            ; Muda logo para a proxima coluna 
    Call Lig_pixel         ; Chama a rotina que adiciona o pixel

;;; Faz a mudanca de coluna e se está tiver na ultima coluna vai para muda_linh
muda_col:
    CMP R11,R3             ; Verifica se a coluna já chegou ao fim, e se chegar vai mudar de linha        
	JZ  muda_linh
    ADD R11,1              ; Adiciona-se ao contador de colunas 1
	ADD R1,1               ; Passa para a proxima coluna
	JMP PIXEL_LINHA  

;;; Faz o reset da coluna e verifica se está na ultima linha	
muda_linh:
	MOV R11,1              ; Reset do Contador de colunas
    Mov R1,R8              ; Reset da coluna
	CMP R10,R4             ; Caso esteja na ultima linha o programa termina
	JZ  fim_ninja           
	ADD R10,1              ; Acrescenta um valor ao contador de linhas
	ADD R0,1               ; Proxima linha
	JMP PIXEL_LINHA  

;;; Acaba o apagamento ou a colocaçao dos pixeis no pixel_screen
fim_ninja:
    MOV R2, lin_i_n       
    MOVB R0, [R2]          ; Reset da linha
	MOV R2, col_i_n        
    MOVB R1, [R2]          ; Reset da coluna
	POP R11
	POP R10
	POP R9
    POP R8
    POP R7
	POP R6
	POP R5
	POP R4
	POP R3
	POP R2
	POP R1
	POP R0
	RET


;;; Rotina que apaga ou coloca os pixeis no pixel_screen
Lig_pixel:
    Push R2
	Push R3
	Push R4
	Push R5
	Push R6
    MOV R2,var_pinta_ecra
	MOVB R6, [R2]
	CMP  R6,1
	JZ  reset_pix          ; Caso esteja ativo vai inicializar a linha e a coluna a 0
	MOV R2, var_apag_ecra
	MOVB R6, [R2]          ; Caso esteja ativo vai inicializar a linha e a coluna a 0
	CMP R6,1
	JNZ voltar_pixel

reset_pix:
    MOV R0,0
	MOV R1,0

voltar_pixel:
	MOV R2, 8               
	MOV R3, R1
	Div R3 , R2             ; R3 = Coluna/8
	Mov R2, 4 
    MOV R4, R0	
	Mul R4 , R2             ; R4 = Linha*4
	ADD R3, R4              ; R3 - numero do byte
	MOV R2, PIXSCRN         ; R2 - endereço onde comeca o pixel screen
    ADD R3, R2              ; R3 - Numero do byte no pixel Screen
	MOV R2,var_pinta_ecra
	MOVB R6, [R2]
	CMP  R6,1
	JZ  ecra_inicial_final
	MOV R2, var_apag_ecra
	MOVB R6, [R2]
	CMP R6,1
	JZ  ecra_apagar
	MOV R2, 8 
    MOV R4, R1	
	MOD R4, R2             ; R4 - Resto da coluna que nos vai indicar o lugar do bit
	MOV R5, 80h            ; R5 - Comeca no 10000000b pois funciona ao contrario
	
Mascara:	
	CMP R4,0               ; Verifica se ja encontrou o pixel a marcar
	JZ  pixel_screen
	SHR R5,1               ; Passa para o bit seguinte
	SUB R4,1               ; Subtrai o numero de vezes ate chegar ao bit
	JMP Mascara

pixel_screen:	
	CMP R9,1               ; R9 - Se for um quer dizer que foi ativado a limpeza do ninja
	JZ limpa_screen
	MOVB R2,[R3]          
	OR R5, R2              ; Adiciona o bit ao que já lá está
    MOVB [R3],R5	       ; Liga o bit no pixel screen
    JMP fim_pixel	

limpa_screen:
    MovB R2,[R3]
	NOT R5                 ; Inverte           
	AND R5, R2             ; Apaga o bit
    MOVB [R3],R5	       ; Desliga o bit no pixel screen
    JMP fim_pixel

ecra_inicial_final:
	MOV R6, 2  
    MOV R7,R0	            ; R7 - Linha par ou linha impar
	MOD R7,R6               ; Se for impar adiciona 10101010b
	JNZ pix_imp
	MOV R2, Pixeis_Pares    ; Se for par adiciona 01010101b
	JMP pix_byte

pix_imp:                    ; Coloca a mascara impar
     MOV R2, Pixeis_Impares
	 JMP pix_byte

ecra_apagar:               ; Coloca a mascara a 0
	 MOV R2, Pixeis_apagados

pix_byte:
    MOVB [R3],R2            ; Adiciona a máscara ao pixelscreen
    MOV R6,8	            ; Passa para o byte seguinte  
	Add R1,R6
	Mov R6,32               ; Verifica se a coluna acabou 
	CMP R1,R6               ; Coluna ja chegou ao fim?
	JZ  verificar_colu
	JMP voltar_pixel

verificar_colu:            ; Verifica se o programa vai terminar ou se vai passar para a proxima linha
    CMP R0,R6              ; Verifica se a linha chegou ao fim
	JZ  fim_pixel          ; Se sim o pixel inicial esta feito
	MOV R1,0               ; Reset das colunas
	Add R0,1               ; Proxima linha
	JMP voltar_pixel   

fim_pixel:
    POP R6	
	POP R5
	POP R4
	POP R3
	POP R2
	RET


;;;Rotina que pode descer a linha do ninja, , mover para a esquerda a coluna do objecto de cima ou do objecto de baixo e ativar o apagar de cada um deles
mov_nap_pixel:
    PUSH R0
	PUSH R1
	PUSH R2
	PUSH R3
	PUSH R4
	PUSH R6
	MOV R6,1               ; R6 = 1

ninja_ap:
    MOV R0, var_ninja      
	MOVB R1, [R0]          ; Vai verificar o ninja está ativo
    CMP R1, R6             ; Ninja esta ativo? = 0
	JNZ ninja_pr   	

baixo_ap:	
	MOV R0, var_baixo      
	MOVB R1, [R0]         ; Vai verificar se o objecto de baixo está ativo
	CMP R1,0              ; baixo esta ativa? = 1
	JNZ baixo_pr

cima_ap:
    MOV R0, var_cima
	MOVB R1, [R0]         ; Vai verificar se o objecto de cima está ativo
	CMP R1, 0             ; objecto de baixo esta ativa? = 1
	JNZ cima_pr
	JMP fim_de_rot

baixo_pr:;Verifica se vai apagar ou andar para o lado

apagar_baixo:
	MOV R0, var_apagar_baixo       
	MOV R1,1               
	MOVB [R0],R1           ; Ativa apagar objecto de baixo
	MOV R0,var_liga_baixo
	MOV R1,1
	MOVB [R0],R1           ; Permite que apague no pixel screen
	CALL n_a_p_pixel       ; Chama a rotina para apagar o pixel
	MOV R0, var_apagar_baixo 
	MOV R1, 0
	MOVB [R0], R1
	MOV R0, var_destruir
	MOVB R1, [R0]
	CMP R1,1
	JZ fim_de_rot          ; Caso esteja ativo apenas apaga o objecto de baixo

andar_baixo:
    MOV R0, col_i_b             
	MOVB R1, [R0]          ; R1 - Coluna do objecto de baixo
	MOV R2,0           
	CMP R1,R2              ; Se a coluna for igual a 0, significa que o objecto de baixo bateu na parede
	JZ desat_baixo             
	SUB R1,1               ; Anda uma coluna para a esquerda
	MOVB [R0], R1          
	Call n_a_p_pixel
	MOV R0,var_liga_baixo
	MOV R1, 0
	MOVB [R0],R1           ; Desativa a variavel que permite modificacoes do objecto de baixo no pixel screen
	JMP cima_ap
cima_pr:;Verifica se vai apagar ou andar para o lado o objecto de cima

apagar_cima:; Vai apagar o objecto de cima do pixel_screen
	MOV R0, var_apagar_cima    
	MOV R1,1               
	MOVB [R0],R1            ; Ativa apagar objecto de cima
	MOV R0,var_liga_cima
	MOV R1,1
	MOVB [R0],R1           ; ligado, permite que se apague o objecto de cima do pixel_screen
	Call n_a_p_pixel       ; Chama a rotina para apagar o pixel
	MOV R0, var_apagar_cima
	MOV R1, 0
    MOVB [R0], R1          ; Desativa apagar o objecto de cima
    MOV R0, var_destruir
	MOVB R1, [R0]
	CMP R1,2
	JZ fim_de_rot          ; Caso esteja ativo apenas apaga o objecto de cima

andar_cima:; O objecto de cima vai andar para a esquerda
    MOV R0, col_i_c
	MOVB R1, [R0]          ; Coluna do objecto de cima
	MOV R2,0
	CMP R1,R2              ; Verifica se o objecto de cima já bateu na parede
	JZ desat_cima
	SUB R1,1               ; Anda uma coluna para a esquerda
	MOVB [R0], R1          
	Call n_a_p_pixel       ; Colocar o objecto de cima no pixel screen
	MOV R0,var_liga_cima
	MOV R1,0
	MOVB [R0],R1           ; desativa a variavel que permite modificações do pixel screen
	JMP fim_de_rot
ninja_pr:; Indica se vai apagar o ninja ou descer

apagar_nin:; Vai apagar o ninja do pixel_screen
	MOV R0, var_apagar_nin   
	MOV R1,1
	MOVB [R0], R1          ;Ativa a variavel que apaga o ninja
	CALL n_a_p_pixel  
	MOV R0, var_apagar_nin
	MOV R1, 0
    MOVB [R0], R1          ; Desativa o apagar ninja
	MOV R0, var_apg_nin_colid
	MOVB R1, [R0]
	CMP R1,1
	JZ fim_de_rot          ; Caso esteja ativa apenas apga o ninja

andar_ninja:; O ninja vai descer
	MOV R1, lin_i_n        
	MOVB R0, [R1]          ; Numero da linha atual
	MOV R2,28              
	CMP R0,R2              ; Se já chegou ao fim do pixel screen, morre
	JZ fim_de_rot
	MOV R3, var_sobe_ninja
	MOVB R4, [R3]
	CMP R4,1
	JZ six_nin
	ADD R0, 1              ; Linha seguinte
	JMP colocar_ecra

six_nin:
    SUB R0,1

colocar_ecra:
	MOVB [R1],R0           ; Coloca o valor da linha em memoria
	Call n_a_p_pixel       ; chama a rotina para descer o pixel
	JMP fim_de_rot

desat_baixo:; Vai desativar o objecto de baixo
    MOV R2,29
    MOVB [R0], R2          ; Reset da coluna do objecto de baixo
	MOV R0, alea_a
	MOV R1,0
	MOVB [R0],R1           ; Coloca a aleatoriedade da prenda a 0
	MOV R0, alea_p
	MOVB [R0],R1           ; Coloca a aleatoriedade da arma a 0
    MOV R0,lin_i_b
    MOVB R1, [R0]         
   	MOV R2,21
	MOVB [R0], R2          ; Reset da linha do objecto de baixo
	MOV R0, var_baixo
	MOV R1,0
    MOVB [R0],R1           ; Desativa o objecto de baixo
    JMP cima_ap

desat_cima:;Vai desativar o objecto de cima
    MOV R2,29   
    MOVB [R0], R2          ; Reset da coluna do objecto de cima
	MOV R0, alea_a
	MOV R1,0
	MOVB [R0],R1
	MOV R0, alea_p
	MOVB [R0],R1
	MOV R0, lin_i_c
	MOVB R1, [R0]
   	MOV R2,7 
	MOVB [R0], R2          ; Reset da linha do objecto de cima
	MOV R0, var_cima
	MOV R1,0
    MOVB [R0],R1           ; Desativa o objecto de cima
	JMP fim_de_rot

fim_de_rot:
    MOV R0, var_apg_nin_colid
	MOV R1,0
	MOVB [R0], R1
	MOV R0, var_destruir
    MOVB [R0], R1
	POP R6
	POP R4
	POP R3
	POP R2
	POP R1
	POP R0
	RET


	




