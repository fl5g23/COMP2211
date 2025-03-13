package org.example;

import javafx.application.Application;
import javafx.stage.Stage;
import org.example.Views.LoginPage;

public class Main extends Application {

  @Override
  public void start(Stage primaryStage) {
    // Create the login page (view)
    LoginPage loginPage = new LoginPage(primaryStage);
    loginPage.show(primaryStage);
  }

  public static void main(String[] args) {
    launch(args);
  }
}