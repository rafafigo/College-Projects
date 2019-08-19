gcc -O3 -ansi -Wall tg06.c -o main

for((i=2; i<2000000; i=(i+10000) )); do
	for((z=100; z < i ; z = z*10)); do
			x=1
			y=$(($i+$x))
			if [ $z -lt  1001 ]; then
				./gerador $i $(($i-$x)) $z $x $y >input.in
				conections=$(sed -n 2p input.in)
				(valgrind --tool=massif ./main <input.in) 2>valgrind.t
				number=$((grep "Massif, a heap profiler" | tr -dc '0-9') <valgrind.t)
				ms_print massif.out.$number >valgrind.t
				var1=$((grep -m 1 "B" | tr -d " ") <valgrind.t)
				var2=$((grep -m 1 "#")<valgrind.t)
				echo $var2 $var1 >>resultados/memory$z
				echo $(($i+$conections)) >>resultados/vertexEdgesmemory$z
				rm -rf massif*
			fi
	done
done
