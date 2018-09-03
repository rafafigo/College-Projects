PIXSCRN   EQU 8000H   ; endereço onde comeca o Pixel Screen

PLACE       1000H
pilha:      TABLE 100H      ; Espaço reservado para a pilha 
SP_inicial:               
tab:        WORD    pr_ar_int ; Interrupcão das prendas e das armas liga ao clock 0 
            WORD    nin_int   ; Interrupção dos ninjas ligado ao clock 1
           
;;; Local onde as linhas e colunas, do ninja, da arma e da prenda estão memorizados
lin_i_n: STRING 10         ; Linha inicial do ninja
col_i_n: STRING 0          ; Coluna inicial do ninja
lin_i_p: STRING 16         ; Linha inicial da prenda
col_i_p: STRING 29         ; Coluna inicial da prenda
lin_i_a: STRING 16         ; Linha inicial da arma
col_i_a: STRING 29         ; Coluna inicial da arma
;;; Diferentes variaveries que ajudam a guardar a informação necessaria para que o jogo funcione
var_contador:   STRING 0      ; Contador que vai ser utilizado para indicar as posiçoes das armas e das prendas no pixel screen
;;; Os var_seguintes, são utilizados como forma de perceber se os diversos objectos já foram apagados antes de colocar a nova posição ou o contrário
var_seguinte_n:    STRING 0       ; 0 - apaga o ninja, 1 - anda um para baixo
var_seguinte_a:    STRING 0       ; 0 - apaga a prenda,  1 - anda um para o lado
var_seguinte_p:    STRING 0       ; 0 - apaga a arma,  1 - anda um para o lado
;;; Os var_ints são usados como garantia de que passa pelas interrupções
var_int_n:         STRING 0       ; Verifica que passou pela interrupcao1
var_int_a_p:       STRING 0       ; Verifica que passou pela interrupcao0
;;; Quando activados permitem apagar os pixeis e não colocar pixeis
var_apagar_nin:       STRING 0      ; quando ligada permite que o pixel_screen apagues os pixeis e não coloque
var_apagar_prend:     STRING 0      ; quando ligada permite que o pixel_screen apagues os pixeis e não coloque
var_apagar_arm:       STRING 0      ; quando ligada permite que o pixel_screen apagues os pixeis e não coloque
;;; Verificam se o ninja, a arma e a prenda estão ativos ou estão desativos
var_ninja:      STRING 0      ; 0 - ninja ativo, 1 - ninja inativo
var_arma:       STRING 0      ; 0 - arma inativa, 1 - arma ativa
var_prenda:     STRING 0      ; 0 - prenda inativa, 1 - prenda ativa
;;; Diz-nos se do contador, foi escolhida uma arma ou uma prenda
alea_a:     STRING 0      ; 1 - escolhe a arma da aleatoriedade     
alea_p:     STRING 0      ; 1 - escolhe a prenda da aleatoriedade
;;; Variaveis que sao ligadas unica e exclusivamente para escolher entre a arma ou a prenda
var_liga_arma:  STRING 0      ; indica que é para apagar ou colocar a arma
var_liga_presente: STRING 0   ; indica que é para apagar ou colocar a prenda
;;; Forma da Prenda,Arma,Ninja
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
;;;Inicializaçoes
inicio:
	MOV SP, SP_inicial      ; inicializa SP para a palavra a seguir
                            ; à última da pilha
	MOV BTE, tab            ; incializa BTE
	CALL n_a_p_pixel        ; ativa o ninja
;;; Ciclo principal do programa
Ciclo:
    CALL dec_nin            
	CALL esc_arm_nin        
	JMP Ciclo
