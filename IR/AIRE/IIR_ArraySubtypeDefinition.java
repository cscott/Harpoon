// IIR_ArraySubtypeDefinition.java, created by cananian
package harpoon.IR.AIRE;

/**
 * The predefined <code>IIR_ArraySubtypeDefinition</code> class represents
 * the subtype of a pre-existing array type definition; always a constrained
 * step in an array type definition.  This subtype has an array domain which
 * is a subset of the base type's domain.
 *
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_ArraySubtypeDefinition.java,v 1.1 1998-10-10 07:53:32 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_ArraySubtypeDefinition extends IIR_ArrayTypeDefinition
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_ARRAY_SUBTYPE_DEFINITION
    
    
    //METHODS:  
    static IIR_ArraySubtypeDefinition 
	get(IIR_ArrayTypeDefinition base_type,
	    IIR_ScalarTypeDefinition index_subtype, 
	    IIR_FunctionDeclaration resolution_function) {
	return new IIR_ArraySubtypeDefinition(base_type, index_subtype,
					      resolution_function);
    }
 
    public void set_resolution_function(IIR_FunctionDeclaration 
					resolution_function) {
	_resolution_function = resolution_function;
    }
 
    public IIR_FunctionDeclaration get_resolution_function()
    { return _resolution_function; }
 
    public void release() { /* do nothing. */ }
 
    //MEMBERS:  

// PROTECTED:
    IIR_ArraySubtypeDefinition(IIR_ArrayTypeDefinition base_type,
			       IIR_ScalarTypeDefinition index_subtype,
			       IIR_FunctionDeclaration resolution_function) {
	_base_type = base_type;
	_index_subtype = index_subtype;
	_resolution_function = resolution_function;
    }
    IIR_ArrayTypeDeclaration _base_type;
    IIR_ScalarTypeDefinition _index_subtype;
    IIR_FunctionDeclaration _resolution_function;
} // END class

