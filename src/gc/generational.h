#ifndef INCLUDED_GENERATIONAL_H
#define INCLUDED_GENERATIONAL_H

void generational_gc_init();

void generational_handle_reference(jobject_unwrapped *ref);

void *generational_malloc (size_t size_in_bytes);

void generational_register_inflated_obj(jobject_unwrapped obj);

#endif
