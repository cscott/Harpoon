CC = gcc -O0 -Wall -pg -g -a

RoleInference: RoleInference.o Hashtable.o ObjectSet.o ObjectPair.o GenericHashtable.o CalculateDominators.o Role.o Method.o Effects.o dot.o Incremental.o Names.o Container.o Fields.o RoleRelation.o
	$(CC) -o RoleInference RoleInference.o Hashtable.o ObjectSet.o ObjectPair.o GenericHashtable.o CalculateDominators.o Role.o Method.o Effects.o dot.o Incremental.o Names.o Container.o Fields.o RoleRelation.o

RoleRelation.o: RoleRelation.c RoleRelation.h
	$(CC) -c RoleRelation.c

Hashtable.o: Hashtable.c Hashtable.h
	$(CC) -c Hashtable.c

Fields.o: Fields.c Fields.h
	$(CC) -c Fields.c

Container.o: Container.c Container.h
	$(CC) -c Container.c

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

dot.o: dot.c dot.h
	$(CC) -c dot.c

Method.o: Method.c Method.h
	$(CC) -c Method.c

Names.o: Names.c Names.h
	$(CC) -c Names.c

Effects.o: Effects.c Effects.h
	$(CC) -c Effects.c

Incremental.o: Incremental.c
	$(CC) -c Incremental.c

CalculateDominators.o: CalculateDominators.c CalculateDominators.h
	$(CC) -c CalculateDominators.c

clean:
	rm RoleInference.o Hashtable.o RoleInference ObjectPair.o ObjectSet.o GenericHashtable.o CalculateDominators.o Role.o Method.o Effects.o dot.o Incremental.o Names.o Container.o Fields.o RoleRelation.o

