// IIR_LibraryUnitList.java, created by cananian
package harpoon.IR.AIRE;

/**
 * The predefined <code>IIR_LibraryUnitList</code> class represents
 * ordered sets containing zero or more <code>IIR_LibraryUnit</code>s.
 * These library unit lists appear in predefined public data elements
 * within an <code>IIR_DesignFile</code>.
 *
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_LibraryUnitList.java,v 1.1 1998-10-10 07:53:38 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_LibraryUnitList extends IIR_List
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_LIBRARY_UNIT_LIST
    //CONSTRUCTOR:
    public IIR_LibraryUnitList() { }
    //METHODS:  
    public void prepend_element(IIR_LibraryUnit element)
    { super._prepend_element(element); }
    public void append_element(IIR_LibraryUnit element)
    { super._append_element(element); }
    public boolean 
	insert_after_element(IIR_LibraryUnit existing_element,
			     IIR_LibraryUnit new_element)
    { return super._insert_after_element(existing_element, new_element); }
    public boolean
	insert_before_element(IIR_LibraryUnit existing_element,
			      IIR_LibraryUnit new_element)
    { return super._insert_before_element(existing_element, new_element); }
    public boolean remove_element(IIR_LibraryUnit existing_element)
    { return super._remove_element(existing_element); }
    public IIR_LibraryUnit 
	get_successor_element(IIR_LibraryUnit element)
    { return (IIR_LibraryUnit)super._get_successor_element(element); }
    public IIR_LibraryUnit 
	get_predecessor_element(IIR_LibraryUnit element)
    { return (IIR_LibraryUnit)super._get_predecessor_element(element); }
    public IIR_LibraryUnit get_first_element()
    { return (IIR_LibraryUnit)super._get_first_element(); }
    public IIR_LibraryUnit get_nth_element(int index)
    { return (IIR_LibraryUnit)super._get_nth_element(index); }
    public IIR_LibraryUnit get_last_element()
    { return (IIR_LibraryUnit)super._get_last_element(); }
    public int get_element_position(IIR_LibraryUnit element)
    { return super._get_element_position(element); }
    //MEMBERS:  

// PROTECTED:
} // END class

