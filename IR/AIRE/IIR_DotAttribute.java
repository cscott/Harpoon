// IIR_DotAttribute.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_DotAttribute</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_DotAttribute.java,v 1.2 1998-10-11 00:32:19 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_DotAttribute extends IIR_Attribute
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    public IR_Kind get_kind()
    { return IR_Kind.IR_DOT_ATTRIBUTE; }
    //CONSTRUCTOR:
    public IIR_DotAttribute( ) { }
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

