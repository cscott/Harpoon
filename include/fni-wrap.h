/* These are the data structures and macros associated with wrapping and
 * unwrapping objects.  They've been moved from jni-private.h because they
 * need to be independently referenced from the precisec backend (as part
 * of struct FNI_Thread_State, defined in fni-threadstate.h). */

#ifndef INCLUDED_FNI_WRAP
#define INCLUDED_FNI_WRAP

/* --------------- wrapping and unwrapping objects. ------------ */
/* an unwrapped jobject is a struct oobj *...*/
typedef struct oobj * jobject_unwrapped;
/* a wrapped object is a struct _jobject *...*/
struct _jobject {
  struct oobj * obj;
};
/* a global ref has additional 'next' and 'prev' fields */
typedef struct _jobject_globalref {
  struct _jobject jobject;
  struct _jobject_globalref * prev;
  struct _jobject_globalref * next;
} *jobject_globalref;

/* define handy (un)wrapper macros */
#define FNI_WRAP(x) (FNI_NewLocalRef(env, x))
#define FNI_UNWRAP(_x) ({jobject x=_x; (x==NULL)?NULL:x->obj; })
#define FNI_UNWRAP_MASKED(_x) ((struct oobj *)PTRMASK(FNI_UNWRAP(_x)))

/* --------------------------------------------------------------*/

#endif /* INCLUDED_FNI_WRAP */
