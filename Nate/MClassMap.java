package harpoon.Nate;

import harpoon.ClassFile.*;

import java.util.*;

class MClassMap {
    Hashtable map;

    MClassMap(){
	map = new Hashtable();
    }

    MClass get (HClass key){
	return (MClass) map.get (key);
    }

    boolean containsKey (HClass key){
	return map.containsKey (key);
    }

    void put (HClass key, MClass value){
	map.put (key, value);
    }

    void remove (HClass key){
	map.remove (key);
    }

    Enumeration elements (){
	return map.elements();
    }

}
