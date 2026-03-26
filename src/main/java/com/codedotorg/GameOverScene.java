package com.codedotorg;

import com.codedotorg.modelmanager.CameraController;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class GameOverScene {

    /** Button to play the game again */
    private Button playAgainButton;

    /** Button to exit the app */
    private Button exitButton;

    /**
     * Constructs a GameOverScene object with an exit
     * button and a play again button.
     */
    public GameOverScene() {
        exitButton = new Button("Exit");
        playAgainButton = new Button("Play Again");
    }

    /**
     * Creates a new Scene object for the game over screen.
     * @param winner The name of the winner of the game.
     * @param cameraController The CameraController object used in the game.
     * @return A Scene object representing the game over screen.
     */
    public Scene createGameOverScene(String winner, CameraController cameraController) {
        // Set the action for when the exit button is clicked
        createExitButtonAction(cameraController);
        
        // Create the layout for the scene
        VBox layout = new VBox(20);
        layout.setAlignment(Pos.CENTER);

        // Create the label to display the winner
        Label winnerLabel = new Label(winner);

        // Add the label and buttons to the layout
        layout.getChildren().addAll(winnerLabel, playAgainButton, exitButton);

        // Create the scene with the layout, maintaining the mobile ratio
        Scene gameOverScene = new Scene(layout, 450, 800);
        gameOverScene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

        // Return the scene
        return gameOverScene;
    }

    /**
     * Returns the play again button.
     * 
     * @return the play again button
     */
    public Button getPlayAgainButton() {
        return playAgainButton;
    }

    /**
     * Sets the action for the exit button. When clicked, it
     * stops the camera capture and exits the program.
     */
    private void createExitButtonAction(CameraController cameraController) {
        exitButton.setOnAction(event -> {
            cameraController.stopCapture();
            System.exit(0);
        });
    }

}
