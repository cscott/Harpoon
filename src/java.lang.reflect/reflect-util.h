/* utility methods for reflection */
#ifndef INCLUDED_REFLECT_UTIL_H
#define INCLUDED_REFLECT_UTIL_H

/* return the class object corresponding to the first component of the
 * given descriptor. */
jclass REFLECT_parseDescriptor(JNIEnv *env, const char *desc);
/* advance the given descriptor to the next component; returns NULL
 * if there are no more components. */
char *REFLECT_advanceDescriptor(char *desc);

#endif /* INCLUDED_REFLECT_UTIL_H */
