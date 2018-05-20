/**
 * Author: Jeremy Dong
 * Date: May 16, 2018
 * Project: Text Editor
 */

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.text.Text;
import javafx.scene.text.Font;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.event.EventHandler;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.KeyCode;
import javafx.event.ActionEvent;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.util.Duration;
import javafx.geometry.VPos;


// TODO: HOW TO HANDLE ENTERS? KEY PRESSED - KeyCode.ENTER

public class Editor extends Application {
    private Group root;
    private Scene scene;
    private TextBuffer buffer; // Fast DLL for storing text
    private Rectangle cursor;
    private int cursorX;
    private int cursorY;

    private final static int WINDOW_HEIGHT = 500;
    private final static int WINDOW_WIDTH = 500;
    private final static int FONT_SIZE = 12;
    private final static String FONT_NAME = "Verdana";
    private final static int STARTING_X = 5;
    private final static int STARTING_Y = 0;

    /** Constructor for instantiating Cursor, TextBuffer */
    public Editor() {
		buffer = new TextBuffer();
		cursorX = STARTING_X;
		cursorY = STARTING_Y;
		cursor = new Rectangle(cursorX, cursorY, 1, FONT_SIZE);
		cursor.setFill(Color.BLACK); // sets color of rectangle to black

    }
    
	/** Event Handler for handling keys that get pressed */
	private class KeyEventHandler implements EventHandler<KeyEvent> {
		private Text textToDisplay;

		@Override
		public void handle(KeyEvent keyEvent) {
			// Check if a character-generating key was typed
			if (keyEvent.getEventType() == keyEvent.KEY_TYPED) {
				String character = keyEvent.getCharacter();
				if (character.length() > 0 && character.charAt(0) != 8 && !keyEvent.isShortcutDown()) {
					// Create a new text object containing the typed character
					textToDisplay = new Text(cursorX, cursorY, character);
					textToDisplay.setTextOrigin(VPos.TOP);
					textToDisplay.setFont(Font.font(FONT_NAME, FONT_SIZE));
					buffer.add(textToDisplay); // add text to buffer
					root.getChildren().add(textToDisplay); // add text to scene graph

					// Update cursor position
					cursorX += (int) Math.rint(textToDisplay.getLayoutBounds().getWidth());
					cursor.setX(cursorX);

					// marks key event as finished
					keyEvent.consume();
				}
			} else if (keyEvent.getEventType() == keyEvent.KEY_PRESSED) {
				KeyCode code = keyEvent.getCode(); // only key pressed key events have an associated code

				// Need to handle arrows, backspace, shortcut keys (command)
				// Shortcut: + or =, -, s
				if (code == KeyCode.UP) {
					// DO SOMETHING
				} else if (code == KeyCode.DOWN) {
					// DO SOMETHING
				} else if (code == KeyCode.LEFT) {
					// DO SOMETHING
				} else if (code == KeyCode.RIGHT) {
					// DO SOMETHING
				} else if (code == KeyCode.BACK_SPACE) {
					// DO SOMETHING
				} else {

				}
			}
		}
	}

    /** Event Handler for handling blinking cursor */
    private class BlinkCursorEventHandler implements EventHandler<ActionEvent> {
    	private int currentColorIndex;
    	private Color[] boxColors;

    	public BlinkCursorEventHandler() {
    		currentColorIndex = 0;
    		boxColors =  new Color[] {Color.BLACK, Color.WHITE};
    		changeColor();
    	}

    	private void changeColor() {
    		cursor.setFill(boxColors[currentColorIndex]);
    		currentColorIndex = (currentColorIndex + 1) % boxColors.length;
    	}

    	@Override
    	public void handle(ActionEvent event) {
    		changeColor();
    	}
    }

    private void makeCursorBlink() {
    	final Timeline timeline = new Timeline();
    	timeline.setCycleCount(Timeline.INDEFINITE);
    	BlinkCursorEventHandler blinkCursor = new BlinkCursorEventHandler(); // instantiate event handler for blinking
    	KeyFrame keyFrame = new KeyFrame(Duration.seconds(0.8), blinkCursor); // create blinking key frame
    	timeline.getKeyFrames().add(keyFrame); // add blinking key frame to timeline
    	timeline.play();
    }

    @Override
    public void start(Stage stage) {
        root = new Group();
        scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);

        // Add blinking cursor to screen 
        root.getChildren().add(cursor);
        makeCursorBlink();

		// Once KeyEventHandler object is instantiated, it calls on the handle method
		// to handle the KeyEvent every time a key is pressed
		EventHandler<KeyEvent> keyEventHandler = new KeyEventHandler();
		scene.setOnKeyTyped(keyEventHandler);
		scene.setOnKeyPressed(keyEventHandler);

        stage.setTitle("Jeremy's Editor");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
