// DSFactories.java, created Thu Jul  7 05:57:46 2005 by salcianu
// Copyright (C) 2003 Alexandru Salcianu <salcianu@alum.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PA2;

import java.util.Set;

import jpaul.DataStructs.Relation;

import jpaul.DataStructs.Factory;
import jpaul.DataStructs.SetFactory;
import jpaul.DataStructs.RelationFactory;

import jpaul.DataStructs.SetFacts;
import jpaul.DataStructs.MapFacts;
import jpaul.DataStructs.RelFacts;

import jpaul.DataStructs.Pair;

import harpoon.ClassFile.HField;

/**
 * <code>DSFactories</code>
 * 
 * @author  Alexandru Salcianu <salcianu@alum.mit.edu>
 * @version $Id: DSFactories.java,v 1.4 2005-08-31 02:37:54 salcianu Exp $
 */
public class DSFactories {

    /*
    public static final Factory<PAEdgeSet> edgeSetFactory = 
	new MapRelEdgeSetFactory(new HashMapFactory<PANode,Relation<HField,PANode>>(),
				 new MapSetRelationFactory<HField,PANode>());
    */
    

    /** Factory used to generate all set of edges (inside and outside). */
    public static final Factory<PAEdgeSet> edgeSetFactory = 
	new MapRelEdgeSetFactory
	(MapFacts.<PANode,Relation<HField,PANode>>tree(PAUtil.nodeComparator),
	 RelFacts.<HField,PANode>cow
	 (RelFacts.<HField,PANode>mapSet
	  (MapFacts.<HField,Set<PANode>>tree(PAUtil.fieldComparator),
	   SetFacts.<PANode>cow
	   (SetFacts.<PANode>tree(PAUtil.nodeComparator)))));

    /*
    public static final SetFactory<PANode> nodeSetFactory =
	new HashSetFactory<PANode>();
    */


    /** Factory used to generate all set of nodes (returned/thrown
        nodes, escaped nodes etc.) */
    public static final SetFactory<PANode> nodeSetFactory =
	SetFacts.<PANode>cow
	(SetFacts.<PANode>tree(PAUtil.nodeComparator));

    /*
    public static final RelationFactory<PANode,PANode> mappingFactory =
	new MapSetRelationFactory<PANode,PANode>
	(new HashMapFactory<PANode,Set<PANode>>(),
	 new HashSetFactory<PANode>());
    */


    public static final RelationFactory<PANode,PANode> mappingFactory =
	RelFacts.<PANode,PANode>mapSet
	(MapFacts.<PANode,Set<PANode>>tree(PAUtil.nodeComparator),
	 SetFacts.<PANode>tree(PAUtil.nodeComparator));


    /** Factory used to generate the sets of mutated abstract fields. */
    public static final SetFactory<Pair<PANode,HField>> abstractFieldSetFactory =
	SetFacts.<Pair<PANode,HField>>hash();

}
