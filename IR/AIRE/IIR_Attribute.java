// IIR_Attribute.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_Attribute</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_Attribute.java,v 1.3 1998-10-11 00:32:16 cananian Exp $
 */

//-----------------------------------------------------------
public abstract class IIR_Attribute extends IIR_Name
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    
    
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

