PIXSCRN   EQU 8000H   ; endereço onde comeca o Pixel Screen
PLACE       1000H
pilha:      TABLE 100H      ; espaço reservado para a pilha 
                            ; (200H bytes, pois são 100H words)
SP_inicial:                 ; este é o endereço (1200H) com que o SP deve ser
Ninja: STRING 3,4
       STRING 0,1,0
       STRING 1,1,1
       STRING 0,1,0
       String 1,0,1
place 0000H
inicio:
	MOV SP, SP_inicial      ; inicializa SP para a palavra a seguir
                            ; à última da pilha
	Mov R1, 0              ;coluna
	Mov R0, 0              ;linha
	Call Ninja_pixel

Ninja_pixel:
    MOV R2, Ninja
	MOVB R3, [R2]	
	ADD R2, 1
	MOVB R4, [R2]
Novo_String:
    ADD R2,1
PIXEL_LINHA:
	MOVB R5,[R2]
	CMP R1,R3
	JZ  muda_linh
    CMP R5,0
	JZ muda_col
    JMP Lig_pixel
muda_col:
	ADD R1,1
	JMP Novo_String
muda_linh:
    ADD R0,1
    Mov R1,0
	CMP R0,R4
	JZ  fim_ninja
	JMP PIXEL_LINHA
Lig_pixel:
	MOV R8, 0
	MOV R9, 8
	MOV R8, R1
	Div R8 , R9             ;R1 = Coluna/8
	Mov R9, 4 
    MOV R12, R0	
	Mul R12 , R9             ;R0 = Linha*4
	ADD R8, R12               
	MOV R9, PIXSCRN         ;R4 - endereço onde comeca o pixel screen
    ADD R8, R9              ;Numero do byte no pixel Screen
	MOV R9, 8 
    MOV R11, R1	
	MOD R11, R9             ;Resto 0 da coluna que nos vai indicar o lugar do bit
	MOV R10, 80h             ;Comeca no 10000000b pois funciona ao contrario
Mascara:	
	CMP R11,0                ;Verifica se ja encontrou o pixel a marcar
	JZ  pixel_screen
	SHR R10,1                ;passa para o bit seguinte
	SUB R11,1                ;subtrai o numero de vezes ate chegar ao bit
	JMP Mascara
pixel_screen:
    MovB R9,[R8]
	OR R10, R9
    MOVB [R8],R10	        ;liga o bit no pixel screen   
    JMP muda_col
fim_ninja:
    RET