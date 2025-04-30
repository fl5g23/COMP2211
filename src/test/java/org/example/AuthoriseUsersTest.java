package org.example;

import javafx.application.Platform;
import javafx.stage.Stage;
import org.example.Controllers.UIController;
import org.example.Views.AuthoriseUsersView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

import java.util.ArrayList;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class AuthoriseUsersTest extends ApplicationTest {

    @Mock
    private UIController mockController;

    private Stage stage;
    private AuthoriseUsersView authoriseUsersView;

    @Override
    public void start(Stage stage) {
        this.stage = stage;
    }

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        // Create sample unauthorized users
        ArrayList<String> unauthorizedUsers = new ArrayList<>(Arrays.asList(
                "user1", "user2", "user3"
        ));

        // Setup mock controller behavior
        when(mockController.getUnauthorisedUsers()).thenReturn(unauthorizedUsers);

        // Initialize the view on the JavaFX Application Thread
        Platform.runLater(() -> {
            authoriseUsersView = new AuthoriseUsersView(stage, mockController);
        });
        WaitForAsyncUtils.waitForFxEvents();
    }

    @Test
    public void testRegularUserAuthorisation() {
        // Show the authorisation view on FX thread
        Platform.runLater(() -> authoriseUsersView.show());
        WaitForAsyncUtils.waitForFxEvents();

        // Select the first user from the list
        clickOn(".list-view .list-cell:text=\"user1\"");

        // Click the Accept button
        clickOn(".button:text=\"Accept\"");
        WaitForAsyncUtils.waitForFxEvents();

        // Verify that the controller was called with correct parameters
        verify(mockController).authoriseUser("user1", "User");
    }

    @Test
    public void testAdminUserAuthorisation() {
        // Show the authorisation view on FX thread
        Platform.runLater(() -> authoriseUsersView.show());
        WaitForAsyncUtils.waitForFxEvents();

    // Select the first user from the list
    clickOn(".list-view .list-cell:text=\"user1\"");

        // Check the admin checkbox
        clickOn(".check-box");

        // Click the Accept button
        clickOn(".button:text=\"Accept\"");
        WaitForAsyncUtils.waitForFxEvents();

        // Verify that the controller was called with correct parameters
        verify(mockController).authoriseUser("user1", "Admin");
    }

    @Test
    public void testRejectUser() {
        // Show the authorisation view on FX thread
        Platform.runLater(() -> authoriseUsersView.show());
        WaitForAsyncUtils.waitForFxEvents();

    // Select the first user from the list
    clickOn(".list-view .list-cell:text=\"user1\"");

    // Click the Reject button
    clickOn(lookup(".button").nth(1).queryButton());
        WaitForAsyncUtils.waitForFxEvents();

        // Verify that the controller was called to delete the user
        verify(mockController).deleteUser("user1");
    }

    @Test
    public void testNoUserSelected() {
        // Show the authorisation view on FX thread
        Platform.runLater(() -> authoriseUsersView.show());
        WaitForAsyncUtils.waitForFxEvents();

        // Click Accept without selecting a user
        clickOn(".button:text=\"Accept\"");
        WaitForAsyncUtils.waitForFxEvents();

        // Verify that no authorization was performed
        verify(mockController, never()).authoriseUser(anyString(), anyString());
    }
}