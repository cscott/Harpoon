#include <jni.h>
/* Header for class java_lang_reflect_Field */

#ifndef _Included_java_lang_reflect_Field
#define _Included_java_lang_reflect_Field
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     java_lang_reflect_Field
 * Method:    getModifiers
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_java_lang_reflect_Field_getModifiers
  (JNIEnv *, jobject);

/*
 * Class:     java_lang_reflect_Field
 * Method:    get
 * Signature: (Ljava/lang/Object;)Ljava/lang/Object;
 */
JNIEXPORT jobject JNICALL Java_java_lang_reflect_Field_get
  (JNIEnv *, jobject, jobject);

/*
 * Class:     java_lang_reflect_Field
 * Method:    set
 * Signature: (Ljava/lang/Object;Ljava/lang/Object;)V
 */
JNIEXPORT void JNICALL Java_java_lang_reflect_Field_set
  (JNIEnv *, jobject, jobject, jobject);

#if !defined(WITHOUT_HACKED_REFLECTION) /* this is our hacked implementation */
/*
 * Class:     java_lang_reflect_Field
 * Method:    getDeclaringClass
 * Signature: ()Ljava/lang/Class;
 */
JNIEXPORT jclass JNICALL Java_java_lang_reflect_Field_getDeclaringClass
  (JNIEnv *, jobject);

/*
 * Class:     java_lang_reflect_Field
 * Method:    getName
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_java_lang_reflect_Field_getName
  (JNIEnv *, jobject);
/*
 * Class:     java_lang_reflect_Field
 * Method:    getType
 * Signature: ()Ljava/lang/Class;
 */
JNIEXPORT jclass JNICALL Java_java_lang_reflect_Field_getType
  (JNIEnv *, jobject);

#else /* this is the original Sun header */

/*
 * Class:     java_lang_reflect_Field
 * Method:    getBoolean
 * Signature: (Ljava/lang/Object;)Z
 */
JNIEXPORT jboolean JNICALL Java_java_lang_reflect_Field_getBoolean
  (JNIEnv *, jobject, jobject);

/*
 * Class:     java_lang_reflect_Field
 * Method:    getByte
 * Signature: (Ljava/lang/Object;)B
 */
JNIEXPORT jbyte JNICALL Java_java_lang_reflect_Field_getByte
  (JNIEnv *, jobject, jobject);

/*
 * Class:     java_lang_reflect_Field
 * Method:    getChar
 * Signature: (Ljava/lang/Object;)C
 */
JNIEXPORT jchar JNICALL Java_java_lang_reflect_Field_getChar
  (JNIEnv *, jobject, jobject);

/*
 * Class:     java_lang_reflect_Field
 * Method:    getShort
 * Signature: (Ljava/lang/Object;)S
 */
JNIEXPORT jshort JNICALL Java_java_lang_reflect_Field_getShort
  (JNIEnv *, jobject, jobject);

/*
 * Class:     java_lang_reflect_Field
 * Method:    getInt
 * Signature: (Ljava/lang/Object;)I
 */
JNIEXPORT jint JNICALL Java_java_lang_reflect_Field_getInt
  (JNIEnv *, jobject, jobject);

/*
 * Class:     java_lang_reflect_Field
 * Method:    getLong
 * Signature: (Ljava/lang/Object;)J
 */
JNIEXPORT jlong JNICALL Java_java_lang_reflect_Field_getLong
  (JNIEnv *, jobject, jobject);

/*
 * Class:     java_lang_reflect_Field
 * Method:    getFloat
 * Signature: (Ljava/lang/Object;)F
 */
JNIEXPORT jfloat JNICALL Java_java_lang_reflect_Field_getFloat
  (JNIEnv *, jobject, jobject);

/*
 * Class:     java_lang_reflect_Field
 * Method:    getDouble
 * Signature: (Ljava/lang/Object;)D
 */
JNIEXPORT jdouble JNICALL Java_java_lang_reflect_Field_getDouble
  (JNIEnv *, jobject, jobject);

/*
 * Class:     java_lang_reflect_Field
 * Method:    setBoolean
 * Signature: (Ljava/lang/Object;Z)V
 */
JNIEXPORT void JNICALL Java_java_lang_reflect_Field_setBoolean
  (JNIEnv *, jobject, jobject, jboolean);

/*
 * Class:     java_lang_reflect_Field
 * Method:    setByte
 * Signature: (Ljava/lang/Object;B)V
 */
JNIEXPORT void JNICALL Java_java_lang_reflect_Field_setByte
  (JNIEnv *, jobject, jobject, jbyte);

/*
 * Class:     java_lang_reflect_Field
 * Method:    setChar
 * Signature: (Ljava/lang/Object;C)V
 */
JNIEXPORT void JNICALL Java_java_lang_reflect_Field_setChar
  (JNIEnv *, jobject, jobject, jchar);

/*
 * Class:     java_lang_reflect_Field
 * Method:    setShort
 * Signature: (Ljava/lang/Object;S)V
 */
JNIEXPORT void JNICALL Java_java_lang_reflect_Field_setShort
  (JNIEnv *, jobject, jobject, jshort);

/*
 * Class:     java_lang_reflect_Field
 * Method:    setInt
 * Signature: (Ljava/lang/Object;I)V
 */
JNIEXPORT void JNICALL Java_java_lang_reflect_Field_setInt
  (JNIEnv *, jobject, jobject, jint);

/*
 * Class:     java_lang_reflect_Field
 * Method:    setLong
 * Signature: (Ljava/lang/Object;J)V
 */
JNIEXPORT void JNICALL Java_java_lang_reflect_Field_setLong
  (JNIEnv *, jobject, jobject, jlong);

/*
 * Class:     java_lang_reflect_Field
 * Method:    setFloat
 * Signature: (Ljava/lang/Object;F)V
 */
JNIEXPORT void JNICALL Java_java_lang_reflect_Field_setFloat
  (JNIEnv *, jobject, jobject, jfloat);

/*
 * Class:     java_lang_reflect_Field
 * Method:    setDouble
 * Signature: (Ljava/lang/Object;D)V
 */
JNIEXPORT void JNICALL Java_java_lang_reflect_Field_setDouble
  (JNIEnv *, jobject, jobject, jdouble);
#endif /* WITHOUT_HACKED_REFLECTION */

#ifdef __cplusplus
}
#endif
#endif
