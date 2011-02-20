// IIR_IntegerLiteral.java, created by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.AIRE;

import harpoon.Util.Tuple;
import java.util.Hashtable;
/**
 * The predefined <code>IIR_IntegerLiteral</code> class is the most
 * general representation of an integer literal.  It is capable of
 * representing <i>any</i> integer literal value falling within the
 * limitations of a specific IIR foundation implementation.
 *
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_IntegerLiteral.java,v 1.4 1998-10-11 02:37:19 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_IntegerLiteral extends IIR_Literal
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    /**
     * Returns the <code>IR_Kind</code> of this class (IR_INTEGER_LITERAL).
     * @return <code>IR_Kind.IR_INTEGER_LITERAL</code>
     */
    public IR_Kind get_kind()
    { return IR_Kind.IR_INTEGER_LITERAL; }
    
    //METHODS:  
    public static IIR_IntegerLiteral get(int base, String mantissa, int mantissa_length, String exponent, int exponent_length) {
        Tuple t = new Tuple(new Object[] { new Integer(base), mantissa, new Integer(mantissa_length), exponent, new Integer(exponent_length) } );
        IIR_IntegerLiteral ret = (IIR_IntegerLiteral) _h.get(t);
        if (ret==null) {
            ret = new IIR_IntegerLiteral(base, mantissa, mantissa_length, exponent, exponent_length);
            _h.put(t, ret);
        }
        return ret;
    }
 
    public String print( int length) { throw new Error(); }
 
    public void release() { /* do nothing. */ }
 
    //MEMBERS:  

// PROTECTED:
    int _base;
    String _mantissa;
    int _mantissa_length;
    String _exponent;
    int _exponent_length;
    private IIR_IntegerLiteral(int base, String mantissa, int mantissa_length, String exponent, int exponent_length) {
        _base = base;
        _mantissa = mantissa;
        _mantissa_length = mantissa_length;
        _exponent = exponent;
        _exponent_length = exponent_length;
    }
    private static Hashtable _h = new Hashtable();
} // END class

