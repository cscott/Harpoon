/*
 * %W% %G%
 *
 * Copyright (c) 1998 Standard Performance Evaluation Corporation (SPEC)
 *               All rights reserved.
 * Copyright (c) 1997,1998 Sun Microsystems, Inc. All rights reserved.
 *
 * This source code is provided as is, without any express or implied warranty.
 */

/**
 * This class implements a special form of PrintStream that is used by
 * the benchmarks. The class vaieable spec.harness.Context.out is made
 * to point to an instance of this class. The purpuse of the class is to
 * record validity check information with the recorded output. This is done
 * using one of nine integer values (0-8). 0 means the default validity checking
 * is to be used and is what a Context.out.println() would employ. The numbers 
 * 1 through 8 are used to set various valitity cheching rules. This class 
 * implements a set of println() type methods that allow the text output to be 
 * does so within the context of a certain validity cheching value. 
 *
 * These routines will output the valitity check value to the associated 
 * OutputStream unchanged. This will cause them to be output as the character 
 * values \u0000 to \u0008 these values are not normally used (the next one 
 * \u0009 is) so this should not cause a problem. However this is checked for 
 * in ValidityCheckOutputStream.
 *
 * @see ConsoleOutputStream
 * @see ValidityCheckOutputStream
 */ 
public
class PrintStream extends java.io.PrintStream {
    /**
     * The actual output stream. This is the same as 'out' in our superclass but
     * kept here as well to avoid a lot of runtime casting.
     */
    ConsoleOutputStream cout;    
    
    /**
     * Creates a new PrintStream.
     * @param out the output stream
     */
    public PrintStream(java.io.OutputStream out) {
	super(out, false);
	cout = (ConsoleOutputStream)out;
    }
    
    /**
     * Print a string in a validity context
     * @param v the validity context value.
     * @param s the data to be printed.
     */    
    synchronized public void print(char v, String s) {
	char save = cout.setValidityCheckValue(v);
	super.print(s);
	cout.setValidityCheckValue(save);	
    }
    
    /**
     * Print a string in a validity context
     * @param v the validity context value.
     * @param s the data to be printed.
     */    
    synchronized public void println(char v, String s) {
	char save = cout.setValidityCheckValue(v);
	super.println(s);
	cout.setValidityCheckValue(save);	
    }    
}
