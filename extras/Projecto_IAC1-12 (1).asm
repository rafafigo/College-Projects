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
pilha:      TABLE 100H        ; Espaço reservado para a pilha 
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
lin_i_n: STRING 0           ; Linha inicial do ninja
col_i_n: STRING 0           ; Coluna inicial do ninja
lin_i_c: STRING 7           ; Linha inicial de cima
col_i_c: STRING 29          ; Coluna inicial de cima
lin_i_b: STRING 21          ; Linha inicial de baixo
col_i_b: STRING 29          ; Coluna inicial de baixo

;;; Variáveis que na rotina liga pixel, permitem pintar o ecra, e apagá-lo
var_pinta_ecra: STRING 0    ; Utilizada para ativar o pixel screen final ou inicial
var_apag_ecra:  STRING 0    ; Utilizada para apagar tudo o que esteja no pixel screen

;;; Diferentes variáveis que ajudam a guardar a informação necessária para que o jogo funcione
var_contador:     STRING 0    ; Contador que vai ser utilizado para indicar as posições dos objectos no pixel screen e se é prenda ou arma
var_contador_p_a: STRING 0    ; Contador que vai dizer a disposição da prenda se em cima ou em baixo

;;; Os var_ints são usados como garantia de que passa pelas interrupções
var_int_n:         STRING 0       ; Verifica que passou pela interrupção1
var_int_a_p:       STRING 0       ; Verifica que passou pela interrupção0

;;; Quando ativados permitem apagar os pixeis e não ligar pixeis
var_apagar_nin:      STRING 0     ; quando ligada permite que o pixel screen apague os pixeis e não coloque o ninja
var_apagar_cima:     STRING 0     ; quando ligada permite que o pixel screen apague os pixeis e não coloque do objeto de cima
var_apagar_baixo:    STRING 0     ; quando ligada permite que o pixel screen apague os pixeis e não coloque do objeto de baixo

;;; Verificam se o ninja, o objeto de cima e o objeto de baixo estão ativos ou estão inativos
var_ninja:      STRING 0      ; 0 - ninja ativo, 1 - ninja inativo
var_baixo:      STRING 0      ; 0 - objecto baixo inativo, 1 - objecto de baixo ativo
var_cima:       STRING 0      ; 0 - objecto de cima inativo, 1 - objecto de cima ativo

;;; Diz-nos se do contador, foi escolhida uma arma ou uma prenda
alea_a:     STRING 0      ; 1 - escolhe a arma da aleatoriedade     
alea_p:     STRING 0      ; 1 - escolhe a prenda da aleatoriedade

;;;  Liga as permissões para apagar ou ligar os pixeis no pixel screen dos objetos de cima ou de baixo
var_liga_cima:  STRING 0   
var_liga_baixo: STRING 0

;;; Variáveis que apagam pela ultima vez o ninja, a arma e a prenda atual 
var_apg_nin_colid: STRING 0
var_destruir:      STRING 0

;;; Variáveis que ativam, desativam e pausam o jogo
var_jogo_ativado: STRING 0
var_pausa_jogo:   STRING 0

;;; Variáveis extras importantes para o funcionamento do jogo
var_valor_display: STRING 0     ; Valor guardado que está no display
var_teclado:       STRING 0		; Valor indicado pelo teclado	
var_reset_display: STRING 0	    ; Quando ligado faz reset do display		
var_qual_ninja:    STRING 0     ; Indica qual o ninja que vai subir ou descer com o teclado
var_sobe_desce:    STRING 0     ; Indica se o ninja sobe ou desce
var_sobe_ninja:    STRING 0     ; Ativa a subida do ninja
var_colo_desc_nin: STRING 0     ; Quando está a 1 - coloca no ecrã, a 2 - desce o ninja

;;; Forma da Prenda, Arma e Ninja
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

;;;Inicializações
principio:
   MOV SP, SP_inicial      ; Inicializa SP para a palavra a seguir
                            ; À última da pilha
   MOV BTE, tab            ; Inicializa BTE

;;; Ciclo principal do programa
Ciclo:
   CALL keyboard			; keyboard - descobre tecla e função
   CALL dec_nin            ; dec_nin  - gravidade
   CALL esc_arm_nin        ; randomizer arma/prenda
   JMP Ciclo


; ***************************************************************************
; * Rotina que primeiramente vai verificar se o jogo está ativo, e apenas   *
; * sai da rotina se estiver, e verifica se alguma das teclas estão primidas*
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
   MOV  R5, 0	       ; R5 - contador de coluna
	
	
tecla:				   ; Scan da linha e coluna
   MOV  R2, TEC_LIN
   MOV  R3, TEC_COL
   MOVB [R2], R1       ; Atribui o número da linha ao periférico de linha  
   MOVB R0, [R3]       ; Adquire a coluna do periférico de coluna
   MOV  R4, 0Fh
   AND  R0,R4
   CMP  R0, 0          ; Sem valor?
   JNZ  ha_tecla       ; Guarda até largar a tecla
   SHL  R0, 1          ; Coluna seguinte
   MOV  R4, 8          ; R4 - linha 8
   CMP  R1 , R4        ; Completou as linhas?
   JNZ  aument_linha
   MOV	R1, 1          ; Reinicia linha
   MOV  R8, var_pausa_jogo
   MOVB R9, [R8]       ; R9 - verifica se a pausa está ativa, se estiver permanece no teclado até não estar
   CMP  R9, R1           
   JZ   tecla
   MOV  R8, var_jogo_ativado
   MOVB R9, [R8]       ; R9 - verifica se o jogo já está ativo, se não estiver permanece no teclado
   CMP  R9,0
   JZ   tecla
   JMP  fim_tec	       ; Se não foi clicado, então sai da rotina do teclado	
	
	
aument_linha:		   ; Scan da linha seguinte 
   SHL  R1,1           ; Linha seguinte
   JMP  tecla          ; Reinicia
	
	
ha_tecla:              ; Segurar tecla
   MOVB [R2],R1        ; A linha é adicionada ao periférico da linha
   MOVB R6,[R3]        ; R6 - adquire o valor do periférico da coluna
   MOV  R4, 0Fh
   AND  R6,R4
   CMP  R6,0           ; Botão largado
   JNZ  ha_tecla     
   JMP  num
	
	
num:
   CMP  R0,1           ; Verifica se R0 está na primeira coluna
   JNZ  adici          ; Vai guardar um bit da coluna
   CMP  R1,1           ; Verifica se ja está no 1
   JNZ  adici_2        ; Vai guardar um bit da linha
   JMP  valr           ; Se já estiverem os dois obtem-se o número do teclado
	
	
adici:
   SHR  R0,1           ; Anda um bit para o lado da coluna
   ADD  R5,1           ; R5 - contador de colunas 
   JMP  num
	
	
adici_2:
   SHR  R1,1           ; Um bit para o lado da linha
   ADD  R7,1           ; R7 - contador de linhas
   JMP  num

	
valr:	                ; Binário para Hexa
   MOV  R4,4
   MUL  R7,R4          
   ADD  R7,R5			; R7 - valor do teclado da linha e coluna "clicada"
   MOV  R8, var_teclado
   MOVB [R8],R7

op_tec:                  ; Operações das teclas
   MOV  R9, R7
   MOV  R10,12            
   CMP  R9,R10            
   JZ   preparar_jogo    ; Clicou na tecla c
   MOV  R8, var_jogo_ativado
   MOVB R9, [R8]
   CMP  R9,1             ; Vai manter-se no teclado até o jogo estiver ativo
   JNZ  inicio
   MOV  R9,R7
   MOV  R10,13            
   CMP  R9,R10
   JZ   loop_pausa       ; Clicou na tecla d
   MOV  R8, var_pausa_jogo 
   MOVB R9, [R8]
   CMP  R9,1             ; Se estiver em pausa vai se manter até a variável pausa se desligar             
   JZ   inicio
   MOV  R9, R7
   MOV  R10,14           ; Clicou na tecla d
   CMP  R9,R10            
   JZ   terminar_jogo    
   CALL verificar_ninjas ; Vai verificar se foi clicada alguma das teclas que sobem ou descem o ninja
   JMP  fim_tec
   
