// WebSpider.java created Tue Mar 21 22:58:11 EST 2000 by govereau
// Licensed under the terms of the GNU GPL; see COPYING for details.

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.FileWriter;

import java.net.URL;
import java.net.URLConnection;

/**
 * a simple web spider for testing single process compiler
 * some cases are not handled
 *
 * @author govereau@mit.edu
 * @version $Id: WebSpider.java,v 1.1 2000-03-22 09:10:54 govereau Exp $
 */
class WebSpider extends Thread {

	private String baseURL;
	private String startURL;
	private int level;

	public WebSpider(String url, int level) {
		try {
			this.level = level;
			startURL = url;
			if (url.endsWith("/")) {
				baseURL = url;
			} else {
				URL u = new URL(url);
				baseURL = u.getProtocol() + "://" + u.getHost() + "/";
			}
		}
		catch(Exception e) {
			e.printStackTrace(System.err);
		}		
	}

	public WebSpider(String url) {
		this(url, 1);
	}

	public void run() {
		try {
			URL url = new URL(startURL);
			URLConnection urlc = url.openConnection();
			String content = urlc.getContentType();
			InputStream stream = urlc.getInputStream();

			String filename = url.getFile().replace('/', '_');
			if (filename.compareTo("_") == 0)
				filename = new String("index.html");
			else
				filename = filename.substring(1);

			System.out.println("pocesssing " + filename);
			FileWriter fw = new FileWriter(filename);

				// if not html or at last level just same file
			if ((content.compareTo("text/html") != 0) || level == 2) {
				InputStreamReader isr = new InputStreamReader(stream);
				char b[] = new char[100];
				if (-1 == isr.read(b, 0, 100)) {
					fw.close();
					isr = null;
					stream = null;
					urlc = null;
					url = null;
					return;
				}
				fw.write(b, 0, 100);
			}

			int i, j;
			String href;
			BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
			String line = reader.readLine();
			while(line != null) {
				fw.write(line, 0, line.length());
					// find links (href) in document
				i = line.indexOf("href=");
				if (i != -1) {
					href = line.substring(i+5);
					if (href.startsWith("\"")) {
						j = href.indexOf("\"", 2);
						href = href.substring(1,j);
					} else {
						j = href.indexOf(" ", 2);
						href = href.substring(0,j);
					}
					if (!href.startsWith("mail")) {
						if (!href.startsWith("http")) {
							if (href.startsWith("/"))
								href = baseURL + href.substring(1);
							else
								href = baseURL + href;
						}
							//System.out.println("url>> " + href);
						WebSpider spider = new WebSpider(href, this.level + 1);
						spider.start();
					}
				}
				
					// find images in document
					// doesn't get all cases -- or even most of them?
				i = line.indexOf("img src="); 
				if (i != -1) {
					href = line.substring(i+8);
					if (href.startsWith("\"")) {
						j = href.indexOf("\"", 2);
						href = href.substring(1,j);
					} else {
						j = href.indexOf(" ", 2);
						href = href.substring(0,j);
					}
					if (!href.startsWith("http")) {
						if (href.startsWith("/"))
							href = baseURL + href.substring(1);
						else
							href = baseURL + href;
					}						
						//System.out.println("img>> " + href);
					WebSpider spider = new WebSpider(href, this.level + 1);
					spider.start();
				}
				line = reader.readLine();
			}
			fw.close();
			stream = null;
			urlc = null;
			url = null;			
		}
		catch (Exception e) {
			e.printStackTrace(System.err);
		}
	}

	
	public static void main(String args[]) {
		System.out.println("Spidering...");
		WebSpider ws = new WebSpider("http://web.mit.edu");
		ws.start();
	}
}
