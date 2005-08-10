// MapRelEdgeSetFactory.java, created Thu Jul  7 05:46:11 2005 by salcianu
// Copyright (C) 2003 Alexandru Salcianu <salcianu@alum.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PA2;

import java.util.Map;

import jpaul.DataStructs.Factory;
import jpaul.DataStructs.Relation;

import harpoon.ClassFile.HField;

/**
 * <code>MapRelEdgeSetFactory</code>
 * 
 * @author  Alexandru Salcianu <salcianu@alum.mit.edu>
 * @version $Id: MapRelEdgeSetFactory.java,v 1.1 2005-08-10 02:58:19 salcianu Exp $
 */
public class MapRelEdgeSetFactory implements Factory<PAEdgeSet> {
    public MapRelEdgeSetFactory(Factory<Map<PANode,Relation<HField,PANode>>> mapFact,
				Factory<Relation<HField,PANode>> relFact) {
	this.mapFact = mapFact;
	this.relFact = relFact;
    }

    private final Factory<Map<PANode,Relation<HField,PANode>>> mapFact;
    private final Factory<Relation<HField,PANode>> relFact;

    public PAEdgeSet create() {
	return new MapRelPAEdgeSet(mapFact, relFact);
    }

    public PAEdgeSet create(PAEdgeSet edges) {
	return (PAEdgeSet) edges.clone();
    }
}