preparar_jogo:      
   CALL apagar_display   ; Rotina que apaga o display
   CALL pixel_inic_fin   ; Rotina que coloca o ecrã inicial
   CALL contar_inicio    ; Faz uma contagem antes de apagar
   
   
continua_jogo:           ; Ativação das diversas variáveis e colocação dos ninjas no pixel screen
   CALL apagar_display   ; Apaga o display
   MOV  R8, var_jogo_ativado
   MOV  R9,1
   MOVB [R8], R9         ; Jogo ativo
   MOV  R8,var_reset_display
   MOVB [R8], R9         ; Ativa reset do display
   CALL display          ; Chama a rotina que faz o reset do display, colocando-o a zero
   MOV  R8,var_reset_display
   MOV  R9,0
   MOVB [R8], R9         ; Desativa o reset do display
   MOV  R8, var_ninja
   MOV  R9,0              
   MOVB [R8], R9         ; Ativa a colocação dos ninjas
   MOV  R8, var_colo_desc_nin
   MOV  R9,1    
   MOVB [R8], R9         ; Coloca a 1 a variável que indica que vai colocar o ninja e não descer na rotina seguinte
   CALL colocar_nin
   MOV  R9,0
   MOVB [R8],R9          ; Desativa
   MOV  R9,1
   MOV  R8, var_ninja      
   MOVB [R8], R9         ; Desativa a colocação dos ninjas
   JMP  fim_tec

terminar_jogo:
   MOV  R8, var_jogo_ativado ; Desativa jogo
   MOV  R9, 0
   MOVB [R8],R9             
   CALL fimjogo              ; Chama rotina que faz o fim do jogo
   JMP  inicio

   
loop_pausa:
   MOV  R8, var_pausa_jogo 
   MOVB R9, [R8]
   ADD  R9,1                 ;Adiciona 1 a pausa do jogo, se for 2 significa que já tinha sido clicado na pausa, logo está a sair da pausa
   CMP  R9,2
   JZ   voltar_jogo

cont_pausa:                
   MOV  R10,1
   MOVB [R8],R10            ; Coloca 1 na pausa
   JMP  inicio

voltar_jogo:
   MOV  R10,0 
   MOVB [R8],R10            ; Reset da variável 

fim_tec:
   MOV  R7,0
   MOV  R8, var_teclado
   MOVB [R8],R7   
   POP  R10
   POP  R9
   POP  R8
   POP  R7
   POP  R6
   POP  R5
   POP  R4
   POP  R3
   POP  R2
   POP  R1
   POP  R0
   RET

	
contar_inicio:	            ; Apenas faz tempo para apagar o pixel screen inicial
   PUSH R0
   PUSH R1
   MOV  R8,0
   MOV  R9,10000
   
contar:
   CMP  R8, R9
   JZ   final_contar
   ADD  R8, 1
   JMP  contar
   
final_contar:
   POP  R1
   POP  R0
   RET


;;; Rotina que faz descer o ninja segundo a interrupção	
dec_nin:                    ; Interrupção 1 - referente ao ninja    
   PUSH R0
   PUSH R1
   MOV  R0, var_contador        
   MOVB R1,[R0]             ; R1 - contador aleatório
   ADD  R1,1                ; Adiciona 1 ao contador, que irá assim provocar a aleatoriedade
   MOVB [R0],R1
   EI1                      ; Ativa interrupção 1
   EI
   MOV  R0, var_int_n       
   MOVB R1, [R0]            ; R1 = 0 não passou pela interrupção R1 = 1 passou pela interrupção       
   CMP  R1,0                ; R1 = 0 vai esperar que passe pela interrupção R1 = 1 vai para o passo seguinte
   JZ   nin_ret
   MOV  R0, var_ninja       
   MOV  R1,0                
   MOVB [R0],R1             ; Ativa o ninja
   MOV  R0, var_colo_desc_nin 
   MOV  R1,2
   MOVB [R0], R1            ; Provoca a descida automática
   CALL colocar_nin
   MOV  R1,0
   MOVB [R0], R1            ; Guarda em memória o novo valor
   MOV  R0, var_ninja
   MOV  R1,1
   MOVB [R0], R1            ; Desativa o ninja
   MOV  R0, var_int_n       
   MOV  R1,0                 
   MOVB [R0], R1   
	
nin_ret:
   POP  R1
   POP  R0
   RET

	
;;; Rotina que escolhe por aleatoriedade a prenda ou a arma e coloca-os no pixel screen numa dada posição também aleatória e trata ainda do seu movimento
esc_arm_nin:
   PUSH R0
   PUSH R1	
   EI0                      ; Ativa interrupção 0
   EI
   MOV  R0, var_contador_p_a        
   MOVB R1,[R0]             ; R1 - contador aleatório que nos vai dizer onde dispor a prenda
   ADD  R1,3                ; Adiciona 3 ao contador, que irá assim provocar a aleatoriedade
   MOVB [R0],R1
   MOV  R0, var_int_a_p
   MOVB R1, [R0]            ; R1 = 0 não passou pela interrupção R1 = 1 passou pela interrupção
   CMP  R1,0                ; R1 = 0 vai esperar que passe pela interrupção R1 = 1 vai para o passo seguinte
   JZ   pr_ar_ret
   MOV  R0, var_ninja      
   MOV  R1,1                
   MOVB [R0],R1             ; Ninja desativado
   MOV  R0, var_baixo       
   MOVB R1, [R0]
   CMP  R1,0                ; Se for 0 vai buscar um novo valor
   JNZ  ecra_cim_baix
   MOV  R0, var_cima
   MOVB R1, [R0]
   CMP  R1,0                ; Se os dois objetos estão em jogo entao passa a frente a escolha de um objeto
   JNZ  ecra_cim_baix
   CALL arma_prenda         ; Vai identificar se vai adicionar ao pixel screen uma arma ou uma prenda 
   CALL onde_ar_pr          ; Indica onde vai ser disposta essa arma ou prenda em cima ou em baixo a não ser que já esteja em jogo

ecra_cim_baix:	
   CALL mov_nap_pixel       ; Vai provocar o movimento dos objetos que estejam ativos       
   CALL colisoes            ; Verifica se o ninja colide com algum objeto
   CALL fimjogo             ; Verifica se o jogo já acabou
   MOV  R0,var_int_a_p       
   MOV  R1,0                       
   MOVB [R0], R1            ; Volta a esperar que passe pela interrupção

pr_ar_ret:
   POP  R1
   POP  R0
   RET

	
nin_int: 			       ; Interrupção 1
   PUSH R0
   PUSH R1
   MOV  R0, var_int_n    
   MOV  R1,1
   MOVB [R0],R1            ; Ativa a variável da passagem pela interrupção
   POP  R1
   POP  R0
   RFE
	
	
pr_ar_int:				   ; Interrupção 0
   PUSH R0
   PUSH R1
   MOV  R0, var_int_a_p        
   MOV  R1, 1
   MOVB [R0], R1           ; Ativa a variável da passagem pela interrupção
   POP  R1
   POP  R0
   RFE


;;;Vai usar o contador aleatório para ligar ou uma prenda ou uma arma	
arma_prenda:
   PUSH R0
   PUSH R1
   PUSH R2
   MOV  R0, var_contador  
   MOVB R1,[R0]            ; Acesso ao contador pela memória
   MOV  R2,3               
   AND  R1, R2             ; Lê os dois últimos bits
   MOV  R2, R1             ; Se esse valor for 0 = prenda, se não for dá arma (75% Arma, 25% Prenda)
   CMP  R2, 0            
   JZ   prend_var 

arma_var:				   ; Escolheu arma	
   MOV  R0, alea_a
   MOV  R1,1               
   MOVB [R0], R1           ; Liga a variável que indica que escolheu arma
   JMP  fim_armpr

