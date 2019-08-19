# Makefile Geral - Versão 3
# IST LEIC-T Sistemas Operativos 18/19
# Miguel Levezinho, 90756
# Rafael Figueiredo, 90770

CircuitRouter:
	cd lib && make
	cd CircuitRouterParSolver && make
	cd CircuitRouterSeqSolver && make
	cd CircuitRouterAdvShell && make
	cd CircuitRouterClient && gcc CircuitRouter-Client.c -Wall -std=gnu99 -o CircuitRouter-Client 

	cd CircuitRouterParSolver && mv CircuitRouter-ParSolver ../
	cd CircuitRouterSeqSolver && mv CircuitRouter-SeqSolver ../
	cd CircuitRouterAdvShell && mv CircuitRouter-AdvShell ../
	cd CircuitRouterClient && mv CircuitRouter-Client ../

clean:
	cd lib && make $@
	cd CircuitRouterParSolver && make $@
	cd CircuitRouterSeqSolver && make $@
	cd CircuitRouterAdvShell && make $@ 
	rm -f CircuitRouter-ParSolver CircuitRouter-SeqSolver CircuitRouter-AdvShell CircuitRouter-Client
	cd /tmp/ && rm -rf client_*

