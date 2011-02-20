// NoValueException.java, created by benster
// Copyright (C) 2003 Reuben Sterling <benster@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package imagerec.util;

/**
 * {@link RuntimeException} used by the {@link CommonMemory} class.
 */
public class NoValueException extends RuntimeException {
    public NoValueException(String message) {
	super(message);
    }
}
