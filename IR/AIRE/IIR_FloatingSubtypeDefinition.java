// IIR_FloatingSubtypeDefinition.java, created by cananian
package harpoon.IR.AIRE;

/**
 * The predefined <code>IIR_FloatingSubtypeDefinition</code> class
 * represents a subset of an existing floating base type definition.
 *
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_FloatingSubtypeDefinition.java,v 1.1 1998-10-10 07:53:36 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_FloatingSubtypeDefinition extends IIR_FloatingTypeDefinition
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IIR_FLOATING_SUBTYPE_DEFINITION
    
    
    //METHODS:  
    static IIR_FloatingSubtypeDefinition 
	get( IIR_FloatingTypeDefinition base_type, 
	     IIR left_limit, IIR direction, IIR right_limit, 
	     IIR_FunctionDeclaration resolution_function) {
	return new IIR_FloatingSubtypeDefinition(base_type, left_limit,
						 direction, right_limit,
						 resolution_function);
    }
 
    public void set_base_type( IIR_FloatingTypeDefintion base_type )
    { _base_type = base_type; }
    public IIR_FloatingTypeDefinition get_base_type()
    { return _base_type; }

    public void set_resolution_function(IIR_FunctionDeclaration resolution_function)
    { _resolution_function = resolution_function; }
 
    public IIR_FunctionDeclaration get_resolution_function()
    { return _resolution_function; }
 
    public void release() { /* do nothing. */ }
 
    //MEMBERS:  

// PROTECTED:
    IIR_FloatingSubtypeDefinition(IIR_FloatingTypeDefinition base_type, 
				  IIR left_limit, IIR direction, 
				  IIR right_limit, 
				  IIR_FunctionDeclaration resolution_function)
    {
	_base_type = base_type;
	_left_limit = left_limit;
	_direction = direction;
	_right_limit = right_limit;
	_resolution_function = resolution_function;
    }
    IIR_FloatingTypeDefinition _base_type;
    IIR _left_limit, _direction, _right_limit;
    IIR_FunctionDeclaration _resolution_function;
} // END class

