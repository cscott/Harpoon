/*
   This file is part of Kvasir, a Valgrind skin that implements the
   C language front-end for the Daikon Invariant Detection System

   Copyright (C) 2004 Philip Guo, MIT CSAIL Program Analysis Group

   This program is free software; you can redistribute it and/or
   modify it under the terms of the GNU General Public License as
   published by the Free Software Foundation; either version 2 of the
   License, or (at your option) any later version.
*/

/* kvasir_runtime.h:
   Contains the majority of the type definitions that are necessary
   for Kvasir functionality.
*/

#ifndef DUMP_H
#define DUMP_H

#include "typedata.h"
#include "common.h"
#include "GenericHashtable.h"

struct StructureElement {
  char *fieldname;
  int StructureType;
  int isArray;
  int arraySize;
  char *structurename;
  struct StructureElement *next;
};

struct Structure {
  char* name;
  struct StructureElement * struct_ele;
};

#define TYPE_INT 0
#define TYPE_SHORT 1
#define TYPE_BYTE 2
#define TYPE_BIT 3
#define TYPE_STRUCTURE 4
#define TYPE_POINTER 5
#define TYPE_RESERVED 6

/* Array that holds information about all functions*/
struct Structure * TypeArray;
unsigned long TypeArraySize;

void daikon_preprocess_entry_array();
void initializeTypeArray();
int entry_is_type(dwarf_entry *entry);
char * printname(dwarf_entry * entry,int op);
int getsize(dwarf_entry *type);
int printtype(collection_type *collection_ptr,struct genhashtable *);
#endif
