#ifndef TMODEL_H
#define TMODEL_H

#include "classlist.h"

#define STYPE_STRUCT 1
#define STYPE_INT 2
#define STYPE_BIT 3
#define STYPE_BYTE 4
#define STYPE_SHORT 5
#define STYPE_ARRAY 0x100

class structure {
  public:
  structure(char *nam);
  void settype(int t);
  //  void setparams(tparam **tp,int n);
  void setsubtype(ttype *sub);
  ttype * getsubtype();
  void setfields(tfield **fieldarray, int n);
  void setlabels(tlabel **lab,int n);
  /*  int getnumparams();
      tparam * getparam(int i);*/
  int getnumlabels();
  tlabel * getlabel(int i);
  int getnumfields();
  tfield * getfield(int i);
  char * getname();
  void print();
  int getsize(bitreader *br,model *m, Hashtable *env);

  private:
  int type;
  char *name;
  /*  tparam ** params;
      int numparams;*/
  ttype *subtype;
  tfield ** fields;
  int numfields;
  tlabel ** labels;
  int numlabels;
};

#define pointersize 4
#define intsize 4
#define shortsize 2

#define TTYPE_STRUCT 0x1
#define TTYPE_INT 0x2
#define TTYPE_BIT 0x3
#define TTYPE_BYTE 0x4
#define TTYPE_SHORT 0x5
#define TTYPE_PTR 0x100

class ttype {
 public:
  void print();
  ttype(int typ);
  ttype(char *t);
  ttype(char *t,  AElementexpr *size);
  ttype(int type, AElementexpr * size);
  void makeptr();
  void setsize(AElementexpr *size);
  AElementexpr * getsize();
  int getbytes(bitreader *br,model *m,Hashtable *env);

  bool isptr();
  int numderef();
  int gettype();
  int basesize(bitreader *br,model *m,Hashtable *env);
  /*  int getnumparamvalues();
      AElementexpr * getparamvalues(int i);*/
  char * getname();

 private:
  int primtype;
  int intlength; /*for variable length integers*/
  char * type;
  /*  AElementexpr ** paramvalues;
      int numparamvalues;*/
  AElementexpr * asize;
};

/*class tparam {
 public:
  void print();
  tparam(ttype *t,char * n);
  char * getname();

 private:
  ttype *type;
  char *name;
  };*/

class tlabel {
 public:
  void print();
  tlabel(tfield *f, char *fld,AElementexpr *a);
  char * getname();
  ttype * gettype();
  char *getfield();
  AElementexpr * getindex();

 private:
  char *field;
  AElementexpr *index;
  tfield * specifictype;
};

class tfield {
 public:
  tfield(ttype *tt, char *n);
  void print();
  ttype * gettype();
  char * getname();

 private:
  char *name;
  ttype *type;
  bool reserved;
};
#endif
