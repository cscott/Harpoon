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

    public static final Usage NONE = new Usage("NONE");
    public static final Usage IMAGE = new Usage("IMAGE");
    public static final Usage INVIMAGE = new Usage("INVIMAGE");
    public static final Usage BOTH = new Usage("BOTH");

    public static class Usage {
        String name;
        private Usage(String name) { this.name = name; }
        public String toString() { return name; }
    }
    
    Usage usage = NONE;

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

    public void addUsage(Usage newusage) {
        if (newusage == null || newusage == NONE || newusage == BOTH) {
            throw new IllegalArgumentException();
        }
        
        if (usage == BOTH) {
            return;
        } else if (usage == IMAGE && newusage == INVIMAGE) {
            usage = BOTH;
        } else if (usage == INVIMAGE && newusage == IMAGE) {
            usage = BOTH;
        } else if (usage == NONE) {
            usage = newusage;
        }             
    }

    public boolean testUsage(Usage testusage) {
        return (usage == BOTH) || (testusage == usage);
    }

}









