// DefaultInlineMap.java, created Sat Jan 16 20:53:59 1999 by cananian
package harpoon.Backend.Maps;

import harpoon.ClassFile.HField;

/**
 * A <code>DefaultInlineMap</code> returns the conservate answer that we
 * can't inline anything.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: DefaultInlineMap.java,v 1.1.2.1 1999-01-17 02:51:28 cananian Exp $
 */
public final class DefaultInlineMap extends InlineMap {
    /** Creates a <code>DefaultInlineMap</code>. Doesn't need any 
     *  arguments to the constructor, because it is very stupid. */
    public DefaultInlineMap() {
	// throw a party.
    }
    public boolean canInline1(HField hf) { return false; }
    public boolean canInline2(HField hf) { return false; }
}
