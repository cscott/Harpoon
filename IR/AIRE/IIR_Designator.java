// IIR_Designator.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_Designator</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_Designator.java,v 1.4 1998-10-11 00:32:18 cananian Exp $
 */

//-----------------------------------------------------------
public abstract class IIR_Designator extends IIR_Tuple
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    
    
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

