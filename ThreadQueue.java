// PriorityScheduler.java, created by cata
// Copyright (C) 2001 Catalin Francu <cata@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package javax.realtime;

public class ThreadQueue {
	private ThreadList head;
	private ThreadList tail;

	// Leaves threadID undefinded!
	public ThreadQueue() {
		head = tail = null;
	}

	public ThreadQueue(long threadID) {
		head = tail = new ThreadList(threadID);
	}

	public void enqueue(long threadID) {
// 		System.out.println("Enqueueing" + threadID);
		if (head == null)
			head = tail = new ThreadList(threadID);
		else
			tail = tail.addToTail(threadID);
// 		System.out.println("Done! " + toString());
	}

	// THROWS: NullPointerException if dequeue() is called on an empty list
	public long dequeue() {
// 		System.out.println("Dequeueing");
		long result = head.threadID;
		head = head.next;
		if (head == null)
			tail = null;
// 		System.out.println("Done! " + toString());
		return result;
	}

	// Dequeues the first element, returns it and enqueues it again
	// If the queue is empty, returns 0
	public long roll() {
		if (isEmpty()) return 0;

// 		System.out.println("Rolling");
		tail.next = head;
		tail = head;
		head = head.next;
		tail.next = null;
// 		System.out.println("Done! " + toString());
		return tail.threadID;
	}

	// Returns true iff the element was found in the queue
	public boolean remove(long threadID) {
		// Want to delete the first element?
		if(head.threadID == threadID) {
// 			System.out.println("Removing first");
			head = head.next;
			if (head == null)
				tail = null;
// 			System.out.println("Done! " + toString());
			return true;
		}
		
// 		System.out.println("Removing other");
		ThreadList t1 = head, t2 = head.next;
		while(t2 != null) {
	    if(t2.threadID == threadID) {
				t1.next = t2.next;
				if(t1.next == null)
					tail = t1;
				return true;
	    }
			t1 = t2; t2 = t2.next;
		}
// 		System.out.println("Done! " + toString());
		return false;
	}

	public boolean isEmpty() {
		return (head == null);
	}

	public String toString() {
		return "ThreadQueue" + ((head == null) ? "[]" : head.toString());
	}

}
