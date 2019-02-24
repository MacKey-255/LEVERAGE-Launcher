package leverage.client;

import javafx.scene.control.Alert;
import leverage.Kernel;
import java.util.TimerTask;

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
        kernel.showAlert(Alert.AlertType.INFORMATION, "Publicidad", "Done Tarjeta Nauta");

        try {
            Thread.sleep(1000*timer);
            run();
        } catch (InterruptedException ex) {
            kernel.getConsole().print("Publicidad interrumpida!");
        }
    }

}
