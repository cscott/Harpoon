package imagerec;

public class Main {
    public static void main(String args[]) {
// 	Node[] d = new Node[] { new Display("original"), new Display("robertsCross"),
// 				new Display("threshold"), new Display("hysteresis"),
// 				new Display("thinning"), new Display("pruning")};
// 	Node n6 = new Pruning(d[5]);
// 	Node n5 = new Thinning(new Node(d[4],n6));
// 	Node n4 = new Hysteresis(new Node(d[3],n5));
// 	Node n3 = new SearchThreshold(new Node(d[2], n4));
// 	Node n2 = new RobertsCross(new Node(d[1], n3));
// 	Node n1 = new Load("../movie/mov3.gz", 139, new Node(d[0], n2));


	Node n2 = new Hysteresis(new Thinning(new Pruning(new Display("output"))));
	Node n1 = new Load("../movie/mov3.gz", 139, new RobertsCross(new SearchThreshold(n2)));
	while (true) {
	    n1.run();
	}
    }
}
