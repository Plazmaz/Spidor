package me.dylan.spidor;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class HelperThread {
	Thread thread;
	Tendril parent;
	public HelperThread(final Tendril parent, final ArrayList<Webpage> visited, final ArrayList<Webpage> unvisited, final File file) {
		this.parent = parent;
		thread = new Thread(new Runnable() {
			public void run() {
				for(int i=unvisited.size()-1; i > parent.index+1;i--) {
						try {
							updateHelper(file, unvisited, visited, i);
						} catch (IOException e) {
//							e.printStackTrace();
							System.out.println("No robots found.");
						}
					}


					try {
						Thread.sleep(50);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
		});
		thread.start();
	}

	private void updateHelper(final File file, ArrayList<Webpage> sitesToVisit,
			ArrayList<Webpage> sitesVisited, int index) throws IOException {
			Webpage w = sitesToVisit.get(index);
			sitesToVisit.remove(index);
			String root = "http://"+w.rooturl;
			 if(!Main.allRoots.keySet().contains(root)) {
				if(!Main.allRoots.get(root).isTimeoutOver(System.currentTimeMillis())) {
					//Reset the queue.
					Webpage s1 = sitesToVisit.get(index);
					sitesToVisit.remove(index);
					sitesToVisit.add(s1);
					return;
				}
			}
			if (!sitesVisited.contains(root + "/favicon.ico")) {
				sitesToVisit.add(new Webpage(root + "/favicon.ico"));
			}
			if (!sitesVisited.contains(root + "/robots.txt")) {
				Main.disallowed.addAll(HTTPUtil
						.getDisallowedFromRobots(root,
								HTTPUtil.readRobots(root)));
				sitesToVisit.add(new Webpage(root+"/robots.txt"));
				
			}
			if (!sitesVisited.contains(w) && w != null && !w.equals("http://")
					&& !w.equals("https://")) {
				System.out.println(w.suburl);
				String rawData = HTTPUtil.sendHTTPRequest(w.suburl);
				// sitesToVisit.addAll(HTTPUtil.extractLinks(rawData,
				// "href=\"(.*?)\""));
				sitesToVisit
						.addAll(HTTPUtil
								.extractLinks(rawData,
										"((https?):((//)|(\\\\))+[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*)"));
				sitesVisited.add(w);
				if (w.suburl.contains("favicon.")) {
					FileUtil.writeToFile(file,
							"==========DOWNLOADED IMAGE==========");
					System.err.println("==========DOWNLOADED IMAGE==========");
					FileUtil.writeToFile(file, w.suburl);
					BufferedImage favicon = ImageUtil.parseImage(w.suburl);
					ImageUtil.saveImage(favicon, "favicons/" + w.rooturl
							+ ".ico");
					if(!Main.allRoots.keySet().contains(root)) {
						Main.allRoots.put(root, new Website(root, favicon));
						parent.uniqueDomains++;
					}
					FileUtil.writeToFile(file,
							"==========DOWNLOADED IMAGE==========");
				} else {
					FileUtil.writeToFile(file, w.suburl);
				}
				

		}
	}
}
