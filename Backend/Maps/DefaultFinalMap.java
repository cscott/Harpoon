// DefaultFinalMap.java, created Sat Jan 16 21:12:44 1999 by cananian
package harpoon.Backend.Maps;

import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HField;
import harpoon.ClassFile.HMethod;

import java.lang.reflect.Modifier;
/**
 * <code>DefaultFinalMap</code> is a stupid implementation of
 * <code>FinalMap</code> that just looks for the <code>final</code>
 * access modifier.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: DefaultFinalMap.java,v 1.1.2.2 1999-01-23 10:06:08 cananian Exp $
 */

public class DefaultFinalMap extends FinalMap {
    /** Creates a <code>DefaultFinalMap</code>. No arguments, because
     *  this implementation is very simple-minded. */
    public DefaultFinalMap() {
        // make snow angels in the park.
    }
    public boolean isFinal(HClass hc) {
	return Modifier.isFinal(hc.getModifiers());
    }
    public boolean isFinal(HMethod hm) {
	// a buglet in javac doesn't explcitly put the final tag on
	// methods when the class is final.
	return
	    isFinal(hm.getDeclaringClass()) ||
	    Modifier.isFinal(hm.getModifiers());
	// private methods are really final, too.
    }
    public boolean isFinal(HField hf) {
	// trust the JVM, even though it lies about System.{in,out,err}
	return Modifier.isFinal(hf.getModifiers());
    }
}
