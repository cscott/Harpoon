// TempMap.java, created Sat Sep 12 21:13:23 1998 by cananian
package harpoon.Temp;

/**
 * A <code>TempMap</code> maps one temp to another temp.  It is typically
 * used to represent a set of variable renamings.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: TempMap.java,v 1.1 1998-09-13 23:57:33 cananian Exp $
 */

public interface TempMap  {
    public Temp tempMap(Temp t);
}
