/* handy constants (mostly locations) defined by the runtime interface */

/* first method in the java program. */
extern void (*javamain)(void);
/* null-terminated ordered list of static initializers */
extern void *static_inits;

/* starts and ends of various segments */
extern void *gc_start, *gc_end;
extern void *static_objects_start, *static_objects_end;
extern void *string_constants_start, *string_constants_end;
extern void *fixup_start, *fixup_end;
extern void *code_start, *code_end;

/* reflection tables */
extern void *name2class_start, *name2class_end;
extern void *class2info_start, *class2info_end;
