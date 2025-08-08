package com.unified.client;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class UnifiedGUI {
	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			setSystemLookAndFeel();
			ClientController controller = new ClientController();
			LoginFrame frame = new LoginFrame(controller);
			frame.setLocationRelativeTo(null);
			frame.setVisible(true);
		});
	}

	private static void setSystemLookAndFeel() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception ignored) {
		}
	}
}
