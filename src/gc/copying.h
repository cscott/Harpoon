#ifndef INCLUDED_COPYING_H
#define INCLUDED_COPYING_H

#ifdef WITH_PRECISE_C_BACKEND
void copying_add_to_root_set(jobject_unwrapped *obj);

void *copying_malloc (size_t size_in_bytes);

void copying_handle_nonroot(jobject_unwrapped *nonroot);
#else
void *copying_malloc (size_t size_in_bytes, void *saved_registers[]);
#endif

#endif
