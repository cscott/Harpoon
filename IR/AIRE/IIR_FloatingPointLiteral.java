// IIR_FloatingPointLiteral.java, created by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
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
 * @version $Id: IIR_FloatingPointLiteral.java,v 1.5 1998-10-11 02:37:18 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_FloatingPointLiteral extends IIR_Literal
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    /**
     * Returns the <code>IR_Kind</code> of this class (IR_FLOATING_POINT_LITERAL).
     * @return <code>IR_Kind.IR_FLOATING_POINT_LITERAL</code>
     */
    public IR_Kind get_kind()
    { return IR_Kind.IR_FLOATING_POINT_LITERAL; }
    
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
 
    public String print(int length)
    { throw new Error("unimplemented."); /* FIXME */ }
 
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

