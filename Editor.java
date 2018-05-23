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
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.util.Duration;
import javafx.geometry.VPos;


/**
 * Core DS:
 * 	TextBuffer - Fast DLL for storing Text objects displayed in Editor
 * 		curr: pointer to current node that cursor was adj to
 * 		traverser: pointer to current node that can be used to traverse TextBuffer
 * 	Scene Graph - Tree for rendering Text in TextBuffer
 * 		root: Group Node for displaying all Text
 * 		children: cursor node + text nodes
 *
 * Rendering (linear time)
 * 	Recalculated position of all Text objects after the cursor whenever a special key
 * 	such as enter or backspace was pressed
 * 	Used TextBuffer to traverse through all Text nodes that needed to be repositioned
 *
 */

// TODO: FIX UP COMMAND
// TODO: IMPLEMENT DOWN COMMAND


public class Editor extends Application {
    private Group root;
    private TextBuffer buffer;
    private Rectangle cursor;
    private int cursorX;
    private int cursorY;
    private int windowHeight;
    private int windowWidth;
    private double textHeight;
    private boolean enterSeen;
    private int lowerBoundY;

    private final static int STARTING_WINDOW_HEIGHT = 500;
    private final static int STARTING_WINDOW_WIDTH = 500;
    private final static int FONT_SIZE = 12;
    private final static String FONT_NAME = "Verdana";
    private final static int STARTING_X = 5;
    private final static int STARTING_Y = 0;

