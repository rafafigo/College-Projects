(time ./main) <T06.in 2>time.t
			(grep "user	" | cut -d "	" -f2 | tr -d  '/a-z,A-Z/' ) <time.t >>results