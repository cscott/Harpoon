// PersistentTreeNode.java, created Wed Mar 31 18:41:03 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util.Collections;

import harpoon.Util.Util; // for log2c in self-test function.
import java.util.Comparator;
/**
 * <code>PersistentTreeNode</code>s are nodes of a persistent randomized
 * search tree.  This is the representation from: <cite>
 * C. R. Aragon and R. G. Seidel, "Randomized search trees", Proc. 30th IEEE
 * FOCS (1989), 540-545.
 * </cite>
 * We use the suggestion of Dan Sleator, mention in the paper on p 543,
 * which avoids the necessity of storing a separate priority field in the
 * nodes of the treap (heap-ordered tree) by using a hash of the key as the
 * priority.  Because in many cases we might expect Object.hashCode() to
 * present the same ordering as a Comparator, we redundantly hash the
 * hashcode to obtain a good distribution.  Since treaps representing
 * collections of elements are unique, we can use simple comparison and
 * equality tests for treaps.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: PersistentTreeNode.java,v 1.8 2003-06-08 16:40:46 cananian Exp $
 */
// XXX implement the javadoc
// parameterize the allocation function. (pass it in as an argument?)
// maybe parameterize tree node such that 'value' is not necessarily present?
// define an iterator here?
// XXX do we want fast hashCode()?
class PersistentTreeNode<K,V> extends AbstractMapEntry<K,V> 
    implements java.io.Serializable {

    public final K key;
    public final V value;
    public final PersistentTreeNode<K,V> left;
    public final PersistentTreeNode<K,V> right;

    PersistentTreeNode(K key, V value,
		       PersistentTreeNode<K,V> left,
		       PersistentTreeNode<K,V> right) {
	this.key = key;  this.value = value;
	this.left= left; this.right = right;
    }
    // ACCESSOR FUNCTIONS for Map.Entry.
    public K getKey() { return key; }
    public V getValue() { return value; }

    // For debugging
    public String toString() {
	return "["+key+"->"+value+", left="+left+", right="+right+"]";
    }
    static <K,V> int depth(PersistentTreeNode<K,V> n) {
	if (n==null) return 0;
	return 1+Math.max(depth(n.left), depth(n.right));
    }

    /** equals() merely checks that key and value are equivalent;
     *  isSame() checks that left and right branches are equivalent, too.
     */
    public boolean isSame(PersistentTreeNode n) {
	if (this==n) return true; // quick common case
	if (null==n) return false; // protect us from evil; this!=null
	return
	    isSame(key,  n.key)  && isSame(value, n.value) &&
	    isSame(left, n.left) && isSame(right, n.right);
    }
    private static boolean isSame(Object o1, Object o2) {
	return (o1==null)?(o2==null):(o1.equals(o2));
    }
    private static boolean isSame(PersistentTreeNode n1,PersistentTreeNode n2){
	return (n1==null)?(n2==null):(n1.isSame(n2));
    }

    // TREE UTILITY FUNCTIONS.
    /** Creates a new node iff the created node would not be identical
     *  to the given <code>n</code>. */
    private static <K,V> 
	PersistentTreeNode<K,V> newNode(PersistentTreeNode<K,V> n,
					K key, V value,
					PersistentTreeNode<K,V> left,
					PersistentTreeNode<K,V> right) {
	if (n != null && n.key.equals(key) && n.value.equals(value) &&
	    n.left == left && n.right == right)
	    return n;
	// check heap condition
	assert left==null || heapKey(key) < heapKey(left.key);
	assert right==null|| heapKey(key) <= heapKey(right.key);
	return new PersistentTreeNode<K,V>(key, value, left, right);
    }
    /** Creates a new node (possibly reusing existing node n) rebalancing
     *  the left side if necessary. */
    private static <K,V> 
	PersistentTreeNode<K,V> newNode_balanceLeft
	(PersistentTreeNode<K,V> n, K key, V value,
	 PersistentTreeNode<K,V> left, PersistentTreeNode<K,V> right) {
	// tie goes to the left (lesser tree key)
	if (left!=null && left!=n.left/*speed optimization*/ &&
	    heapKey(left.key) <= heapKey(key)) { // needs rebalancing!
	    // bring left up to top
	    //     d         b
	    //    / \   ->  / \
	    //   b   e     a   d
	    //  / \           / \
	    // a   c         c   e
	    return newNode(left, left.key, left.value,
			   left.left, newNode(n, key, value,
					      left.right, right));
	} else { // okay as is
	    return newNode(n, key, value, left, right);
	}
    }
    /** Creates a new node (possibly reusing existing node n) rebalancing
     *  the right side if necessary. */
    private static <K,V> 
	PersistentTreeNode<K,V> newNode_balanceRight
	(PersistentTreeNode<K,V> n, K key, V value,
	 PersistentTreeNode<K,V> left, PersistentTreeNode<K,V> right) {
	// tie goes to the top (lesser tree key)
	if (right!=null && right!=n.right/*speed optimization*/ &&
	    heapKey(right.key) < heapKey(key)) { // needs rebalancing!
	    // bring right up to top
	    //     b          d
	    //    / \   ->   / \
	    //   a   d      b   e
	    //      / \    / \
	    //     c   e  a   c
	    return newNode(right, right.key, right.value,
			   newNode(n, key, value, left, right.left),
			   right.right);
	} else { // okay as is
	    return newNode(n, key, value, left, right);
	}
    }
    /** Returns the number of nodes in the tree rooted at <code>n</code>.
     * @return 0 if <code>n==null</code>, else 1+size(n.left)+size(n.right)
     */
    static <K,V> int size(PersistentTreeNode<K,V> n) {
	return (n==null) ? 0 : (1 + size(n.left) + size(n.right));
    }
    /** Returns the <code>PersistentTreeNode</code> matching <code>key</code>
     *  if any, else <code>null</code>. */
    static <K,V> PersistentTreeNode<K,V> get(PersistentTreeNode<K,V> n,
					     Comparator<K> c,
					     K key) {
	if (n==null) return null; /* no node with this key. */
	int r = c.compare(key, n.key);
	return
	    (r ==0) ? n :
	    (r < 0) ? get(n.left, c, key) : get(n.right, c, key);
    }

    /** Returns a node rooting a tree containing all the mappings in
     *  the tree rooted at the given <code>n</code>, plus a mapping from
     *  <code>key</code> to <code>value</code>. */
    static <K,V> PersistentTreeNode<K,V> put(PersistentTreeNode<K,V> n,
					     Comparator<K> c,
					     K key, V value) {
	if (n==null) return newNode(null, key, value, null, null);
	
	int r = c.compare(key, n.key);
	if (r==0)
	    // already heap-balanced
	    return newNode(n, key, value, n.left, n.right);
	if (r < 0)
	    return newNode_balanceLeft(n, n.key, n.value,
				       put(n.left, c, key, value), n.right);
	if (r > 0)
	    return newNode_balanceRight(n, n.key, n.value,
					n.left, put(n.right, c, key, value));
	throw new Error("Impossible!");
    }
    /** Returns a node rooting a tree containing all the mappings in
     *  the tree rooted at the given <code>n</code> except that it does
     *  not contain a mapping for <code>key</code>. */
    static <K,V> PersistentTreeNode<K,V> remove(PersistentTreeNode<K,V> n,
						Comparator<K> c,
						K key) {
	if (n==null) return null; // key not found.

	int r = c.compare(key, n.key);
	if (r==0) // remove this node.
	    return merge(n.left, n.right);
	if (r < 0)
	    return newNode_balanceLeft(n, n.key, n.value,
				       remove(n.left, c, key), n.right);
	if (r > 0)
	    return newNode_balanceRight(n, n.key, n.value,
					n.left, remove(n.right, c, key));
	throw new Error("Impossible!");
    }
    /** Merge two nodes into one. */
    private static <K,V>
	PersistentTreeNode<K,V> merge(PersistentTreeNode<K,V> left,
				      PersistentTreeNode<K,V> right) {
	if (left==null) return right;
	if (right==null) return left;
	// the node with the smallest heap key goes on top.
	// in case of tie, the smallest tree key goes on top (left node)
	if (heapKey(left.key) > heapKey(right.key))
	    return newNode(null, right.key, right.value,
			   merge(left, right.left), right.right);
	else
	    return newNode(null, left.key, left.value,
			   left.left, merge(left.right, right));
    }

    /** This is a "randomized" hash function, based on the object's own
     *  <code>key.hashCode()</code> value.  We protect against
     *  <code>a.hashCode() < b.hashCode()</code> being correlated with
     *  <code>a.compareTo(b)</code> by permuting and shuffling the bits
     *  of <code>key.hashCode()</code> around.  This, hopefully,
     *  results in an heap ordering relation based on hash values which is
     *  uncorrelated with the tree ordering relation based on the
     *  <code>Comparator</code> for the key type. */
    public static final <K> int heapKey(K treeKey) {
	int hash = treeKey.hashCode();
	// permute bits
	hash =
	    permute1[hash&0xFF] |
	    permute2[(hash>>8)&0xFF] |
	    permute3[(hash>>16)&0xFF] |
	    permute4[(hash>>>24)];
	// shuffle bytes
	hash =
	    (shuffle[(hash&0xFF)]) |
	    (shuffle[(hash>>8)&0xFF]<<8) |
	    (shuffle[(hash>>16)&0xFF]<<16) |
	    (shuffle[(hash>>>24)]<<24);
	// could do more rounds of this, but I think one's enough.
	return hash;
    }
    private static final int[]
	permute1 = new int[256],
	permute2 = new int[256],
	permute3 = new int[256],
	permute4 = new int[256];
    private static final short[] shuffle = new short[256];
    static {
	// use an explicit seed so that this is deterministic
	java.util.Random r = new java.util.Random(0xDEAFCAFEBABECABAL);
	// come up with a permutation of bit positions 0 to 31
	int[] bitpos = new int[32];
	for (int i=0; i<bitpos.length; i++)
	    bitpos[i] = 1<<i;
	for (int i=0; i<bitpos.length-1; i++) {//see Collections.shuffle() doc
	    int switcharoo = i+r.nextInt(bitpos.length-i);
	    // switch element 'i' and element 'switcharoo'
	    int tmp = bitpos[i];
	    bitpos[i] = bitpos[switcharoo];
	    bitpos[switcharoo] = tmp;
	}
	// initialize permutation arrays.
	for (int p=0; p<4; p++) {
	    int[] permute;
	    switch(p) {
	    case 0: permute=permute1; break;
	    case 1: permute=permute2; break;
	    case 2: permute=permute3; break;
	    case 3: permute=permute4; break;
	    default: throw new AssertionError("impossible!"); 
	    }
	    for (int i=0; i<permute.length; i++) {
		int result=0, input=i<<(p*8);
		for (int j=0; j<32; j++, input>>=1)
		    if (0!=(input&1))
			result |= bitpos[j];
		permute[i] = result;
	    }
	}
	// initialize shuffle array.
	for (short i=0; i<shuffle.length; i++)
	    shuffle[i] = i;
	for (int i=0; i<shuffle.length-1; i++) {//see Collections.shuffle() doc
	    int switcharoo = i+r.nextInt(shuffle.length-i);
	    // switch element 'i' and element 'switcharoo'
	    short tmp = shuffle[i];
	    shuffle[i] = shuffle[switcharoo];
	    shuffle[switcharoo] = tmp;
	}
	// done with shuffle/permute initialization.
    }

    /** Self-test method for the class. */
    public static void main(String[] args) {
	PersistentTreeNode<Integer,Integer> root = null;
	Comparator<Integer> c = new Comparator<Integer>() {
	    public int compare(Integer a, Integer b) {
		return a.compareTo(b);
	    }
	};
	int N=1000;
	// number of nodes is 'i'
	// perfectly balanced tree has depth ceil(log2(i))
	for (int i=1; i<=N; i++) {
	    root = put(root, c, new Integer(i), new Integer(i));
	    System.out.println(i+" DEPTH "+depth(root)+" IDEAL "+Util.log2c(i));
	}
	for (int i=N; i>=1; i--) {
	    root = remove(root, c, new Integer(i));
	    System.out.println(i+" DEPTH "+depth(root)+" IDEAL "+Util.log2c(i));
	}
	// XXX not a very good self-test at the moment: no automatic
	//     checking!
    }
}
