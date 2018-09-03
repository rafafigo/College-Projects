PIXSCRN   EQU 8000H   ; endereço onde comeca o Pixel Screen

PLACE       1000H
pilha:      TABLE 100H      ; Espaço reservado para a pilha 
SP_inicial:               
tab:        WORD    pr_ar_int ; Interrupcão das prendas e das armas liga ao clock 0 
            WORD    nin_int   ; Interrupção dos ninjas ligado ao clock 1
           
;;; Local onde as linhas e colunas, do ninja, da arma e da prenda estão memorizados
lin_i_n: STRING 10         ; Linha inicial do ninja
col_i_n: STRING 0          ; Coluna inicial do ninja
lin_i_c: STRING 7         ; Linha inicial de cima
col_i_c: STRING 29         ; Coluna inicial de cima
lin_i_b: STRING 21         ; Linha inicial de baixo
col_i_b: STRING 29         ; Coluna inicial de baixo
;;; Diferentes variaveries que ajudam a guardar a informação necessaria para que o jogo funcione
var_contador:   STRING 0      ; Contador que vai ser utilizado para indicar as posiçoes dos objectos no pixel screen
;;; Os var_ints são usados como garantia de que passa pelas interrupções
var_int_n:         STRING 0       ; Verifica que passou pela interrupcao1
var_int_a_p:       STRING 0       ; Verifica que passou pela interrupcao0
;;; Quando activados permitem apagar os pixeis e não colocar pixeis
var_apagar_nin:       STRING 0     ; quando ligada permite que o pixel_screen apague os pixeis e não coloque o ninja
var_apagar_cima:     STRING 0      ; quando ligada permite que o pixel_screen apague os pixeis e não coloque do objecto de cima
var_apagar_baixo:       STRING 0   ; quando ligada permite que o pixel_screen apague os pixeis e não coloque do objecto de baixo
;;; Verificam se o ninja, o objecto de cima e o objecto de baixo estão ativos ou estão desativos
var_ninja:      STRING 0      ; 0 - ninja ativo, 1 - ninja inativo
var_baixo:       STRING 0      ; 0 - objecto baixo inativo, 1 - objecto de cima ativo
var_cima:     STRING 0      ; 0 - objecto de baixo inativo, 1 - objecto de cima ativo
;;; Diz-nos se do contador, foi escolhida uma arma ou uma prenda
alea_a:     STRING 0      ; 1 - escolhe a arma da aleatoriedade     
alea_p:     STRING 0      ; 1 - escolhe a prenda da aleatoriedade
;;;  Liga as permissoes para apagar ou colocar os pixeis no pixel_screen dos objectos de cima ou de baixo
var_liga_cima: STRING 0
var_liga_baixo: STRING 0
;;; ativa o fim do jogo
fim_do_jogo: STRING 0
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
	MOVB [R0],R1             ; Ativa o ninja
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
;;; Rotina que escolhe por aleatoriedade a prenda ou a arma e coloca-os no pixel_screen numa dada posição tambem aleatoria e, trata ainda do seu movimento
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
	CALL arma_prenda        ; Vai identificar se vai adicionar ao pixel_screen uma arma ou uma prenda 
	CALL onde_ar_pr         ; Indica onde vai ser disposta essa arma ou prenda em cima ou em baixo a não ser que ja esteja em jogo
	CALL mov_nap_pixel      ; Vai provocar o movimento dos objectos que estejam ativos       
	CALL colisoes           ; Verifica se o ninja colide com algum objecto
	CALL fimjogo            ; Verifica se o jogo já acabou
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

;;; Rotina que escolhe se é ninja, objecto de cima ou objecto de baixo e verifica as suas linhas e colunas
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
    MOV R0,var_liga_cima
	MOVB R1, [R0]           ; Verifica se a variavel que liga o objecto de cima está ativada
	CMP R1, 1
	JZ CIMA
    MOV R0,var_liga_baixo
	MOVB R1, [R0]           ; Verifica se a variavel que liga o objecto de baixo está ativada
	CMP R1, 1
	JZ BAIXO	
