package harpoon.Backend.Maps;

import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.HField;
import harpoon.ClassFile.HClass;

/**
 * An <code>OffsetMap</code> maps an <code>HField</code> or an 
 * <code>HMethod</code> to an offset in bytes.  
 * 
 * @author  Duncan Bryce  <duncan@lcs.mit.edu>
 * @version 1.1.2.1
 */
public interface OffsetMap
{
  /** Maps an <code>HField</code> to an offset (in bytes) */
  public int offset(HField hf, InlineMap iMap);

  /** Maps an <code>HMethod</code> to an offset (in bytes) */
  public int offset(HMethod hm);

  /** Maps an <code>HClass</code> to a size (in bytes) */
  public int size(HClass hc);
}
