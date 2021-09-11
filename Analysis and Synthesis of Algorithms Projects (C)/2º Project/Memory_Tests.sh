gcc -O3 -ansi -Wall hiper.c -o project

for((i=39100; i < 100000 ; i = (i+500))); do
	calc=$(echo "sqrt ($i)" | bc )
	numVertices=$(($calc*$calc))
	adj=$(($calc/2))

	./gerador $calc $adj $calc $adj 2 >input.in

	(valgrind --tool=massif ./project <input.in) 2>valgrind.t
	number=$((grep "Massif, a heap profiler" | tr -dc '0-9') <valgrind.t)
	ms_print massif.out.$number >valgrind.t
	var1=$((grep -m 1 "B" | tr -d " ") <valgrind.t)
	var2=$((awk 'NR == 9')<valgrind.t)
	echo $var2 $var1 >>resultadosMemory/memory/memory
	echo $numVertices >>resultadosMemory/vertex/vertexmemory
	rm -rf massif*
done
