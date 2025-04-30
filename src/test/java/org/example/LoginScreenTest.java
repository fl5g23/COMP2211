package org.example;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Fail.fail;

import org.example.Views.LoginPage;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationTest;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.Controllers.UIController;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import java.util.ArrayList;
import java.util.Arrays;

@ExtendWith(ApplicationExtension.class)
public class LoginScreenTest {
    private LoginPage loginPage;
    private Stage primaryStage;
    private TextField usernameField;
    private PasswordField passwordField;
    private Button loginButton;
    private Button registerButton;

    @Mock
    private UIController mockController;

    private AutoCloseable closeable;

    @Start
    private void start(Stage stage) {
        this.primaryStage = stage;
    }

    @BeforeEach
    public void setUp() {
        // Initialize Mockito mocks
        closeable = MockitoAnnotations.openMocks(this);

        Platform.runLater(() -> {
            try {
                // Initialize the LoginPage with the mock controller
                primaryStage = new Stage();
                loginPage = new LoginPage(primaryStage);

                // Set the mock controller using the provided setter method
                loginPage.setController(mockController);

                // Display the login page
                loginPage.show(primaryStage);

                // Find UI components using lookup
                Scene scene = primaryStage.getScene();

                // Get VBox inside StackPane
                StackPane root = (StackPane) scene.getRoot();
                VBox vbox = (VBox) root.getChildren().get(0);

                // Find the username and password fields by type
                for (javafx.scene.Node node : vbox.getChildren()) {
                    if (node instanceof TextField && !(node instanceof PasswordField)) {
                        usernameField = (TextField) node;
                    } else if (node instanceof PasswordField) {
                        passwordField = (PasswordField) node;
                    } else if (node instanceof HBox) {
                        HBox buttonBox = (HBox) node;
                        // Find buttons
                        for (javafx.scene.Node buttonNode : buttonBox.getChildren()) {
                            if (buttonNode instanceof Button) {
                                Button button = (Button) buttonNode;
                                if ("Register".equals(button.getText())) {
                                    registerButton = button;
                                } else if ("Login".equals(button.getText())) {
                                    loginButton = button;
                                }
                            }
                        }
                    }
                }

                assertNotNull(usernameField, "Username field not found");
                assertNotNull(passwordField, "Password field not found");
                assertNotNull(loginButton, "Login button not found");
                assertNotNull(registerButton, "Register button not found");

            } catch (Exception e) {
                e.printStackTrace();
                fail("Failed to set up LoginPage test: " + e.getMessage());
            }
        });

        // Wait for JavaFX thread to complete initialization
        WaitForAsyncUtils.waitForFxEvents();
    }

    @Test
    public void testSuccessfulLogin(FxRobot robot) {
        // Set up mock controller behavior for successful login
        ArrayList<Object> successResult = new ArrayList<>(Arrays.asList(true, true, true, "admin"));
        when(mockController.userExists("validUser", "validPassword")).thenReturn(successResult);

        // Enter credentials
        Platform.runLater(() -> {
            usernameField.setText("validUser");
            passwordField.setText("validPassword");
        });
        WaitForAsyncUtils.waitForFxEvents();

        // Click login button
        robot.clickOn(loginButton);
        WaitForAsyncUtils.waitForFxEvents();

        // Verify interactions
        verify(mockController).userExists("validUser", "validPassword");
        verify(mockController).setup();
        verify(mockController).showMainScreen("admin");

        // Verify no alerts were shown
        verify(mockController, never()).showAlert(any(), anyString());
    }

    @Test
    public void testInvalidUsername(FxRobot robot) {
        // Set up mock controller behavior for invalid username
        ArrayList<Object> invalidUsernameResult = new ArrayList<>(Arrays.asList(false, false, false, ""));
        when(mockController.userExists("invalidUser", "password")).thenReturn(invalidUsernameResult);

        // Enter credentials
        Platform.runLater(() -> {
            usernameField.setText("invalidUser");
            passwordField.setText("password");
        });
        WaitForAsyncUtils.waitForFxEvents();

        // Click login button
        robot.clickOn(loginButton);
        WaitForAsyncUtils.waitForFxEvents();

        // Verify interactions
        verify(mockController).userExists("invalidUser", "password");
        verify(mockController).setup();
        verify(mockController).showAlert(null, "usernotexist");
        verify(mockController, never()).showMainScreen(anyString());
    }

