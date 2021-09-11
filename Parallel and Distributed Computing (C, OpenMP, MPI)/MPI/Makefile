PDC:
	cd Serial && make
	cd MPI && make
	cd Serial && cp ballAlg  ../
	cd MPI && cp ballAlg-mpi ../

clean:
	@echo Cleaning...
	cd Serial && make $@
	cd MPI && make $@
	rm -f ballAlg ballAlg-omp ballAlg-mpi
