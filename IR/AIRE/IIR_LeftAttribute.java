// IIR_LeftAttribute.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_LeftAttribute</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_LeftAttribute.java,v 1.1 1998-10-10 07:53:37 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_LeftAttribute extends IIR_Attribute
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_LEFT_ATTRIBUTE
    //CONSTRUCTOR:
    public IIR_LeftAttribute() { }

    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

