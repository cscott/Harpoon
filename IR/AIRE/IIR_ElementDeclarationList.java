// IIR_ElementDeclarationList.java, created by cananian
package harpoon.IR.AIRE;

/**
 * The predefined <code>IIR_ElementDeclarationList</code> class represents
 * ordered sets containing zero or more <code>IIR_ElementDeclaration</code>s.
 * Element declaration lists appear as public data elements within
 * record type definitions.
 *
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_ElementDeclarationList.java,v 1.2 1998-10-11 00:32:19 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_ElementDeclarationList extends IIR_List
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    public IR_Kind get_kind()
    { return IR_Kind.IR_ELEMENT_DECLARATION_LIST; }
    //CONSTRUCTOR:
    public IIR_ElementDeclarationList() { }
    //METHODS:  
    public void prepend_element(IIR_ElementDeclaration element)
    { super._prepend_element(element); }
    public void append_element(IIR_ElementDeclaration element)
    { super._append_element(element); }
    public boolean 
	insert_after_element(IIR_ElementDeclaration existing_element,
			     IIR_ElementDeclaration new_element)
    { return super._insert_after_element(existing_element, new_element); }
    public boolean
	insert_before_element(IIR_ElementDeclaration existing_element,
			      IIR_ElementDeclaration new_element)
    { return super._insert_before_element(existing_element, new_element); }
    public boolean remove_element(IIR_ElementDeclaration existing_element)
    { return super._remove_element(existing_element); }
    public IIR_ElementDeclaration 
	get_successor_element(IIR_ElementDeclaration element)
    { return (IIR_ElementDeclaration)super._get_successor_element(element); }
    public IIR_ElementDeclaration 
	get_predecessor_element(IIR_ElementDeclaration element)
    { return (IIR_ElementDeclaration)super._get_predecessor_element(element); }
    public IIR_ElementDeclaration get_first_element()
    { return (IIR_ElementDeclaration)super._get_first_element(); }
    public IIR_ElementDeclaration get_nth_element(int index)
    { return (IIR_ElementDeclaration)super._get_nth_element(index); }
    public IIR_ElementDeclaration get_last_element()
    { return (IIR_ElementDeclaration)super._get_last_element(); }
    public int get_element_position(IIR_ElementDeclaration element)
    { return super._get_element_position(element); }
    //MEMBERS:  

// PROTECTED:
} // END class

