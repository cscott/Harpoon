// StmList.java, created Wed Jan 13 21:14:57 1999 by cananian
package harpoon.IR.Tree;

import harpoon.Temp.CloningTempMap;


/**
 * <code>StmList</code>s for singly-linked lists of <code>Stm</code>s.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>, based on
 *          <i>Modern Compiler Implementation in Java</i> by Andrew Appel.
 * @version $Id: StmList.java,v 1.1.2.2 1999-02-09 21:54:23 duncan Exp $
 */
public final class StmList {
    /** The statement at this list entry. */
    public final Stm head;
    /** The next list entry. */
    public final StmList tail;
    /** List constructor. */
    public StmList(Stm head, StmList tail)
    { this.head=head; this.tail=tail; }

    public StmList rename(TreeFactory tf, CloningTempMap ctm) {
        return new StmList((Stm)((head==null)?null:head.rename(tf, ctm)),
			   ((tail==null)?null:tail.rename(tf, ctm)));
    }
}