;;; Rotina que faz descer o ninja segundo a interrupção	
dec_nin: ;interrupçao 1 trata do ninja    
	PUSH R0
	PUSH R1
	MOV R0, var_contador        
    MOVB R1,[R0]            ; R1 - contador aleatorio
   	ADD R1,1                ; Adiciona 1 ao contador, que irá assim provocar a aleatoriedade
	MOVB [R0], R1           ; Guarda em memoria o novo valor
    EI1                     ; Ativa interrupcao 1
	EI
	MOV R0, var_int_n       
	MOVB R1, [R0]           ; R1 = 0 não passou pela interrupçao R1=1 passou pela interrupçao       
	CMP R1,0                ; R1 = 0 vai esperar que passe pela interrupçao R1=1 vai para o passo seguinte
	JZ nin_ret
	MOV R0, var_ninja       
	MOV R1,0                
	MOVB [R0],R1            ; Ativa o ninja
	CALL mov_nap_pixel       ; Numa primeira vez vai apagar o ninja segunda vez vai decrementar o ninja  
	MOV R1,1
	MOVB [R0], R1           ; Desativa o ninja
	MOV R0, var_int_n       ;
	MOV R1,0                ; 
	MOVB [R0], R1           ; Volta a esperar que passe pela interrupção
nin_ret:
    POP R1
	POP R0
	RET
;;; Rotina que escolhe por aleatoriedade o ninja ou a arma e coloca os no pixel_screen numa dada posição tambem aleatoria e, trata ainda do seu movimento
esc_arm_nin:
    PUSH R0
    PUSH R1	
	EI0                     ; Ativa interrupcao 0
	EI
	MOV R0, var_int_a_p
	MOVB R1, [R0]           ; R1 = 0 não passou pela interrupçao R1=1 passou pela interrupçao
	CMP R1,0                ; R1 = 0 vai esperar que passe pela interrupçao R1=1 vai para o passo seguinte
	JZ pr_ar_ret
	MOV R0, var_ninja      
	MOV R1,1                
	MOVB [R0],R1            ; Ninja desativado
	CALL arma_prenda        ; Vai identificar se vai adicionar ao pixel_screen uma arma ou uma prenda, 
	CALL onde_ar_pr         ; Indica onde vai ser disposta essa arma ou prenda a não ser que uma ja esteja em jogo
	CALL verificar_ar_pr    ; Vai provocar o movimento dos objectos que estejam ativos       
	MOV R0,var_int_a_p       
	MOV R1,0                       
	MOVB [R0], R1           ; Volta a esperar que passe pela interrupção
pr_ar_ret:
    POP R1
	POP R0
    RET

nin_int:; ;Interrupçao 1
    PUSH R0
	PUSH R1
	MOV R0, var_int_n    
	MOV R1,1
	MOVB [R0],R1           ; Ativa a variavel da passagem pela interrupcao
	POP R1
	POP R0
    RFE
	
pr_ar_int:;interrupcao 0
    PUSH R0
	PUSH R1
    MOV R0, var_int_a_p        
	MOV R1, 1
	MOVB [R0], R1          ; Ativa a variavel da passagem pela interrupcao
	POP R1
	POP R0
	RFE

;;; Rotina que escolhe se é ninja, arma ou prenda e verifica as suas linhas e colunas
n_a_p_pixel:
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
	CMP R1,0                ; Verifica se o ninja está ativo
	JZ nin
	MOV R0, var_liga_arma   
	MOVB R1, [R0]
	CMP R1,1                ; Verifica se a arma está ativa
	JZ arm
	MOV R0, var_liga_presente 
	MOVB R1, [R0]
	CMP R1,1                ; Verifica se a prenda está ativa
	JNZ fim_pixel
;;; Vai retirar a linha, a coluna, e a forma da prenda, para registos, vai tambem colocar num registo se é para apagar a prenda ou para colocar
prenda:
    MOV R2, lin_i_p         
	MOV R5, col_i_p
	MOV R6, Prenda
	MOV R0, var_apagar_prend
	MOVB R9, [R0]
	JMP Coord_n_a_p
;;; Vai retirar os endereços da linha, da coluna, e da forma da arma, para registos, vai tambem colocar num registo se é para apagar a arma ou para a colocar no pixel screen
arm:
    MOV R2, lin_i_a
	MOV R5, col_i_a
	MOV R6, Arma
    MOV R0, var_apagar_arm
	MOVB R9, [R0]
	JMP Coord_n_a_p
