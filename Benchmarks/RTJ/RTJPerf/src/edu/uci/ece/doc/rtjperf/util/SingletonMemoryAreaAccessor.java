/*-------------------------------------------------------------------------*
 * $Id: SingletonMemoryAreaAccessor.java,v 1.1 2002-07-02 15:55:07 wbeebee Exp $
 *-------------------------------------------------------------------------*/
package edu.uci.ece.doc.rtjperf.util;

// -- RTJava Import --
import javax.realtime.MemoryArea;
import javax.realtime.HeapMemory;
import javax.realtime.ImmortalMemory;

/**
 * This is an utility class that provides the ability of obtaining
 * reference to the different types of memory areas by using a string
 * that represent the name of that memory area.
 *
 * @author <a href="mailto:corsaro@doc.ece.uci.edu">Angelo Corsaro</a>
 * @version 1.0
 */
public class SingletonMemoryAreaAccessor {

    public final static String IMMORTAL_MEMORY = "immortal";
    public final static String HEAP_MEMORY     = "heap";

    public static MemoryArea instance(String memoryAreaType) {
        if (memoryAreaType.equals(IMMORTAL_MEMORY)) {
            return ImmortalMemory.instance();
        }
        else if (memoryAreaType.equals(HEAP_MEMORY)) {
            return HeapMemory.instance();
        }

        return null;
    }
}

