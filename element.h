#ifndef ELEMENT_H
#define ELEMENT_H
#include "common.h"
#include <stdlib.h>
#include "classlist.h"

class ElementWrapper;

#define ELEMENT_INT 1
#define ELEMENT_BIT 2
#define ELEMENT_BYTE 3
#define ELEMENT_TOKEN 4
#define ELEMENT_OBJECT 5
#define ELEMENT_FTUPLE 6
#define ELEMENT_SHORT 7

class ElementWrapper {
 public:
  virtual unsigned int hashCode()=0;
  virtual bool equals(ElementWrapper *other)=0;
  virtual int type()=0;
 private:
};

class Element:public ElementWrapper {
 public:
  Element();
  Element(int value);
  Element(short value);
  Element(bool b);
  Element(char byte);
  Element(char * token);
  Element(Element * o);
  unsigned int hashCode();
  bool equals(ElementWrapper *other);
  int intvalue();
  short getshortvalue();
  char getbytevalue();
  bool getboolvalue();
  bool isnumber();
  void * getobject();
  int type();
  structure * getstructure();
  Element(void * objptr, structure * str);
  char * gettoken();
  ~Element();
  void print();

 private:
  char *token;
  void *object;
  int typevalue;
  short shortvalue;
  int integervalue;
  char bytevalue;
  bool bitvalue;
  structure *itemtype;
};

unsigned int hashelement(ElementWrapper *e);

int elementequals(ElementWrapper *e1, ElementWrapper *e2);

#endif
