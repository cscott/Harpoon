// IIR_DotAttribute.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_DotAttribute</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_DotAttribute.java,v 1.1 1998-10-10 07:53:35 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_DotAttribute extends IIR_Attribute
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_DOT_ATTRIBUTE
    //CONSTRUCTOR:
    public IIR_DotAttribute( ) { }
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

