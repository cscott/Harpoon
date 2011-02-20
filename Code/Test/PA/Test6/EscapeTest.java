// EscapeTest.java, created Thu Feb 13 14:08:30 2000 by whaley
// Copyright (C) 2000 John Whaley <jwhaley@ALUM.MIT.EDU>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Test.PA.Test6;


public class EscapeTest {
    
    volatile static int count;
    
    public static void main(String[] args) throws Exception {
	if ((args.length != 0) && args[0].equals("3"))
	    test3(args);
	else if ((args.length != 0) && args[0].equals("2"))
	    test2(args);
	else
	    test1(args);
    }
    
    public static void test1(String[] args) throws Exception {
	EscapeTest o = EscapeTest.factory();
	EscapeTest p = EscapeTest.factory();
	o.testsync0();
	p.testsync0();
	o.testsync1(o);
	o.testsync1(p);
	o.testsync2(o);
	o.testsync2(p);
	o.testsync3(o);
	o.testsync3(p);
	o.testsync4(o);
	o.testsync4(p);
	
	MyThread[] t = new MyThread[2];
	int i;
	for (i=0; i<2; ++i) {
	    t[i] = new MyThread();
	    t[i].init(o);
	    t[i].start();
	}
	
	for (i=0; i<2; ++i) {
	    t[i].join();
	}
	//System.out.println(count);
    }
    
    public static void test2(String[] args) throws Exception {
	EscapeTest o = EscapeTest.factory();
	EscapeTest p = EscapeTest.factory();
	o.testsync0();
	p.testsync0();
	o.testsync1(o);
	o.testsync1(p);
	o.testsync2(o);
	o.testsync2(p);
	o.testsync3(o);
	o.testsync3(p);
	o.testsync4(o);
	o.testsync4(p);
	
	MyThread t = new MyThread();
	t.init(o);
	t.start();
	
	o.testsync0();
	p.testsync0();
	o.testsync1(o);
	o.testsync1(p);
	o.testsync2(o);
	o.testsync2(p);
	o.testsync3(o);
	o.testsync3(p);
	o.testsync4(o);
	o.testsync4(p);
	
	for (int i=0; i<16; ++i) {
	    o.testsync0();
	    p.testsync0();
	    o.testsync1(o);
	    o.testsync1(p);
	    o.testsync2(o);
	    o.testsync2(p);
	    o.testsync3(o);
	    o.testsync3(p);
	    o.testsync4(o);
	    o.testsync4(p);
	}
	t.join();
	//System.out.println(count);
    }

    public static void test3(String[] args) throws Exception {
	EscapeTest o = EscapeTest.factory();
	EscapeTest p = EscapeTest.factory();
	
	o.testsync1(o);
	p.testsync1(p);
	
	SimpleThread t = new SimpleThread();
	t.init(o);
	t.start();
	
	o.testsync1(o);
	p.testsync1(p);
	
	for (int i=0; i<16; ++i) {
	    o.testsync1(o);
	    p.testsync1(p);
	}
	t.join();
	//System.out.println(count);
    }
    
    public static EscapeTest factory() {
	return new EscapeTest();
    }
    
    public synchronized void testsync0() {
	++count;
    }
    
    public synchronized void testsync1(EscapeTest arg1) {
	++count;
    }
    
    public synchronized void testsync2(EscapeTest arg1) {
	synchronized (arg1) {
	    ++count;
	}
    }
    
    public synchronized void testsync3(EscapeTest arg1) {
	arg1.testsync0();
    }
    
    public void testsync4(EscapeTest arg1) {
	synchronized (arg1) {
	    synchronized (this) {
		synchronized (arg1) {
		    ++count;
		}
	    }
	}
    }

}

class MyThread extends Thread {
    
    EscapeTest o;
    
    void init(EscapeTest t) {
	o = t;
    }

    public void run() {
	EscapeTest p = EscapeTest.factory();
	o.testsync0();
	p.testsync0();
	o.testsync1(o);
	o.testsync1(p);
	o.testsync2(o);
	o.testsync2(p);
	o.testsync3(o);
	o.testsync3(p);
	o.testsync4(o);
	o.testsync4(p);
	
	for (int i=0; i<16; ++i) {
	    o.testsync0();
	    p.testsync0();
	    o.testsync1(o);
	    o.testsync1(p);
	    o.testsync2(o);
	    o.testsync2(p);
	    o.testsync3(o);
	    o.testsync3(p);
	    o.testsync4(o);
	    o.testsync4(p);
	}
    }
    
}

class SimpleThread extends Thread {
    
    EscapeTest o;
    
    void init(EscapeTest t) {
	o = t;
    }

    public void run() {
	EscapeTest p = EscapeTest.factory();
	p.testsync0();
	
	for (int i=0; i<16; ++i) {
	    p.testsync0();
	}
    }
    
}
