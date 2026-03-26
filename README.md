# Rock, Paper, Scissors with Computer Vision

A Java-based Rock, Paper, Scissors game where the user plays against the computer using a webcam. The game leverages a custom machine learning model trained via Google Teachable Machine to recognize user hand gestures (Rock, Paper, or Scissors) in real-time.

## 🧠 Model Training Process
We trained an image classification model using [Google Teachable Machine](https://teachablemachine.withgoogle.com/). The model was trained to recognize three distinct classes: Rock, Paper, and Scissors based on hand gestures. 

Watch the model training process below:

https://github.com/user-attachments/assets/579da093-e6fb-4505-b4b1-f58530149400

*(If the video above doesn't load, you can also [download/view it here](./Train.mp4))*

## 🎮 Application Demo
Here is a demonstration of the application in action, showing the real-time gesture recognition and gameplay against the computer:

https://github.com/user-attachments/assets/b519b5e7-5ad7-46fc-a3d0-0e37a1199d74

*(If the video above doesn't load, you can also [download/view it here](./Demo.mp4))*

## 💻 Code Explanation

The application follows a modular architecture separating the user interface, game logic, and model inference.

### Core Components

1. **`RockPaperScissors.java`**
   The main driver class that orchestrates the overall application flow. It sets up the primary window (stage), manages the transitions between different scenes (Main Scene vs Game Over Scene), starts the camera capture, and initializes the machine learning model.

2. **`MainScene.java`**
   Handles the primary gameplay UI. It displays the live camera feed, real-time predictions, confidence scores, and instructions. It captures frames from the webcam and continuously sends them to the model for prediction. It implements stabilization logic so that a gesture is only confirmed if the model predicts the same gesture with high confidence continuously over a designated period.

3. **`GameLogic.java`**
   Contains the core rules of Rock, Paper, Scissors. It generates a random choice for the computer, compares it against the user's predicted gesture, and evaluates the winner based on the traditional game rules.

4. **`ModelProcessor.java`**
   Responsible for interfacing with the exported Teachable Machine model. It processes incoming image frames from the webcam, runs them through the loaded model, and returns the predicted class along with its probability/confidence score.

5. **`GameOverScene.java`**
   Manages the end-of-game UI. It displays the final result (Win, Lose, or Tie) along with the choices made by both the player and the computer, offering options to either play again or gracefully exit the application.
