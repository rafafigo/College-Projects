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

	Call Ninja_pixel

Ninja_pixel:
    Mov R1, 0              ; R1 - numero da coluna
	Mov R0, 0              ; R0 - numero da linha
    MOV R2, Ninja          ; R2 - primeiro endereço do ninja
	MOVB R3, [R2]	       ; R3 - Largura do ninja
	ADD R2, 1              ; R2 - segundo endereço do ninja
	MOVB R4, [R2]          ; R4 - Tamanho do Ninja
    MOV R5, 1              ; Contador de colunas
    MOV R6, 1	           ; Contador de Linhas
PIXEL_LINHA:
    ADD R2,1
	MOVB R7,[R2]           ; R7 - Verificar se este bit vai ser colocado no display
    CMP R7,0               ; Se R7 = 0 então passa para o proximo
	JZ muda_col
    Call Lig_pixel         ; Vai ligar o pixel
muda_col:
    CMP R5,R3              ; Verifica se a coluna já chegou ao fim        
	JZ  muda_linh
    ADD R5,1
	ADD R1,1               ; Passa para o proximo bit
	JMP PIXEL_LINHA       
muda_linh:
	MOV R5,1
    Mov R1,0               ; Proxima Coluna
	CMP R6,R4              ; Se chegou há ultima linha então retorna
	JZ  fim_ninja           
	ADD R6,1               ;
	ADD R0,1               ; Proxima linha
	JMP PIXEL_LINHA       
fim_ninja:
    RET
Lig_pixel:
	Push R1                  
	Push R0
	PUSH R3
	PUSH R4
	PUSH R2
	MOV R2, 0
	MOV R4, 8
	MOV R2, R1
	Div R2 , R4             ;R1 = Coluna/8
	Mov R4, 4               
	Mul R0 , R4             ;R0 = Linha*4
	ADD R2, R0               
	MOV R4, PIXSCRN         ;R4 - endereço onde comeca o pixel screen
    ADD R2, R4              ;Numero do byte no pixel Screen
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
    MovB R4,[R2]
	OR R3, R4
    MOVB [R2],R3	        ;liga o bit no pixel screen   
	POP R2
	POP R4
	POP R3
	Pop R0
	Pop R1
	Ret