    /** Constructor for instantiating Cursor, TextBuffer */
    public Editor() {
		buffer = new TextBuffer();
		cursorX = STARTING_X;
		cursorY = STARTING_Y;
		lowerBoundY = STARTING_Y;
		windowHeight = STARTING_WINDOW_HEIGHT;
		windowWidth = STARTING_WINDOW_WIDTH;
		enterSeen = false;

		// t is a temporary Text obj used for determining the height of the font so that
		// cursor height can be set
		Text t = new Text(0, 0, "");
		t.setFont(Font.font(FONT_NAME, FONT_SIZE));
		textHeight = t.getLayoutBounds().getHeight();
		cursor = new Rectangle(cursorX, cursorY, 1, textHeight);
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
					textToDisplay = new Text(character);
					textToDisplay.setTextOrigin(VPos.TOP);
					textToDisplay.setFont(Font.font(FONT_NAME, FONT_SIZE));

					// Add Text to buffer and scene graph
					buffer.add(textToDisplay);
					root.getChildren().add(textToDisplay);

					int prevX = cursorX;
					updateCursor(cursorX + textToDisplay.getLayoutBounds().getWidth(), cursorY);
					if (cursorX > windowWidth - 5) { // if at end of line, move cursor to new line and display Text there
						newline();
						textToDisplay.setX(cursorX);
						textToDisplay.setY(cursorY);
						updateCursor(cursorX + textToDisplay.getLayoutBounds().getWidth(), cursorY);
					} else { // display Text normally and move cursor normally
						textToDisplay.setX(prevX);
						textToDisplay.setY(cursorY);
					}
					setCursor(cursorX, cursorY);
					reformat();

					// marks key event as finished
					keyEvent.consume();
				}
			} else if (keyEvent.getEventType() == keyEvent.KEY_PRESSED) {
				KeyCode code = keyEvent.getCode(); // only key pressed key events have an associated code
				Text text = buffer.currText();

				// Need to handle arrows, backspace, shortcut keys (command), and enter
				// Shortcut: + or =, -, s
				if (code == KeyCode.UP) {
					if (cursorY != STARTING_Y) {
						while (buffer.currText().getY() > cursorY - Math.floor(textHeight)) {
							buffer.prevCurr();
						}
						while (buffer.currText().getX() > cursorX) {
							buffer.prevCurr();
						}

						// Update cursor to either be on left or right side of character ABOVE
						// depending on which side is closer to cursorX
						setCursorInBestPos();
					}
				} else if (code == KeyCode.DOWN) {
					if (lowerBoundY > cursorY) {
						while (buffer.nextText().getY() < cursorY + Math.floor(textHeight)) {
							buffer.nextCurr();
						}
						buffer.nextCurr();
                        while (buffer.nextText() != null &&  buffer.nextText().getX() < cursorX
                                && buffer.nextText().getY() <= cursorY + Math.floor(textHeight)) {
                            buffer.nextCurr();
                        }

                        // Update cursor to either be on left or right side of character BELOW
                        // depending on which side is closer to cursorX
                        setCursorInBestPos();
					}
				} else if (code == KeyCode.LEFT) {
					if (text != null) {
						// If cursor is at beginning of new line, move to end of line above without changing buffer
						if (round(text.getY()) != cursorY) {
							updateCursor(text.getX() + text.getLayoutBounds().getWidth(), text.getY());
						} else {
							updateCursor(text.getX(), text.getY());
							buffer.prevCurr();
						}
						setCursor(cursorX, cursorY);

						// If enter "\r" is seen, call handle again to skip over it
						skipOverEnter(text, keyEvent);
					}
				} else if (code == KeyCode.RIGHT) {
					Text nextText = buffer.nextText();
					if (nextText != null) {
						// If cursor is at end of line, move to beginning of new line without changing buffer
						if (round(nextText.getY()) != cursorY) {
							updateCursor(nextText.getX(), nextText.getY());
						} else {
							updateCursor(cursorX + nextText.getLayoutBounds().getWidth(), cursorY);
							buffer.nextCurr();
						}
						setCursor(cursorX, cursorY);

						// If enter "\r" is seen, call handle again to skip over it
                        skipOverEnter(nextText, keyEvent);
					}
				} else if (code == KeyCode.BACK_SPACE) {
					if (text != null) {
						root.getChildren().remove(text); // remove from graph
						buffer.remove(); // remove from buffer;
						text = buffer.currText(); // removing from buffer will cause text to be assigned to a new Text Node
						if (text != null) {
							updateCursor(text.getX() + text.getLayoutBounds().getWidth(), text.getY());
						} else {
							updateCursor(STARTING_X, STARTING_Y);
						}
						setCursor(cursorX, cursorY);
					}
					reformat();
				} else if (code == KeyCode.ENTER) {
					newline();
					setCursor(cursorX, cursorY);
					reformat();
				}
				enterSeen = false;
			}
		}

		private void newline() {
			cursorX = STARTING_X;
			cursorY += textHeight;
		}

		private void updateCursor(double x, double y) {
			cursorX = round(x);
			cursorY = round(y);
		}

		private void setCursor(int x, int y) {
			cursor.setX(x);
			cursor.setY(y);
		}

		private int round(double x) {
			return (int) Math.rint(x);
		}

		/**
		 * Reformats all of the Text after the cursor
		 * Assumes that cursor is in correct position
		 */
		private void reformat() {
			int newX = cursorX;
			int newY = cursorY;
			while (buffer.hasNextTrav()) {
				Text textToBeMoved = buffer.nextTrav();
				if (newX + textToBeMoved.getLayoutBounds().getWidth() > windowWidth - 5
						|| textToBeMoved.getText().equals("\r")) {
					newX = STARTING_X;
					newY += textHeight;
				}
				textToBeMoved.setX(newX);
				textToBeMoved.setY(round(newY));
				newX += round(textToBeMoved.getLayoutBounds().getWidth());
			}

			Text text = buffer.currText();
			if (text != null && text.getY() > lowerBoundY) {
				lowerBoundY = round(buffer.currText().getY());
			}

			buffer.resetTrav(); // reset traversal pointer
		}

		private void skipOverEnter(Text text, KeyEvent keyEvent) {
            if (text.getText().equals("\r") && !enterSeen) {
                enterSeen = true;
                this.handle(keyEvent);
            }
        }

		private void setCursorInBestPos() {
            Text text = buffer.currText();
            if (Math.abs(text.getX() - cursorX) < Math.abs(text.getX() + text.getLayoutBounds().getWidth() - cursorX)) {
                updateCursor(text.getX(), text.getY());
                buffer.prevCurr();
            } else {
                updateCursor(text.getX() + text.getLayoutBounds().getWidth(), text.getY());
            }
            setCursor(cursorX, cursorY);
        }
	}


    private void makeCursorBlink() {
    	final Timeline timeline = new Timeline();
    	timeline.setCycleCount(Timeline.INDEFINITE);
    	BlinkCursorHandler blinkCursor = new BlinkCursorHandler(cursor); // instantiate event handler for blinking
    	KeyFrame keyFrame = new KeyFrame(Duration.seconds(0.8), blinkCursor); // create blinking key frame
    	timeline.getKeyFrames().add(keyFrame); // add blinking key frame to timeline
    	timeline.play();
    }

    @Override
    public void start(Stage stage) {
        root = new Group();
        Scene scene = new Scene(root, STARTING_WINDOW_WIDTH, STARTING_WINDOW_HEIGHT);

        // Add blinking cursor to screen 
        root.getChildren().add(cursor);
        makeCursorBlink();

		// Once KeyEventHandler object is instantiated, it calls on the handle method
		// to handle the KeyEvent every time a key is pressed/typed
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




