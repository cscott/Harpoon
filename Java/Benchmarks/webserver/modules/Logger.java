

import java.io.*;

import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.java.io.LogWriter;
import org.apache.java.util.Configurations;
import org.apache.java.util.ExtendedProperties;

public class Logger extends HttpServlet
{

	private LogWriter log;

    public void init(ServletConfig config) throws ServletException {
		
		super.init(config);
		String confFile =  getInitParameter("configfile");
		Configurations confs = null;
		try {
			confs = new Configurations(new ExtendedProperties(confFile));
			log = new LogWriter("log", confs);
		} catch (IOException e) {
			throw new ServletException("could not open config file");
		}
		
		if (log.active) log.log("hits", "Logging module started");
	}
	
	
    public void doGet (HttpServletRequest request,
                       HttpServletResponse response) 
        throws ServletException, IOException
        {
			if (log.active)
				log.log("hits",
						"request for: " + request.getRequestURI() +
						"\tfrom: " + request.getRemoteAddr()
						);
        }
}
 
