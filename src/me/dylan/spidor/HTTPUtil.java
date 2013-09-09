package me.dylan.spidor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.SocketAddress;
import java.net.Proxy.Type;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.net.InetSocketAddress;

public class HTTPUtil {
	static String charset = "UTF-8";

	public static String sendHTTPRequest(String url) {
		try {
		URL destination = new URL(url);
		URLConnection connection = destination.openConnection();
		connection.setRequestProperty("Accept-Charset", charset);
		connection.setRequestProperty("User-agent", "Spid0rBot - v0.1a | An artistic robot");
		// Launched August 22, 2013
		connection.setRequestProperty("Content-Type",
				"application/x-www-form-urlencoded;charset=" + charset);

		BufferedReader data = new BufferedReader(new InputStreamReader(
				connection.getInputStream()));
		String input = "";
		String headerData = "";
		String curLine;
		while ((curLine = data.readLine()) != null) {
			// System.out.println("Current Line: "+curLine);
			input += curLine;
		}
		for (String key : connection.getHeaderFields().keySet()) {
			input += connection.getHeaderField(key);
		}
		// for (Entry<String, List<String>> header :
		// connection.getHeaderFields().entrySet()) {
		// headerData+=("\n" + header.getKey() + header.getValue());
		// }
		// System.err.println(headerData);

		String formattedData = input.replaceAll("\\<br\\>", "\n").replaceAll(
				"<", "\n<");
		// System.out.println(formattedData);
		return input.replace("<br>", "\n");
		}catch(IOException e) {
			
		}
		return "";
	}

	public static ArrayList<String> getDisallowedFromRobots(String baseurl,
			ArrayList<String> robots) {
		ArrayList<String> disallowed = new ArrayList<String>();
		for (String s : robots) {
			if (s.startsWith("Disallow: ")) {
				disallowed.add(s.replace("Disallow: ", ""));
			}
		}
		return disallowed;
	}

	public static ArrayList<String> readRobots(String base) {
		String url = base + "/robots.txt";
		String robotscontent = sendHTTPRequest(url);
		ArrayList<String> robotdata = new ArrayList<String>();
		int index1 = robotscontent.indexOf("\"");
		int index2 = robotscontent.indexOf("\"", index1 + 1);
		String[] robotarray;
		if (index1 != -1 && index2 != -1) {
			robotarray = robotscontent.substring(index1, index2).split("\n");
		} else {
			robotarray = robotscontent.split("\n");

		}
		for (String s : robotarray) {
			robotdata.add(s);
		}
		return robotdata;
	}

	public static ArrayList<Webpage> extractLinks(String rawData,
			String patternstr) {
		ArrayList<Webpage> links = new ArrayList<Webpage>();
		Pattern pattern = Pattern.compile(patternstr);
		Matcher match = pattern.matcher(rawData);
		String lastURL = Main.seedURL1;
		while (match.find()) {
			String s = match.group(1).replace("\\", "");
			if (s.startsWith("/")) {
				s = lastURL + s;
			}
			if (s.startsWith("www")) {
				s = "http://" + s;
			}
			if (!s.equals("http://") && !s.equals("https://")
					&& !s.equals("www") && !s.equals("/")) {
				if (getAllowed(s)) {
					lastURL = s;
					links.add(new Webpage(s));
				}
			}

		}
		// String[] data = rawData.split("\n");
		/*
		 * for(int i = 0; i < data.length; i++) { String tmp = data[i];
		 * 
		 * 
		 * if(tmp.contains("<a href") && tmp.contains("</a>")) { String link =
		 * tmp.substring(tmp.indexOf("<a href=\""), tmp.indexOf("</a>")); link =
		 * link.replace("<a href=\"", ""); links.add(link); }
		 * if(tmp.contains("<link rel") && tmp.contains("/>")) { String link =
		 * tmp.substring(tmp.indexOf("<link rel=\""), tmp.indexOf("/>")); link =
		 * link.replace("<link rel=\"", "").replace(" ", "").replace("rel=\"",
		 * "").replace("\"", ""); links.add(link); } }
		 */
		return links;
	}

	public static boolean getAllowed(String url) {
		for (String s : Main.disallowed) {
			if (url.contains(s.replaceAll("/*/", ""))) {
				return false;
			}
		}
		return true;
	}
}