prend_var:				   ; Escolheu prenda
   MOV  R0, alea_p       
   MOV  R1,1
   MOVB [R0], R1           ; Liga a variável que indica que escolheu prenda

fim_armpr:
   POP  R2
   POP  R1
   POP  R0
   RET

	
;;; Verifica se já existe algum objeto de cima ou algum objeto de baixo em jogo e se não existir onde os colocar	
onde_ar_pr:
   PUSH R0
   PUSH R1
   PUSH R2
   PUSH R3
   PUSH R4
   PUSH R5

baixo_var_c:			   ; Verifica se existe um objeto de baixo em jogo
   MOV  R3, var_baixo
   MOVB R1, [R3]      
   MOV  R2, 0 
   CMP  R1, R2             ; Se R1 = 1, então já existe uma arma ativa no jogo, se R1 = 2 então já existe uma prenda ativa
					       ; Se R1 = 0 não existe vai verificar se foi escolhido uma arma para a ativar
   JZ   col_ar

cima_var_c:				   ; Verifica se existe um objeto de cima em jogo	
   MOV  R4,1
   MOV  R3, var_cima
   MOVB R1, [R3]          
   MOV  R2, 0 
   CMP  R1, R2             ; Se R1 = 1, então já existe uma arma ativa no jogo, se R1 = 2 então já existe uma prenda ativa
    					   ; Se R1 = 0 não existe vai verificar se foi escolhido uma arma para a ativar
   JZ   col_ar
   JMP  fin_call           ; Fim da rotina

col_ar:					   ; Verifica se foi escolhido uma arma na aleatoriedade
   MOV  R0, alea_a
   MOVB R1,[R0]            ; Verifica se na rotina anterior foi escolhido uma arma
   MOV  R2, 1
   CMP  R1,R2
   JZ   incr_valor         ; Se foi escolhida uma arma então vai se ativar a arma, se não vai verificar a prenda

col_bar:				   ; Verifica se foi escolhido uma prenda na aleatoriedade
   MOV  R0, alea_p
   MOVB R1,[R0]            ; Verifica se na rotina anterior foi escolhida uma prenda
   MOV  R2, 1
   CMP  R1,R2              ; Se foi escolhida uma prenda então vai se ativar a prenda, se não vai sair da rotina
   JNZ  fin_call

inc_var:					  ; Vai ativar a prenda
   MOV  R0, var_contador_p_a  ; Contador aleatório 2
   MOVB R1, [R0]
   MOV  R2, 1
   AND  R1, R2                ; (0 ou 1)
   CMP  R1,R4
   JZ   incr_valor            ; Vai provocar aleatoriedade onde a prenda é disposta se em cima ou em baixo
   MOV  R1,2
   MOVB [R3],R1
   CMP  R4,1
   JZ   liga_cima
	
liga_baixo:					  ; Coloca em baixo
   MOV  R0, var_liga_baixo
   MOV  R1,1
   MOVB [R0],R1
   JMP  rotinas

liga_cima:					  ; Coloca em cima
   MOV  R0, var_liga_cima
   MOV  R1,1
   MOVB [R0],R1
   JMP  rotinas

incr_valor:					  ; Vai ativar a arma
   MOV  R1,1
   MOVB [R3],R1
   CMP  R4,1
   JZ   liga_cima
   JMP  liga_baixo

rotinas:					  ; Colocar prenda ou arma em jogo ou em cima ou em baixo
   CALL onde_obj              ; Indica-nos onde colocar os objetos de cima ou de baixo
   CALL n_a_p_pixel           ; Apaga e desce o pixel
   MOV  R0, var_liga_baixo    ; Desativa o apaga e desce o pixel em baixo
   MOV  R1,0
   MOVB [R0],R1
   MOV  R0, var_liga_cima     ; Desativa o apaga e desce o pixel em cima
   MOV  R1,0
   MOVB [R0],R1
   JMP  cima_var_c

fin_call:
   POP  R5
   POP  R4
   POP  R3
   POP  R2
   POP  R1
   POP  R0
   RET

	
;;;Onde colocar o objeto de cima ou onde colocar o objeto de baixo
onde_obj:
   PUSH R0
   PUSH R1
   PUSH R2
   PUSH R3
   PUSH R4
   MOV  R0, var_contador      ; Endereço do contador 
   MOVB R1,[R0]               ; R1 - valor do contador
   MOV  R2,7                  ; Primeiros 3 bits
   AND  R1, R2                ; Retira um valor de 0 a 7
   MOV  R2, R1                ; Valor a adicionar ou a subtrair da linha
   Bit  R1, 1                 ; Se o bit 1 for 0 salta para next
   JZ   next
   MOV  R4,1                  ; R4 - indica que vai diminuir 

next:					      ; Função que vai verificar se é o objeto de cima ou o objeto de baixo que são gerados
   MOV  R0, var_cima          ; Verifica se o objeto de cima vai ser adicionada
   MOVB R1, [R0]
   MOV  R3,0
   CMP  R1,R3
   JNZ  cima_linha
   MOV  R0, var_baixo         ; Verifica se o objeto de baixo 
   MOVB R1, [R0]
   MOV  R3,0
   CMP  R1,R3
   JZ   final_col

baixo_linha:			      ; Adiciona o endereço da linha de baixo
   MOV  R0, lin_i_b
   MOVB R1, [R0]
   JMP  addi

cima_linha:				      ; Adiciona o endereço da linha da prenda
   MOV  R0, lin_i_c
   MOVB R1, [R0]
   
addi:					      ; Adiciona ao 7 ou ao 21 que está na linha
   CMP  R4,1
   JZ   nega                  ; Decrementa
   ADD  R1, R2                ; Incrementa
   MOVB [R0], R1
   JMP  final_col

nega:					      ; Subtrai ao 7 ou ao 21 que está na linha
   SUB  R1, R2                ; Decrementa
   MOVB [R0], R1
   
final_col: 	
   POP  R4
   POP  R3
   POP  R2
   POP  R1
   POP  R0
   RET
	
colisoes:				      ; Verifica se colidiram
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
   MOV  R0, lin_i_b          
   MOVB R2, [R0]              ; R2 - linha onde está o objeto de baixo 
   MOV  R0, lin_i_c
   MOVB R3, [R0]              ; R3 - linha onde está objeto de cima
   MOV  R0, col_i_b
   MOVB R6, [R0]              ; R6 - coluna onde está o objeto de baixo
   MOV  R0, col_i_c
   MOVB R7, [R0]              ; R7 - coluna onde está o objeto de cima

   
; ***************************************************************************
; * Igual em todos os ninjas                                                *
; *  R11 - endereço da linha do ninja                                       *
; *  R1 - linha guardada do ninja                                           *
; *  Verifica se o ninja já morreu por chegar ao limite do ecrã,            *
; *  Se sim passa para o outro ninja                                        *
; *  R5 - Coluna guardada do ninja                                          *
; *  Chama a rotina colidir                                                 *
; *************************************************************************** 

ninja_1_coli:
   MOV  R11, linha_nin1
   MOVB R1, [R11]    
   MOV  R9, 28
   CMP  R1, R9
   JZ   ninja_2_coli   
   MOV  R0, colun_nin1
   MOVB R5, [R0]
   CALL colidir
   
ninja_2_coli:
   MOV  R11, linha_nin2
   MOVB R1, [R11]
   MOV  R9, 28
   CMP  R1, R9
   JZ   ninja_3_coli 
   MOV  R0, colun_nin2
   MOVB R5, [R0] 
   CALL colidir
   
ninja_3_coli:
   MOV  R11, linha_nin3
   MOVB R1, [R11]
   MOV  R9, 28
   CMP  R1,R9
   JZ   ninja_4_coli 
   MOV  R0, colun_nin3
   MOVB R5, [R0]
   CALL colidir
   
ninja_4_coli:
   MOV  R11, linha_nin4
   MOVB R1, [R11]
   MOV  R9, 28
   CMP  R1, R9
   JZ   fim_coli
   MOV  R0, colun_nin4
   MOVB R5, [R0]
   CALL colidir
   
