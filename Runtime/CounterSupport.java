// CounterSupport.java, created Tue Nov  7 14:12:49 2000 by root
// Copyright (C) 2000 Brian Demsky <bdemsky@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Runtime;
import java.io.ObjectOutputStream;
import java.io.FileOutputStream;
import java.io.PrintStream;
/**
 * <code>CounterSupport</code> provides support for simple instrumentation.
 * using counters identified by integers.
 * 
 * @author  Brian Demsky <bdemsky@mit.edu>
 * @version $Id: CounterSupport.java,v 1.4 2003-02-09 18:31:23 salcianu Exp $ */
public class CounterSupport {
    static int sizesync;
    static int numbins, bincap;
    static long[] obj_count;
    static int obj_count_length;
    static long[] mem_amount;
    static int mem_amount_length;
    static long[] arraysync;
    static Object[][] boundedkey;
    static int[][] boundedvalue;
    static Object lock=new Object();
    static boolean counton,setup;
    static int error;
    static int overflow;
    static int[][] callchain;
    static int csize,tsize;
    static int[] depth;
    static int[][] alloccall;
    static int size1,size2;
    static Thread[] threadarray;
    static int depthoverflow, threadoverflow;

    static {
	//redefine these
	numbins=211;
	bincap=30;
	csize=100;
	tsize=100;
	boundedkey=new Object[numbins][bincap];
	boundedvalue=new int[numbins][bincap];
	callchain=new int[csize][tsize];
	depth=new int[tsize];
	threadarray=new Thread[tsize];

	depthoverflow=0;
	threadoverflow=0;
	overflow=0;
	error=0;

	obj_count_length = 10;
	obj_count = new long[obj_count_length];
	mem_amount_length = obj_count_length;
	mem_amount = new long[mem_amount_length];

	sizesync=10;
	arraysync=new long[sizesync];
	size1=10;
	size2=10;
	alloccall=new int[size1][size2];
	counton=true;
	setup=true;
    }
    

    static void count(int allocID) {
	if (setup && counton)
	    synchronized(lock) {
		count_aux(allocID);
	    }
    }


    static void count2(int allocID, int length) {
	if (setup && counton)
	    synchronized(lock) {
		count_aux(allocID);
		update_mem_amount(allocID, length);
	    }
    }

    
    static void update_mem_amount(int allocID, int length) {
	// allocID == -1 stands for allocation sites that haven't
	// received an unique integer ID 
	if (allocID == -1) return;
	if (allocID >= mem_amount_length) {
	    long[] new_mem_amount = new long[allocID * 2];
	    System.arraycopy(mem_amount, 0,
			     new_mem_amount, 0,
			     mem_amount_length);
	    mem_amount = new_mem_amount;
	    mem_amount_length = allocID * 2;
	}
	mem_amount[allocID]++;
    }
    

    static void count_aux(int value) {
	if (counton) {
	    boolean resize=false;
	    int nsize1=size1,nsize2=size2;
	    if (value>=size1) {
		nsize1=value*2;
		resize=true;
	    }
	    int thisthread=-1;
	    Thread t=Thread.currentThread();
	    for(int j=0;j<tsize;j++) {
		if (t==threadarray[j]) {
		    thisthread=j;
		    break;
		}
	    }
	    if (thisthread!=-1)
		if ((depth[thisthread]!=0)&&((depth[thisthread]-1)<csize))
		    if (callchain[depth[thisthread]-1][thisthread]>=size2) {
			nsize2=callchain[depth[thisthread]-1][thisthread]*2;
			resize=true;
		    }
	    if (resize) {
		int[][] newarray=new int[nsize1][nsize2];
		for(int i=0;i<size1;i++) {
		    System.arraycopy(alloccall[i],0,newarray[i],0,size2);
		}
		size1=nsize1;
		size2=nsize2;
		alloccall=newarray;
	    }
	    if (thisthread!=-1) {
		if ((depth[thisthread]!=0)&&((depth[thisthread]-1)<csize))
		    alloccall[value][callchain[depth[thisthread]-1][thisthread]]++;
	    } else alloccall[value][0]++;
	    
	    if (value >= obj_count_length) {
		long[] new_obj_count = new long[value*2];
		System.arraycopy(obj_count, 0,
				 new_obj_count, 0,
				 obj_count_length);
		obj_count = new_obj_count;
		obj_count_length = value*2;
	    }
	    obj_count[value]++;
	}
    }

