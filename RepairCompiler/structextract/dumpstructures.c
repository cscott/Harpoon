/*
   This file is part of Kvasir, a Valgrind skin that implements the
   C language front-end for the Daikon Invariant Detection System

   Copyright (C) 2004 Philip Guo, MIT CSAIL Program Analysis Group

   This program is free software; you can redistribute it and/or
   modify it under the terms of the GNU General Public License as
   published by the Free Software Foundation; either version 2 of the
   License, or (at your option) any later version.
*/

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>

#include "dumpstructures.h"
#include "typedata.h"
#include "elf/dwarf2.h"

#define GETTYPE 1
#define POSTNAME 2

int process_elf_binary_data(char* filename);

int main(int argc, char **argv) {
  if (argc<2)
    return 0;
  process_elf_binary_data(argv[1]);
  daikon_preprocess_entry_array();
}

// Pre-processes global dwarf_entry_array in order to place
// the data in a form that can easily be turned into .decls
// and .dtrace files
void daikon_preprocess_entry_array()
{
  initializeTypeArray();
}

int typecount=0;

int entry_is_type(dwarf_entry *entry) {
  if (entry->tag_name==DW_TAG_structure_type||
      entry->tag_name==DW_TAG_union_type) {
    collection_type* collection_ptr = (collection_type*)(entry->entry_ptr);
    if (collection_ptr->name==0) {
      collection_ptr->name=(char*)malloc(100);
      sprintf(collection_ptr->name,"TYPE%ld",typecount++);
    }
    return 1;
  }
  return 0;
}

int entry_is_valid_function(dwarf_entry *entry) {
  if (tag_is_function(entry->tag_name)) {
    function* funcPtr = (function*)(entry->entry_ptr);
    if (funcPtr->start_pc != 0 && funcPtr->name != 0) {
      return 1;
    } else {
#ifdef SHOW_DEBUG
      printf("Skipping invalid-looking function %s\n", funcPtr->name);
#endif
    }
  }
  return 0;
}

// Finds the number of function entries in the dwarf_entry_array
// and creates the DaikonFunctionInfo array to match that size

void initializeTypeArray()
{
  int i;
  dwarf_entry * cur_entry;
  for (i = 0; i < dwarf_entry_array_size; i++)
    {
      cur_entry = &dwarf_entry_array[i];
      if (entry_is_type(cur_entry))
        {
	  collection_type* collection_ptr = (collection_type*)(cur_entry->entry_ptr);
	  int j=0;
	  int offset=0;
	  printf("structure %s {\n",collection_ptr->name);
	  
	  for(j=0;j<collection_ptr->num_members;j++) {
	    dwarf_entry *entry=collection_ptr->members[j];
	    member * member_ptr=(member *)entry->entry_ptr;
	    char *name=member_ptr->name;
	    dwarf_entry *type=member_ptr->type_ptr;
	    char *typestr=printname(type,GETTYPE);
	    char *poststr=printname(type,POSTNAME);
	    if (member_ptr->data_member_location>offset) {
	      printf("   reserved byte[%ld];\n",member_ptr->data_member_location-offset);
	      offset=member_ptr->data_member_location;
	    }
	    offset+=getsize(type);

	    printf("   %s %s%s;\n",typestr,name,poststr);
	  }
	  printf("}\n\n");
        }
    }
}

int getsize(dwarf_entry *type) {
  if (type==NULL)
    return 0;
  switch(type->tag_name) {
  case DW_TAG_enumeration_type:
    return 4;
  case DW_TAG_array_type: {
    modifier_type * modifier_ptr=(modifier_type*)type->entry_ptr;
    int size=((array_bound*)modifier_ptr->array_ptr[0]->entry_ptr)->upperbound+1;
    return size*getsize(modifier_ptr->target_ptr);
  }
  case DW_TAG_base_type: {
    base_type *base=(base_type*)type->entry_ptr;
    return base->byte_size;
  }
  case DW_TAG_pointer_type: {
    return 4;
  }
  case DW_TAG_structure_type: {
    collection_type *ctype=(collection_type*)type->entry_ptr;
    return ctype->byte_size;
  }
  default:
    return 0;
  }
}

char * printname(dwarf_entry * type,int op) {
  if (type==NULL) {
    if (op==GETTYPE)
      return NULL;
  }

  switch(type->tag_name) {
  case DW_TAG_enumeration_type:
    if (op==GETTYPE)
      return "int";
    break;
  case DW_TAG_array_type: {
    modifier_type * modifier_ptr=(modifier_type*)type->entry_ptr;
    if (op==GETTYPE) {
	char *typename=printname(modifier_ptr->target_ptr,op);
	return typename;
    } else if (op==POSTNAME) {
      int size=((array_bound*)modifier_ptr->array_ptr[0]->entry_ptr)->upperbound+1;
      char *typename=printname(modifier_ptr->target_ptr,op);
      char *newptr=(char *)malloc(200);
      sprintf(newptr,"%s[%ld]",typename,size);
      return newptr;
    }
  }
    break;
  case DW_TAG_base_type: {
    base_type *base=(base_type*)type->entry_ptr;
    if (op==GETTYPE)
      switch(base->byte_size) {
      case 1:
	return "byte";
      case 2:
	return "short";
      case 4:
	return "int";
      default: {
	char *m=(char*)malloc(100);
	sprintf(m,"error%ld",base->byte_size);
	return m;
      }
      }
  }
    break;
  case DW_TAG_pointer_type: {
    modifier_type * modifier_ptr=(modifier_type*)type->entry_ptr;
    if (op==GETTYPE) {
      if (modifier_ptr->target_ptr==NULL)
	return "void *"; /* seems like a good guess */
      {
	char *typename=printname(modifier_ptr->target_ptr,op);
	/* evil hack */
	char *newptr=(char *)malloc(200);
	sprintf(newptr,"%s *",typename);
	return newptr;
      }
    }
  }
    break;
  case DW_TAG_structure_type: {
    collection_type *ctype=(collection_type*)type->entry_ptr;
    if (op==GETTYPE&&ctype->name==NULL) {
      ctype->name=(char*)malloc(100);
      sprintf(ctype->name,"TYPE%ld",typecount++);
    }
    if (op==GETTYPE)
      return ctype->name;
  }
    break;
  default:
    if (op==GETTYPE)
      return "unknown";
  }
  if (op==POSTNAME)
    return "";
  return "ERROR";
}


