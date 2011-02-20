#ifndef x2

// crazy preprocessor tricks necessary to append *expanded version of*
// v to the given token x
# define x1(x,v) x2(x,v)
# define x2(x,v) x ## _ ## v

# if defined(ARRAY)
#  define A(x) x1(x,Array)
#  define OBJ_OR_ARRAY(x,y) (y)
# else
#  define A(x) x
#  define OBJ_OR_ARRAY(x,y) (x)
# endif

# if !defined(NO_VALUETYPE)
#  define T(x) x1(x,VALUENAME)
#  define TA(x) T(A(x))

# define PTRBITS (SIZEOF_VOID_P*8)
# define FIELDBASE(obj) \
	OBJ_OR_ARRAY(obj->field_start,((struct aarray *)obj)->element_start)
# endif /* !NO_VALUETYPE */

#else /* clean up after ourself */

# undef TA
# undef A
# undef T
# undef x1
# undef x2
# undef OBJ_OR_ARRAY
# undef BASE
# undef FIELDBASE
# undef PTRBITS

#endif
