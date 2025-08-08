package com.unified.client;

import javax.swing.*;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;
import java.awt.*;

public class UnifiedGUI {
	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			setModernLookAndFeel();
			ClientController controller = new ClientController();
			LoginFrame frame = new LoginFrame(controller);
			frame.setLocationRelativeTo(null);
			frame.setVisible(true);
		});
	}

	private static void setModernLookAndFeel() {
		try {
			// Try to use Nimbus for a modern look
			UIManager.setLookAndFeel(new NimbusLookAndFeel());
			
			// Set modern UI defaults
			UIManager.put("control", new Color(248, 248, 248));
			UIManager.put("nimbusBase", new Color(51, 98, 140));
			UIManager.put("nimbusBlueGrey", new Color(169, 176, 190));
			UIManager.put("nimbusFocus", new Color(115, 164, 209));
			UIManager.put("nimbusGreen", new Color(176, 179, 50));
			UIManager.put("nimbusInfoBlue", new Color(47, 92, 180));
			UIManager.put("nimbusLightBackground", new Color(255, 255, 255));
			UIManager.put("nimbusOrange", new Color(191, 98, 4));
			UIManager.put("nimbusRed", new Color(169, 46, 34));
			UIManager.put("nimbusSelectedText", new Color(255, 255, 255));
			UIManager.put("nimbusSelectionBackground", new Color(57, 105, 138));
			UIManager.put("text", new Color(0, 0, 0));
			
			// Modern fonts
			Font defaultFont = new Font("Segoe UI", Font.PLAIN, 12);
			UIManager.put("defaultFont", defaultFont);
			UIManager.put("Button.font", defaultFont);
			UIManager.put("Label.font", defaultFont);
			UIManager.put("TextField.font", defaultFont);
			UIManager.put("TextArea.font", defaultFont);
			UIManager.put("List.font", defaultFont);
			
		} catch (Exception e) {
			// Fallback to system look and feel
			try {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			} catch (Exception ignored) {
			}
		}
	}
}
