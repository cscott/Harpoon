#ifndef INCLUDED_GC_DATA_H
#define INCLUDED_GC_DATA_H

/* gc-data.h contains declarations to be exported from gc-data.c
   to other files in the src/gc package */

/* effects: saves away the struct FNI_Thread_State 
            pointer of the main thread */
void gc_data_init();

#endif
