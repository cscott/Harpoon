/*
   This file is part of Kvasir, a Valgrind skin that implements the
   C language front-end for the Daikon Invariant Detection System

   Copyright (C) 2004 Philip Guo, MIT CSAIL Program Analysis Group

   This program is free software; you can redistribute it and/or
   modify it under the terms of the GNU General Public License as
   published by the Free Software Foundation; either version 2 of the
   License, or (at your option) any later version.
*/

/* typedata.c:
   This file contains functions that serve to complement readelf.c
   and arrange the DWARF2 debugging information in an orderly
   format within dwarf_entry_array
*/

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "typedata.h"
#include "elf/dwarf2.h"

// Global array of all dwarf entries, sorted (hopefully) by dwarf_entry.ID
// so that binary search is possible
// DO NOT MODIFY THIS POINTER MANUALLY!!!
// Representation invariants:
// 1. Every entry in dwarf_entry_array is sorted by ascending ID
//    (This makes binary search possible)
// 2. dwarf_entry_array points to the beginning of the array
// 3. The size of the array is specified by dwarf_entry_array_size
// 4. All function entries are listed adjacent to their formal parameters
// 5. All struct, union, and enumeration entries are listed adjacent
//    to their members
// 6. All entries in the array belong to the file specified by the first
//    compile_unit entry to its left (lower indices) in the array
dwarf_entry* dwarf_entry_array = 0;

// The size of this array
unsigned long dwarf_entry_array_size = 0;


/*----------------------------------------
Extracting type information from DWARF tag
-----------------------------------------*/


/*
Requires:
Modifies:
Returns: 1 if tag = {DW_TAG_base_type, _const_type, _enumerator,
                     _formal_parameter, _pointer_type, _array_type, _subprogram,
                     _union_type, _enumeration_type, _member,
                     _structure_type, _volatile_type, _compile_unit},
                     0 otherwise
Effects: Used to determine which entries to record into a dwarf_entry structure;
         All relevant entries should be included here
*/
char tag_is_relevant_entry(unsigned long tag)
{
  return 1;
  switch (tag)
    {
    case DW_TAG_enumeration_type:
    case DW_TAG_formal_parameter:
    case DW_TAG_member:
    case DW_TAG_pointer_type:
    case DW_TAG_structure_type:
    case DW_TAG_union_type:
    case DW_TAG_base_type:
    case DW_TAG_const_type:
    case DW_TAG_enumerator:
    case DW_TAG_subprogram:
    case DW_TAG_volatile_type:
    case DW_TAG_compile_unit:
    case DW_TAG_array_type:
    case DW_TAG_subroutine_type:
    case DW_TAG_subrange_type:
      return 1;
    default:
      return 0;
    }
}

/*
Requires:
Modifies:
Returns: 1 if tag = {DW_TAG_pointer_type, _array_type, _const_type, _volatile_type},
                     0 otherwise
Effects: Used to determine if the type is a modifier - modifier types
         refer to another type within the dwarf_entry_array after
         preprocessing
*/
char tag_is_modifier_type(unsigned long tag)
{
  switch (tag)
    {
    case DW_TAG_pointer_type:
    case DW_TAG_array_type:
    case DW_TAG_const_type:
    case DW_TAG_volatile_type:
      return 1;
    default:
      return 0;
    }
}

/*
Requires:
Modifies:
Returns: 1 if tag = {DW_TAG_enumeration_type, _structure_type, _union_type},
                     0 otherwise
Effects: Used to determine if the type is a collection of some sort -
         collections have members and unique type names
*/
char tag_is_collection_type(unsigned long tag)
{
  switch (tag)
    {
    case DW_TAG_enumeration_type:
    case DW_TAG_structure_type:
    case DW_TAG_union_type:
      return 1;
    default:
      return 0;
    }
}

// The rest of these should be self-explanatory:
char tag_is_base_type(unsigned long tag)
{
  return (tag == DW_TAG_base_type);
}

char tag_is_member(unsigned long tag)
{
  return (tag == DW_TAG_member);
}

char tag_is_enumerator(unsigned long tag)
{
  return (tag == DW_TAG_enumerator);
}

char tag_is_function(unsigned long tag)
{
  return (tag == DW_TAG_subprogram);
}

char tag_is_formal_parameter(unsigned long tag)
{
  return (tag == DW_TAG_formal_parameter);
}

char tag_is_compile_unit(unsigned long tag)
{
  return (tag == DW_TAG_compile_unit);
}

char tag_is_function_type(unsigned long tag) {
  return (tag == DW_TAG_subroutine_type);
}

/*------------------
 Attribute listeners
 ------------------*/

// Each type stored in dwarf_entry.entry_ptr listens for particular
// attributes.  e.g. collection_type listens for DW_AT_name and DW_AT_byte_size

