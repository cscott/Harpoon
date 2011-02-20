CC = gcc -O0 -Wall -pg -g -a

all: FastScan RoleInference

RoleInference: RoleInference.o Hashtable.o ObjectSet.o ObjectPair.o GenericHashtable.o CalculateDominators.o Role.o Method.o Effects.o dot.o Incremental.o Names.o Container.o Fields.o Classes.o RoleRelation.o web.o Common.o
	$(CC) -o RoleInference RoleInference.o Hashtable.o ObjectSet.o ObjectPair.o GenericHashtable.o CalculateDominators.o Role.o Method.o Effects.o dot.o Incremental.o Names.o Container.o Fields.o RoleRelation.o web.o Common.o Classes.o


FastScan: FastScan.o GenericHashtable.o Names.o Common.o
	$(CC) -o FastScan FastScan.o GenericHashtable.o Names.o Common.o

Common.o: Common.c
	$(CC) -c Common.c

RoleRelation.o: RoleRelation.c RoleRelation.h
	$(CC) -c RoleRelation.c

FastScan.o: FastScan.c FastScan.h RoleRelation.h
	$(CC) -c FastScan.c

Hashtable.o: Hashtable.c Hashtable.h
	$(CC) -c Hashtable.c

Fields.o: Fields.c Fields.h
	$(CC) -c Fields.c

Classes.o: Classes.c Classes.h
	$(CC) -c Classes.c

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

web.o: web.c web.h
	$(CC) -c web.c

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
	rm RoleInference.o Hashtable.o RoleInference ObjectPair.o ObjectSet.o GenericHashtable.o CalculateDominators.o Role.o Method.o Effects.o dot.o Incremental.o Names.o Container.o Fields.o RoleRelation.o web.o FastScan.o FastScan Common.o Classes.o

