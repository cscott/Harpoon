// IIR_NatureDefinition.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_NatureDefinition</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_NatureDefinition.java,v 1.4 1998-10-11 01:24:59 cananian Exp $
 */

//-----------------------------------------------------------
public abstract class IIR_NatureDefinition extends IIR_TypeDefinition
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    
    
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

