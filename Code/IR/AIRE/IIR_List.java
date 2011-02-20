// IIR_List.java, created by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.AIRE;

import java.util.Vector;
/**
 * The <code>IIR_List</code> class represents a collection of zero or
 * more dynamically allocated elements having a specified class
 * or common parent class.
 *
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_List.java,v 1.4 1998-10-11 02:37:20 cananian Exp $
 */

//-----------------------------------------------------------
public abstract class IIR_List extends IIR
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    
    
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
    protected void _prepend_element(IIR element) { 
	_vector.insertElementAt(element, 0);
    }
    protected void _append_element(IIR element) {
	_vector.addElement(element);
    }
    protected boolean _insert_after_element(IIR existing_element,
					   IIR new_element) {
	int index = _vector.indexOf(existing_element);
	if (index<0) return false;
	_vector.insertElementAt(new_element, index+1);
	return true;
    }
    protected boolean _insert_before_element(IIR existing_element,
					    IIR new_element) {
	int index = _vector.indexOf(existing_element);
	if (index < 0) return false;
	_vector.insertElementAt(new_element, index);
	return true;
    }
    protected boolean _remove_element(IIR element) {
	return _vector.removeElement(element);
    }
    protected IIR _get_successor_element(IIR existing_element) {
	int index = _vector.indexOf(existing_element);
	if (0 <= index && index < _vector.size()-1)
	    return (IIR) _vector.elementAt(index+1);
	else return null;
    }
    protected IIR _get_predecessor_element(IIR existing_element) {
	int index = _vector.indexOf(existing_element);
	if (0 < index)
	    return (IIR) _vector.elementAt(index-1);
	else return null;
    }
    protected IIR _get_first_element() {
	return (IIR) _vector.elementAt(0);
    }
    protected IIR _get_nth_element(int index) {
	return (IIR) _vector.elementAt(index);
    }
    protected IIR _get_last_element() {
	return (IIR) _vector.elementAt(_vector.size()-1);
    }
    protected int _get_element_position( IIR element ) {
	return _vector.indexOf(element);
    }

// PRIVATE:
    private Vector _vector = new Vector();
} // END class

