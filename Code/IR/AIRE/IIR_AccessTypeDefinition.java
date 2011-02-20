// IIR_AccessTypeDefinition.java, created by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.AIRE;

/**
 * <code>IIR_AccessTypeDefinition</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_AccessTypeDefinition.java,v 1.5 1998-10-11 02:37:10 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_AccessTypeDefinition extends IIR_TypeDefinition
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    /**
     * Returns the <code>IR_Kind</code> of this class (IR_ACCESS_TYPE_DEFINITION).
     * @return <code>IR_Kind.IR_ACCESS_TYPE_DEFINITION</code>
     */
    public IR_Kind get_kind()
    { return IR_Kind.IR_ACCESS_TYPE_DEFINITION; }
    
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

