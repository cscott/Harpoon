#ifndef DOT
#define DOT

struct dotclass {
  struct dottransition * transitions;
};

struct dottransition {
  char *role1;
  char *role2;
  char *transitionname;
  char type; /* 0=method, 1=nonmethod*/
  struct dottransition *same;
  struct dottransition *next;
};
void addtransition(struct genhashtable *htable, struct classname *class, char *role1, char * transitionname, char *role2, int type);
void dotrolemethod(struct genhashtable * htable, struct genhashtable *revtable, struct rolemethod *rm);
void dotrolechange(struct genhashtable *htable, struct heap_state *hs,
		   struct rolechange *rc);
void printdot(struct classname *class, struct dotclass *dotclass);
#endif
