# Makefile Geral - Versão 2
# IST LEIC-T Sistemas Operativos 18/19
# Miguel Levezinho, 90756
# Rafael Figueiredo, 90770

CircuitRouter:
	cd lib && make
	cd CircuitRouterParSolver && make
	cd CircuitRouterSeqSolver && make

	cd CircuitRouterParSolver && mv CircuitRouter-ParSolver ../
	cd CircuitRouterSeqSolver && mv CircuitRouter-SeqSolver ../

clean:
	cd lib && make $@
	cd CircuitRouterParSolver && make $@
	cd CircuitRouterSeqSolver && make $@
	rm -f CircuitRouter-ParSolver CircuitRouter-SeqSolver

