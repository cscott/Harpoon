// IIR_Comment.java, created by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.AIRE;

import harpoon.Util.Tuple;
import java.util.Hashtable;
/**
 * The predefined <code>IIR_Comment</code> class represents a single,
 * contiguous comment within the original source code.
 *
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_Comment.java,v 1.5 1998-10-11 02:37:14 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_Comment extends IIR
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    /**
     * Returns the <code>IR_Kind</code> of this class (IR_COMMENT).
     * @return <code>IR_Kind.IR_COMMENT</code>
     */
    public IR_Kind get_kind()
    { return IR_Kind.IR_COMMENT; }
    
    //METHODS:  
    public static IIR_Comment get(String text, int text_length)
    { return get(text); }
    public static IIR_Comment get(String text ) {
        IIR_Comment ret = (IIR_Comment) _h.get(text);
        if (ret==null) {
            ret = new IIR_Comment(text);
	    _h.put(text, ret);
        }
        return ret;
    }
 
    public String get_text()
    { return _comment.toString(); }
 
    public int get_text_length()
    { return _comment.length(); }
 
    public char get_element( int subscript )
    { return _comment.charAt(subscript); }
    // FIXME: set_element changes entry in _h
    public void set_element( int subscript, char value)
    { _comment.setCharAt(subscript, value); }
 
    public void release() { /* do nothing. */ }
 
    //MEMBERS:  

// PROTECTED:
    StringBuffer _comment;

    private IIR_Comment(String comment) {
	_comment = new StringBuffer(comment);
    }
    private static Hashtable _h = new Hashtable();
} // END class

