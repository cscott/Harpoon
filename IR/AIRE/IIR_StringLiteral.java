// IIR_StringLiteral.java, created by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.AIRE;

import java.util.Hashtable;
/**
 * The predefined <code>IIR_StringLiteral</code> class represents an array
 * of zero or more character literals defined by ISO Std. 8859-1.
 *
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_StringLiteral.java,v 1.7 1998-10-11 02:37:24 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_StringLiteral extends IIR_TextLiteral
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    /**
     * Returns the <code>IR_Kind</code> of this class (IR_STRING_LITERAL).
     * @return <code>IR_Kind.IR_STRING_LITERAL</code>
     */
    public IR_Kind get_kind()
    { return IR_Kind.IR_STRING_LITERAL; }
    
    
    //METHODS:  
    public static IIR_StringLiteral get_value( String value, int length)
    { return get_value(value); }
    public static IIR_StringLiteral get_value( String value )
    { return new IIR_StringLiteral(value); }
 
    public String get_text()
    { return _value.toString(); }
    public int get_text_length()
    { return _value.length(); }

    public char get_element( int subscript )
    { return _value.charAt(subscript); }
    public void set_element( int subscript, char value)
    { _value.setCharAt(subscript, value); }
 
    public void release() { /* do nothing. */ }
 
    //MEMBERS:  

// PROTECTED:
    StringBuffer _value;
    IIR_StringLiteral(String value) {
	_value = new StringBuffer(value);
    }
} // END class

