// IIR_ConcurrentStatementList.java, created by cananian
package harpoon.IR.AIRE;

/**
 * The predefined <code>IIR_ConcurrentStatementList</code> class represents
 * ordered sets containing zero or more <core>IIR_ConcurrentStatement</code>s.
 * Such lists are found directly within entities, architectures, block
 * statements, and generate statements.
 *
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_ConcurrentStatementList.java,v 1.1 1998-10-10 07:53:34 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_ConcurrentStatementList extends IIR_List
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_CONCURRENT_STATEMENT_LIST
    //CONSTRUCTOR:
    public IIR_ConcurrentStatementList() { }
    //METHODS:  
    public void prepend_element(IIR_ConcurrentStatement element)
    { super._prepend_element(element); }
    public void append_element(IIR_ConcurrentStatement element)
    { super._append_element(element); }
    public boolean 
	insert_after_element(IIR_ConcurrentStatement existing_element,
			     IIR_ConcurrentStatement new_element)
    { return super._insert_after_element(existing_element, new_element); }
    public boolean
	insert_before_element(IIR_ConcurrentStatement existing_element,
			      IIR_ConcurrentStatement new_element)
    { return super._insert_before_element(existing_element, new_element); }
    public boolean remove_element(IIR_ConcurrentStatement existing_element)
    { return super._remove_element(existing_element); }
    public IIR_ConcurrentStatement 
	get_successor_element(IIR_ConcurrentStatement element)
    { return (IIR_ConcurrentStatement)super._get_successor_element(element); }
    public IIR_ConcurrentStatement 
	get_predecessor_element(IIR_ConcurrentStatement element)
    { return (IIR_ConcurrentStatement)super._get_predecessor_element(element); }
    public IIR_ConcurrentStatement get_first_element()
    { return (IIR_ConcurrentStatement)super._get_first_element(); }
    public IIR_ConcurrentStatement get_nth_element(int index)
    { return (IIR_ConcurrentStatement)super._get_nth_element(index); }
    public IIR_ConcurrentStatement get_last_element()
    { return (IIR_ConcurrentStatement)super._get_last_element(); }
    public int get_element_position(IIR_ConcurrentStatement element)
    { return super._get_element_position(element); }
    //MEMBERS:  

// PROTECTED:
} // END class

