
import java.io.*;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

/**
 * serves files
 */
public class FileServer extends HttpServlet {

	private String docRoot;
	
    public void init(ServletConfig config) throws ServletException {
		super.init(config);
		docRoot =  getInitParameter("docroot");
	}
	
	
    public void doGet (HttpServletRequest req, HttpServletResponse res)
		throws ServletException, IOException {
		
		PrintWriter	out;

		res.setContentType("text/html");
		out = res.getWriter();

		String filename = req.getPathInfo(); // this is the file they want
		if (filename.endsWith("/"))
			filename = filename.concat("index.html"); // HACK
		
		try {
			int len;
			char[] buf;
			FileReader r = new FileReader(docRoot + filename);

			buf = new char[500];
			while (true) {
				len = r.read(buf, 0, 500);
				if (len == -1) break;
				out.print(new String(buf));
			}
			r.close();
		}
		catch (FileNotFoundException fnf) {
			out.print("File Not Found");
		}
		catch (Exception e) {
			throw new ServletException(e.getMessage());
		}
	}	
}
