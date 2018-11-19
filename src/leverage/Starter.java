package leverage;

import javafx.application.Application;
import javafx.stage.Stage;

/**
 * @author MacKey - Modificated
 * @author DarkLBP - Created
 *         website https://krothium.com
 */
public class Starter extends Application {

    public static void main(String[] args) {
        //Lanzar metodo Start
        launch(args);
    }


    /**
     * Loads the JavaFX environment
     * @param primaryStage The main stage
     */
    @Override
    public final void start(Stage primaryStage) {
        //Crear Kernel
        new Kernel(primaryStage, getHostServices());
    }
}
