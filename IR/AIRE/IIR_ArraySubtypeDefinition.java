// IIR_ArraySubtypeDefinition.java, created by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.AIRE;

import harpoon.Util.Tuple;
import java.util.Hashtable;
/**
 * The predefined <code>IIR_ArraySubtypeDefinition</code> class represents
 * the subtype of a pre-existing array type definition; always a constrained
 * step in an array type definition.  This subtype has an array domain which
 * is a subset of the base type's domain.
 *
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_ArraySubtypeDefinition.java,v 1.6 1998-10-11 02:37:12 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_ArraySubtypeDefinition extends IIR_ArrayTypeDefinition
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    /**
     * Returns the <code>IR_Kind</code> of this class (IR_ARRAY_SUBTYPE_DEFINITION).
     * @return <code>IR_Kind.IR_ARRAY_SUBTYPE_DEFINITION</code>
     */
    public IR_Kind get_kind()
    { return IR_Kind.IR_ARRAY_SUBTYPE_DEFINITION; }
    
    
    //METHODS:  
    public static IIR_ArraySubtypeDefinition get(IIR_ArrayTypeDefinition base_type, IIR_ScalarTypeDefinition index_subtype, IIR_FunctionDeclaration resolution_function) {
        Tuple t = new Tuple(new Object[] { base_type, index_subtype, resolution_function } );
        IIR_ArraySubtypeDefinition ret = (IIR_ArraySubtypeDefinition) _h.get(t);
        if (ret==null) {
            ret = new IIR_ArraySubtypeDefinition(base_type, index_subtype, resolution_function);
            _h.put(t, ret);
        }
        return ret;
    }
 
    // FIXME: changing resolution function changes entry in _h.
    public void set_resolution_function(IIR_FunctionDeclaration 
					resolution_function) {
	_resolution_function = resolution_function;
    }
 
    public IIR_FunctionDeclaration get_resolution_function()
    { return _resolution_function; }
 
    public void release() { /* do nothing. */ }
 
    //MEMBERS:  

// PROTECTED:
    IIR_ArrayTypeDefinition _base_type;
    IIR_ScalarTypeDefinition _index_subtype;
    IIR_FunctionDeclaration _resolution_function;
    private IIR_ArraySubtypeDefinition(IIR_ArrayTypeDefinition base_type, IIR_ScalarTypeDefinition index_subtype, IIR_FunctionDeclaration resolution_function) {
        _base_type = base_type;
        _index_subtype = index_subtype;
        _resolution_function = resolution_function;
    }
    private static Hashtable _h = new Hashtable();
} // END class

