package harpoon.Backend.Analysis.DisplayInfo;

import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.HField;

/**
 * An <code>OffsetMap</code> maps an <code>HField</code> or an 
 * <code>HMethod</code> to an offset in bytes.  
 * 
 * @author  Duncan Bryce  <duncan@lcs.mit.edu>
 * @version 1.1.1.1
 */
public interface OffsetMap
{
  /** Maps an <code>HField</code> to an offset (in bytes) */
  public int offset(HField hf, InlineMap iMap);

  /** Maps an <code>HMethodM</code> to an offset (in bytes) */
  public int offset(HMethod hm);
}