fim_coli:
   POP  R11
   POP  R10
   POP  R9
   POP  R8
   POP  R7
   POP  R6
   POP  R5
   POP  R4
   POP  R3
   POP  R2
   POP  R1
   POP  R0
   RET
   
   
colidir:
  
baixo_col:
   MOV  R9, R6
   ADD  R9, 2
   CMP  R9, R5
   JLT  cima_col   ; Se a coluna final do objeto de baixo for menor que a primeira coluna do ninja então não está a tocar 
   MOV  R8, R5
   ADD  R8, 2
   CMP  R6, R8
   JGT cima_col    ; Se a coluna inicial do objeto de baixo for maior que a coluna final do ninja então não está a tocar
   
colisao_baixo_linha: 
   MOV  R10, R2
   ADD  R10, 2
   CMP  R10, R1
   JLT cima_col    ; Verifica se a última linha do objeto de baixo é menor que a linha inicial do ninja, se for então não está a tocar
   MOV  R4, R1
   ADD  R4, 3
   CMP  R2, R4
   JGT  cima_col   ; Se a linha inicial do objeto de baixo for maior que a linha final do ninja então não está a tocar

colisao_baixo:     ; Ninja colidiu com o objeto de baixo
   MOV  R0, var_destruir
   MOV  R9, 1
   MOVB [R0], R9   ; Ativa a destruição do ninja em que o apaga pela última vez
   CALL mov_nap_pixel
   MOV  R0, lin_i_b
   MOV  R9, 21      
   MOVB [R0], R9   ; Reset da linha da arma de baixo
   MOV  R0, col_i_b
   MOV  R9, 29     ; Reset da coluna da arma de baixo
   MOVB [R0], R9
   MOV  R0, var_baixo
   MOVB R9, [R0]
   MOV  R4,0       ; Desativa a variável de baixo
   MOVB [R0],R4
   CMP  R9, 2      ; Se a variável era 2, entao era prenda e vai adicionar valor ao display
   JZ   add_display
   CMP  R9,1       ; Se a variável era 1, então era arma e o ninja com que colidiu vai morrer
   JZ   morrer

cima_col:
   MOV  R9, R7 
   ADD  R9, 2          
   CMP  R9, R5
   JLT  final_colis    ; Se a coluna final do objeto de cima for menor que a primeira coluna do ninja então não está a colidir
   MOV  R8, R5
   ADD  R8, 2
   CMP  R7, R8   
   JGT  final_colis    ; Se a coluna inicial do objeto de cima for maior que a última coluna do ninja então não está a colidir

colisao_cima_linha:
   MOV  R10, R3
   ADD  R10, 2
   CMP  R10, R1
   JLT  final_colis    ; Se a linha final do objeto de cima for menor que a linha inicial do ninja então não está a colidir
   MOV  R4, R1
   ADD  R4, 3
   CMP  R3, R4
   JGT  final_colis    ; Se a linha inicial do objecto de cima for maior que a linha final do ninja então nao está a colidir

colisao_cima:          ; Objeto de cima colidiu com o ninja
   MOV  R0, var_destruir
   MOV  R1,2
   MOVB [R0], R1       ; Vai destruir o objeto de cima
   CALL mov_nap_pixel
   MOV  R0, lin_i_c
   MOV  R1, 7
   MOVB [R0], R1       ; Reset da linha do objeto de cima
   MOV  R0, col_i_c
   MOV  R1, 29
   MOVB [R0], R1       ; Reset da coluna do objeto de cima
   MOV  R0, var_cima
   MOVB R7, [R0]       ; Se R7 =2  significa que era prenda e vai adicionar pontos ao display se não era arma e mata o ninja
   MOV  R8, 0
   MOVB [R0],R8        ; Desativa a variável do objeto de cima
   CMP  R7, 2
   JZ   add_display
   CMP  R7, 1
   JZ   morrer
   JMP  final_colis

add_display:
   CALL display        ; Adiciona valor ao display
   JMP  final_colis
  
morrer:
   MOV  R0, var_ninja
   MOV  R1, 0
   MOVB [R0], R1       ; Ativa modificações do ninja
   MOV  R0, var_apg_nin_colid
   MOV  R1, 1
   MOVB [R0], R1       ; Permite apagar pela última vez o ninja matando - o
   MOVB R1, [R11]    
   MOV  R0, lin_i_n
   MOVB [R0], R1       ; Colocar a linha do ninja no determinante do ninja
   MOV  R0, col_i_n
   MOVB [R0], R5       ; Colocar a coluna do ninja no determinante do ninja
   CALL mov_nap_pixel
   MOV  R2, 28
   MOVB [R11],R2       ; Coloca o ninja no final do ecrã como morto
   MOV  R0, var_ninja
   MOV  R1, 1
   MOVB [R0], R1       ; Desativa modificações do ninja
   
final_colis:
   RET

   
; ******************************************************************************************************************************
; * Fim do jogo                                                                                                                *
; * Se todas os ninjas estiverem mortos, então fim do jogo, ou se a tecla d foi carregada então fim do jogo                    *
; * Caso aconteça, vai apagar o display e fazer reset a todas as variáveis e depois coloca o pixel screen final                *
; * Caso não tenha desativado, pelo botão do teclado, faz-se o reset da variável para 0 de modo a que o jogo esteja desativado *
; * Posições dos ninjas todos serem a última linha do ecrã é uma condição para fim de jogo                                     *
; ******************************************************************************************************************************

fimjogo:
   PUSH R0
   PUSH R1
   MOV  R0, linha_nin1
   MOVB R1, [R0]
   MOV  R2, 28			
   CMP  R1, R2
   JNZ  volta_jogo
   MOV  R0, linha_nin2
   MOVB R1, [R0]
   MOV  R2, 28
   CMP  R1, R2
   JNZ  volta_jogo
   MOV  R0, linha_nin3
   MOVB R1, [R0]
   MOV  R2, 28
   CMP  R1, R2
   JNZ  volta_jogo
   MOV  R0, linha_nin4
   MOVB R1, [R0]
   MOV  R2, 28
   CMP  R1, R2
   JNZ  volta_jogo

final_jogo:
   CALL apagar_display
   CALL pixel_inic_fin
   MOV  R0, var_jogo_ativado
   MOV  R1, 0
   MOVB [R0], R1
   MOV  R0, linha_nin1
   MOV  R1, 12
   MOVB [R0], R1
   MOV  R0, linha_nin2
   MOVB [R0], R1
   MOV  R0, linha_nin3
   MOVB [R0], R1
   MOV  R0, linha_nin4
   MOVB [R0], R1  
   MOV  R0, colun_nin1
   MOV  R1, 0
   MOVB [R0], R1
   MOV  R0, colun_nin2
   MOV  R1, 4
   MOVB [R0], R1
   MOV  R0, colun_nin3
   MOV  R1, 8
   MOVB [R0], R1
   MOV  R0, colun_nin4
   MOV  R1, 12
   MOVB [R0], R1
   MOV  R0, lin_i_c
   MOV  R1, 7
   MOVB [R0], R1
   MOV  R0, col_i_c
   MOV  R1, 29
   MOVB [R0], R1
   MOV  R0, lin_i_b
   MOV  R1, 21
   MOVB [R0], R1
   MOV  R0, col_i_b
   MOV  R1, 29
   MOVB [R0], R1
   MOV  R0, var_cima
   MOV  R1, 0
   MOVB [R0], R1
   MOV  R0, var_baixo
   MOV  R1, 0
   MOVB [R0], R1
   MOV  R0,var_jogo_ativado
   MOV  R1,0
   MOVB [R8], R9
   JMP  fim_game

volta_jogo:
   MOV  R0, var_jogo_ativado
   MOVB R1, [R0]
   CMP  R1, 0
   JZ   final_jogo

fim_game:
   POP  R1
   POP  R0
   RET
 
	
