package harpoon.Backend.CSAHack.RegAlloc;
import harpoon.Backend.CSAHack.Graph.Node;
import harpoon.Backend.CSAHack.Graph.Graph;

abstract public class InterferenceGraph extends Graph {
   abstract public Node tnode(harpoon.Temp.Temp temp);
   abstract public harpoon.Temp.Temp gtemp(Node node);
   abstract public MoveList moves();
   public int spillCost(Node node) {return 1;}
}
