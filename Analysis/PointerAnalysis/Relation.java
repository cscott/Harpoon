// Relation.java, created Tue Jan 11 14:52:48 2000 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PointerAnalysis;

import java.util.Collection;
import java.util.Set;
import java.util.Map;

import harpoon.Util.PredicateWrapper;

/**
 * <code>Relation</code> is a mathematical relation, accepting one to many
 * and many to one mappings.
 *
 * <p>It is similar to harpoon.Util.Collections.MultiMap but it is intended
 * to be simpler and better tailored for the implementation of the Pointer
 * Analysis algorithm.
 *
 * @author  Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
 * @version $Id: Relation.java,v 1.2 2005-08-17 23:34:01 salcianu Exp $
 */
public interface Relation {
    
    /** Adds the pair <code>&lt;key, value&gt;</code> to the relation.
	Returns <code>true</code> if the new relation is bigger. */
    public boolean add(Object key, Object value);


    /** Adds a relation from <code>key</code> to each element of the set
	<code>values</code>. <code>values</code> should not contain
	duplicated elements.
	Returns <code>true</code> if the new relation is bigger. */
    public boolean addAll(Object key, Collection values);


    /** Removes the relation between <code>key</code> and 
	<code>value</code>. */ 
    public void remove(Object key, Object value);


    /** Removes the relation between <code>key</code> and 
	any element from <code>values</code>. */
    public void removeAll(Object key, Collection values);


    /** Removes all the relations attached to <code>key</code>. */
    public void removeKey(Object key);


    /** Removes all the keys that satisfy <code>predicate.check()</code>. */
    public void removeKeys(PredicateWrapper predicate);


    /** Removes all the values that satisfy <code>predicate.check()</code>. */
    public void removeValues(PredicateWrapper predicate);


    /** Removes all the relations involving at least one object that
	satisfy <code>predicate.check()</code>. */
    public void removeObjects(PredicateWrapper predicate);


    /** Checks the existence of the relation <code>&lt;key,value&gt;</code>. */
    public boolean contains(Object key, Object value);


    /** Checks the existence of the <code>key</code> key in this relation. */
    public boolean containsKey(Object key);


    /** Tests if this relation is empty or not. */
    public boolean isEmpty();


    /** Returns the image of <code>key</code> through this relation.
	The returned collection is guarranted not to contain duplicates.
	Can return <code>null</code> if no value is attached to key.
	If the result is non-null, additions and removals on the returned
	collection take effect on the relation. */
    public Set getValues(Object key);


    /** Returns all the keys appearing in this relation. The result is
	guaranted not to contain duplicates. */
    public Set keys();


    /** Returns all the values appearing in this relation. */
    public Set values();


    /** Combines <code>this</code> relation with relation
	<code>rel</code>.  A <code>null</code> parameter is considered
	to be an empty relation.

	@return <code>true</code> iff <code>this</code> relation has
	changed. */
    public boolean union(Relation rel);


    /** Checks the equality of two relations */
    public boolean equals(Object o);


    /** Returns the subrelation of this relation that contains
	only the keys that appear in <code>selected_keys</code>. */
    public Relation select(Collection selected_keys);


    /** Visits all the entries <code>&lt;key,value&gt;</code> of
	<code>this</code> relation and calls <code>visitor.visit</code>
	on each of them. */
    public void forAllEntries(RelationEntryVisitor visitor);


    /** Clones this relation. */
    public Object clone();


    /** Revert <code>this</code> relation and store the result into
	the relation <code>result</code>. &lt;a,b&gt; appears in the
	reverse relation iff &lt;b,a&gt; appears in <code>this</code>
	relation.  Returns the new relation (ie,
	<code>result</code>). */
    public Relation revert(Relation result);

    /** Convert <code>this</code> relation through the mapping
	<code>map</code>.  The converted mapping contains all pairs
	<code>(a,b)</code> such that there exists <code>c,d</code>
	such that <code>(c,d)</code> appears in <code>this</code>
	mapping, and map maps c to a and d to b.  If an object is not
	mapped to anything by <code>map</code>, it will be mapped to
	itself by default.  The result is stored in
	<code>result</code>.  Returns the converted mapping (ie,
	result).  */
    public Relation convert(Map map, Relation result);
}
