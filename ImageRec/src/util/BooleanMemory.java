// BooleanMemory.java, created by benster
// Copyright (C) 2003 Reuben Sterling <benster@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package imagerec.util;

/**
 * {@link BooleanMemory} implements a utility that allows Nodes that require image-specific state
 * to support multi-threading (i.e., processing more than one image simultaneously).
 * 
 * A single {@link BooleanMemory} object holds information for one boolean field.
 * The object may hold many different values for that one field, each keyed by an integer.
 * 
 * @author Reuben Sterling <<a href="mailto:benster@mit.edu">benster@mit.edu</a>>
 */
public class BooleanMemory {

    private Pair[] pairs;

    public BooleanMemory() {
	pairs = new Pair[0];
    }

    /**
     * Add the specified boolean to this memory object, keyed by the specified integer.
     */
    public synchronized void addValue(int key, boolean val) {
	Boolean value = new Boolean(val);
	addValue(key, value);
    }
   
    
    /**
     * Add the specified value to this memory object, keyed by the specified integer.
     */
    private synchronized void addValue(int key, Boolean val) {
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
    public synchronized void remValue(int key) {
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
     * Gets the boolean associated with the specified key from this memory object.
     */
    public synchronized boolean getBoolean(int key) {
	int index;
	for (index = 0; index < pairs.length; index++) {
	    if (pairs[index].key == key)
		return pairs[index].val.booleanValue();
	}
	throw new NoValueException("The Boolean with the specified key ("+key+") does not exist.");
    }

    public synchronized void setBoolean(int key, boolean val) {
	int index;
	//System.out.println("key: "+key);
	for (index = 0; index < pairs.length; index++) {
	    // System.out.println("pairs[index].key: "+pairs[index].key);
	    if (pairs[index].key == key) {
		pairs[index].val = new Boolean(val);
		return;
	    }
	}
 	throw new NoValueException("The Boolean with the specified key ("+key+") does not exist.");
   }

    private class Pair {
	Boolean val;
	int key;
	Pair(int key, Boolean val) {
	    this.key = key;
	    this.val = val;
	}
    }

}
