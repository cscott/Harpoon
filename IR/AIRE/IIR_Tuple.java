// IIR_Tuple.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_Tuple</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_Tuple.java,v 1.3 1998-10-11 00:32:28 cananian Exp $
 */

//-----------------------------------------------------------
public abstract class IIR_Tuple extends IIR
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    
    
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

