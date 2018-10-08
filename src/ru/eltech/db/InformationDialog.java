package ru.eltech.db;

import javafx.scene.control.Alert;

public class InformationDialog extends Alert {
	private final int WIDTH_DIALOG = 350;
	private final int HEIGHT_DIALOG = 100;

	public InformationDialog(String title, String content) {
		super(AlertType.INFORMATION);

		getDialogPane().setPrefSize(WIDTH_DIALOG, HEIGHT_DIALOG);
		setTitle(title);
		setContentText(content);
		setHeaderText(null);
	}
}