CIMA:
	MOV R0, var_cima   
	MOVB R1, [R0]
	CMP R1,0                ; Verifica se a variavel do objecto está ativa, se estiver vai verificar se é arma
	JNZ var_c_arma
BAIXO:
	MOV R0, var_baixo  
	MOVB R1, [R0]
	CMP R1,0                ; Verifica se a variavel do objecto está ativa, se estiver vai verificar se é arma
	JZ fim_ninja
var_b_arma:
    CMP R1,1                ; Se R1 = 1 entao é arma, que vai ser adicionada na parte de baixo, se R1 = 2, é prenda
	JZ arm_b
	JMP prenda_b
var_c_arma:
    CMP R1,1
	JZ baixo_linhac              ; Se R1 = 1 entao é arma, que vai ser adicionada na parte de cima, se R1 = 2, é prenda
	JMP cima_var_cc    
;;; Vai retirar a linha de cima , a coluna de cima, e a forma da prenda, para registos, vai tambem colocar num registo se é para apagar a prenda ou para colocar
cima_var_cc:
    MOV R2, lin_i_c         
	MOV R5, col_i_c
	MOV R6, Prenda
	MOV R0, var_apagar_cima
	MOVB R9, [R0]
	JMP Coord_n_a_p
;;; Vai retirar a linha de baixo , a coluna de baixo, e a forma da prenda, para registos, vai tambem colocar num registo se é para apagar a prenda ou para colocar
prenda_b:
    MOV R2, lin_i_b         
	MOV R5, col_i_b
	MOV R6, Prenda
	MOV R0, var_apagar_baixo
	MOVB R9, [R0]
	JMP Coord_n_a_p
;;; Vai retirar os endereços da linha de baixo, da coluna de baixo, e da forma da armaa, para registos, vai tambem colocar num registo se é para apagar a arma ou para a colocar no pixel screen
arm_b:
    MOV R2, lin_i_b
	MOV R5, col_i_b
	MOV R6, Arma
    MOV R0, var_apagar_baixo
	MOVB R9, [R0]
	JMP Coord_n_a_p
;;; Vai retirar os endereços da linha de cima, da coluna de cima, e da forma da arma, para registos, vai tambem colocar num registo se é para apagar a arma ou para a colocar no pixel screen
baixo_linhac:
    MOV R2, lin_i_c
	MOV R5, col_i_c
	MOV R6, Arma
    MOV R0, var_apagar_cima
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

;;;Rotina que pode descer a linha do ninja, , mover para a esquerda a coluna do objecto de cima ou do objecto de baixo e ativar o apagar de cada um deles
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
baixo_ap:	
	MOV R0, var_baixo      
	MOVB R1, [R0]         ; Vai verificar se o objecto de baixo está ativo
	CMP R1,0              ; baixo esta ativa? = 1
	JNZ baixo_pr
cima_ap:
    MOV R0, var_cima
	MOVB R1, [R0]         ; Vai verificar se o objecto de cima está ativo
	CMP R1, 0             ; objecto de baixo esta ativa? = 1
	JNZ cima_pr
	JMP fim_de_rot
baixo_pr:;Verifica se vai apagar ou andar para o lado
apagar_baixo:
	MOV R0, var_apagar_baixo       
	MOV R1,1               
	MOVB [R0],R1           ; Ativa apagar objecto de baixo
	MOV R0,var_liga_baixo
	MOV R1,1
	MOVB [R0],R1           ; Permite que apague no pixel screen
	CALL n_a_p_pixel       ; Chama a rotina para apagar o pixel
	MOV R0, var_apagar_baixo 
	MOV R1, 0
	MOVB [R0], R1
andar_baixo:
    MOV R0, col_i_b             
	MOVB R1, [R0]          ; R1 - Coluna do objecto de baixo
	MOV R2,0           
	CMP R1,R2              ; Se a coluna for igual a 0, significa que o objecto de baixo bateu na parede
	JZ desat_baixo             
	SUB R1,1               ; Anda uma coluna para a esquerda
	MOVB [R0], R1          
	Call n_a_p_pixel
	MOV R0,var_liga_baixo
	MOV R1, 0
	MOVB [R0],R1           ; Desativa a variavel que permite modificacoes do objecto de baixo no pixel screen
	JMP cima_ap