;;; Vai retirar a linha, a coluna, e a forma do ninja , para registos, vai tambem colocar num registo se é para apagar o ninja ou para colocar
nin:
    MOV R2, lin_i_n
	MOV R6, Ninja
	MOV R5, col_i_n
    MOV R0, var_apagar_nin
	MOVB R9, [R0]
;;; Obtem os valores e coloca-os em registos
Coord_n_a_p:
	MOVB R0, [R2]          ; R0 - numero da linha
	MOVB R1, [R5]          ; R1 - numero da coluna
	MOV R8, R1             ; R8 - Garantia da linha inicial
	MOVB R3, [R6]	       ; R3 - largura do objecto
	ADD R6, 1              ; R6 - segundo endereço do objecto
	MOVB R4, [R6]          ; R4 - tamanho do objecto
    MOV R11, 1             ; R11 - contador de colunas
    MOV R10, 1	           ; R10 - contador de Linhas
;;;  Verifica se o o bit é 0 ou 1 de forma a saber se vai pintar ou não esse pixel
PIXEL_LINHA:
    ADD R6,1               ; R6 - Proximo endereco do objecto (parte para colocar no pixel screen)
	MOVB R7,[R6]           ; Adiciona o bit do ninja ao R7
    CMP R7,0               ; Se R7 = 0 então passa para o proximo, ou seja nao vai pintar o pixel
	JZ muda_col            ; Muda logo para a proxima coluna 
    Call Lig_pixel         ; Chama a rotina que adiciona o pixel
;;; Faz a mudanca de coluna e se está tiver na ultima coluna vai para muda_linh
muda_col:
    CMP R11,R3             ; Verifica se a coluna já chegou ao fim, e se chegar vai mudar de linha        
	JZ  muda_linh
    ADD R11,1              ; Adiciona-se ao contador de colunas 1
	ADD R1,1               ; Passa para a proxima coluna
	JMP PIXEL_LINHA  
;;; Faz o reset da coluna e verifica se está na ultima linha	
muda_linh:
	MOV R11,1              ; Reset do Contador de colunas
    Mov R1,R8              ; Reset da coluna
	CMP R10,R4             ; Caso esteja na ultima linha o programa termina
	JZ  fim_ninja           
	ADD R10,1              ; Acrescenta um valor ao contador de linhas
	ADD R0,1               ; Proxima linha
	JMP PIXEL_LINHA  
;;; Acaba o apagamento ou a colocaçao dos pixeis no pixel_screen
fim_ninja:
    MOV R2, lin_i_n       
    MOVB R0, [R2]          ; Reset da linha
	MOV R2, col_i_n        
    MOVB R1, [R2]          ; Reset da coluna
	MOV R0,var_liga_arma
	MOV R1,0
	MOVB [R0],R1           ; Desliga a variavel arma
	MOV R0, var_liga_presente
	MOVB [R0],R1           ; Desliga a variavel presente
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
;;; Rotina que apaga ou coloca os pixeis no pixel_screen
Lig_pixel:
    Push R2
	Push R3
	Push R4
	Push R5
	MOV R2, 8               
	MOV R3, R1
	Div R3 , R2             ; R3 = Coluna/8
	Mov R2, 4 
    MOV R4, R0	
	Mul R4 , R2             ; R4 = Linha*4
	ADD R3, R4              ; R3 - numero do byte
	MOV R2, PIXSCRN         ; R2 - endereço onde comeca o pixel screen
    ADD R3, R2              ; R3 - Numero do byte no pixel Screen
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
pixel_screen:
	CMP R9,1               ; R9 - Se for um quer dizer que foi ativado a limpeza do ninja
	JZ limpa_screen
	MovB R2,[R3]          
	OR R5, R2              ; Adiciona o bit ao que já lá está
    MOVB [R3],R5	       ; Liga o bit no pixel screen
    JMP fim_pixel	
