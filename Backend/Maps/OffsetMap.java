package harpoon.Backend.Maps;

import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.HField;
import harpoon.ClassFile.HClass;

/**
 * An <code>OffsetMap</code> maps an <code>HField</code> or an 
 * <code>HMethod</code> to an offset in bytes.  It also reports the
 * total size of an <code>HClass</code> object.
 * 
 * @author  Duncan Bryce  <duncan@lcs.mit.edu>
 * @version $Id: OffsetMap.java,v 1.1.2.4 1999-01-17 02:19:09 cananian Exp $
 */
public abstract class OffsetMap // use an abstract class, if we can.
{
  /** Maps an <code>HField</code> to an offset (in bytes).
   *  If the field is inlined using type 1 inlining (which preserves
   *  the class pointer) then the specified offset points just after the
   *  class descriptor, in the same place a normal object pointer points.
   *  If the field in inlined using type 2 inlining (which omits the
   *  class pointer) then the specified offset points to the first field
   *  of the object. */
  public abstract int offset(HField hf);

  /** Maps an <code>HMethod</code> to an offset (in bytes).
   *  This method must work for interface methods as well as class methods. */
  public abstract int offset(HMethod hm);

  /** Maps an <code>HClass</code> to a size (in bytes) */
  public abstract int size(HClass hc);
}
