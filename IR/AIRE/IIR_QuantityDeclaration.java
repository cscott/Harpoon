// IIR_QuantityDeclaration.java, created by cananian
package harpoon.IR.AIRE;

/**
 * The <code>IIR_QuantityDeclaration</code> class.
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_QuantityDeclaration.java,v 1.1 1998-10-10 07:53:40 cananian Exp $
 */

//-----------------------------------------------------------
public abstract class IIR_QuantityDeclaration extends IIR_ObjectDeclaration
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_QUANTITY_DECLARATION
    
    
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