limpa_screen:
    MovB R2,[R3]
	NOT R5                 ; Inverte           
	AND R5, R2             ; Apaga o bit
    MOVB [R3],R5	       ; Desliga o bit no pixel screen
fim_pixel:	
	POP R5
	POP R4
	POP R3
	POP R2
	RET

;;;Rotina que pode descer a linha do ninja, , mover para a esquerda a coluna da arma ou da prenda e ativar o apagar de cada um deles
mov_nap_pixel:
    PUSH R0
	PUSH R1
	PUSH R2
	PUSH R6
	MOV R6,1               ; R6 = 1
ninja_ap:
    MOV R0, var_ninja      
	MOVB R1, [R0]          ; Vai verificar o ninja está ativo
    CMP R1, R6             ; Ninja esta ativo? = 0
	JNZ ninja_pr   	
arm_ap:	
	MOV R0, var_liga_arma     
	MOVB R1, [R0]          ; Vai verificar se a arma está ativa
	CMP R1,R6              ; Arma esta ativa? = 1
	JZ arm_pr
prend_ap:
    MOV R0, var_liga_presente
	MOVB R1, [R0]          ; Vai verificar se a arma está ativa
	CMP R1, R6             ; Prenda esta ativa? = 1
	JZ prend_pr
	JMP fim_de_rot
arm_pr:;Verifica se vai apagar ou andar para o lado
    MOV R0, var_seguinte_a  
	MOVB R1, [R0]          ; R1: 0 = apagar, 1 = andar para o lado
	CMP R1, 1
	JZ andar_arma
apagar_arm:; Vai Apagar a arma do pixel screen 
	MOV R0, var_apagar_arm       
	MOV R1,1               
	MOVB [R0],R1           ; Ativa apagar arma
    MOV R0,var_liga_arma
	MOV R1,1               ; Liga a variavel que indica que é a arma
	MOVB [R0], R1
	CALL n_a_p_pixel       ; Chama a rotina para apagar o pixel
	MOV R0, var_apagar_arm 
	MOV R1, 0
    MOVB [R0], R1          ; Desativa apagar arma
	MOV R0,var_liga_arma
	MOV R1,0
	MOVB [R0], R1          ; Desliga a variavel que indica que é a arma
	MOV R0, var_seguinte_a
	MOV R1,1
	MOVB [R0], R1          ; Indica que já apagou e que agora vais andar para o lado
	JMP prend_ap
prend_pr:;Verifica se vai apagar ou andar para o lado
    MOV R0, var_seguinte_p   ; endereço que diz-nos se ja foi apagado
	MOVB R1, [R0]          ; R1: 0 - apagar, 1 - andar para o lado
	CMP R1, 1
	JZ andar_prenda
apagar_prend:; Vai apagar a prenda do pixel_screen
	MOV R0, var_apagar_prend    
	MOV R1,1               
	MOVB [R0],R1           ; Ativa apagar prenda
	MOV R0,var_liga_presente
	MOV R1,1
	MOVB [R0], R1          ; Liga a variavel que indica que é a prenda
	Call n_a_p_pixel       ; Chama a rotina para apagar o pixel
	MOV R0, var_apagar_prend
	MOV R1, 0
    MOVB [R0], R1          ; Desativa apagar ninja
	MOV R0,var_liga_arma
	MOV R1,0
	MOVB [R0], R1          ; Desliga a variavel que indica que é a prenda
	MOV R0,var_seguinte_p
	MOV R1,1
	MOVB [R0], R1          ; Indica que já apagou e que agora vai andar para o lado
	JMP fim_de_rot
ninja_pr:; Indica se vai apagar o ninja ou descer
	MOV R0, var_seguinte_n  
	MOVB R1, [R0]          ; R1: 0 - apagar, 1 - descer 
	CMP R1, 1              
	JZ andar_ninja 
