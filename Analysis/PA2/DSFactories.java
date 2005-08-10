// DSFactories.java, created Thu Jul  7 05:57:46 2005 by salcianu
// Copyright (C) 2003 Alexandru Salcianu <salcianu@alum.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PA2;

import java.util.Set;

import jpaul.DataStructs.Factory;

import jpaul.DataStructs.HashSetFactory;
import jpaul.DataStructs.TreeSetFactory;
import jpaul.DataStructs.COWSetFactory;

import jpaul.DataStructs.TreeMapFactory;
import jpaul.DataStructs.HashMapFactory;
import jpaul.DataStructs.SharedTreeMapFactory;

import jpaul.DataStructs.Relation;
import jpaul.DataStructs.RelationFactory;
import jpaul.DataStructs.SetFactory;
import jpaul.DataStructs.MapSetRelationFactory;
import jpaul.DataStructs.COWRelationFactory;

import harpoon.ClassFile.HField;

/**
 * <code>DSFactories</code>
 * 
 * @author  Alexandru Salcianu <salcianu@alum.mit.edu>
 * @version $Id: DSFactories.java,v 1.1 2005-08-10 02:58:19 salcianu Exp $
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
	(new TreeMapFactory<PANode,Relation<HField,PANode>>(PAUtil.nodeComparator),
	 new COWRelationFactory<HField,PANode>
	 (new MapSetRelationFactory<HField,PANode>
	  (new TreeMapFactory<HField,Set<PANode>>(PAUtil.fieldComparator),
	   new COWSetFactory<PANode>
	   (new TreeSetFactory<PANode>(PAUtil.nodeComparator)))));

    /*
    public static final SetFactory<PANode> nodeSetFactory =
	new HashSetFactory<PANode>();
    */


    /** Factory used to generate all set of nodes (returned/thrown
        nodes, escaped nodes etc.) */
    public static final SetFactory<PANode> nodeSetFactory =
	new COWSetFactory<PANode>
	(new TreeSetFactory<PANode>(PAUtil.nodeComparator));

    /*
    public static final RelationFactory<PANode,PANode> mappingFactory =
	new MapSetRelationFactory<PANode,PANode>
	(new HashMapFactory<PANode,Set<PANode>>(),
	 new HashSetFactory<PANode>());
    */


    public static final RelationFactory<PANode,PANode> mappingFactory =
	new MapSetRelationFactory<PANode,PANode>
	(new TreeMapFactory<PANode,Set<PANode>>(PAUtil.nodeComparator),
	 new TreeSetFactory<PANode>(PAUtil.nodeComparator));
    
}
