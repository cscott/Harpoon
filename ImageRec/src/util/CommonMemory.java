// CommonMemory.java, created by benster
// Copyright (C) 2003 Reuben Sterling <benster@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package imagerec.util;

//needed so that javadoc can find it
import imagerec.graph.Node;

/**
 * {@link CommonMemory} implements a utility that allows
 * communication between {@link Node}s that are typically
 * isolated.<br><br>
 * 
 * Items in memory are keyed by a string.<br>
 * All methods may be referenced statically.<br>
 * All methods are synchronized.<br><br>
 *
 * Currently inefficient for large amounts variables.
 * Search time is linear in # of variables stored.
 * Also very inefficient for large numbers of additions/removals
 * of variables, since internal array is resized every time.
 * 
 * @author Reuben Sterling <<a href="mailto:benster@mit.edu">benster@mit.edu</a>>
 */
public class CommonMemory {
    /**
     * Stores all key-value pairs.
     */
    private static Pair[] pairs = new Pair[0];

    /**
     * Sets a variable to the specified value.
     * If the key is already bound to a value, that value
     * is replaced. If the key does not exist, then a new
     * key-value pair is created.
     *
     * @param key The name of the variable to modify or add.
     * @param value The value to set the specified variable to.
     */
    public static synchronized void setValue(String key, Object value) {
	int index;
	for (index = 0; index < pairs.length; index++) {
	    if (pairs[index].key.equals(key)) {
		pairs[index].value = value;
		return;
	    }
	}
	Pair newPair = new Pair(key, value);
	int size = pairs.length;
	Pair[] newPairs = new Pair[size+1];
	System.arraycopy(pairs, 0, newPairs, 0, size);
	newPairs[size] = newPair;
	pairs = newPairs;
	if (size == 100) {
	    System.out.println("CommonMemory: WARNING: more than 100 items in common memory");
	}
    }

    /**
     * Removes the specified variable from memory.
     * If the variable does not exist, then an exception is thrown.
     * 
     * @param key The variable to be removed.
     *
     * @throws NoValueException {@link RuntimeException} thrown
     * if the specified variable does not exist.
     */
    public static synchronized void remValue(String key) {
	int index;
	for (index = 0; index < pairs.length; index++) {
	    if (pairs[index].key.equals(key))
		break;
	}
	//if found nothing, then throw exception
	if (index == pairs.length) {
	    throw new NoValueException("The value with the specified key ("+key+") does not exist.");
	}
	Pair[] newPairs = new Pair[pairs.length-1];
	System.arraycopy(pairs, 0, newPairs, 0, index);
	System.arraycopy(pairs, index+1, newPairs, index, newPairs.length - index);
	pairs = newPairs;
	return;	
    }

    /**
     * Determines whether the specified variable exists in the common memory.
     *
     * @param key The variable to check for existence.
     *
     * @return true If the variable exists in memory.
     * @return false If the variable does not exist in memory.
     */
    public static synchronized boolean valueExists(String key) {
	int index;
	for (index = 0; index < pairs.length; index++) {
	    if (pairs[index].key.equals(key)) {
		return true;
	    }
	}
	return false;
    }

    /**
     * Returns the {@link Object} value that is bound to the
     * specified variable.
     *
     * @param key The variable whose value is to be returned.
     *
     * @throws NoValueException {@link RuntimeException} thrown
     * if the specified variable does not exist.
     */
    public static synchronized Object getValue(String key) {
	int index;
	for (index = 0; index < pairs.length; index++) {
	    if (pairs[index].key.equals(key)) {
		return pairs[index].value;
	    }
	}
	throw new NoValueException("The Object with the specified key ("+key+") does not exist.");
    }

    /**
     * Private internal class that stores key-value bindings.
     */
    private static class Pair {
	Object value;
	String key;
	Pair(String key, Object value) {
	    this.key = key;
	    this.value = value;
	}
    }

}
