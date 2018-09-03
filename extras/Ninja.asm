PIXSCRN   EQU 8000H   ; endereço onde comeca o Pixel Screen
linha_i   EQU 0       ; Linha inicial
coluna_i  EQU 0       ; Coluna inicial
PLACE       1000H
pilha:      TABLE 100H      ; espaço reservado para a pilha 
                            ; (200H bytes, pois são 100H words)
SP_inicial:                 ; este é o endereço (1200H) com que o SP deve ser
tab:        WORD    decrementar_ninja  
lin_i: STRING 0             ; linha inicial
col_i: STRING 0             ; coluna inicial
var_descida: STRING 0
var_decr: STRING 0
Ninja: STRING 3,4
       STRING 0,1,0
       STRING 1,1,1
       STRING 0,1,0
       String 1,0,1
place 0000H
inicio:
	MOV SP, SP_inicial      ; inicializa SP para a palavra a seguir
                            ; à última da pilha
	MOV BTE, tab            ; incializa BTE
	Call Ninja_pixel
Ciclo:	
    EI0                     ; permite interrupcoes
	EI
	Mov R0,var_descida
	MOVB R1, [R0]
	CMP R1,1
	JNZ Ciclo
	Call descer_pixel
	JMP  Ciclo

Ninja_pixel:
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
	MOV R2, lin_i          ; R2 - endereço da linha
	MOVB R0, [R2]          ; R0 - numero da linha
	MOV R2, col_i          ; R2 - endereco da coluna
	MOVB R1, [R2]          ; R1 - numero da coluna
	MOV R2, var_decr       ; R2 - endereço para limpar ninja
    MOVB R9, [R2]	       ; R9 - se tiver a 1 - ligado, Se tiver a 0 desligado
    MOV R2, Ninja          ; R2 - primeiro endereço do ninja
	MOVB R3, [R2]	       ; R3 - largura do ninja
	ADD R2, 1              ; R2 - segundo endereço do ninja
	MOVB R4, [R2]          ; R4 - tamanho do Ninja
    MOV R5, 1              ; contador de colunas
    MOV R6, 1	           ; contador de Linhas
PIXEL_LINHA:
    ADD R2,1               ; R2 - Proximo endereco do ninja (parte para colocar no pixel screen)
	MOVB R7,[R2]           ; adiciona o bit do ninja ao R7
    CMP R7,0               ; se R7 = 0 então passa para o proximo, ou seja nao vai pintar o pixel
	JZ muda_col
    Call Lig_pixel         ; Chama a rotina que adiciona o pixel
muda_col:
    CMP R5,R3              ; verifica se a coluna já chegou ao fim        
	JZ  muda_linh
    ADD R5,1               ; adiciona-se ao contador de colunas um
	ADD R1,1               ; passa para a proxima coluna
	JMP PIXEL_LINHA       
muda_linh:
	MOV R5,1               ; reset do Contador de colunas
    Mov R1,R8              ; reset da coluna
	CMP R6,R4              ; Caso esteja na ultima linha o programa termina
	JZ  fim_ninja           
	ADD R6,1               ; acrescenta um valor ao contador de linhas
	ADD R0,1               ; proxima linha
	JMP PIXEL_LINHA  
fim_ninja:
    MOV R2, lin_i          ; endereço da linha
    MOVB R0, [R2]          ; reset da linha
	MOV R2, col_i          ; endereco da coluna
    MOVB R1, [R2]          ; reset da coluna
	MOV R9, var_decr
	MOV R2, 0
    MOVB [R9], R2          ; R9 - decrementacao do ninja
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
Lig_pixel:
    Push R2
	Push R3
	Push R4
	Push R5
	MOV R2, 8               
	MOV R3, R1
	Div R3 , R2             ; R8 = Coluna/8
	Mov R2, 4 
    MOV R4, R0	
	Mul R4 , R2             ; R4 = Linha*4
	ADD R3, R4              ; R3 - numero do byte
	MOV R2, PIXSCRN         ; R4 - endereço onde comeca o pixel screen
    ADD R3, R2              ; R3 - Numero do byte no pixel Screen
	MOV R2, 8 
    MOV R4, R1	
	MOD R4, R2             ; resto da coluna que nos vai indicar o lugar do bit
	MOV R5, 80h            ; comeca no 10000000b pois funciona ao contrario
Mascara:	
	CMP R4,0               ; Verifica se ja encontrou o pixel a marcar
	JZ  pixel_screen
	SHR R5,1               ; passa para o bit seguinte
	SUB R4,1               ; subtrai o numero de vezes ate chegar ao bit
	JMP Mascara
pixel_screen:
    MOV R2, var_decr
    MOVB R9, [R2]
	CMP R9,1               ; se for um quer dizer que foi ativado a limpeza do ninja
	JZ limpa_screen
	MovB R2,[R3]          
	OR R5, R2
    MOVB [R3],R5	       ; liga o bit no pixel screen
    JMP fim_pixel	
limpa_screen:
    MovB R2,[R3]
	NOT R5              
	AND R5, R2
    MOVB [R3],R5	       ; desliga o bit no pixel screen
fim_pixel:	
    POP R5
	POP R4
	POP R3
	POP R2
	RET
decrementar_ninja:
    PUSH R0
	PUSH R1
	Mov R0,var_descida
	MOV R1,1
	MOVB [R0], R1
	POP R1
	POP R0
    RFE
descer_pixel:
    PUSH R0
	PUSH R1
	PUSH R2
	PUSH R3
    MOV R0, var_decr       ; endereço do apagar pixel
	MOV R2,1
	MOVB [R0],R2       
	Call Ninja_pixel       ; chama a rotina para apagar o pixel
	MOV R1, lin_i
	MOVB R0, [R1]
	MOV R3,28
	CMP R0,R3
	JZ morte_certa
	ADD R0, 1
	MOVB [R1],R0
	Call Ninja_pixel       ; chama a rotina para apagar o pixel
morte_certa:
    POP R3
	POP R2
	POP R1
	POP R0
	RET
	
