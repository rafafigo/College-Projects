DISPLAYS   EQU 0A000H   ; endereço dos displays de 7 segmentos (periférico POUT-1)
TEC_LIN    EQU 0C000H   ; endereço das linhas do teclado (periférico POUT-2)
TEC_COL    EQU 0E000H   ; endereço das colunas do teclado (periférico PIN)

PLACE      0
inicio:		            ;inicializações

    MOV  R4, DISPLAYS   ; endereço do periférico dos displays

CALL keyboard	
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
inicio
    MOV  R1, 1          ; R1 - linha 1
    MOV  R7, 0          ; R7 - contador de linha
    MOV  R5, 0	        ; R5 - contador de coluna
tecla:				    ;Scan da linha e coluna
    MOV  R2, TEC_LIN
	MOV  R3, TEC_COL
    MOVB [R2], R1       ; Atribui o numero da linha ao periferico de linha  
	MOVB R0, [R3]       ; Adquire a coluna do periferico de coluna
	MOV  R4, 0F
	AND  R0,R4
	CMP  R0, 0          ; Sem valor?
	JNZ  ha_tecla       ; Guarda ate desprimir a tecla
	SHL  R0, 1          ; Coluna seguinte
	MOV  R4, 8          ; R8 - linha 8
	CMP  R1 , R4        ; Completou as linhas?
	JNZ  aument_linha
	MOV	 R1, 1          ; Reinicia linha
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
   MOV R10,1
   JZ ativa_sobninj1
   MOV R10,2
   JZ ativa_sobninj2
   MOV R10,3
   JZ ativa_sobninj3
   MOV R10,4
   JZ ativa_sobninj4
   MOV R10,5
   JZ ativa_desninj1
   MOV R10,6
   JZ ativa_desninj2
   MOV R10,7
   JZ ativa_desninj3
   MOV R10,8
   JZ ativa_desninj4
preparar jogo:
   Call apagar_display
   Call pixel_inic_fin
   MOV R8, jogo_ativado
   MOV R9,1
   MOVB [R8], R9
   MOV R8,reset_display
   MOVB [R8], R9
   CALL display
   MOV R8,reset_display
   MOV R9,0
   MOVB [R8], R9
   MOV R8, var_ninja
   MOV R9,1
   MOVB [R8], R9
   MOV R8, coloc_desc_sub
   MOVB [R8], R9
   CALL colocar_nin
   MOV R9,0
   MOVB [R8],R9
   MOV R8, var_ninja
   MOVB [R8], R9
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
   CALL apagar display
   MOV R8,jogo_ativado
   MOV R9,0
   MOVB [R8],R9
   JMP inicio
ativa_sobninj1:
   MOV R8, qual_o_ninja
   MOV R9, 1
   MOVB [R8], R9
   MOV R8, sobe_desce
   MOV R9, 1
   MOVB [R8], R9
   CALL colocar_nin
   MOV R8, qual_o_ninja
   MOV R9, 0
   MOVB [R8], R9
   MOV R8, sobe_desce
   MOV R9, 0
   MOVB [R8], R9
   JMP fim_tec
ativa_sobninj2:
   MOV R8, qual_o_ninja
   MOV R9, 2
   MOVB [R8], R9
   MOV R8, sobe_desce
   MOV R9, 1
   MOVB [R8], R9
   CALL colocar_nin
   MOV R8, qual_o_ninja
   MOV R9, 0
   MOVB [R8], R9
   MOV R8, sobe_desce
   MOV R9, 0
   MOVB [R8], R9
   JMP fim_tec
ativa_sobninj3:
   MOV R8, qual_o_ninja
   MOV R9, 3
   MOVB [R8], R9
   MOV R8, sobe_desce
   MOV R9, 1
   MOVB [R8], R9
   CALL colocar_nin
   MOV R8, qual_o_ninja
   MOV R9, 0
   MOVB [R8], R9
   MOV R8, sobe_desce
   MOV R9, 0
   MOVB [R8], R9
   JMP fim_tec
ativa_sobninj4:
   MOV R8, qual_o_ninja
   MOV R9, 4
   MOVB [R8], R9
   MOV R8, sobe_desce
   MOV R9, 1
   MOVB [R8], R9
   CALL colocar_nin
   MOV R8, qual_o_ninja
   MOV R9, 0
   MOVB [R8], R9
   MOV R8, sobe_desce
   MOV R9, 0
   MOVB [R8], R9
   JMP fim_tec
