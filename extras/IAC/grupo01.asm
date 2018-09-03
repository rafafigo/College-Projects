; **********************************************************************
; * Grupo 01:                                                          *
; * Ana David (90702)                                                  *
; * Manuel Mascarenhas (90751)                                         *
; * Rafael Figueiredo (90770)                                          *
; **********************************************************************
DISPLAYS   EQU 0A000H   ; endereço dos displays de 7 segmentos (periférico POUT-1)
TEC_LIN    EQU 0C000H   ; endereço das linhas do teclado (periférico POUT-2)
TEC_COL    EQU 0E000H   ; endereço das colunas do teclado (periférico PIN)

PLACE      0
inicio:		            ;inicializações
    MOV  R2, TEC_LIN    ; endereço do periférico das linhas
    MOV  R3, TEC_COL    ; endereço do periférico das colunas
    MOV  R4, DISPLAYS   ; endereço do periférico dos displays
    MOVB [R4], R1       ; escreve zero nos displays
	MOV  R6, 0          ; R6 - valor do display em decimal
	MOV  R9, 0          ; R9 - verifica se ja foi iniciada uma operacao
	
ciclo:                  ;reset de registos
    MOV  R1, 1          ; R1 - linha 1
    MOV  R7, 0          ; R7 - contador de linha
    MOV  R5, 0	        ; R5 - contador de coluna
	
tecla:				    ;scan da linha e coluna
    MOVB [R2], R1       ; atribui o numero da linha ao periferico de linha  
	MOVB R0, [R3]       ; adquire a coluna do periferico de coluna
	CMP  R0, 0          ; sem valor?
	JNZ  ha_tecla       ; guarda ate desprimir a tecla
	SHL  R0, 1          ; coluna seguinte
	MOV  R8, 8          ; R8 - linha 8
	CMP  R1 , R8        ; completou as linhas?
	JNZ  aument_linha
	MOV	 R1, 1          ; reinicia linha
	JMP  tecla	        ; repete ate ser "clicado"
	
aument_linha:		    ;scan da linha seguinte 
    SHL  R1,1           ; linha seguinte
	JMP  tecla          ; volta a reiniciar
	
ha_tecla:               ;segurar tecla
    MOVB  [R2],R1       ; a linha e adicionada ao periferico da linha
    MOVB  R10,[R3]      ; R10 - adquire o valor do periferico da coluna
	CMP   R10,0         ; botao largado
	JNZ   ha_tecla     
	JMP   num
	
num:
	CMP  R0,1           ; verifica se R0 esta na primeira coluna
	JNZ  adici          ; vai guardar um bit da coluna
	CMP  R1,1           ; verifica se ja ta no 1
    JNZ  adici_2        ; vai guardar um bit da linha
	JMP  valr           ; se ja tiverem os dois obtem-se o numero do teclado
	
adici:
	SHR  R0,1           ; anda um bit para o lado da coluna
	ADD  R5,1           ; R5 - contador de colunas 
	JMP  num
	
adici_2:
	SHR  R1,1           ; um bit para o lado da linha
	ADD  R7,1           ; R7 - contador de linhas
	JMP  num
	
valr:	                ;binario para Hexa
    MOV  R8,4
	MUL  R7,R8          
	ADD  R7,R5			; R7 - valor do teclado da linha e coluna "clicada"

teclas_branco:          ;retirar as teclas a e b
	MOV  R8,10          ; R8 - tecla a
	CMP  R7,R8          ; ignorar - teclas em "branco"
	JZ   ciclo  
	MOV  R8,11          ; R8 - tecla b
	CMP  R7,R8          ; ignorar - teclas em "branco"
	JZ   ciclo  

op_tec:                 ;operacoes das teclas
	MOV  R8,12          ; R8 - tecla c
	CMP  R7,R8          ; somar
	JZ   addi          	
    MOV  R8,13	        ; R8 - tecla d
	CMP  R7, R8         ; subtrair
	JZ   subbi
    MOV	 R8,14          ; R8 - tecla e
	CMP  R7,R8          ; soma 3
	JZ   adi_3
    MOV  R8,15	        ; R8 - tecla f
	CMP  R7,R8          ; subtrair 3
	JZ   subi_3
	CMP  R9,0           ; R9 - registo de operacoes, neste caso verifica se ja passou
	JZ   ciclo          ; se for igual volta ao inicio
	CMP  R9,1           ; se R9 = 1 entao salta para addi_2 - Ja foi "clicado" na operacao de somar
	JZ   addi_2
	CMP  R9,2           ; se R9 = 2 entao alta para subbi_2 - Ja foi "clicado" na operacao de subtrair
	JZ   subbi_2
	
addi:                   ;vai adquirir o numero a ser somado
	MOV  R9,1           ; R9 - soma
	JMP  ciclo          ; "clicar" no numero a ser somado

subbi:                  ;vai adquirir o numero a ser subtraido
	MOV  R9,2           ; permite operacao de subtrair
	JMP  ciclo          ; "clicar" no numero a ser somado
	
addi_2:                 ;adiciona o valor    
    ADD  R6, R7         ; R6 - numero guardado das operacoes anteriores, adiciona ao R6 o valor escolhido(R7)
    JMP  condic     
	
subbi_2:                ;subtrai o valor
	SUB  R6, R7         ; R6 - numero guardado das operacoes anteriores, subtrai ao R6 o valor escolhido(R7)
	JMP  condic
	
adi_3: 					;soma 3 ao valor do display 
    MOV  R8,3           ; R8 - valor a ser somado
	Add  R6, R8         ; adiciona ao valor no display(R6)
	JMP  condic 
	
subi_3:				    ;subtrai 3 ao valor do display
    MOV  R8,3           ; R8 - valor a ser subtraido
	SUB  R6, R8         ; subtrai ao valor no display(R6)
	JMP  condic
	
condic:                 ;condicionantes ao valor do display
	MOV  R8,99          ; R8 - numero maximo que o display pode atingir
	CMP  R6,R8          ; e maior ou igual que 99?
    JGT  disp_fin1
	MOV  R8,0           ; R8 - numero minimo que o display pode atingir
	CMP  R6,R8          ; e maior ou igual que 0?
	JLT  disp_fin2
	JMP  dec_1

dec_1:                  ;passar o numero a decimal
    MOV  R5, R6         
	Mov  R1, R6
    MOV  R8,10	        ; R8 - usado para obter o valor em decimal
	DIV  R5,R8          ; R5 - parte inteira em decimal
	MOD  R1,R8          ; R1 - resto em decimal
	JMP  disp
	
disp:					;colocar valor no display
    SHL  R5, 4          ; R5 - parte inteira no nibble high(dezenas)
	OR   R5,R1          ; R1 - resto no nibble low(unidades)
	MOVB [R4], R5       ; valores importados para o display, na memoria(R4)
	MOV  R9, 0          ; reset as operacoes
	JMP  ciclo
	
disp_fin1:			    ;valor maximo do display
	MOV  R6,R8          ; R6 fica com o valor 99
	JMP  dec_1
	
disp_fin2:		    	;valor minimo do display
	MOV  R6,R8          ; R6 fica com o valor 0
	JMP  dec_1