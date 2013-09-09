package me.dylan.spidor;

public class Webpage extends Website {
	String suburl;
	int uid = ++Main.uid;
	public Webpage(String url) {
		super(url.split("/")[2], null);
		suburl = url;
	}

}
