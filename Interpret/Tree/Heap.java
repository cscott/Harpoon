package harpoon.Interpret.Tree;

import harpoon.Backend.Maps.OffsetMap;
import harpoon.IR.Tree.Type;
import harpoon.Temp.Label;
import harpoon.Util.Util;

import java.util.Hashtable;

/**
 * The <code>Heap</code> class is used to simulate a program's
 * heap storage.  
 *
 * @author   Duncan Bryce <duncan@lcs.mit.edu>
 * @version  $Id: Heap.java,v 1.1.2.1 1999-02-23 05:38:01 duncan Exp $
 */
public class Heap {
   
    private Hashtable l2a  = new Hashtable();
    private int[]     memory;
    private boolean   pointersAreLong;
    private OffsetMap offsetMap;
   

    /** Constructor. */
    Heap(int sizeOfMemory, boolean pointersAreLong, OffsetMap offsetMap) {
        this.memory          = new int[sizeOfMemory];
        this.pointersAreLong = pointersAreLong;
	this.offsetMap       = offsetMap;
    }

    /** Returns the address at the bottom of this head */
    int bottom() { throw new Error("Not implemented"); }

    /** Maps a label to an address in memory */
    void map(Label label, int address) { l2a.put(label, new Integer(address)); }

    /**
     * Returns the value at the specified address in memory. 
     */
    Object read(int address, int type) {
        Util.assert((address % 4)==0);  // access must be word-aligned
	
	address /= 4;  // Map address to index in our memory

        switch (type) 
	  {
	  case Type.INT: 
	    return new Integer(memory[address]);
	  case Type.LONG:
	    return new Long((long)memory[address] &
			    ((long)memory[address+1] << 32));
	  case Type.FLOAT:
	    return new Float(Float.intBitsToFloat(memory[address]));
	  case Type.DOUBLE:
	    return new Double(Double.longBitsToDouble
			      (((long)memory[address]) & 
			       ((long)memory[address+1] << 32)));
	  case Type.POINTER:
	    return pointersAreLong?
	      read(address, Type.LONG):read(address, Type.INT);

	  default:
	      throw new Error("Don't know how to read type: " + type);
	  }
    }
  
    /** Returns the address at the top of this heap */
    int top() { throw new Error("Not implemented"); }


    /**
     * Returns the value at the specified address in memory. 
     */
    Object read(Label label, int type) {
        if (!l2a.containsKey(label)) 
	  throw new InternalError("Don't know about memory location: " + label);
	else 
	  return read(((Integer)l2a.get(label)).intValue(), type);
    }

    /** 
     * Writes the data to the specified address in memory.
     */
    void write(Object data, int address, int type) {
      Util.assert((address % 4)==0); // access must be word-aligned
      
      address /= 4;  // Map address to index in our memory

      switch (type) 
	{
	case Type.INT:
	  int iData = ((Integer)data).intValue();
	  memory[address] = iData;
	  break;
	case Type.LONG:
	  long lData = ((Long)data).longValue();
	  memory[address]   = (int)lData;
	  memory[address+1] = (int)(lData >> 32);
	  break;
	case Type.FLOAT:
	  int fData = Float.floatToIntBits(((Float)data).floatValue());
	  memory[address]   = (int)fData;
	  break;
	case Type.DOUBLE:
	  long dData = Double.doubleToLongBits(((Double)data).floatValue());
	  memory[address]   = (int)dData;
	  memory[address+1] = (int)(dData >> 32);
	  break;
	case Type.POINTER:
	  if (pointersAreLong) write(data, address, Type.LONG);
	  else write(data, address, Type.INT);
	  break;
	default:
	  throw new Error("Don't know how to write type: " + type);
	}
    }

    /** 
     * Writes the data to the specified address in memory.
     */
    void write(Object data, Label label, int type) {
        if (!l2a.containsKey(label))
	    throw new InternalError("Don't know about memory location: " + label);
	else 
	    write(data, ((Integer)l2a.get(label)).intValue(), type);
    }
}
