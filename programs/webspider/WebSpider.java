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
 * @version $Id: WebSpider.java,v 1.2 2000-04-03 00:15:14 govereau Exp $
 */
class WebSpider extends Thread {

	public final int MAX_LEVEL = 1;

	private String baseURL;
	private String startURL;
	private int level;
	private boolean save;
	private String keyword;
	private int nKeyword = 0;

	public WebSpider(String url, int level, boolean save, String keyword) {
		try {
			this.save = save;
			this.keyword = keyword;
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

	public WebSpider(String url, boolean save) {
		this(url, 1, save, null);
	}

	public WebSpider(String url) {
		this(url, 1, false, null);
	}

	public void run() {
		FileWriter fw = null;
		try {
			URL url = new URL(startURL);
			URLConnection urlc = url.openConnection();
			String content = urlc.getContentType();
			InputStream stream = urlc.getInputStream();

			System.out.println("pocesssing " + url.toString());

			if (save) {
				String filename = url.getFile().replace('/', '_');
				if (filename.compareTo("_") == 0)
					filename = new String("index.html");
				else
					filename = filename.substring(1);
				fw = new FileWriter(filename);
			}

				// if not html or at last level just same file
			if (save && ((content.compareTo("text/html") != 0) || level == MAX_LEVEL)) {
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
				if (keyword != null)
					if (-1 != line.indexOf(keyword)) nKeyword++;
					
				if (save)
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
						WebSpider spider = new WebSpider(href, this.level + 1, save, keyword);
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
					WebSpider spider = new WebSpider(href, this.level + 1, save, keyword);
					spider.start();
				}
				line = reader.readLine();
			}
			if (keyword != null)
				System.out.println("Found " + nKeyword + " occurances of " + keyword);
			if (save)
				fw.close();
			stream = null;
			urlc = null;
			url = null;
			
		}
		catch (Exception e) {
			e.printStackTrace(System.err);
		}
	}

	private static void usage() {
		System.out.println("Usage:\n WebSpider <url> [mode [keyword]]\n" +
						   "\tmode:\n" +
						   "\t 1 = just walk through files.\n" +
						   "\t 2 = save files to disk.\n" +
						   "\t 3 = search for keyword in files.\n"
						   );
		System.exit(1);
	}
	
	public static void main(String args[]) {
		String url = null;
		int mode = 1;
		boolean save = false;
		String keyword = null;
		switch(args.length) {
			case 1:
				url = args[0];
				mode = 1;
				break;

			case 2:
				url = args[0];
				mode = Integer.parseInt(args[1]);
				break;

			case 3:
				url = args[0];
				mode = Integer.parseInt(args[1]);
				keyword = args[2];
				break;
				
			default:
				usage();
				break;
		}
		if (mode == 2) save = true;
		if (mode == 3 && keyword == null) usage();
		
		System.out.print("Spidering " + url);
		if (!save) System.out.print(" not"); System.out.print(" saving files");
		if (keyword != null) System.out.print(" searching for " + keyword);
		System.out.println(".");
		WebSpider ws = new WebSpider(url, save);
		ws.start();
	}
}
