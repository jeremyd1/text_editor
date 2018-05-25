/**
 * Author: Jeremy Dong
 * Date: May 16, 2018
 * Project: Text Editor
 */

import javafx.application.Application;
import javafx.event.ActionEvent;
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
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import java.io.File;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.ChangeListener;
import javafx.scene.input.MouseEvent;
import java.util.List;



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

// TODO: NEED UP, DOWN ARROW KEYS TO REMEMBER PREV POSITION
// TODO: MOUSE CLICKER
// TODO: LOAD AND SAVE

public class Editor extends Application {
    private Group root;
    private TextBuffer buffer;
    private Rectangle cursor;
    private int cursorX;
    private int cursorY;
    private int windowHeight;
    private int windowWidth;
    private int textHeight;
    private boolean enterSeen;
    private int lowerBoundY;
    private File file;

    private final static int STARTING_WINDOW_HEIGHT = 500;
    private final static int STARTING_WINDOW_WIDTH = 500;
    private final static int MARGIN = 5;
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
		textHeight = (int) Math.floor(t.getLayoutBounds().getHeight());
		cursor = new Rectangle(cursorX, cursorY, 1, textHeight);
		cursor.setFill(Color.BLACK); // sets color of rectangle to black
    }




	/****************************************************
     * Event Handler for handling keys that get pressed *
     ***************************************************/


	private class KeyEventHandler implements EventHandler<KeyEvent> {

		@Override
		public void handle(KeyEvent keyEvent) {
			// Check if a character-generating key was typed
			if (keyEvent.getEventType() == keyEvent.KEY_TYPED) {
				String character = keyEvent.getCharacter();
                    if (!keyEvent.isShortcutDown()) {
                        add(character);
                    }
					// marks key event as finished
					keyEvent.consume();
			} else if (keyEvent.getEventType() == keyEvent.KEY_PRESSED) {
				KeyCode code = keyEvent.getCode(); // only key pressed key events have an associated code
				Text text = buffer.currText();

				// Need to handle shortcuts,  arrows, backspace, and enter
				// Shortcut: + or =, -, s
				if (keyEvent.isShortcutDown()) {
				    if (code == KeyCode.S) {
				        try {
                            FileWriter fileWriter = new FileWriter(file);
                            buffer.currToSentinel();
                            buffer.nextCurr();
                            while (buffer.currText() != null) {
                                text = buffer.currText();
                                if (text.getText().equals("\r")) {
                                    fileWriter.write('\n');
                                } else {
                                    fileWriter.write(text.getText().toCharArray()[0]);
                                }
                                buffer.nextCurr();
                            }
                            fileWriter.close();
                            buffer.resetCurr();
                        } catch (IOException e) {
				            System.out.println("Unable to write to file");
				            return;
                        }
                    }
                } else if (code == KeyCode.UP) {
					if (cursorY != STARTING_Y) {
						while (buffer.currText().getY() > cursorY - textHeight) {
							buffer.prevCurr();
						}
						while (buffer.currText().getX() > cursorX) {
							buffer.prevCurr();
						}

						// Update cursor to either be on left or right side of character ABOVE
						// depending on which side is closer to cursorX
						setCursorToClosest(cursorX);
					}
				} else if (code == KeyCode.DOWN) {
					if (lowerBoundY > cursorY) {
						while (buffer.nextText().getY() < cursorY + textHeight) {
							buffer.nextCurr();
						}
						buffer.nextCurr();
                        while (buffer.nextText() != null &&  buffer.nextText().getX() < cursorX
                                && buffer.nextText().getY() <= cursorY + textHeight) {
                            buffer.nextCurr();
                        }

                        // Update cursor to either be on left or right side of character BELOW
                        // depending on which side is closer to cursorX
                        setCursorToClosest(cursorX);
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
						reformat();
					}
				} else if (code == KeyCode.ENTER) {
				    // If pressing enter at starting X,Y, need to add a place holder "\r" at the 1st line
                    // so that you can get back to that position
					if (text == null) {
					    Text placeHolderText = new Text("\r");
					    placeHolderText.setX(STARTING_X);
					    placeHolderText.setY(STARTING_Y);
					    placeHolderText.setTextOrigin(VPos.TOP);
					    placeHolderText.setFont(Font.font(FONT_NAME, FONT_SIZE));

					    buffer.add(placeHolderText);
					    root.getChildren().add(placeHolderText);
                    }
				    newline();
					setCursor(cursorX, cursorY);
				}
				enterSeen = false;
			}
		}

        private void skipOverEnter(Text text, KeyEvent keyEvent) {
            if (text.getText().equals("\r") && !enterSeen) {
                enterSeen = true;
                this.handle(keyEvent);
            }
        }
	}

	private void add(String character) {
        if (character.length() > 0 && character.charAt(0) != 8) {
            // Create a new text object containing the typed character
            Text textToDisplay = new Text(character);
            textToDisplay.setTextOrigin(VPos.TOP);
            textToDisplay.setFont(Font.font(FONT_NAME, FONT_SIZE));

            // Add Text to buffer and scene graph
            buffer.add(textToDisplay);
            root.getChildren().add(textToDisplay);

            int prevX = cursorX;
            updateCursor(cursorX + textToDisplay.getLayoutBounds().getWidth(), cursorY);
            if (cursorX > windowWidth - MARGIN) { // if at end of line, move cursor to new line and display Text there
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
        }
    }

    private void newline() {
        updateCursor(STARTING_X, cursorY + textHeight);
        lowerBoundY += textHeight;
    }

    private void updateCursor(double x, double y) {
        cursorX = round(x);
        cursorY = round(y);
    }

    private void setCursor(int x, int y) {
        cursor.setX(x);
        cursor.setY(y);
    }

    private void setCursorToClosest(double x) {
        Text text = buffer.currText();
        if (text == null) {
            updateCursor(STARTING_X, STARTING_Y);
        } else if (Math.abs(text.getX() - x) < Math.abs(text.getX() + text.getLayoutBounds().getWidth() - x)) {
            updateCursor(text.getX(), text.getY());
            buffer.prevCurr();
        } else {
            updateCursor(text.getX() + text.getLayoutBounds().getWidth(), text.getY());
        }
        setCursor(cursorX, cursorY);
    }

    private int round(double x) {
        return (int) Math.rint(x);
    }

    /**
     * Reformats all of the Text after the cursor
     * Assumes that cursor is in correct position
     */
    private void reformat() {
        int X = cursorX;
        int Y = cursorY;
        Text textToBeMoved = null;
        while (buffer.hasNextTrav()) {
            // When reformatting, this condition is necessary to ensure that a new line isn't created
            // if "\r" is the first Text obj in the editor
            if (buffer.currText() == null && buffer.nextText().getText().equals("\r")) {
                buffer.nextCurr();
            } else {
                textToBeMoved = buffer.nextTrav();
                if (X + textToBeMoved.getLayoutBounds().getWidth() > windowWidth - MARGIN
                        || textToBeMoved.getText().equals("\r")) {
                    X = STARTING_X;
                    Y += textHeight;
                }
                textToBeMoved.setX(X);
                textToBeMoved.setY(Y);
                X = round(X + textToBeMoved.getLayoutBounds().getWidth());
            }
        }

        if (textToBeMoved != null) {
            lowerBoundY = (int) textToBeMoved.getY();
        }

        buffer.resetTrav(); // reset traversal pointer
    }

    private void reformatFromBeginning() {
        cursorX = STARTING_X;
        cursorY = STARTING_Y;

        buffer.currToSentinel(); // by moving curr to sentinel, trav is also set to point at sentinel
        reformat();
        buffer.resetCurr();

        // Update and set cursor dynamically according to the Text obj it was adjacent to before reformatting
        Text text = buffer.currText();
        updateCursor(text.getX() + text.getLayoutBounds().getWidth(), text.getY());
        setCursor(cursorX, cursorY);
    }




    /**********************************************
     * Event Handler for handling blinking cursor *
     *********************************************/


    public class BlinkCursorHandler implements EventHandler<ActionEvent> {
        private int currentColorIndex;
        private Color[] boxColors;
        private Rectangle cursor;

        public BlinkCursorHandler(Rectangle r) {
            cursor = r;
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
    	BlinkCursorHandler blinkCursor = new BlinkCursorHandler(cursor); // instantiate event handler for blinking
    	KeyFrame keyFrame = new KeyFrame(Duration.seconds(0.8), blinkCursor); // create blinking key frame
    	timeline.getKeyFrames().add(keyFrame); // add blinking key frame to timeline
    	timeline.play();
    }




    /*******************************************
     * Event Handler for handling Mouse Clicks *
     *******************************************/


    private class MouseClickHandler implements EventHandler<MouseEvent> {
        @Override
        public void handle(MouseEvent mouseEvent) {
            double mousePressedX = mouseEvent.getX();
            double mousePressedY = mouseEvent.getY();

            int targetY = roundToLowestY(mousePressedY);

            // If mouse click is below the lowest Y, set cursor to last Text object
            if (targetY > lowerBoundY) {
                if (buffer.currText() == null) {
                    if (buffer.nextText() != null) {
                        buffer.nextCurr();
                    } else {
                        return;
                    }
                }

                while (buffer.nextText() != null) {
                    buffer.nextCurr();
                }

                Text text = buffer.currText();
                updateCursor(text.getX() + text.getLayoutBounds().getWidth(), text.getY());
                setCursor(cursorX, cursorY);
            } else {
                if (cursorY < targetY) {
                    if (buffer.currText() == null) {
                        if (buffer.nextText() != null) {
                            buffer.nextCurr();
                        } else {
                            return;
                        }
                    }
                    while (buffer.currText() != null && buffer.currText().getY() < targetY) {
                        buffer.nextCurr();
                    }
                } else if (cursorY > targetY) {
                    while (buffer.currText() != null && buffer.currText().getY() > targetY) {
                        buffer.prevCurr();
                    }
                }

                if (buffer.currText() == null) {
                    if (buffer.nextText() != null) {
                        buffer.nextCurr();
                    } else {
                        return;
                    }
                }
                if (buffer.currText().getX() > mousePressedX) {
                    while (buffer.currText() != null && buffer.currText().getX() > mousePressedX
                            && buffer.currText().getY() == targetY) {
                        buffer.prevCurr();
                    }
                } else if (buffer.currText().getX() < mousePressedX) {
                    while (buffer.nextText() != null && buffer.nextText().getX() < mousePressedX
                            && buffer.nextText().getY() == targetY) {
                        buffer.nextCurr();
                    }
                }
                setCursorToClosest(mousePressedX);
            }
        }


        private int roundToLowestY(double Y) {
            return textHeight * (int) Math.floor(Y / textHeight);
        }
    }


    @Override
    public void start(Stage stage) {
        root = new Group();
        Scene scene = new Scene(root, STARTING_WINDOW_WIDTH, STARTING_WINDOW_HEIGHT);

        // Resizing Window
        // Set the image view and listeners
        final Image image = new Image(new File("image.jpg").toURI().toString());
        final ImageView imageView = new ImageView(image);
        imageView.setFitHeight(STARTING_WINDOW_HEIGHT);
        imageView.setFitWidth(STARTING_WINDOW_WIDTH - 2 * MARGIN);
        imageView.setX(STARTING_X);
        imageView.setY(STARTING_Y);
        root.getChildren().add(imageView);

        // Add listeners to detect when window size changes
        scene.widthProperty().addListener(new ChangeListener<Number>() {
            @Override public void changed(
                ObservableValue<? extends Number> observableValue,
                Number oldScreenWidth,
                Number newScreenWidth) {
                    windowWidth = newScreenWidth.intValue();
                    int newImageWidth = windowWidth - 2 * MARGIN;
                    imageView.setFitWidth(newImageWidth);
                    reformatFromBeginning(); // only need to reformat when width is adjusted
                }
        });
        scene.heightProperty().addListener(new ChangeListener<Number>() {
            @Override public void changed(
                ObservableValue<? extends Number> observableValue,
                Number oldScreenHeight,
                Number newScreenHeight) {
                    windowHeight = newScreenHeight.intValue();
                    int newImageHeight = windowHeight;
                    imageView.setFitHeight(newImageHeight);
                }
        });

        // Add blinking cursor to screen
        root.getChildren().add(cursor);
        makeCursorBlink();

        // Add mouse click
        scene.setOnMouseClicked(new MouseClickHandler());

		// Once KeyEventHandler object is instantiated, it calls on the handle method
		// to handle the KeyEvent every time a key is pressed/typed
		EventHandler<KeyEvent> keyEventHandler = new KeyEventHandler();
		scene.setOnKeyTyped(keyEventHandler);
		scene.setOnKeyPressed(keyEventHandler);

		// Read in File if it exists
        List<String> params = getParameters().getRaw();
        if (params.size()!= 1) {
            System.out.println("Expected Usage: Editor <filename>");
            System.exit(1);
        }
        String fileName = params.get(0);

        try {
            file = new File(fileName);
            if (!file.exists()) {
                file.createNewFile();
            } else {
                FileReader reader = new FileReader(fileName);
                BufferedReader bufferedReader = new BufferedReader(reader);

                int intRead = -1;
                while ((intRead = bufferedReader.read()) != -1) {
                    char charRead = (char) intRead;
                    if (charRead == '\n') {
                        add("\r");
                    } else {
                        add(Character.toString(charRead));
                    }
                }
                bufferedReader.close();
                buffer.currToSentinel();
                updateCursor(STARTING_X, STARTING_Y);
                setCursor(cursorX, cursorY);
                reformat();
            }
        } catch (FileNotFoundException e) {
            System.out.println("Unable to open file " + fileName);
            return;
        } catch (IOException e) {
            System.out.println("Unable to read file " + fileName);
            return;
        }


        stage.setTitle(fileName);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