// DW_AT_location: formal_parameter
// DW_AT_name: collection_type, member, enumerator, function, formal_parameter, compile_unit
// DW_AT_byte_size: base_type, collection_type, member
// DW_AT_bit_offset: base_type, member
// DW_AT_bit_size: base_type, member
// DW_AT_const_value: enumerator
// DW_AT_data_member_location: member
// DW_AT_type: modifier, member, function, formal_parameter
// DW_AT_encoding: base_type
// DW_AT_comp_dir: compile_unit
// DW_AT_external: function
// DW_AT_low_pc: function

// Returns: 1 if the entry has a type that is listening for the
// given attribute (attr), 0 otherwise
char entry_is_listening_for_attribute(dwarf_entry* e, unsigned long attr)
{
  unsigned long tag;

  if(e == 0)
    return 0;

  tag = e->tag_name;
  switch(attr)
    {
    case DW_AT_location:
      return tag_is_formal_parameter(tag);
    case DW_AT_name:
      return (tag_is_collection_type(tag) ||
              tag_is_member(tag) ||
              tag_is_enumerator(tag) ||
              tag_is_function(tag) ||
              tag_is_formal_parameter(tag) ||
              tag_is_compile_unit(tag));
    case DW_AT_byte_size:
      return (tag_is_base_type(tag) ||
              tag_is_collection_type(tag) ||
              tag_is_member(tag));
    case DW_AT_bit_offset:
      return (tag_is_base_type(tag) ||
              tag_is_member(tag));
    case DW_AT_bit_size:
      return (tag_is_base_type(tag) ||
              tag_is_member(tag));
    case DW_AT_const_value:
      return tag_is_enumerator(tag);
    case DW_AT_data_member_location:
      return tag_is_member(tag);
    case DW_AT_type:
      return (tag_is_modifier_type(tag) ||
              tag_is_member(tag) ||
              tag_is_function(tag) ||
              tag_is_formal_parameter(tag) ||
              tag_is_function_type(tag));
    case DW_AT_upper_bound:
      return (tag==DW_TAG_subrange_type);
    case DW_AT_encoding:
      return tag_is_base_type(tag);
    case DW_AT_comp_dir:
      return tag_is_compile_unit(tag);
    case DW_AT_external:
      return tag_is_function(tag);
    case DW_AT_low_pc:
      return tag_is_function(tag);
    default:
      return 0;
    }
}

/*--------
Harvesters
---------*/
// Harvest attribute values into the appropriate entry
// Returns a boolean to signal success or failure
// Remember to only harvest attribute value if the type is listening for it

char harvest_type_value(dwarf_entry* e, unsigned long value)
{
  unsigned long tag;
  if ((e == 0) || (e->entry_ptr == 0))
    return 0;

  tag = e->tag_name;

  if (tag ==DW_TAG_subrange_type) {
    ((array_bound*)e->entry_ptr)->target_ID = value;
    return 1;
  } else if (tag_is_modifier_type(tag))
    {
      ((modifier_type*)e->entry_ptr)->target_ID = value;
      return 1;
    }
  else if (tag_is_member(tag))
    {
      ((member*)e->entry_ptr)->type_ID = value;
      return 1;
    }
  else if (tag_is_function(tag))
    {
      ((function*)e->entry_ptr)->return_type_ID = value;
      return 1;
    }
  else if (tag_is_formal_parameter(tag))
    {
      ((formal_parameter*)e->entry_ptr)->type_ID = value;
      return 1;
    }
  else if (tag_is_function_type(tag))
    {
      ((function_type *)e->entry_ptr)->return_type_ID = value;
      return 1;
    }
  else
    return 0;
}

char harvest_byte_size_value(dwarf_entry* e, unsigned long value)
{
  unsigned long tag;
  if ((e == 0) || (e->entry_ptr == 0))
    return 0;

  tag = e->tag_name;

  if (tag_is_base_type(tag))
    {
      ((base_type*)e->entry_ptr)->byte_size = value;
      return 1;
    }
  else if (tag_is_collection_type(tag))
    {
      ((collection_type*)e->entry_ptr)->byte_size = value;
      return 1;
    }
  else if (tag_is_member(tag))
    {
      ((member*)e->entry_ptr)->byte_size = value;
      return 1;
    }
  else
    return 0;
}

char harvest_encoding_value(dwarf_entry* e, unsigned long value)
{
  unsigned long tag;
  if ((e == 0) || (e->entry_ptr == 0))
    return 0;

  tag = e->tag_name;

  if (tag_is_base_type(tag))
    {
      ((base_type*)e->entry_ptr)->encoding = value;
      return 1;
    }
  else
    return 0;
}

