PIXSCRN   EQU 8000H   ; endereço onde comeca o Pixel 
Pixeis_Pares   EQU 01010101b ;pares
Pixeis_Impares EQU 10101010b ;impares1
Pixeis_apagados EQU 00000000b

PLACE       1000H
pilha:      TABLE 100H      ; espaço reservado para a pilha 
                            ; (200H bytes, pois são 100H words)
SP_inicial:                 ; este é o endereço (1200H) com que o SP deve ser inicializado
var_pinta_ecra: STRING 0
var_apag_ecra:  STRING 0
place 0000H
inicio:
	MOV SP, SP_inicial      ; inicializa SP para a palavra a seguir
;;; Rotina que apaga ou coloca os pixeis no pixel_screen
Lig_pixel:
    Push R2
	Push R3
	Push R4
	Push R5
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
	MOD R7,R6               ; se for impar adiciona 10101010b
	JNZ pix_imp
	MOV R2, Pixeis_Pares    ; se for par adiciona 01010101b
	JMP pixel_screen
pix_imp:                    ;coloca a mascara impar
     MOV R2, Pixeis_Impares
	 JMP pixel_screen
ecra_apagar:
	 MOV R2, Pixeis_apagados
pixel_screen:
    MOVB [R3],R2            ; adiciona a máscara ao pixelscreen
    MOV R6,8	            ; passa para o byte seguinte  
	Add R1,R6
	Mov R6,32               ; verifica se a coluna acabou 
	CMP R1,R6               ; coluna ja chegou ao fim?
	JZ  verificar_colu
	JMP voltar_pixel
verificar_colu:            ;verifica se o programa vai terminar ou se vai passar para a proxima linha
    CMP R0,R6              ; verifica se a linha chegou ao fim
	JZ  fim_pixel          ; Se sim o pixel inicial esta feito
	MOV R1,0               ; reset das colunas
	Add R0,1               ; proxima linha
	JMP voltar_pixel   
fim_pixel:	
	POP R5
	POP R4
	POP R3
	POP R2
	RET