package me.dylan.spidor;

import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

public class Main {
	public static String seedURL1 = "http://www.google.com/adplanner/static/top1000/#";
	public String seedURL2 = "http://en.wikipedia.org/";
	public String seedURL3 = "http://www.msn.com";
	static ArrayList<String> disallowed = new ArrayList<String>();
	public static int uid = 0;
	public static ArrayList<Tendril> tendrils = new ArrayList<Tendril>();
	public static ArrayList<Webpage> sitesVisited = new ArrayList<Webpage>();
	public static HashMap<String, Website> allRoots = new HashMap<String, Website>();
	public static HashMap<BufferedImage, Point> graphicalData = new HashMap<BufferedImage, Point>();
	ArrayList<Webpage> lines;
	JFrame frame;
	BufferedImage img = null;
	DrawingPanel panel;
	static ArrayList<BufferedImage> icons = new ArrayList<BufferedImage>();
	public Main() {
			icons.addAll(ImageUtil.loadAllFromFolder("favicons"));
		frame = new JFrame("SpidOr - The artistic web spider");
		frame.setSize(Toolkit.getDefaultToolkit().getScreenSize());
		try {
			img = ImageIO.read(new File("TestSpidor.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		panel = new DrawingPanel(new ArrayList<Website>(allRoots.values()), img);
		frame.add(panel);
		// disallowed.add("facebook.com");
		frame.setVisible(true);
		 disallowed.add("opengraphprotocol.org/schema");
		int it = 0;
		File file = null;
		try {
			File tmp;
			runUpdateThread();
			if ((tmp = new File("Visited Websites.txt")).exists()) {
				lines = FileUtil.getAllURLS(tmp);
				file = FileUtil.hookIntoFile("Visited Websites.txt");
				int filesize = lines.size() - 1;
				for (int i = 0; i + (int) (filesize / 300) < filesize - 1; i += (int) (filesize / 300)) {
					Tendril tendril = new Tendril(lines.get(i).suburl);
					lines.remove(i);
					tendril.runGrowingThread(file);
					tendrils.add(tendril);
				}
				sitesVisited.addAll(lines);
			} else {
				file = FileUtil.hookIntoFile("Visited Websites.txt");
				FileUtil.writeToFile(file, "Initialize Data Block.");
				Tendril tendril1 = new Tendril(seedURL1);
				Tendril tendril2 = new Tendril(seedURL2);
				Tendril tendril3 = new Tendril(seedURL3);
				tendril1.runGrowingThread(file);
				tendril2.runGrowingThread(file);
				tendril3.runGrowingThread(file);
				tendrils.add(tendril1);
				tendrils.add(tendril2);
				tendrils.add(tendril3);
			}

		} catch (IOException e2) {
			e2.printStackTrace();
		}

	}

	public static void main(String[] args) {
		new Main();

	}

	public void runUpdateThread() {
		Thread th = new Thread(new Runnable() {
			public void run() {
				while (true) {

					if (sitesVisited.size() > 100) {
						sitesVisited.remove(sitesVisited.size()-1);
					}
					for (int i = 0; i < tendrils.size(); i++) {
						Tendril tendril = tendrils.get(i);
						if (tendril != null && tendril.pagesVisited>0) {
							if (tendril.pagesVisited > tendril.uniqueDomains * 50) {
								sliceTendril(tendril);
								tendrils.add(new Tendril(lines.get(new Random()
										.nextInt(lines.size() - 1)).suburl));
							}
							for (Website site : tendril.myroots) {
								if (site.timeLastVisited - tendril.lastUpdate > 1500) {
									sliceTendril(tendril);
								}
							}
						}

					}
					
					for(Website w : allRoots.values()) {
						if(!icons.contains(w.icon))
							icons.add(w.icon);
					}
//					try {
//						icons.addAll(ImageUtil.loadAllFromFolder("favicons"));
//					} catch (IOException e1) {
//						e1.printStackTrace();
//					}
					
					
					graphicalData = ImageUtil.assemblePieces(icons, img);
					panel.repaint();
					
					try {
						Thread.sleep(50);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		});
		th.start();
	}

	public void sliceTendril(Tendril tendril) {
		tendrils.remove(tendrils.indexOf(tendril));
		tendril.tendrilTh = null;
		for (HelperThread thread : tendril.helpers) {
			thread.thread = null;
		}
		System.err.println("SLICED TENDRIL");

	}
}
