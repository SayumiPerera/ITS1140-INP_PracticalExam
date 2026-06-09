package lk.ijse.auction_system;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;


public class ClientHandler {

    @FXML
    private Label lblHighestbid;

    @FXML
    private Button btnPlaceBid;

   @FXML
    private TextField txtBidAmount;

   @FXML
    private TextArea txtBidFeed;

   @FXML
    private TextField txtItemName;

   @FXML
    private TextField txtUserName;

   @FXML
    private Button btnDisconnect;

    @FXML
    private ListView<Object> messageView;

    private ObjectOutputStream out;
    private ObjectInputStream in;
    private String clientName;
    private boolean nameAccepted = false;

//    public ClientHandler(Socket clientSocket) {
//    }


    public void initialize() {
        messageView.setCellFactory(listView -> new ListCell<Object>() {
            @Override
            protected void updateItem(Object item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else if (item instanceof String) {
                    setText((String) item);
                    setGraphic(null);
                }
            }
        });


        try {
            Socket socket = new Socket("localhost", 6000);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            Thread thread = new Thread(() -> listenForMessages());
            thread.start();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }


    private void listenForMessages() {
        try {
            while(true){
                Object message = in.readObject();
                if (message == null) break;
                if (message instanceof String){
                    String text = (String) message;
                    if (text.startsWith("CLIENTNAME")){
                        if (!nameAccepted){
                            txtUserName();
                        }
                    }else if (text.startsWith("NAMEACCEPTED")){
                        nameAccepted = true;
                        Platform.runLater(() -> messageView.getItems().add("Connected as " + clientName));
                        System.out.println("Client Side: Connected as"+clientName);
                        Platform.runLater(() -> btnPlaceBid.setText("Client - " + clientName));
                    }else if (text.startsWith("TEXT")){
                        Platform.runLater(() ->{

                            if (text.startsWith("TEXT " + clientName + ": "+"BYE")){
                                messageView.getItems().add("Disconnected");
                                System.out.println("Client Side: "+"Disconnected");
                                closeConnection();
                                Platform.exit();
                            }
                            if (text.startsWith("TEXT " + clientName + ": "+"UPTIME")){
                                messageView.getItems().add("Server Uptime: "+text.substring(5+clientName.length()+2));
                            }
                            else if (text.startsWith("TEXT " + clientName + ": ")&&(!text.startsWith("TEXT " + clientName + ": "+"HELP"))){
                                messageView.getItems().add("You: " + text.substring(clientName.length()+2+5));
                                /*System.out.println("Client Side: You: " +text);///*/
                            }else {
                                messageView.getItems().add(text.substring(5));
                            }
                        });
                    }
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            Platform.runLater(() -> messageView.getItems().add("Disconnected" + e.getMessage()));
        }finally {
            closeConnection();
        }
    }


    public void closeConnection() {
        try {
            if (out != null) {
                out.close();
            }

            if (in != null) {
                in.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void txtUserName(){
        Platform.runLater(()-> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Enter your name");
            dialog.setHeaderText("Please enter your name");

            dialog.showAndWait().ifPresent(name -> {
                clientName = name.trim();
                if (clientName.isEmpty()) {
                    messageView.getItems().add("Name cannot be empty, Please try again");
                    System.out.println("Client Side: " + "Name cannot be empty, Please try again");
                    txtUserName();
                } else {
                    try {
                        out.writeObject(clientName);
                        out.flush();
                        dialog.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                        messageView.getItems().add("Error sending name, Please try again");
                        System.out.println("Client Side: " + "Error sending name, Please try again");
                    }
                }
            });

        });
    }

    public void btnDisconnect(ActionEvent actionEvent) {
    }

    public void txtItemName(ActionEvent actionEvent) {


    }

    public void btnPlaceBid(ActionEvent actionEvent) {
//        try {
//            while(true){
//                Object message = in.readObject();
//                if (bid > current_highest_bid) break;
//                if (message instanceof String){
//                    String text = (String) message;
//                    if (text.startsWith("")){
//                        if (!nameAccepted){
//                            updateState();
//                            broadcastMessage();
//                        }
//
//
    }

    public void txtBidAmount(ActionEvent actionEvent) {
    }

}
