import java.util.*;
import harpoon.ClassFile.*;

class test {
    public static void main(String argv[]) {
        test.test(false);
    }
    public static int test(boolean tt) {
	int i=1;
	if (tt)
		i=3;
	return i;
    }
}