display:
   PUSH R0
   PUSH R1
   PUSH R2
   PUSH R3
   PUSH R4
   PUSH R5
   MOV  R0, var_reset_display
   MOVB R1, [R0]
   MOV  R2, 1
   CMP  R1, R2
   JZ   display_apagar   ; Caso esteja ativa, faz o reset do display

adicionar_presente:      ; Soma 3 ao valor do display 
   MOV  R0, var_valor_display
   MOVB R3, [R0]
   MOV  R4, 3            ; R4 - Valor a ser somado
   Add  R3, R4           ; Adiciona ao valor no display
	
condic:                  ; Condicionantes ao valor do display
   MOV  R4, 99           ; R4 - número máximo que o display pode atingir
   CMP  R3, R4           ; É maior ou igual que 99?
   JGT  disp_fin1
   MOV  R4, 0            ; R4 - número minimo que o display pode atingir
   CMP  R3, R4           ; É maior ou igual que 0?
   JLT  disp_fin2

dec_1:                   ; Passar o numero a decimal
   MOV  R0, var_valor_display
   MOVB [R0], R3
   MOV  R5, R3         
   Mov  R1, R3
   MOV  R4, 10	         ; R4 - Usado para obter o valor em decimal
   DIV  R5, R4           ; R5 - Parte inteira em decimal
   MOD  R1, R4           ; R1 - Resto em decimal

disp:					 ; Colocar valor no display
   SHL  R5, 4            ; R5 - parte inteira no nibble high(dezenas)
   OR   R5, R1           ; R1 - resto no nibble low(unidades)

addicionar_valor:
   MOV  R4, DISPLAYS
   MOVB [R4], R5         ; Valores importados para o display, na memória(R4)
   JMP  fim_dis
	
disp_fin1:			     ; Valor máximo do display
   MOV  R3, R4           ; R3 fica com o valor 99
   JMP  dec_1
	
disp_fin2:		    	 ; Valor minimo do display
   MOV  R3, R4           ; R3 fica com o valor 0
   JMP  dec_1
	
display_apagar:
   MOV  R5, 0            ; Reset do display
   JMP  addicionar_valor
   
fim_dis:
   MOV  R1, 0
   MOV  R0, var_reset_display
   MOVB [R0],R1          ; Desativa o reset do display
   POP  R5
   POP  R4
   POP  R3
   POP  R2
   POP  R1
   POP  R0
   RET
 
 
;*********************************************************************************************  
; Rotinas essenciais para o funcionamento do programa, usadas em várias partes do programa   *
;*********************************************************************************************

pixel_inic_fin:       ; Coloca no pixel screen o ecrã inicial e o final
   PUSH R0
   PUSH R1
   MOV  R0,var_pinta_ecra 
   MOV  R1,1
   MOVB [R0],R1       ; Ativa variável que permite pintar o ecrã
   CALL Lig_pixel     ; Chama o ligar pixel onde vai colocar
   MOV  R0, var_pinta_ecra
   MOV  R1,0
   MOVB [R0],R1       ; Desativa variável que permite pintar o ecrã
   POP  R1
   POP  R0
   RET
   
   
apagar_display:       ; Apaga tudo o que esteja no pixel screen
   PUSH R0
   PUSH R1
   MOV  R0,var_apag_ecra
   MOV  R1,1
   MOVB [R0],R1       ; Ativa a variável que permite apagar o ecrã 
   CALL Lig_pixel
   MOV  R0, var_apag_ecra
   MOV  R1,0
   MOVB [R0],R1       ; Desativa a variável que permite apagar o ecrã
   POP  R1
   POP  R0
   RET

   
verificar_ninjas:
   PUSH R0
   PUSH R1
   PUSH R2
   MOV  R0, var_teclado
   MOVB R3, [R0]      ; R3 - valor do teclado
   MOV  R2,0
   CMP  R3,R2         ; Clicou na tecla 0
   JZ   ativa_sobninj1
   MOV  R2,1 
   CMP  R3,R2         ; Clicou na tecla 1
   JZ   ativa_sobninj2
   MOV  R2,2
   CMP  R3,R2         ; Clicou na tecla 2
   JZ   ativa_sobninj3
   MOV  R2,3
   CMP  R3,R2         ; Clicou na tecla 3
   JZ   ativa_sobninj4
   MOV  R2,4
   CMP  R3,R2         ; Clicou na tecla 4
   JZ   ativa_desninj1
   MOV  R2,5
   CMP  R3,R2         ; Clicou na tecla 5
   JZ   ativa_desninj2
   MOV  R2,6
   CMP  R3,R2         ; Clicou na tecla 6
   JZ   ativa_desninj3
   MOV  R2,7          
   CMP  R3,R2         ; Clicou na telca 7
   JZ   ativa_desninj4
   JMP  fim_ninj
   
ativa_sobninj1:      ; Vai subir o ninja 1
   MOV  R0, var_ninja
   MOV  R1, 0
   MOVB [R0], R1     ; Ativa a modificação do ninja
   MOV  R0, var_qual_ninja
   MOV  R1, 1
   MOVB [R0], R1     ; Ativa ninja 1
   MOV  R0, var_sobe_desce
   MOV  R1, 1
   MOVB [R0], R1     ; Ativa subida 
   CALL colocar_nin  ; Vai subir o ninja
   CALL reset_atvn   ; Reset das variáveis utilizadas
   JMP  fim_ninj
   
ativa_sobninj2:      ; Vai subir o ninja 2
   MOV  R0, var_ninja  
   MOV  R1, 0
   MOVB [R0], R1     ; Ativa a modificação do ninja
   MOV  R0, var_qual_ninja
   MOV  R1, 2
   MOVB [R0], R1     ; Ativa ninja 2
   MOV  R0, var_sobe_desce
   MOV  R1, 1
   MOVB [R0], R1     ; Ativa a subida
   CALL colocar_nin
   CALL reset_atvn   ; Faz reset as variavéis utilizadas
   JMP  fim_ninj
   
ativa_sobninj3:      ; Vai subir o ninja 3
   MOV  R0, var_ninja
   MOV  R1, 0
   MOVB [R0], R1     ; Ativa a modificação do ninja
   MOV  R0, var_qual_ninja
   MOV  R1, 3
   MOVB [R0], R1     ; Ativa ninja 3
   MOV  R0, var_sobe_desce
   MOV  R1, 1
   MOVB [R0], R1     ; Ativa subida
   CALL colocar_nin
   CALL reset_atvn   ; Reset das variáveis
   JMP  fim_ninj
   
ativa_sobninj4:      ; Ativa subida de ninja 4
   MOV  R0, var_ninja 
   MOV  R1, 0
   MOVB [R0], R1     ; Ativa modificação do ninja
   MOV  R0, var_qual_ninja
   MOV  R1, 4
   MOVB [R0], R1     ; Ativa ninja 4
   MOV  R0, var_sobe_desce
   MOV  R1, 1
   MOVB [R0], R1     ; Ativa subida
   CALL colocar_nin
   CALL reset_atvn   ; Reset das variáveis
   JMP  fim_ninj
   
ativa_desninj1:      ; Ativa a descida do ninja 1
   MOV  R0, var_ninja
   MOV  R1, 0
   MOVB [R0], R1     ; Ativa modificações do ninja
   MOV  R0, var_qual_ninja
   MOV  R1, 1
   MOVB [R0], R1     ; Ativa ninja 1
   MOV  R0, var_sobe_desce
   MOV  R1, 2
   MOVB [R0], R1     ; Ativa a descida
   CALL colocar_nin
   CALL reset_atvn   ; Reset das variavéis
   JMP  fim_ninj
   
ativa_desninj2:      ; Ativa a descida do ninja 2
   MOV  R0, var_ninja
   MOV  R1, 0
   MOVB [R0], R1     ; Ativa modificações do ninja 
   MOV  R0, var_qual_ninja
   MOV  R1, 2
   MOVB [R0], R1     ; Ativa ninja 2
   MOV  R0, var_sobe_desce
   MOV  R1, 2
   MOVB [R0], R1     ; Ativa descida do ninja 2
   CALL colocar_nin
   CALL reset_atvn   ; Faz reset das variavéis usadas
   JMP  fim_ninj
   
