// IIR_Designator.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_Designator</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_Designator.java,v 1.5 1998-10-11 01:24:55 cananian Exp $
 */

//-----------------------------------------------------------
public abstract class IIR_Designator extends IIR_Tuple
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    
    
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

