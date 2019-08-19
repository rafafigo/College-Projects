#!/bin/bash
# =====================================
# IST LEIC-T Sistemas Operativos 18/19
# Exercicio 2 - doTest.sh
#
# Authors:
# Miguel Levezinho  - 90756
# Rafael Figueiredo - 90770
# =====================================

SEQ="CircuitRouter-SeqSolver"
PAR="CircuitRouter-ParSolver"

# Checks if number of arguments != 2 >&2 means that is written in stderr
if [ ! $# -eq 2 ]; then
	echo Error: Invalid Number of Arguments  >&2

# Checks if File Exists
elif [ ! -e $2 ]; then
	echo Error: $2 does not exist  >&2

# Checks if SeqSolver exists
elif [ ! -e $SEQ ]; then
	echo Error: $SEQ missing  >&2

# Checks if ParSolver exists
elif [ ! -e $PAR ]; then
	echo Error: $PAR missing >&2

# Checks if number of tasks >= 0
elif [[ ! $1 =~ ^[0-9]+$ ]]; then                              
	echo Error: Number of tasks invalid!

else
	# By using only > we are overriding whats inside the file, if it exists
	echo "#threads,exec_time,speedup" > "$2.speedups.csv"

	# Runs with seqSolver and then goes to the file created and gets the Elapsed time
	./$SEQ $2
	seqTime=$(grep "Elapsed time" "$2.res"| cut -d "=" -f2 | cut -d "s" -f1 | tr -d ' ')
		
	# By using >> we are appending to whats already inside the file
	echo 1s,$seqTime,1 >> "$2.speedups.csv"
		
	# Runs the parSolver from 1 to numberOfTasks given, gets the Elapsed time and computes the speedup for each one
	for i in $(seq 1 $1)
	do
		./$PAR -t $i $2
		parTime=$(grep "Elapsed time" "$2.res"| cut -d "=" -f2 | cut -d "s" -f1 | tr -d ' ')
		speedup=$(echo "scale=6; ${seqTime}/${parTime}" | bc)
		echo $i,$parTime,$speedup >> "$2.speedups.csv"
	done
fi