apagar_nin:; Vai apagar o ninja do pixel_screen
	MOV R0, var_apagar_nin   
	MOV R1,1
	MOVB [R0], R1          ;Ativa a variavel que apaga o ninja
	CALL n_a_p_pixel  
	MOV R0, var_apagar_nin
	MOV R1, 0
    MOVB [R0], R1          ; Desativa o apagar ninja
	MOV R0,var_seguinte_n
	MOV R1,1               
	MOVB [R0], R1          ; Indica que ja apagou e agora vai descer
	JMP fim_de_rot
andar_arma:; A arma vai andar para a esquerda 
    MOV R0, col_i_a             
	MOVB R1, [R0]          ; R1 - Coluna da arma
	MOV R2,0           
	CMP R1,R2              ; Se a coluna for igual a 0, significa que a arma bateu na parede
	JZ desat_arma             
	SUB R1,1               ; Anda uma coluna para a esquerda
	MOVB [R0], R1          ; Nova coluna
	MOV R0,var_liga_arma
	MOV R1,1
	MOVB [R0], R1          ; Ativa a variavel arma
	Call n_a_p_pixel
	MOV R0,var_liga_arma
	MOV R1,0
	MOVB [R0], R1          ; Desativa a variavel arma
	MOV R0, var_seguinte_a
	MOV R1, 0
	MOVB [R0],R1           ; Na proxima vai voltar a apagar
	JMP prend_ap
andar_prenda:; A prenda vai andar para a esquerda
    MOV R0, col_i_p
	MOVB R1, [R0]          ; Coluna da prenda
	MOV R2,0
	CMP R1,R2              ; Verifica se a prenda já bateu na parede
	JZ desat_prend
	SUB R1,1               ; Anda uma coluna para a esquerda
	MOVB [R0], R1
	MOV R0,var_liga_presente
	MOV R1,1
	MOVB [R0], R1          ; Ativa a variavel do presente
	Call n_a_p_pixel       ; Colocar o presente no pixel screen
	MOV R0,var_liga_arma
	MOV R1,0
	MOVB [R0], R1          ; Desativa a variavel presente
	MOV R0, var_seguinte_p
	MOV R1, 0
	MOVB [R0],R1           ; Na proxima vai voltar a apagar
	JMP fim_de_rot	
andar_ninja:; O ninja vai descer
	MOV R1, lin_i_n        
	MOVB R0, [R1]          ; Numero da linha atual
	MOV R2,28              
	CMP R0,R2              ; Se já chegou ao fim do pixel screen, morre
	JZ fim_de_rot
	ADD R0, 1              ; Linha seguinte
	MOVB [R1],R0           ; Coloca o valor da linha em memoria
	Call n_a_p_pixel       ; chama a rotina para descer o pixel
	MOV R0, var_seguinte_n
	MOV R1, 0              ; Na proxima vai voltar a apagar
	MOVB [R0],R1
	JMP fim_de_rot
desat_arma:; Vai desativar a arma
    MOV R2,29
    MOVB [R0], R2          ; Reset da coluna da arma
    MOV R0,lin_i_a
    MOVB R1, [R0]          
   	MOV R2,16
	MOVB [R0], R2          ; Reset da linha da arma
	MOV R0, var_arma
	MOV R1,0
    MOVB [R0],R1           ; Desativa a arma
	MOV R0, var_seguinte_a
	MOV R1, 0
	MOVB [R0],R1           ; Vai apagar da proxima vez
    JMP prend_ap
desat_prend:;Vai desativar a prenda
    MOV R2,29              
    MOVB [R0], R2          ; Reset da coluna da prenda
	MOV R0, lin_i_p
	MOVB R1, [R0]
   	MOV R2,16 
	MOVB [R0], R2          ; Reset da linha da arma
	MOV R0, var_prenda
	MOV R1,0
    MOVB [R0],R1           ; Desativa a prenda
	MOV R0, var_seguinte_p
	MOV R1, 0
	MOVB [R0],R1           ; Vai apagar da proxima vez
	JMP fim_de_rot
fim_de_rot:
	POP R6
	POP R2
	POP R1
	POP R0
	RET
	
verificar_ar_pr:
    PUSH R0
	PUSH R1
	PUSH R2
