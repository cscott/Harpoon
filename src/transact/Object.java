package java.lang;

/** Quick interface definition file from which we can generate
 *  a JNI-style header for the methods we'd like to define. */
import harpoon.Runtime.Transactions.*;
import java.lang.reflect.*;

public class Object {
    /** Get a version suitable for reading. */
    public native Object getReadableVersion( CommitRecord cr );
    /** Get a version suitable for reading or writing. */
    public native Object getReadWritableVersion( CommitRecord cr );
    /** Get the most recently committed version to read from. */
    public native Object getReadCommittedVersion();
    /** Get the most recently committed version to write to (and read from). */
    public native Object getWriteCommittedVersion();
    /** Create a new fully committed version (if one does not already exist)
     *  to write (values equal to the FLAG) to. */
    public native Object makeCommittedVersion();
    /** Ensure that a given field is flagged. */
    public native void writeFieldFlag( Field f, Class type );
    /** Ensure that a given array element is flagged. */
    public native void writeArrayElementFlag( int index, Class type );
    /** Ensure that a flag exists at the specified offset and size from
     *  this object. */
    public native void writeFlag( int offset, int size );
}
