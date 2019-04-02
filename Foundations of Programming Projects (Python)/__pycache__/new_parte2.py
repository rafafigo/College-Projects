""" Rafael Alexandre Roberto Figueiredo - 90770 - Taguspark """
from parte1 import e_palavra
from itertools import permutations

""" TAD palavra_potencial """

def cria_palavra_potencial(cad_carac,tup_let):
    """ Verifica se a palavra_potencial e o conjunto de letras submetido esta 
    de acordo com os parametros para ser uma palavra, caso esteja que torna a
    palavra potencial como string """
    if not isinstance(tup_let,tuple) : # O conjunto de letras tem de ser tuple
        raise ValueError('cria_palavra_potencial:argumentos invalidos.')
    for i in tup_let:
        if  not isinstance(i,str): # Cada um dos elementos do tuple tem de ser string
            raise ValueError('cria_palavra_potencial:argumentos invalidos.')
        for c in i:
            if not 65<=ord(c)<=90: # Cada letra tem de ser maiuscula
                raise ValueError('cria_palavra_potencial:argumentos invalidos.')        
    if not isinstance(cad_carac,str): # A potencial palavra tem de ser string
        raise ValueError('cria_palavra_potencial:argumentos invalidos.')
    for d in cad_carac:
        if not 65<=ord(d)<=90: # Cada letra tem de ser maiuscula
            raise ValueError('cria_palavra_potencial:argumentos invalidos.')    
    tup_let = list(tup_let)
    for l in cad_carac: 
        if l not in tup_let: # Cada letra da potencial palavra tem de estar dentro do tuple
                raise ValueError('cria_palavra_potencial:a palavra nao e valida.')
        else:
            for m in range(len(tup_let)): # Corre o numero da letra a ser retirada
                if tup_let[m] == l:
                    tup_let = tup_let[:m] + tup_let[m+1:] # Se uma letra pertencer ao tuple entao essa letra e retirada
                    break               
    return str(cad_carac) #Retorna String


def palavra_tamanho(pala_pot):
    return len(pala_pot) # Indica o tamanho da palavra potencial


def e_palavra_potencial(univer):
    if not isinstance(univer,str): #Verifica se e string
        return False
    for i in univer:
        if not 65<=ord(i)<=90: #Verifica se  cada letra e maiuscula
            return False
    return True  #Caso cumpra os requisitos para ser palavra_potencial retorna true  


def palavras_potenciais_iguais(pal_pot1,pal_pot2):
    if pal_pot1 == pal_pot2: #Verifica se as duas palavras potenciais sao iguais
        return True
    else:
        return False


def palavra_potencial_menor(pal_pot1,pal_pot2):
    if pal_pot1 < pal_pot2: #Verifica se a primeira palavra_potencial e menor que a segunda
        return True
    else:
        return False


def palavra_potencial_para_cadeia(pala_pot):
    return pala_pot # Retorna a propria string pois nesta TAD é utilizado a string como abstracao 


""" TAD Conjunto_palavras """


def cria_conjunto_palavras():
    return [] # Neste TAD o conjunto de palavras vai estar numa lista


def numero_palavras(conjunto_palavras):
    return len(conjunto_palavras) # Como ja diz no nome indica-nos o numero de palavras potenciais que pussuem


def subconjunto_por_tamanho(conjunto_palavras,num):
    """ Vai nos indicar todas as palavras do conjunto de palavras que possuem um determinado conjunto de letras"""
    lst = []
    for i in conjunto_palavras:
        tamanho = palavra_tamanho(i)  #Numero de letras numa palavra potencial
        if tamanho == num:
            lst = lst + [i]  # Caso o numero de letras seja igual ao numero escolido e adicionado a uma nova lista so com palavras com esse numero de letras
    return lst


