; **********************************************************************
DISPLAYS   EQU 0A000H   ; endereço dos displays de 7 segmentos (periférico POUT-1)
TEC_LIN    EQU 0C000H   ; endereço das linhas do teclado (periférico POUT-2)
TEC_COL    EQU 0E000H   ; endereço das colunas do teclado (periférico PIN)


; **********************************************************************
; * Codigo
; **********************************************************************
PLACE      0
inicio:		
                        ; inicializações
    MOV  R2, TEC_LIN    ; endereço do periférico das linhas
    MOV  R3, TEC_COL    ; endereço do periférico das colunas
    MOV  R4, DISPLAYS   ; endereço do periférico dos displays
						; corpo principal do programa
    MOVB [R4], R1       ; escreve zero nos displays
	MOV  R6, 0          ; 0 valor para o display 
	MOV  R9, 0          ; 0 valor da operaçao
ciclo:
    MOV  R1, 1          ; reset linha
    MOV  R7, 0          ; reset valor da linha 
    MOV  R5, 0	        ; reset valor da coluna
tecla:				    ; scan da linha inicial
    MOVB [R2], R1       ; atribui o numero da coluna 
	MOVB R0, [R3]       ; adquire a coluna
	CMP  R0, 0          ; sem valor?
	JNZ  ha_tecla       ; Se tiver temos coluna e linha - passa para guardar ate desprimir a tecla
	SHL  R0, 1          ; passa a 2 ou a 4 ou a 8
	MOV  R8, 8          ; linha 8
	CMP  R1 , R8        ; se nao tiver na 8 linha avanca para a linha seguinte
	JNZ  aumant_linha
	MOV	 R1, 1          ; primeira linha de novo
	JMP  tecla	        ; volta a repetir até acabar as colunas	
aumant_linha:		    ; scan da linha seguinte 
    SHL  R1,1           ; anda para a linha 2 4 ou 8
	JMP  tecla          ; volta a reiniciar
num:
	CMP  R0,1           ; verifica se ja ta no 1
	JNZ  adici          ; vai guardar um bit da coluna
	CMP  R1,1           ; verifica se ja ta no 1
    JNZ  adici_2        ;vai guardar um bit da linha
	JMP  valr           ; se ja tiverem os dois obtem-se o numero do teclado
adici:
	SHR  R0,1           ; anda um bit para o lado da coluna
	ADD  R5,1           ; add 1 ao R5 - contador de colunas
	JMP  num
adici_2:
	SHR  R1,1           ; anda um bit para o lado da linha
	Add  R7,1           ; add 1 ao R7 - contador de colunas
	JMP  num
valr:	                ; Binario para Hexa
    MOV  R8,4
	MUL  R7,R8          ; linha x 4
	Add  R7,R5          ; linha x 4 + coluna	
                        ; Atribuição de operacões as teclas
	MOV  R8,10          ; a
	CMP  R7,R8          ; ignorar - Teclas em "branco"
	JZ   ciclo  
	MOV  R8,11          ; b
	CMP  R7,R8          ; ignorar - Teclas em "branco"
	JZ   ciclo  
	MOV  R8,12          ; c
	CMP  R7,R8          ; somar
	JZ   addi          	;
    MOV  R8,13	        ; d
	CMP  R7, R8         ; Subtrair
	JZ   subbi
    MOV	 R8,14          ; e
	CMP  R7,R8          ; Somar 3
	JZ   adi_3
    MOV  R8,15	        ; f
	CMP  R7,R8          ; Subtrair 3
	JZ   subi_3
	CMP  R9,0           ; ja passou 1 vez pelas operacoes?
	JZ   ciclo          ; se for igual volta ao inicio
	CMP  R9,1           ; R9 é 1 entao salta para addi_2 - Ja foi "clickado" na operaçao de somar
	JZ   addi_2
	CMP  R9,2           ; Salta para subbi_2 - Ja foi "clickado" na operaçao de subtrair
	JZ   subbi_2
addi:
	Mov  R9,1           ; Permite a operaçao de somar
	JMP  ciclo          ; agora vai contar!
addi_2:
    Add  R6, R7         ; Adiciona o novo numero ao valor obtido previamente
	MOV  R8,99         ; Comparar valor em decimal, 99d é 153h
	CMP  R6,R8          ; é maior ou igual que 99?
    JGT  disp_fin1
	JMP  dec_1          ; colocar no display
	MOV  R8,0
	CMP  R6,R8
	JLT  disp_fin2
	JMP  dec_1
subbi:
	Mov  R9,2           ; Permite operaçao de subtrair
	JMP  ciclo          ; agora vai contar!
subbi_2:
	SUB  R6, R7         ; Subtração do numero que está no display
	MOV  R8,99         ; Comparar valor em decimal, 99d é 153h
	CMP  R6,R8          ; é maior ou igual que 99?
    JGT  disp_fin1
	MOV  R8,0           ; comparar valor em decimal, 0h é 0d 
    CMP  R6,R8
	JLT  disp_fin2
	JMP  dec_1
adi_3: 					; Função de incrementar valor de display por 3
    MOV  R8,3
	Add  R6, R8         ; adiciona ao numero que poderia ja la estar
	MOV  R8,99         ; Comparar valor em decimal, 99d é 153h
	CMP  R6,R8          ; é maior ou igual que 99?
    JGT  disp_fin1
	MOV  R8,0           ; comparar valor em decimal, 0h é 0d 
    CMP  R6,R8
	JLT  disp_fin2
	JMP  dec_1  
subi_3:				    ; funcao de diminuir valor de display por 3
    MOV  R8,3
	SUB  R6, R8         ; adiciona ao numero que poderia ja la estar
	MOV  R8,99
	CMP  R6,R8
	JGT  disp_fin1
	MOV  R8,0           ; comparar valor em decimal, 0h é 0d 
    CMP  R6,R8
	JLT  disp_fin2
dec_1:
    MOV  R5, R6         ; divide se o novo numero sumado
	Mov  R1, R6
    MOV  R8,10	
	Div  R5,R8          ; parte inteira em decimal
	MOD  R1,R8          ; resto em decimal
	JMP  disp
disp:					;Colocar valor no display
    SHL  R5, 4          ; R5 vai 4 bits para o lado
	OR   R5,R1          ; é implementado o segundo valor
    JLT  disp_fin2
disp1:
	MOVB  [R4], R5      ; aparece no display
	MOV  R9,   0
	JMP  ciclo
disp_fin1:			    ; valor maximo do display
	Mov  R8,99          ; Comparar valor em hexadecimal 
	MOV  R6,R8          ; R6 ainda está em hexadecimal
	JMP  dec_1
disp_fin2:		    	; valor minimo do display
	MOV  R6,R8
	JMP  disp1
ha_tecla:               ;Segurar tecla
    MOVB  [R2],R1        
    MOVB  R10,[R3]      ; botão da coluna clicado
	CMP   R10,0         ; botao largado
	JNZ   ha_tecla     
	JMP   num