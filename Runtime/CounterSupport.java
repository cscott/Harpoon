// CounterSupport.java, created Tue Nov  7 14:12:49 2000 by root
// Copyright (C) 2000 bdemsky <bdemsky@LM.LCS.MIT.EDU>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Runtime;
import java.io.ObjectOutputStream;
import java.io.FileOutputStream;
import java.io.PrintStream;
/**
 * <code>CounterSupport</code> provides support for simple instrumentation.
 * using counters identified by integers.
 * 
 * @author  bdemsky <bdemsky@lm.lcs.mit.edu>
 * @version $Id: CounterSupport.java,v 1.1.2.2 2000-11-10 06:46:06 bdemsky Exp $
 */
public class CounterSupport {
    static int size,sizesync;
    static int numbins, bincap;
    static long[] array;
    static long[] arraysync;
    static int[][] boundedkey;
    static int[][] boundedvalue;
    static Object lock=new Object();
    static boolean counton;
    static int error;
    static int overflow;
    static {
	//redefine these
	numbins=211;
	bincap=10;
	boundedkey=new int[numbins][bincap];
	boundedvalue=new int[numbins][bincap];

	overflow=0;
	error=0;
	size=10;
	array=new long[size];
	sizesync=10;
	arraysync=new long[sizesync];
	counton=true;
    }
    
    static void count(int value) {
	synchronized(lock) {
	    if (counton) {
		if (value>size) {
		    long[] newarray=new long[value*2];
		    System.arraycopy(array,0,newarray,0,size);
		    array=newarray;
		    size=value*2;
		}
		array[value]++;
	    }
	}
    }


    //Sync code only
    static void countm(Object obj) {
	synchronized(lock) {
	    if (counton) {
		int hash=obj.hashCode();
		int hashmod=hash % numbins;
		int bin=-1;
		for(int i=0;i<bincap;i++)
		    if (boundedkey[hashmod][i]==hash) {
			bin=i;
			break;
		    }
		if(bin==-1)
		    error++;
		else {
		    int value=boundedvalue[hashmod][bin];
		    for(int i=bin;i>0;i--) {
			boundedkey[hashmod][i]=boundedkey[hashmod][i-1];
			boundedvalue[hashmod][i]=boundedvalue[hashmod][i-1];
		    }
		    boundedkey[hashmod][0]=hash;
		    boundedvalue[hashmod][0]=value;
		    if (value>sizesync) {
			long[] newarray=new long[value*2];
			System.arraycopy(arraysync,0,newarray,0,sizesync);
			arraysync=newarray;
			sizesync=value*2;
		    }
		    arraysync[value]++;
		}
	    }
	}
    }

    static void label(Object obj, int value) {
	int hash=obj.hashCode();
	int hashmod=hash % numbins;
	if (boundedkey[hashmod][bincap-1]!=0)
	    overflow++;
	for (int i=bincap-1;i>0;i--) {
	    boundedkey[hashmod][i]=boundedkey[hashmod][i-1];
	    boundedvalue[hashmod][i]=boundedvalue[hashmod][i-1];
	}
	boundedkey[hashmod][0]=hash;
	boundedvalue[hashmod][0]=value;
    }

    static void exit() {
 	counton=false;
	//Show to the screen for the curious
	System.out.println("Error count[no mapping]="+error);
	System.out.println("# overflowed="+overflow);
	System.out.println("Allocation array");
	for(int i=0;i<size;i++)
	    System.out.println(i+"  "+array[i]);
	System.out.println("Sync array");
	for(int i=0;i<sizesync;i++)
	    System.out.println(i+"  "+arraysync[i]);

	try {
	    PrintStream fos=new java.io.PrintStream(new FileOutputStream("profile"));
	    fos.println(size);
	    for(int i=0;i<size;i++)
		fos.println(array[i]);
	    fos.println(sizesync);
	    for(int i=0;i<sizesync;i++)
		fos.println(arraysync[i]);
	    fos.close();
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }
}
