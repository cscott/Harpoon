package harpoon.ClassFile.Bytecode;

/**
 * The <code>Op</code> class contains constants and tables that we are
 * likely to find useful when grokking a java bytecode stream.
 * <p>
 * Included are: <UL>
 * <LI>final static byte constants for all the JVM opcodes.
 * <LI>a function giving the size (number of operands) of an
 *     arbitrary opcode.
 * <LI>a way to tell whether a given opcode is a branch instruction.
 * <LI>a function to decode the targets of any branch instruction.
 * </UL>
 *
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Op.java,v 1.1 1998-08-03 03:22:30 cananian Exp $
 * @see     harpoon.ClassFile.Raw.Attribute.AttributeCode
 * @see     harpoon.ClassFile.Bytecode.Instr
 * @see     harpoon.ClassFile.Bytecode.Code
 */

public final class Op {
  // Set up table of opcodes.
  public final static byte 
    NOP		= (byte) 0x00,
    ACONST_NULL	= (byte) 0x01,
    ICONST_M1   = (byte) 0x02,
    ICONST_0    = (byte) 0x03,
    ICONST_1    = (byte) 0x04,
    ICONST_2    = (byte) 0x05,
    ICONST_3    = (byte) 0x06,
    ICONST_4    = (byte) 0x07,
    ICONST_5    = (byte) 0x08,
    LCONST_0    = (byte) 0x09,
    LCONST_1    = (byte) 0x0A,
    FCONST_0    = (byte) 0x0B,
    FCONST_1    = (byte) 0x0C,
    FCONST_2    = (byte) 0x0D,
    DCONST_0    = (byte) 0x0E,
    DCONST_1    = (byte) 0x0F,
    BIPUSH      = (byte) 0x10,
    SIPUSH      = (byte) 0x11,
    LDC         = (byte) 0x12,
    LDC_W	= (byte) 0x13,
    LDC2_W	= (byte) 0x14,
    ILOAD	= (byte) 0x15,
    LLOAD	= (byte) 0x16,
    FLOAD	= (byte) 0x17,
    DLOAD	= (byte) 0x18,
    ALOAD	= (byte) 0x19,
    ILOAD_0	= (byte) 0x1A,
    ILOAD_1	= (byte) 0x1B,
    ILOAD_2	= (byte) 0x1C,
    ILOAD_3	= (byte) 0x1D,
    LLOAD_0	= (byte) 0x1E,
    LLOAD_1	= (byte) 0x1F,
    LLOAD_2	= (byte) 0x20,
    LLOAD_3	= (byte) 0x21,
    FLOAD_0	= (byte) 0x22,
    FLOAD_1	= (byte) 0x23,
    FLOAD_2	= (byte) 0x24,
    FLOAD_3	= (byte) 0x25,
    DLOAD_0	= (byte) 0x26,
    DLOAD_1	= (byte) 0x27,
    DLOAD_2	= (byte) 0x28,
    DLOAD_3	= (byte) 0x29,
    ALOAD_0	= (byte) 0x2A,
    ALOAD_1	= (byte) 0x2B,
    ALOAD_2	= (byte) 0x2C,
    ALOAD_3	= (byte) 0x2D,
    IALOAD	= (byte) 0x2E,
    LALOAD	= (byte) 0x2F,
    FALOAD	= (byte) 0x30,
    DALOAD	= (byte) 0x31,
    AALOAD	= (byte) 0x32,
    BALOAD	= (byte) 0x33,
    CALOAD	= (byte) 0x34,
    SALOAD	= (byte) 0x35,
    ISTORE	= (byte) 0x36,
    LSTORE	= (byte) 0x37,
    FSTORE	= (byte) 0x38,
    DSTORE	= (byte) 0x39,
    ASTORE	= (byte) 0x3A,
    ISTORE_0	= (byte) 0x3B,
    ISTORE_1	= (byte) 0x3C,
    ISTORE_2	= (byte) 0x3D,
    ISTORE_3	= (byte) 0x3E,
    LSTORE_0	= (byte) 0x3F;
  public final static byte 
    LSTORE_1    = (byte) 0x40,
    LSTORE_2	= (byte) 0x41,
    LSTORE_3	= (byte) 0x42,
    FSTORE_0	= (byte) 0x43,
    FSTORE_1	= (byte) 0x44,
    FSTORE_2	= (byte) 0x45,
    FSTORE_3	= (byte) 0x46,
    DSTORE_0	= (byte) 0x47,
    DSTORE_1	= (byte) 0x48,
    DSTORE_2	= (byte) 0x49,
    DSTORE_3	= (byte) 0x4A,
    ASTORE_0	= (byte) 0x4B,
    ASTORE_1	= (byte) 0x4C,
    ASTORE_2	= (byte) 0x4D,
    ASTORE_3	= (byte) 0x4E,
    IASTORE	= (byte) 0x4F,
    LASTORE	= (byte) 0x50,
    FASTORE	= (byte) 0x51,
    DASTORE	= (byte) 0x52,
    AASTORE	= (byte) 0x53,
    BASTORE	= (byte) 0x54,
    CASTORE	= (byte) 0x55,
    SASTORE	= (byte) 0x56,
    POP		= (byte) 0x57,
    POP2	= (byte) 0x58,
    DUP		= (byte) 0x59,
    DUP_X1	= (byte) 0x5A,
    DUP_X2	= (byte) 0x5B,
    DUP2	= (byte) 0x5C,
    DUP2_X1	= (byte) 0x5D,
    DUP2_X2	= (byte) 0x5E,
    SWAP	= (byte) 0x5F,
    IADD	= (byte) 0x60,
    LADD	= (byte) 0x61,
    FADD	= (byte) 0x62,
    DADD	= (byte) 0x63,
    ISUB	= (byte) 0x64,
    LSUB	= (byte) 0x65,
    FSUB	= (byte) 0x66,
    DSUB	= (byte) 0x67,
    IMUL	= (byte) 0x68,
    LMUL	= (byte) 0x69,
    FMUL	= (byte) 0x6A,
    DMUL	= (byte) 0x6B,
    IDIV	= (byte) 0x6C,
    LDIV	= (byte) 0x6D,
    FDIV	= (byte) 0x6E,
    DDIV	= (byte) 0x6F,
    IREM	= (byte) 0x70,
    LREM	= (byte) 0x71,
    FREM	= (byte) 0x72,
    DREM	= (byte) 0x73,
    INEG	= (byte) 0x74,
    LNEG	= (byte) 0x75,
    FNEG	= (byte) 0x76,
    DNEG	= (byte) 0x77,
    ISHL	= (byte) 0x78,
    LSHL	= (byte) 0x79,
    ISHR	= (byte) 0x7A,
    LSHR	= (byte) 0x7B,
    IUSHR	= (byte) 0x7C,
    LUSHR	= (byte) 0x7D,
    IAND	= (byte) 0x7E,
    LAND	= (byte) 0x7F;
  public final static byte 
    IOR         = (byte) 0x80,
    LOR		= (byte) 0x81,
    IXOR	= (byte) 0x82,
    LXOR	= (byte) 0x83,
    IINC	= (byte) 0x84,
    I2L		= (byte) 0x85,
    I2F		= (byte) 0x86,
    I2D		= (byte) 0x87,
    L2I		= (byte) 0x88,
    L2F		= (byte) 0x89,
    L2D		= (byte) 0x8A,
    F2I		= (byte) 0x8B,
    F2L		= (byte) 0x8C,
    F2D		= (byte) 0x8D,
    D2I		= (byte) 0x8E,
    D2L		= (byte) 0x8F,
    D2F		= (byte) 0x90,
    I2B		= (byte) 0x91,
    I2C		= (byte) 0x92,
    I2S		= (byte) 0x93,
    LCMP	= (byte) 0x94,
    FCMPL	= (byte) 0x95,
    FCMPG	= (byte) 0x96,
    DCMPL	= (byte) 0x97,
    DCMPG	= (byte) 0x98,
    IFEQ	= (byte) 0x99,
    IFNE	= (byte) 0x9A,
    IFLT	= (byte) 0x9B,
    IFGE	= (byte) 0x9C,
    IFGT	= (byte) 0x9D,
    IFLE	= (byte) 0x9E,
    IF_ICMPEQ	= (byte) 0x9F,
    IF_ICMPNE	= (byte) 0xA0,
    IF_ICMPLT	= (byte) 0xA1,
    IF_ICMPGE	= (byte) 0xA2,
    IF_ICMPGT	= (byte) 0xA3,
    IF_ICMPLE	= (byte) 0xA4,
    IF_ACMPEQ	= (byte) 0xA5,
    IF_ACMPNE	= (byte) 0xA6,
    GOTO	= (byte) 0xA7,
    JSR		= (byte) 0xA8,
    RET		= (byte) 0xA9,
    TABLESWITCH = (byte) 0xAA,
    LOOKUPSWITCH= (byte) 0xAB,
    IRETURN	= (byte) 0xAC,
    LRETURN	= (byte) 0xAD,
    FRETURN	= (byte) 0xAE,
    DRETURN	= (byte) 0xAF,
    ARETURN	= (byte) 0xB0,
    RETURN	= (byte) 0xB1,
    GETSTATIC	= (byte) 0xB2,
    PUTSTATIC	= (byte) 0xB3,
    GETFIELD	= (byte) 0xB4,
    PUTFIELD	= (byte) 0xB5,
    INVOKEVIRTUAL=(byte) 0xB6,
    INVOKESPECIAL=(byte) 0xB7,
    INVOKESTATIC= (byte) 0xB8,
    INVOKEINTERFACE=(byte) 0xB9,
    XXXUNUSEDXXX= (byte) 0xBA,
    NEW		= (byte) 0xBB,
    NEWARRAY	= (byte) 0xBC,
    ANEWARRAY	= (byte) 0xBD,
    ARRAYLENGTH	= (byte) 0xBE,
    ATHROW	= (byte) 0xBF;
  public final static byte 
    CHECKCAST	= (byte) 0xC0,
    INSTANCEOF	= (byte) 0xC1,
    MONITORENTER= (byte) 0xC2,
    MONITOREXIT	= (byte) 0xC3,
    WIDE	= (byte) 0xC4,
    MULTIANEWARRAY=(byte)0xC5,
    IFNULL	= (byte) 0xC6,
    IFNONNULL	= (byte) 0xC7,
    GOTO_W	= (byte) 0xC8,
    JSR_W	= (byte) 0xC9;

