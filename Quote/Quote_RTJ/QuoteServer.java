import java.io.*; // Import the package names to be 
import java.net.*; // used by this application. 
import java.util.*; 

/** 
* This is an application which implements our stock 
* quote application protocol to provide stock quotes. 
* @author David W. Baker 
* @version 1.1 
*/ 
public class QuoteServer { 
    // The port on which the server should listen. 
    int numStocks = 40; 

    private static int SERVER_PORT = 1701; 
    // Queue length of incoming connections. 
    private static final int MAX_CLIENTS = 50; 
    // File that contains the stock data of format: 
    // <stock-id> <stock information> 
    private static final File STOCK_QUOTES_FILE = 
	new File("stockquotes.txt"); 
    private ServerSocket listenSocket = null; 
    private String[] stockInfo; 
    private long stockFileMod; 
    // A boolean used to keep the server looping until 
    // interrupted. 
    private boolean keepRunning = true; 

    /** 
     * Starts up the application. 
     * @param args Ignored command line arguments. 
     */ 
    public static void main(String[] args) { 
	QuoteServer.SERVER_PORT=Integer.parseInt(args[0]);
	QuoteServer server = new QuoteServer(); 
	server.serveQuotes(); 
    } 

    /** 
     * The constructor creates an instance of this class, 
     * loads the stock data, and then our server listens 
     * for incoming clients. 
     */ 
    public QuoteServer() {
	// Load the quotes and exit if it is unable to do so.
	stockInfo = new String[numStocks]; 

	if (!loadQuotes()) System.exit(1);
	try {
	    // Create a listening socket. 
	    listenSocket =
		new ServerSocket(SERVER_PORT,MAX_CLIENTS);
	} catch(IOException excpt) {
	    System.err.println("Unable to listen on port " +
			       SERVER_PORT + ": " + excpt);
	    System.exit(1);
	}
    }

    /** 
     * This method loads in the stock data from a file. 
     */ 
    protected boolean loadQuotes() { 
	String fileLine; 
	StringBuffer inputBuffer = new StringBuffer(); 

	/* try { 
	   // Create a decorated stream to the data file. 
	   DataInputStream stockInput = new DataInputStream( 
	   new FileInputStream(STOCK_QUOTES_FILE)); 
	   // Read in each line. 
	   while ((fileLine = stockInput.readLine()) != null) { 
	   // Put line into a buffer. 
	   inputBuffer.append(fileLine + "\n"); 
	   numStocks++; // Increment the counter. 
	   } 
	   stockInput.close(); 
	   // Store the last modified timestamp. 
	   stockFileMod = STOCK_QUOTES_FILE.lastModified(); 
	   } catch(FileNotFoundException excpt) { 
	   System.err.println("Unable to find file: " + excpt); 
	   return false; 
	   } catch(IOException excpt) { 
	   System.err.println("Failed I/O: " + excpt); 
	   return false; 
	   } */


	// Create an array of strings for each data file line. 

	String inputString = inputBuffer.toString(); 
	// Pointers for creating substrings. 
	//int stringStart = 0,stringEnd = 0; 
	for (int index = 0; index < numStocks; index ++) { 
	    // Find the end of line. 
	    //stringEnd = inputString.indexOf("\n",stringStart); 
	    // If there is no more \n, then take the rest 
	    // of inputString. 
	    //if (stringEnd == -1) { 
		stockInfo[index] = index+" "+(new Double(Math.random()*200)).intValue();
		// Otherwise, take from the start to the \n 
		//} else { 
		//stockInfo[index] = 
		//    inputString.substring(stringStart,stringEnd); 
		//} 
	    // Increment the start of the substring. 
		//stringStart = stringEnd + 1; 
	} 
	return true; 
    } 

