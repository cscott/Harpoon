// Instrumentate.java, created Tue Nov  7 14:12:49 2000 by root
// Copyright (C) 2000 bdemsky <bdemsky@LM.LCS.MIT.EDU>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Runtime;
import java.io.ObjectOutputStream;
import java.io.FileOutputStream;
import java.io.PrintStream;
/**
 * <code>Instrumentate</code>
 * 
 * @author  bdemsky <bdemsky@lm.lcs.mit.edu>
 * @version $Id: Instrumentate.java,v 1.1.2.3 2000-11-08 20:07:02 bdemsky Exp $
 */
public class Instrumentate {
    static int size;
    static long[] array;
    static Object lock=new Object();
    static boolean counton;
    static {
	size=10;
	array=new long[size];
	counton=true;
    }
    
    static void count(int value) {
	synchronized(lock) {
	    if (counton) {
		if (value>size) {
		    long[] newarray=new long[size*2];
		    System.arraycopy(array,0,newarray,0,size);
		    array=newarray;
		    size=size*2;
		}
		array[value]++;
	    }
	}
    }
    
    static void exit() {
	counton=false;
	//Show to the screen for the curious
	for(int i=0;i<size;i++)
	    System.out.println(i+"  "+array[i]);
	try {
	    PrintStream fos=new java.io.PrintStream(new FileOutputStream("profile"));
	    fos.println(size);
	    for(int i=0;i<size;i++)
		fos.println(array[i]);
	    fos.close();
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }
}
