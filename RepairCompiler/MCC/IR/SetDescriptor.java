/**
 * SetDescriptor
 *
 * represents a set in the model space
 */

package MCC.IR;
import java.util.*;

public class SetDescriptor extends Descriptor {
    
    TypeDescriptor type;
    boolean partition;
    Vector subsets;       

    public SetDescriptor(String name) {
        super(name);
        subsets = new Vector();
        partition = false;
    }

    public static Set expand(Set descriptors) {
        HashSet expanded = new HashSet();
        Iterator d = descriptors.iterator();
        
        while (d.hasNext()) {
            Descriptor descriptor = (Descriptor) d.next();
            
            if (descriptor instanceof SetDescriptor) {
                expanded.addAll(((SetDescriptor) descriptor).allSubsets());
            } else
		expanded.add(descriptor); /* Still need the descriptor */
        }

        expanded.addAll(descriptors);
        return expanded;        
    }

    public boolean isPartition() {
        return partition;
    }
    
    public void isPartition(boolean newvalue) {
        partition = newvalue;
    }

    public void setType(TypeDescriptor td) {
        type = td;
    }

    public TypeDescriptor getType() {
        return type;
    }

    public void addSubset(SetDescriptor sd) {
        subsets.addElement(sd);
    }

    public Vector getSubsets() {
        return subsets;
    }

    public Iterator subsets() {
        return subsets.iterator();
    }

    public boolean isSubset(SetDescriptor sd) {
        if (sd == this) {
            return true;
        }
 
        for (int i = 0; i < subsets.size(); i++) {
            SetDescriptor subset = (SetDescriptor) subsets.elementAt(i);
            if (subset.isSubset(sd)) {
                return true;
            }
        }

        return false;
    }

    public Set allSubsets() {
        Set v = new HashSet();
        v.add(this);

        for (int i = 0; i < subsets.size(); i++) {
            SetDescriptor subset = (SetDescriptor) subsets.elementAt(i);
            v.addAll(subset.allSubsets());
        }
        
        return v;
    }        

}
