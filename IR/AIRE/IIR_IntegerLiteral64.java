// IIR_IntegerLiteral64.java, created by cananian
package harpoon.IR.AIRE;

import harpoon.Util.Tuple;
import java.util.Hashtable;
/**
 * The predefined <code>IIR_IntegerLiteral64</code> class is an integer
 * literal class capable of representing any literal value within the
 * range covered by a 64-bit signed, two's complement representation.
 *
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_IntegerLiteral64.java,v 1.2 1998-10-11 00:32:21 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_IntegerLiteral64 extends IIR_Literal
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    public IR_Kind get_kind()
    { return IR_Kind.IR_INTEGER_LITERAL64; }
    
    //METHODS:  
    public static IIR_IntegerLiteral64 get(long value) {
        IIR_IntegerLiteral64 ret = 
	    (IIR_IntegerLiteral64) _h.get(new Long(value));
        if (ret==null) {
            ret = new IIR_IntegerLiteral64(value);
	    _h.put(new Long(value), ret);
        }
        return ret;
    }
 
    public long get_value() { return _value; }
 
    public void release() { /* do nothing. */ }
 
    //MEMBERS:  

// PROTECTED:
    long _value;
    private IIR_IntegerLiteral64( long value ) {
	_value = value;
    }
    private static Hashtable _h = new Hashtable();
} // END class