cima_pr:;Verifica se vai apagar ou andar para o lado o objecto de cima
apagar_cima:; Vai apagar o objecto de cima do pixel_screen
	MOV R0, var_apagar_cima    
	MOV R1,1               
	MOVB [R0],R1            ; Ativa apagar objecto de cima
	MOV R0,var_liga_cima
	MOV R1,1
	MOVB [R0],R1           ; ligado, permite que se apague o objecto de cima do pixel_screen
	Call n_a_p_pixel       ; Chama a rotina para apagar o pixel
	MOV R0, var_apagar_cima
	MOV R1, 0
    MOVB [R0], R1          ; Desativa apagar o objecto de cima
andar_cima:; O objecto de cima vai andar para a esquerda
    MOV R0, col_i_c
	MOVB R1, [R0]          ; Coluna do objecto de cima
	MOV R2,0
	CMP R1,R2              ; Verifica se o objecto de cima já bateu na parede
	JZ desat_cima
	SUB R1,1               ; Anda uma coluna para a esquerda
	MOVB [R0], R1          
	Call n_a_p_pixel       ; Colocar o objecto de cima no pixel screen
	MOV R0,var_liga_cima
	MOV R1,0
	MOVB [R0],R1           ; desativa a variavel que permite modificações do pixel screen
	JMP fim_de_rot
ninja_pr:; Indica se vai apagar o ninja ou descer
apagar_nin:; Vai apagar o ninja do pixel_screen
	MOV R0, var_apagar_nin   
	MOV R1,1
	MOVB [R0], R1          ;Ativa a variavel que apaga o ninja
	CALL n_a_p_pixel  
	MOV R0, var_apagar_nin
	MOV R1, 0
    MOVB [R0], R1          ; Desativa o apagar ninja
andar_ninja:; O ninja vai descer
	MOV R1, lin_i_n        
	MOVB R0, [R1]          ; Numero da linha atual
	MOV R2,28              
	CMP R0,R2              ; Se já chegou ao fim do pixel screen, morre
	JZ fim_de_rot
	ADD R0, 1              ; Linha seguinte
	MOVB [R1],R0           ; Coloca o valor da linha em memoria
	Call n_a_p_pixel       ; chama a rotina para descer o pixel
	JMP fim_de_rot
desat_baixo:; Vai desativar o objecto de baixo
    MOV R2,29
    MOVB [R0], R2          ; Reset da coluna do objecto de baixo
	MOV R0, alea_a
	MOV R1,0
	MOVB [R0],R1           ; Coloca a aleatoriedade da prenda a 0
	MOV R0, alea_p
	MOVB [R0],R1           ; Coloca a aleatoriedade da arma a 0
    MOV R0,lin_i_b
    MOVB R1, [R0]         
   	MOV R2,21
	MOVB [R0], R2          ; Reset da linha do objecto de baixo
	MOV R0, var_baixo
	MOV R1,0
    MOVB [R0],R1           ; Desativa o objecto de baixo
    JMP cima_ap
desat_cima:;Vai desativar o objecto de cima
    MOV R2,29   
    MOVB [R0], R2          ; Reset da coluna do objecto de cima
	MOV R0, alea_a
	MOV R1,0
	MOVB [R0],R1
	MOV R0, alea_p
	MOVB [R0],R1
	MOV R0, lin_i_c
	MOVB R1, [R0]
   	MOV R2,7 
	MOVB [R0], R2          ; Reset da linha do objecto de cima
	MOV R0, var_cima
	MOV R1,0
    MOVB [R0],R1           ; Desativa o objecto de cima
	JMP fim_de_rot
fim_de_rot:
	POP R6
	POP R2
	POP R1
	POP R0
	RET

;;;Vai usar o contador aleatorio para ligar ou uma prenda ou uma arma	
arma_prenda:
    PUSH R0
	PUSH R1
	PUSH R2
	MOV R0, var_contador  
    MOVB R1,[R0]           ; Acesso ao contador pela memoria
	MOV R2,3               
	AND R1, R2             ; le os dois ultimos bits
	MOV R2, R1             ; Se esse valor for 0 = prenda, se nao for dá arma (75% Arma, 25%Prenda)
	CMP R2, 0            
	JZ  prend_var 