  // Reserved
  public final static byte 
    BREAKPOINT  = (byte) 0xCA,
    IMPDEP1	= (byte) 0xFE,
    IMPDEP2	= (byte) 0xFF;

  // Set up table indicating instruction length for each opcode.
  private static byte offset[] = new byte[256];
  static {
    for (int i=0; i<offset.length; i++) offset[i]=0;
    offset[ub2i(BIPUSH)] = 1;
    offset[ub2i(SIPUSH)] = 2;
    offset[ub2i(LDC)]    = 1;
    offset[ub2i(LDC_W)]  = 2;
    offset[ub2i(LDC2_W)] = 2;
    for (int i=ub2i(ILOAD); i<=ub2i(ALOAD); i++) offset[i]=1;
    for (int i=ub2i(ISTORE);i<=ub2i(ASTORE);i++) offset[i]=1;
    offset[ub2i(IINC)]   = 2;
    for (int i=ub2i(IFEQ);  i<=ub2i(JSR); i++)   offset[i]=2;
    offset[ub2i(RET)]    = 1;
    // TABLESWITCH and LOOKUPSWITCH are highly irregular.
    for (int i=ub2i(GETSTATIC); i<=ub2i(INVOKESTATIC); i++) offset[i]=2;
    offset[ub2i(INVOKEINTERFACE)] = 4;
    offset[ub2i(NEW)]      = 2;
    offset[ub2i(NEWARRAY)] = 1;
    offset[ub2i(ANEWARRAY)]= 2;
    offset[ub2i(CHECKCAST)]= 2;
    offset[ub2i(INSTANCEOF)]=2;
    // WIDE is irregular.
    offset[ub2i(MULTIANEWARRAY)] = 3;
    offset[ub2i(IFNULL)]   = 2;
    offset[ub2i(IFNONNULL)]= 2;
    offset[ub2i(GOTO_W)]   = 4;
    offset[ub2i(JSR_W)]    = 4;
  }

