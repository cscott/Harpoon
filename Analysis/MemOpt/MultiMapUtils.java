/*
  Some utilities for MultiMaps
*/

package harpoon.Analysis.MemOpt;

import harpoon.Analysis.GraphColoring.Color;
import harpoon.Analysis.GraphColoring.ColorFactory;
import harpoon.Analysis.GraphColoring.DefaultSparseNode;
import harpoon.Analysis.GraphColoring.SimpleGraphColorer;
import harpoon.Analysis.GraphColoring.SparseGraph;
import harpoon.Analysis.GraphColoring.SparseNode;
import harpoon.Analysis.GraphColoring.UnboundedGraphColorer;
import harpoon.Util.Collections.AggregateSetFactory;
import harpoon.Util.Collections.GenericMultiMap;
import harpoon.Util.Collections.MultiMap;
import harpoon.Util.Collections.WorkSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class MultiMapUtils { 
    /*
      "Closes" the given multi-map, i.e. makes it transitive.
      only nodes accessible from startingNodes are processed.
      Time: O(N*M)
      N: number of nodes in startingNodes
      M: number of edges accessible from startingNodes

      Only uses iterator from startingNodes, i.e. other ops can be slow

      UPDATE: now forbids closures over dontCloseOver. I *really* need this
        capability. dontCloseOver can be null, and it'll be ignored.
        For sane results, dontCloseOver should be a subset of startingNodes.

      FIXME: I have to make this specification clearer. 
    */
    static MultiMap multiMapClosure(MultiMap oldMap, Collection startingNodes,
                                    Set dontCloseOver) {
        MultiMap map = new GenericMultiMap(new AggregateSetFactory());
        
        Set reachable = new HashSet();
        WorkSet workset = new WorkSet();
        
        for (Iterator i = startingNodes.iterator(); i.hasNext(); ) {
            Object x = i.next();
            
            reachable.clear();

            workset.add(x);

            while (!workset.isEmpty()) {
                Object y = workset.removeFirst();

                if (dontCloseOver != null && y!= x &&
                    dontCloseOver.contains(y)) continue;

                for (Iterator j = oldMap.getValues(y).iterator();
                     j.hasNext(); ) {
                    Object z = j.next();

                    if (reachable.add(z) && z!=x) {
                            workset.addLast(z);
                        }
                }
            
            }
            map.addAll(x, reachable);
        }

        return map;
    }


    // compute map inverse, as per relevantNodes (i.e, only relevantNodes
    //   will appear on the right hand side
    // Only uses iterator from relevantNodes, i.e. other ops can be slow
    // Relevantnodes can be null to invert the whole map
     static MultiMap multiMapInvert(MultiMap oldMap, Collection relevantNodes) {
        MultiMap map = new GenericMultiMap(new AggregateSetFactory());

        if (relevantNodes == null) relevantNodes = new ArrayList(oldMap.keySet());

        for (Iterator i = relevantNodes.iterator(); i.hasNext(); ) {
            Object x = i.next();

            for (Iterator j = oldMap.getValues(x).iterator(); j.hasNext(); ) {
                Object y = j.next();
                map.add(y, x);

            }
        }

        return map;
    }

    // computes the union of the mappings of the elements in the collection
    // Only uses iterator from keys, i.e. other ops can be slow
    static Set multiMapUnion(MultiMap map, Collection keys) {
        Set union = new HashSet();
        
        for (Iterator i = keys.iterator(); i.hasNext(); ) {
            Object x = i.next();
            union.addAll(map.getValues(x));
        }

        return union;
    }

    // filters the given multimap through the given key and value sets
    //   only mappings with key in relevantKeys and values in relValues
    //   are kept
    // one or both of relKeys and relValues can be null, in which case they
    //   will be ignored. also, they can be equal
    // this uses the contains operations of both collections, which should
    //   therefore be fast
    static MultiMap multiMapFilter(MultiMap oldMap, Collection relevantKeys, Collection relevantValues) {
        MultiMap newMap = new GenericMultiMap(new AggregateSetFactory());

        for (Iterator it = oldMap.keySet().iterator(); it.hasNext(); ) {
            Object key = it.next();

            // add this key?
            if (relevantKeys == null ||
                relevantKeys.contains(key)) {

                for (Iterator it2 = oldMap.getValues(key).iterator(); it2.hasNext(); ) {
                    Object value = (Object) it2.next();
                    if (relevantValues == null ||
                        relevantValues.contains(value)) {
                        newMap.add(key, value);
                    }
                }
            }
        }

        return newMap;
    }

    // similar to the above, but filters only keys
    // optimized on the assumption that:
    //   relevantKeys is a small subset of oldMap.keySet()
    // uses only the iterator in relevantKeys(), i.e. other ops can be slow
    static MultiMap multiMapFilterKeys(MultiMap oldMap, Collection relevantKeys) {
        MultiMap newMap = new GenericMultiMap(new AggregateSetFactory());

        for (Iterator it = relevantKeys.iterator(); it.hasNext(); ) {
            Object key = it.next(); 
            newMap.addAll(key, oldMap.getValues(key));
        }

        return newMap;
    }

    // add a value for all the specified keys
    static void multiMapAddAll(MultiMap map, Collection keys, Object value) {
        for (Iterator it = keys.iterator(); it.hasNext(); ) {
            Object key = it.next();
            map.add(key, value);
        }
    }

    // this does not really belong here...
    // optimized on the assumption that:
    //   - b is a large collection with a fast contains() operation
    //   - a is a rather small collection (does not have to have a fast contains()

    static Set intersect(Collection a, Collection b) {
        Set intersection = new HashSet();

        for (Iterator i = a.iterator(); i.hasNext(); ) {
            Object x = i.next();
            if (b.contains(x)) {
                intersection.add(x);
            }
        }

        return intersection;
    }

    static void ensureSymmetric(MultiMap map) {
        List keyList = new ArrayList(map.keySet());

        for (Iterator it = keyList.iterator(); it.hasNext(); ) {
            Object key = it.next();

            multiMapAddAll(map, map.getValues(key), key);
        }
    }

    // this does not belong here and WILL be deleted
    // new and better code is in MyGraphColorer
    // keeping it around just in case for a few more days
    static Collection computeCompatibleClasses(final MultiMap incompatible) {

        // decreasing order of incompatibilites
        Comparator myComparator = new Comparator() {
                public int compare(Object a, Object b) {
                    int sizeA = incompatible.getValues(a).size();
                    int sizeB = incompatible.getValues(b).size();

                    return sizeB - sizeA;
                }
                
            };

        
        Object[] nodes = incompatible.keySet().toArray();

        // sort on decreasing order of #incompatible
        // Arrays.sort(nodes, myComparator);

        Collection classes = new LinkedList();
        
        for (int i = 0; i < nodes.length; i++) {
            Object candidate = nodes[i];
            
            boolean assigned = false;

            for (Iterator it = classes.iterator(); it.hasNext() && !assigned; ) {
                Collection members = (Collection) it.next();
                boolean compatible = true;
                for (Iterator it2 = members.iterator(); it2.hasNext() && compatible; ) {
                    Object member = it2.next();

                    compatible &= !incompatible.contains(candidate, member);
                }

                if (compatible) {
                    members.add(candidate);
                    assigned = true;
                }
            }

            if (!assigned) {
                Collection newClass = new LinkedList();
                newClass.add(candidate);

                classes.add(newClass);
            }
        }

        return classes;
    }

    // alternate algorithm for above using Felix's UnboundedGraphColoring
    //   this will be deleted too, since that alg performs very poorly
    // currently does not return anything, just prints a result for comparison
    static Collection computeCompatibleClassesAlt(final MultiMap incompatible) {
        Map tokens2nodes = new HashMap();
        SparseGraph graph = new SparseGraph();

        // nodes
        for (Iterator it = incompatible.keySet().iterator(); it.hasNext(); ) {
            Object token = it.next();
            SparseNode node = new DefaultSparseNode();
            tokens2nodes.put(token, node);
            graph.addNode(node);
        }

        // edges
        for (Iterator it = incompatible.keySet().iterator(); it.hasNext(); ) {
            Object token1 = it.next();
            SparseNode node1 = (SparseNode) tokens2nodes.get(token1);
            for (Iterator it2 = incompatible.getValues(token1).iterator();
                 it2.hasNext(); ) {
                Object token2 = it2.next();
                SparseNode node2 = (SparseNode) tokens2nodes.get(token2);
                graph.makeEdge(node1, node2);
            }
        }

        ColorFactory cfactory = new ColorFactory() {
                protected Color newColor() {
                    return new Color() {
                            };
                }
            };
        
        UnboundedGraphColorer colorer =
            new UnboundedGraphColorer(new SimpleGraphColorer(),
                                      cfactory);

        colorer.findColoring(graph);

        System.out.println("*** # classes via alt method: "
                           + cfactory.getColors().size());

        return null;
    }        
    
}
