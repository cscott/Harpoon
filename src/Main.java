package imagerec;

public class Main {
    public static void main(String args[]) {
	(new Load("../movie/mov3.gz", 139, new Display("foo"))).process(null);
    }
}