ativa_desninj3:      ; Ativa a descida do ninja 3
   MOV  R0, var_ninja
   MOV  R1, 0
   MOVB [R0], R1     ; Ativa modificações do ninja
   MOV  R0, var_qual_ninja
   MOV  R1, 3
   MOVB [R0], R1     ; Ativa ninja 3
   MOV  R0, var_sobe_desce
   MOV  R1, 2
   MOVB [R0], R1     ; Ativa descida do ninja
   CALL colocar_nin
   CALL reset_atvn   ; Faz reset das variavéis utilizadas
   JMP  fim_ninj
   
ativa_desninj4:      ; Ativa a descida do ninja 4
   MOV  R0, var_ninja
   MOV  R1, 0
   MOVB [R0], R1     ; Ativa a modificação do ninja
   MOV  R0, var_qual_ninja
   MOV  R1, 4
   MOVB [R0], R1     ; Ativa ninja 4
   MOV  R0, var_sobe_desce
   MOV  R1, 2
   MOVB [R0], R1     ; Ativa descida
   CALL colocar_nin
   CALL reset_atvn   ; Reset das variavéis utilizadas
   
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
   MOV  R0, var_qual_ninja   ; Verifica qual o ninja a ser escolhido se vier do teclado, caso contrário faz todos
   MOVB R1, [R0]
   CMP  R1,1
   JZ   ninja_1
   CMP  R1,2
   JZ   ninja_2
   CMP  R1,3
   JZ   ninja_3
   CMP  R1,4
   JZ   ninja_4

   
; ***************************************************************************
; * Igual em todos os ninjas                                                *
; *  R1 - linha guardada do ninja                                           *
; *  R2 - adiciona ao determinador da linha do ninja, o ninja a colocar     *
; *  R4 - coluna guardada do ninja                                          *
; *  R5 - adiciona ao determinador de coluna do ninja o ninja a colocar     *
; *  Chama rotina que escolhe as variantes possíveis (subir e descer)       *
; *  Desativa a variável que sobe ou desce o ninja                          *
; *************************************************************************** 

ninja_1:
   MOV  R0, linha_nin1
   MOVB R1, [R0]             
   MOV  R2, lin_i_n
   MOVB [R2], R1             
   MOV  R3, colun_nin1
   MOVB R4, [R3]              
   MOV  R5, col_i_n
   MOVB [R5], R4		         
   CALL escolhe_variantes
   MOV  R0, var_sobe_desce
   MOVB R1, [R0]
   CMP  R1,0
   JNZ  fim_voluntario

ninja_2:
   MOV  R0, linha_nin2
   MOVB R1, [R0]
   MOV  R2, lin_i_n
   MOVB [R2], R1
   MOV  R3, colun_nin2
   MOVB R4, [R3]
   MOV  R5, col_i_n
   MOVB [R5], R4		
   CALL escolhe_variantes
   MOV  R0, var_sobe_desce
   MOVB R1, [R0]
   CMP  R1,0
   JNZ  fim_voluntario

ninja_3:
   MOV  R0, linha_nin3
   MOVB R1, [R0]
   MOV  R2, lin_i_n
   MOVB [R2], R1
   MOV  R3, colun_nin3
   MOVB R4, [R3]
   MOV  R5, col_i_n
   MOVB [R5], R4		
   CALL escolhe_variantes
   MOV  R0, var_sobe_desce
   MOVB R1, [R0]
   CMP  R1,0
   JNZ  fim_voluntario

ninja_4:
   MOV  R0, linha_nin4
   MOVB R1, [R0]
   MOV  R2, lin_i_n
   MOVB [R2], R1
   MOV  R3, colun_nin4
   MOVB R4, [R3]
   MOV  R5, col_i_n
   MOVB [R5], R4		
   CALL escolhe_variantes

fim_voluntario:
   POP  R7
   POP  R6
   POP  R5
   POP  R4
   POP  R3
   POP  R2
   POP  R1
   POP  R0
   RET
  
  
escolhe_variantes: 
   PUSH R6
   PUSH R7  
   MOV  R6, var_colo_desc_nin 
   MOVB R7, [R6]
   CMP  R7,1                   ; Caso esta variável esteja a 1, vai colocar o ninja no ecrã
   JZ   coloc_pix
   CMP  R7,2                   ; Vai  descer o ninja
   JZ   descida_aut
   MOV  R6, var_sobe_desce
   MOVB R7, [R6] 
   CMP  R7,1                   ; Verifica se o ninja vai subir ou descer pelo teclado, se estiver a 1 - sobe
   JZ   sob_pix
   CMP  R7,2                   ; Se estiver a 2 - desce
   JZ   descida_aut

sob_pix:
   MOV  R6, lin_i_n
   MOVB R7, [R6]              
   CMP  R7, 0                  ; Caso o ninja a subir já esteja na primeira linha não faz nada 
   JZ   fim_call
   MOV  R6,28 
   CMP  R7,R6
   JZ   fim_call               ; Caso esteja na última linha significa que está morto
   MOV  R6, var_sobe_ninja
   MOV  R7,1
   MOVB [R6], R7               ; Ativa a variável que faz subir o ninja
   CALL mov_nap_pixel
   MOV  R6, var_sobe_ninja    
   MOV  R7,0
   MOVB [R6], R7               ; Desativa a variável que faz subir o ninja
   CALL insert_ninja           ; Coloca as novas coordenadas nos respetivos sítios em memória
   JMP  fim_call

coloc_pix:
   CALL n_a_p_pixel            ; Vai colocar o ninja
   CALL insert_ninja           ; Coloca as novas coordenadas nos respetivos sítios em memória
   JMP  fim_call

descida_aut:
   CALL mov_nap_pixel          ; Desce o ninja
   CALL insert_ninja           ; Coloca as novas coordenadas nos respetivos sítios em memória
  
fim_call:
   POP  R7
   POP  R6
   RET

  
insert_ninja:                  ; Guarda os novos valores nas variáveis inicializadas no colocar_nin
   MOVB R1, [R2]
   MOVB [R0], R1
   MOVB R1, [R5]
   MOVB [R3], R1
   RET
 
 
reset_atvn:                    ; Reset de todas as variáveis usadas
   PUSH R0
   PUSH R1
   MOV  R0, var_ninja
   MOV  R1, 1
   MOVB [R0], R1               ; Desativa o ninja
   MOV  R0, var_qual_ninja
   MOV  R1, 0
   MOVB [R0], R1               ; Desativa qual o ninja a colocar
   MOV  R0, var_sobe_desce
   MOV  R1, 0
   MOVB [R0], R1               ; Desativa o sobe e desce
   POP  R1
   POP  R0
   RET
   
   
;;; Rotina que escolhe se é ninja, objeto de cima ou objeto de baixo e verifica as suas linhas e colunas
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
   MOV  R0, var_ninja       
   MOVB R1, [R0]
   CMP  R1,0                  ; Verifica se o ninja está ativo
   JZ   nin
   MOV  R0,var_liga_cima
   MOVB R1, [R0]              ; Verifica se a variável que liga o objeto de cima está ativada
   CMP  R1, 1
   JZ   CIMA
   MOV  R0,var_liga_baixo
   MOVB R1, [R0]              ; Verifica se a variável que liga o objeto de baixo está ativada
   CMP  R1, 1
   JZ   BAIXO
	
CIMA:
   MOV  R0, var_cima   
   MOVB R1, [R0]
   CMP  R1,0                  ; Verifica se a variável do objeto está ativa, se estiver vai verificar se é arma
   JNZ  var_c_arma

BAIXO:
   MOV  R0, var_baixo  
   MOVB R1, [R0]
   CMP  R1,0                  ; Verifica se a variável do objeto está ativa, se estiver vai verificar se é arma
   JZ   fim_ninja

