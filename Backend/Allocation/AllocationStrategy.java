package harpoon.Backend.Allocation;

import harpoon.IR.Tree.Exp;

public interface AllocationStrategy {
    public Exp malloc(Exp size);
}
