// IIR_ConfigurationItem.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_ConfigurationItem</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_ConfigurationItem.java,v 1.4 1998-10-11 01:24:55 cananian Exp $
 */

//-----------------------------------------------------------
public abstract class IIR_ConfigurationItem extends IIR_Tuple
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    
    
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

