PIXSCRN   EQU 8000H   ; endereço onde comeca o Pixel 
Pixeis_Pares   EQU 01010101b ;pares
Pixeis_Impares EQU 10101010b ;impares1
PLACE       1000H
pilha:      TABLE 100H      ; espaço reservado para a pilha 
                            ; (200H bytes, pois são 100H words)
SP_inicial:                 ; este é o endereço (1200H) com que o SP deve ser 
                            ; inicializado. O 1.º end. de retorno será 
                            ; armazenado em 11FEH (1200H-2)
place 0000H
inicio:
	MOV SP, SP_inicial      ; inicializa SP para a palavra a seguir
                            ; à última da pilha
ciclo:
	Mov R0,0 ;linha
    MOv R1,0 ;Coluna
    Call Lig_pixel
Lig_pixel:
	Push R1               
	Push R0
	Push R3
	MOV R4, PIXSCRN          ;R4 - endereço onde comeca o pixel screen
voltar:
	MOV R5, 8              	           
	Div R1, R5              ;R6 = Coluna/8
	Mov R5, 4
	Mul R0 , R5             ;R2 = Linha*4
	ADD R2, R1              ;R2 = Numero do byte
    Add R2, R0
	ADD R2, R4              ;R2 = Numero do byte no pixel Screen
	MOV R6, 2               
	MOD R0,R6               ;Se for impar adiciona 10101010
	JNZ pix_imp
	MOV R3, Pixeis_Pares    ;Se for par adiciona 01010101
	JMP pixel_screen
pix_imp:
     MOV R3, Pixeis_Impares
	 JMP pixel_screen
pixel_screen:
    MOVB [R2],R3            ;Adiciona a mascara ao pixelscreen
	Mov R6,31                
	CMP R1,R6               ;Coluna ja chegou ao fim?
	JZ  Verificar
    MOV R6,8	            ;Adiciona 8 e siga!  
	Add R1,R6
	JMP voltar
Verificar:
    CMP R0,R6              ;Linha chegou ao fim?
	JZ  fim                ;Se sim acaba
	Add R0,1               ;Proxima linha
	JMP voltar
fim:	
	POP R3
	Pop R0
	Pop R1
	Ret