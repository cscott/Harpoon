// IIR_QuantityDeclaration.java, created by cananian
package harpoon.IR.AIRE;

/**
 * The predefined <code>IIR_QuantityDeclaration</code> class.
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_QuantityDeclaration.java,v 1.3 1998-10-11 01:25:00 cananian Exp $
 */

//-----------------------------------------------------------
public abstract class IIR_QuantityDeclaration extends IIR_ObjectDeclaration
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

