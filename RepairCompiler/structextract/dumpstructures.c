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
int assigntype=0;
int entry_is_type(dwarf_entry *entry) {
  if (entry->tag_name==DW_TAG_structure_type||
      entry->tag_name==DW_TAG_union_type) {
    collection_type* collection_ptr = (collection_type*)(entry->entry_ptr);
    /*    if (collection_ptr->name==0&&assigntype) {
      collection_ptr->name=(char*)malloc(100);
      sprintf(collection_ptr->name,"TYPE%ld",typecount++);
      }*/
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

struct valuepair {
  int index;
  int value;
};

void initializeTypeArray()
{
  int i;
  dwarf_entry * cur_entry;
  struct genhashtable * ght=genallocatehashtable((unsigned int (*)(void *)) & hashstring,(int (*)(void *,void *)) &equivalentstrings);
  
  for (i = 0; i < dwarf_entry_array_size; i++)
    {
      cur_entry = &dwarf_entry_array[i];
      if (entry_is_type(cur_entry))
        {
	  collection_type* collection_ptr = (collection_type*)(cur_entry->entry_ptr);
	  int j=0;
	  int offset=0;
	  int value=0;
	  for(j=0;j<collection_ptr->num_members;j++) {
	    dwarf_entry *entry=collection_ptr->members[j];
	    if (entry->tag_name==DW_TAG_inheritance) {
	      value++;
	      continue;
	    }
	    {
	      member * member_ptr=(member *)entry->entry_ptr;
	      char *name=member_ptr->name;
	      dwarf_entry *type=member_ptr->type_ptr;
	      char *typestr=printname(type,GETTYPE);
	      char *poststr=printname(type,POSTNAME);
	      
	      if (typestr!=NULL)
		value++;
	    }
	  }
	  if (collection_ptr->name!=NULL) {
	    struct valuepair *vp=NULL;
	    if (gencontains(ght,collection_ptr->name))
	      vp=(struct valuepair *)gengettable(ght,collection_ptr->name);
	    if (vp==NULL||vp->value<value) {
	      if (vp==NULL) {
		vp=(struct valuepair*)calloc(1,sizeof(struct valuepair));
		genputtable(ght,collection_ptr->name,vp);
	      }
	      vp->value=value;
	      vp->index=i;
	    }
	  }
        }
    }

  assigntype=1;
  for (i = 0; i < dwarf_entry_array_size; i++)
    {
      cur_entry = &dwarf_entry_array[i];
      if (entry_is_type(cur_entry))
        {
	  collection_type* collection_ptr = (collection_type*)(cur_entry->entry_ptr);
	  int j=0;
	  int offset=0;
	  if (collection_ptr->name==NULL)
	    continue;
	  if (gencontains(ght,collection_ptr->name)) {
	    struct valuepair *vp=(struct valuepair*)gengettable(ght,collection_ptr->name);
	    if (vp->index!=i)
	      continue;
	  }
	  j=0;
	  printf("structure %s ",collection_ptr->name);

	  while(j<collection_ptr->num_members&&
		collection_ptr->members[j]->tag_name==DW_TAG_inheritance) {
	    inherit *in_ptr=(inherit*)collection_ptr->members[j]->entry_ptr;
	    dwarf_entry *typeptr=in_ptr->target_ptr;
	    collection_type* sub_ptr = (collection_type*)(typeptr->entry_ptr);
	    if (j==0)
	      printf("subclasses ");
	    else
	      printf(", ");
	    printf("%s ",sub_ptr->name);
	    j++;
	  }
	  printf("{ \n");
	  
	  for(j=0;j<collection_ptr->num_members;j++) {
	    dwarf_entry *entry=collection_ptr->members[j];
	    if (entry->tag_name==DW_TAG_inheritance) {
	      inherit * inherit_ptr=(inherit *)entry->entry_ptr;
	      if (inherit_ptr->data_member_location>offset) {
		printf("   reserved byte[%ld];\n",inherit_ptr->data_member_location-offset);
		offset=inherit_ptr->data_member_location;
	      }
	      {
		dwarf_entry *type=inherit_ptr->target_ptr;
		collection_type *c_ptr=(collection_type*)type->entry_ptr;
		offset+=printtype(c_ptr,ght);
	      }
    	    } else {
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
	  }
	  if (offset<collection_ptr->byte_size)
	    printf("   reserved byte[%ld];\n",collection_ptr->byte_size-offset);
	  printf("}\n\n");
        }
    }
}

int printtype(collection_type *collection_ptr,struct genhashtable *ght)
{
  int j=0;
  int offset=0;
  int value=0;
  
  struct valuepair *vp=NULL;
  if (gencontains(ght,collection_ptr->name))
    vp=(struct valuepair *)gengettable(ght,collection_ptr->name);
  if (vp!=NULL)
    collection_ptr=(collection_type*) dwarf_entry_array[vp->index].entry_ptr;

  for(j=0;j<collection_ptr->num_members;j++) {
    dwarf_entry *entry=collection_ptr->members[j];
    if (entry->tag_name==DW_TAG_inheritance) {
      inherit * inherit_ptr=(inherit *)entry->entry_ptr;
      if (inherit_ptr->data_member_location>offset) {
	printf("   reserved byte[%ld];\n",inherit_ptr->data_member_location-offset);
	offset=inherit_ptr->data_member_location;
      }

      {
	dwarf_entry *type=inherit_ptr->target_ptr;
	collection_type *c_ptr=(collection_type*)type->entry_ptr;
	offset+=printtype(c_ptr,ght);
      }
    } else {
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
  }
  return offset;
}



int getsize(dwarf_entry *type) {
  if (type==NULL)
    return 0;
  switch(type->tag_name) {
  case DW_TAG_enumeration_type:
    return 4;
  case DW_TAG_array_type: {
    modifier_type * modifier_ptr=(modifier_type*)type->entry_ptr;
    int size=1;
    int i;
    for(i=0;i<modifier_ptr->num_array;i++) {
      size*=((array_bound*)modifier_ptr->array_ptr[i]->entry_ptr)->upperbound+1;
    }
    return size*getsize(modifier_ptr->target_ptr);
  }
  case DW_TAG_const_type:
    {
      consttype * ctype_ptr=(consttype*)type->entry_ptr;
      return getsize(ctype_ptr->target_ptr);
    }
    break;
  case DW_TAG_base_type: {
    base_type *base=(base_type*)type->entry_ptr;
    return base->byte_size;
  }
  case DW_TAG_pointer_type: {
    return 4;
  }
  case DW_TAG_union_type: 
  case DW_TAG_structure_type: {
    collection_type *ctype=(collection_type*)type->entry_ptr;
    return ctype->byte_size;
  }
  case DW_TAG_subroutine_type: {
    return 4;
  }
  case DW_TAG_typedef: 
    {
      tdef * tdef_ptr=(tdef*)type->entry_ptr;
      return getsize(tdef_ptr->target_ptr);
    }
    break;

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
      int i;
      int size=1;
      char *typename=printname(modifier_ptr->target_ptr,op);
      char *newptr=(char *)malloc(200);
      for(i=0;i<modifier_ptr->num_array;i++) {
	size*=((array_bound*)modifier_ptr->array_ptr[i]->entry_ptr)->upperbound+1;
      }
      sprintf(newptr,"%s[%ld]",typename,size);
      return newptr;
    }
  }
    break;
  case DW_TAG_const_type:
    {
      consttype * ctype_ptr=(consttype*)type->entry_ptr;
      if (op==GETTYPE) {
	char *typename=printname(ctype_ptr->target_ptr,op);
	return typename;
      }
    }
    break;
  case DW_TAG_subroutine_type: {
    return "void";
  }
  case DW_TAG_typedef: 
    {
      tdef * tdef_ptr=(tdef*)type->entry_ptr;
      if (op==GETTYPE) {
	char *typename=printname(tdef_ptr->target_ptr,op);
	return typename;
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
  case DW_TAG_union_type:
  case DW_TAG_structure_type: {
    collection_type *ctype=(collection_type*)type->entry_ptr;
    if (op==GETTYPE&&ctype->name==NULL&&assigntype) {
      ctype->name=(char*)malloc(100);
      sprintf(ctype->name,"TYPE%ld",typecount++);
    }
    if (op==GETTYPE)
      return ctype->name;
  }
    break;
  default:
    if (op==GETTYPE) {
      if (!assigntype)
	return NULL;
      else {
	char * p=(char *)malloc(100);
	sprintf(p,"0x%x",type->tag_name);
	return p;
      }
    }
  }
  if (op==POSTNAME)
    return "";
  return "ERROR";
}


