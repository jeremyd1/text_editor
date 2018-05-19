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


public class Editor extends Application {
        private Group root;
        private Scene scene;
        TextBuffer buffer = new TextBuffer(); // Fast DLL for storing text

        public final static int WINDOW_HEIGHT = 500;
        public final static int WINDOW_WIDTH = 500;

        @Override
        public void start(Stage stage) {
            root = new Group();
            scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);

            // Cursor



//            Text t1 = new Text(10,50, "h");
//            Text t2 = new Text(20, 50, "i");
//            root.getChildren().add(t2);
//            root.getChildren().add(t1);



            stage.setTitle("Jeremy's Editor");
            stage.setScene(scene);
            stage.show();
        }

        public static void main(String[] args) {
            launch(args);
        }

    }
