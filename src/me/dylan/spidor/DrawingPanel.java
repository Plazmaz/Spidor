package me.dylan.spidor;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.swing.JPanel;

public class DrawingPanel extends JPanel {
	private static final long serialVersionUID = 6006533000914233220L;
	BufferedImage collageTarget;
	public DrawingPanel(ArrayList<Website> iconReferences, BufferedImage collageTarget) {
		this.collageTarget = collageTarget;
	}
	@Override
	public void paint(Graphics g) {
		for(BufferedImage b : Main.graphicalData.keySet()) {
			Point loc = Main.graphicalData.get(b);
			g.drawImage(b, loc.x, loc.y, null);
			System.out.println("X: "+loc.x+"Y: "+loc.y);
		}
		System.err.println("Repainted.");
	}
}
