CircuitRouter:
	cd CircuitRouterSeqSolver && make
	cd CircuitRouterSimpleShell && make
	cd CircuitRouterSeqSolver && mv CircuitRouter-SeqSolver ../
	cd CircuitRouterSimpleShell && mv CircuitRouter-SimpleShell ../

clean:
	cd CircuitRouterSeqSolver && make $@
	cd CircuitRouterSimpleShell && make $@
	rm -f CircuitRouter-SeqSolver CircuitRouter-SimpleShell

