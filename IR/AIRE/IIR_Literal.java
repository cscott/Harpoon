// IIR_Literal.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_Literal</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_Literal.java,v 1.3 1998-10-11 00:32:22 cananian Exp $
 */

//-----------------------------------------------------------
public abstract class IIR_Literal extends IIR
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    
    
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

