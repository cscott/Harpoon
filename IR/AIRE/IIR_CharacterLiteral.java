// IIR_CharacterLiteral.java, created by cananian
package harpoon.IR.AIRE;

import harpoon.Util.Tuple;
import java.util.Hashtable;
/**
 * The predefined <code>IIR_CharacterLiteral</code> class represents
 * character literals defined by ISO Std. 8859-1.
 *
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_CharacterLiteral.java,v 1.1 1998-10-10 07:53:33 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_CharacterLiteral extends IIR_TextLiteral
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_CHARACTER_LITERAL
    
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

