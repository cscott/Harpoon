// IIR_NatureDefinition.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_NatureDefinition</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_NatureDefinition.java,v 1.3 1998-10-11 00:32:22 cananian Exp $
 */

//-----------------------------------------------------------
public abstract class IIR_NatureDefinition extends IIR_TypeDefinition
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    
    
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

