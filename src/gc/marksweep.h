#ifndef INCLUDED_MARKSWEEP_H
#define INCLUDED_MARKSWEEP_H

#ifdef WITH_PRECISE_C_BACKEND
void marksweep_add_to_root_set(jobject_unwrapped *obj);

void marksweep_collect();

void marksweep_gc_init();

void *marksweep_malloc (size_t size_in_bytes);

void marksweep_handle_nonroot(jobject_unwrapped *nonroot);
#endif

#endif
