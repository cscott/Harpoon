/*
   This file is part of Kvasir, a Valgrind skin that implements the
   C language front-end for the Daikon Invariant Detection System

   Copyright (C) 2004 Philip Guo, MIT CSAIL Program Analysis Group

   This program is free software; you can redistribute it and/or
   modify it under the terms of the GNU General Public License as
   published by the Free Software Foundation; either version 2 of the
   License, or (at your option) any later version.
*/

/* typedata.h:
   Everything here attempts to extract the information directly
   from the DWARF2 debugging information embedded within an ELF
   executable, piggy-backing off of readelf.c code. These data
   structures mimic the types of DWARF2 entries that we are interested
   in tracking.
*/

#ifndef TYPEDATA_H
#define TYPEDATA_H

// Type information data structures

// Contains one entry that holds data for one of many possible types
// depending on tag_name
typedef struct
{
  unsigned long ID; // Unique ID for each entry
  unsigned long tag_name; // DW_TAG_____ for the type of this entry
  void* entry_ptr; // Cast this pointer depending on value of tag_name
  unsigned int level;
} dwarf_entry;

// Entries for individual types

typedef struct
{
  unsigned long byte_size; // DW_AT_byte_size
  unsigned long encoding;

  //  char is_bit_field; // 1 = bit field
  // Only relevant for bit fields
  unsigned long bit_size;
  unsigned long bit_offset;
} base_type; // DW_TAG_base_type

// COP-OUT!!! Treat array_type JUST LIKE pointer_type for now
// so we don't keep track of the array size.  We only care about the
// FIRST ELEMENT of the array since we just treat all pointers as
// arrays of size 1 -
// I will add full support for arrays later!!! //PG
// modifier_type = {const_type, pointer_type, array_type, volatile_type}
typedef struct
{
  unsigned long target_ID; // ID of the entry that contains the type that this modifies
  dwarf_entry* target_ptr; // Type that this entry modifies (DW_AT_type)
  int num_array;
  dwarf_entry** array_ptr;
} modifier_type;

typedef struct
{
  unsigned long target_ID; // ID of the entry that contains the type that this modifies
  unsigned long upperbound;
  dwarf_entry* target_ptr; // Type that this entry modifies (DW_AT_type)
} array_bound;

typedef struct
{
  unsigned long target_ID; // ID of the entry that contains the type that this modifies
  char *name;
  dwarf_entry* target_ptr; // Type that this entry modifies (DW_AT_type)
} tdef;

typedef struct
{
  unsigned long target_ID; // ID of the entry that contains the type that this modifies
  dwarf_entry* target_ptr; // Type that this entry modifies (DW_AT_type)
} consttype;

typedef struct
{
  unsigned long target_ID; // ID of the entry that contains the type that this modifies
  dwarf_entry* target_ptr; // Type that this entry modifies (DW_AT_type)
  long data_member_location; // Addr offset relative to struct head
} inherit;

// collection_type = {structure_type, union_type, enumeration_type}
typedef struct
{
  char* name;
  unsigned long byte_size;
  unsigned long num_members;
  dwarf_entry** members; // Array of size num_members, type = {member, enumerator}
} collection_type;

// struct or union member
typedef struct
{
  char* name;
  unsigned long type_ID;
  dwarf_entry* type_ptr;
  long data_member_location; // Addr offset relative to struct head
                             // This will be 0 for a union
                             // This is stored as:
                             // (DW_OP_plus_uconst: x)
                             // where x is the location relative to struct head
  //  char is_bit_field; // 1 = bit field
  // Only relevant for bit fields
  unsigned long byte_size;
  unsigned long bit_offset;
  unsigned long bit_size;
} member;

// enumeration member
typedef struct
{
  char* name;
  long const_value; // Enumeration value (SIGNED!)
} enumerator;