  /** 
   * Gives the length of the instruction at the specified offset in a 
   * bytecode array.
   * <P>
   * The length is the number of operand bytes plus 1 byte for the opcode.
   * @param code the bytecode array.
   * @param pc   the offset into the array where the opcode of interest is
   *             located.
   * @return the length of the opcode and operands.
   */
  public final static int instrSize(int pc, byte[] code) {
    byte opcode = code[pc];
    switch(opcode) {
    case LOOKUPSWITCH:
      {
      int pad = 3-(pc%4);
      long npairs = make_u4(code, pc+pad+5);
      return (int) (npairs*8) + pad + 9;
      }
    case TABLESWITCH:
      {
      int pad = 3-(pc%4);
      long low = make_u4(code, pc+pad+5);
      long high= make_u4(code, pc+pad+9);
      long npairs = high - low + 1;
      return (int) (npairs*4) + pad + 13;
      }
    case WIDE:
      if (code[pc+1]==IINC) return 6;
      return 4;
    default:
      return 1+offset[ub2i(opcode)];
    }
  }

  // Set up table to determine whether an opcode is a branch instruction.
  private static boolean isBranch[] = new boolean[256];
  static {
    for (int i=0; i<isBranch.length; i++) isBranch[i]=false;
    for (int i=ub2i(IFEQ); i<=ub2i(RET); i++) isBranch[i]=true;
    for (int i=ub2i(IFNULL); i<=ub2i(JSR_W); i++) isBranch[i]=true;
    for (int i=ub2i(IRETURN); i<=ub2i(RETURN); i++) isBranch[i]=true;
    isBranch[ub2i(TABLESWITCH)] = true;
    isBranch[ub2i(LOOKUPSWITCH)]= true;
  }

