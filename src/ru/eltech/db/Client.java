package ru.eltech.db;

import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Client extends Application {
	private final String CLIENT_TITLE = "Client";
	private final String EXIT_MESSAGE = "Client closed";
	private ClientFormController m_clientFormController;
	
	public static void main(String[] args) {
		launch(args);
	}
	
	private void initialize(Stage stage) throws IOException {
		FXMLLoader fxmlLoader = new FXMLLoader();
        Parent root = fxmlLoader.load(getClass().getResource("ClientForm.fxml").openStream());
        Scene scene = new Scene(root);
        scene.getStylesheets().add(Server.class.getResource("style.css").toExternalForm());
        
        m_clientFormController = (ClientFormController) fxmlLoader.getController();
         
        stage.setScene(scene);         
        stage.setTitle(CLIENT_TITLE);         
        stage.show();
	}

	@Override
	public void start(Stage stage) {
		try {
			initialize(stage);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void stop(){
		m_clientFormController.closeConnection();
		System.out.println(EXIT_MESSAGE);
	}
}
