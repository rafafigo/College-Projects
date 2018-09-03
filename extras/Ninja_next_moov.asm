PIXSCRN   EQU 8000H   ; endereço onde comeca o Pixel Screen
linha_i   EQU 0       ; Linha inicial
coluna_i  EQU 0       ; Coluna inicial
PLACE       1000H
pilha:      TABLE 100H      ; espaço reservado para a pilha 
                            ; (200H bytes, pois são 100H words)
SP_inicial:                 ; este é o endereço (1200H) com que o SP deve ser
tab:        WORD    decrementar_ninja  
            WORD    objectos

lin_i_n: STRING 16         ; linha inicial
col_i_n: STRING 0           ; coluna inicial
lin_i_p: STRING 16          ; linha inicial
col_i_p: STRING 29         ; coluna inicial
lin_i_a: STRING 16         ; linha inicial
col_i_a: STRING 29         ; coluna inicial

var_contador: STRING 0      ; Contador que nos vai dar a informacao de ser prenda ou arma e a sua posicao
var_seguinte: STRING 0      ; quando está a 0 apaga o ninja, quando está a um decrementa o ninja
var_seguinte2: STRING 0     ; quando esta a 0 apaga a prenda ou a arma, quando esta a um anda um ppara o lado
var_seguinte3: STRING 0     ; 
var_descida:  STRING 0      ; ativa a descida, quando o clock está ligado
var_decr:     STRING 0      ; permite apagar os pixeis do pixel screen 
var_ninja:     STRING 0
var_arma:    STRING 0
var_prenda:    STRING 0
var_lado:     STRING 0      ; indica que o clock 2 foi ligado
conta_arma: STRING 0
conta_prend: STRING 0
var_liga_arma: STRING 0
var_liga_presente: STRING 0

Prenda: STRING 3,3
        STRING 0,1,0
		STRING 1,1,1
		STRING 0,1,0
Arma:   STRING 3,3
        STRING 1,0,1
		STRING 0,1,0
		STRING 1,0,1
Ninja:  STRING 3,4
        STRING 0,1,0
        STRING 1,1,1
        STRING 0,1,0
        String 1,0,1
place 0000H

inicio:
	MOV SP, SP_inicial      ; inicializa SP para a palavra a seguir
                            ; à última da pilha
	MOV BTE, tab            ; incializa BTE
	CALL Ninja_pixel        ; ativa o ninja
Ciclo:
    MOV R0, var_contador     
    MOVB R1,[R0]
   	ADD R1,1                ; adiciona 1 ao contador
	MOVB [R0], R1
    EI0                     ; ativa interrupcao 1
	EI
	MOV R0, var_descida     ; endereço que indica se o clock1 esta ativo
	MOVB R1, [R0]           ; R1 = 0 inativo R1=1 ativo        
	CMP R1,0                ; R1 = 0 vai repetir R1=1 vai para a rotina
	JZ Ciclo2
	MOV R0, var_ninja   
	MOV R1,0
	MOVB [R0],R1           ; ativa o ninja
	CALL descer_pixel       ; Numa primeira vez vai apagar o ninja segunda vez vai decrementar o ninja  
	MOV R1,1
	MOVB [R0], R1
	MOV R0, var_descida
	MOV R1,0                ; Desliga o clock
	MOVB [R0], R1           ; Guarda em memoria que o clock1 esta desligado
Ciclo2:	
	EI1                     ; ativa interrupcao 2
	EI
	MOV R0, var_lado        ; endereco que indica se o clock2 esta ativo
	MOVB R1, [R0]           ; R1 = 0 inativo R1= 1 ativo
	CMP R1,0 
	JZ Ciclo
	MOV R0, var_ninja   
	MOV R1,1
	MOVB [R0],R1       
	Call arma_prenda        ; Vai identificar se vai adicionar ao pixel_screen uma arma ou uma prenda
	Call verificar_ar_pr     ; indica onde vai ser disposta essa arma ou prenda a não que uma ja esteja em jogo
	CALL descer_pixel       ; vai descer esse pixel
	MOV R1,0
	MOVB [R0],R1 
	MOV R0,var_lado
	MOV R1,0                
	MOVB [R0], R1
	JMP  Ciclo

