package org.example;

import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

  @Override
  public void start(Stage primaryStage) {
    LoginPage loginPage = new LoginPage(primaryStage);
    loginPage.show(primaryStage);
  }

  public static void main(String[] args) {
    launch(args);
  }
}
