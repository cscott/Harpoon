// Eval.java, created Wed Sep  9 21:57:19 1998 by cananian
package harpoon.IR.QuadSSA;

import harpoon.ClassFile.*;
/**
 * <code>Eval</code>
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Eval.java,v 1.1 1998-09-10 04:00:34 cananian Exp $
 */

class Eval  {

    static boolean acmpeq(Object a, Object b) {
	return (a == b);
    }
    static float d2f(double d) {
	return (float) d;
    }
    static int d2i(double d) {
	return (int) d;
    }
    static long d2l(double d) {
	return (long) d;
    }
    static double dadd(double a, double b) {
	return a + b;
    }
    static int dcmpg(double a, double b) {
	if (a < b)
	    return -1;
	if (a == b)
	    return 0;
	return 1;
    }
    static int dcmpl(double a, double b) {
	if (a > b)
	    return 1;
	if (a == b)
	    return 0;
	return -1;
    }
    static double ddiv(double a, double b) {
	return a / b;
    }
    static double dmul(double a, double b) {
	return a * b;
    }
    static double dneg(double a) {
	return -a;
    }
    static double drem(double a, double b) {
	return a % b;
    }
    static double dsub(double a, double b) {
	return a - b;
    }
    static double f2d(float f) {
	return (double) f;
    }
    static int f2i(float f) {
	return (int) f;
    }
    static long f2l(float f) {
	return (long) f;
    }
    static float fadd(float a, float b) {
	return a + b;
    }
    static int fcmpg(float a, float b) {
	if (a < b)
	    return -1;
	if (a == b)
	    return 0;
	return 1;
    }
    static int fcmpl(float a, float b) {
	if (a > b)
	    return 1;
	if (a == b)
	    return 0;
	return -1;
    }
    static float fdiv(float a, float b) {
	return a / b;
    }
    static float fmul(float a, float b) {
	return a * b;
    }
    static float fneg(float a) {
	return -a;
    }
    static float frem(float a, float b) {
	return a % b;
    }
    static float fsub(float a, float b) {
	return a - b;
    }
    static int i2b(int i) {
	return (byte) i;
    }
    static int i2c(int i) {
	return (char) i;
    }
    static double i2d(int i) {
	return (double) i;
    }
    static float i2f(int i) {
	return (float) i;
    }
    static long i2l(int i) {
	return (long) i;
    }
    static int i2s(int i) {
	return (short) i;
    }
    static int iadd(int a, int b) {
	return a + b;
    }
    static int iand(int a, int b) {
	return a & b;
    }
    static boolean icmpeq(int a, int b) {
	return (a == b);
    }
    static boolean icmpge(int a, int b) {
	return (a >= b);
    }
    static boolean icmpgt(int a, int b) {
	return (a > b);
    }
    static int idiv(int a, int b) {
	return a / b;
    }
    static int imul(int a, int b) {
	return a * b;
    }
    static int ineg(int a) {
	return -a;
    }
    static int ior(int a, int b) {
	return a | b;
    }
    static int irem(int a, int b) {
	return a % b;
    }
    static int ishl(int a, int b) {
	return a << b;
    }
    static int ishr(int a, int b) {
	return a >> b;
    }
    static int isub(int a, int b) {
	return a - b;
    }
    static int iushr(int a, int b) {
	return a >>> b;
    }
    static int ixor(int a, int b) {
	return a ^ b;
    }
    static double l2d(long l) {
	return (double) l;
    }
    static float l2f(long l) {
	return (float) l;
    }
    static int l2i(long l) {
	return (int) l;
    }
    static long ladd(long a, long b) {
	return a + b;
    }
    static long land(long a, long b) {
	return a & b;
    }
    static boolean lcmpeq(long a, long b) {
	return (a == b);
    }
    static boolean lcmpge(long a, long b) {
	return (a >= b);
    }
    static boolean lcmpgt(long a, long b) {
	return (a > b);
    }
    static long ldiv(long a, long b) {
	return a / b;
    }
    static long lmul(long a, long b) {
	return a * b;
    }
    static long lneg(long a) {
	return -a;
    }
    static long lor(long a, long b) {
	return a | b;
    }
    static long lrem(long a, long b) {
	return a % b;
    }
    static long lshl(long a, int b) {
	return a << b;
    }
    static long lshr(long a, int b) {
	return a >> b;
    }
    static long lsub(long a, long b) {
	return a - b;
    }
    static long lushr(long a, int b) {
	return a >>> b;
    }
    static long lxor(long a, long b) {
	return a ^ b;
    }
}
