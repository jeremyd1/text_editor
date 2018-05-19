import javafx.scene.text.Text;

/**
 * TextBuffer class is a Fast DLL used to store the text within the Editor
 */
public class TextBuffer {
    Node sentinel;
    Node curr; // stores pointer to the current node you're at
    int size;

    public TextBuffer() {
        sentinel = new Node();
        curr = sentinel;
        size = 0;
    }

    /**
     * Adds a new node containing the text to curr.next
     * Adding can only occur after curr
     * curr points to text that was just typed
     */
    public void add(Text text) {

        size++;
    }

    /**
     * Removes node at curr.prev
     * Removing can only occur before curr
     */
    public void remove() {

        size--;
    }

    public void currNext() {
        curr = curr.next;
    }

    public void currPrev() {
        curr = curr.prev;
    }

    public int size() {
        return size;
    }






    private class Node {
        Node prev;
        Node next;
        Text data;

        /**
         * Empty constructor for sentinel nodes
         */
        public Node() {
            prev = this;
            next = this;
            data = null;
        }

        /**
         * Constructor for text nodes
         */
        public Node(Text text) {
            data = text;
        }
    }

}
