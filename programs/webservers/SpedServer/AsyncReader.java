
import java.io.*

class AsyncReader extends Reader {

	private nThreads;
	
	public AsyncReader(int num_threads) {
		nThreads = num_threads;
	}

		//
		// required methods
		//
	void close() {
	}
	
	int read(char[] cbuf, int off, int len) {
	}
	
		//
		// extras
		//
	void mark(int readAheadLimit) {
	}

	boolean markSupported() {
		return false;
	}
	
	boolean ready() {
	}

	void reset() {
	}

	long skip(long n) {
	}
	
}