char harvest_bit_size_value(dwarf_entry* e, unsigned long value)
{
  unsigned long tag;
  if ((e == 0) || (e->entry_ptr == 0))
    return 0;

  tag = e->tag_name;

  if (tag_is_base_type(tag))
    {
      ((base_type*)e->entry_ptr)->bit_size = value;
      return 1;
    }
  else if (tag_is_member(tag))
    {
      ((member*)e->entry_ptr)->bit_size = value;
      return 1;
    }
  else
    return 0;
}


char harvest_bit_offset_value(dwarf_entry* e, unsigned long value)
{
  unsigned long tag;
  if ((e == 0) || (e->entry_ptr == 0))
    return 0;

  tag = e->tag_name;

  if (tag_is_base_type(tag))
    {
      ((base_type*)e->entry_ptr)->bit_offset = value;
      return 1;
    }
  else if (tag_is_member(tag))
    {
      ((member*)e->entry_ptr)->bit_offset = value;
      return 1;
    }
  else
    return 0;
}

char harvest_const_value(dwarf_entry* e, unsigned long value)
{
  unsigned long tag;
  if ((e == 0) || (e->entry_ptr == 0))
    return 0;

  tag = e->tag_name;

  if (tag_is_enumerator(tag))
    {
      ((enumerator*)e->entry_ptr)->const_value = value;
      return 1;
    }
  else
    return 0;
}

char harvest_upper_bound(dwarf_entry* e, unsigned long value)
{
  unsigned long tag;
  if ((e == 0) || (e->entry_ptr == 0))
    return 0;

  tag = e->tag_name;

  if (tag==DW_TAG_subrange_type)
    {
      ((array_bound*)e->entry_ptr)->upperbound = value;
      return 1;
    }
  else
    return 0;
}

// REMEMBER to use strdup to make a COPY of the string
// or else you will run into SERIOUS memory corruption
// problems when readelf.c frees those strings from memory!!!
char harvest_name(dwarf_entry* e, const char* str)
{
  unsigned long tag;
  if ((e == 0) || (e->entry_ptr == 0))
    return 0;

  tag = e->tag_name;

  if (tag_is_enumerator(tag))
    {
      ((enumerator*)e->entry_ptr)->name = strdup(str);
      return 1;
    }
  else if (tag_is_collection_type(tag))
    {
      ((collection_type*)e->entry_ptr)->name = strdup(str);
      return 1;
    }
  else if (tag_is_member(tag))
    {
      ((member*)e->entry_ptr)->name = strdup(str);
      return 1;
    }
  else if (tag_is_function(tag))
    {
      ((function*)e->entry_ptr)->name = strdup(str);
      return 1;
    }
  else if (tag_is_formal_parameter(tag))
    {
      ((formal_parameter*)e->entry_ptr)->name = strdup(str);
      return 1;
    }
  else if (tag_is_compile_unit(tag))
    {
      ((compile_unit*)e->entry_ptr)->filename = strdup(str);
      return 1;
    }
  else
    return 0;
}

char harvest_comp_dir(dwarf_entry* e, const char* str)
{
  unsigned long tag;
  if ((e == 0) || (e->entry_ptr == 0))
    return 0;

  tag = e->tag_name;

  if (tag_is_compile_unit(tag))
    {
      ((compile_unit*)e->entry_ptr)->comp_dir = strdup(str);
      return 1;
    }
  else
    return 0;
}

char harvest_location(dwarf_entry* e, long value)
{
  unsigned long tag;
  if ((e == 0) || (e->entry_ptr == 0))
    return 0;

  tag = e->tag_name;

  if (tag_is_formal_parameter(tag))
    {
      ((formal_parameter*)e->entry_ptr)->location = value;
      return 1;
    }
  else
    return 0;
}

char harvest_data_member_location(dwarf_entry* e, long value)
{
  unsigned long tag;
  if ((e == 0) || (e->entry_ptr == 0))
    return 0;

  tag = e->tag_name;

  if (tag_is_member(tag))
    {
      ((member*)e->entry_ptr)->data_member_location = value;
      return 1;
    }
  else
    return 0;
}

char harvest_string(dwarf_entry* e, unsigned long attr, const char* str)
{
  if ((e == 0) || (e->entry_ptr == 0))
    return 0;

  if (attr == DW_AT_name)
    return harvest_name(e, str);
  else if (attr == DW_AT_comp_dir)
    return harvest_comp_dir(e, str);
  else
    return 0;
}

char harvest_external_flag_value(dwarf_entry *e, unsigned long value) {
  unsigned long tag;
  if ((e == 0) || (e->entry_ptr == 0))
    return 0;

  tag = e->tag_name;

  if (tag_is_function(tag))
    {
      ((function*)e->entry_ptr)->is_external = value;
      return 1;
    }
  else
    return 0;
}

