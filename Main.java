// Main.java, created by cata
// Copyright (C) 2001 Catalin Francu <cata@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.

// This class is only written for debugging purposes

package javax.realtime;
import java.io.*;

public class Main {
	public static void main(String[] argc) {
		ThreadQueue t = new ThreadQueue();
		t.enqueue(1);
		t.enqueue(2);
		t.enqueue(3);
		t.enqueue(4);
		t.enqueue(5);
		System.out.println(t);
		System.out.println(t.remove(3));
		System.out.println(t.remove(1));
		System.out.println(t);
	}
}
