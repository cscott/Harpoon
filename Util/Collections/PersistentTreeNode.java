// PersistentTreeNode.java, created Wed Mar 31 18:41:03 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util.Collections;

import harpoon.Util.Util; // for log2c in self-test function.
import java.util.Comparator;
import java.util.Iterator;
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
 * @version $Id: PersistentTreeNode.java,v 1.11 2003-06-08 16:51:06 cananian Exp $
 */
abstract class PersistentTreeNode<N extends PersistentTreeNode<N,K,V>,K,V>
    extends AbstractMapEntry<K,V> implements java.io.Serializable {

    public final K key;
    /* also an optional value field of type 'V'; not included in base class.*/
    public final N left;
    public final N right;

    PersistentTreeNode(K key, N left, N right) {
	this.key = key; this.left= left; this.right = right;
    }
    // ACCESSOR FUNCTIONS for Map.Entry.
    public final K getKey() { return key; }
    public V getValue() { return null; } // can be overridden by subclass

    // For debugging
    public String toString() {
	return "["+key+"->"+getValue()+", left="+left+", right="+right+"]";
    }
    /** Returns the length of the longest path from the root to a leaf. */
    static <N extends PersistentTreeNode<N,K,V>,K,V> int depth(N n) {
	if (n==null) return 0;
	return 1+Math.max(depth(n.left), depth(n.right));
    }
    /** Returns the number of nodes in the tree rooted at <code>n</code>.
     * @return 0 if <code>n==null</code>, else
     *         <code>1+size(n.left)+size(n.right)</code>.
     */
    static <N extends PersistentTreeNode<N,K,V>,K,V> int size(N n) {
	return (n==null) ? 0 : (1 + size(n.left) + size(n.right));
    }

    /** equals() merely checks that key and value are equivalent;
     *  isSame() checks that left and right branches are equivalent, too.
     */
    public boolean isSame(PersistentTreeNode n) {
	if (this==n) return true; // quick common case
	if (null==n) return false; // protect us from evil; this!=null
	return
	    isSame(key,  n.key)  && isSame(getValue(), n.getValue()) &&
	    isSame(left, n.left) && isSame(right, n.right);
    }
    private static boolean isSame(Object o1, Object o2) {
	return (o1==null)?(o2==null):(o1.equals(o2));
    }
    private static boolean isSame(PersistentTreeNode n1,PersistentTreeNode n2){
	return (n1==null)?(n2==null):(n1.isSame(n2));
    }

    // factory class.
    static abstract class Allocator<N extends PersistentTreeNode<N,K,V>,K,V> {
	abstract N newNode(K key, V value, N left, N right);
    }

    // TREE UTILITY FUNCTIONS.
    /** Creates a new node iff the created node would not be identical
     *  to the given <code>n</code>. */
    private static <N extends PersistentTreeNode<N,K,V>,K,V> 
			      N newNode(N n, K key, V value,
					N left, N right,
					Allocator<N,K,V> allocator) {
	if (n != null && n.key.equals(key) && isSame(n.getValue(), value) &&
	    n.left == left && n.right == right)
	    return n;
	// check heap condition
	assert left==null || heapKey(key) < heapKey(left.key);
	assert right==null|| heapKey(key) <= heapKey(right.key);
	// the actual work of allocation is deferred to the allocator.
	// (so that the proper subclass can be created!)
	return allocator.newNode(key, value, left, right);
    }
    /** Creates a new node (possibly reusing existing node n) rebalancing
     *  the left side if necessary. */
    private static <N extends PersistentTreeNode<N,K,V>,K,V> 
			      N newNode_balanceLeft(N n, K key, V value,
						    N left, N right,
						    Allocator<N,K,V> allocator)
    {
	// tie goes to the left (lesser tree key)
	if (left!=null && left!=n.left/*speed optimization*/ &&
	    heapKey(left.key) <= heapKey(key)) { // needs rebalancing!
	    // bring left up to top
	    //     d         b
	    //    / \   ->  / \
	    //   b   e     a   d
	    //  / \           / \
	    // a   c         c   e
	    return newNode(left, left.key, left.getValue(),
			   left.left, newNode(n, key, value,
					      left.right, right, allocator),
			   allocator);
	} else { // okay as is
	    return newNode(n, key, value, left, right, allocator);
	}
    }
    /** Creates a new node (possibly reusing existing node n) rebalancing
     *  the right side if necessary. */
    private static <N extends PersistentTreeNode<N,K,V>,K,V> 
			      N newNode_balanceRight(N n, K key, V value,
						     N left, N right,
						     Allocator<N,K,V>allocator)
    {
	// tie goes to the top (lesser tree key)
	if (right!=null && right!=n.right/*speed optimization*/ &&
	    heapKey(right.key) < heapKey(key)) { // needs rebalancing!
	    // bring right up to top
	    //     b          d
	    //    / \   ->   / \
	    //   a   d      b   e
	    //      / \    / \
	    //     c   e  a   c
	    return newNode(right, right.key, right.getValue(),
			   newNode(n, key, value, left, right.left, allocator),
			   right.right, allocator);
	} else { // okay as is
	    return newNode(n, key, value, left, right, allocator);
	}
    }
    /** Returns the <code>PersistentTreeNode</code> matching <code>key</code>
     *  if any, else <code>null</code>. */
    static <N extends PersistentTreeNode<N,K,V>,K,V>
		      N get(N n, Comparator<K> c, K key) {
	if (n==null) return null; /* no node with this key. */
	int r = c.compare(key, n.key);
	return
	    (r ==0) ? n :
	    (r < 0) ? get(n.left, c, key) : get(n.right, c, key);
    }

    /** Returns a node rooting a tree containing all the mappings in
     *  the tree rooted at the given <code>n</code>, plus a mapping from
     *  <code>key</code> to <code>value</code>. */
    static <N extends PersistentTreeNode<N,K,V>,K,V>
		      N put(N n, Comparator<K> c, K key, V value,
			    Allocator<N,K,V> allocator) {
	if (n==null) return newNode(null, key, value, null, null, allocator);
	
	int r = c.compare(key, n.key);
	if (r==0)
	    // already heap-balanced
	    return newNode(n, key, value, n.left, n.right, allocator);
	if (r < 0)
	    return newNode_balanceLeft(n, n.key, n.getValue(),
				       put(n.left, c, key, value, allocator),
				       n.right,
				       allocator);
	if (r > 0)
	    return newNode_balanceRight(n, n.key, n.getValue(),
					n.left,
					put(n.right, c, key, value, allocator),
					allocator);
	throw new Error("Impossible!");
    }
    /** Returns a node rooting a tree containing all the mappings in
     *  the tree rooted at the given <code>n</code> except that it does
     *  not contain a mapping for <code>key</code>. */
    static <N extends PersistentTreeNode<N,K,V>,K,V>
		      N remove(N n, Comparator<K> c, K key,
			       Allocator<N,K,V> allocator) {
	if (n==null) return null; // key not found.

	int r = c.compare(key, n.key);
	if (r==0) // remove this node.
	    return merge(n.left, n.right, allocator);
	if (r < 0)
	    return newNode_balanceLeft(n, n.key, n.getValue(),
				       remove(n.left, c, key, allocator),
				       n.right,
				       allocator);
	if (r > 0)
	    return newNode_balanceRight(n, n.key, n.getValue(),
					n.left,
					remove(n.right, c, key, allocator),
					allocator);
	throw new Error("Impossible!");
    }
    /** Merge two nodes into one. */
    private static <N extends PersistentTreeNode<N,K,V>,K,V>
			      N merge(N left, N right,
				      Allocator<N,K,V> allocator) {
	if (left==null) return right;
	if (right==null) return left;
	// the node with the smallest heap key goes on top.
	// in case of tie, the smallest tree key goes on top (left node)
	if (heapKey(left.key) > heapKey(right.key))
	    return newNode(null, right.key, right.getValue(),
			   merge(left, right.left, allocator), right.right,
			   allocator);
	else
	    return newNode(null, left.key, left.getValue(),
			   left.left, merge(left.right, right, allocator),
			   allocator);
    }

    /** Define an iterator over a tree (in tree order). */
    public static <N extends PersistentTreeNode<N,K,V>,K,V>
			     Iterator<N> iterator(N root) {
	return new NodeIterator<N,K,V>(root);
    }
    /** An iterator class over a tree of <code>PersistentTreeNode</code>s. */
    private static class NodeIterator<N extends PersistentTreeNode<N,K,V>,K,V>
	extends UnmodifiableIterator<N> {
	NodeList<N,K,V> stack = null;
	NodeIterator(N root) {
	    for (N n=root; n!=null; n=n.left)
		stack = new NodeList<N,K,V>(n, stack);
	}
	public boolean hasNext() { return stack==null; }
	public N next() {
	    N n = stack.head;
	    stack = stack.tail;
	    // now recurse down the left side of the right-hand node
	    for (N nn=n.right; nn!=null; nn=nn.left)
		stack = new NodeList<N,K,V>(nn, stack);
	    // done.
	    return n;
	}
	private static class NodeList<N extends PersistentTreeNode<N,K,V>,K,V>{
	    N head;
	    NodeList<N,K,V> tail;
	    NodeList(N head, NodeList<N,K,V> tail) {
		this.head = head; this.tail = tail;
	    }
	}
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

    /** Sample implementation of <code>PersistentTreeNode</code>.
     *  Note how we implement a fast 'hashCode' method. */
    private static class WithValue<K,V>
	extends PersistentTreeNode<WithValue<K,V>,K,V> {
	final V value;
	/** Hash code of a map implementation with the contents of the
	 *  tree rooted at this node. */
	final int mapHashCode;
	WithValue(K key, V value, WithValue<K,V> left, WithValue<K,V> right) {
	    super(key, left, right);
	    this.value = value;
	    this.mapHashCode = this.hashCode() + // this entry
		((left==null)?0:left.mapHashCode) + // hash of left tree
		((right==null)?0:right.mapHashCode); // hash of right tree
	}
	public V getValue() { return value; }
	/** Return the hash code of a <code>java.util.Map</code> with the
	 *  contents of the tree rooted at this node. */
	int mapHashCode() { return mapHashCode; }
	static class Allocator<K,V>
	    extends PersistentTreeNode.Allocator<WithValue<K,V>,K,V> {
	    WithValue<K,V> newNode(K key, V value,
				   WithValue<K,V> left, WithValue<K,V> right) {
		return new WithValue<K,V>(key, value, left, right);
	    }
	}
    }
    /** Self-test method for the class. */
    public static void main(String[] args) {
	WithValue<Integer,Integer> root = null;
	WithValue.Allocator<Integer,Integer> allocator =
	    new WithValue.Allocator<Integer,Integer>();
	Comparator<Integer> c = new Comparator<Integer>() {
	    public int compare(Integer a, Integer b) {
		return a.compareTo(b);
	    }
	};
	int N=1000;
	// number of nodes is 'i'
	// perfectly balanced tree has depth ceil(log2(i))
	for (int i=1; i<=N; i++) {
	    root = put(root, c, new Integer(i), new Integer(i), allocator);
	    System.out.println(i+" DEPTH "+depth(root)+" IDEAL "+Util.log2c(i));
	}
	for (int i=N; i>=1; i--) {
	    root = remove(root, c, new Integer(i), allocator);
	    System.out.println(i+" DEPTH "+depth(root)+" IDEAL "+Util.log2c(i));
	}
	// XXX not a very good self-test at the moment: no automatic
	//     checking!
    }
}
