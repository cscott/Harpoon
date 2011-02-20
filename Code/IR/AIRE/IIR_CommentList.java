// IIR_CommentList.java, created by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.AIRE;

/**
 * The predefined <code>IIR_CommentList</code> class represents ordered
 * sets containing zero or more <code>IIR_Comment</code>s.  Such
 * comment lists appear only within <code>IIR_DesignFile</code>s within
 * the predefined <code>IIR</code> class hierachy.
 *
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_CommentList.java,v 1.4 1998-10-11 02:37:14 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_CommentList extends IIR_List
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    /**
     * Returns the <code>IR_Kind</code> of this class (IR_COMMENT_LIST).
     * @return <code>IR_Kind.IR_COMMENT_LIST</code>
     */
    public IR_Kind get_kind()
    { return IR_Kind.IR_COMMENT_LIST; }
    //CONSTRUCTOR:
    public IIR_CommentList() { }
    //METHODS:  
    public void prepend_element(IIR_Comment element)
    { super._prepend_element(element); }
    public void append_element(IIR_Comment element)
    { super._append_element(element); }
    public boolean 
	insert_after_element(IIR_Comment existing_element,
			     IIR_Comment new_element)
    { return super._insert_after_element(existing_element, new_element); }
    public boolean
	insert_before_element(IIR_Comment existing_element,
			      IIR_Comment new_element)
    { return super._insert_before_element(existing_element, new_element); }
    public boolean remove_element(IIR_Comment existing_element)
    { return super._remove_element(existing_element); }
    public IIR_Comment 
	get_successor_element(IIR_Comment element)
    { return (IIR_Comment)super._get_successor_element(element); }
    public IIR_Comment 
	get_predecessor_element(IIR_Comment element)
    { return (IIR_Comment)super._get_predecessor_element(element); }
    public IIR_Comment get_first_element()
    { return (IIR_Comment)super._get_first_element(); }
    public IIR_Comment get_nth_element(int index)
    { return (IIR_Comment)super._get_nth_element(index); }
    public IIR_Comment get_last_element()
    { return (IIR_Comment)super._get_last_element(); }
    public int get_element_position(IIR_Comment element)
    { return super._get_element_position(element); }
    //MEMBERS:  

// PROTECTED:
} // END class

