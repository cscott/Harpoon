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
  struct _jobject * next;
};
/* define handy (un)wrapper macros */
#define FNI_WRAP(x) (FNI_NewLocalRef(env, x))
#define FNI_UNWRAP(_x) ({jobject x=_x; (x==NULL)?NULL:x->obj; })

/* --------------------------------------------------------------*/

#endif /* INCLUDED_FNI_WRAP */