char harvest_address_value(dwarf_entry* e, unsigned long attr,
                           unsigned long value) {
  unsigned long tag;
  if ((e == 0) || (e->entry_ptr == 0))
    return 0;

  tag = e->tag_name;

  if (tag_is_function(tag) && attr == DW_AT_low_pc)
    {
      ((function*)e->entry_ptr)->start_pc = value;
      return 1;
    }
  else
    return 0;
}


char harvest_ordinary_unsigned_value(dwarf_entry* e, unsigned long attr, unsigned long value)
{
  if ((e == 0) || (e->entry_ptr == 0))
    return 0;

  // Multiplex since
  // DW_AT_byte_size, DW_AT_encoding, DW_AT_const_value,
  // DW_AT_bit_size, DW_AT_bit_offset and DW_AT_external
  // return ordinary unsigned data
  switch(attr)
    {
    case DW_AT_byte_size:
      return harvest_byte_size_value(e, value);
    case DW_AT_encoding:
      return harvest_encoding_value(e, value);
    case DW_AT_const_value:
      return harvest_const_value(e, value);
    case DW_AT_upper_bound:
      return harvest_upper_bound(e, value);
    case DW_AT_bit_size:
      return harvest_bit_size_value(e, value);
    case DW_AT_bit_offset:
      return harvest_bit_offset_value(e, value);
    case DW_AT_external:
      return harvest_external_flag_value(e, value);
    default:
      return 0;
    }
}

/*
Requires: dwarf_entry_array initialized
Modifies:
Returns: success
Effects: Performs a binary search through dwarf_entry_array, looking for
         the entry with the matching ID field (target_ID).
         Stores the index of the matching entry in index_ptr
*/
char binary_search_dwarf_entry_array(unsigned long target_ID, unsigned long* index_ptr)
{
  unsigned long upper = dwarf_entry_array_size - 1;
  unsigned long lower = 0;

  //  printf("--target_ID: 0x%x, index_ptr: 0x%x, upper.ID: 0x%x, lower.ID: 0x%x\n",
         //         target_ID,
         //         index_ptr,
         //         dwarf_entry_array[upper].ID,
         //         dwarf_entry_array[lower].ID);

  // First do boundary sanity check to save ourselves lots of useless work:
  if ((target_ID > dwarf_entry_array[upper].ID) ||
      (target_ID < dwarf_entry_array[lower].ID))
    return 0;

  while (upper > lower)
    {
      unsigned long mid = (upper + lower) / 2;
      unsigned long cur_ID = dwarf_entry_array[mid].ID;

      //      printf("**lower: %d, mid: %d, upper: %d, target_ID: 0x%x, cur_ID: 0x%x\n",
      //             lower,
      //             mid,
      //             upper,
      //             target_ID,
      //             cur_ID);

      // Special case - (upper == (lower + 1)) - that means only 2 entries left to check:
      if (upper == (lower + 1))
        {
          if (target_ID == dwarf_entry_array[lower].ID)
            {
              *index_ptr = lower;
              return 1;
            }
          else if (target_ID == dwarf_entry_array[upper].ID)
            {
              *index_ptr = upper;
              return 1;
            }
          else
            {
              // YOU LOSE!  The target_ID is BETWEEN the lower and upper entries
              return 0;
            }
        }
      else if (target_ID == cur_ID) // Right on!
        {
          *index_ptr = mid;
          return 1;
        }
      else if (target_ID < cur_ID)
        {
          upper = mid;
        }
      else if (target_ID > cur_ID)
        {
          lower = mid;
        }
    }

  // Return 0 if no answer found
  return 0;
}

