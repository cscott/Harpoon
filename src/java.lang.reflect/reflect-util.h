/* utility methods for reflection */
#ifndef INCLUDED_REFLECT_UTIL_H
#define INCLUDED_REFLECT_UTIL_H

/* Return the class object corresponding to the first component of the
 * given descriptor. */
jclass REFLECT_parseDescriptor(JNIEnv *env, const char *desc);
/* Advance the given descriptor to the next component; returns NULL
 * if there are no more components. */
char *REFLECT_advanceDescriptor(char *desc);
/* Return an object-wrapped version of the given primitive value.
 * The value ought to be in the proper field of the jvalue 'unwrapped'
 * and the type of the primitive (expressed as the single-character
 * type signature) in the 'type' argument. */
jobject REFLECT_wrapPrimitive(JNIEnv *env, jvalue unwrapped, char type);
/* Unwrap the given object to the type requested by 'desired sig'.
 * May throw an 'IllegalArgumentException' if this cannot be done via
 * a valid widening conversion. */
jvalue REFLECT_unwrapPrimitive(JNIEnv *env, jobject wrapped, char desiredsig);

#endif /* INCLUDED_REFLECT_UTIL_H */
