package ClassFile;

class ClassDataInputStream extends java.io.DataInputStream {
  public ClassDataInputStream(java.io.InputStream in) {
    super(in);
  }

  public final long readUnsignedInt() throws java.io.IOException {
    return (((long) readUnsignedShort()) << 16) | ((long) readUnsignedShort());
  }

  public final int  read_u1() throws java.io.IOException 
  { return readUnsignedByte(); }
  public final int  read_u2() throws java.io.IOException 
  { return readUnsignedShort(); }
  public final long read_u4() throws java.io.IOException 
  { return readUnsignedInt(); }
}