ativa_desninj1:
   MOV R8, qual_o_ninja
   MOV R9, 1
   MOVB [R8], R9
   MOV R8, sobe_desce
   MOV R9, 2
   MOVB [R8], R9
   CALL colocar_nin
   MOV R8, qual_o_ninja
   MOV R9, 0
   MOVB [R8], R9
   MOV R8, sobe_desce
   MOV R9, 0
   MOVB [R8], R9
   JMP fim_tec
ativa_desninj2:
   MOV R8, qual_o_ninja
   MOV R9, 2
   MOVB [R8], R9
   MOV R8, sobe_desce
   MOV R9, 2
   MOVB [R8], R9
   CALL colocar_nin
   MOV R8, qual_o_ninja
   MOV R9, 0
   MOVB [R8], R9
   MOV R8, sobe_desce
   MOV R9, 0
   MOVB [R8], R9
   JMP fim_tec
ativa_desninj3:
   MOV R8, qual_o_ninja
   MOV R9, 3
   MOVB [R8], R9
   MOV R8, sobe_desce
   MOV R9, 2
   MOVB [R8], R9
   CALL colocar_nin
   MOV R8, qual_o_ninja
   MOV R9, 0
   MOVB [R8], R9
   MOV R8, sobe_desce
   MOV R9, 0
   MOVB [R8], R9
   JMP fim_tec
ativa_desninj4:
   MOV R8, qual_o_ninja
   MOV R9, 4
   MOVB [R8], R9
   MOV R8, sobe_desce
   MOV R9, 2
   MOVB [R8], R9
   CALL colocar_nin
   MOV R8, qual_o_ninja
   MOV R9, 0
   MOVB [R8], R9
   MOV R8, sobe_desce
   MOV R9, 0
   MOVB [R8], R9
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

	
pixel_inic_fin:
   PUSH R0
   PUSH R1
   MOV R0,var_pinta_ecra
   MOV R1,1
   MOVB [R0],R1
   CALL lig_pixel
   MOV R1,0
   MOVB [R0],R1
   RET
apagar_display:
   MOV R0,var_apag_ecra
   MOV R1,1
   MOVB [R0],R1
   CALL lig_pixel
   MOV R1,0
   MOVB [R0],R1
   POP R0 
   POP R1
   RET

  

  
display:
   PUSH R0
   PUSH R1
   PUSH R2
   PUSH R3
   PUSH R4
   PUSH R5
   MOV R0, reset_display
   MOVB R1, [R0]
   MOV R2,1
   CMP R1,R2
   JZ  display_apagar
adicionar_presente:     ;soma 3 ao valor do display 
    MOV  R0,valor_do_display
	MOVB R3,[R0]
    MOV  R4,3           ; R8 - valor a ser somado
	Add  R3, R4         ; adiciona ao valor no display(R3)
	JMP  condic 
condic:                 ;condicionantes ao valor do display
	MOV  R4,99          ; R8 - numero maximo que o display pode atingir
	CMP  R3,R4          ; e maior ou igual que 99?
    JGT  disp_fin1
	MOV  R4,0           ; R8 - numero minimo que o display pode atingir
	CMP  R3,R4          ; e maior ou igual que 0?
	JLT  disp_fin2
	JMP  dec_1

dec_1:                  ;passar o numero a decimal
    MOV  R5, R3         
	Mov  R1, R3
    MOV  R4,10	        ; R8 - usado para obter o valor em decimal
	DIV  R5,R4          ; R5 - parte inteira em decimal
	MOD  R1,R4          ; R1 - resto em decimal
	JMP  disp
	
disp:					;colocar valor no display
    SHL  R5, 4          ; R5 - parte inteira no nibble high(dezenas)
	OR   R5,R1          ; R1 - resto no nibble low(unidades)
addicionar_valor:
	MOVB [R4], R5       ; valores importados para o display, na memoria(R4)
	JMP  ciclo
	
disp_fin1:			    ;valor maximo do display
	MOV  R3,R4          ; R3 fica com o valor 99
	JMP  dec_1
	
disp_fin2:		    	;valor minimo do display
	MOV  R3,R4         ; R3 fica com o valor 0
	JMP  dec_1
display_apagar:
   MOV R5,0
   JMP addicionar_valor
fim_dis:
   MOV R0, adicionar_disp
   MOV R1,0
   MOVB [R0],R1
   MOV R0, reset_display
   MOVB [R0],R1
   POP R5
   POP R4
   POP R3
   POP R2
   POP R1
   POP R0
   ret
   

   

   
