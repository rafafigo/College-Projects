"""Funcoes Reultado"""
def e_silaba(sila):
    """Verifica se o que foi inserido e uma string, e verifica se essa string e uma silaba"""
    if not isinstance(sila, str):
        raise ValueError('e_silaba:argumento invalido')#Se nao for string, entao erro
    return sila in sil() #Verifica se e uma silaba
def e_monossilabo(monoss):
    """Verifica se o que foi inserido e uma string e verifica tambem se essa string e um monossilabo """
    if not isinstance(monoss, str):
        raise ValueError('e_monossilabo:argumento invalido')#Se nao for string, entao erro
    return monoss in mono() #Verifica se e um monossilabo
def e_palavra(pala):
    """Primeiro, verifica se e uma string, se for vai verificar se a string e uma palavra segundo a gramatica """
    if not isinstance(pala, str):
        raise ValueError('e_palavra:argumento invalido')#Erro caso nao for string
    if pala in mono(): #Se for monossilabo ja e uma palavra
        val_log = True
        return val_log
    if pala in sil_final(): #Se for silaba final entao ja e uma palavra
        val_log = True
        return val_log
    return final_sil(pala) #Vai verificar se e uma palavra

"""Onde esta armazenada toda a gramatica"""
def art_def():
    """Artigo definido que e junto num tuple"""
    artigo_def = ('A','O') #Junta A, O num tuple 
    return artigo_def
def vog_pal():
    """Junta ao tuple o tuple com a nova vogal que da a vogal_palavra """
    vogal_palavra = art_def() + ('E',) # Junta os dois Tupples
    return vogal_palavra
def vog():
    """Faz juncao de 2 tuples, num so, dando a vogal"""
    vogal = ('I','U',) + vog_pal()
    return vogal
def dit_pal():
    """Indica nos o ditongo_palavra do dicionario, transformando- o num tuple"""
    ditongo_palavra = ('AI','AO','EU','OU',)
    return ditongo_palavra
def dit():
    """Junta dois tuples, dando um ditongo"""
    ditongo = ('AE','AU','EI','OE','OI','IU',) + dit_pal()
    return ditongo
def pa_vog():
    """Junta dois tuples, formando par_vogais"""
    par_vogais = dit() + ('IA','IO',)
    return par_vogais
def cons_freq():    
    """Junta a consoante_freq num tuple"""
    consoante_freq = ('D','L','M','N','P','R','S','T','V',)
    return consoante_freq
def cons_term():
    
    consoante_terminal = ('L','M','R','S','X','Z',)
    return consoante_terminal
def cons_final():
    """Juncao de um tuple com outro, formando consoante_final"""
    consoante_final = ('N','P',) + cons_term()
    return consoante_final
def cons():
    """Junta todas as consoantes num tuple"""
    consoante = ('B','C','D','F','G','H','J','L','M','N','P','Q','R','S','T','V','X','Z',)
    return consoante
def pa_cons():
    """junta todos os par_consoantes num tuple"""
    par_consoantes = ('BR','CR','FR','GR','PR','TR','VR','BL','CL','FL','GL','PL',)
    return par_consoantes
def mono_2():
    """Junta varios tuples num so,sempre que se usa a funcao juntar 2 juntao aos elementos do primeiro os do segundo"""
    monossilabo_2 = ('AR','IR','EM','UM') + juntar2(vog_pal(),('S',)) + dit_pal() + juntar2(cons_freq(),vog()) # Faz uso da funcao para atribuir a casda vogal_palavra um S
    return monossilabo_2
def mono_3():
    """Junta varios tuples num so, existe juntar2 e existe juntar3 que funciona da mesma maneira mas o juntar3 adiciona primeiro todos os elementos do segundo a cada elemento do primeiro e depois junta os elementos do terceiro a todas as hipoteses dos primeiros dois"""
    monossilabo_3 = juntar3(cons(),vog(),cons_term()) + juntar2(cons(),dit()) + juntar2(pa_vog(),cons_term())
    return monossilabo_3
def mono():
    """Junta 3 tuples num so que e o monossilabo"""
    monossilabo = vog_pal() + mono_2() + mono_3()
    return monossilabo
def sil_2():
    """junta varios tuples num so formando a silaba_2"""
    silaba_2 = pa_vog() + juntar2(cons(),vog()) + juntar2(vog(),cons_final())
    return silaba_2
def sil_3():
    """Junta varios tuples num so formando a silaba_3"""
    silaba_3 = ('QUA','QUE','QUI','GUE','GUI') + juntar2(vog(),('NS',)) + juntar2(cons(),pa_vog()) + juntar3(cons(),vog(),cons_final()) + juntar2(pa_vog(),cons_final()) + juntar2(pa_cons(),vog())
    return silaba_3