/*
Requires: dwarf_entry_array initialized
Modifies: certain fields within certain entries within dwarf_entry_array
          (modifier_type::target_ptr, function::return_type,
           member::type_ptr, formal_parameter::type_ptr)
Returns:
Effects: Links every entry with a type_ID to the actual entry of that type
         within dwarf_entry_array.  Sets the appropriate type_ptr pointers to point
         to entries within dwarf_entry_array where that type resides
         (relevant for modifier_type, member, function, and formal_parameter entries)
*/
void link_entries_to_type_entries()
{
  unsigned long idx;
  dwarf_entry* cur_entry = 0;

  for (idx = 0; idx < dwarf_entry_array_size; idx++)
    {
      unsigned long tag;
      cur_entry = &dwarf_entry_array[idx];
      tag = cur_entry->tag_name;

      if (tag_is_modifier_type(tag))
        {
          char success = 0;
          unsigned long target_index = 0;
          modifier_type* modifier_ptr = (modifier_type*)(cur_entry->entry_ptr);
          unsigned long target_ID = modifier_ptr->target_ID;

          // Use a binary search to try to find the index of the entry in the
          // array with the corresponding target_ID
          success = binary_search_dwarf_entry_array(target_ID, &target_index);
          if (success)
            {
              modifier_ptr->target_ptr=&dwarf_entry_array[target_index];
            }
	  if (tag==DW_TAG_array_type) {
	    int currentlevel=cur_entry->level;
	    dwarf_entry* tmp_entry = cur_entry+1;
	    int dist_to_end=dwarf_entry_array_size-idx;
	    int member_count=0;
	    while ((member_count < dist_to_end) // put this first! short-circuit eval.
		   && tmp_entry->level> currentlevel)
	      {
		if (tmp_entry->tag_name==DW_TAG_subrange_type&&(tmp_entry->level==(currentlevel+1)))
		  member_count++;
		tmp_entry++;
	      }
	    modifier_ptr->array_ptr=(dwarf_entry**)calloc(1,member_count*sizeof(dwarf_entry*));
	    modifier_ptr->num_array=member_count;
	    member_count=0;
	    tmp_entry=cur_entry+1;
	    while ((member_count < dist_to_end) // put this first! short-circuit eval.
		   && tmp_entry->level> currentlevel)
	      {
		if (tmp_entry->tag_name==DW_TAG_subrange_type&&(tmp_entry->level==(currentlevel+1)))
		  modifier_ptr->array_ptr[member_count++]=tmp_entry;
		tmp_entry++;
	      }
	  }
        }
      else if (tag==DW_TAG_subrange_type) {
          char success = 0;
          unsigned long target_index = 0;
          array_bound* bound_ptr = (array_bound*)(cur_entry->entry_ptr);
          unsigned long target_ID = bound_ptr->target_ID;

          // Use a binary search to try to find the index of the entry in the
          // array with the corresponding target_ID
          success = binary_search_dwarf_entry_array(target_ID, &target_index);
          if (success)
            {
              bound_ptr->target_ptr=&dwarf_entry_array[target_index];
            }
      }
      else if (tag_is_function(tag))
        {
          char success = 0;
          unsigned long target_index = 0;
          function* function_ptr = (function*)(cur_entry->entry_ptr);
          unsigned long target_ID = function_ptr->return_type_ID;

          // Use a binary search to try to find the index of the entry in the
          // array with the corresponding target_ID
          success = binary_search_dwarf_entry_array(target_ID, &target_index);
          if (success)
            {
              function_ptr->return_type=&dwarf_entry_array[target_index];
            }
        }
      else if (tag_is_function_type(tag))
        {
          char success = 0;
          unsigned long target_index = 0;
          function_type *function_ptr
            = (function_type *)(cur_entry->entry_ptr);
          unsigned long target_ID = function_ptr->return_type_ID;

          // Use a binary search to try to find the index of the entry in the
          // array with the corresponding target_ID
          success = binary_search_dwarf_entry_array(target_ID, &target_index);
          if (success)
            {
              function_ptr->return_type=&dwarf_entry_array[target_index];
            }
        }
      else if (tag_is_member(tag))
        {
          char success = 0;
          unsigned long target_index = 0;
          member* member_ptr = (member*)(cur_entry->entry_ptr);
          unsigned long target_ID = member_ptr->type_ID;

          // Use a binary search to try to find the index of the entry in the
          // array with the corresponding target_ID
          success = binary_search_dwarf_entry_array(target_ID, &target_index);
          if (success)
            {
              member_ptr->type_ptr=&dwarf_entry_array[target_index];
            }
        }
      else if (tag_is_formal_parameter(tag))
        {
          char success = 0;
          unsigned long target_index = 0;
          formal_parameter* formal_param_ptr = (formal_parameter*)(cur_entry->entry_ptr);
          unsigned long target_ID = formal_param_ptr->type_ID;

          // Use a binary search to try to find the index of the entry in the
          // array with the corresponding target_ID
          success = binary_search_dwarf_entry_array(target_ID, &target_index);
          if (success)
            {
              formal_param_ptr->type_ptr=&dwarf_entry_array[target_index];
            }
        }
    }
}