  /**
   * Tells whether a given opcode is a branch instruction.
   * @param opcode the opcode to check.
   * @return true if the opcode is a branch instruction.
   */
  public final static boolean isBranch(byte opcode) { 
    return isBranch[ub2i(opcode)];
  }

  /**
   * Tells whether a given opcode is an unconditional branch instruction.
   * @param opcode the opcode to check.
   * @return true if the opcode is an unconditional branch instruction.
   */
  public final static boolean isUnconditionalBranch(byte opcode) {
    switch(opcode) {
    case GOTO:
    case GOTO_W:
    case JSR:
    case JSR_W:
    case RET:
    case IRETURN:
    case LRETURN:
    case FRETURN:
    case DRETURN:
    case ARETURN:
    case RETURN:
      return true;
    default:
      return false;
    }
  }

  /** 
   * Computes all possible targets for a branch instruction.
   * @exception Error if the opcode is not a branch instruction.
   * @param code the bytecode array.
   * @param pc   the offset into the bytecode array at which the desired
   *             instruction is found.
   * @return     an array of all possible targets, in the same format as 
   *             <code>pc</code>. (ie, offsets from the start of the
   *             bytecode array.)
   */
  public final static int[] branchTargets(int pc, byte[] code) {
    if (!isBranch(code[pc])) 
      throw new Error("Asking for target of non-branch.");
    // Funky switch statements.
    if (code[pc]==LOOKUPSWITCH) {
      int pad = 3-(pc%4);
      long deflt  = make_u4(code, pc+pad+1);
      long npairs = make_u4(code, pc+pad+5);

      int result[] = new int[(int)npairs+1];
      result[0] = pc + ((int) deflt);
      for (int i=0; i<npairs; i++) {
	long offset = make_u4(code, pc+pad+9+4+(8*i));
	result[i+1] = pc + ((int) offset);
      }
      return result;
    }
    if (code[pc]==TABLESWITCH) {
      int pad = 3-(pc%4);
      long deflt  = make_u4(code, pc+pad+1);
      long low    = make_u4(code, pc+pad+5);
      long high   = make_u4(code, pc+pad+9);
      long npairs = high - low + 1;

      int result[] = new int[(int)npairs+1];
      result[0] = pc + ((int) deflt);
      for (int i=0; i<npairs; i++) {
	long offset = make_u4(code, pc+pad+13+(4*i));
	result[i+1] = pc + ((int) offset);
      }
      return result;
    }
    // Wide goto and jsr.
    if (code[pc]==GOTO_W || code[pc]==JSR_W) {
      long offset = make_u4(code, pc+1);
      int result[] = new int[1];
      result[0] = pc + ((int)offset);
      return result;
    }
    // ret/return (no targets)
    if (code[pc]==RET     || code[pc]==RETURN  ||
	code[pc]==IRETURN || code[pc]==LRETURN ||
	code[pc]==FRETURN || code[pc]==DRETURN ||
	code[pc]==ARETURN)
      return new int[0];
    // all other cases:
    {
      short offset = (short) ((ub2i(code[pc+1]) << 8) | ub2i(code[pc+2]));
      int result[] = new int[1];
      result[0] = pc+offset;
      return result;
    }
  }

  // Helper functions.
  private static final long make_u4(byte[] b, int off) {
    return make_u4(b[off], b[off+1], b[off+2], b[off+3]);
  }
  private static final long make_u4(byte b1, byte b2, byte b3, byte b4) {
    long l1 = ((long) b1) & 0xFF;
    long l2 = ((long) b2) & 0xFF;
    long l3 = ((long) b3) & 0xFF;
    long l4 = ((long) b4) & 0xFF;
    return (l1<<24)|(l2<<16)|(l3<<8)|l4;
  }
  private static int ub2i(byte b) {
    return ((int) b) & 0xFF;
  }
}
