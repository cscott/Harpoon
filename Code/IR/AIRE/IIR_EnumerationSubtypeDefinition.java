// IIR_EnumerationSubtypeDefinition.java, created by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.AIRE;

import harpoon.Util.Tuple;
import java.util.Hashtable;
/**
 * The predefined <code>IIR_EnumerationSubtypeDefinition</code> class
 * represents a subset of the literals represented by an
 * <code>IIR_EnumerationTypeDefinition</code> base type.
 *
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_EnumerationSubtypeDefinition.java,v 1.7 1998-10-11 02:37:18 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_EnumerationSubtypeDefinition extends IIR_EnumerationTypeDefinition
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    /**
     * Returns the <code>IR_Kind</code> of this class (IR_ENUMERATION_SUBTYPE_DEFINITION).
     * @return <code>IR_Kind.IR_ENUMERATION_SUBTYPE_DEFINITION</code>
     */
    public IR_Kind get_kind()
    { return IR_Kind.IR_ENUMERATION_SUBTYPE_DEFINITION; }
    
    
    //METHODS:  
    public static IIR_EnumerationSubtypeDefinition get(IIR_EnumerationTypeDefinition base_type, IIR_EnumerationLiteral left_limit, IIR_EnumerationLiteral right_limit, IIR_FunctionDeclaration resolution_function) {
        Tuple t = new Tuple(new Object[] { base_type, left_limit, right_limit, resolution_function } );
        IIR_EnumerationSubtypeDefinition ret = (IIR_EnumerationSubtypeDefinition) _h.get(t);
        if (ret==null) {
            ret = new IIR_EnumerationSubtypeDefinition(base_type, left_limit, right_limit, resolution_function);
            _h.put(t, ret);
        }
        return ret;
    }
 
    // FIXME: set_resolution_function changes entry in _h
    public void set_resolution_function(IIR_FunctionDeclaration resolution_function)
    { _resolution_function = resolution_function; }
 
    public IIR_FunctionDeclaration get_resolution_function()
    { return _resolution_function; }
 
    public void release() { /* do nothing */ }
 
    //MEMBERS:  
	// GETS ENUMERATION_LITERALS FROM PARENT_CLASS. FIXME?
	//public IIR_EnumerationLiteralList enumeration_literals;

// PROTECTED:
    IIR_EnumerationTypeDefinition _base_type;
    IIR_EnumerationLiteral _left_limit;
    IIR_EnumerationLiteral _right_limit;
    IIR_FunctionDeclaration _resolution_function;
    private IIR_EnumerationSubtypeDefinition(IIR_EnumerationTypeDefinition base_type, IIR_EnumerationLiteral left_limit, IIR_EnumerationLiteral right_limit, IIR_FunctionDeclaration resolution_function) {
        _base_type = base_type;
        _left_limit = left_limit;
        _right_limit = right_limit;
        _resolution_function = resolution_function;
    }
    private static Hashtable _h = new Hashtable();
} // END class

