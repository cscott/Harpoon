package MCC.IR;

import java.util.*;

public class SymbolTable {

    private Hashtable table;
    private SymbolTable parent;
  
    public SymbolTable() {
	table = new Hashtable();
	this.parent = null;
    }

    public SymbolTable(SymbolTable parent) {
	table = new Hashtable();
	this.parent = parent;
    }

    //public void add(String name, Descriptor d) {
	//table.put(name, d);
    //}

    public void add(Descriptor d) {
	table.put(d.getSymbol(), d);
    }
    
    public void add(String name, Descriptor d) {
	table.put(name, d);
	
    }

    public void dump() {
	Enumeration e = getDescriptors();
	while (e.hasMoreElements()) {
	    Descriptor d = (Descriptor) e.nextElement();
	    System.out.println(d.getSymbol());
	}
	if (parent != null) {
	    System.out.println("parent:");
	    parent.dump();
	}
    }
    
    public Descriptor get(String name) {
	Descriptor d = (Descriptor) table.get(name);
	if (d == null && parent != null) {
	    return parent.get(name);
	} else {
	    return d;
	}
    }

    public Descriptor getFromSameScope(String name) {
	return (Descriptor)table.get(name);
    }
    
    public Enumeration getNames() {
	return table.keys();
    }

    public Enumeration getDescriptors() {
	return table.elements();
    }

    public Iterator descriptors() {
        return table.values().iterator();
    }

    public Vector getAllDescriptors() {
	Vector d;
	if (parent == null) {
	    d = new Vector();
	} else {
	    d = parent.getAllDescriptors();
	}

	Enumeration e = getDescriptors();
	while(e.hasMoreElements()) {
	    d.addElement(e.nextElement());
	}

	return d;
    }

    public boolean contains(String name) {
        return (get(name) != null);
    }
	    
	
    public int size() {
	return table.size();
    }

    public int sizeAll() {
	if (parent != null) {
	    return parent.sizeAll() + table.size();
	} else {
	    return table.size();
	}
    }

    public SymbolTable getParent() {
	return parent;
    }
    
    public void setParent(SymbolTable parent) {
	this.parent = parent;
    }

    /**
     * Adds contents of st2.table to this.table and returns a
     * Vector of shared names, unless there are no shared names,
     * in which case returns null.
     */
    public Vector merge(SymbolTable st2) {
        Vector v = new Vector();
        Enumeration names = st2.table.keys();

        while (names.hasMoreElements()) {
            Object o = names.nextElement();

            if (table.containsKey(o)) {
                v.addElement(o);
            } else {
                table.put(o, st2.table.get(o));
            }
        }

        if (v.size() == 0) {
            return null;
        } else {
            return v;
        }
    }

    public String toString() {
        return "ST: " + table.toString();               
    }
}
