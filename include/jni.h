/* Java Native Interface header file.  C. Scott Ananian. */
/* Implemented from the JNI spec, v 1.1 */

#ifndef INCLUDED_JNI_H
#define INCLUDED_JNI_H

#include <stdio.h> /* for NULL */

#include <jni-types.h>
#include <jni-func.h>
#include <jni-link.h>

/* constants: */

/* the following definition "is provided for convenience" */
#define JNI_FALSE 0
#define JNI_TRUE  1

/* primitive array release modes */
#define JNI_COMMIT 1
#define JNI_ABORT  2

/* --- constants below here added in JNI v1.2 --- */

/* version constants */
#define JNI_VERSION_1_1 0x00010001
#define JNI_VERSION_1_2 0x00010002

/* Error codes */
#define JNI_EDETACHED    (-2)              /* thread detached from the VM */
#define JNI_EVERSION     (-3)              /* JNI version error */

/*     (there was no JNI v1.3)                    */

/* --- constants below here added in JNI v1.4 --- */

/* version constants */
#define JNI_VERSION_1_4 0x00010004

#endif /* INCLUDED_JNI_H */
