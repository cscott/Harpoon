// IIR_IntegerLiteral32.java, created by cananian
package harpoon.IR.AIRE;

import harpoon.Util.Tuple;
import java.util.Hashtable;
/**
 * The predefined <code>IIR_IntegerLiteral32</code> class is an integer
 * literal class capable of representing any literal value within
 * the range covered by a 32-bit signed, two's complement representation.
 *
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_IntegerLiteral32.java,v 1.2 1998-10-11 00:32:21 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_IntegerLiteral32 extends IIR_Literal
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    public IR_Kind get_kind()
    { return IR_Kind.IR_INTEGER_LITERAL32; }
    
    //METHODS:  
    public static IIR_IntegerLiteral32 get(int value) {
        IIR_IntegerLiteral32 ret = (IIR_IntegerLiteral32) _h.get(new Integer(value));
        if (ret==null) {
            ret = new IIR_IntegerLiteral32(value);
	    _h.put(new Integer(value), ret);
        }
        return ret;
    }
 
    public int get_value() { return _value; }
 
    public void release() { /* do nothing. */ }
 
    //MEMBERS:  

// PROTECTED:
    int _value;
    private IIR_IntegerLiteral32(int value) {
	_value = value;
    }
    private static Hashtable _h = new Hashtable();
} // END class

