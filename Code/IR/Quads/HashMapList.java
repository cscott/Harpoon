// HashMapList.java, created Tue Aug 10 17:15:58 1999 by root
// Copyright (C) 1999 Brian Demsky <bdemsky@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Quads;

/**
 * <code>HashMapList</code>
 * 
 * @author  Brian Demsky <bdemsky@mit.edu>
 * @version $Id: HashMapList.java,v 1.2 2002-02-25 21:05:12 cananian Exp $
 */

import java.util.ArrayList;
import java.util.HashMap;

class HashMapList {
    ArrayList alist;
    HashMap hashmap;

    HashMapList() {
	this.hashmap=new HashMap();
    }
    void add(Object key,Object member) {
	ArrayList list;
	if (hashmap.containsKey(key))
	    list=(ArrayList) hashmap.get(key);
	else {
	    list=new ArrayList();
	    hashmap.put(key, list);
	}
	list.add(member);
    }
    ArrayList get(Object key) {
	return (ArrayList) hashmap.get(key);
    }
    boolean containsKey(Object key) {
	return hashmap.containsKey(key);
    }
}
