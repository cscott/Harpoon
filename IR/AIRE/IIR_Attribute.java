// IIR_Attribute.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_Attribute</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_Attribute.java,v 1.2 1998-10-10 09:58:34 cananian Exp $
 */

//-----------------------------------------------------------
public abstract class IIR_Attribute extends IIR_Name
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = 
    
    
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

