// IIR_CompositeNatureDefinition.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_CompositeNatureDefinition</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_CompositeNatureDefinition.java,v 1.4 1998-10-11 01:24:54 cananian Exp $
 */

//-----------------------------------------------------------
public abstract class IIR_CompositeNatureDefinition extends IIR_NatureDefinition
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    
    
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

