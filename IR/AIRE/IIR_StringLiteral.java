// IIR_StringLiteral.java, created by cananian
package harpoon.IR.AIRE;

/**
 * The predefined <code>IIR_StringLiteral</code> class represents an array
 * of zero or more character literals defined by ISO Std. 8859-1.
 *
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_StringLiteral.java,v 1.3 1998-10-11 00:32:27 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_StringLiteral extends IIR_TextLiteral
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    public IR_Kind get_kind()
    { return IR_Kind.IR_STRING_LITERAL; }
    
    
    //METHODS:  
    public static IIR_StringLiteral get_value( String value, int length)
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
    IIR_StringLiteral(String value) {
	_value = new StringBuffer(value);
    }
    StringBuffer _value;
} // END class

