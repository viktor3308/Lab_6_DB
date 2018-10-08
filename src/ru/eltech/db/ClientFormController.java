package ru.eltech.db;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.util.Duration;

public class ClientFormController implements Initializable {

    @FXML
    private TextField tfdMatch;

    @FXML
    private TextField tfdResult;

    @FXML
    private Button btnRequest;

    @FXML
    private Button btnClear;
    
	private final static int PORT = 6666;
	private final static String ADDRESS = "127.0.0.1";
	private final static String DIALOG_TITLE = "Client";
	private final static String CONNECTION_SUCCESSFUL = "Connection to the server was successful";
	private final static String CONNECTION_FAILED = "Unable to connect to server";
	private final static String CONNECTION_CLOSED = "Connection to the server was closed";
	private final static String NULL_RESULT = "No matching values were found in database";
	private final static String REQUEST_TABLE = "conditioners";
	private final static String REQUEST_COLUMN = "description";
	private final static String MATCH_COLUMN = "name";
	
	private Socket m_socket = null;
	private ObjectOutputStream m_out = null;
	private ObjectInputStream m_in = null;
	
	private Timeline m_heartbeatTimeline = new Timeline(
		    new KeyFrame(Duration.seconds(1), handler -> {			    	
		    	try {
		    		Request heartBeatRequest = new Request(0);
		    		ClientFormController.this.sendRequest(heartBeatRequest);
				} catch (Exception e) {
					ClientFormController.this.m_heartbeatTimeline.stop();
					System.out.println(CONNECTION_CLOSED);
		    		final InformationDialog informationDialog =
		    				new InformationDialog(DIALOG_TITLE, CONNECTION_CLOSED);
		    		informationDialog.setOnHidden(evt -> {
		    				ClientFormController.this.closeConnection();
			    			Platform.exit();
		    			});
		    		informationDialog.show();
				}
		    })
		);

    private void initConnection() {
    	try {
    		m_socket = new Socket(InetAddress.getByName(ADDRESS), PORT);
    		m_out = new ObjectOutputStream(m_socket.getOutputStream());
    		System.out.println(CONNECTION_SUCCESSFUL);
    	}
    	catch (Exception x) {
    		System.out.println(CONNECTION_FAILED);
    		final InformationDialog informationDialog =
    				new InformationDialog(DIALOG_TITLE, CONNECTION_FAILED);
    		informationDialog.setOnHidden(evt -> {
				ClientFormController.this.closeConnection();
    			Platform.exit();
			});
    		informationDialog.showAndWait();
		}
    }
    
    public void closeConnection() {
			try {
				m_in.close();
			}
			catch (Exception e) {
			}
			try {
				m_out.close();
			}
			catch (Exception e) {
			}
			try {
				m_socket.close();
			}
			catch (Exception e) {
			}
    }
    
    private void sendRequest(Request request) throws IOException {
    	m_out.writeObject(request);
    	m_out.flush();
    }
    
    private void processRequest(Request request) {
    	try {
    		sendRequest(request);
    	}
    	catch (Exception e) {
		}
    	
    	Task<Response> waitForResponseTask = new Task<Response>() {
    	    @Override protected Response call() throws Exception {
    	    	if(m_in == null)
    				m_in = new ObjectInputStream(m_socket.getInputStream());
    	    	
    	    	return (Response) m_in.readObject();
    	    }
    	};
    	
    	waitForResponseTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
    	    @Override
    	    public void handle(WorkerStateEvent t) {
    	    	Response response = waitForResponseTask.getValue();
    	    	if(response != null) {
    				if (response.result != null) {
    					tfdResult.setText(response.result);
    				}
    				else {
    					final InformationDialog informationDialog =
    		    				new InformationDialog(
    		    						DIALOG_TITLE,
    		    						NULL_RESULT);
    		    		informationDialog.show();
    				}
    				btnClear.setDisable(false);
    			}
    	    }
    	});
    	
    	new Thread(waitForResponseTask).start();
    }
    
	private void clearValues() {
		tfdMatch.setText("");
		tfdResult.setText("");
    }

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		
		initConnection();
		
		m_heartbeatTimeline.setCycleCount(Timeline.INDEFINITE);
		m_heartbeatTimeline.play();
	}
	
	public void onRequest() {
		tfdMatch.setEditable(false);
		btnRequest.setDisable(true);
		btnClear.setDisable(true);
		processRequest(new Request(1, REQUEST_TABLE, MATCH_COLUMN, tfdMatch.getText(), REQUEST_COLUMN));
	}
	
	public void onClear() {
		clearValues();
		tfdMatch.setEditable(true);
		btnRequest.setDisable(false);
	}
}
