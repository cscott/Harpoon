// IIR_StringLiteral.java, created by cananian
package harpoon.IR.AIRE;

/**
 * The predefined <code>IIR_StringLiteral</code> class represents an array
 * of zero or more character literals defined by ISO Std. 8859-1.
 *
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_StringLiteral.java,v 1.1 1998-10-10 07:53:44 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_StringLiteral extends IIR_TextLiteral
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_STRING_LITERAL
    
    
    //METHODS:  
    static IIR_StringLiteral get_value( String value, Iint length)
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

