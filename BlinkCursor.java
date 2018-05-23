import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/** Event Handler for handling blinking cursor */
public class BlinkCursor implements EventHandler<ActionEvent> {
    private int currentColorIndex;
    private Color[] boxColors;
    private Rectangle cursor;

    public BlinkCursor(Rectangle r) {
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