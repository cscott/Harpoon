/* stubs to get our files to link */

/* missing classes */
void *Primitive_byte;
void *Primitive_char;
void *Primitive_int;
void *Primitive_long;

/* native methods */
/* in java.lang.Class */
void *Java_java_lang_Class_getPrimitiveClass__Ljava_lang_String_2;
void *Java_java_lang_Class_getName__;
/* in java.lang.Float */
void *Java_java_lang_Float_floatToIntBits__F;
/* in java.lang.Object */
void *Java_java_lang_Object_getClass__;
void *Java_java_lang_Object_hashCode__;
/* in java.lang.String */
void *Java_java_lang_String_intern__;
/* in java.lang.System */
void *Java_java_lang_System_arraycopy__Ljava_lang_Object_2ILjava_lang_Object_2II;
void *Java_java_lang_System_initProperties__Ljava_util_Properties_2;
void *Java_java_lang_System_setIn0__Ljava_io_InputStream_2;
void *Java_java_lang_System_setOut0__Ljava_io_PrintStream_2;
void *Java_java_lang_System_setErr0__Ljava_io_PrintStream_2;
void *Java_java_lang_System_currentTimeMillis__;
/* in java.lang.Throwable */
void *Java_java_lang_Throwable_fillInStackTrace__;

/* errors in class hierarchy */
void *Class__3Ljava_lang_Object_2;
void *Java_java_io_FileDescriptor_in;
void *Java_java_io_FileDescriptor_out;
void *Java_java_io_FileDescriptor_err;
void *Java_java_io_PrintStream_newLine__;

/* errors in backend */
asm(".global L927\nL927:");
asm(".global L930\nL930:");
asm(".global L1157\nL1157:");
asm(".global L1160\nL1160:");
asm(".global L3354\nL3354:");
asm(".global L4352\nL4352:");
asm(".global L4364\nL4364:");
asm(".global L6391\nL6391:");

/* errors in register allocator */
void *Class_java_lang_FloatingDecimal;
void *Java_java_lang_FloatingDecimal__0003cclinit_0003e__;
void *Java_java_lang_FloatingDecimal__0003cinit_0003e__F;
void *Java_java_lang_Integer__0003cclinit_0003e__;
void *Java_java_lang_Integer_toHexString__I;

/* runtime symbols */
void *lookup;
asm(".global stdexit\nstdexit:");
