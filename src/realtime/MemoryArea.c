/* MemoryArea.c, created by wbeebee
   Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
   Licensed under the terms of the GNU GPL; see COPYING for details. */

#include "MemoryArea.h"

/*
 * Class:     MemoryArea
 * Method:    enterMemBlock
 * Signature: (Ljavax/realtime/RealtimeThread;)V
 */
JNIEXPORT void JNICALL Java_javax_realtime_MemoryArea_enterMemBlock
(JNIEnv* env, jobject memoryArea, jobject realtimeThread, jobject memAreaStack) {
  struct MemBlock* memBlock;
#ifdef RTJ_DEBUG
  checkException();
  printf("MemoryArea.enterMemBlock(0x%08x, 0x%08x, 0x%08x, 0x%08x)\n", 
	 env, memoryArea, realtimeThread, memAreaStack);
#endif
  getInflatedObject(env, memAreaStack)->temp = 
    (memBlock = MemBlock_currentMemBlock());
  MemBlock_setCurrentMemBlock(env, realtimeThread,
			      MemBlock_new(env, memoryArea, 
					   realtimeThread, memBlock));
}

/*
 * Class:     MemoryArea
 * Method:    exitMemBlock
 * Signature: (Ljavax/realtime/RealtimeThread;)V
 */
JNIEXPORT void JNICALL Java_javax_realtime_MemoryArea_exitMemBlock
(JNIEnv* env, jobject memoryArea, jobject realtimeThread) {
  struct MemBlock* memBlock;
#ifdef RTJ_DEBUG
  printf("MemoryArea.exitMemBlock(0x%08x, 0x%08x, 0x%08x)\n",
	 env, memoryArea, realtimeThread);
#endif
  memBlock = MemBlock_currentMemBlock();
  MemBlock_setCurrentMemBlock(env, realtimeThread, 
			      MemBlock_prevMemBlock(memBlock));
  MemBlock_free(memBlock);
}

/* RTJ version of the FNI_NewObjectArray -> only difference is that it calls 
   RTJ_malloc. */

jarray RTJ_NewObjectArray(JNIEnv *env, jsize length,
			  jclass elementClass, jobject initialElement) {
  struct FNI_classinfo *info;
  jclass arrayclazz;
  jobject result;
#ifdef RTJ_DEBUG
  checkException();
  printf("RTJ_NewObjectArray(0x%08x, %d, 0x%08x, 0x%08x)\n",
	 env, length, elementClass, initialElement);
#endif
  assert(FNI_NO_EXCEPTIONS(env) && length>=0 && elementClass!=NULL);
  assert(Java_java_lang_Class_isPrimitive(env, elementClass)==JNI_FALSE);
  info = FNI_GetClassInfo(elementClass);
  {
    char arraydesc[strlen(info->name)+4];
    arraydesc[0]='[';
    if (info->name[0]=='[') strcpy(arraydesc+1, info->name);
    else {
      arraydesc[1]='L';
      strcpy(arraydesc+2, info->name);
      arraydesc[strlen(arraydesc)+1]='\0';
      arraydesc[strlen(arraydesc)]=';';
    }
    arrayclazz = FNI_FindClass(env, arraydesc);
    if (arrayclazz==NULL) return NULL; /* bail on exception */
    info = FNI_GetClassInfo(arrayclazz);
    FNI_DeleteLocalRef(env, arrayclazz);
  }
  result = FNI_Alloc(env, info, info->claz, RTJ_jmalloc,
		     sizeof(struct aarray) + sizeof(ptroff_t)*length);
  if (result==NULL) return NULL; /* bail on error */
  ((struct aarray *)FNI_UNWRAP(result))->length = length;
  if (initialElement != NULL) {
    jsize i;
    for (i=0; i<length; i++)
      (*env)->SetObjectArrayElement(env, (jobjectArray) result, i,
				    initialElement);
  }
  return (jobjectArray) result;
}

/*
 * Class:     javax_realtime_MemoryArea
 * Method:    newArray
 * Signature: (Ljavax/realtime/RealtimeThread;Ljava/lang/Class;ILjava/lang/Object;)Ljava/lang/Object;
 */
JNIEXPORT jobject JNICALL Java_javax_realtime_MemoryArea_newArray__Ljavax_realtime_RealtimeThread_2Ljava_lang_Class_2ILjava_lang_Object_2
(JNIEnv *env, jobject memoryArea, jobject realtimeThread, 
 jclass componentClass, jint length, jobject memBlockObj) {
  struct MemBlock* oldMemBlock;
  jobject result;
  jarray (*oldNewObjectArray) (JNIEnv *env, jsize length,
			       jclass elementClass, jobject initialElement);
#ifdef RTJ_DEBUG
  checkException();
  printf("MemoryArea.newArray(0x%08x, 0x%08x, 0x%08x, 0x%08x, %d, 0x%08x)\n",
	 env, memoryArea, realtimeThread, componentClass, length, memBlockObj);
#endif  
  oldMemBlock = MemBlock_currentMemBlock();
  oldNewObjectArray = (*env)->NewObjectArray;
  MemBlock_setCurrentMemBlock(env, realtimeThread, 
			      getInflatedObject(env, memBlockObj)->temp);
  ((struct JNINativeInterface*)(*env))->NewObjectArray = RTJ_NewObjectArray;
  result = Java_java_lang_reflect_Array_newArray(env, NULL, componentClass, length);
  ((struct JNINativeInterface*)(*env))->NewObjectArray = oldNewObjectArray;
  MemBlock_setCurrentMemBlock(env, realtimeThread, oldMemBlock);
  return result;
}

