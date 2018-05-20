import javafx.scene.text.Text;


public class TextBufferTest {
    public static void main(String[] args) {
        TextBuffer buffer = new TextBuffer();
        buffer.add(new Text("hi"));
        buffer.add(new Text("bye"));
        buffer.add(new Text("cya"));
        buffer.prevCurr();
        buffer.add(new Text("hello"));
        buffer.remove();
        buffer.nextCurr();
        buffer.remove();
    }
}
