#ifndef NAMES
#define NAMES
struct classname {
  char *classname;
};

struct fielddesc {
  char *fielddesc;
};

struct methodname {
  struct classname * classname;
  char * methodname;
  char * signature;
};

struct fieldname {
  struct classname * classname;
  struct fielddesc * fielddesc;
  char * fieldname;
};

struct namer {
  struct genhashtable * classtable;
  struct genhashtable * desctable;
  struct genhashtable * methodtable;
  struct genhashtable * fieldtable;
};

struct namer * allocatenamer();
struct fieldname * getfieldc(struct namer *n,struct classname *cn, char *fieldname, char * fielddesc);
struct classname * getclass(struct namer * n, char * classname);
struct fielddesc * getdesc(struct namer * n, char * fielddesc);
struct methodname * getmethod(struct namer *n,char *classname, char *methodname, char *signature);
struct fieldname * getfield(struct namer *n,char *classname, char *fieldname, char * fielddesc);
int hashclass(struct classname *class);
int compareclass(struct classname *cl1, struct classname *cl2);
int hashdesc(struct fielddesc *fd);
int comparedesc(struct fielddesc *fd1, struct fielddesc *fd2);
int hashmethod(struct methodname *method);
int comparemethod(struct methodname *m1, struct methodname *m2);
int hashfield(struct fieldname *fn);
int comparefield(struct fieldname *fn1, struct fieldname *fn2);
int hashptr(void *);

#endif
