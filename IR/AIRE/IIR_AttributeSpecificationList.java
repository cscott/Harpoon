// IIR_AttributeSpecificationList.java, created by cananian
package harpoon.IR.AIRE;

/**
 * The <code>IIR_AttributeSpecificationList</code> class represents
 * ordered sets containing zero or more 
 * <code>IIR_AttributeSpecification</code>s.
 *
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_AttributeSpecificationList.java,v 1.2 1998-10-11 00:32:16 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_AttributeSpecificationList extends IIR_List
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    public IR_Kind get_kind()
    { return IR_Kind.IR_ATTRIBUTE_SPECIFICATION_LIST; }
    //CONSTRUCTOR:
    public IIR_AttributeSpecificationList() { }
    //METHODS:  
    public void prepend_element(IIR_AttributeSpecification element)
    { super._prepend_element(element); }
    public void append_element(IIR_AttributeSpecification element)
    { super._append_element(element); }
    public boolean 
	insert_after_element(IIR_AttributeSpecification existing_element,
			     IIR_AttributeSpecification new_element)
    { return super._insert_after_element(existing_element, new_element); }
    public boolean
	insert_before_element(IIR_AttributeSpecification existing_element,
			      IIR_AttributeSpecification new_element)
    { return super._insert_before_element(existing_element, new_element); }
    public boolean remove_element(IIR_AttributeSpecification existing_element)
    { return super._remove_element(existing_element); }
    public IIR_AttributeSpecification 
	get_successor_element(IIR_AttributeSpecification element)
    { return (IIR_AttributeSpecification)super._get_successor_element(element); }
    public IIR_AttributeSpecification 
	get_predecessor_element(IIR_AttributeSpecification element)
    { return (IIR_AttributeSpecification)super._get_predecessor_element(element); }
    public IIR_AttributeSpecification get_first_element()
    { return (IIR_AttributeSpecification)super._get_first_element(); }
    public IIR_AttributeSpecification get_nth_element(int index)
    { return (IIR_AttributeSpecification)super._get_nth_element(index); }
    public IIR_AttributeSpecification get_last_element()
    { return (IIR_AttributeSpecification)super._get_last_element(); }
    public int get_element_position(IIR_AttributeSpecification element)
    { return super._get_element_position(element); }

    //MEMBERS:  

// PROTECTED:
} // END class
