// IIR_Allocator.java, created by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.AIRE;

/**
 * The predefined <code>IIR_Allocator</code> class dynamically
 * allocates an object of specified subtype.
 *
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_Allocator.java,v 1.4 1998-10-11 02:37:12 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_Allocator extends IIR_Expression
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    /**
     * Returns the <code>IR_Kind</code> of this class (IR_ALLOCATOR).
     * @return <code>IR_Kind.IR_ALLOCATOR</code>
     */
    public IR_Kind get_kind()
    { return IR_Kind.IR_ALLOCATOR; }
    //CONSTRUCTOR:
    /** The constructor initializes an allocated object. */
    public IIR_Allocator() { }
    //METHODS: 
    /** Type mark methods denote the subtype of the object to be allocated.*/
    public void set_type_mark(IIR_TypeDefinition type_mark)
    { _type_mark = type_mark; }
 
    /** Type mark methods denote the subtype of the object to be allocated.*/
    public IIR_TypeDefinition get_type_mark()
    { return _type_mark; }
 
    public void set_value(IIR value)
    { _value = value; }
 
    public IIR get_value()
    { return _value; }
 
    //MEMBERS:  

// PROTECTED:
    IIR_TypeDefinition _type_mark;
    IIR _value;
} // END class