arma_prenda:
    PUSH R0
	PUSH R1
	PUSH R2
	MOV R0, var_contador   ; endereco que contem o contador
    MOVB R1,[R0]           ; acesso ao contador pela memoria
	MOV R2,3               
	AND R1, R2             ; le os dois ultimos bits
	MOV R2, R1             ; Se esse valor for 0 - da prenda se nao da arma dando assim aleatoriadade
	CMP R2, 0
	JZ  prend_var         
    MOV R0, conta_arma
	MOV R1,1               ; Foi escolhido arma
	MOVB [R0], R1
	JMP fim_armpr
prend_var:
	MOV R0, conta_prend
	MOV R1,1
	MOVB [R0], R1          ; Foi escolhido prenda
fim_armpr:
    POP R2
	POP R1
	POP R0
	RET

verificar_ar_pr:
    PUSH R0
	PUSH R1
	PUSH R2
    MOV R0, var_arma
	MOVB R1, [R0]      
	MOV R2, 1 
	CMP R1, R2              ; Se R1 = 1, entao ja existe uma arma ativa no jogo e vai verificar se a prenda esta ativa, se R1 = 0 nao existe vai verificar se foi escolhido uma arma para a ativar
	JNZ col_ar
prenda_c:	
	MOV R0, var_prenda
	MOVB R1, [R0]          
	MOV R2, 1 
	CMP R1, R2              ; Se R1 = 1 entao ja existe uma prenda ativa no jogo, se R1 = 0 nao existe e vai verificar se foi escolhida uma prenda para ativar
	JNZ col_bar
	JMP fin_call            ; fim da rotina
col_ar:
    MOV R0, conta_arma
    MOVB R1,[R0]            ; verifica se na rotina anterior foi escolhido uma arma
 	MOV R2, 1
	CMP R1,R2
	JZ incr_valor           ; se foi escolhida uma arma entao vai se ativar a arma, se nao vai verificar a prenda
	JMP prenda_c
col_bar:
    MOV R0, conta_prend
    MOVB R1,[R0]            ; verifica se na rotina anterior foi escolhida uma prenda
    MOV R2, 1
	CMP R1,R2               ; se foi escolhida uma prenda entao vai se ativar a prenda, se nao vai sair da rotina
	JNZ fin_call
inc_var:
	MOV R0, var_prenda
	MOV R1,1
	MOVB [R0],R1
	MOV R0,var_liga_presente
	MOV R1,1
	MOVB [R0], R1
	JMP rotinas
incr_valor:
	MOV R0, var_arma
	MOV R1,1
	MOVB [R0],R1
	MOV R0,var_liga_arma
	MOV R1,1
	MOVB [R0], R1
rotinas:
    Call onde_obj           ; indica-nos onde colocar a prenda ou a arma
	Call Ninja_pixel
fin_call:
	POP R2
	POP R1
	POP R0
    RET

objectos:
    PUSH R0
	PUSH R1
    MOV R0, var_lado        ; ativa a variavel do clock2
	MOV R1, 1
	MOVB [R0], R1           
	POP R1
	POP R0
	RFE

onde_obj:
    PUSH R0
	PUSH R1
	PUSH R2
	PUSH R3
	PUSH R4
	MOV R0, var_contador   ; endereco do contador 
    MOVB R1,[R0]           ; R1 - valor do contador
	MOV R2,15              ; Primeiros 4 bits
	AND R1,R2              ; Retira um valor de 0 a 15
	MOV R2, R1             ; Valor a adicionar ou a subtrair da linha
	Bit R1,1               ; Se o bit 1 for 0 jumpa para next
	JZ next
    MOV R4,1               ; R4 - indica que vai diminuir 
next:
	MOV R0, var_prenda    ; Vai colocar o ojecto arma
	MOVB R1, [R0]
	MOV R3,1
	CMP R1,R3
	JZ  prend_c
    MOV R0, var_arma
	MOVB R1, [R0]
	MOV R3,1
	CMP R1,R3
	JNZ final_col
	MOV R0, lin_i_a
	MOVB R1, [R0]
addi:
    CMP R4,1
	JZ nega                 ; Decrementa
	ADD R1,R2               ; Encrementa
	MOVB [R0], R1
	JMP final_col
