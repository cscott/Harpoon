package MCC.IR;

/**
 * RelationDescriptor
 *
 * represents a set in the model space
 */

public class RelationDescriptor extends Descriptor {

    SetDescriptor domain;
    SetDescriptor range;
    boolean bStatic;

    public RelationDescriptor(String name) {
        super(name);
        bStatic = false;
        domain = null;
        range = null;
    }
    
    public boolean isStatic() {
        return bStatic;
    }
    
    public void isStatic(boolean newvalue) {
        bStatic = newvalue;
    }

    public void setDomain(SetDescriptor td) {
        domain = td;
    }

    public SetDescriptor getDomain() {
        return domain;
    }

    public void setRange(SetDescriptor td) {
        range = td;
    }

    public SetDescriptor getRange() {
        return range;
    }

}
