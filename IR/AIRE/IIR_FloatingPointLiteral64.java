// IIR_FloatingPointLiteral64.java, created by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.AIRE;

/**
 * <code>IIR_FloatingPointLiteral64</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_FloatingPointLiteral64.java,v 1.4 1998-10-11 02:37:18 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_FloatingPointLiteral64 extends IIR_Literal
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    /**
     * Returns the <code>IR_Kind</code> of this class (IR_FLOATING_POINT_LITERAL64).
     * @return <code>IR_Kind.IR_FLOATING_POINT_LITERAL64</code>
     */
    public IR_Kind get_kind()
    { return IR_Kind.IR_FLOATING_POINT_LITERAL64; }
    
    
    //METHODS:  
    static IIR_FloatingPointLiteral64 get_value( double value)
    { return new IIR_FloatingPointLiteral64(value); }

    public double get_value() { return _value; }
 
    public void release() { /* do nothing. */ }
 
    //MEMBERS:  

// PROTECTED:
    IIR_FloatingPointLiteral64(double value) {
	_value = value;
    }
    double _value;
} // END class

