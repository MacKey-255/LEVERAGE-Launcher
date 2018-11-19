package leverage.exceptions;

import leverage.Kernel;
import leverage.util.Urls;
import leverage.util.Utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CheatsDetectedException extends Exception {
    public CheatsDetectedException(String message, boolean cheat) {
        super(message);
        if(cheat) {
            // Avisar al Servidor que el Cliente contiene Parches
            String url = Urls.cheatsWarning, r = null;
            Map<String, String> params = new HashMap<>();
            params.put("Access-Token", Kernel.getAuthenticationStatic().getSelectedUser().getAccessToken());
            params.put("Client-Token", Kernel.getAuthenticationStatic().getSelectedUser().getAccessToken());
            try {
                r = Utils.sendPost(url, null, params);
                if (!"OK".equals(r))
                    message = "Usted ha sido Baneado del Servidor!";
            } catch (IOException ex) {
                System.out.println(ex.getMessage());
            }
            System.out.println(r);
        }
        System.out.println(message);
    }
}