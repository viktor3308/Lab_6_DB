package ru.eltech.db;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javafx.application.Platform;
import javafx.concurrent.Task;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ServerTask extends Task<Void> {
	
	private final static String DIALOG_TITLE = "Server";
	private final static String DIALOG_DISCONNECTION_MESSAGE = "The client has disconected. Server will shut down now.";
	private final static String SERVER_WAIT = "Server is waiting for connection...";
	private final static String CLIENT_CONNECT = "Client connected to the server";
	private final static String CLOSE_CONNECT = "Connection with the client is closed";
	private final static String CLOSE_TASK = "Server stopped";
	private final static String ERROR = "Something went wrong";
	private final static int PORT = 6666;
	
	private Request m_request;
	private Socket m_socket = null;
	private final Lock m_socketLock = new ReentrantLock(true);
	private Statement m_statement = null;
	
	public ServerTask(Statement statement) {
		super();
		m_statement = statement;
	}
	
	private String getResult() {
		String result = null;
		
		final String sql =
				"select " +
				m_request.requestColumn +
				" from " +
				m_request.table +
				" where " +
				m_request.matchColumn +
				"=\"" +
				m_request.matchValue +
				"\"";
		try (ResultSet resultSet = m_statement.executeQuery(sql)) {
			if (resultSet.next()) {
				result = resultSet.getString(m_request.requestColumn);
			}
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
		
		return result;
	}

	@Override
	protected Void call() throws Exception {
		try (ServerSocket ss = new ServerSocket(PORT);) {			
			System.out.println(SERVER_WAIT);
			updateMessage(SERVER_WAIT);
			ss.setSoTimeout(1000);
			
			while(!isCancelled()) {
				try (Socket socket = ss.accept();
					ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
					ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {
					
					m_socketLock.lock();
					m_socket = socket;
					m_socketLock.unlock();
					
					System.out.println(CLIENT_CONNECT);
					updateMessage(CLIENT_CONNECT);
					
					while (!isCancelled()) {
						m_request = (Request) in.readObject();
						if (m_request.id != 0) {
							String result = getResult();
							Response response = new Response();
							response.id = m_request.id;
							response.result = result;
							
							//Some processing time simulation 
							Thread.sleep(3000);
							
							out.writeObject(response);						
							out.flush();
						}
					}
				}
				catch (java.net.SocketTimeoutException e) {
				}
			}			
		}
		catch (java.net.SocketException e) {
			if (!isCancelled()) {
				System.out.println(CLOSE_CONNECT);
				updateMessage(CLOSE_CONNECT);
			}
		}
		catch (EOFException e) {
			System.out.println(CLOSE_CONNECT);
			updateMessage(CLOSE_CONNECT);
		}
		catch (Exception e) {
			System.out.println(ERROR);
			updateMessage(ERROR);
			e.printStackTrace();
		}
		
		System.out.println(CLOSE_TASK);
        updateMessage(CLOSE_TASK);
		
		return null;
	}
	
    @Override protected void succeeded() {
        super.succeeded();
        
		final InformationDialog informationDialog =
				new InformationDialog(DIALOG_TITLE, DIALOG_DISCONNECTION_MESSAGE);
		informationDialog.setOnHidden(evt -> {
			Platform.exit();
		});
		informationDialog.show();
    }

    @Override protected void cancelled() {
        super.cancelled();
        
        m_socketLock.lock();
	
        if(m_socket != null && !m_socket.isClosed()) {
			try {
				m_socket.close();
			}
        	catch (IOException e) {
				e.printStackTrace();
			}
        }
        
        m_socketLock.unlock();
    }
}
