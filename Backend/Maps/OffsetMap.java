package harpoon.Backend.Maps;

import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.HField;
import harpoon.ClassFile.HClass;

import harpoon.Temp.Label;

/**
 * An <code>OffsetMap</code> maps an <code>HField</code> or an 
 * <code>HMethod</code> to an offset in bytes.  It also reports the
 * total size of an <code>HClass</code> object.
 * 
 * @author  Duncan Bryce  <duncan@lcs.mit.edu>
 * @version $Id: OffsetMap.java,v 1.1.2.8 1999-01-28 18:56:10 duncan Exp $
 */
public abstract class OffsetMap // use an abstract class, if we can.
{
  /** Maps a non-static <code>HField</code> to an offset (in bytes).
   *  If the field is inlined using type 1 inlining (which preserves
   *  the class pointer) then the specified offset points just after the
   *  class descriptor, in the same place a normal object pointer points.
   *  If the field in inlined using type 2 inlining (which omits the
   *  class pointer) then the specified offset points to the first field
   *  of the object. */
  public abstract int offset(HField hf);

  /** Maps a non-static <code>HMethod</code> to an offset (in bytes).
   *  This method must work for interface methods as well as class methods. */
  public abstract int offset(HMethod hm);

  /** Maps a static <code>HField</code> to a <code>Label</code>. */
  public abstract Label label(HField hf);

  /** Maps an <code>HMethod</code> to a <code>Label</code>. Note that
   *  the method does not have to be static or final; in many cases we
   *  can determine the identity of a virtual function exactly using 
   *  type information, and <code>label()</code> should return a
   *  <code>Label</code> we can use to take advantage of this information. */
  public abstract Label label(HMethod hf);

  /** Maps an <code>HClass</code> to a size (in bytes). */
  public abstract int size(HClass hc);

  /** Maps an <code>HClass</code> to an offset (in bytes).  
   *  Returns the offset of the class pointer
  public abstract int classOffset(HClass hc);

  /** Maps an <code>HClass</code> to an offset (in bytes).
   *  Returns the offset from the class pointer of the specified
   *  class.  This will be some function of the class's depth in
   *  the class hierarchy. */
  public abstract int displayOffset(HClass hc);

  /** Maps an <code>HClass</code> to an offset (in bytes).  
   *  If hc is an array type, returns the offset of the
   *  array's 0th element. */
  public abstract int elementsOffset(HClass hc);

  /** Maps an <code>HClass</code> to an offset (in bytes).  
   *  If <code>hc</code> is an array type, returns the offset of the
   *  array's length field. */
  public abstract int lengthOffset(HClass hc); 

}



