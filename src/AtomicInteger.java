// AtomicInteger.java, created by harveyj
// Copyright (C) 2003 Harvey Jones <harveyj@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.

package java.util.concurrent.atomic;

public class AtomicInteger {
    public AtomicInteger(int initialValue){
	myValue = initialValue;
    }
    
    public native boolean compareAndSet(int expect, int update);
    
    public int get(){
	return myValue;
    }

    public native int getAndAdd(int delta);

    public int getAndDecrement(){
	return getAndAdd(-1);
    }

    public int getAndIncrement(){
	return getAndAdd(1);
    }

    public native int getAndSet(int newValue);

    public void set(int newValue){
	myValue = newValue;
    }

    public boolean weakCompareAndSet(int expect, int update){
	return compareAndSet(expect, update);
    }

    private int myValue;
}
