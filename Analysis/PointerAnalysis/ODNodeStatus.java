package harpoon.Analysis.PointerAnalysis;
import harpoon.IR.Quads.Quad;

public class ODNodeStatus {

    public boolean onStack     = false;
    public boolean onLocalHeap = false;
    public boolean memalloc_phase = false;
    public boolean syncelim_phase = false;
    public boolean inlining_phase = false;
    public int nCallers = 0;
    public int nInlines = 0;
    public long stack = 0;
    public long thread = 0;
    public long global = 0;
    public long synchro = 0;
    public boolean touched_memalloc_phase = false;
    public boolean touched_syncelim_phase = false;
    public long total_time = 0;
    public long  sync_time = 0;
    public long alloc_time = 0;
    public PANode node = null;
    public int mapups  = 0;
    public int methods = 0;
    public Quad quad = null;
    public int index = -1;

    public ODNodeStatus(){
	this.onStack     = false;
	this.onLocalHeap = false;
	this.nCallers = 0;
	this.nInlines = 0;
    }

    public void incrInlines(){
	nInlines++;
    }

    public String toString(){
	return
	    "ODNodesStatus{Stack " + onStack + "; Thread " + onLocalHeap
	    + " Inlining: " + nInlines + "/" + nCallers + "}";
    }

}
