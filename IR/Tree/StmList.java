// StmList.java, created Wed Jan 13 21:14:57 1999 by cananian
package harpoon.IR.Tree;

import harpoon.Temp.CloningTempMap;


/**
 * <code>StmList</code>s for singly-linked lists of <code>Stm</code>s.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>, based on
 *          <i>Modern Compiler Implementation in Java</i> by Andrew Appel.
 * @version $Id: StmList.java,v 1.1.2.3 1999-02-09 22:47:11 duncan Exp $
 */
public final class StmList {
    /** The statement at this list entry. */
    public final Stm head;
    /** The next list entry. */
    public final StmList tail;
    /** List constructor. */
    public StmList(Stm head, StmList tail)
    { this.head=head; this.tail=tail; }

    public static StmList rename(StmList s, TreeFactory tf, 
				 CloningTempMap ctm) {
        if (s==null) return null;
	else return new StmList
	       ((Stm)((s.head==null)?null:s.head.rename(tf, ctm)),
		rename(s.tail, tf, ctm));
    }
}



