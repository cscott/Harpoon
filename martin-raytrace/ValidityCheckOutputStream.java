/*
 * %W% %G%
 *
 * Copyright (c) 1998 Standard Performance Evaluation Corporation (SPEC)
 *               All rights reserved.
 * Copyright (c) 1997,1998 Sun Microsystems, Inc. All rights reserved.
 *
 * This source code is provided as is, without any express or implied warranty.
 */

/**
 * This class implements a special from of OutputStream that is used by
 * to validity check the output from the benchmarks.
 * 
 * Data is written here when spec.harness.Context.out.println() is called
 * which will cause the PrintStream class to call is here. Regular
 * PrintStream data arrives here along with valitity chech codes that show
 * how the data should be compaired. These codes are:
 * <PRE>
 * 0 - Validity checking is not performed at all.
 * 1 - Validity check all the data
 * 2 - Remove '/' and '\' characters before validity checking.
 * 3 - Remove numbers before validity checking.
 * 4 - Rules 2 and 3 together.
 * </PRE>
 *
 * These validity chech codes are injected into the regular data as the characters
 * \u0000 to \u0008. A check is made that these characters along with any other strange
 * data is not printed accidenially
 *
 * @see PrintStream
 */ 
public
class ValidityCheckOutputStream extends ConsoleOutputStream {

    final static char NONE               = '0';
    final static char ALL                = '1';    
    final static char NOSLASHES          = '2';
    final static char NONUMBERS          = '3';
    final static char NOSLASHESORNUMBERS = '4';

    /**
     * Flag show we are about to start a line
     */    
    boolean startOfLine = true;	
    
    /**
     * Current validity check value 
     */
    char value = '0';

    /**
     * Name of benchmark
     */
    String benchName;
					        
    /**
     * Name of validity check file
     */
    String fileName;

    /** 
     * Output stream used in collect the data.
     */
    java.io.ByteArrayOutputStream ostream = new java.io.ByteArrayOutputStream(4096);	
    
    /**
     * Number of validation errors found
     */
    int errors = 0;

    /**
     * Creates a new PrintStream.
     * @param benchName Name of benchmark
     * @param fileName Name of validity check file
     * @exception IOException If an I/O error has occurred.     
     */
    public ValidityCheckOutputStream(String benchName, String fileName) throws java.io.IOException {
	this(benchName, fileName, '0');
    }
    
    /**
     * Creates a new PrintStream.
     * @param benchName Name of benchmark
     * @param fileName Name of validity check file
     * @param defValue the default validity check value.
     * @exception IOException If an I/O error has occurred.     
     */
    public ValidityCheckOutputStream(String benchName, String fileName, char defValue) throws java.io.IOException {
	this.benchName = benchName;
	this.fileName  = fileName;	
	setValidityCheckValue(defValue);
    }    
 
   /**
     * Change the validity checking value. 
     * @param v the value (between 0 and 8)
     * @return the value we replaced
     */
    public char setValidityCheckValue(char v) {
	char res = value;
        value = v;
	return res;
    }
     
    /**
     * Writes a byte. 
     * @param b the byte
     * @exception IOException If an I/O error has occurred.
     */
    public void write(int b) throws java.io.IOException {
	/*
	 * Just let old fashoned ASCII through
	 */
	if ((b >= 0x20 && b <= 0x7f) || b == '\t' || b == '\n') {		    	
	    if (startOfLine) {
		ostream.write(value);
		startOfLine = false;
	    }	
			
	    super.write(b);			
	    ostream.write(b);    
	        
	    if (b == '\n') {
		startOfLine = true;
	    }
	}
    }
    
     /**
     * Create the valitity check file. This is only used in the devlopment cycle.
     */    
    public void createValidityFile() {	
	try {
	    String fullName = Context.getBasePath()+fileName;
	    Context.out.println("Opening "+fullName);
	    java.io.FileOutputStream out = new java.io.FileOutputStream(fullName);  
	    out.write(ostream.toByteArray());
	    out.close();
	} catch(java.io.IOException x) {
	    Context.out.println("Error creating validity check file: "+x);    
	}       
    }
	    
		
		        
    /**
     * Validity check the output and write any errors into the results property file
     * @param results the property file to contain the errors
     * @param run the run iteration number
     */
    public boolean validityCheck(java.util.Properties results, int run) {	
	try {
	    validityCheck2(results, run);
	} catch(java.io.IOException x) {
	    Context.out.println("Error in validityCheck: "+x.getMessage());	   
	    errors++;
	    results.put("spec.results."+benchName+".run"+run+".validity.error"+nerrors(), 
			"Exception:"+x);
	}	
	results.put("spec.results."+benchName+".run"+run+".valid", ((errors == 0) ? "true" : "false"));
	if (errors != 0) {
	    Context.out.println(">>>>>>>>>>>>> RUN WAS NOT VALID <<<<<<<<<<<");
	    return false;
	} else {
	    return true;
	}
    }
    
    
    /**
     * Return the number of errors as a two digit string with a leading zero if
     * necessary
     */     
    private String nerrors() {
	return ((errors < 10) ? "0" : "") + errors;
    }    


