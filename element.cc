// provides an object wrapper around elementary types

#include <stdlib.h>
#include <stdio.h>
#include <assert.h>
#include "element.h"


Element::Element() {
  typevalue=ELEMENT_OBJECT;
  token=NULL;
  object=NULL;
  integervalue=0;
  bytevalue=0;
  bitvalue=false;
  shortvalue=0;
  itemtype=NULL;
}

char * Element::gettoken() {
  assert(typevalue==ELEMENT_TOKEN);
  return token;
}

bool Element::isnumber() {
  if ((type()==ELEMENT_INT)||
      (type()==ELEMENT_BYTE)||
      (type()==ELEMENT_SHORT))
    return true;
  else
    return false;
}

Element::Element(void * objptr, structure * str) {
  object=objptr;
  typevalue=ELEMENT_OBJECT;
  itemtype=str;
  token=NULL;
  integervalue=0;
  bytevalue=0;
  bitvalue=false;
  shortvalue=0;
}

Element::Element(int value) {
  typevalue=ELEMENT_INT;
  integervalue=value;
  object=NULL;
  itemtype=NULL;
  bytevalue=0;
  bitvalue=false;
  token=NULL;
  shortvalue=0;
}

Element::Element(short value) {
  typevalue=ELEMENT_SHORT;
  shortvalue=value;
  integervalue=0;
  object=NULL;
  itemtype=NULL;
  bytevalue=0;
  bitvalue=false;
  token=NULL;
}

Element::Element(bool b) {
  typevalue=ELEMENT_BIT;
  bitvalue=b;
  integervalue=0;
  object=NULL;
  itemtype=NULL;
  bytevalue=0;
  token=NULL;
  shortvalue=0;
}

Element::Element(char byte) {
  typevalue=ELEMENT_BYTE;
  bytevalue=byte;
  integervalue=0;
  object=NULL;
  itemtype=NULL;
  token=NULL;
  bitvalue=false;
  shortvalue=0;
}

Element::Element(char * token) {/*Value destroyed by this destructor*/
  this->token=token;
  typevalue=ELEMENT_TOKEN;
  integervalue=0;
  bytevalue=0;
  bitvalue=false;
  object=NULL;
  itemtype=NULL;
  shortvalue=0;
}

Element::Element(Element * o) {
  this->integervalue=o->integervalue;
  this->bytevalue=o->bytevalue;
  this->itemtype=o->itemtype;
  this->token=copystr(o->token);
  this->object=o->object;
  this->typevalue=o->typevalue;
  this->bitvalue=o->bitvalue;
  this->shortvalue=o->shortvalue;
}

void Element::print() {
  switch(typevalue) {
  case ELEMENT_INT:
    printf("Int(%d)",integervalue);
    break;
  case ELEMENT_SHORT:
    printf("Short(%d)",shortvalue);
    break;
  case ELEMENT_BIT:
    if (bitvalue)
      printf("Bit(true)");
    else
      printf("Bit(false)");
    break;
  case ELEMENT_BYTE:
    printf("Int(%c)",bytevalue);
    break;
  case ELEMENT_TOKEN:
    printf("Token(%s)",token);
    break;
  case ELEMENT_OBJECT:
    printf("Object(%p)",object);
    break;
  }
}

unsigned int Element::hashCode() {
  switch(typevalue) {
  case ELEMENT_INT:
    return integervalue;
  case ELEMENT_SHORT:
    return shortvalue;
  case ELEMENT_BIT:
    if (bitvalue)
      return 1;
    else
      return 0;
  case ELEMENT_BYTE:
    return bytevalue;
  case ELEMENT_TOKEN:
    return hashstring(token);
  case ELEMENT_OBJECT:
    return (int)object;
  }
}

bool Element::equals(ElementWrapper *other) {
  if (other->type()!=typevalue)
    return false;
  switch(typevalue) {
  case ELEMENT_INT:
    return (this->integervalue==((Element *)other)->integervalue);
  case ELEMENT_SHORT:
    return (this->shortvalue==((Element *)other)->shortvalue);
  case ELEMENT_BIT:
    return (this->bitvalue==((Element *)other)->bitvalue);
  case ELEMENT_BYTE:
    return (this->bytevalue==((Element *)other)->bytevalue);
  case ELEMENT_TOKEN:
    return (equivalentstrings(token,((Element *)other)->token)==1);
  case ELEMENT_OBJECT:
    return object==((Element *)other)->object;
  }
}

int Element::intvalue() {
  switch(typevalue) {
  case ELEMENT_INT:
    return integervalue;
  case ELEMENT_SHORT:
    return shortvalue;
  case ELEMENT_BYTE:
    return bytevalue;
  default:
    printf("Illegal int conversion on Element\n");
    exit(-1);
  }
}

short Element::getshortvalue() {
  assert(typevalue==ELEMENT_SHORT);
  return shortvalue;
}

char Element::getbytevalue() {
  assert(typevalue==ELEMENT_BYTE);
  return bytevalue;
}

bool Element::getboolvalue() {
  assert(typevalue==ELEMENT_BIT);
  return bitvalue;
}

void * Element::getobject() {
  assert(typevalue==ELEMENT_OBJECT);
  return object;
}

int Element::type() {
  return typevalue;
}

structure * Element::getstructure() {
  return itemtype;
}

Element::~Element() {
  if(typevalue==ELEMENT_TOKEN)
    delete[] token;
}

unsigned int hashelement(ElementWrapper *e) {
  return e->hashCode();
}

int elementequals(ElementWrapper *e1, ElementWrapper *e2) {
  return e1->equals(e2);
}