nega:
    SUB R1,R2               ; Decrementa
	MOVB [R0], R1
	JMP final_col
prend_c:
    MOV R0, lin_i_p
	MOVB R1, [R0]
    JMP addi
final_col:	
    POP R4
	POP R3
	POP R2
	POP R1
	POP R0
	RET

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
	PUSH R10
	MOV R0, var_ninja
	MOVB R1, [R0]
	CMP R1,0
	JZ nin
	MOV R0, var_liga_arma
	MOVB R1, [R0]
	CMP R1,1
	JZ arm
	MOV R0, var_liga_presente
	MOVB R1, [R0]
	CMP R1,1
	JNZ fim_pixel
prenda:
    MOV R2, lin_i_p
	MOV R5, col_i_p
	MOV R6, Prenda
	JMP Coord_ninja
arm:
    MOV R2, lin_i_a
	MOV R5, col_i_a
	MOV R6, Arma
	JMP Coord_ninja
nin:
    MOV R2, lin_i_n
	MOV R6, Ninja
	MOV R5, col_i_n
Coord_ninja:
	MOVB R0, [R2]          ; R0 - numero da linha
	MOVB R1, [R5]          ; R1 - numero da coluna
	MOV R8, R1
	MOVB R3, [R6]	       ; R3 - largura do ninja
	ADD R6, 1              ; R2 - segundo endereço do ninja
	MOVB R4, [R6]          ; R4 - tamanho do Ninja
    MOV R9, 1              ; contador de colunas
    MOV R10, 1	           ; contador de Linhas
PIXEL_LINHA:
    ADD R6,1               ; R2 - Proximo endereco do ninja (parte para colocar no pixel screen)
	MOVB R7,[R6]           ; adiciona o bit do ninja ao R7
    CMP R7,0               ; se R7 = 0 então passa para o proximo, ou seja nao vai pintar o pixel
	JZ muda_col
    Call Lig_pixel         ; Chama a rotina que adiciona o pixel
muda_col:
    CMP R9,R3              ; verifica se a coluna já chegou ao fim        
	JZ  muda_linh
    ADD R9,1               ; adiciona-se ao contador de colunas um
	ADD R1,1               ; passa para a proxima coluna
	JMP PIXEL_LINHA       
muda_linh:
	MOV R9,1               ; reset do Contador de colunas
    Mov R1,R8              ; reset da coluna
	CMP R10,R4              ; Caso esteja na ultima linha o programa termina
	JZ  fim_ninja           
	ADD R10,1               ; acrescenta um valor ao contador de linhas
	ADD R0,1               ; proxima linha
	JMP PIXEL_LINHA  
fim_ninja:
    MOV R2, lin_i_n          ; endereço da linha
    MOVB R0, [R2]          ; reset da linha
	MOV R2, col_i_n          ; endereco da coluna
    MOVB R1, [R2]          ; reset da coluna
	MOV R9, var_decr
	MOV R2, 0
    MOVB [R9], R2          ; R9 - decrementacao do ninja
	MOV R0,var_liga_arma
	MOV R1,0
	MOVB [R0],R1
	MOV R0, var_liga_presente
	MOVB [R0],R1
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
Lig_pixel:
    Push R2
	Push R3
	Push R4
	Push R5
	Push R9
	MOV R9, 0
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
    POP R9
	POP R5
	POP R4
	POP R3
	POP R2
	RET

decrementar_ninja:
    PUSH R0
	PUSH R1
	MOV R0, var_descida    ; endereço onde ve se ja passou pelo clock
	MOV R1,1
	MOVB [R0],R1           ; Ligar
	POP R1
	POP R0
    RFE

descer_pixel:;Rotina que vai descer o pixel e vai fazer com que as armas ou as prendas andem para o lado
    PUSH R0
	PUSH R1
	PUSH R2
	MOV R6,1               ; R6 = 1 
arm_ap:	
	MOV R0, var_arma     
	MOVB R1, [R0]          ; R1 - se arma esta ativa
	CMP R1,R6              ; Arma esta ativa? - 1
	JZ arm_pr