pix_ar:
    MOV R0,var_arma
	MOVB R1,[R0]
	MOV R2,1
	CMP R1,R2
	JZ arma_con 
pix_pr:
    MOV R0, var_prenda
	MOVB R1,[R0]
	MOV R2,1
	CMP R1,R2
	JZ prend_con
	JMP pix_fi
arma_con:
    MOV R0, var_liga_arma
	MOV R1,1
	MOVB [R0], R1
	CALL mov_nap_pixel
	MOV R1,0
	MOVB [R0], R1
	JMP pix_pr
prend_con:
    MOV R0, var_liga_presente
	MOV R1,1
	MOVB [R0], R1
	CALL mov_nap_pixel
	MOV R1,0
	MOVB [R0], R1
pix_fi:
    POP R2
	POP R1
	POP R0
    RET	
	

arma_prenda:;Vai usar o contador aleatorio para ligar ou uma prenda ou uma arma
    PUSH R0
	PUSH R1
	PUSH R2
	MOV R0, var_contador   ; endereco que contem o contador
    MOVB R1,[R0]           ; acesso ao contador pela memoria
	MOV R2,3               
	AND R1, R2             ; le os dois ultimos bits
	MOV R2, R1             ; Se esse valor for 0 - da prenda se nao da arma 75% Arma, 25%Prenda
	CMP R2, 0            
	JZ  prend_var         
    MOV R0, alea_a
	MOV R1,1               ; Foi escolhido arma
	MOVB [R0], R1
	JMP fim_armpr
prend_var:
	MOV R0, alea_p       
	MOV R1,1
	MOVB [R0], R1          ; Foi escolhido prenda
fim_armpr:
    POP R2
	POP R1
	POP R0
	RET

onde_ar_pr:;verifica se ja existe alguma arma ou alguma prenda em jogo
    PUSH R0
	PUSH R1
	PUSH R2
arma_c:; verifica se existe uma arma em jogo
    MOV R0, var_arma
	MOVB R1, [R0]      
	MOV R2, 1 
	CMP R1, R2              ; Se R1 = 1, entao ja existe uma arma ativa no jogo e vai verificar se a prenda esta ativa, se R1 = 0 nao existe vai verificar se foi escolhido uma arma para a ativar
	JNZ col_ar
prenda_c:; verifica se existe uma prenda em jogo	
	MOV R0, var_prenda
	MOVB R1, [R0]          
	MOV R2, 1 
	CMP R1, R2              ; Se R1 = 1 entao ja existe uma prenda ativa no jogo, se R1 = 0 nao existe e vai verificar se foi escolhida uma prenda para ativar
	JNZ col_bar
	JMP fin_call            ; fim da rotina
col_ar:;verifica se foi escolhido uma arma na aleatoriedade
    MOV R0, alea_a
    MOVB R1,[R0]            ; verifica se na rotina anterior foi escolhido uma arma
 	MOV R2, 1
	CMP R1,R2
	JZ incr_valor           ; se foi escolhida uma arma entao vai se ativar a arma, se nao vai verificar a prenda
	JMP prenda_c
col_bar:;verifica se foi escolhido uma prenda na aleatoriedade
    MOV R0, alea_p
    MOVB R1,[R0]            ; verifica se na rotina anterior foi escolhida uma prenda
    MOV R2, 1
	CMP R1,R2               ; se foi escolhida uma prenda entao vai se ativar a prenda, se nao vai sair da rotina
	JNZ fin_call
inc_var:;vai ativar a prenda
	MOV R0, var_prenda
	MOV R1,1
	MOVB [R0],R1
	MOV R0,var_liga_presente
	MOV R1,1
	MOVB [R0], R1
	JMP rotinas
incr_valor:; vai ativar a arma
	MOV R0, var_arma
	MOV R1,1
	MOVB [R0],R1
	MOV R0,var_liga_arma
	MOV R1,1
	MOVB [R0], R1
rotinas:;Colocar prenda ou arma em jogo
    CALL onde_obj           ; indica-nos onde colocar a prenda ou a arma
	CALL linhas_iguais
