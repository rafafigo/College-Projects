%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%                                    Projecto Termometros                                               %
%                                  Rafael Figueiredo - 90770                                            %  
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

:- [exemplos_puzzles].

             % 1 predicado - Propaga 
% Obtem as implicacoes que a posicao pertendida tem

propaga([[H|_], _, _], Pos, Posicoes):- member(Pos,H),				     % Verifica se o Termometro contem aquela posicao
							prefixo(Pos, H, Ele_ant),       	         % Obtem todos os elementos anteriores ha posicao
							append(Ele_ant,[Pos],Nao_Ordenado),          % Adiciona o elemento posicao
							sort(Nao_Ordenado,Posicoes),!.               % Ordena os elementos da lista
										
propaga([[_|T], _, _], Pos, Posicoes):-	propaga([T, _, _],Pos,Posicoes). % Verifica se o proximo termometro contem a posicao e obtem a sua propagacao

prefixo(Pos,[Pos|_],[]):-!.                                              % Encontrou a posicao pretendida

prefixo(Pos,[H|T],[H|Z]) :- prefixo(Pos,T,Z).                            % Obtem recursivamente todos os elementos anteriores ao pos


                 % 2 predicado  Nao Altera linhas anteriores
/* Verifica se o preenchimento de uma linha nao altera as linhas anteriores, ou seja se o 
ja_preenchidas contem todos os elementos com linhas inferiores, que vem do preenchimento dessa linha */ 

nao_altera_linhas_anteriores([],_,_).                                    % Percorreu todos os elementos

nao_altera_linhas_anteriores([H|R],L,Ja_preenchidas):-
				pertence(H,L,Ja_preenchidas),                            % Vai verificar se o elemento verifica as condicoes necessarias
				nao_altera_linhas_anteriores(R,L,Ja_preenchidas).


pertence((H,_),L,_):- H >= L,!.                                          % Caso a linha do elemento seja maior ou igual ha linha a ser verificada, nao falha

pertence(H,_,Ja_preenchidas):- member(H,Ja_preenchidas).                 % Caso a linha seja menor verifica se pertence ao ja prenchidas

              % 3 predicado - Verifica Parcial
/* Verifica a possibilidade de preencher uma dada linha, sem violar 
o total das colunas, contando com as escolhas feitas anteriormente */ 

verifica_parcial([_,_,Num_col],Ja_preenchidas,Dim,Poss):-
								Cont_Col is 0,                               % Inicializa a coluna
								union(Ja_preenchidas,Poss,Juncao),           % Une as posicoes potenciais a preencher para uma linha com as ja preenchidas
								tentar_colunas(Num_col,Juncao,Dim,Cont_Col). % Verifica se nao ultrapassa o numero total de colunas

tentar_colunas([],_,Dim,Dim):-!.                                             % Chegou ha ultima coluna

tentar_colunas([H|R],Juncao,Dim,Cont_Col):-
						Cont_Col_1 is Cont_Col + 1,                          % Passa para a proxima coluna
						Cont_Col_1 =< Dim,                                   % Verifica que ainda nao chegou ha ultima coluna
						verifica_coluna(H,Juncao,0,Cont_Col_1,_),            % Vai verificar se essa coluna nao ultrapassa os limites da coluna para o puzzle
						tentar_colunas(R,Juncao,Dim,Cont_Col_1).

verifica_coluna(_,[],N_Col,_,N_Col):-!.

verifica_coluna(C_M,[(_,H)|R],N_Col,Num_da_Col,Col_Pr):-
				H =:= Num_da_Col,                                             % Verifica se este elemento possui a coluna a avaliar  
				Col_cont is N_Col + 1,                                        % Incrementa o numero de colunas preenchidas na hipotese
				Col_cont =< C_M,!,                                            % Verifica se nao ultrapassou o numero total de colunas a preencher para o puzzle
				verifica_coluna(C_M,R,Col_cont,Num_da_Col,Col_Pr).            % Continua a verificar os outros elementos

