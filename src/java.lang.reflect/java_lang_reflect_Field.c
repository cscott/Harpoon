#include <assert.h>
#include <jni.h>
#include "jni-private.h"
#include "java_lang_reflect_Field.h"
#include "java_lang_reflect_Modifier.h"
#include "reflect-util.h"

/*
 * Class:     java_lang_reflect_Field
 * Method:    getModifiers
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_java_lang_reflect_Field_getModifiers
  (JNIEnv *env, jobject _this) {
    return FNI_GetFieldInfo(_this)->modifiers;
}

/*
 * Class:     java_lang_reflect_Field
 * Method:    get
 * Signature: (Ljava/lang/Object;)Ljava/lang/Object;
 */
JNIEXPORT jobject JNICALL Java_java_lang_reflect_Field_get
  (JNIEnv *env, jobject fieldobj, jobject receiverobj) {
  struct FNI_field2info *field; /* field information */
  jclass fieldclazz; /* declaring class of field */
  jvalue value; /* (primitive) value of the field */
  char desc; /* first character of the field type descriptor */

  assert(fieldobj!=NULL);
  field = FNI_GetFieldInfo(fieldobj);
  assert(field!=NULL);
  fieldclazz = FNI_WRAP(field->declaring_class_object);

  if (field->modifiers & java_lang_reflect_Modifier_STATIC) {/* static field */
    /* fetch field value */
    switch(desc=field->fieldID->desc[0]) {
#define STATICCASE(typename,shortname,longname,descchar,jvalfield)\
    case descchar:\
      value.jvalfield =\
	(*env)->GetStatic##shortname##Field(env, fieldclazz, field->fieldID);\
      break;
    FORPRIMITIVETYPESX(STATICCASE)
#undef STATICCASE
    default: /* object type */
      assert(field->fieldID->desc[0]=='L' || field->fieldID->desc[0]=='[');
      return (*env)->GetStaticObjectField(env, fieldclazz, field->fieldID);
    }
    return REFLECT_wrapPrimitive(env, value, desc);
  } else { /* non-static field */
    /* do checks first */
    if (receiverobj==NULL) {
      jclass excls = (*env)->FindClass(env, "java/lang/NullPointerException");
      (*env)->ThrowNew(env, excls, "null receiver for non-static field");
      return NULL;
    }
    if (!(*env)->IsInstanceOf(env, receiverobj, fieldclazz)) {
      jclass excls=(*env)->FindClass(env,"java/lang/IllegalArgumentException");
      (*env)->ThrowNew(env, excls,
		       "receiver not instance of field declaring class");
      return NULL;
    }
    /* fetch field value */
    /* (note similarities with the way we did things above */
    switch(desc=field->fieldID->desc[0]) {
#define NONSTATICCASE(typename,shortname,longname,descchar,jvalfield)\
    case descchar:\
      value.jvalfield =\
	(*env)->Get##shortname##Field(env, receiverobj, field->fieldID);\
      break;
    FORPRIMITIVETYPESX(NONSTATICCASE)
#undef NONSTATICCASE
    default: /* object type */
      assert(field->fieldID->desc[0]=='L' || field->fieldID->desc[0]=='[');
      return (*env)->GetObjectField(env, receiverobj, field->fieldID);
    }
    return REFLECT_wrapPrimitive(env, value, desc);
  }
}

/*
 * Class:     java_lang_reflect_Field
 * Method:    set
 * Signature: (Ljava/lang/Object;Ljava/lang/Object;)V
 */
