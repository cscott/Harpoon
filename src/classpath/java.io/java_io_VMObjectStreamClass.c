/* java_io_VMObjectStreamClass.c, created by wbeebee
   Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
   Licensed under the terms of the GNU GPL; see COPYING for details. */

#include "java_io_VMObjectStreamClass.h"

/*
 * Returns true if CLAZZ has a static class initializer
 * (a.k.a. <clinit>).
 *
 * Class:     java_io_VMObjectStreamClass
 * Method:    hasClassInitializer
 * Signature: (Ljava/lang/Class;)Z
 */
JNIEXPORT jboolean JNICALL Java_java_io_VMObjectStreamClass_hasClassInitializer
    (JNIEnv *env, jclass VMObjectStreamClaz, jclass claz) {
    jmethodID methodID = 
	(*env)->GetStaticMethodID(env, claz, "<clinit>", "()V");
    if (methodID==NULL) {
	(*env)->ExceptionClear(env);
	return JNI_FALSE;
    } else {
	return JNI_TRUE;
    }
}