arma_var:;Escolheu arma	
    MOV R0, alea_a
	MOV R1,1               
	MOVB [R0], R1          ; Liga a variavel que indica que escolheu arma
	JMP fim_armpr
prend_var:;Escolheu prenda
	MOV R0, alea_p       
	MOV R1,1
	MOVB [R0], R1          ; Liga a variavel que indica que escolheu prenda
fim_armpr:
    POP R2
	POP R1
	POP R0
	RET

;;; Verifica se ja existe algum objecto de cima ou algum objecto de baixo em jogo e se não existir onde as colocar	
onde_ar_pr:
    PUSH R0
	PUSH R1
	PUSH R2
	PUSH R3
	PUSH R4
baixo_var_c:; verifica se existe um objecto de baixo em jogo
    MOV R3, var_baixo
	MOVB R1, [R3]      
	MOV R2, 0 
	CMP R1, R2              ; Se R1 = 1, entao ja existe uma arma ativa no jogo, se R1=2 então ja existe uma prenda ativa,se R1 = 0 nao existe vai verificar se foi escolhido uma arma para a ativar
	JZ col_ar
cima_var_c:; verifica se existe um objecto de cima em jogo	
	MOV R4,1
	MOV R3, var_cima
	MOVB R1, [R3]          
	MOV R2, 0 
	CMP R1, R2              ; Se R1 = 1, entao ja existe uma arma ativa no jogo, se R1=2 então ja existe uma prenda ativa,se R1 = 0 nao existe vai verificar se foi escolhido uma arma para a ativar
	JZ col_ar
	JMP fin_call            ; fim da rotina
col_ar:;verifica se foi escolhido uma arma na aleatoriedade
    MOV R0, alea_a
    MOVB R1,[R0]            ; verifica se na rotina anterior foi escolhido uma arma
 	MOV R2, 1
	CMP R1,R2
	JZ incr_valor           ; se foi escolhida uma arma entao vai se ativar a arma, se nao vai verificar a prenda
col_bar:;verifica se foi escolhido uma prenda na aleatoriedade
    MOV R0, alea_p
    MOVB R1,[R0]            ; verifica se na rotina anterior foi escolhida uma prenda
    MOV R2, 1
	CMP R1,R2               ; se foi escolhida uma prenda entao vai se ativar a prenda, se nao vai sair da rotina
	JNZ fin_call
inc_var:;vai ativar a prenda
	MOV R0, var_contador
	MOVB R1, [R0]
	MOV R2,1
	AND R1,R2
	CMP R1,R4
	JZ incr_valor
	MOV R1,2
	MOVB [R3],R1
	CMP R4,1
	JZ liga_cima
liga_baixo:;Coloca em baixo
	MOV R0, var_liga_baixo
	MOV R1,1
	MOVB [R0],R1
	JMP rotinas
liga_cima:;Coloca em cima
	MOV R0, var_liga_cima
	MOV R1,1
	MOVB [R0],R1
	JMP rotinas
incr_valor:; vai ativar a arma
	MOV R1,1
	MOVB [R3],R1
    CMP R4,1
	JZ liga_cima
	JMP liga_baixo
rotinas:;Colocar prenda ou arma em jogo ou em cima ou em baixo
    CALL onde_obj           ; indica-nos onde colocar os objectos de cima ou de baixo
	CALL n_a_p_pixel        ; apaga e desce o pixel
	MOV R0, var_liga_baixo  ; desativa o apaga e desce o pixel em baixo
	MOV R1,0
	MOVB [R0],R1
	MOV R0, var_liga_cima   ; desativa o apaga e desce o pixel em cima
	MOV R1,0
	MOVB [R0],R1
	JMP cima_var_c
fin_call:
	POP R4
	POP R3
	POP R2
	POP R1
	POP R0
    RET

