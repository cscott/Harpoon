// IIR_TypeDefinition.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_TypeDefinition</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_TypeDefinition.java,v 1.1 1998-10-10 07:53:45 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_TypeDefinition extends IIR
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = 
    
    
    //METHODS:  
    public void set_base_type(IIR_TypeDefinition base_type)
    { _base_type = base_type; }
 
    public IIR_TypeDefinition get_base_type()
    { return _base_type; }
 
    //MEMBERS:  

// PROTECTED:
    IIR_TypeDefinition _base_type;
} // END class

