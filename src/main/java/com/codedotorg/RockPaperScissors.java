package com.codedotorg;

import com.codedotorg.modelmanager.CameraController;
import com.codedotorg.modelmanager.ModelManager;

import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.util.Duration;

public class RockPaperScissors {

    /** The main window of the app */
    private Stage window;

    /** The MainScene of the game */
    private MainScene game;

    /** The GameOverScene to display the winner */
    private GameOverScene gameOver;

    /** The GameLogic to handle the game logic */
    private GameLogic logic;

    /** Manages the TensorFlow model used for image classification */
    private ModelManager model;

    /** Controls the camera capture and provides frames to the TensorFlow model for classification */
    private CameraController cameraController;

    /** The Timeline to manage how often a prediction is made */
    private Timeline timeline;

    /**
     * Constructor for the RockPaperScissors class.
     * Sets up the window using the primaryStage, initializes the model
     * and camera capture, sets up the game scenes and logic.
     *
     * @param primaryStage the primary stage for the application
     */
    public RockPaperScissors(Stage primaryStage) {
        // Set up the window using the primaryStage
        setUpWindow(primaryStage);

        // Set up the model and camera capture
        cameraController = new CameraController();
        model = new ModelManager();

        // Set up the game scenes and logic
        game = new MainScene();
        gameOver = new GameOverScene();
        logic = new GameLogic();
    }

    /**
     * Sets up the window with the given primaryStage, sets the title of
     * the window to "Rock, Paper, Scissors", and adds a shutdown hook to
     * stop the camera capture when the app is closed.
     *
     * @param primaryStage the primary stage of the application
     */
    public void setUpWindow(Stage primaryStage) {
        // Set window to point to the primaryStage
        window = primaryStage;

        // Set the title of the window
        window.setTitle("Rock, Paper, Scissors");

        // Shutdown hook to stop the camera capture when the app is closed
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            cameraController.stopCapture();
        }));
    }
    
    /**
     * Starts a new game of Rock Paper Scissors by loading the main
     * screen and updating the game state.
     */
    public void playGame() {
        loadMainScreen();
        updateGame();
    }

    /**
     * Loads the main screen of the game, setting it to starting defaults
     * and displaying the window. Captures the camera view and sets the model
     * for the cameraController object. Retrieves the Loading object and
     * shows the loading animation while the camera is loading.
     */
    public void loadMainScreen() {
        // Set the game to starting defaults
        resetGame();

        // Display the window
        window.show();

        // Capture the camera view and set the model for the cameraController object
        cameraController.captureCamera(game.getCameraView(), model);

        // Retrieve the Loading object
        Loading cameraLoading = game.getLoadingAnimation();

        // Show the loading animation while the camera is loading
        cameraLoading.showLoadingAnimation(game.getCameraView());
    }

    /**
     * Updates the game by preparing the Start button.
     */
    public void updateGame() {
        game.getReadyButton().setVisible(true);
        game.getPromptLabel().setText("Click Start to begin!");
        game.getReadyButton().setOnAction(event -> {
            game.getReadyButton().setVisible(false);
            startStabilityCheck();
        });
    }

    private String candidateClass = null;
    private long candidateStartTime = 0;
    private float minConfidence = 0;
    private float maxConfidence = 0;

    /**
     * Continuously checks the tracked gesture.
     * To be valid, the user's gesture must remain the same, 
     * the confidence score must be >= 70%, and it must not fluctuate 
     * more than +- 7.5% (15% total) over a period of 3 seconds.
     */
    private void startStabilityCheck() {
        candidateClass = null;
        candidateStartTime = System.currentTimeMillis();
        minConfidence = 0;
        maxConfidence = 0;

        timeline = new Timeline(new KeyFrame(Duration.millis(100), event -> {
            String predictedClass = cameraController.getPredictedClass();
            float score = cameraController.getPredictedScore();
            long now = System.currentTimeMillis();

            if (predictedClass != null && !predictedClass.isEmpty()) {
                String cleanPredictedClass = predictedClass;
                if (cleanPredictedClass.contains(" ")) {
                    cleanPredictedClass = cleanPredictedClass.substring(cleanPredictedClass.indexOf(" ") + 1);
                }

                if (score < 0.70f) {
                    // Reset if confidence drops below 70%
                    candidateClass = null;
                    game.getPromptLabel().setText("Need more confidence (>70%)...");
                } else {
                    if (!cleanPredictedClass.equals(candidateClass)) {
                        candidateClass = cleanPredictedClass;
                        candidateStartTime = now;
                        minConfidence = score;
                        maxConfidence = score;
                        game.getPromptLabel().setText("Hold steady... (" + candidateClass + ")");
                    } else {
                        minConfidence = Math.min(minConfidence, score);
                        maxConfidence = Math.max(maxConfidence, score);
                        
                        if ((maxConfidence - minConfidence) > 0.15f) { // Fluctuation > 15% (+- 7.5%)
                            candidateStartTime = now;
                            minConfidence = score;
                            maxConfidence = score;
                            game.getPromptLabel().setText("Fluctuating! Hold steady...");
                        } else {
                            long elapsed = now - candidateStartTime;
                            if (elapsed >= 2000) {
                                timeline.stop();
                                game.getPromptLabel().setText("Locked!");
                                executeGameResult(predictedClass, score);
                                return;
                            } else {
                                game.getPromptLabel().setText("Hold steady... " + String.format("%.1f", (2000 - elapsed) / 1000.0) + "s");
                            }
                        }
                    }
                }
                game.showUserResponse(predictedClass, score);
            }
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    /**
     * Evaluates the game result against the computer choice and
     * transitions to the GameOver scene.
     */
    private void executeGameResult(String predictedClass, double predictedScore) {
        String computerChoice = logic.getComputerChoice();
        String winner = logic.determineWinner(predictedClass, computerChoice);
        
        if (logic.isGameOver()) {
            PauseTransition pause = new PauseTransition(Duration.seconds(3));
            pause.setOnFinished(e -> loadGameOver(winner));
            pause.play();
        }
    }

    /**
     * Loads the Game Over scene with the winner's name and sets the
     * playAgainButton to reset the game when clicked. Stops the timeline.
     *
     * @param winner the name of the winner of the game
     */
    public void loadGameOver(String winner) {
        // Retrieve the playAgainButton from the GameOverScene
        Button playAgainButton = gameOver.getPlayAgainButton();

        // Set the playAgainButton to reset the game when clicked
        playAgainButton.setOnAction(event -> {
            resetGame();
        });

        // Create the GameOverScene layout
        Scene gameOverScene = gameOver.createGameOverScene(winner, cameraController);

        // Set the GameOverScene in the window
        window.setScene(gameOverScene);

        // Stop the timeline
        timeline.stop();
    }

    /**
     * Resets the game by resetting the game logic, creating a new main scene,
     * and setting the window to display the new scene. If a timeline is currently
     * running, it is resumed.
     */
    public void resetGame() {
        // Reset the GameLogic
        logic.resetLogic();

        // Create the MainScene for the game
        Scene mainScene = game.createMainScene(cameraController);

        // Set the MainScene in the window
        window.setScene(mainScene);
        
        // Play the timeline if it is not null
        if (timeline != null) {
            timeline.play();
        }
    }

}
