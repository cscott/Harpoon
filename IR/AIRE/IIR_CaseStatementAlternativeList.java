// IIR_CaseStatementAlternativeList.java, created by cananian
package harpoon.IR.AIRE;

/**
 * The predefined <code>IIR_CaseStatementAlternativeList</code> class
 * represents ordered sets containing zero or more
 * <code>IIR_CaseStatementAlternative</code> objects.  Case statement
 * alternative lists are used within case statements to denote
 * the list of choice, implication pairs.
 *
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_CaseStatementAlternativeList.java,v 1.3 1998-10-11 00:32:17 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_CaseStatementAlternativeList extends IIR_List
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    public IR_Kind get_kind()
    { return IR_Kind.IR_CASE_STATEMENT_ALTERNATIVE_LIST; }
    //CONSTRUCTOR:
    public IIR_CaseStatementAlternativeList() { }
    //METHODS:  
    public void prepend_element(IIR_CaseStatementAlternative element)
    { super._prepend_element(element); }
    public void append_element(IIR_CaseStatementAlternative element)
    { super._append_element(element); }
    public boolean 
	insert_after_element(IIR_CaseStatementAlternative existing_element,
			     IIR_CaseStatementAlternative new_element)
    { return super._insert_after_element(existing_element, new_element); }
    public boolean
	insert_before_element(IIR_CaseStatementAlternative existing_element,
			      IIR_CaseStatementAlternative new_element)
    { return super._insert_before_element(existing_element, new_element); }
    public boolean remove_element(IIR_CaseStatementAlternative existing_element)
    { return super._remove_element(existing_element); }
    public IIR_CaseStatementAlternative 
	get_successor_element(IIR_CaseStatementAlternative element)
    { return (IIR_CaseStatementAlternative)super._get_successor_element(element); }
    public IIR_CaseStatementAlternative 
	get_predecessor_element(IIR_CaseStatementAlternative element)
    { return (IIR_CaseStatementAlternative)super._get_predecessor_element(element); }
    public IIR_CaseStatementAlternative get_first_element()
    { return (IIR_CaseStatementAlternative)super._get_first_element(); }
    public IIR_CaseStatementAlternative get_nth_element(int index)
    { return (IIR_CaseStatementAlternative)super._get_nth_element(index); }
    public IIR_CaseStatementAlternative get_last_element()
    { return (IIR_CaseStatementAlternative)super._get_last_element(); }
    public int get_element_position(IIR_CaseStatementAlternative element)
    { return super._get_element_position(element); }
    //MEMBERS:  

// PROTECTED:
} // END class

