// IIR_Attribute.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_Attribute</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_Attribute.java,v 1.4 1998-10-11 01:24:53 cananian Exp $
 */

//-----------------------------------------------------------
public abstract class IIR_Attribute extends IIR_Name
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    
    
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

