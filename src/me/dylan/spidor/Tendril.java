package me.dylan.spidor;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

public class Tendril {
	ArrayList<Webpage> sitesToVisit = new ArrayList<Webpage>();
	String seedURL = "";
	private static Tendril instance;
	public volatile Thread tendrilTh;
	ArrayList<Website> myroots = new ArrayList<Website>();
	public int uniqueDomains = 0;
	public int pagesVisited = 0;
	int index=0;
	long lastUpdate = System.currentTimeMillis();
	public ArrayList<HelperThread> helpers = new ArrayList<HelperThread>();
	public Tendril(String seed) {
		seedURL = seed;
		sitesToVisit.add(new Webpage(seedURL));
		instance = this;
	}

	public void runGrowingThread(final File file) {
		tendrilTh = new Thread(new Runnable() {
			public void run() {
				while (true) {
					lastUpdate = System.currentTimeMillis();
					try {
//						if (sitesToVisit.size() >= 30) {
//							helpers.add(new HelperThread(instance, Main.sitesVisited, sitesToVisit, file));
//							System.err.println("Spun new helper thread.");
//						}
						for (int i = 0; i < sitesToVisit.size(); i++) {
							String s = sitesToVisit.get(i).suburl;
							index=i;
							String root = "http://"+sitesToVisit.get(i).rooturl;
							if (!getIfVisited(new Webpage(root+"/robots.txt"), Main.sitesVisited)) {
									Main.disallowed.addAll(HTTPUtil
										.getDisallowedFromRobots(root,
												HTTPUtil.readRobots(root)));
//								sitesToVisit.add(new Webpage(root+"/sitemap.xml"));
								
							}
							if(Main.allRoots.keySet().contains(root)){
								if(!Main.allRoots.get(root).isTimeoutOver(System.currentTimeMillis())) {
									//Reset the queue.
									Webpage s1 = sitesToVisit.get(i);
									sitesToVisit.remove(i);
									sitesToVisit.add(s1);
									continue;
								}
							}
							sitesToVisit.remove(i);
							if (!getIfVisited(new Webpage(s), Main.sitesVisited) && !getIfVisited(new Webpage(s), sitesToVisit)&& s != null
									&& !s.equals("http://")
									&& !s.equals("https://") && HTTPUtil.getAllowed(s)) {
								
								System.out.println(s);
								pagesVisited++;
								String rawData = HTTPUtil.sendHTTPRequest(s);
								// sitesToVisit.addAll(HTTPUtil.extractLinks(rawData,
								// "href=\"(.*?)\""));
								sitesToVisit.addAll(HTTPUtil
										.extractLinks(rawData,
												"((https?):((//)|(\\\\))+[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*)"));
								Main.sitesVisited.add(new Webpage(s));
								if (s.contains("favicon.")) {
								
									System.err
											.println("==========DOWNLOADED IMAGE==========");
									System.err.println(s);
									System.err
									.println("==========DOWNLOADED IMAGE==========");
									FileUtil.writeToFile(file, s);
									BufferedImage favicon = ImageUtil
											.parseImage(s);
									Main.allRoots.put(root, new Website(root, favicon));
									if(!Main.allRoots.keySet().contains(root)) {
										uniqueDomains++;
									}
									Main.icons.add(favicon);
									ImageUtil.saveImage(favicon, "favicons/"
											+ root.replace("http://", "") + ".ico");
									
								} else {
									FileUtil.writeToFile(file, s);
								}

							}
						}

					} catch (IOException e1) {
						e1.printStackTrace();
					}

					try {
						Thread.sleep(50);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		});
		tendrilTh.start();
	}
	public static boolean getIfVisited(Webpage url, ArrayList<Webpage> visited) {
		for(int i = 0; i < visited.size(); i++) {
			Webpage page = visited.get(i);
			if(page.suburl.equals(url.suburl)) {
				return true;
			}
		}
		return false;
	}
}
