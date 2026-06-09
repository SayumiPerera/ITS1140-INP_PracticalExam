package lk.ijse.auction_system;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class ClientApp extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ClientApp.class.getResource("client.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 320, 240);
        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

//    public void btnDisconnect(ActionEvent actionEvent) {
//    }

//    public void txtUserName(ActionEvent actionEvent) {
//    }

//    public void txtItemName(ActionEvent actionEvent) {
//    }
//
//    public void btnPlaceBid(ActionEvent actionEvent) {
//    }
//
//    public void txtBidAmount(ActionEvent actionEvent) {
//    }
}