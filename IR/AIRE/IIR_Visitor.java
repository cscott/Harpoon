// IIR_Visitor.java, created Sat Oct 10 00:03:16 1998 by cananian
package harpoon.IR.AIRE;

import harpoon.ClassFile.*;
/**
 * <code>IIR_Visitor</code>
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_Visitor.java,v 1.1 1998-10-10 07:53:46 cananian Exp $
 */

public abstract class IIR_Visitor  {
    public abstract void visit(IIR iir);
}
