CC = gcc -O0 -g -Wall


RoleInference: RoleInference.o Hashtable.o ObjectSet.o ObjectPair.o GenericHashtable.o CalculateDominators.o Role.o Method.o
	$(CC) -o RoleInference RoleInference.o Hashtable.o ObjectSet.o ObjectPair.o GenericHashtable.o CalculateDominators.o Role.o Method.o

Hashtable.o: Hashtable.c Hashtable.h
	$(CC) -c Hashtable.c

GenericHashtable.o: GenericHashtable.c GenericHashtable.h
	$(CC) -c GenericHashtable.c

ObjectSet.o: ObjectSet.c ObjectSet.h
	$(CC) -c ObjectSet.c

ObjectPair.o: ObjectPair.c ObjectPair.h
	$(CC) -c ObjectPair.c

RoleInference.o: RoleInference.c RoleInference.h
	$(CC) -c RoleInference.c

Role.o: Role.c Role.h
	$(CC) -c Role.c

Method.o: Method.c Method.h
	$(CC) -c Method.c

CalculateDominators.o: CalculateDominators.c CalculateDominators.h
	$(CC) -c CalculateDominators.c

clean:
	rm RoleInference.o Hashtable.o RoleInference ObjectPair.o ObjectSet.o GenericHashtable.o CalculateDominators.o Role.o Method.o





