PIXSCRN   EQU 8000H   ; endereço onde comeca o Pixel 
Pixeis_Pares   EQU 01010101b ;pares
Pixeis_Impares EQU 10101010b ;impares1
PLACE       1000H
pilha:      TABLE 100H      ; espaço reservado para a pilha 
                            ; (200H bytes, pois são 100H words)
SP_inicial:                 ; este é o endereço (1200H) com que o SP deve ser inicializado
place 0000H
inicio:
	MOV SP, SP_inicial      ; inicializa SP para a palavra a seguir
ciclo:                      ;reset de colunas e chamada da funcao
	Mov R0,0                ; R0 - linhas
    MOv R1,0                ; R1 - Colunas
    Call lig_pixel          ; chamar a função para aparecer o inicio
lig_pixel:                  ;funçao pixel do inicio do jogo
	Push R1               
	Push R0
	MOV R4, PIXSCRN         ; R4 - endereço onde comeca o pixel screen
voltar_pixel:               ;onde se descobre qual o byte
	MOV R5, 8              
    MOV R6, R1	                  
	Div R6, R5              ; R6 - Coluna/8
	Mov R5, 4              
	Mov R2, R0
	Mul R2 , R5             ; R2 - Linha*4
	ADD R2, R6              ; R2 - Numero do byte
    ADD R2, R4              ; R2 - Numero do byte no pixel Screen
	MOV R6, 2  
    MOV R7,R0	            ; R7 - Linha par ou linha impar
	MOD R7,R6               ; se for impar adiciona 10101010b
	JNZ pix_imp
	MOV R3, Pixeis_Pares    ; se for par adiciona 01010101b
	JMP pixel_screen
pix_imp:                    ;coloca a mascara impar
     MOV R3, Pixeis_Impares
pixel_screen:
    MOVB [R2],R3            ; adiciona a máscara ao pixelscreen
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
fim_pixel:	               ;fim do porgrama   
	Pop R0
	Pop R1
	Ret