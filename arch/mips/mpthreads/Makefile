# compile with SGI's fancy 'openMP' compiler.

mpthreads.o: mpthreads.c
	cc -non_shared -64 -G8 -O3 -mp -g -o $@ -c $<
mpthreads.so: mpthreads.c
	cc -64 -mp -g -o $@ -c $<
clean:
	$(RM) mpthreads.*o
