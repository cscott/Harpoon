// IIR_ZTFAttribute.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_ZTFAttribute</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_ZTFAttribute.java,v 1.1 1998-10-10 07:53:46 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_ZTFAttribute extends IIR_Attribute
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_ZTF_ATTRIBUTE
    //CONSTRUCTOR:
    public IIR_ZTFAttribute() { }

    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

