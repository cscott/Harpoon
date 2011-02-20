OBJS=support.o api.o

xyz: ${OBJS}
	gcc -O4 -I .. -o ole-dump ole-dump.c ${OBJS} ../libchecker.a -lstdc++
	gcc -O4 -I .. -o ole-break ole-break.c ${OBJS} ../libchecker.a -lstdc++

clean:
	rm -f *.o ole-dump
