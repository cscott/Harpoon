// WritableTempMap.java, created Thu Dec 31 11:25:09 1998 by cananian
package harpoon.Temp;

/**
 * <code>WritableTempMap</code> is a mutable instance of a
 * <code>TempMap</code>.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: WritableTempMap.java,v 1.1.2.1 1999-01-03 03:01:45 cananian Exp $
 */
public interface WritableTempMap extends TempMap {
    /** Add a mapping from <code>Temp</code> <code>Told</code> to
     *  <code>Temp</code> <code>Tnew</code>. */
    public void associate(Temp Told, Temp Tnew);
}
