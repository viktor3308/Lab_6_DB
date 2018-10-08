package ru.eltech.db;

import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ResourceBundle;
import javafx.fxml.Initializable;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.util.Callback;

public class ServerFormController implements Initializable {
	
	private final static String URL = "jdbc:sqlite:db.db";
	private final static String CONDITIONERS_TABLE = "conditioners";
	private final static String ROOMS_TABLE = "rooms";
	private final static String ALARMS_TABLE = "alarms";
	private final static String DATABASE_CONNECTION_FAILED = "Unable to connect to database";
	private final static String DATABASE_READ_ERROR = "Error on Building Data";
	private Connection m_connection = null;
	private Statement m_statement = null;
	private ObservableList<ObservableList<String>> m_conditionersData;
	private ObservableList<ObservableList<String>> m_roomsData;
	private ObservableList<ObservableList<String>> m_alarmsData;

    @FXML
    private TableView<ObservableList<String>> tvRooms;

    @FXML
    private TableView<ObservableList<String>> tvAlarms;

    @FXML
    private TableView<ObservableList<String>> tvConditioners;
    
    @FXML
    private Label lblStatus;
    
	public StringProperty statusProperty() {
		return lblStatus.textProperty();
	}
	
	public Statement getStatement() {
		return m_statement;
	}
    
	private void connect() {
		try {
			m_connection = DriverManager.getConnection(URL);
			m_statement = m_connection.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println(DATABASE_CONNECTION_FAILED);
		}
	}
	
	@SuppressWarnings("unchecked")
	public void buildData(
			String tableName,
			TableView<ObservableList<String>> tableView,
			ObservableList<ObservableList<String>> data
			) {
		data = FXCollections.observableArrayList();
		ResultSet rs = null;
		try {
			String sql = "SELECT * from	" + tableName;
			rs = m_statement.executeQuery(sql);
			for (int i=0; i<rs.getMetaData().getColumnCount(); i++) {
				final int j = i;
				TableColumn<ObservableList<String>, String> col = new TableColumn<ObservableList<String>, String>(rs.getMetaData().getColumnName(i+1));
				col.setCellValueFactory(new Callback<CellDataFeatures<ObservableList<String>,String>,ObservableValue<String>>() {
					public ObservableValue<String> call(CellDataFeatures<ObservableList<String>, String> param) {
						return new SimpleStringProperty(param.getValue().get(j).toString());
					} });

				tableView.getColumns().addAll(col);
			}
			while(rs.next()) {
				ObservableList<String> row = FXCollections.observableArrayList();
				for(int i=1 ; i<=rs.getMetaData().getColumnCount(); i++) {
					row.add(rs.getString(i));
				}
				data.add(row);
			}
			tableView.setItems(data);
			}
			catch(Exception e) {
				e.printStackTrace();
				System.out.println(DATABASE_READ_ERROR);
			}
	}
    
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		connect();
		buildData(CONDITIONERS_TABLE, tvConditioners, m_conditionersData);
		buildData(ROOMS_TABLE, tvRooms, m_roomsData);
		buildData(ALARMS_TABLE, tvAlarms, m_alarmsData);
	}

}
