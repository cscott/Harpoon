// ClassDataInputStream.java, created Mon Jan 18 22:44:36 1999 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.RawClass;

/**
 * A specialized input stream reader for java bytecode files.
 *
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: ClassDataInputStream.java,v 1.2 2002-02-25 21:05:26 cananian Exp $
 * @see ClassDataOutputStream
 */
public class ClassDataInputStream extends java.io.DataInputStream {
  /** Creates a <code>ClassDataInputStream</code> from a standard 
   *  <code>InputStream</code>. */
  public ClassDataInputStream(java.io.InputStream in) {
    super(in);
  }

  /** Supplies the mysterious missing method from the standard
   *  <code>DataInputStream</code> superclass. */
  public final long readUnsignedInt() throws java.io.IOException {
    return (((long) readUnsignedShort()) << 16) | ((long) readUnsignedShort());
  }

  /** Read an unsigned one-byte quantity, high byte first. */
  public final int  read_u1() throws java.io.IOException 
  { return readUnsignedByte(); }
  /** Read an unsigned two-byte quantity, high byte first. */
  public final int  read_u2() throws java.io.IOException 
  { return readUnsignedShort(); }
  /** Read an unsigned four-byte quantity, high byte first. */
  public final long read_u4() throws java.io.IOException 
  { return readUnsignedInt(); }
}