// FUNCTION!!!
typedef struct
{
  char* name;
  char* filename; // The file name relative to the compilation directory
  unsigned long return_type_ID;
  dwarf_entry* return_type;
  unsigned long num_formal_params;
  dwarf_entry* params; // Array of size num_formal_params, type = {formal_parameter}
  int is_external; /* Is it extern? If so, probably want to skip it */
  unsigned long start_pc; /* Location of the function in memory */
} function;

/* This is for abstract function types, as might be used in declaring
   a parameter as taking a function pointer. At least for the moment, we
   won't bother about the parameters. */
typedef struct {
  unsigned long return_type_ID;
  dwarf_entry* return_type;
} function_type;

// function formal parameter
typedef struct
{
  char* name;
  unsigned long type_ID;
  dwarf_entry* type_ptr;
  long location; // Offset from function base (this is SIGNED!)
                 // This is stored as: (DW_OP_fbreg: x),
                 // where x is location offset
} formal_parameter;

// compile_unit - only used to figure out filename and compilation directory
// We assume that every function belongs to the file specified
// by the nearest compile_unit entry (to its left) in dwarf_entry_array
typedef struct
{
  char* filename;
  char* comp_dir;
} compile_unit;

// Globals

extern dwarf_entry* dwarf_entry_array;
extern unsigned long dwarf_entry_array_size;

// Function declarations

// From readelf.c
char *get_TAG_name(unsigned long tag);
int process_elf_binary_data(char* filename);

// From typedata.c
char tag_is_relevant_entry(unsigned long tag);
char tag_is_modifier_type(unsigned long tag);
char tag_is_collection_type(unsigned long tag);
char tag_is_base_type(unsigned long tag);
char tag_is_member(unsigned long tag);
char tag_is_enumerator(unsigned long tag);
char tag_is_function(unsigned long tag);
char tag_is_formal_parameter(unsigned long tag);
char tag_is_compile_unit(unsigned long tag);
char tag_is_function_type(unsigned long tag);
char entry_is_listening_for_attribute(dwarf_entry* e, unsigned long attr);

char harvest_type_value(dwarf_entry* e, unsigned long value);
char harvest_byte_size_value(dwarf_entry* e, unsigned long value);
char harvest_encoding_value(dwarf_entry* e, unsigned long value);
char harvest_bit_size_value(dwarf_entry* e, unsigned long value);
char harvest_bit_offset_value(dwarf_entry* e, unsigned long value);
char harvest_const_value(dwarf_entry* e, unsigned long value);
char harvest_name(dwarf_entry* e, const char* str);
char harvest_comp_dir(dwarf_entry* e, const char* str);
char harvest_location(dwarf_entry* e, long value);
char harvest_data_member_location(dwarf_entry* e, long value);
char harvest_string(dwarf_entry* e, unsigned long attr, const char* str);
char harvest_external_flag_value(dwarf_entry *e, unsigned long value);
char harvest_address_value(dwarf_entry* e, unsigned long attr, unsigned long value);
char harvest_ordinary_unsigned_value(dwarf_entry* e, unsigned long attr, unsigned long value);

char binary_search_dwarf_entry_array(unsigned long target_ID, unsigned long* index_ptr);

void link_entries_to_type_entries();
void link_collection_to_members(dwarf_entry* e, unsigned long dist_to_end);
void link_function_to_params(dwarf_entry* e, unsigned long dist_to_end);
void initialize_function_filenames();
void link_array_entries_to_members();
void print_dwarf_entry(dwarf_entry* e);

void initialize_dwarf_entry_array(unsigned long num_entries);
void destroy_dwarf_entry_array(void);
void print_dwarf_entry_array();
void initialize_dwarf_entry_ptr(dwarf_entry* e);
void finish_dwarf_entry_array_init(void);

char tag_is_modifier_type(unsigned long tag);
char tag_is_collection_type(unsigned long tag);
char tag_is_base_type(unsigned long tag);
char tag_is_member(unsigned long tag);
char tag_is_enumerator(unsigned long tag);
char tag_is_function(unsigned long tag);
char tag_is_formal_parameter(unsigned long tag);


#endif