verifica_coluna(C_M,[(_,H)|R],N_Col,Num_da_Col,Col_Pr):-
				H =\= Num_da_Col,                                             % Este elemento nao possui a coluna a avaliar
				verifica_coluna(C_M,R,N_Col,Num_da_Col,Col_Pr).               % Continua a verificar os outros elementos


           % 4 predicado - Possibilidades Linha
/* Determina as possibilidades existentes para preencher uma 
determinada linha, tendo em conta as escolhas das linhas anteriores */

possibilidades_linha(Puz,[(H,P)|R],Total,Ja_preenchidas,Possibilidades):-     
				intersection([(H,P)|R],Ja_preenchidas,Elems),                                % Obtem os elementos da linha que ja estao na ja preenchidas
				findall(Lista_hip,comb(Total,[(H,P)|R],Lista_hip),Lista_hipoteses),          % O findall junta todas as hipotes das combinacoes numa lista
				hipot_pr(Elems,Lista_hipoteses,Lista_hip_1),                                 % Elimina as hipoteses que nao contem os elementos do ja preenchidas  
				verificar_hipotese(Puz,Lista_hip_1,H,Total,Ja_preenchidas,Possibilidades).


% Esta funcao gera todas as combinacoes possiveis, mas em hipoteses 
comb(0,_,[]):-!.                                                   % Encontrou uma hipotese                                                    

comb(N_El_Lst,[Ele_L|Res_L],[Ele_L|Comb]):- N_El_Lst>0,         % Verifica se ja obteu todos os elementos
			N_El_Lst_1 is N_El_Lst-1,                           % Decrementa o numero de elementos que e preciso obter
			comb(N_El_Lst_1,Res_L,Comb).                        % Volta a chamar a funcao ate obter os elementos de uma combinacao

comb(N_El_Lst,[_|Res_L],Comb):- N_El_Lst>0,                         
			comb(N_El_Lst,Res_L,Comb).                          % Chama com elemento seguinte 


hipot_pr(_,[],[]):-!.                                           % Chegou ao fim da lista de elementos

hipot_pr(Elem,[H|R],[H|S]):-                                    
		subset(Elem,H),!,                                       % Verifica se os elementos com a linha pretendida que estao no ja preenchidas estao na lista a verificar
		hipot_pr(Elem,R,S).                                     % Proxima possibilidade

hipot_pr(Elem,[_|R],Lista_hip_1):-
		hipot_pr(Elem,R,Lista_hip_1).                           % Nao possui os elementos necessarios


verificar_hipotese(_,[],_,_,_,[]):-!.                                          % Acabou as possibilidades

verificar_hipotese(Puz,[H|R],Line,Total,Ja_preenchidas,Possibilidades):-
			obtem_propagacao(Puz,H,Total,Ja_preenchidas,Pos_1),                % Obtem a propagacao de todos os elementos
			sort(Pos_1,Pos_Ord),                                               % Ordena a lista
			conta_ele(Pos_Ord,Line,Total,0,Ele),                               % Conta o numero de vezes que aparece a linha na propagacao
			Ele =:= Total,                                                     
			tamanho_Puzz(Puz,Dim),                                             % Obtem a Dimensao
			verifica_parcial(Puz,Ja_preenchidas,Dim,Pos_Ord),                  % Faz a verificacao do 3 predicado
			nao_altera_linhas_anteriores(Pos_Ord,Line,Ja_preenchidas),!,       % Faz a verificacao do 4 predicado
			verificar_hipotese(Puz,R,Line,Total,Ja_preenchidas,Resto),         % Continua a verficar as outras possibilidades
			sort([Pos_Ord|Resto],Possibilidades).                              % Faz a ordenacao

verificar_hipotese(Puz,[_|R],Line,Total,Ja_preenchidas,Resto):-
			verificar_hipotese(Puz,R,Line,Total,Ja_preenchidas,Resto).         % Proxima possibilidade

obtem_propagacao(_,[],_,_,[]):-!.                                              % Fez a propagacao de todos os elementos

