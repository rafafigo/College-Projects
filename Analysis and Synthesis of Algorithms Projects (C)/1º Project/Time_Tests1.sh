gcc -O3 -ansi -Wall tg06.c -o main

for((i=2; i<2000000; i=(i+12000))); do
	for((z=100; z < i; z = z*10)); do
		k=1
		y=$(($i+$k))
		if [ $z -lt  1001  ]; then
				./gerador $i $(($i-$k)) $z $k $y>input.in
				conections=$(sed -n 2p input.in)
				for((x=1; x<1000;x++)); do
					./gerador $i $(($i-$k)) $z $k $y>>input.in
				done
				(time ./main) <input.in 2>time.t
				(grep "user	" | cut -d "	" -f2) <time.t >>resultados/time$z.in
				echo $(($i+$conections)) >>resultados/vertexEdge$z
		fi
	done
done

