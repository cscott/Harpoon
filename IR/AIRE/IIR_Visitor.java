// IIR_Visitor.java, created Sat Oct 10 00:03:16 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.AIRE;

import harpoon.ClassFile.*;
/**
 * <code>IIR_Visitor</code>
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_Visitor.java,v 1.2 1998-10-11 02:37:25 cananian Exp $
 */

public abstract class IIR_Visitor  {
    public abstract void visit(IIR iir);
}