/*
 * Class:     javax_realtime_MemoryArea
 * Method:    newArray
 * Signature: (Ljavax/realtime/RealtimeThread;Ljava/lang/Class;[ILjava/lang/Object;)Ljava/lang/Object;
 */
JNIEXPORT jobject JNICALL Java_javax_realtime_MemoryArea_newArray__Ljavax_realtime_RealtimeThread_2Ljava_lang_Class_2_3ILjava_lang_Object_2
(JNIEnv *env, jobject memoryArea, jobject realtimeThread, 
 jclass componentClass, jintArray dims, jobject memBlockObj) {
  struct MemBlock* oldMemBlock;
  jobject result;
  jarray (*oldNewObjectArray) (JNIEnv *env, jsize length,
			       jclass elementClass, jobject initialElement);
#ifdef RTJ_DEBUG
  checkException();
  printf("MemoryArea.newArray(0x%08x, 0x%08x, 0x%08x, 0x%08x, 0x%08x, 0x%08x)\n",
	 env, memoryArea, realtimeThread, componentClass, dims, memBlockObj);
#endif
  oldNewObjectArray = (*env)->NewObjectArray;
  oldMemBlock = MemBlock_currentMemBlock();
  MemBlock_setCurrentMemBlock(env, realtimeThread,
			      getInflatedObject(env, memBlockObj)->temp);
  ((struct JNINativeInterface*)(*env))->NewObjectArray = RTJ_NewObjectArray;
  result = Java_java_lang_reflect_Array_multiNewArray(env, NULL, componentClass, dims);
  ((struct JNINativeInterface*)(*env))->NewObjectArray = oldNewObjectArray;
  MemBlock_setCurrentMemBlock(env, realtimeThread, oldMemBlock);
  return result;
}

/*
 * Class:     javax_realtime_MemoryArea
 * Method:    newInstance
 * Signature: (Ljavax/realtime/RealtimeThread;Ljava/lang/reflect/Constructor;[Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
 */
JNIEXPORT jobject JNICALL Java_javax_realtime_MemoryArea_newInstance
(JNIEnv *env, jobject memoryArea, jobject realtimeThread, 
 jobject constructor, jobjectArray parameters, jobject memBlockObj) {
  struct MemBlock* oldMemBlock; 
  struct FNI_method2info *method; /* method information */
  jclass methodclazz; /* declaring class of method */
  jobject result;
#ifdef RTJ_DEBUG
  checkException();
  printf("MemoryArea.newInstance(0x%08x, 0x%08x, 0x%08x, 0x%08x, 0x%08x, 0x%08x)\n",
	 env, memoryArea, realtimeThread, constructor, parameters, memBlockObj);
#endif
  oldMemBlock = MemBlock_currentMemBlock();
  
#ifdef RTJ_DEBUG  
  assert(constructor != NULL);
  checkException();
#endif
  method = FNI_GetMethodInfo(constructor);
#ifdef RTJ_DEBUG
  assert(method != NULL);
  checkException();
#endif
  methodclazz = FNI_WRAP(method->declaring_class_object);
  
  /* check that declaring class is not abstract. */
  if (FNI_GetClassInfo(methodclazz)->modifiers &
      java_lang_reflect_Modifier_ABSTRACT) {
    jclass excls=(*env)->FindClass(env, "java/lang/IllegalAccessException");
    (*env)->ThrowNew(env, excls,
		     "attempted instantiation of an abstract class");
    return NULL;
  }
  /* create zero-filled-object instance. */
  MemBlock_setCurrentMemBlock(env, realtimeThread,
			      getInflatedObject(env, memBlockObj)->temp);
  result = FNI_AllocObject_using(env, methodclazz, RTJ_jmalloc);
  MemBlock_setCurrentMemBlock(env, realtimeThread, oldMemBlock);
  if ((*env)->ExceptionOccurred(env)) return NULL; /* bail */

  /* okay, now invoke constructor */
  Java_java_lang_reflect_Method_invoke(env, constructor, result, parameters);
  if ((*env)->ExceptionOccurred(env)) return NULL; /* bail */
  return result;
}

/*
 * Class:     javax_realtime_MemoryArea
 * Method:    throwIllegalAssignmentError
 * Signature: (Ljava/lang/Object;Ljavax/realtime/MemoryArea;)V
 */
JNIEXPORT void JNICALL Java_javax_realtime_MemoryArea_throwIllegalAssignmentError
(JNIEnv* env, jobject fromMA, jobject toObj, jobject toMA) {
  jclass excls;
#ifdef RTJ_DEBUG
  printf("An Illegal assignment was detected.  Throwing an IllegalAssignmentError.\n");
  printf("From a %s to a %s\n", 
	 FNI_GetClassInfo((jclass)(FNI_UNWRAP(fromMA)->claz))->name,
	 FNI_GetClassInfo((jclass)(FNI_UNWRAP(toMA)->claz))->name);
  printf("illegal access to ");
#ifdef RTJ_DEBUG_REF
  printPointerInfo(toObj, 1);
#else
  printf("location 0x%08x of type %s\n", toObj, 
	 FNI_GetClassInfo((jclass)(FNI_UNWRAP(toObj)->claz))->name);
#endif
#endif
  excls = (*env)->FindClass(env, "javax/realtime/IllegalAssignmentError");
  (*env)->ThrowNew(env, excls, 
		   "illegal assignment detected: use RTJ_DEBUG and RTJ_DEBUG_REF to debug");
#ifdef RTJ_DEBUG
  checkException();
#endif
}


