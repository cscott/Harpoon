/**
 * Header file containing definitions of the run-time datatypes.
 * Much of it is based on Scott's original native.h, updated to reflect
 * the current class layout.  
 *
 * $Id: native.h,v 1.1 1999-09-08 15:42:23 cananian Exp $ 
 */

union  _field;
struct _claz;

/* Object information. */
typedef struct _oobj {
    int     hashcode;    // initialized on allocation, if neccessary.
    claz *  clazptr;     // pointer actually points here.
    field * objectdata;  // field info goes here:
    // for an array, first word of objectdata is always length.
} oobj;

/* Field info. */
typedef union _field {
    oobj *   objectref;
    int     i;          // also used for char, boolean, etc.
    float   f;
} field;

typedef field (*method_t)(); 

/* Static class data */
typedef struct _claz { 
    // Array of methods inherited from interfaces implemented by the class
    method_t         iMethods[MAX_INTERFZ_METHODS];
    // Null-terminated list of interfaces implemented by the class
    struct claz **   iListPtr;
    // If an array class, pointer to the component-type's claz structure.
    // Otherwise this field is NULL. 
    struct claz *    componentType;
    // Display information for this class
    struct claz      display[MAX_CLASS_DEPTH];
    // Array of methods inherited from superclasses, and declared by this class
    method_t         cMethods[MAX_CLASS_METHODS];
} claz;

/* Specification for memory layout of java.lang.Class objects.  */
typedef struct _jlclass { 
    int    hashcode;
    claz * clazPtr; 
    // Pointer to a string representing the name of this class
    oobj * strName;
    // Pointer to the clazz structure for the class represented by this object
    claz * jlClazzPtr;  
    // Pointer to a java.lang.reflect.Method[] object storing the methods of 
    // this class (NULL if the class is a primitive type)
    oobj * methods;
    // Pointer to a java.lang.reflect.Field[] object storing the fields of 
    // this class (NULL if the class is a primitive type)
    oobj * fields;
} jlclass;