obtem_propagacao(Puz,[(H,P)|R],Total,Ja_preenchidas,Poss_L):-
			propaga(Puz,(H,P),Poss),                                           % Faz a propagacao do elemento
			obtem_propagacao(Puz,R,Total,Ja_preenchidas,Poss_R),               % Faz para o Proximo
			append(Poss,Poss_R,Poss_L).                                        % Adiciona a propagacao de todos os elementos a uma lista


tamanho_Puzz([_,_,C],Dim):-
				length(C,Dim),!.                                                 % Dimensao do Puzzle

conta_ele([],_,_,Ele,Ele):-!.                                                    % Fica o numero de elementos com a dada linha

conta_ele([(H,_)|R],L,Total,Ele_1,Ele):-
				Ele_1 =< Total,                                                % Verifica que e menor que o numero total de elementos
				H =:= L,                                                       % Verifica que este elemento possui a dada linha
				Ele_2 is Ele_1 + 1,!,                                          % Aumenta o numero de vezes que aparece
				conta_ele(R,L,Total,Ele_2,Ele).                                % Proximo elemento

conta_ele([(H,_)|R],L,Total,Ele_1,Ele):-                                      
				H =\= L,                                                       % Nao e a dada linha
				conta_ele(R,L,Total,Ele_1,Ele).                                % Proximo elemento

%  5 predicado  - Resolve
% Obtem a solucao se existir 

resolve([Term,L,C],Solucao):-
				length(C,Tamanho),                                             % Calcula a dimensao do puzzle
			    procura_Solu([Term,L,C],0,[],Tamanho,Solucao).              

/* Procura Solucao vai criando para cada linha todas as possibilidades de preenchimento da linha de acordo com escolhas anteriores,
utilizando o member, conseguimos percorrer cada um dos elementos da lista, se falhar este tenta com o proximo elemento da lista de possibilidades
de cada uma das linhas */

procura_Solu(_,Linha,Ja_preenchidas,C,Ja_preenchidas):-
			Linha >= C,!.                                                                  % Encontrou Solucao

procura_Solu([Term,L,C],Linha,Ja_preenchidas,Tamanho,Solucao_1):-
		Linha_1 is Linha + 1,                                                              % Passa ha proxima linha
		encontra_linhas(Tamanho,Linha_1,1,Lista_listas),                                   % Obtem todas os elementos com aquela linha
		encontra_total(L,1,Linha_1,Dim),                                                   % Encontra o Total de Elementos daquela linha referente ao puzzle               
		possibilidades_linha([Term,L,C],Lista_listas,Dim,Ja_preenchidas,Possibilidades_l), % Pedicado 5 que nos da todas as hipoteses para a linha
		member(Poss_L_P,Possibilidades_l),                                                 % O member permite permite percorrer todos os elementos das possibilidades um a um, se falhar, tenta com o elemento seguinte da lista
		union(Poss_L_P,Ja_preenchidas,Poss_T),                                             % Junta esta possibilidade de linha ao ja preenchidas
		procura_Solu([Term,L,C],Linha_1,Poss_T,Tamanho,Solucao),                           % Proxima linha
		sort(Solucao,Solucao_1).                                                           % Faz a ordenacao



encontra_linhas(T,Linha,T,[(Linha,T)]):-!.                                                  % Encontrou a coluna final               

encontra_linhas(Tamanho,Linha,C,[(Linha,C)|R]):-
			C =< Tamanho,                                                                   % Verifica se ja fez todos os elementos com a dada linha
			Tamanho_N is C + 1,                                                             % Proxima coluna
			encontra_linhas(Tamanho,Linha,Tamanho_N,R).                                     % Continua ate encontrar a coluna final

encontra_total([Dim|_],Linha,Linha,Dim):-!.                                                 % Encontra a dimensao do preenchimento da linha referente ao puzzle

encontra_total([_|P],T,Linha,Dim):-
			T < Linha,                                                                      % Ainda nao esta na linha correcta     
			T_1 is T + 1,                                                                   % Incrementa a linha
			encontra_total(P,T_1,Linha,Dim).                                                % Tenta novamente