fin_call:
	POP R2
	POP R1
	POP R0
    RET
	
linhas_iguais:
    PUSH R0
	PUSH R1
	PUSH R2
	PUSH R3
	PUSH R4
    MOV R0, var_liga_presente ; verifica se vai ser adicionado presente
	MOVB R1, [R0]
	MOV R2,1
	CMP R1,R2 ; se vai ser passa para prenda_l
	JZ  prenda_l
	MOV R0, var_liga_arma ; verifica se vai ser adicionado arma
	MOVB R1, [R0]
	MOV R2,1
	CMP R1,R2
	JZ arma_l
	JMP ig
prenda_l:
    MOV R0, var_arma
	MOV R2,1
	MOVB R1, [R0]
	CMP R1,R2
	JNZ ig
    MOV R0, lin_i_a
	MOVB R1, [R0]
	MOV R2, lin_i_p
	MOVB R3, [R2]
	CMP R1,R3
	JLT igual_p
	MOV R4,3
	ADD R3,R4
	CMP R1, R3
	JGT igual_p
    MOV R0, var_prenda
	MOV R1,0
	MOVB [R0], R1
	JMP ig
arma_l:
    MOV R0, var_prenda
	MOV R2,1
	MOVB R1, [R0]
	CMP R1,R2
	JNZ ig
    MOV R0, lin_i_a
	MOVB R1, [R0]
	MOV R2, lin_i_p
	MOVB R3, [R2]
	CMP R3,R1
	JLT igual_a
	MOV R4,3
	ADD R1,R4
	CMP R3, R1
	JGT igual_a
	MOV R0, var_arma
	MOV R1,0
	MOVB [R0], R1
	JMP ig
igual_a:
    MOV R0, var_seguinte_a
	MOVB R1, [R0]
	MOV R0, var_seguinte_p
    MOVB R2, [R0]
	CMP R1,R2
	JZ ig
	CALL n_a_p_pixel
	JMP ig
igual_p:
    MOV R0, var_seguinte_a
	MOVB R1, [R0]
	MOV R0, var_seguinte_p
    MOVB R2, [R0]
	CMP R1,R2
	JNZ ig
	CALL n_a_p_pixel
ig:    
    POP R4
    POP R3
	POP R2
	POP R1
	POP R0
    RET


onde_obj:
    PUSH R0
	PUSH R1
	PUSH R2
	PUSH R3
	PUSH R4
	MOV R0, var_contador   ; endereco do contador 
    MOVB R1,[R0]           ; R1 - valor do contador
	MOV R2,12              ; Primeiros 4 bits
	AND R1,R2              ; Retira um valor de 0 a 12
	MOV R2, R1             ; Valor a adicionar ou a subtrair da linha
	Bit R1,1               ; Se o bit 1 for 0 jumpa para next
	JZ next
    MOV R4,1               ; R4 - indica que vai diminuir 
next:; funcao que vai verificar se e a prenda ou a arma que sao geradas
	MOV R0, var_liga_presente     ; verifica se a prenda vai ser adicionada
	MOVB R1, [R0]
	MOV R3,1
	CMP R1,R3
	JZ  prend_c
    MOV R0, var_liga_arma
	MOVB R1, [R0]
	MOV R3,1
	CMP R1,R3
	JNZ final_col
arm_c:; adiciona o endereco da linha da arma
	MOV R0, lin_i_a
	MOVB R1, [R0]
	JMP addi
prend_c:; adiciona o endereco da linha da prenda
    MOV R0, lin_i_p
	MOVB R1, [R0]
addi:;Adiciona ao 16 que esta na linha
    CMP R4,1
	JZ nega                 ; Decrementa
	ADD R1,R2               ; Encrementa
	MOVB [R0], R1
	JMP final_col
nega:; Subtrai ao 16 que esta na linha
    SUB R1,R2               ; Decrementa
	MOVB [R0], R1
final_col: 	
    POP R4
	POP R3
	POP R2
	POP R1
	POP R0
	RET
	