prend_ap:
    MOV R0, var_prenda
	MOVB R1, [R0]          ; R1 - se prenda esta ativa
	CMP R1, R6             ; Prenda esta ativa? - 1
	JZ prend_pr
ninja_ap:
    MOV R0, var_ninja      
	MOVB R1, [R0]          ; R1 - se ninja esta ativo	
    CMP R1, R6             ; Ninja esta ativo? - 0
	JNZ ninja_pr
	JMP morte_certa
arm_pr:
    MOV R0, var_seguinte2   ; endereço que diz-nos se ja foi apagado
	MOVB R1, [R0]          ; R1 = 0 - apagar R1=1 - andar para o lado
	CMP R1, 1
	JZ andar_arma
	MOV R0, var_decr       ; endereço do apagar pixel
	MOV R1,1               
	MOVB [R0],R1           ; ativa apagar
    MOV R0,var_liga_arma
	MOV R1,1
	MOVB [R0], R1
	Call Ninja_pixel       ; chama a rotina para apagar o pixel
	MOV R0, var_seguinte2
	MOV R1,1
	MOVB [R0], R1
	JMP prend_ap
prend_pr:
    MOV R0, var_seguinte3   ; endereço que diz-nos se ja foi apagado
	MOVB R1, [R0]          ; R1 = 0 - apagar R1=1 - andar para o lado
	CMP R1, 1
	JZ andar_prenda
	MOV R0, var_decr       ; endereço do apagar pixel
	MOV R1,1               
	MOVB [R0],R1           ; ativa apagar
	MOV R0,var_liga_presente
	MOV R1,1
	MOVB [R0], R1
	Call Ninja_pixel       ; chama a rotina para apagar o pixel
	MOV R0,var_seguinte3
	MOV R1,1
	MOVB [R0], R1
	JMP morte_certa
ninja_pr:
	MOV R0, var_seguinte   ; endereço que diz-nos se ja foi apagado para o ninja
	MOVB R1, [R0]          ; R1 = 0 - apagar R1=1 - descer 
	CMP R1, 1              
	JZ andar_ninja
	MOV R0, var_decr       ; endereço do apagar pixel
	MOV R1,1
	MOVB [R0], R1
	CALL Ninja_pixel  
	MOV R0,var_seguinte
	MOV R1,1               ; No proximo vai descer
	MOVB [R0], R1
	JMP morte_certa
andar_arma:
    MOV R0, col_i_a             
	MOVB R1, [R0]
	MOV R2,0
	CMP R1,R2
	JZ arma_var
	SUB R1,1
	MOVB [R0], R1
	MOV R0,var_liga_arma
	MOV R1,1
	MOVB [R0], R1
	Call Ninja_pixel
	MOV R0, var_seguinte2
	MOV R1, 0
	MOVB [R0],R1
	JMP prend_ap
andar_prenda:
    MOV R0, col_i_p
	MOVB R1, [R0]
	MOV R2,0
	CMP R1,R2
	JZ prenda_var
	SUB R1,1
	MOVB [R0], R1
	MOV R0,var_liga_presente
	MOV R1,1
	MOVB [R0], R1
	Call Ninja_pixel
	MOV R0, var_seguinte2
	MOV R1, 0
	MOVB [R0],R1
	JMP morte_certa	
andar_ninja:
	MOV R1, lin_i_n        ; endereço da linha
	MOVB R0, [R1]          ; numero da linha atual
	MOV R2,29              
	CMP R0,R2              ; se ja chegou ao fim morre
	JZ morte_certa
	ADD R0, 1              ; adiciona uma linha
	MOVB [R1],R0           ; coloca o valor na linha
	MOV R0, var_ninja
	Call Ninja_pixel       ; chama a rotina para apagar o pixel
	MOV R0, var_seguinte
	MOV R1, 0
	MOVB [R0],R1
	JMP morte_certa
arma_var:
    MOV R2,29
    MOVB [R0], R2        
	MOV R0, var_arma
	MOV R1,0
    MOVB [R0],R1
    JMP prend_ap
prenda_var:
    MOV R2,29
    MOVB [R0], R2
	MOV R0, var_prenda
	MOV R1,0
    MOVB [R0],R1
	JMP morte_certa
morte_certa:
	POP R2
	POP R1
	POP R0
	RET

