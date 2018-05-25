import javafx.scene.text.Text;

/**
 * TextBuffer class is a Fast DLL used to store the text within the Editor
 */
public class TextBuffer {
    private Node sentinel;
    private Node curr; // stores pointer to the current node you're at
    private Node traverser; // pointer used to traverse the buffer
    private Node temp;

    public TextBuffer() {
        sentinel = new Node();
        curr = sentinel;
        traverser = curr;
        temp = null;
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
        traverser = curr;
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
            traverser = curr;
        }
    }

    /** Moves curr pointer to next node */
    public void nextCurr() {
        curr = curr.next;
        traverser = curr;
    }

    /** Moves curr pointer to prev node */
    public void prevCurr() {
        curr = curr.prev;
        traverser = curr;
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

    public void currToSentinel() {
        temp = curr;
        curr = sentinel;
        traverser = curr;
    }

    public void resetCurr() {
        curr = temp;
        temp = null;
        traverser = curr;

    }

    /** Returns the Text object at current node */
    public Text currText() {
        return curr.data;
    }


    /***************************************************************
     * The following methods are all based on the traverser pointer*
     **************************************************************/


    /** Returns whether a node has a next node based on traverser pointer */
    public boolean hasNextTrav() {
        return traverser.next != sentinel;
    }

    /** Returns whether a node has a prev node based on traverser pointer */
    public boolean hasPrevTrav() {
        return traverser.prev != sentinel;
    }

    /**
     * Moves traverser pointer to next node and returns its Text obj
     * Only call this method after calling hasNext()
     */
    public Text nextTrav() {
        traverser = traverser.next;
        return traverser.data;
    }

    /**
     * Moves traverser pointer to prev node and returns its Text obj
     * Only call this method after calling hasPrev()
     */
    public Text prevTrav() {
        traverser = traverser.prev;
        return traverser.data;
    }

    public void resetTrav() {
        traverser = curr;
    }

    /** Nested Node Class used as links in TextBuffer */
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
