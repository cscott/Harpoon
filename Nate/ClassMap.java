package harpoon.Nate;

import harpoon.ClassFile.*;

import java.util.*;

class ClassMap {
    Hashtable map;

    ClassMap(){
	map = new Hashtable();
    }

    HClassSyn get (HClass key){
	return (HClassSyn) map.get (key);
    }

    boolean contains (HClass key){
	return map.contains (key);
    }

    void put (HClass key, HClassSyn value){
	map.put (key, value);
    }
    Enumeration getElements (){
	return map.elements();
    }

}
