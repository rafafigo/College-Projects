"""Funcoes Reultado"""
def e_silaba(sil):
    if not isinstance(sil, str):
        raise ValueError('e_silaba:argumento invalido')
    return sil in gram(0) #Verifica se a silaba pertence
def e_monossilabo(monoss):
    if not isinstance(monoss, str):
        raise ValueError('e_monossilabo:argumento invalido')
    return monoss in gram(1) #Verifica se o monossilabo pertence
def e_palavra(pala):
    if not isinstance(pala, str):
        raise ValueError('e_palavra:argumento invalido')
    if pala in gram(1): #monossilabo
        val_log = True
        return val_log
    if pala in gram(2): #So silaba final
        val_log = True
        return val_log
    sil_fin = tesil(pala)
    if sil_fin == False:
        return sil_fin
    else:
        sil_fin = final_sil(sil_fin)
        return sil_fin    

"""Funcao Gramatica"""
"""Onde esta armazenada toda a gramatica"""
def gram(x):
    artigo_def = ('A','O') #Junta A, O num tuple 
    vogal_palavra = artigo_def + ('E',) # Junta os dois Tupples
    vogal = ('I','U',) + vogal_palavra
    ditongo_palavra = ('AI','AO','EU','OU',)
    ditongo = ('AE','AU','EI','OE','OI','IU',) + ditongo_palavra
    par_vogais = ditongo + ('IA','IO',)
    consoante_freq = ('D','L','M','N','P','R','S','T','V',)
    consoante_terminal = ('L','M','R','S','X','Z',)
    consoante_final = ('N','P',) + consoante_terminal
    consoante = ('B','C','D','F','G','H','J','L','M','N','P','Q','R','S','T','V','X','Z',)
    par_consoantes = ('BR','CR','FR','GR','PR','TR','VR','BL','CL','FL','GL','PL',)
    monossilabo_2 = ('AR','IR','EM','UM') + juntar2(vogal_palavra,('S',)) + ditongo_palavra + juntar2(consoante_freq,vogal) # Faz uso da funcao para atribuir a casda vogal_palavra um S
    monossilabo_3 = juntar3(consoante,vogal,consoante_terminal) + juntar2(consoante,ditongo) + juntar2(par_vogais,consoante_terminal)
    monossilabo = vogal_palavra + monossilabo_2 + monossilabo_3
    silaba_2 = par_vogais + juntar2(consoante,vogal) + juntar2(vogal,consoante_final)
    silaba_3 = ('QUA','QUE','QUI','GUE','GUI') + juntar2(vogal,('NS',)) + juntar2(consoante,par_vogais) + juntar3(consoante,vogal,consoante_final) + juntar2(par_vogais,consoante_final) + juntar2(par_consoantes,vogal)
    silaba_4 = juntar2(par_vogais,('NS',)) + juntar3(consoante,vogal,('NS',)) + juntar2(par_consoantes,par_vogais) + juntar3(consoante,par_vogais,consoante_final) #Usa uma nova funcao para atribuir todas as possiveis hipoteses de formar uma silaba_3 , sendo que cada elemento do primeiro e atribuido cada elemento do segundo e cada elemento do terceiro 
    silaba_5 = juntar3(par_consoantes,vogal,('NS',))
    silaba_final = monossilabo_2 + monossilabo_3 + silaba_4 + silaba_5
    silaba = vogal + silaba_2 + silaba_3 + silaba_4 + silaba_5
    if x == 0: #Se o x for 0 entao e silaba
        return silaba
    if x== 1: #Se o x for 1 verifica se e monossilabo
        return monossilabo
    if x == 2:
        return silaba_final #Se o x for 2 verigica se e silaba final

"""Funcoes internas da gramatica"""

"""Funcao que junta a cada elemento de um tuple uma certa string"""
def juntar2(x_tup1,y_tup2): 
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

"""Funcao que junta a cada elemento de um tuple uma certa string e junta de seguida outra"""
def juntar3(x_tup1,y_tup2,z_tup3):
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

"""Esta funcao ira testar se a silaba_final aparece e caso a encontre ira retir essa silaba para prosteriormente encontrar as palavras"""
def tesil(var_sil_val):
    contador = 0 #Os 5 caracteres possiveis
    valor1 = len(var_sil_val)
    while contador < valor1:
        if (var_sil_val[contador:] in gram(2)):
            novo_contador = contador #Cria -se um novo contador para adquirir quando foi que formou uma silaba_final
            val_log = True
            contador = valor1 + 1 # Nao repete mais
            new_word = var_sil_val[:novo_contador] #retira-se a silaba_final do resto
        else:
            contador = contador + 1 # Se nao verificar nao e palavra
            val_log = False
    if val_log == True:
        return new_word #Se for verdadeiro e importante retornar o restante de forma a verificarmos se e uma palavra
    if val_log == False:
        return val_log #Valor logico

"""Para alem de verificar se e uma silaba tambem vai adicionando as silavas ate nao existirem caracteres"""
def final_sil(var_sil_val):
    contador=0
    valor1 = len(var_sil_val)
    while contador < valor1:
        if var_sil_val[contador:] in gram(0):
            var_sil_val = var_sil_val[:contador]
            contador = 0
            val_log = True
            if len(var_sil_val) == 0:
                return val_log
        else:
            contador = contador + 1
            val_log = False
    return val_log
"""Tentativas"""
