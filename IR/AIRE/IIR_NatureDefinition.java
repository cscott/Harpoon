// IIR_NatureDefinition.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_NatureDefinition</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_NatureDefinition.java,v 1.2 1998-10-10 09:58:35 cananian Exp $
 */

//-----------------------------------------------------------
public abstract class IIR_NatureDefinition extends IIR_TypeDefinition
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_NATURE_DEFINITION
    
    
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

