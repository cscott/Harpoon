// Eval.java, created Wed Sep  9 21:57:19 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Quads;

/**
 * <code>Eval</code>
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Eval.java,v 1.2 2002-02-25 21:05:12 cananian Exp $
 */
class Eval  {

    /** Reference comparison. */
    static boolean acmpeq(Object a, Object b) {
	return (a == b);
    }
    /** Convert double to float. */
    static float d2f(double d) {
	return (float) d;
    }
    /** Convert double to integer. */
    static int d2i(double d) {
	return (int) d;
    }
    /** Convert double to long. */
    static long d2l(double d) {
	return (long) d;
    }
    /** Add two doubles. */
    static double dadd(double a, double b) {
	return a + b;
    }
    /** Compare two doubles for equality */
    static boolean dcmpeq(double a, double b) {
	return (a==b);
    }
    /** Compare two doubles for greater-than-or-equal-to. */
    static boolean dcmpge(double a, double b) {
	return (a>=b);
    }
    /** Compare two doubles for greater-than. */
    static boolean dcmpgt(double a, double b) {
	return (a>b);
    }
    /** @deprecated */
    static int dcmpg(double a, double b) {
	if (a < b)
	    return -1;
	if (a == b)
	    return 0;
	return 1;
    }
    /** @deprecated */
    static int dcmpl(double a, double b) {
	if (a > b)
	    return 1;
	if (a == b)
	    return 0;
	return -1;
    }
    /** Divide two doubles. */
    static double ddiv(double a, double b) {
	return a / b;
    }
    /** Multiply two doubles. */
    static double dmul(double a, double b) {
	return a * b;
    }
    /** Negate a double. */
    static double dneg(double a) {
	return -a;
    }
    /** Remainder two doubles. */
    static double drem(double a, double b) {
	return a % b;
    }
    /** Subtract two doubles. */
    static double dsub(double a, double b) {
	return a - b;
    }
    /** Convert a float to a double. */
    static double f2d(float f) {
	return (double) f;
    }
    /** Convert a float to an integer. */
    static int f2i(float f) {
	return (int) f;
    }
    /** Convert a float to a long. */
    static long f2l(float f) {
	return (long) f;
    }
    /** Add two floats. */
    static float fadd(float a, float b) {
	return a + b;
    }
    /** Compare two floats for equality. */
    static boolean fcmpeq(float a, float b) {
	return (a==b);
    }
    /** Compare two floats for greater-than-or-equal-to. */
    static boolean fcmpge(float a, float b) {
	return (a>=b);
    }
    /** Compare two floats for greater-than. */
    static boolean fcmpgt(float a, float b) {
	return (a>b);
    }
    /** @deprecated */
    static int fcmpg(float a, float b) {
	if (a < b)
	    return -1;
	if (a == b)
	    return 0;
	return 1;
    }
    /** @deprecated */
    static int fcmpl(float a, float b) {
	if (a > b)
	    return 1;
	if (a == b)
	    return 0;
	return -1;
    }
    /** Divide two floats. */
    static float fdiv(float a, float b) {
	return a / b;
    }
    /** Multiply two floats. */
    static float fmul(float a, float b) {
	return a * b;
    }
    /** Negate a float. */
    static float fneg(float a) {
	return -a;
    }
    /** Remainder a float. */
    static float frem(float a, float b) {
	return a % b;
    }
    /** Subtract two floats. */
    static float fsub(float a, float b) {
	return a - b;
    }
    /** Convert an integer to a (8-bit) byte. */
    static int i2b(int i) {
	return (byte) i;
    }
    /** Convert an integer to a (16-bit) character. */
    static int i2c(int i) {
	return (char) i;
    }
    /** Convert an integer to a double. */
    static double i2d(int i) {
	return (double) i;
    }
    /** Convert an integer to a float. */
    static float i2f(int i) {
	return (float) i;
    }
    /** Convert an integer to a long. */
    static long i2l(int i) {
	return (long) i;
    }
    /** Convert an integer to a short. */
    static int i2s(int i) {
	return (short) i;
    }
    /** Add two integers. */
    static int iadd(int a, int b) {
	return a + b;
    }
    /** Bitwise-and two integers. */
    static int iand(int a, int b) {
	return a & b;
    }
    /** Compare two integers for equality. */
    static boolean icmpeq(int a, int b) {
	return (a == b);
    }
    /** Compare two integers for greater-than-or-equal-to
     * @deprecated */
    static boolean icmpge(int a, int b) {
	return (a >= b);
    }
    /** Compare two integers for greater-than. */
    static boolean icmpgt(int a, int b) {
	return (a > b);
    }
    /** Divide two integers. */
    static int idiv(int a, int b) {
	return a / b;
    }
    /** Multiply two integers. */
    static int imul(int a, int b) {
	return a * b;
    }
    /** Negate two integers. */
    static int ineg(int a) {
	return -a;
    }
    /** Bitwise-or two integers. */
    static int ior(int a, int b) {
	return a | b;
    }
    /** Remainder two integers. */
    static int irem(int a, int b) {
	return a % b;
    }
    /** Integer shift left. */
    static int ishl(int a, int b) {
	return a << b;
    }
    /** Signed integer shift right. */
    static int ishr(int a, int b) {
	return a >> b;
    }
    /** Integer subtract. */
    static int isub(int a, int b) {
	return a - b;
    }
    /** Unsigned integer shift right. */
    static int iushr(int a, int b) {
	return a >>> b;
    }
    /** Bitwise-xor two integers. */
    static int ixor(int a, int b) {
	return a ^ b;
    }
    /** Convert a long to a double. */
    static double l2d(long l) {
	return (double) l;
    }
    /** Convert a long to a float. */
    static float l2f(long l) {
	return (float) l;
    }
    /** Convert a long to an integer. */
    static int l2i(long l) {
	return (int) l;
    }
    /** Add two longs. */
    static long ladd(long a, long b) {
	return a + b;
    }
    /** Bitwise-and two longs. */
    static long land(long a, long b) {
	return a & b;
    }
    /** Compare two longs for equality. */
    static boolean lcmpeq(long a, long b) {
	return (a == b);
    }
    /** Compare two longs for greater-than-or-equal-to.
     * @deprecated */
    static boolean lcmpge(long a, long b) {
	return (a >= b);
    }
    /** Compare two longs for greater-than. */
    static boolean lcmpgt(long a, long b) {
	return (a > b);
    }
    /** Divide two longs. */
    static long ldiv(long a, long b) {
	return a / b;
    }
    /** Multiply two longs. */
    static long lmul(long a, long b) {
	return a * b;
    }
    /** Negate a long. */
    static long lneg(long a) {
	return -a;
    }
    /** Bitwise-or two longs. */
    static long lor(long a, long b) {
	return a | b;
    }
    /** Remainder two longs. */
    static long lrem(long a, long b) {
	return a % b;
    }
    /** Long shift left. */
    static long lshl(long a, int b) {
	return a << b;
    }
    /** Signed long shift right. */
    static long lshr(long a, int b) {
	return a >> b;
    }
    /** Subtract two longs. */
    static long lsub(long a, long b) {
	return a - b;
    }
    /** Unsigned long shift right. */
    static long lushr(long a, int b) {
	return a >>> b;
    }
    /** Bitwise-XOR two longs. */
    static long lxor(long a, long b) {
	return a ^ b;
    }
}
