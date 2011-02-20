// IIR_IntegerSubtypeDefinition.java, created by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.AIRE;

import harpoon.Util.Tuple;
import java.util.Hashtable;
/**
 * The predefined <code>IIR_IntegerSubtypeDefinition</code> class
 * represents a subset of an existing integer base type definition.
 *
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_IntegerSubtypeDefinition.java,v 1.6 1998-10-11 02:37:19 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_IntegerSubtypeDefinition extends IIR_IntegerTypeDefinition
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    /**
     * Returns the <code>IR_Kind</code> of this class (IR_INTEGER_SUBTYPE_DEFINITION).
     * @return <code>IR_Kind.IR_INTEGER_SUBTYPE_DEFINITION</code>
     */
    public IR_Kind get_kind()
    { return IR_Kind.IR_INTEGER_SUBTYPE_DEFINITION; }
    
    
    //METHODS:  
    public static IIR_IntegerSubtypeDefinition get(IIR_IntegerTypeDefinition base_type, IIR left_limit, IIR direction, IIR right_limit, IIR_FunctionDeclaration resolution_function) {
        Tuple t = new Tuple(new Object[] { base_type, left_limit, direction, right_limit, resolution_function } );
        IIR_IntegerSubtypeDefinition ret = (IIR_IntegerSubtypeDefinition) _h.get(t);
        if (ret==null) {
            ret = new IIR_IntegerSubtypeDefinition(base_type, left_limit, direction, right_limit, resolution_function);
            _h.put(t, ret);
        }
        return ret;
    }
 
    // FIXME: set_base_type modifies entry in _h
    public void set_base_type(IIR_IntegerTypeDefinition base_type)
    { super.set_base_type(base_type); }
    /*
    { _base_type = base_type; }
    public IIR_IntegerTypeDefinition get_base_type()
    { return _base_type; }
    */
 
    public void set_resolution_function(IIR_FunctionDeclaration resolution_function)
    { _resolution_function = resolution_function; }
 
    public IIR_FunctionDeclaration get_resolution_function()
    { return _resolution_function; }
 
    public void release() { /* do nothing. */ }
 
    //MEMBERS:  

// PROTECTED:
    IIR_IntegerTypeDefinition _base_type;
    IIR _left_limit;
    IIR _direction;
    IIR _right_limit;
    IIR_FunctionDeclaration _resolution_function;
    private IIR_IntegerSubtypeDefinition(IIR_IntegerTypeDefinition base_type, IIR left_limit, IIR direction, IIR right_limit, IIR_FunctionDeclaration resolution_function) {
        _base_type = base_type;
        _left_limit = left_limit;
        _direction = direction;
        _right_limit = right_limit;
        _resolution_function = resolution_function;
    }
    private static Hashtable _h = new Hashtable();
} // END class

