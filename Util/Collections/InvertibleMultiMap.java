package harpoon.Util.Collections;

/**
 * An <code>InvertibleMultiMap</code> is an extension of the
 * <code>MultiMap</code> interface to allow users to do reverse lookups on
 * the mappings maintained.  
 * If, for <code>MultiMap</code> <code>m</code>,
 * <code>m.contains(a, b)</code>, then
 * <code>m.invert().contains(b, a)</code>.
 *
 * If the <code>InvertibleMultiMap</code> is mutable, the
 * <code>InvertibleMultiMap</code> returned by its <code>invert()</code>
 * method should also be mutable.  Moreover, for any
 * <code>InvertibleMultiMap</code>, 
 * <code>this.invert().invert()==this</code>.
 *
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: InvertibleMultiMap.java,v 1.1.2.2 2001-06-17 20:22:55 cananian Exp $
 */
public interface InvertibleMultiMap extends MultiMap {
    /** Returns a inverted view of <code>this</code>.
	Thus, if <code>this</code> is a <code>MultiMap</code> with domain A
	and range B, the returned <code>MultiMap</code>,
	<code>imap</code>, will be a <code>MultiMap</code> with domain
	B and range A, such that <em>b</em> in B will map in
	<code>imap</code> to a collection containing <em>a</em>,
	if and only if <em>a</em> in <code>this</code> maps to
	a collection containing <em>b</em>.
     */
    public InvertibleMultiMap invert();
}
