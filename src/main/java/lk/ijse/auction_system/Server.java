package lk.ijse.auction_system;


import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;


public class Server  {

    @FXML
    private TextArea txtBidFeed;

    @FXML
    private Button btnPlaceBid;

    private static final int PORT = 6000;
    private static HashSet<ObjectOutputStream> writers = new HashSet<>();
    private boolean isRunning = false;
    private ServerSocket serverSocket;


    @FXML
    public void initialize() {
        appendMessage("Auction Server Started, Port 6000 ...");
    }

    private void appendMessage(String s) {
        Platform.runLater(() -> txtBidFeed.appendText(s + "\n"));
    }


    private void openClient() {
        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("client.fxml"));
                Scene scene = new Scene(loader.load());
                Stage stage = new Stage();
                stage.setTitle("Client connection");
                stage.setScene(scene);
                stage.setOnCloseRequest(windowEvent -> {
                    ClientHandler controller = loader.getController();
                    controller.closeConnection();
                });
                stage.show();
                appendMessage("New Client Added");
            } catch (IOException e) {
                appendMessage("Error while opening Client window");
                e.printStackTrace();
            }
        });
    }


//    public void closeConnection() {
//        try {
//            if (out != null) {
//                out.close();
//            }
//
//            if (in != null) {
//                in.close();
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }


    private void startServer() {
        isRunning = true;

        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(PORT);
                appendMessage("[Auction Server Started - Port " + PORT + "]");
                System.out.println("[Auction Server Started - Port " + PORT + "]");
                System.out.println("[Item:  Vintage Watch | Starting price: LKR 5,0000 ]");


                while(isRunning){
                    Socket clientSocket = serverSocket.accept();
                    appendMessage("Client connected: " + clientSocket.getInetAddress().getHostName());
                    System.out.println("Client connected : " + clientSocket.getInetAddress().getHostName());


                    Thread clientThread = new Thread(new ClientHandler(clientSocket));
                    clientThread.start();
                }
            } catch (IOException e) {
                e.printStackTrace();
                if (isRunning){
                    appendMessage("Error starting server: " + e.getMessage());
                    System.out.println("Error starting server: " + e.getMessage());
                }
            }
        }).start();
    }


    @FXML
    void btnPlaceBid(ActionEvent event) {
        if (!isRunning){
            startServer();
        }

        openClient();
    }

    private class ClientHandler implements Runnable{
        private Socket socket;
        private ObjectInputStream in;
        private ObjectOutputStream out;
        private String clientName;

        ClientHandler(Socket socket){
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                out = new ObjectOutputStream(socket.getOutputStream());
                in = new ObjectInputStream(socket.getInputStream());

                while(true){
                    out.writeObject("CLIENTNAME");
                    clientName = (String) in.readObject();
                    if (!clientName.trim().isEmpty() && clientName != null){
                        break;
                    }
                    appendMessage("Invalid name, requesting again");
                    System.out.println("Invalid name, requesting again");
                }

                out.writeObject("NAMEACCEPTED");
                appendMessage("Client " + clientName + " connected");
                System.out.println("Client " + clientName + " connected");
                broadcast("TEXT " + clientName + " joined the auction");
                System.out.println("Client " + clientName + " joined the auction");

                synchronized (writers){
                    writers.add(out);
                }

                while(true){
                    try {
                        Object message = in.readObject();
                        if (message == null) break;
                        if (message instanceof String){
                            String text = (String) message;
                            broadcast("TEXT " + clientName + ": " +text);
                            System.out.println("TEXT " + clientName + ": " +text);
                        }
                    } catch (IOException e) {
                        /*throw new RuntimeException(e);*/
                        System.out.println("Client Logout");
                        break;
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);

                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                appendMessage("Error connecting to client");
                System.out.println("Error connecting to client");
            }finally {
                if (clientName != null){
                    appendMessage("Client " + clientName + " disconnected");
                    System.out.println("Client " + clientName + " disconnected");
                    broadcast("TEXT " + clientName + " left the auction");
                    System.out.println("Client " + clientName + " left the auction");
                }

                synchronized (writers){
                    writers.remove(out);
                }

                try {
                    socket.close();
                } catch (IOException e) {
                    appendMessage("Error closing client socket");
                    System.out.println("Error closing client socket");
                    e.printStackTrace();
                }
            }
        }


    }

    private void broadcast ( String message){
        synchronized (writers){
            for (ObjectOutputStream writer : writers){
                try {
                    writer.writeObject(message);
                    writer.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                    appendMessage("Error broadcasting the message..");
                    System.out.println("Error broadcasting the message..");
                }
            }
        }
    }

}