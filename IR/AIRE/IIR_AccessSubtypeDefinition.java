// IIR_AccessSubtypeDefinition.java, created by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.AIRE;

import harpoon.Util.Tuple;
import java.util.Hashtable;
/**
 * The predefined <code>IIR_AccessSubtypeDefinition</code> class represents
 * a subtype of an access type definition.
 *
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_AccessSubtypeDefinition.java,v 1.4 1998-10-11 02:37:10 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_AccessSubtypeDefinition extends IIR_AccessTypeDefinition
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    /**
     * Returns the <code>IR_Kind</code> of this class (IR_ACCESS_SUBTYPE_DEFINITION).
     * @return <code>IR_Kind.IR_ACCESS_SUBTYPE_DEFINITION</code>
     */
    public IR_Kind get_kind()
    { return IR_Kind.IR_ACCESS_SUBTYPE_DEFINITION; }
    
    //METHODS:  
    public static IIR_AccessSubtypeDefinition get(IIR_TypeDefinition designated_type, IIR_FunctionDeclaration resolution_function) {
        Tuple t = new Tuple(new Object[] { designated_type,
					   resolution_function } );
        IIR_AccessSubtypeDefinition ret = 
	    (IIR_AccessSubtypeDefinition) _h.get(t);
        if (ret==null) {
            ret = new IIR_AccessSubtypeDefinition(designated_type,
						  resolution_function);
            _h.put(t, ret);
        }
        return ret;
    }
 
    public void set_designated_subtype( IIR_TypeDefinition designated_subtype)
    { _designated_subtype = designated_subtype; }
 
    public IIR_TypeDefinition get_designated_subtype()
    { return _designated_subtype; }
 
    public void set_resolution_function(IIR_FunctionDeclaration 
					resolution_function)
    { _resolution_function = resolution_function; }
 
    public IIR_FunctionDeclaration get_resolution_function()
    { return _resolution_function; }
 
    public void release() { /* do nothing. */ }
 
    //MEMBERS:  

// PROTECTED:

    IIR_TypeDefinition _designated_subtype;
    IIR_FunctionDeclaration _resolution_function;
    private IIR_AccessSubtypeDefinition(IIR_TypeDefinition designated_type,
			         IIR_FunctionDeclaration resolution_function) {
	super(null); // FIXME
        _designated_subtype = designated_type;
        _resolution_function = resolution_function;
	throw new Error("unimplemented."); // FIXME
    }
    private static Hashtable _h = new Hashtable();
} // END class

