/**
 * Author: Jeremy Dong
 * Date: May 16, 2018
 * Project: Text Editor
 */

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class Editor extends Application {
        @Override
        public void start(Stage stage) {
            Group root = new Group();
            Scene scene = new Scene(root, 800,500);
            stage.setTitle("Jeremy's Editor");
            stage.setScene(scene);
            stage.show();
        }

        public static void main(String[] args) {
            launch(args);
        }

    }
