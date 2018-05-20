import javafx.scene.text.Text;

/**
 * TextBuffer class is a Fast DLL used to store the text within the Editor
 */
public class TextBuffer {
    private Node sentinel;
    private Node curr; // stores pointer to the current node you're at
    private int size;

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
        Node toAdd = new Node(text);
        if (curr.next == sentinel) {
            toAdd.prev = curr;
            toAdd.next = sentinel;
            curr.next = toAdd;
            sentinel.prev = toAdd;
        } else {
            Node next = curr.next;
            toAdd.prev = curr;
            toAdd.next = next;
            next.prev = toAdd;
            curr.next = toAdd;
        }
        curr = curr.next;
        size++;
    }

    /**
     * Removes node at curr.prev
     * Removing can only occur before curr
     */
    public void remove() {
        if (curr != sentinel) {
            if (curr.next == sentinel) {
                sentinel.prev = curr.prev;
                curr.prev.next = sentinel;
                curr = curr.prev;
            } else {
                Node prev = curr.prev;
                Node next = curr.next;
                prev.next = next;
                next.prev = prev;
                curr = prev;
            }
            size--;
        }
    }

    /** Moves curr pointer to next node */
    public void nextCurr() {
        curr = curr.next;
    }

    /** Moves curr pointer to prev node */
    public void prevCurr() {
        curr = curr.prev;
    }

    /** Returns the Text object next to curr */
    public Text nextText() {
        if (curr.next == sentinel) {
            return null;
        }
        return curr.next.data;
    }

    /** Returns the Text object prev to curr */
    public Text prevText() {
        if (curr.prev == sentinel) {
            return null;
        }
        return curr.prev.data;
    }

    public int size() {
        return size;
    }


    private static class Node {
        Node prev;
        Node next;
        Text data;

        /**
         * Empty constructor for sentinel nodes
         */
        Node() {
            prev = this;
            next = this;
            data = null;
        }

        /**
         * Constructor for text nodes
         */
        Node(Text text) {
            data = text;
        }
    }

}
