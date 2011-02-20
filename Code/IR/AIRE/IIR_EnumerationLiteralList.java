// IIR_EnumerationLiteralList.java, created by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.AIRE;

/**
 * The predefined <code>IIR_EnumerationLiteralList</code> class represents
 * ordered sets containing zero or more <code>IIR_EnumerationLiteral</code>s.
 * Enumeration literal lists are found as predefined public data elements
 * within <code>IIR_EnumerationTypeDefinition</code> and
 * <code>IIR_EnumerationSubtypeDefintion</code> classes.
 *
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_EnumerationLiteralList.java,v 1.4 1998-10-11 02:37:17 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_EnumerationLiteralList extends IIR_List
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    /**
     * Returns the <code>IR_Kind</code> of this class (IR_ENUMERATION_LITERAL_LIST).
     * @return <code>IR_Kind.IR_ENUMERATION_LITERAL_LIST</code>
     */
    public IR_Kind get_kind()
    { return IR_Kind.IR_ENUMERATION_LITERAL_LIST; }
    //CONSTRUCTOR:
    public IIR_EnumerationLiteralList() { }
    //METHODS:  
    public void prepend_element(IIR_EnumerationLiteral element)
    { super._prepend_element(element); }
    public void append_element(IIR_EnumerationLiteral element)
    { super._append_element(element); }
    public boolean 
	insert_after_element(IIR_EnumerationLiteral existing_element,
			     IIR_EnumerationLiteral new_element)
    { return super._insert_after_element(existing_element, new_element); }
    public boolean
	insert_before_element(IIR_EnumerationLiteral existing_element,
			      IIR_EnumerationLiteral new_element)
    { return super._insert_before_element(existing_element, new_element); }
    public boolean remove_element(IIR_EnumerationLiteral existing_element)
    { return super._remove_element(existing_element); }
    public IIR_EnumerationLiteral 
	get_successor_element(IIR_EnumerationLiteral element)
    { return (IIR_EnumerationLiteral)super._get_successor_element(element); }
    public IIR_EnumerationLiteral 
	get_predecessor_element(IIR_EnumerationLiteral element)
    { return (IIR_EnumerationLiteral)super._get_predecessor_element(element); }
    public IIR_EnumerationLiteral get_first_element()
    { return (IIR_EnumerationLiteral)super._get_first_element(); }
    public IIR_EnumerationLiteral get_nth_element(int index)
    { return (IIR_EnumerationLiteral)super._get_nth_element(index); }
    public IIR_EnumerationLiteral get_last_element()
    { return (IIR_EnumerationLiteral)super._get_last_element(); }
    public int get_element_position(IIR_EnumerationLiteral element)
    { return super._get_element_position(element); }
    //MEMBERS:  

// PROTECTED:
} // END class