/*
Requires: dist_to_end indicates distance from e until end of dwarf_entry_array,
          e points to an element of dwarf_entry_array
Modifies: e->num_members, e->members
Returns:
Effects: Links the collection entry to its members, which are located
         adjacent to it in the dwarf_entry array, making sure not to
         accidentally segfault by indexing out of bounds
         (indicated by dist_to_end param
          which indicates distance until the end of the array)
*/
void link_collection_to_members(dwarf_entry* e, unsigned long dist_to_end)
{
  // 1. Figure out what kind of collection it is (struct, union, enumeration)
  // 2. Traverse through subsequent entries, checking if the type_ID
  //    is the appropriate member type for that collection
  // 3. Stop either when you hit a type_ID that is not a member OR
  //    when you are about to run out of array bounds
  // 4. Store the subsequent array entry (e + 1) as pointer to first member
  //    (as long as its not out of bounds) and store the number of entries
  //    you traversed as the number of members

  unsigned long member_count = 0;
  unsigned int currentlevel=e->level;
  dwarf_entry* cur_entry = e;
  collection_type* collection_ptr = (collection_type*)(e->entry_ptr);

  // If you are at the end of the array, you're screwed anyways
  if(dist_to_end == 0)
    return;

  switch (e->tag_name)
    {
      // enumerations expect DW_TAG_enumerator as members
    case DW_TAG_enumeration_type:
      cur_entry++; // Move to the next entry - safe since dist_to_end > 0 by this point
      while ((member_count < dist_to_end) // put this first! short-circuit eval.
             && cur_entry->level> currentlevel)
        {
	  if (cur_entry->tag_name==DW_TAG_enumerator&&(cur_entry->level==(currentlevel+1)))
	    member_count++;
          cur_entry++;
        }
      break;
      // structs and unions expect DW_TAG_member as members
    case DW_TAG_structure_type:
    case DW_TAG_union_type:
      cur_entry++; // Move to the next entry - safe since dist_to_end > 0 by this point
      while ((member_count < dist_to_end) // put this first! short-circuit eval.
             && cur_entry->level>currentlevel)
        {
	  if (cur_entry->tag_name==DW_TAG_member&&cur_entry->level==(currentlevel+1))
	    member_count++;
          cur_entry++;
        }
      break;
    default:
      return;
    }

  collection_ptr->num_members = member_count;
  collection_ptr->members = (dwarf_entry **)calloc(1,member_count*sizeof(dwarf_entry *));

  cur_entry = e;
  member_count=0;
  switch (e->tag_name)
    {
      // enumerations expect DW_TAG_enumerator as members
    case DW_TAG_enumeration_type:
      cur_entry++; // Move to the next entry - safe since dist_to_end > 0 by this point
      while ((member_count < dist_to_end) // put this first! short-circuit eval.
             && cur_entry->level> currentlevel)
        {
	  if (cur_entry->tag_name==DW_TAG_enumerator&&(cur_entry->level==(currentlevel+1)))
	    collection_ptr->members[member_count++]=cur_entry;
          cur_entry++;
        }
      break;
      // structs and unions expect DW_TAG_member as members
    case DW_TAG_structure_type:
    case DW_TAG_union_type:
      cur_entry++; // Move to the next entry - safe since dist_to_end > 0 by this point
      while ((member_count < dist_to_end) // put this first! short-circuit eval.
             && cur_entry->level>currentlevel)
        {
	  if (cur_entry->tag_name==DW_TAG_member&&cur_entry->level==(currentlevel+1))
	    collection_ptr->members[member_count++]=cur_entry;
          cur_entry++;
        }
      break;
    default:
      return;
    }
}

// Same as above except linking functions with formal parameters
void link_function_to_params(dwarf_entry* e, unsigned long dist_to_end)
{
  unsigned long param_count = 0;
  dwarf_entry* cur_entry = e;
  function* function_ptr = (function*)(e->entry_ptr);

  // If you are at the end of the array, you're screwed anyways
  if(dist_to_end == 0)
    return;

  cur_entry++; // Move to the next entry - safe since dist_to_end > 0 by this point
  // functions expect DW_TAG_formal_parameter as parameters
  while ((param_count < dist_to_end) // important that this is first! short-circuit eval.
         && cur_entry->tag_name == DW_TAG_formal_parameter)
    {
      param_count++;
      cur_entry++;
    }

  function_ptr->num_formal_params = param_count;
  function_ptr->params = (e + 1);
}

/*
Requires: dwarf_entry_array is initialized
Modifies: ((function*)cur_entry->entry_ptr)->filename for function entries
Returns:
Effects: Initialize the filename field of each function entry
         by linearly traversing dwarf_entry_array and noting that every compile_unit
         entry describes a file and all functions to the right of that entry
         (but to the left of the next entry) belong to that file
         e.g. [compile_unit foo.c][...][func1][...][func2][...][compile_unit bar.c][func3]
         func1 and func2 belong to foo.c and func3 belongs to bar.c
*/
void initialize_function_filenames()
{
  unsigned long idx;
  char* cur_file = 0;
  dwarf_entry* cur_entry = 0;

  for (idx = 0; idx < dwarf_entry_array_size; idx++)
    {
      cur_entry = &dwarf_entry_array[idx];

      if (tag_is_compile_unit(cur_entry->tag_name))
        cur_file = ((compile_unit*)cur_entry->entry_ptr)->filename;
      else if (tag_is_function(cur_entry->tag_name))
        ((function*)cur_entry->entry_ptr)->filename = cur_file;
    }
}

