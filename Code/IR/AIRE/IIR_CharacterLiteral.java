// IIR_CharacterLiteral.java, created by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.AIRE;

import harpoon.Util.Tuple;
import java.util.Hashtable;
/**
 * The predefined <code>IIR_CharacterLiteral</code> class represents
 * character literals defined by ISO Std. 8859-1.
 *
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_CharacterLiteral.java,v 1.4 1998-10-11 02:37:14 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_CharacterLiteral extends IIR_TextLiteral
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    /**
     * Returns the <code>IR_Kind</code> of this class (IR_CHARACTER_LITERAL).
     * @return <code>IR_Kind.IR_CHARACTER_LITERAL</code>
     */
    public IR_Kind get_kind()
    { return IR_Kind.IR_CHARACTER_LITERAL; }
    
    //METHODS:  
    public static IIR_CharacterLiteral get(char character) {
        IIR_CharacterLiteral ret = 
	    (IIR_CharacterLiteral) _h.get(new Character(character));
        if (ret==null) {
            ret = new IIR_CharacterLiteral(character);
	    _h.put(new Character(character), ret);
        }
        return ret;
    }
 
    /** The value method returns an ISO Std. 8859-1 representation of
     *  the character. */
    public char get_text()
    { return _character; }
 
    //MEMBERS:  

// PROTECTED:
    char _character;
    private IIR_CharacterLiteral(char character) {
        _character = character;
    }
    private static Hashtable _h = new Hashtable();
} // END class