    //Sync code only
    static void countm(Object obj) {
	if (setup)
	synchronized(lock) {
	    if (counton) {
		counton=false;
		int hash=java.lang.System.identityHashCode(obj);
		counton=true;
		int hashmod=hash % numbins;
		int bin=-1;
		for(int i=0;i<bincap;i++)
		    if (boundedkey[hashmod][i]==obj) {
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
		    boundedkey[hashmod][0]=obj;
		    boundedvalue[hashmod][0]=value;
		    if (value>=sizesync) {
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
	if (setup)
	synchronized(lock) {
	    if (counton) {
		counton=false;
		int hash=java.lang.System.identityHashCode(obj);
		counton=true;
		int hashmod=hash % numbins;
		if (boundedkey[hashmod][bincap-1]!=null)
		    overflow++;
		//	    for (int i=bincap-1;i>0;i--) {
		//	boundedkey[hashmod][i]=boundedkey[hashmod][i-1];
		//}
		System.arraycopy(boundedkey[hashmod],0,boundedkey[hashmod],1,bincap-1);
		System.arraycopy(boundedvalue[hashmod],0,boundedvalue[hashmod],1,bincap-1);
		boundedkey[hashmod][0]=obj;
		boundedvalue[hashmod][0]=value;
	    }
	}
    }

    static void callenter(int callsite) {
	if (setup)
	    synchronized(lock) {
		if (counton) {
		    int firstnull=-1,thisthread=-1;
		    Thread t=Thread.currentThread();
		    for(int j=0;j<tsize;j++) {
			if (t==threadarray[j]) {
			    thisthread=j;
			    break;
			}
			if ((threadarray[j]==null)&&(firstnull==-1))
			    firstnull=j;
		    }
		    if (thisthread==-1) {
			if (firstnull!=-1) {
			    threadarray[firstnull]=t;
			    depth[firstnull]=0;
			    thisthread=firstnull;
			} else
			    threadoverflow++;
		    }
		    if (thisthread!=-1) {
			if (depth[thisthread]<csize)
			    callchain[depth[thisthread]][thisthread]=callsite+1;
			else
			    depthoverflow++;
			depth[thisthread]++;
		    }
		}
	    }
    }

    static void callexit() {
	if (setup)
	    synchronized(lock) {
		if (counton) {
		    int thisthread=-1;
		    Thread t=Thread.currentThread();
		    for(int j=0;j<tsize;j++) {
			if (t==threadarray[j]) {
			    thisthread=j;
			    break;
			}
		    }
		    if (thisthread!=-1) {
			depth[thisthread]--;
			if (depth[thisthread]==0)
			    threadarray[thisthread]=null;
		    }
		}
	    }
    }

    static void exit() {
 	counton=false;
	//Show to the screen for the curious
	System.out.println("Error count[no mapping]="+error);
	System.out.println("# overflowed="+overflow);
	System.out.println("Call stack depth overflow="+depthoverflow);
	System.out.println("Call stack thread overflow="+threadoverflow);

	System.out.println("Allocation array BEGIN");
	for(int i = 0; i < obj_count_length; i++)
	    if (obj_count[i] != 0)
		System.out.println
		    (i + "  " + obj_count[i] +
		     ((i < mem_amount_length) ? mem_amount[i] : 0));
	System.out.println("Allocation array END");


	System.out.println("Sync array BEGIN");
	for(int i=0;i<sizesync;i++)
	    if (arraysync[i]!=0)
		System.out.println(i+"  "+arraysync[i]);
	System.out.println("Sync array END");

	int tripletcount=0;
	System.out.println("Call Chain Allocation array");
	for(int i=0;i<size1;i++) {
	    if (alloccall[i][0]!=0) {
		System.out.println(i+" Unknown or main caller callsite = "+alloccall[i][0]);
		tripletcount++;
	    }
	    for(int j=1;j<size2;j++)
		if (alloccall[i][j]!=0) {
		    System.out.println(i+"  "+" "+(j-1)+" = "+alloccall[i][j]);
		    tripletcount++;
		}
	}

	try {
	    PrintStream fos
		= new java.io.PrintStream(new FileOutputStream("profile"));

	    fos.println(obj_count_length);
	    for(int i = 0; i < obj_count_length; i++) {
		fos.println(obj_count[i]);
		fos.println((i < mem_amount_length) ? mem_amount[i] : 0);
	    }

	    fos.println(sizesync);
	    for(int i=0;i<sizesync;i++)
		fos.println(arraysync[i]);

	    fos.println(tripletcount);
	    for(int i=0;i<size1;i++)
		for(int j=0;j<size2;j++)
		    if (alloccall[i][j]!=0) {
			fos.println(i);
			fos.println(j);
			fos.println(alloccall[i][j]);
		    }
	    fos.close();
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }
}
