package leverage.client.components;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import leverage.Kernel;
import java.util.TimerTask;

import static leverage.Kernel.APPLICATION_ICON;

public class Promotion extends TimerTask {
    private final Kernel kernel;                          // Consola del Sistema
    private int timer = 1;

    public Promotion(Kernel kernel, int timer) {
        this.kernel = kernel;
        this.timer = timer;
    }

    @Override
    public void run() {
        kernel.getConsole().print("Mostrando Promocion");
        showAlert(Alert.AlertType.INFORMATION, "Publicidad", "Done Tarjeta Nauta");

        try {
            Thread.sleep(1000*timer);
            run();
        } catch (InterruptedException ex) {
            kernel.getConsole().print("Publicidad interrumpida!");
        }
    }

    public int showAlert(Alert.AlertType type, String title, String content) {
        Alert a = new Alert(type);
        ((Stage) a.getDialogPane().getScene().getWindow()).getIcons().add(APPLICATION_ICON);
        a.setTitle(title);
        a.setHeaderText(title);
        a.setContentText(content);
        a.showAndWait();
        if (type == Alert.AlertType.CONFIRMATION) {
            if (a.getResult() == ButtonType.OK) {
                return 1;
            }
            return 0;
        }
        return -1;
    }

}
