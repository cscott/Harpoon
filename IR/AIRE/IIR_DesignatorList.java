// IIR_DesignatorList.java, created by cananian
package harpoon.IR.AIRE;

/**
 * The predefined <code>IIR_DesignatorList</code> class represents an
 * ordered set containing zero or more <code>IIR_Designator</code> tuples.
 *
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_DesignatorList.java,v 1.1 1998-10-10 07:53:35 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_DesignatorList extends IIR_List
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_DESIGNATOR_LIST
    //CONSTRUCTOR:
    public IIR_DesignatorList() { }
    //METHODS:  
    public void prepend_element(IIR_Designator element)
    { super._prepend_element(element); }
    public void append_element(IIR_Designator element)
    { super._append_element(element); }
    public boolean 
	insert_after_element(IIR_Designator existing_element,
			     IIR_Designator new_element)
    { return super._insert_after_element(existing_element, new_element); }
    public boolean
	insert_before_element(IIR_Designator existing_element,
			      IIR_Designator new_element)
    { return super._insert_before_element(existing_element, new_element); }
    public boolean remove_element(IIR_Designator existing_element)
    { return super._remove_element(existing_element); }
    public IIR_Designator 
	get_successor_element(IIR_Designator element)
    { return (IIR_Designator)super._get_successor_element(element); }
    public IIR_Designator 
	get_predecessor_element(IIR_Designator element)
    { return (IIR_Designator)super._get_predecessor_element(element); }
    public IIR_Designator get_first_element()
    { return (IIR_Designator)super._get_first_element(); }
    public IIR_Designator get_nth_element(int index)
    { return (IIR_Designator)super._get_nth_element(index); }
    public IIR_Designator get_last_element()
    { return (IIR_Designator)super._get_last_element(); }
    public int get_element_position(IIR_Designator element)
    { return super._get_element_position(element); }
    //MEMBERS:  

// PROTECTED:
} // END class