JNIEXPORT void JNICALL Java_java_lang_reflect_Field_set
  (JNIEnv *env, jobject fieldobj, jobject receiverobj, jobject value) {
  struct FNI_field2info *field; /* field information */
  jclass fieldclazz; /* declaring class of field */
  char desc; /* first character of field descriptor */
  jvalue primval; /* this will hold the (primitive) value of the field */

  assert(fieldobj!=NULL);
  field = FNI_GetFieldInfo(fieldobj);
  assert(field!=NULL);
  fieldclazz = FNI_WRAP(field->declaring_class_object);
  assert(!(*env)->ExceptionOccurred(env));
  
  /* according to spec, we do receiver checks first */
  if (!(field->modifiers & java_lang_reflect_Modifier_STATIC)) {
    /* check receiver */
    if (receiverobj==NULL) {
      jclass excls = (*env)->FindClass(env, "java/lang/NullPointerException");
      (*env)->ThrowNew(env, excls, "null receiver for non-static field");
      return;
    }
    if (!(*env)->IsInstanceOf(env, receiverobj, fieldclazz)) {
      jclass excls=(*env)->FindClass(env,"java/lang/IllegalArgumentException");
      (*env)->ThrowNew(env, excls,
		       "receiver not instance of field declaring class");
      return;
    }
  }
  /* if underlying field is final, throws an IllegalAccessException */
  if (field->modifiers & java_lang_reflect_Modifier_FINAL) { /* final field */
      jclass excls=(*env)->FindClass(env,"java/lang/IllegalAccessException");
      (*env)->ThrowNew(env, excls, "attempt to set a final field");
      return;
  }
  /* if field is primitive, attempt to unwrap */
  desc = field->fieldID->desc[0];
  if (desc!='L' && desc!='[')
    primval = REFLECT_unwrapPrimitive(env, value, desc);
  if ((*env)->ExceptionOccurred(env)) return; /* abort! */
  /* okay, do it! */
  if (field->modifiers & java_lang_reflect_Modifier_STATIC) {/* static field */
    /* set field value */
    switch(desc) {
#define STATICCASE(typename,shortname,longname,descchar,jvalfield)\
    case descchar:\
      (*env)->SetStatic##shortname##Field(env, fieldclazz, field->fieldID,\
				          primval.jvalfield);\
      return;
    FORPRIMITIVETYPESX(STATICCASE)
#undef STATICCASE
    default:
      /* object type */
      (*env)->SetStaticObjectField(env, fieldclazz, field->fieldID, value);
      return;
    }
  } else { /* non-static field */
    /* set field value */
    switch(desc) {
#define NONSTATICCASE(typename,shortname,longname,descchar,jvalfield)\
    case descchar:\
      (*env)->Set##shortname##Field(env, receiverobj, field->fieldID,\
			            primval.jvalfield);\
      return;
    FORPRIMITIVETYPESX(NONSTATICCASE)
#undef NONSTATICCASE
    default:
      /* object type */
      (*env)->SetObjectField(env, receiverobj, field->fieldID, value);
      return;
    }
  }
}

#if !defined(WITHOUT_HACKED_REFLECTION) /* this is our hacked implementation */
/*
 * Class:     java_lang_reflect_Field
 * Method:    getDeclaringClass
 * Signature: ()Ljava/lang/Class;
 */
JNIEXPORT jclass JNICALL Java_java_lang_reflect_Field_getDeclaringClass
  (JNIEnv *env, jobject _this) {
    return FNI_WRAP(FNI_GetFieldInfo(_this)->declaring_class_object);
}

/*
 * Class:     java_lang_reflect_Field
 * Method:    getName
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_java_lang_reflect_Field_getName
  (JNIEnv *env, jobject _this) {
    return (*env)->NewStringUTF(env, FNI_GetFieldInfo(_this)->fieldID->name);
}
/*
 * Class:     java_lang_reflect_Field
 * Method:    getType
 * Signature: ()Ljava/lang/Class;
 */
JNIEXPORT jclass JNICALL Java_java_lang_reflect_Field_getType
  (JNIEnv *env, jobject _this) {
    return REFLECT_parseDescriptor
	(env, FNI_GetFieldInfo(_this)->fieldID->desc);
}

#else /* this is the original Sun header */

#if 0
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
#endif /* 0 */

#endif /* WITHOUT_HACKED_REFLECTION */
