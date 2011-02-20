// StateMemory.java, created by benster
// Copyright (C) 2003 Reuben Sterling <benster@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package imagerec.util;

/**
 * {@link StateMemory} implements a utility that allows Nodes that require image-specific state
 * to support multi-threading (i.e., processing more than one image simultaneously).
 * 
 * A single {@link StateMemory} object holds information for one field, like a double, int, intarray, etc...
 * The object may hold many different values for that one field, each keyed by an integer.
 * By default, the key is the thread ID, but the user may provide his own key if desired otherwise.
 * 
 * @author Reuben Sterling <<a href="mailto:benster@mit.edu">benster@mit.edu</a>>
 */
public class StateMemory {
    public static final int INT = 0;
    public static final int DOUBLE = 1;
    public static final int FLOAT = 2;
    public static final int BOOLEAN = 3;
    public static final int LONG = 4;

    private Pair[] pairs;

    public StateMemory() {
	pairs = new Pair[0];
    }

    /**
     * Add the specified double to this memory object, keyed by the specified integer.
     */
    public void addValue(int key, double val) {
	Object value = new Double(val);
	addValue(key, value);
    }

    /**
     * Add the specified integer to this memory object, keyed by the specified integer.
     */
    public void addValue(int key, int val) {
	Object value = new Integer(val);
	addValue(key, value);
    }
   
    
    /**
     * Add the specified value to this memory object, keyed by the specified integer.
     */
    private void addValue(int key, Object val) {
	Pair newPair = new Pair(key, val);
	int size = pairs.length;
	for (int count = 0; count < size; count++) {
	    if (pairs[count].key == key)
		return;
	}
	Pair[] newPairs = new Pair[size+1];
	for (int count = 0; count < size; count++) {
	    newPairs[count] = pairs[count];
	}
	newPairs[size] = newPair;
	pairs = newPairs;
    }

    /**
     * Removes the value associated with the specified key from this memory object.
     */
    public void remValue(int key) {
	int index;
	for (index = 0; index < pairs.length; index++) {
	    if (pairs[index].key == key)
		break;
	}
	//if found nothing, then do nothing and return
	if (index == pairs.length) {
	    throw new NoValueException("The value with the specified key ("+key+") does not exist.");
	}

	Pair[] newPairs = new Pair[pairs.length-1];
	for (int count = 0; count < index; count++)
	    newPairs[count] = pairs[count];
	for(int count = index; count < newPairs.length; count++)
	    newPairs[count] = pairs[count+1];
	pairs = newPairs;
	return;
    }

    /**
     * Gets the integer associated with the specified key from this memory object.
     */
    public int getInt(int key) {
	int index;
	for (index = 0; index < pairs.length; index++) {
	    if (pairs[index].key == key)
		return ((Integer)pairs[index].val).intValue();
	}
	throw new NoValueException("The Integer with the specified key ("+key+") does not exist.");
    }

    public double getDouble(int key) {
	int index;
	for (index = 0; index < pairs.length; index++) {
	    if (pairs[index].key == key)
		return ((Double)pairs[index].val).doubleValue();
	}
	throw new NoValueException("The Double with the specified key ("+key+") does not exist.");
    }


    public void setInt(int key, int val) {
	int index;
	//System.out.println("key: "+key);
	for (index = 0; index < pairs.length; index++) {
	    // System.out.println("pairs[index].key: "+pairs[index].key);
	    if (pairs[index].key == key) {
		pairs[index].val = new Integer(val);
		return;
	    }
	}
 	throw new NoValueException("The Integer with the specified key ("+key+") does not exist.");
   }

    private class Pair {
	Object val;
	int key;
	Pair(int key, Object val) {
	    this.key = key;
	    this.val = val;
	}
    }

}