def acrescenta_palavra(conj_pala,pala_pot):
    """ Acrescenta ao conjunto de palavras uma palavra potencial caso esta seja de facto uma palavra potencial e esta nao esta ja contida no conjunto """
    if not e_conjunto_palavras(conj_pala):
        raise ValueError('acrescenta_palavra:argumentos invalidos.')
    if not e_palavra_potencial(pala_pot): #Verifica se a palavra potencial a tentar ser adicionada e uma palavra potencial
        raise ValueError('acrescenta_palavra:argumentos invalidos.')
    if pala_pot not in conj_pala:
        conj_pala.append(pala_pot)   #Se satisfizer todas as condicoes e nao estar ja no conjunto de palavras e adicionada


def e_conjunto_palavras(pot_conj):
    """ Verifica se e um conjunto de palavras potencial"""
    if not isinstance(pot_conj,list):
        return False
    else:
        for i in pot_conj:
            if e_palavra_potencial(i)==False:
                return False
        else:
            return True # Se for lista e todas as suas palavras  constituirem palavras potenciais entao e uma palavra potencial 


def conjuntos_palavras_iguais(conj_pal1,conj_pal2):
    """ Como os dois conjunto de palavras sao listas com strings, entao e possivel organizar por ordem, caso depois de organizadas sejam iguais entao é true """
    conj_pal1.sort() 
    conj_pal2.sort()
    if conj_pal1 == conj_pal2:
        return True
    else:
        return False


def conjunto_palavras_para_cadeia(conj_pal):
    """ Cria o conjunto de palavras primeiramente a partir de um dicionario que e separado numa lista com tuples, que prosteriormente se transforma numa so lista que conforme a sua posicao forma a apresentacao pedida no projecto"""
    dic = {} #Dicionario
    newlist = [] #
    string = ''
    lista = []
    valor1 = 0
    conj_pal.sort()
    for i in conj_pal:
        tamanho = palavra_tamanho(i)      # Numero de palavras no conjunto de palavras
        if tamanho in dic:
            dic[tamanho] = dic[tamanho] + [i]   #Se na key Tamanho ja estiver algum valor é adicionado mais esta palavra que tem necessariamente o mesmo numero de silabas
        else:
            dic[tamanho] = [i]  # Se ainda nao existia uma key com esse tamanho e criado uma pela primeira vez
    dic = dic.items() # Disfaz o dicionario 

    for i in dic:
            newlist = newlist + [i] # lista com tuples com a juncao da key tamanho com o conjunto de palavras
    newlist.sort() 
    for i in newlist:
        for l in i:
            lista = lista + [l] # Desfaz-se do tuple e tem apenas os argumentos
    for i in lista:
        if isinstance(i,int):    #Se i for int significa que esta na parte do numero
            i = str(i)
            if string == '':
                string = string + i + '->' #Se for a primeira vez, adiciona se o numero e depois a seta
            else:
                string = string + ';' + i + '->' # Todas as outras vezes tem-se sempre ; pois vem antes um subconjunto
        else:
            if isinstance(i,list):
                for d in i:   # palavras potenciais dentro das listas
                    valor2 = i[-1] # Indica a ultima palavra potencial
                    if valor1 == 0 and valor2 == d:
                        string = string + '[' + d + ']'  # Primeiro e unico elemento da lista                       
                    elif valor1 == 0:
                        string = string + '[' + d + ', ' # Primeiro elemento da lista
                    elif valor2 == d:
                        string = string + d + ']' # ultimo elemento da lista
                    else:
                        string = string + d + ', '  # elementos intermedios da lista
                    valor1 = valor1 + 1
                valor1 = 0 # reset do valor1
    string = '[' + string + ']' #fechar por ultimo a string 
    return string


""" TAD Jogador"""

def cria_jogador(cad_carac):
    """ Criacao do jogador numa lista com 4 variaveis, o nome submetido, a pontuacao e uma lista vazia de palavras validas e uma lita vazia de palavras invalidas """
    if not isinstance(cad_carac,str): #Verifica se o nome e uma string
        raise ValueError('cria_jogador:argumento invalido.')
    else:
        jogador = [cad_carac,0,[],[]]
        return jogador


def jogador_nome(jogador):
    cad_carac = jogador[0] #Nome do jogador
    return cad_carac