var_b_arma:
   CMP  R1,1                  ; Se R1 = 1 entao é arma, que vai ser adicionada na parte de baixo, se R1 = 2 é prenda
   JZ   arm_b
   JMP  prenda_b

var_c_arma:
   CMP  R1,1
   JZ   baixo_linhac          ; Se R1 = 1 entao é arma, que vai ser adicionada na parte de cima, se R1 = 2 é prenda
   JMP  cima_var_cc    

;;; Vai retirar a linha de cima, a coluna de cima e a forma da prenda para registos, vai também colocar num registo se é para apagar a prenda ou para a colocar no pixel screen
cima_var_cc:
   MOV  R2, lin_i_c         
   MOV  R5, col_i_c
   MOV  R6, Prenda
   MOV  R0, var_apagar_cima
   MOVB R9, [R0]
   JMP  Coord_n_a_p

;;; Vai retirar a linha de baixo, a coluna de baixo e a forma da prenda para registos, vai também colocar num registo se é para apagar a prenda ou para a colocar no pixel screen
prenda_b:
   MOV  R2, lin_i_b         
   MOV  R5, col_i_b
   MOV  R6, Prenda
   MOV  R0, var_apagar_baixo
   MOVB R9, [R0]
   JMP  Coord_n_a_p

;;; Vai retirar os endereços da linha de baixo, da coluna de baixo e da forma da arma para registos, vai também colocar num registo se é para apagar a arma ou para a colocar no pixel screen
arm_b:
   MOV  R2, lin_i_b
   MOV  R5, col_i_b
   MOV  R6, Arma
   MOV  R0, var_apagar_baixo
   MOVB R9, [R0]
   JMP  Coord_n_a_p

;;; Vai retirar os endereços da linha de cima, da coluna de cima e da forma da arma para registos, vai também colocar num registo se é para apagar a arma ou para a colocar no pixel screen
baixo_linhac:
   MOV  R2, lin_i_c
   MOV  R5, col_i_c
   MOV  R6, Arma
   MOV  R0, var_apagar_cima
   MOVB R9, [R0]
   JMP  Coord_n_a_p
	
;;; Vai retirar a linha, a coluna e a forma do ninja para registos, vai também colocar num registo se é para apagar o ninja ou para o colocar no pixel screen
nin:
   MOV  R2, lin_i_n
   MOV  R6, Ninja
   MOV  R5, col_i_n
   MOV  R0, var_apagar_nin
   MOVB R9, [R0]

;;; Obtem os valores e coloca-os em registos
Coord_n_a_p:
   MOVB R0, [R2]           ; R0 - número da linha
   MOVB R1, [R5]           ; R1 - número da coluna
   MOV  R8, R1             ; R8 - garantia da linha inicial
   MOVB R3, [R6]	       ; R3 - largura do objeto
   ADD  R6, 1              ; R6 - segundo endereço do objeto
   MOVB R4, [R6]           ; R4 - tamanho do objeto
   MOV  R11, 1             ; R11 - contador de colunas
   MOV  R10, 1	           ; R10 - contador de linhas
	
;;;  Verifica se o o bit é 0 ou 1 de forma a saber se vai pintar ou não esse pixel
PIXEL_LINHA:
   ADD  R6,1               ; R6 - próximo endereco do objeto (parte para colocar no pixel screen)
   MOVB R7,[R6]            ; Adiciona o bit do ninja ao R7
   CMP  R7,0               ; Se R7 = 0 então passa para o próximo, ou seja não vai pintar o pixel
   JZ   muda_col           ; Muda logo para a próxima coluna 
   CALL Lig_pixel          ; Chama a rotina que adiciona o pixel

;;; Faz a mudança de coluna e se esta tiver na última coluna vai para muda_linh
muda_col:
   CMP  R11,R3             ; Verifica se a coluna já chegou ao fim, e se chegar vai mudar de linha        
   JZ   muda_linh
   ADD  R11,1              ; Adiciona-se ao contador de colunas 1
   ADD  R1,1               ; Passa para a próxima coluna
   JMP  PIXEL_LINHA  

;;; Faz o reset da coluna e verifica se está na ultima linha	
muda_linh:
   MOV  R11,1              ; Reset do Contador de colunas
   MOV  R1,R8              ; Reset da coluna
   CMP  R10,R4             ; Caso esteja na ultima linha o programa termina
   JZ   fim_ninja           
   ADD  R10,1              ; Acrescenta um valor ao contador de linhas
   ADD  R0,1               ; Proxima linha
   JMP  PIXEL_LINHA  

;;; Acaba o apagamento ou a colocação dos pixeis no pixel screen
fim_ninja:
   MOV  R2, lin_i_n       
   MOVB R0, [R2]           ; Reset da linha
   MOV  R2, col_i_n        
   MOVB R1, [R2]           ; Reset da coluna
   POP  R11
   POP  R10
   POP  R9
   POP  R8
   POP  R7
   POP  R6
   POP  R5
   POP  R4
   POP  R3
   POP  R2
   POP  R1
   POP  R0
   RET


;;; Rotina que apaga ou coloca os pixeis no pixel screen
Lig_pixel:
   PUSH R2
   PUSH R3
   PUSH R4
   PUSH R5
   PUSH R6
   MOV  R2,var_pinta_ecra
   MOVB R6, [R2]
   CMP  R6,1
   JZ   reset_pix          ; Caso esteja ativo vai inicializar a linha e a coluna a 0
   MOV  R2, var_apag_ecra
   MOVB R6, [R2]           ; Caso esteja ativo vai inicializar a linha e a coluna a 0
   CMP  R6,1
   JNZ  voltar_pixel

reset_pix:
   MOV  R0,0
   MOV  R1,0

voltar_pixel:
   MOV  R2, 8               
   MOV  R3, R1
   DIV  R3 , R2            ; R3 = coluna/8
   MOV  R2, 4 
   MOV  R4, R0	
   MUL  R4 , R2            ; R4 = linha*4
   ADD  R3, R4             ; R3 - número do byte
   MOV  R2, PIXSCRN        ; R2 - endereço onde comeca o pixel screen
   ADD  R3, R2             ; R3 - número do byte no pixel screen
   MOV  R2,var_pinta_ecra
   MOVB R6, [R2]
   CMP  R6,1
   JZ   ecra_inicial_final
   MOV  R2, var_apag_ecra
   MOVB R6, [R2]
   CMP  R6,1
   JZ   ecra_apagar
   MOV  R2, 8 
   MOV  R4, R1	
   MOD  R4, R2             ; R4 - Resto da coluna que nos vai indicar o lugar do bit
   MOV  R5, 80H            ; R5 - Começa no 10000000b pois funciona ao contrário
	
Mascara:	
   CMP  R4,0               ; Verifica se já encontrou o pixel a marcar
   JZ   pixel_screen
   SHR  R5,1               ; Passa para o bit seguinte
   SUB  R4,1               ; Subtrai o número de vezes até chegar ao bit
   JMP  Mascara

pixel_screen:	
   CMP  R9,1               ; R9 - Se for um quer dizer que foi ativado a limpeza do ninja
   JZ   limpa_screen
   MOVB R2,[R3]          
   OR   R5, R2             ; Adiciona o bit ao que já lá está
   MOVB [R3],R5	           ; Liga o bit no pixel screen
   JMP  fim_pixel	

limpa_screen:
   MOVB R2,[R3]
   NOT  R5                 ; Inverte           
   AND  R5, R2             ; Apaga o bit
   MOVB [R3],R5	           ; Desliga o bit no pixel screen
   JMP  fim_pixel

ecra_inicial_final:
   MOV  R6, 2  
   MOV  R7,R0	           ; R7 - linha par ou linha ímpar
   MOD  R7,R6              ; Se for ímpar adiciona 10101010b
   JNZ  pix_imp
   MOV  R2, Pixeis_Pares   ; Se for par adiciona 01010101b
   JMP  pix_byte

pix_imp:                   ; Coloca a máscara impar
   MOV  R2, Pixeis_Impares
   JMP  pix_byte

