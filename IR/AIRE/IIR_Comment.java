// IIR_Comment.java, created by cananian
package harpoon.IR.AIRE;

import harpoon.Util.Tuple;
import java.util.Hashtable;
/**
 * The predefined <code>IIR_Comment</code> class represents a single,
 * contiguous comment within the original source code.
 *
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_Comment.java,v 1.2 1998-10-11 00:32:17 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_Comment extends IIR
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
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