def jogador_pontuacao(jogador):
    cad_carac = jogador[1] #Pontuacao do jogador
    return cad_carac


def jogador_palavras_validas(jogador):
    cad_carac = jogador[2] #Conjunto de palavras validas
    return cad_carac


def jogador_palavras_invalidas(jogador):
    cad_carac = jogador[3] #Conjunto de palavras invalidas
    return cad_carac


def adiciona_palavra_valida(jogador,palavra_potencial): 
    """ Vai caso sejam submetidos argumentos validos, verificado se a palavra valida colocada ja existe no conjunto e se nao existir muda a pontuacao, caso ja existe nada faz"""
    if e_palavra_potencial(palavra_potencial) or e_jogador(jogador): #Verifica que o jogador e mesmo jogador conforme as regras e se a palavra e uma palavra potencial
        raise ValueError('adiciona_palavra_valida:argumentos invalidos.') 
    else:
        conjunto = list(jogador_palavras_validas(jogador)) #Lista antes de se adicionar a nova palavra
        acrescenta_palavra(jogador_palavras_validas(jogador),palavra_potencial) # Se ainda nao estiver na lista de palavras validas a lista vai ficar diferente do conjunto criado
        if conjunto != jogador[2]: # Assim adiciona a pontuacao, caso seja uma nova palavra valida
            tamanho = palavra_tamanho(palavra_potencial)
            jogador_pontuacao(jogador) += tamanho
               

def adiciona_palavra_invalida(jogador,palavra_potencial):
    """ Vai caso sejam submetidos argumentos validos, verificado se a palavra invalida colocada ja existe no conjunto e se nao existir muda a pontuacao, caso ja existe nada faz"""
    if e_jogador(jogador) or e_palavra_potencial(palavra_potencial): # Verifica que o jogador e um jogador e a palavra e uma palavra potencial
        raise ValueError('adiciona_palavra_invalida:argumentos invalidos.')
    else:
        conjunto = list(jogador_palavras_invalidas(jogador)) # Lista de palavras invalidas antes de adicionar nova
        acrescenta_palavra(jogador_palavras_invalidas(jogador),palavra_potencial) # Acrescenta palavra se a palavra potencial nao estiver ja na lista de palavras invalidas
        if conjunto != jogador_palavras_invalidas(jogador): # Significa que tem nova palavra
            tamanho = -palavra_tamanho(palavra_potencial)
            jogador[1] += tamanho # Perde pontuacao


def e_jogador(valor_un):
    """ Vai verificar se todos os elementos do jogador criados estao presentes no jogador """
    if isinstance(valor_un,list) and len(valor_un) == 4 and isinstance(jogador_nome(jogador),str) and isinstance(jogador_pontuacao(jogador),int) and e_conjunto_palavras(jogador_palavras_validas(jogador)) and e_conjunto_palavras(jogador_palavras_invalidas(jogador)): # Testa todas as possibilidades para cada uma das variaveis e para as variaveis num todo
        return True
    else:
        return False


def jogador_para_cadeia(jogador):
    """ Cria o jogador de acordo como e pedido no projecto, e feito numa string com tudo escrito"""
    string = 'JOGADOR ' + str(jogador_nome(jogador)) + ' PONTOS=' + str(jogador_pontuacao(jogador)) + ' VALIDAS=' + str(conjunto_palavras_para_cadeia(jogador_palavras_validas(jogador))) + ' INVALIDAS=' + str(conjunto_palavras_para_cadeia(jogador_palavras_invalidas(jogador))) 
    return string


""" Funcoes Adicionais """

