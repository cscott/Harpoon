package MCC.IR;

/**
 * MissingSetDescriptor
 *
 * represents a set in the model space
 */

import java.util.*;

public class MissingSetDescriptor extends SetDescriptor {

    public MissingSetDescriptor(String name) {
        super(name);
    }

    public boolean isPartition() {
        throw new IRException();
    }
    
    public void isPartition(boolean newvalue) {
        throw new IRException();
    }

    public void setType(TypeDescriptor td) {
        throw new IRException();
    }

    public TypeDescriptor getType() {
        throw new IRException();
    }

    public void addSubset(SetDescriptor sd) {
        throw new IRException();
    }

    public Vector getSubsets() {
        throw new IRException();
    }
}