    /** 
     * This method waits to accept incoming client 
     * connections. 
     */ 
    public void serveQuotes() { 
	Socket clientSocket = null; 
	
	try { 
	    while(keepRunning) { 
		// Accept a new client. 
		clientSocket = listenSocket.accept(); 
		// Ensure that the data file hasn't changed; if 
		// so, reload it. 
		//if (stockFileMod != 
		//    STOCK_QUOTES_FILE.lastModified()) { 

		loadQuotes(); 

	        //} 
		// Create a new handler. 
		startserver(clientSocket,stockInfo);
	    } 
	    listenSocket.close(); 
	} catch(IOException excpt) { 
	    System.err.println("Failed I/O: "+ excpt); 
	} 
    } 

    public static void startserver(Socket clientSocket, String[] stockInfo) {
	StockQuoteHandler newHandler = new 
	    StockQuoteHandler(clientSocket,stockInfo);
	
	Thread newHandlerThread = new Thread(newHandler); 
	newHandlerThread.start(); 
    }

    /** 
     * This method allows the server to be stopped. 
     */ 
    protected void stop() { 
	if (keepRunning) { 
	    keepRunning = false; 
	} 
    } 
} 

/** 
 * This class use used to manage a connection to 
 * a specific client. 
 */ 
class StockQuoteHandler implements Runnable { 
    private Socket mySocket = null; 
    private String[] stockInfo; 

     /** 
     * The constructor sets up the necessary instance 
     * variables. 
     * @param newSocket Socket to the incoming client. 
     * @param info The stock data. 
     * @param time The time when the data was loaded. 
     */ 
    public StockQuoteHandler(Socket newSocket, 
			     String[] info) { 
	mySocket = newSocket; 
	stockInfo = info; 
    } 

    /** 
     * This is the thread of execution which implements 
     * the communication. 
     */ 



    public void run() { 
	PrintStream clientSend = null; 
	BufferedReader clientReceive = null; 

	String nextLine; 
	String quoteID; 
	String quoteResponse; 
	try { 
	    clientSend = 
		new PrintStream(mySocket.getOutputStream()); 
	    clientReceive = 
		new BufferedReader
		    (new InputStreamReader(mySocket.getInputStream())); 

	    clientSend.println("+HELLO 1/1/00"); 
	    clientSend.flush(); 
	    // Read in a line from the client and respond. 
	    while((nextLine = clientReceive.readLine()) != null) {
		nextLine = nextLine.toUpperCase(); 
		// QUIT command. 
		if (nextLine.indexOf("QUIT") == 0) break; 
		// STOCK command. 
		else if (nextLine.indexOf("STOCK: ") == 0) { 
		    quoteID = 
			nextLine.substring("STOCK: ".length()); 
		    quoteResponse = getQuote(quoteID);
		    clientSend.println(quoteResponse);
		    clientSend.flush();
		} 
		// Unknown command. 
		else { 
		    clientSend.println("-ERR UNKNOWN COMMAND"); 
		    clientSend.flush();
		}
	    }
	    clientSend.println("+BYE"); 
	    clientSend.flush(); 
	}
	catch(IOException excpt) { 
	    System.err.println("Failed I/O: " + excpt); 
	} // Finally close the streams and socket. 
	finally { 
	    try { 
		if (clientSend != null) clientSend.close(); 
		if (clientReceive != null) clientReceive.close(); 
		if (mySocket != null) mySocket.close(); 
	    } catch(IOException excpt) { 
		System.err.println("Failed I/O: " + excpt); 
	    }
	}
    }

    /** 
     * This method matches a stock ID to relevant information. 
     * @param quoteID The stock ID to look up. 
     * @return The releveant data. 
     */ 
    protected String getQuote(String quoteID) { 
	for(int index = 0; index < stockInfo.length; index++) { 
	    // If there's a match, return the data. 
	    if(stockInfo[index].indexOf(quoteID) == 0) 
		return "+" + stockInfo[index]; 
	}
	// Otherwise, this is an unknown ID.
	return "-ERR UNKNOWN STOCK ID";
    }
}