def gera_todas_palavras_validas(tupl_let):
    """ Primeiramente gera todas as possiveis palavras, depois fica com todas aquelas que sao validas conforme a gramatica do primeiro projecto e e criado a palavra potencial """
    lst = []
    nova_lst = []
    n = len(tupl_let) # Numero de letras
    for i in range(1, n+1):
        for l in permutations(tupl_let,i):
            lst = lst + [l] # Adiciona todas as possiveis palavras, todas em separado
    for p in lst:
        p = ''.join(p) # Junta as palavras
        if e_palavra(p)== True:
            if p not in nova_lst: # Se ainda nao pertencer ha lista adiciona a lista a nova palavra valida
                nova_lst = nova_lst + [p]
    lst_final = []
    for i in nova_lst:
        lst_final.append(cria_palavra_potencial(i, tupl_let)) # Faz uma lista com cada uma das palavras potenciais
    return lst_final


""" Guru_mj """
def nome_dos_jogadores(letras):
    """ Obtem o nome dos jogadores """
    print('Descubra todas as palavras geradas a partir das letras:')
    print(letras)
    print('Introduza o nome dos jogadores (-1 para terminar)...')
    n = 1
    jogadores = []
    jogador = ''
    while jogador !=  str(-1): # Se for dado -1 termina
        escrita = 'JOGADOR ' +  str(n) + ' -> '
        jogador = input(escrita)       # Nome
        n = n + 1 # Numero do jogador 
        if jogador != str(-1):
            jogadores = jogadores + [cria_jogador(jogador)] #Criacao de uma lista com todos os jogadores
    return jogadores

def jogada_dos_jogadores(letras,jogadores):
    """ Parte Principal onde o jogador adiciona palavras e vai verificado se sao validas ou invalidas """
    tod_pala = gera_todas_palavras_validas(letras) # Todas as palavras validas
    l = 0 # inicializacao
    aux = list(tod_pala) #Todas as palavras validas imutaveis
    num_jogadas = 1  # O numero das jogadas  
    while l < len(tod_pala): 
        for i in jogadores: # i = cada um dos jogadores
            total_palavras = len(aux) #Numero de palavras
            if total_palavras == 0:
                l = len(tod_pala) # Fim do jogo
                break
            print ('JOGADA ' + str(num_jogadas) + ' - Falta descobrir ' + str(total_palavras) + ' palavras')
            var_inp = 'JOGADOR ' + str(jogador_nome(i)) + ' -> '
            palavra = input(var_inp) # Palavra colocada pelo utilizador
            pala_potencial = cria_palavra_potencial(palavra,letras)
            if pala_potencial in tod_pala:
                print(str(pala_potencial) + ' - ' 'palavra VALIDA')
                if pala_potencial in aux: # Verifica se a palavra potencial esta ainda em jogo ou ja foi dita
                    adiciona_palavra_valida(i,pala_potencial)
                    aux.remove(pala_potencial) # retira a palavra para poder verificar se ja foi escrita
            else:
                print(str(pala_potencial) + ' - ' 'palavra INVALIDA') 
                adiciona_palavra_invalida(i,pala_potencial) # Adiciona palavra invalida
            num_jogadas = num_jogadas + 1 #proxima jogada
    return jogadores
def fim_do_jogo(letras,jogadores):
    """ Indica os resultados """
    pontuacao = []          
    for i in jogadores:
        pontuacao = pontuacao + [jogador_pontuacao(i)] # Indica as pontuacoes
    conjunto = set(pontuacao) # Retira as palavras repetidas
    if len(conjunto) != len(pontuacao): # Se nao forem iguais - impate
        print('FIM DE JOGO! O jogo terminou em empate.')
    else: 
        pontuacao.sort() # O primeiro e o que tem menos pontuacao logo o ultimo e o vencedor
        vencedor = pontuacao[-1]
        for i in jogadores:
            if vencedor == jogador_pontuacao(i):
                print('FIM DE JOGO! O jogo terminou com a vitoria do jogador ' + str(jogador_nome(i)) +' com ' + str(jogador_pontuacao(i)) + ' pontos.')
    for i in jogadores:
        print(jogador_para_cadeia(i))  #Resultados
        
def guru_mj(letras):
    """ Funcao Principal do programa """
    jogadores = nome_dos_jogadores(letras)
    jogadores =jogadas_dos_jogadores(letras,jogadores)
    fim_do_jogo(letras,jogadores)