    /**
     * Read the first character of a line. If this is a '0' then skip
     * the line and read the next one. Return -1 if at EOF    
     */
    private int getLineStartChar(java.io.InputStream in) throws java.io.IOException {    
	int ch = in.read();
	while (ch == '0') {
	    while (ch != '\n') {
	        ch = in.read();
	        if (ch == -1) {
	    	    return -1;
		}
	    }
	    ch = in.read();
	}   
	return ch;
    }


     /**
     * Validity check the output and write any errors into the results property file
     * @param results the property file to contain the errors
     * @param run the run iteration number     
     */
    private void validityCheck2(java.util.Properties results, int run) throws java.io.IOException {	   

        java.io.LineNumberInputStream in1 = new java.io.LineNumberInputStream(
						new FileInputStream(fileName)
					        );
					    
	java.io.LineNumberInputStream in2 = new java.io.LineNumberInputStream(
						new java.io.ByteArrayInputStream(ostream.toByteArray())
						);
	while (errors <= 50) {
	    int incode1 = getLineStartChar(in1);	
	    int incode2 = getLineStartChar(in2);	  
	    
	    if (incode1 != incode2) {
	        errors++;	    
	        results.put("spec.results."+benchName+".run"+run+".validity.error"+nerrors(), 
			    "line "+in2.getLineNumber()+" has code: "+incode2);
	    } else {    	    	    
		if (incode1 == -1 && incode2 == -1) {
		    return;
		}	  
		  
		String line1 = strip(incode1, in1);
		String line2 = strip(incode2, in2);				    
		if (!line1.equals(line2)) {
	            errors++;	    
		    results.put("spec.results."+benchName+".run"+run+".validity.error"+nerrors(), 
			        "line "+in2.getLineNumber()+" is: "+line2);
		}
	    }
	}
    }
   
   /**
    * Strip the unwanted data from the line
    */
    private String strip(int incode, java.io.InputStream in) throws java.io.IOException {   
        StringBuffer sb = new StringBuffer(128);
	switch (incode) {
            case '1': strip1(sb, in); break;
            case '2': strip2(sb, in); break;	    	
            case '3': strip3(sb, in); break;
            case '4': strip4(sb, in); break;
	    case -1: 
		sb.append("**EOF**");
		break;
   	    default: 
		throw new java.io.IOException("Error in strip incode = "+incode);
	}
	return sb.toString();
    }              
    
    /**
     * Option 1 remove nothing
	 * @param sb - Buffer to write the stripped data
	 * @param in - InputStream which needs to be processed
     */
    private void strip1(StringBuffer sb, java.io.InputStream in) throws java.io.IOException {
	int ch;
        while ((ch = in.read()) != '\n' && ch != -1) {
	    sb.append((char)ch);   
	} 	 
    }    

    /**
     * Option 2 remove '/' and '\' characters
	 * @param sb - Buffer to write the stripped data
	 * @param in - InputStream which needs to be processed
     */
    private void strip2(StringBuffer sb, java.io.InputStream in) throws java.io.IOException {
	int ch;
	while ((ch = in.read()) != '\n' && ch != -1) {
	    if (ch != '/' && ch != '\\') {
		sb.append((char)ch);   
	    }
	} 	 
    }          

    /**
     * Option 3 remove numbers
	 * @param sb - Buffer to write the stripped data
	 * @param in - InputStream which needs to be processed
     */   
    private void strip3(StringBuffer sb, java.io.InputStream in) throws java.io.IOException {
	int ch;
        while ((ch = in.read()) != '\n' && ch != -1) {
	    if (ch < '0' || ch > '9') {
		 sb.append((char)ch);   
	    }
	} 	 
    }         

    /**
     * Option 4 remove numbers, '/' and '\' characters
	 * @param sb - Buffer to write the stripped data
	 * @param in - InputStream which needs to be processed
     */   
    private void strip4(StringBuffer sb, java.io.InputStream in) throws java.io.IOException {
	int ch;
	while ((ch = in.read()) != '\n' && ch != -1) {
	    if ((ch < '0' || ch > '9') && ch != '/' && ch != '\\') {
		sb.append((char)ch);   
	    }
	} 	 
    }    
}
