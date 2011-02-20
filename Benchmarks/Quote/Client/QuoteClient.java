import java.io.*; // Import the names of the packages 
import java.net.*; // to be used. 
/** 
 * This is an application which obtains stock information 
 * using our new application protocol. 
 * @author David W. Baker 
 * @version 1.1 
 */ 
public class QuoteClient extends Thread { 
    // The Stock Quote server listens at this port. 
    private static int SERVER_PORT = 1701; 
    private String serverName; 
    private Socket quoteSocket = null; 
    private BufferedReader quoteReceive = null; 
    private PrintStream quoteSend = null; 
    private String[] stockIDs;  // Array of requested IDs. 
    private String[] stockInfo; // Array of returned data. 
    private String currentAsOf = null; // Timestamp of data. 
    private static boolean debug=false;

    /** 
     * Start the application running, first checking the 
     * arguments, then instantiating a StockQuoteClient, and 
     * finally telling the instance to print out its data. 
     * @param args Arguments which should be <server> <stock ids> 
     */ 
    public static void main(String[] args) { 
	if (args.length < 2) { 
	    System.out.println("Usage: QuoteClient <server> <port> <number of clients> <number of cycles> <debug> <stock ids>"); 
	    System.exit(1); 
	}
	
	long starttime = System.currentTimeMillis();
	QuoteClient.SERVER_PORT = Integer.parseInt(args[1]);
	int numclients = Integer.parseInt(args[2]);
	int numcycles = Integer.parseInt(args[3]);
	QuoteClient.debug = (Integer.parseInt(args[4]) == 1);
	int numstocks = args.length - 5;
	QuoteClient[] carray = new QuoteClient[numclients];
	for (int i = 0; i < numclients; i++) {
	    carray[i] = new QuoteClient(args);
	    carray[i].start();
	}
	try {
	    for (int i = 0; i < numclients; i++) {
		carray[i].join();
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	    System.out.println(e);
	}
	long endtime = System.currentTimeMillis();

	System.out.println("QuoteClient");
	System.out.println("numclients:" + numclients);
	System.out.println("port:" + QuoteClient.SERVER_PORT);
	System.out.println("number of cycles:" + numcycles);
	System.out.println("number of stocks:" + numstocks);
	System.out.println("Elapsed time:(mS)" + (endtime-starttime));
	System.out.println("Throughput:" + (double) numcycles * numstocks *
			   numclients/((double) (endtime-starttime)));
	System.exit(0);
    } 

    String args[];

    public QuoteClient(String[] args) { 
	this.args=args;
    }

    public void run() {
       	String serverInfo; 
	// Server name is the first argument. 
	serverName = args[0]; 
	// Create arrays as long as arguments - 1. 
	stockIDs = new String[args.length-5]; 
	stockInfo = new String[args.length-5]; 
	// Copy in the arguments into are stockIDs[] array. 
	for (int index = 5; index < args.length; index++) { 
	    stockIDs[index-5] = args[index]; 
	} 
	// Contact the server and return the HELLO message. 
	serverInfo = contactServer(); 
	// Parse our the timestamp. 
	if (serverInfo != null) { 
	    currentAsOf = serverInfo.substring( 
					       serverInfo.indexOf(" ")+1); 
	}
 
        for(int i=0;i<Integer.parseInt(args[3]);i++) {
	    getQuotes(); // Go get the quotes. 
	    if (QuoteClient.debug)
		this.printQuotes(System.out); 
	}
	quitServer(); // Close the communication.
    }

    /** 
     * Open the initial connection to the server. 
     * @return The initial connection response. 
     */ 
    protected String contactServer() { 
	String serverWelcome = null; 
	try { 
	    // Open a socket to the server. 
	    quoteSocket = new Socket(serverName,SERVER_PORT); 
	    // Obtain decorated I/O streams. 

	    quoteReceive = 
		new BufferedReader
		    (new InputStreamReader(quoteSocket.getInputStream()));

	    quoteSend = new PrintStream(quoteSocket.getOutputStream()); 

	    // Read the HELLO message. 
	    serverWelcome = quoteReceive.readLine(); 
	} catch (UnknownHostException excpt) { 
	    System.err.println("Unknown host " + serverName + 
			       ": " + excpt); 
	} catch (IOException excpt) { 
	    System.err.println("Failed I/O to " + serverName + 
			       ": " + excpt); 
	} 
	return serverWelcome; // Return the HELLO message. 
    } 

    /** 
     * This method asks for all of the stock info. 
     */ 
    protected void getQuotes() { 
	String response; // Hold the response to stock query. 
	// If the connection is still up. 
	if (connectOK()) { 
	    try { 
		// Iterate through all of the stocks. 
		for (int index = 0; index < stockIDs.length; 
		     index++) { 
		    // Send query. 
		    quoteSend.println("STOCK: "+stockIDs[index]); 
		    // Read response. 
		    response = quoteReceive.readLine(); 
		    // Parse out data. 
		    stockInfo[index] =
			response.substring(response.indexOf(" ") + 1); 
		}
	    } catch (IOException excpt) { 
		System.err.println("Failed I/O to " + serverName 
				   + ": " + excpt); 
	    }
	}
    }

    /** 
     * This method disconnects from the server. 
     * @return The final message from the server. 
     */ 
    protected String quitServer() { 
	String serverBye = null; // BYE message. 
	try { 
	    // If the connection is up, send a QUIT message 
	    // and receive the BYE response. 
	    if (connectOK()) { 
		quoteSend.println("QUIT"); 
		serverBye = quoteReceive.readLine(); 
	    } 
	    // Close the streams and the socket if the 
	    // references are not null. 
	    if (quoteSend != null) quoteSend.close(); 
	    if (quoteReceive != null) quoteReceive.close(); 
	    if (quoteSocket != null) quoteSocket.close(); 
	} catch (IOException excpt) { 
	    System.err.println("Failed I/O to server " + 
			       serverName + ": " + excpt); 
	} 
	return serverBye; // The BYE message. 
    } 

    /** 
     * This method prints out a report on the various 
     * requested stocks. 
     * @param sendOutput Where to send output. 
     */ 
    public void printQuotes(PrintStream sendOutput) { 
	// Provided that we actually received a HELLO message: 
	if (currentAsOf != null) { 
	    sendOutput.print("INFORMATION ON REQUESTED QUOTES" 
			     + "\n\tCurrent As Of: " + currentAsOf + "\n\n"); 
	    // Iterate through the array of stocks. 
	    for (int index = 0; index < stockIDs.length; 
		 index++) { 
		sendOutput.print(stockIDs[index] + ":"); 
		if (stockInfo[index] != null) 
		    sendOutput.println(" " + stockInfo[index]); 
		else sendOutput.println(); 
	    } 
	} 
    } 
    
    /** 
     * Conveniently determine if the socket and streams are 
     * not null. 
     * @return If the connection is OK. 
     */ 
    protected boolean connectOK() { 
	return (quoteSend != null && quoteReceive != null && 
		quoteSocket != null); 
    } 
}
