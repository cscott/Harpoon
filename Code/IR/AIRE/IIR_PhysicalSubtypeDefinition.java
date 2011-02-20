// IIR_PhysicalSubtypeDefinition.java, created by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.AIRE;

import harpoon.Util.Tuple;
import java.util.Hashtable;
/**
 * The predefined <code>IIR_PhysicalSubtypeDefinition</code> class
 * represents a subtype of an existing physical type.  The subtype
 * range must be a subset of the base type's range.
 *
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_PhysicalSubtypeDefinition.java,v 1.5 1998-10-11 02:37:21 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_PhysicalSubtypeDefinition extends IIR_PhysicalTypeDefinition
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    /**
     * Returns the <code>IR_Kind</code> of this class (IR_PHYSICAL_SUBTYPE_DEFINITION).
     * @return <code>IR_Kind.IR_PHYSICAL_SUBTYPE_DEFINITION</code>
     */
    public IR_Kind get_kind()
    { return IR_Kind.IR_PHYSICAL_SUBTYPE_DEFINITION; }
    
    //METHODS:  
    public static IIR_PhysicalSubtypeDefinition get(IIR_PhysicalTypeDefinition base_type, IIR left_limit, IIR direction, IIR right_limit, IIR_FunctionDeclaration resolution_function) {
        Tuple t = new Tuple(new Object[] { base_type, left_limit, direction, right_limit, resolution_function } );
        IIR_PhysicalSubtypeDefinition ret = (IIR_PhysicalSubtypeDefinition) _h.get(t);
        if (ret==null) {
            ret = new IIR_PhysicalSubtypeDefinition(base_type, left_limit, direction, right_limit, resolution_function);
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

// PROTECTED:
    IIR_PhysicalTypeDefinition _base_type;
    IIR _left_limit;
    IIR _direction;
    IIR _right_limit;
    IIR_FunctionDeclaration _resolution_function;
    private IIR_PhysicalSubtypeDefinition(IIR_PhysicalTypeDefinition base_type, IIR left_limit, IIR direction, IIR right_limit, IIR_FunctionDeclaration resolution_function) {
        _base_type = base_type;
        _left_limit = left_limit;
        _direction = direction;
        _right_limit = right_limit;
        _resolution_function = resolution_function;
    }
    private static Hashtable _h = new Hashtable();
} // END class

