// IIR_ZOHAttribute.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_ZOHAttribute</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_ZOHAttribute.java,v 1.1 1998-10-10 07:53:46 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_ZOHAttribute extends IIR_Attribute
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_ZOH_ATTRIBUTE
    //CONSTRUCTOR:
    public IIR_ZOHAttribute() { }

    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

