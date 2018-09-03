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
tecla:				    ;Scan da linha e coluna
    MOV  R2, TEC_LIN
	MOV  R3, TEC_COL
    MOVB [R2], R1       ; Atribui o numero da linha ao periferico de linha  
	MOVB R0, [R3]       ; Adquire a coluna do periferico de coluna
	MOV  R4, 0Fh
	AND  R0,R4
	CMP  R0, 0          ; Sem valor?
	JNZ  ha_tecla       ; Guarda ate desprimir a tecla
	SHL  R0, 1          ; Coluna seguinte
	MOV  R4, 8          ; R8 - linha 8
	CMP  R1 , R4        ; Completou as linhas?
	JNZ  aument_linha
	MOV	 R1, 1          ; Reinicia linha
	MOV R8, pausa_jogo
	MOVB R9, [R8]
	CMP  R9, R1
	JZ  tecla
	MOV R8, jogo_ativado
	MOVB R9, [R8]
	CMP R9,0
	JZ tecla
	JMP  fim_tec	    ; Se nao foi clicado, então sai da rotina do teclado	
aument_linha:		    ;Scan da linha seguinte 
    SHL  R1,1           ; Linha seguinte
	JMP  tecla          ; Volta a reiniciar	
ha_tecla:               ;Segurar tecla
    MOVB  [R2],R1       ; A linha e adicionada ao periferico da linha
    MOVB  R6,[R3]       ; R6 - adquire o valor do periferico da coluna
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
   MOV R10,13            ;Tecla a
   CMP R9,R10            
   JZ  preparar_jogo
   MOV R8, jogo_ativado
   MOVB R9, [R8]
   CMP R9,1               ;Vai manter-se no teclado até o jogo estiver ativado
   JNZ inicio
   MOV R10,14            ; Tecla b
   CMP R9,R10
   JZ  loop_pausa      
   MOV R8, pausa_jogo 
   MOVB R9, [R8]
   CMP R9,1                ; Se estiver em pausa vai se manter até a variavel pausa se desligar             
   JZ inicio
   MOV R10,15            ; Tecla c
   CMP R9,R10            
   JZ  terminar_jogo    
   CALL verificar_ninjas
   JMP fim_tec
preparar_jogo:
   CALL prep_jogo
   JZ fim_tec
loop_pausa:
   MOV R8, pausa_jogo
   MOVB R9, [R8]
   MOV R10,1
   ADD R9,R10 
   CMP R9,2
   JZ voltar_jogo
cont_pausa:
   MOV R10,1
   MOVB [R8],R10
   JZ inicio
voltar_jogo:
   MOV R10,0
   MOVB [R8],R10
   JZ fim_tec 
terminar_jogo:
   CALL term_jogo
   JMP inicio
fim_tec:   
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

verificar_ninjas:
   PUSH R0
   PUSH R1
   PUSH R2
   MOV R0, var_teclado
   MOVB R1, [R0]
   MOV R2,1
   CMP R1,R2
   JZ ativa_sobninj1
   MOV R2,2
   CMP R1,R2
   JZ ativa_sobninj2
   MOV R2,3
   CMP R1,R2
   JZ ativa_sobninj3
   MOV R1,4
   CMP R1,R2
   JZ ativa_sobninj4
   MOV R2,5
   CMP R1,R2
   JZ ativa_desninj1
   MOV R2,6
   CMP R1,R2
   JZ ativa_desninj2
   MOV R2,7
   CMP R1,R2
   JZ ativa_desninj3
   MOV R2,8
   CMP R1,R2
   JZ ativa_desninj4
ativa_sobninj1:
   MOV R0, qual_o_ninja
   MOV R1, 1
   MOVB [R0], R1
   MOV R0, sobe_desce
   MOV R1, 1
   MOVB [R0], R1
   CALL colocar_nin
   MOV R0, qual_o_ninja
   MOV R1, 0
   MOVB [R0], R1
   MOV R0, sobe_desce
   MOV R1, 0
   MOVB [R0], R1
   JMP fim_ninj
ativa_sobninj2:
   MOV R0, qual_o_ninja
   MOV R1, 2
   MOVB [R0], R1
   MOV R0, sobe_desce
   MOV R1, 1
   MOVB [R0], R1
   CALL colocar_nin
   MOV R0, qual_o_ninja
   MOV R1, 0
   MOVB [R0], R1
   MOV R0, sobe_desce
   MOV R1, 0
   MOVB [R0], R1
   JMP fim_ninj
ativa_sobninj3:
   MOV R0, qual_o_ninja
   MOV R1, 3
   MOVB [R0], R1
   MOV R0, sobe_desce
   MOV R1, 1
   MOVB [R0], R1
   CALL colocar_nin
   MOV R0, qual_o_ninja
   MOV R1, 0
   MOVB [R0], R1
   MOV R0, sobe_desce
   MOV R1, 0
   MOVB [R0], R1
   JMP fim_ninj
ativa_sobninj4:
   MOV R0, qual_o_ninja
   MOV R1, 4
   MOVB [R0], R1
   MOV R0, sobe_desce
   MOV R1, 1
   MOVB [R0], R1
   CALL colocar_nin
   MOV R0, qual_o_ninja
   MOV R1, 0
   MOVB [R0], R1
   MOV R0, sobe_desce
   MOV R1, 0
   MOVB [R0], R1
   JMP fim_ninj
ativa_desninj1:
   MOV R0, qual_o_ninja
   MOV R1, 1
   MOVB [R0], R1
   MOV R0, sobe_desce
   MOV R1, 2
   MOVB [R0], R1
   CALL colocar_nin
   MOV R0, qual_o_ninja
   MOV R1, 0
   MOVB [R0], R1
   MOV R0, sobe_desce
   MOV R1, 0
   MOVB [R0], R1
   JMP fim_ninj
ativa_desninj2:
   MOV R0, qual_o_ninja
   MOV R1, 2
   MOVB [R0], R1
   MOV R0, sobe_desce
   MOV R1, 2
   MOVB [R0], R1
   CALL colocar_nin
   MOV R0, qual_o_ninja
   MOV R1, 0
   MOVB [R0], R1
   MOV R0, sobe_desce
   MOV R1, 0
   MOVB [R0], R1
   JMP fim_ninj
ativa_desninj3:
   MOV R0, qual_o_ninja
   MOV R1, 3
   MOVB [R0], R1
   MOV R0, sobe_desce
   MOV R1, 2
   MOVB [R0], R1
   CALL colocar_nin
   MOV R0, qual_o_ninja
   MOV R1, 0
   MOVB [R0], R1
   MOV R0, sobe_desce
   MOV R1, 0
   MOVB [R0], R1
   JMP fim_ninj
ativa_desninj4:
   MOV R0, qual_o_ninja
   MOV R1, 4
   MOVB [R0], R1
   MOV R0, sobe_desce
   MOV R1, 2
   MOVB [R0], R1
   CALL colocar_nin
   MOV R0, qual_o_ninja
   MOV R1, 0
   MOVB [R0], R1
   MOV R0, sobe_desce
   MOV R1, 0
   MOVB [R0], R1
fim_ninj:
    POP R2
	POP R1
    POP R0
	RET 