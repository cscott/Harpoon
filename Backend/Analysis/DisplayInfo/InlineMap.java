package harpoon.Backend.Analysis.DisplayInfo;

import harpoon.ClassFile.HField;

/**
 * An <code>InlineMap</code> maps an <code>HField</code> to a boolean value 
 * indicated whether the <code>HField</code> should be inlined.
 * 
 * @author  Duncan Bryce  <duncan@lcs.mit.edu>
 * @version 1.1.1.1
 */
public interface InlineMap
{
  /**
   * @return true if the <code>HField</code> should be inlined
   */
  public boolean shouldInline(HField);
}
