#ifndef INCLUDED_COPYING_H
#define INCLUDED_COPYING_H

/* copying.h contains function declarations for functions in
   copying.c that are used elsewhere */

#ifdef WITH_PRECISE_C_BACKEND

void copying_add_to_root_set(jobject_unwrapped *obj);

void copying_collect();

jlong copying_free_memory();

void copying_gc_init();

jlong copying_get_heap_size();

void copying_handle_reference(jobject_unwrapped *ref);

void *copying_malloc (size_t size_in_bytes);

void copying_register_inflated_obj(jobject_unwrapped obj);

#else

void copying_collect(void *saved_registers[], int expand_amt);

void *copying_malloc (size_t size_in_bytes, void *saved_registers[]);

#endif

#endif
