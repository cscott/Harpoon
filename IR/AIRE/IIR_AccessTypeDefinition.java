// IIR_AccessTypeDefinition.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_AccessTypeDefinition</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_AccessTypeDefinition.java,v 1.2 1998-10-10 09:21:37 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_AccessTypeDefinition extends IIR_TypeDefinition
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_ACCESS_TYPE_DEFINITION
    
    //METHODS:  
    public IIR_AccessTypeDefinition get( IIR_TypeDefinition designated_type)
    { return new IIR_AccessTypeDefinition( designated_type ); }
 
    public void set_designated_type( IIR_TypeDefinition designated_type)
    { _designated_type = designated_type; }
 
    public IIR_TypeDefinition get_designated_type()
    { return _designated_type; }
 
    //MEMBERS:  

// PROTECTED:
    protected IIR_AccessTypeDefinition( IIR_TypeDefinition designated_type ) {
	_designated_type = designated_type;
    }
    IIR_TypeDefinition _designated_type;
} // END class

