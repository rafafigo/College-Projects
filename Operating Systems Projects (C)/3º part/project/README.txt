=====================================
IST LEIC-T Sistemas Operativos 18/19
Exercicio 3 - README.txt

Authors:
Miguel Levezinho  - 90756
Rafael Figueiredo - 90770
=====================================

Estrutura de Diretorias:

  Temos uma pasta para:
	- CircuitRouterClient    - Código do cliente
	- CircuitRouterAdvShell  - Código da shell
	- CircuitRouterParSolver - Código da solução concorrente
	- CircuitRouterSeqSolver - Código da solução sequencial
	- lib - Código adicional a usar em ambas as soluções
	- inputs - Exemplos de inputs com informação de circuitos a gerar pelo código
	- results - Resultados das comparações entre correr a solução sequencial e a solução paralela

Compilar código:

  Correr o comando 'make', na diretoria /so1819_ex3_g48, que irá correr o Makefile principal e chamar os Makefiles que compilam o código em cada uma das pastas com ficheiros .c (CircuitRouterParSolver, CircuitRouterSeqSolver , CircuitRouter-AdvShell , CircuitRouter-Client e lib).

  Serão criados quatro executáveis que serão movidos para a diretoria /so1819_ex3_g48, CircuitRouter-SeqSolver e CircuitRouter-ParSolver, CircuitRouter-AdvShell, CircuitRouter-Client.

  Para apagar todos os binários, correr 'make clean' na diretoria /so1819_ex3_g48, apaga tambem todas as diretorias criadas pelo client com os respectivos fifos.

Executar código:

AdvShell:
	1. Correr a shell, cria um fifo que é o pathname+.pipe pode ser usado só por stdin ou com vários clientes (tem de ser dado o pathname do fifo).
ParSolver:

  Na diretoria /so1819_ex3_g48:
    1. Correr com script, que irá criar um ficheiro .speedups.csv do respetivo input na pasta results:

      ./doText.sh [numTarefas] [path/nomeDoInput]

    2. Correr na linha de comandos:
		
      ./CircuitRouter-ParSolver -t [numTarefas] [path/nomeDoinput]

Informação da máquina usada para comparar soluções e gerar ficheiros com speedups:
 
  Número de cores: 4
  Clock rate: 3.40GHz
  Modelo: 58, Intel(R) Core(TM) i5-3570
