import java.util.*;

class RoleDisplay {
    HashMap map;
    public RoleDisplay() {
	map=new HashMap();
    }

    public void register(RoleCombination rc, int[] displayroles) {
	if (map.containsKey(rc)) {
	    ((RoleDisplayEntry)map.get(rc)).add(displayroles);
	} else {
	    RoleDisplayEntry rde=new RoleDisplayEntry(displayroles.length);
	    rde.add(displayroles);
	    map.put(rc, rde);
	}
    }
    
    public RoleDisplayEntry get(RoleCombination rc) {
	return (RoleDisplayEntry)map.get(rc);
    }

    static class RoleDisplayEntry {
	HashSet[] sets;
	public RoleDisplayEntry(int length) {
	    sets=new HashSet[length];
	    for(int i=0;i<length;i++) {
		sets[i]=new HashSet();
	    }
	}
	
	void add(int[] displayroles) {
	    if (displayroles.length!=sets.length) {
		System.out.println("ERROR in RoleDisplayEntry");
		System.exit(-1);
	    }
	    for (int i=0;i<sets.length;i++) {
		sets[i].add(new Integer(displayroles[i]));
	    }
	}
    }
}
