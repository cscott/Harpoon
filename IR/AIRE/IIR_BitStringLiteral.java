// IIR_BitStringLiteral.java, created by cananian
package harpoon.IR.AIRE;

import harpoon.Util.Tuple;
import java.util.Hashtable;
/**
 * The <code>IIR_BitStringLiteral</code> class represents an array of
 * zero or more literals having either character literal value '0' or '1'.
 *
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_BitStringLiteral.java,v 1.1 1998-10-10 07:53:32 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_BitStringLiteral extends IIR_TextLiteral
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_BIT_STRING_LITERAL
    
    //METHODS:  
    public static IIR_BitStringLiteral get(String value, int length)
    { return get(value); }
    public static IIR_BitStringLiteral get(String value) {
        IIR_BitStringLiteral ret = (IIR_BitStringLiteral) _h.get(value);
        if (ret==null) {
	    ret = new IIR_BitStringLiteral(value);
	    _h.put(value, ret);
	}
        return ret;
    }
 
    public int  get_element(int subscript)
    { return _bits[subscript]?1:0; }
 
    public void set_element(int subscript, int value)
    { _bits[subscript] = (value!=0)?true:false; }
 
    public void release() { /* do nothing */ }
 
    //MEMBERS:  

// PROTECTED:
    boolean _bits[];
    private IIR_BitStringLiteral(String val) {
	_bits = new boolean[val.length()];
	for (int i=0; i<_bits.length; i++)
	    _bits[i] = val.charAt(i)!='0';
    }
    private static Hashtable _h = new Hashtable();
} // END class

