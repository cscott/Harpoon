// IIR_ChoiceList.java, created by cananian
package harpoon.IR.AIRE;

/**
 * The predefined <code>IIR_ChoiceList</code> class represents
 * ordered sets containing zero or more <code>IIR_Choice</code> objects.
 * Choice lists are used with case statements to denote lists
 * containing two or more choices.
 *
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_ChoiceList.java,v 1.1 1998-10-10 07:53:33 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_ChoiceList extends IIR_List
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_CHOICE_LIST
    //CONSTRUCTOR:
    public IIR_ChoiceList() { }
    //METHODS:  
    public void prepend_element(IIR_Choice element)
    { super._prepend_element(element); }
    public void append_element(IIR_Choice element)
    { super._append_element(element); }
    public boolean 
	insert_after_element(IIR_Choice existing_element,
			     IIR_Choice new_element)
    { return super._insert_after_element(existing_element, new_element); }
    public boolean
	insert_before_element(IIR_Choice existing_element,
			      IIR_Choice new_element)
    { return super._insert_before_element(existing_element, new_element); }
    public boolean remove_element(IIR_Choice existing_element)
    { return super._remove_element(existing_element); }
    public IIR_Choice 
	get_successor_element(IIR_Choice element)
    { return (IIR_Choice)super._get_successor_element(element); }
    public IIR_Choice 
	get_predecessor_element(IIR_Choice element)
    { return (IIR_Choice)super._get_predecessor_element(element); }
    public IIR_Choice get_first_element()
    { return (IIR_Choice)super._get_first_element(); }
    public IIR_Choice get_nth_element(int index)
    { return (IIR_Choice)super._get_nth_element(index); }
    public IIR_Choice get_last_element()
    { return (IIR_Choice)super._get_last_element(); }
    public int get_element_position(IIR_Choice element)
    { return super._get_element_position(element); }
    //MEMBERS:  

// PROTECTED:
} // END class

