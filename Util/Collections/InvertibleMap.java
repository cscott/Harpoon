package harpoon.Util.Collections;

/** An <code>InvertibleMap</code> is an extension of the
    <code>Map</code> interface to allow users to do reverse lookups on
    the mappings maintained.  
    Since <code>Map</code>s are allowed to map multiple keys to a
    single value, the inversion of a <code>Map</code> is not
    necessarily a <code>Map</code> itself; thus we return a
    <code>MultiMap</code> for the inverted view.  The returned
    <code>MultiMap</code> is not guaranteed to be modfiable, even if
    <code>this</code> is (ie, changes to the data structure may still
    have to be made through <code>this</code> rather than directly to
    the returned <code>MultiMap</code>).

    @author  Felix S. Klock II <pnkfelix@mit.edu>
    @version $Id: InvertibleMap.java,v 1.1.2.3 2001-06-17 20:13:18 cananian Exp $
*/
public interface InvertibleMap extends java.util.Map {
    /** Returns a inverted view of <code>this</code>.
	Thus, if <code>this</code> is a <code>Map</code> with domain A
	and range B, the returned <code>MultiMap</code>,
	<code>imap</code>, will be a <code>MultiMap</code> with domain
	B and range A, such that each <em>b</em> in B will map in
	<code>imap</code> to Collection of A, <em>c</em>, if and only if
	each <em>a</em> in <em>c</em> maps to <em>b</em> in
	<code>this</code>. 
     */
    public MultiMap invert();
}