    @Test
    public void testInvalidPassword(FxRobot robot) {
        // Set up mock controller behavior for invalid password
        ArrayList<Object> invalidPasswordResult = new ArrayList<>(Arrays.asList(true, false, false, ""));
        when(mockController.userExists("validUser", "wrongPassword")).thenReturn(invalidPasswordResult);

        // Enter credentials
        Platform.runLater(() -> {
            usernameField.setText("validUser");
            passwordField.setText("wrongPassword");
        });
        WaitForAsyncUtils.waitForFxEvents();

        // Click login button
        robot.clickOn(loginButton);
        WaitForAsyncUtils.waitForFxEvents();

        // Verify interactions
        verify(mockController).userExists("validUser", "wrongPassword");
        verify(mockController).setup();
        verify(mockController).showAlert(null, "passwordwrong");
        verify(mockController, never()).showMainScreen(anyString());
    }

    @Test
    public void testNotAuthorized(FxRobot robot) {
        // Set up mock controller behavior for unauthorized user
        ArrayList<Object> unauthorizedResult = new ArrayList<>(Arrays.asList(true, true, false, ""));
        when(mockController.userExists("unauth", "password")).thenReturn(unauthorizedResult);

        // Enter credentials
        Platform.runLater(() -> {
            usernameField.setText("unauth");
            passwordField.setText("password");
        });
        WaitForAsyncUtils.waitForFxEvents();

        // Click login button
        robot.clickOn(loginButton);
        WaitForAsyncUtils.waitForFxEvents();

        // Verify interactions
        verify(mockController).userExists("unauth", "password");
        verify(mockController).setup();
        verify(mockController).showAlert(null, "notauthorised");
        verify(mockController, never()).showMainScreen(anyString());
    }

    @Test
    public void testEmptyCredentials(FxRobot robot) {
        // Enter empty credentials
        Platform.runLater(() -> {
            usernameField.setText("");
            passwordField.setText("");
        });
        WaitForAsyncUtils.waitForFxEvents();

        // Click login button
        robot.clickOn(loginButton);
        WaitForAsyncUtils.waitForFxEvents();

        // Verify interactions
        verify(mockController, never()).userExists(anyString(), anyString());
        verify(mockController).showAlert(null, "userpwdempty");
        verify(mockController, never()).showMainScreen(anyString());
    }

    @Test
    public void testSuccessfulRegistration(FxRobot robot) {
        // Set up mock controller behavior for registration
        ArrayList<Object> newUserResult = new ArrayList<>(Arrays.asList(false, false, false, ""));
        when(mockController.userExists("newUser", "newPassword")).thenReturn(newUserResult);

        // Enter credentials
        Platform.runLater(() -> {
            usernameField.setText("newUser");
            passwordField.setText("newPassword");
        });
        WaitForAsyncUtils.waitForFxEvents();

        // Click register button
        robot.clickOn(registerButton);
        WaitForAsyncUtils.waitForFxEvents();

        // Verify interactions
        verify(mockController).userExists("newUser", "newPassword");
        verify(mockController).addUser("newUser", "newPassword");
        verify(mockController, never()).showAlert(any(), anyString());
    }

    @Test
    public void testUserAlreadyExists(FxRobot robot) {
        // Set up mock controller behavior for existing username
        ArrayList<Object> existingUserResult = new ArrayList<>(Arrays.asList(true, false, false, ""));
        when(mockController.userExists("existingUser", "password")).thenReturn(existingUserResult);

        // Enter credentials
        Platform.runLater(() -> {
            usernameField.setText("existingUser");
            passwordField.setText("password");
        });
        WaitForAsyncUtils.waitForFxEvents();

        // Click register button
        robot.clickOn(registerButton);
        WaitForAsyncUtils.waitForFxEvents();

        // Verify interactions
        verify(mockController).userExists("existingUser", "password");
        verify(mockController, never()).addUser(anyString(), anyString());
        verify(mockController).showAlert(null, "usernamealreadyexists");
    }

    @Test
    public void testEmptyCredentialsRegistration(FxRobot robot) {
        // Enter empty credentials
        Platform.runLater(() -> {
            usernameField.setText("");
            passwordField.setText("");
        });
        WaitForAsyncUtils.waitForFxEvents();

        // Click register button
        robot.clickOn(registerButton);
        WaitForAsyncUtils.waitForFxEvents();

        // Verify interactions
        verify(mockController, never()).userExists(anyString(), anyString());
        verify(mockController, never()).addUser(anyString(), anyString());
        verify(mockController).showAlert(null, "userpwdempty");
    }

    @AfterEach
    public void tearDown() throws Exception {
        // Clean up Mockito resources
        if (closeable != null) {
            closeable.close();
        }

        // Clean up JavaFX resources
        Platform.runLater(() -> {
            if (primaryStage != null) {
                primaryStage.close();
            }
        });

        // Wait for JavaFX thread to complete cleanup
        WaitForAsyncUtils.waitForFxEvents();
    }
}