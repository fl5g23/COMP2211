package org.example;

import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

  @Override
  public void start(Stage primaryStage) {
    MainScreen mainScreen = new MainScreen(primaryStage);
    mainScreen.show(); // Load the main screen UI
  }

  public static void main(String[] args) {
    launch(args);
  }
}
