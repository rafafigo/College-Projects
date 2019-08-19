gcc -O3 -ansi -Wall hiper.c -o project

for((i=100; i < 100000 ; i = (i+400))); do
	calc=$(echo "sqrt ($i)" | bc )
	numVertices=$(($calc*$calc))
	adj=$(($calc/2))

	./gerador $calc $adj $calc $adj 2 >input.in
	(time ./project) <input.in 2>time.t
	(grep "user	" | cut -d "	" -f2) <time.t >>resultadosTime/time/time.in
	echo $numVertices >>resultadosTime/vertex/vertexes.in
done

