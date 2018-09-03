PIXSCRN   EQU 8000H   ; endereço onde comeca o Pixel Screen
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
    MOV R5,PIXSCRN
    Mov R1,0 ; coluna
	Mov R0,0 ;linha
contador_pixel:
    Mov R3,0	
	Add R3,R0
	Add R3,R1
	Mov R4,2
	Mod R3,R4
	JNZ colu_seguinte
    Call Lig_pixel
colu_seguinte:
    JMP prox_colin
prox_colin:
    ADD R1,1
	Mov R4,31
    CMP R1,R4
	JZ  nova_linha
	JMP contador_pixel
nova_linha:
    Mov R1,0
	Add R0,1
	JMP contador_pixel
Lig_pixel:
	Push R1                  
	Push R0
	Push R3
	MOV R2, 0
	MOV R4, 8
    MOV R6,R1	
	Div R6 , R4             ;R1 = Coluna/8
	Mov R4, 4               
	Mul R0 , R4             ;R0 = Linha*4
	ADD R2, R0               
	ADD R2, R6              ;R2 - byte em que está
    ADD R2, R5              ;Numero do byte no pixel Screen
	MOV R0, 8               
	MOD R1, R0              ;Resto 0 da coluna que nos vai indicar o lugar do bit
	MOV R3, 80h             ;Comeca no 10000000b pois funciona ao contrario
Mascara:	
	CMP R1,0                ;Verifica se ja encontrou o pixel a marcar
	JZ  pixel_screen
	SHR R3,1                ;passa para o bit seguinte
	SUB R1,1                ;subtrai o numero de vezes ate chegar ao bit
	JMP Mascara
pixel_screen:
    MOVB [R2],R3
	Add R5,1
	POP R3
	Pop R0
	Pop R1
	Ret