/*
Requires: dwarf_entry_array is initialized
Modifies: function and collection entries within dwarf_entry_array
Returns:
Effects: Links function and collections entries to their respective members
         e.g. functions need to have a list of their formal parameters
         while structs, unions, and enumeration types need to have lists of members
         THIS ALGORITHM EXPLOITS THE FACT THAT MEMBERS/PARAMETERS ARE LISTED
         RIGHT AFTER THE RESPECTIVE FUNCTION OR STRUCT
         e.g. [function][param1][param2][param3][something_else]
*/
void link_array_entries_to_members()
{
  unsigned long idx;
  dwarf_entry* cur_entry = 0;

  // Linearly traverse the array and pick off function or collections
  // (struct, union, enumeration) entries to link to members:
  for (idx = 0; idx < dwarf_entry_array_size; idx++)
    {
      cur_entry = &dwarf_entry_array[idx];
      if (tag_is_collection_type(cur_entry->tag_name))
        link_collection_to_members(cur_entry, dwarf_entry_array_size - idx - 1);
      else if (tag_is_function(cur_entry->tag_name))
        link_function_to_params(cur_entry, dwarf_entry_array_size - idx - 1);
    }
}

// Prints the contents of the entry depending on its type
void print_dwarf_entry(dwarf_entry* e)
{
  if (e == 0)
    {
      printf("ERROR! Pointer e is null in print_dwarf_entry\n");
      return;
    }

  printf("ID:0x%lx, TAG:%s\n", e->ID, get_TAG_name(e->tag_name));

  switch(e->tag_name)
    {
    case DW_TAG_subprogram:
      {
        function* function_ptr = (function*)(e->entry_ptr);
        printf("  Name: %s, Filename: %s, Return Type ID (addr): 0x%lx (%p), Num. params: %ld, 1st param addr: %p\n",
               function_ptr->name,
               function_ptr->filename,
               function_ptr->return_type_ID,
               function_ptr->return_type,
               function_ptr->num_formal_params,
               function_ptr->params);
        break;
      }
    case DW_TAG_formal_parameter:
      {
        formal_parameter* formal_param_ptr = (formal_parameter*)(e->entry_ptr);
        printf("  Name: %s, Type ID (addr): 0x%lx (%p), Location: %ld\n",
               formal_param_ptr->name,
               formal_param_ptr->type_ID,
               formal_param_ptr->type_ptr,
               formal_param_ptr->location);
        break;
      }
    case DW_TAG_member:
      {
        member* member_ptr = (member*)(e->entry_ptr);
        printf("  Name: %s, Type ID (addr): 0x%lx (%p), Data member location: %ld, Byte size: %ld, Bit offset: %ld, Bit size: %ld\n",
               member_ptr->name,
               member_ptr->type_ID,
               member_ptr->type_ptr,
               member_ptr->data_member_location,
               member_ptr->byte_size,
               member_ptr->bit_offset,
               member_ptr->bit_size);
        break;
      }
    case DW_TAG_enumerator:
      {
        enumerator* enumerator_ptr = (enumerator*)(e->entry_ptr);
        printf("  Name: %s, Const value: %ld\n",
               enumerator_ptr->name,
               enumerator_ptr->const_value);
        break;
      }

    case DW_TAG_structure_type:
    case DW_TAG_union_type:
    case DW_TAG_enumeration_type:
      {
        collection_type* collection_ptr = (collection_type*)(e->entry_ptr);
        printf("  Name: %s, Byte size: %ld, Num. members: %ld, 1st member addr: %p\n",
               collection_ptr->name,
               collection_ptr->byte_size,
               collection_ptr->num_members,
               collection_ptr->members);
        break;
      }

    case DW_TAG_base_type:
      {
        base_type* base_ptr = (base_type*)(e->entry_ptr);
        printf("  Byte size: %ld, Encoding: %ld ",
               base_ptr->byte_size,
               base_ptr->encoding);

        // More detailed encoding information
        switch (base_ptr->encoding)
          {
          case DW_ATE_void:             printf ("(void)"); break;
          case DW_ATE_address:		printf ("(machine address)"); break;
          case DW_ATE_boolean:		printf ("(boolean)"); break;
          case DW_ATE_complex_float:	printf ("(complex float)"); break;
          case DW_ATE_float:		printf ("(float)"); break;
          case DW_ATE_signed:		printf ("(signed)"); break;
          case DW_ATE_signed_char:	printf ("(signed char)"); break;
          case DW_ATE_unsigned:		printf ("(unsigned)"); break;
          case DW_ATE_unsigned_char:	printf ("(unsigned char)"); break;
            /* DWARF 2.1 value.  */
          case DW_ATE_imaginary_float:	printf ("(imaginary float)"); break;
          default:
            if (base_ptr->encoding >= DW_ATE_lo_user
                && base_ptr->encoding <= DW_ATE_hi_user)
              {
                printf ("(user defined type)");
              }
            else
              {
                printf ("(unknown type)");
              }
            break;
          }

        printf(", Bit size: %ld, Bit offset: %ld\n",
               base_ptr->bit_size,
               base_ptr->bit_offset);

        break;
      }
    case DW_TAG_const_type:
    case DW_TAG_pointer_type:
    case DW_TAG_array_type:
    case DW_TAG_volatile_type:
      {
        modifier_type* modifier_ptr = (modifier_type*)(e->entry_ptr);
        printf("  Target ID (addr): 0x%lx (%p)\n",
               modifier_ptr->target_ID,
               modifier_ptr->target_ptr);
        break;
      }

    case DW_TAG_compile_unit:
      {
        compile_unit* compile_ptr = (compile_unit*)(e->entry_ptr);
        printf("  Filename: %s, Compile dir: %s\n",
               compile_ptr->filename,
               compile_ptr->comp_dir);
      }

    case DW_TAG_subroutine_type:
      {
        function_type * func_type = (function_type *)(e->entry_ptr);
        printf("  Return type ID (addr): 0x%lx (%p)\n",
               func_type->return_type_ID, func_type->return_type);
      }

    default:
      return;
    }
}

