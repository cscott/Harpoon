// IIR_FloatingPointLiteral.java, created by cananian
package harpoon.IR.AIRE;

import harpoon.Util.Tuple;
import java.util.Hashtable;
/**
 * <code>IIR_FloatingPointLiteral</code> is the most general representation
 * of a floating point literal.  It is capable of representing any
 * floating point literal value withing the implementation-defined
 * limitations of a specific IIR foundation.
 *
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_FloatingPointLiteral.java,v 1.1 1998-10-10 07:53:36 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_FloatingPointLiteral extends IIR_Literal
{

    // FIXME FIXME FIXME FIXME FIXME

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_FLOATING_POINT_LITERAL
    
    //METHODS:  
    public static IIR_FloatingPointLiteral get(int base, String mantissa, int mantissa_length, String exponent, int exponent_length) {
        Tuple t = new Tuple(new Object[] { new Integer(base), mantissa, new Integer(mantissa_length), exponent, new Integer(exponent_length) } );
        IIR_FloatingPointLiteral ret = (IIR_FloatingPointLiteral) _h.get(t);
        if (ret==null) {
            ret = new IIR_FloatingPointLiteral(base, mantissa, mantissa_length, exponent, exponent_length);
            _h.put(t, ret);
        }
        return ret;
    }
 
    public String print(int length) { throw new Error(); /* FIXME */ }
 
    public void release() { /* do nothing */ }
 
    //MEMBERS:  

// PROTECTED:
    int _base;
    String _mantissa;
    int _mantissa_length;
    String _exponent;
    int _exponent_length;

    private IIR_FloatingPointLiteral(int base, String mantissa, int mantissa_length, String exponent, int exponent_length) {
        _base = base;
        _mantissa = mantissa;
        _mantissa_length = mantissa_length;
        _exponent = exponent;
        _exponent_length = exponent_length;
    }
    private static Hashtable _h = new Hashtable();
} // END class