def sil_4():
    """Junta os varios tuples num so, dando a silaba_4"""
    silaba_4 = juntar2(pa_vog(),('NS',)) + juntar3(cons(),vog(),('NS',)) + juntar3(cons(),vog(),('IS',)) + juntar2(pa_cons(),pa_vog()) + juntar3(cons(),pa_vog(),cons_final())
    return silaba_4
def sil_5():
    """Adiciona a todos os elementos do par_consoante uma vogal e depois de seguida a todas as hipoteses desses adiciona a string,dando a silaba_5"""
    silaba_5 = juntar3(pa_cons(),vog(),('NS',))
    return silaba_5
def sil_final():
    """Junta 4 tuples formando a silaba_final"""
    silaba_final = mono_2() + mono_3() + sil_4() + sil_5()
    return silaba_final
def sil():
    """Juncao de 5 tuples, dando a silaba"""
    silaba = vog() + sil_2() + sil_3() + sil_4() + sil_5()
    return silaba


"""Funcoes internas da gramatica"""
def juntar2(x_tup1,y_tup2):
    """Funcao que junta a cada elemento de um tuple uma certa string"""    
    tup1_ele = 0  
    tup2_ele = 0
    while tup2_ele<len(y_tup2): # Vai passando por cada um dos seus elementos e juntar ao x_tup1
        while tup1_ele<len(x_tup1): #Passa por todos os seus elementos 
            juntar_12= x_tup1[tup1_ele] + y_tup2[tup2_ele] #Junta a cada elemento de x_tup1 um y_tup2
            if (tup1_ele == 0) and (tup2_ele == 0):
                tup_12 = (juntar_12,) #Transforma num tuple
            else:
                tup_12 = tup_12 + (juntar_12,) #Junta tuple
            tup1_ele = tup1_ele + 1 #Seguinte elemento
        tup2_ele = tup2_ele + 1 #Seguinte elemento
        tup1_ele = 0 #Volta a passar por todos os elementos de x_tup1
    return tup_12

def juntar3(x_tup1,y_tup2,z_tup3):
    """Funcao que junta a cada elemento de um tuple uma certa string e junta de seguida outra"""
    tup1_ele = 0
    tup2_ele = 0
    tup3_ele = 0
    while tup3_ele < len(z_tup3): #Vai passando por cada um dos seus elementos e juntar aos 2 outros
        while tup2_ele<len(y_tup2): # Vai passando por cada um dos seus elementos e juntar ao tup1
            while tup1_ele<len(x_tup1):
                juntar_123 = x_tup1[tup1_ele] + y_tup2[tup2_ele] + z_tup3[tup3_ele]  #Junta os elementos 
                if (tup1_ele == 0) and (tup2_ele == 0) and (tup3_ele == 0):
                    tup_123 = (juntar_123,) #Transforma a juncao num tuple
                else:
                    tup_123 = tup_123 + (juntar_123,) # junta os tuples
                tup1_ele = tup1_ele + 1 # passa para o segundo elemento e volta a receber 
            tup2_ele = tup2_ele + 1  # Passa para o elemento seguinte
            tup1_ele = 0 # O seguinte elemento passa novamente por todos os elementos de x_tup1
        tup3_ele = tup3_ele + 1 #Passa para o elemento seguinte
        tup2_ele = 0 # O seguinte elemento volta a passar por todos os elementos do x_tup2
        tup1_ele = 0 # Volta a passar por todos os elementos do x_tup1
    return tup_123

"""Funcoes internas ha verificao de uma palavra """
def final_sil(var_sil_val):
    """Funcao que cria uma lista com todos os possiveis restos da string sem a silaba final"""
    lst = []
    for i in range(0,len(var_sil_val)):
        if (var_sil_val[i:]) in sil_final():
            lst = lst + [var_sil_val[:i]]
    return sils(lst)

def sils(lst):
    """Funcao que vai testar cada elemento da lista de string sem silaba final, e tambem vai testar a lista de cada possivel palavra, faz isto ate conseguir encontrar um conjunto que seja verdadeiro ou seja que ja nao possua elementos pois ja foram retirados todos, se ja passou por toda a lista que vem da final_sil e não encontrou nenhum que funcione então e falso e a palavra nao pertence ha gramatica """
    if lst == []:
        return False
    else:
        val1 = tent(lst[0])
        if val1 == True:
            return True
        else:
            return sils(lst[1:])
def tent(silei):
    """Funcao que vai criando em listas todas as silabas,sendo que cada lista tem todas as possiveis primeiras silabas depois se ainda tiver caracteres cria outra lista com todas as possiveis segundas silabas ate ja nao ter silabas que pertencao ha gramatica ou ja tenha encontrado todas as silabas na palavra """
    lst = []
    if len(silei) == 0:
        return True
    for i in range(0,len(silei)):
        if silei[i:] in sil(): 
            lst = lst + [silei[:i]]
    return(sils(lst))
        
        
    