/*
Requires:
Modifies: dwarf_entry_array (initializes and blanks all entries to zero)
Returns:
Effects: Initializes sets up dwarf_entry_array to hold num_entries components
*/
void initialize_dwarf_entry_array(unsigned long num_entries)
{
  // use calloc to blank everything upon initialization
  dwarf_entry_array = calloc(num_entries, sizeof *dwarf_entry_array);
}

/*
Requires: dwarf_entry_array is initialized
Modifies: dwarf_entry_array (free and set to 0)
Returns:
Effects: Destroys dwarf_entry_array and all entry_ptr fields of all entries
*/
void destroy_dwarf_entry_array()
{
  // Traverse the array and free the entry_ptr of all entries within array

  unsigned long i;
  for (i = 0; i < dwarf_entry_array_size; i++)
    {
      free(dwarf_entry_array[i].entry_ptr);
    }

  // Free the array itself
  free(dwarf_entry_array);
}

void print_dwarf_entry_array()
{
  unsigned long i;
  printf("--- BEGIN DWARF ENTRY ARRAY - size: %ld\n", dwarf_entry_array_size);
  for (i = 0; i < dwarf_entry_array_size; i++)
    {
      printf("array[%ld] (%p): ", i, dwarf_entry_array + i);
      print_dwarf_entry(&dwarf_entry_array[i]);
    }
  printf("--- END DWARF ENTRY ARRAY\n");
}

/*
Requires: e is initialized and has a e->tag_name
Modifies: e->entry_ptr (initializes and set to 0)
Returns:
Effects: Initialize the value of e->entry_ptr to the appropriate sub-type
         based on the value of tag_name
         If tag_name is 0, then don't do anything
*/
void initialize_dwarf_entry_ptr(dwarf_entry* e)
{
  if (e->tag_name)
    {
      if (tag_is_base_type(e->tag_name))
        {
          e->entry_ptr = calloc(1, sizeof(base_type));
        }
      else if (e->tag_name==DW_TAG_subrange_type) {
	e->entry_ptr=calloc(1,sizeof(array_bound));
      }
      else if (tag_is_modifier_type(e->tag_name))
        {
          e->entry_ptr = calloc(1, sizeof(modifier_type));
        }
      else if (tag_is_collection_type(e->tag_name))
        {
          e->entry_ptr = calloc(1, sizeof(collection_type));
        }
      else if (tag_is_member(e->tag_name))
        {
          e->entry_ptr = calloc(1, sizeof(member));
        }
      else if (tag_is_enumerator(e->tag_name))
        {
          e->entry_ptr = calloc(1, sizeof(enumerator));
        }
      else if (tag_is_function(e->tag_name))
        {
          e->entry_ptr = calloc(1, sizeof(function));
        }
      else if (tag_is_formal_parameter(e->tag_name))
        {
          e->entry_ptr = calloc(1, sizeof(formal_parameter));
        }
      else if (tag_is_compile_unit(e->tag_name))
        {
          e->entry_ptr = calloc(1, sizeof(compile_unit));
        }
      else if (tag_is_function_type(e->tag_name))
        {
          e->entry_ptr = calloc(1, sizeof(function_type));
        }
    }
}

// Now that dwarf_entry_array is initialized with values, we must link
// the entries together in a coherent manner
void finish_dwarf_entry_array_init(void)
{
  // These must be done in this order or else things will go screwy!!!
  link_array_entries_to_members();
  initialize_function_filenames();
  link_entries_to_type_entries();
}