;;;Onde colocar o objecto de cima ou onde colocar o objecto de baixo
onde_obj:
    PUSH R0
	PUSH R1
	PUSH R2
	PUSH R3
	PUSH R4
	MOV R0, var_contador   ; endereco do contador 
    MOVB R1,[R0]           ; R1 - valor do contador
	MOV R2,7               ; Primeiros 3 bits
	AND R1,R2              ; Retira um valor de 0 a 7
	MOV R2, R1             ; Valor a adicionar ou a subtrair da linha
	Bit R1,1               ; Se o bit 1 for 0 jumpa para next
	JZ next
    MOV R4,1               ; R4 - indica que vai diminuir 
next:; funcao que vai verificar se e a o objecto de cima ou o objecto de baixo que sao geradas
	MOV R0, var_cima       ; Verifica se o objecto de cima vai ser adicionada
	MOVB R1, [R0]
	MOV R3,0
	CMP R1,R3
	JNZ  cima_linha
    MOV R0, var_baixo      ; Verifica se o objecto de baixo 
	MOVB R1, [R0]
	MOV R3,0
	CMP R1,R3
	JZ final_col
baixo_linha:; adiciona o endereco da linha de baixo
	MOV R0, lin_i_b
	MOVB R1, [R0]
	JMP addi
cima_linha:; adiciona o endereco da linha da prenda
    MOV R0, lin_i_c
	MOVB R1, [R0]
addi:;Adiciona ao 7 ou ao 21 que esta na linha
    CMP R4,1
	JZ nega                 ; Decrementa
	ADD R1,R2               ; Encrementa
	MOVB [R0], R1
	JMP final_col
nega:; Subtrai ao 7 ou ao 21 que esta na linha
    SUB R1,R2               ; Decrementa
	MOVB [R0], R1
final_col: 	
    POP R4
	POP R3
	POP R2
	POP R1
	POP R0
	RET
	
colisoes:;Verifica se colidiram
   PUSH R0
   PUSH R1
   PUSH R2
   PUSH R3
   PUSH R4
   PUSH R5
   PUSH R6
   PUSH R7
   PUSH R8
   MOV R0, lin_i_n
   MOVB R1, [R0]
   MOV R0, lin_i_b
   MOVB R2, [R0]
   MOV R0, lin_i_c
   MOVB R3, [R0]
   MOV R0, col_i_n
   MOVB R5, [R0]
   MOV R0, col_i_b
   MOVB R6, [R0]
   MOV R0, col_i_c
   MOVB R7, [R0]  
   MOV R4, 14
   CMP R1,R4
   JGE cima_col
baixo_col:
   MOV R0,3
   ADD R5,R0
   CMP R6,R5
   JLE colisao_baixo_linha
   JMP final_colis 
colisao_baixo_linha:
   CMP R2,R1
   JGE linha_baixo_b   
   JMP final_colis
linha_baixo_b:
   ADD R1,R0
   CMP R2,R1
   JLE colisao_baixo
   JMP final_colis
colisao_baixo:
   MOV R0, var_baixo
   MOVB R7, [R0]
   CMP R7,2
   JZ  add_display
   CMP R7,1
   JZ  morrer
   JMP final_colis 
cima_col:
   MOV R0,3
   ADD R5,R0
   CMP R6,R5
   JLE colisao_cima_linha
   JMP final_colis  
colisao_cima_linha:
   CMP R2,R1
   JGE linha_baixo_c
   JMP final_colis
linha_baixo_c:
   ADD R1,R0
   CMP R2,R1
   JLE colisao_cima
   JMP final_colis
colisao_cima:
   MOV R0, var_cima
   MOVB R7, [R0]
   CMP R7,2
   JZ  add_display
   CMP R7,1
   JZ  morrer
   JMP final_colis
add_display:
   JMP final_colis
morrer:
   MOV R0,fim_do_jogo
   MOV R8,1
   MOVB [R0], R8
   JMP final_colis
final_colis:
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
   
fimjogo:;Vai ter um limpa tudo e dps aquele display final
  PUSH R0
  PUSH R1
  MOV R0, fim_do_jogo
  MOVB R1, [R0]
  CMP R1,1
  JNZ volta_ao_jogo
ciclo_fim:  
  JMP ciclo_fim
volta_ao_jogo:
  POP R1
  POP R0
  RET
  
	
	






	




