package ru.eltech.db;

import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Server extends Application {
	private final String SERVER_TITLE = "Server";
	private ServerTask m_serverTask;

	public static void main(String[] args) {	
		launch(args);
	}
	
	private void initialize(Stage stage) throws IOException {
		FXMLLoader fxmlLoader = new FXMLLoader();
        Parent root = fxmlLoader.load(getClass().getResource("ServerForm.fxml").openStream());
        Scene scene = new Scene(root);
        scene.getStylesheets().add(Server.class.getResource("style.css").toExternalForm());
         
        stage.setScene(scene);         
        stage.setTitle(SERVER_TITLE);         
        stage.show();
        
        ServerFormController serverFormController = (ServerFormController) fxmlLoader.getController();
        m_serverTask = new ServerTask(serverFormController.getStatement());        
        
        serverFormController.statusProperty().bind(m_serverTask.messageProperty());
        new Thread(m_serverTask).start(); 
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
	    if(m_serverTask.isRunning()) {
	    	m_serverTask.cancel(true);
	    }
	}
}
