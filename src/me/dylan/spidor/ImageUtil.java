package me.dylan.spidor;

import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import javax.imageio.ImageIO;

import net.sf.image4j.codec.bmp.BMPDecoder;
import net.sf.image4j.codec.bmp.BMPEncoder;
import net.sf.image4j.codec.ico.ICODecoder;
import net.sf.image4j.codec.ico.ICOEncoder;

public class ImageUtil {
	public static BufferedImage parseImage(String url) throws IOException {
		URL dest = new URL(url.replaceAll(".*Jumplist;icon-uri", ""));
		if (url.endsWith(".ico")) {
			return ICODecoder.read(dest.openStream()).get(0);
		} else if (url.endsWith(".bmp")) {
			return BMPDecoder.read(dest.openStream());

		} else {
			return ImageIO.read(dest);
		}
	}

	public static BufferedImage parseImageLocal(String url) throws IOException {
		if (url.endsWith(".ico")) {
			System.out.println("Loaded image: " + url.toString());
			return ICODecoder.read(new File(url)).get(0);
		} else if (url.endsWith(".bmp")) {
			return BMPDecoder.read(new File(url));

		} else {
			return ImageIO.read(new File(url));
		}
	}

	public static void saveImage(BufferedImage img, String path)
			throws IOException {

		path = path.replace("https://", "");
		path = path.replace("http://", "");
		File outputfile = new File(path);
		String parentPath = outputfile.getParent();
		String name = outputfile.getName();

		path = parentPath + File.separator + name;
		new File(parentPath).mkdir();
		if (!outputfile.exists()) {
			outputfile.createNewFile();
		}
		if (path.endsWith(".ico")) {
			ICOEncoder.write(img, outputfile);
		} else if (path.endsWith(".bmp")) {
			BMPEncoder.write(img, outputfile);
		} else {
			ImageIO.write(img, "png", outputfile);
		}
	}

	public static HashMap<Point, Color> createColorMap(BufferedImage img) {
		HashMap<Point, Color> colormap = new HashMap<Point, Color>();
		for (int x = 0; x < img.getWidth(); x++) {
			for (int y = 0; y < img.getHeight(); y++) {
				colormap.put(new Point(x, y), new Color(img.getRGB(x, y)));
			}
		}
		return colormap;
	}

	public static HashMap<BufferedImage, Point> assemblePieces(
			ArrayList<BufferedImage> pieces, BufferedImage puzzle) {
		HashMap<Point, Color> colorMap = createColorMap(puzzle);
		HashMap<BufferedImage, Point> iconMap = new HashMap<BufferedImage, Point>();
		iconMap.keySet().addAll(pieces);
		iconMap.values().addAll(iconMap.values());
		return iconMap;
	}

	public static ArrayList<BufferedImage> loadAllFromFolder(String path)
			 {
		File folder = new File(path);
		ArrayList<BufferedImage> images = new ArrayList<BufferedImage>();
		if (!folder.exists()) {
			return images;
		}
		for (File f : folder.listFiles()) {
			try {
				if (f.getCanonicalFile().toString().endsWith(".ico")
						&& f.isFile()
						&& !Main.icons
								.contains(parseImageLocal(f.getAbsolutePath()))) {
					images.add(parseImageLocal(f.getAbsolutePath()));
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return images;
	}

	public static Color getAvgImgColor(BufferedImage img) {
		long red = 0;
		long blue = 0;
		long green = 0;
		int totalValues = 0;
		for (int x = 0; x + 3 < img.getWidth(); x += 3) {
			for (int y = 0; y + 3 < img.getHeight(); y += 3) {
				Color tmp = new Color(img.getRGB(x, y));
				red += tmp.getRed();
				blue += tmp.getBlue();
				green += tmp.getGreen();
				totalValues++;
			}
		}
		int avgr = (int) (red / totalValues);
		int avgb = (int) (blue / totalValues);
		int avgg = (int) (green / totalValues);
		return new Color(avgr, avgb, avgg);
	}
}
