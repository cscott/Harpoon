package harpoon.Nate;

import harpoon.ClassFile.*;

import java.util.*;

public class MethodMap {
    Hashtable map;

    public MethodMap(){
	map = new Hashtable();
    }

    public HMethodSyn get (HMethod key){
	return (HMethodSyn) map.get (key);
    }

    public void put (HMethod key, HMethodSyn value){
	map.put (key, value);
    }

    public Enumeration elements (){
	return map.elements();
    }
    
    public boolean contains (HMethodSyn value) {
	return map.contains(value);
    }

}