ecra_apagar:               ; Coloca a máscara a 0
   MOV  R2, Pixeis_apagados

pix_byte:
   MOVB [R3],R2            ; Adiciona a máscara ao pixel screen
   MOV  R6,8	           ; Passa para o byte seguinte  
   ADD  R1,R6
   MOV  R6,32              ; Verifica se a coluna acabou 
   CMP  R1,R6              ; Coluna já chegou ao fim?
   JZ   verificar_colu
   JMP  voltar_pixel

verificar_colu:            ; Verifica se o programa vai terminar ou se vai passar para a próxima linha
   CMP  R0,R6              ; Verifica se a linha chegou ao fim
   JZ   fim_pixel          ; Se sim o pixel inicial está feito
   MOV  R1,0               ; Reset das colunas
   ADD  R0,1               ; Próxima linha
   JMP  voltar_pixel   

fim_pixel:
   POP  R6	
   POP  R5
   POP  R4
   POP  R3
   POP  R2
   RET


;;;Rotina que pode descer a linha do ninja, mover para a esquerda a coluna do objeto de cima ou do objeto de baixo e ativar o apagar de cada um deles
mov_nap_pixel:
   PUSH R0
   PUSH R1
   PUSH R2
   PUSH R3
   PUSH R4
   PUSH R6
   MOV  R6,1              ; R6 = 1

ninja_ap:
   MOV  R0, var_ninja      
   MOVB R1, [R0]          ; Vai verificar o ninja está ativo
   CMP  R1, R6            ; Ninja está ativo? = 0
   JNZ  ninja_pr   	

baixo_ap:	
   MOV  R0, var_baixo      
   MOVB R1, [R0]          ; Vai verificar se o objeto de baixo está ativo
   CMP  R1,0              ; Baixo está ativa? = 1
   JNZ  baixo_pr

cima_ap:
   MOV  R0, var_cima
   MOVB R1, [R0]          ; Vai verificar se o objeto de cima está ativo
   CMP  R1, 0             ; Cima está ativa? = 1
   JNZ  cima_pr
   JMP  fim_de_rot

baixo_pr:                 ; Verifica se vai apagar ou andar para o lado
apagar_baixo:
   MOV  R0, var_apagar_baixo       
   MOV  R1,1               
   MOVB [R0],R1           ; Ativa apagar objeto de baixo
   MOV  R0,var_liga_baixo
   MOV  R1,1
   MOVB [R0],R1           ; Permite que apague no pixel screen
   CALL n_a_p_pixel       ; Chama a rotina para apagar o pixel
   MOV  R0, var_apagar_baixo 
   MOV  R1, 0
   MOVB [R0], R1
   MOV  R0, var_destruir
   MOVB R1, [R0]
   CMP  R1,1
   JZ   fim_de_rot        ; Caso esteja ativo apenas apaga o objeto de baixo

andar_baixo:
   MOV  R0, col_i_b             
   MOVB R1, [R0]          ; R1 - coluna do objeto de baixo
   MOV  R2,0           
   CMP  R1,R2             ; Se a coluna for igual a 0, significa que o objeto de baixo bateu na parede
   JZ   desat_baixo             
   SUB  R1,1              ; Anda uma coluna para a esquerda
   MOVB [R0], R1          
   CALL n_a_p_pixel
   MOV  R0,var_liga_baixo
   MOV  R1, 0
   MOVB [R0],R1           ; Desativa a variável que permite modificações do objeto de baixo no pixel screen
   JMP  cima_ap
   
cima_pr:                  ; Verifica se vai apagar ou andar para o lado o objeto de cima
apagar_cima:              ; Vai apagar o objeto de cima do pixel screen
   MOV  R0, var_apagar_cima    
   MOV  R1,1               
   MOVB [R0],R1           ; Ativa apagar objeto de cima
   MOV  R0,var_liga_cima
   MOV  R1,1
   MOVB [R0],R1           ; Ligado, permite que se apague o objeto de cima do pixel screen
   CALL n_a_p_pixel       ; Chama a rotina para apagar o pixel
   MOV  R0, var_apagar_cima
   MOV  R1, 0
   MOVB [R0], R1          ; Desativa apagar o objeto de cima
   MOV  R0, var_destruir
   MOVB R1, [R0]
   CMP  R1,2
   JZ   fim_de_rot        ; Caso esteja ativo apenas apaga o objeto de cima

andar_cima:               ; O objeto de cima vai andar para a esquerda
   MOV  R0, col_i_c
   MOVB R1, [R0]          ; Coluna do objeto de cima
   MOV  R2,0
   CMP  R1,R2             ; Verifica se o objeto de cima já bateu na parede
   JZ   desat_cima
   SUB  R1,1              ; Anda uma coluna para a esquerda
   MOVB [R0], R1          
   CALL n_a_p_pixel       ; Colocar o objeto de cima no pixel screen
   MOV  R0,var_liga_cima
   MOV  R1,0
   MOVB [R0],R1           ; Desativa a variável que permite modificações do pixel screen
   JMP  fim_de_rot
   
ninja_pr:                 ; Indica se vai apagar o ninja ou descer
apagar_nin:               ; Vai apagar o ninja do pixel screen
   MOV  R0, var_apagar_nin   
   MOV  R1,1
   MOVB [R0], R1          ; Ativa a variável que apaga o ninja
   CALL n_a_p_pixel  
   MOV  R0, var_apagar_nin
   MOV  R1, 0
   MOVB [R0], R1          ; Desativa o apagar ninja
   MOV  R0, var_apg_nin_colid
   MOVB R1, [R0]
   CMP  R1,1
   JZ   fim_de_rot        ; Caso esteja ativa apenas apaga o ninja

andar_ninja:              ; O ninja vai descer
   MOV  R1, lin_i_n        
   MOVB R0, [R1]          ; Número da linha atual
   MOV  R2,28              
   CMP  R0,R2             ; Se já chegou ao fim do pixel screen, morre
   JZ   fim_de_rot
   MOV  R3, var_sobe_ninja
   MOVB R4, [R3]
   CMP  R4,1
   JZ   six_nin
   ADD  R0, 1             ; Linha seguinte
   JMP  colocar_ecra

six_nin:
   SUB  R0,1

colocar_ecra:
   MOVB [R1],R0           ; Coloca o valor da linha em memória
   CALL n_a_p_pixel       ; Chama a rotina para descer o pixel
   JMP  fim_de_rot

desat_baixo:              ; Vai desativar o objeto de baixo
   MOV R2,29
   MOVB [R0], R2          ; Reset da coluna do objeto de baixo
   MOV R0, alea_a
   MOV R1,0
   MOVB [R0],R1           ; Coloca a aleatoriedade da prenda a 0
   MOV R0, alea_p
   MOVB [R0],R1           ; Coloca a aleatoriedade da arma a 0
   MOV R0,lin_i_b
   MOVB R1, [R0]         
   MOV R2,21
   MOVB [R0], R2          ; Reset da linha do objeto de baixo
   MOV R0, var_baixo
   MOV R1,0
   MOVB [R0],R1           ; Desativa o objeto de baixo
   JMP cima_ap

desat_cima:               ; Vai desativar o objeto de cima
   MOV  R2,29   
   MOVB [R0], R2          ; Reset da coluna do objeto de cima
   MOV  R0, alea_a
   MOV  R1,0
   MOVB [R0],R1
   MOV  R0, alea_p
   MOVB [R0],R1
   MOV  R0, lin_i_c
   MOVB R1, [R0]
   MOV  R2,7 
   MOVB [R0], R2          ; Reset da linha do objeto de cima
   MOV  R0, var_cima
   MOV  R1,0
   MOVB [R0],R1           ; Desativa o objeto de cima
   JMP  fim_de_rot

fim_de_rot:
   MOV  R0, var_apg_nin_colid
   MOV  R1,0
   MOVB [R0], R1
   MOV  R0, var_destruir
   MOVB [R0], R1
   POP  R6
   POP  R4
   POP  R3
   POP  R2
   POP  R1
   POP  R0
   RET
