package leverage.proxy;

import leverage.auth.user.User;
import org.json.JSONObject;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.Socket;

public class SocketConnect {

    private Socket socket;
    private int port = 9876;
    private ObjectOutputStream oos = null;
    private ObjectInputStream ois = null;
    private User user;

    public SocketConnect(User user) throws ConnectException {
        try {
            this.connect();
        } catch (IOException e) {
            e.printStackTrace();
            throw new ConnectException("No se pudo conectar con el Servidor");
        }
    }
    public SocketConnect(User user, String data) throws ConnectException, ClassNotFoundException {
        try {
            this.connect();
        } catch (IOException e) {
            e.printStackTrace();
            throw new ConnectException("No se pudo conectar con el Servidor");
        }
        try {
            this.sendData(data);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error enviando los datos");
        }
        try {
            this.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
            throw new ConnectException("No se pudo desconectar del Servidor");
        }
    }

    public void connect() throws IOException {
        //establish socket connection to server
        socket = new Socket("127.0.0.1", port);     //Testing
        //socket = new Socket(Urls.leverageIP, port);
    }

    public void disconnect() throws IOException {
        socket.close();
    }

    public void sendData(String data) throws IOException, ClassNotFoundException {
        //write to socket using ObjectOutputStream
        oos = new ObjectOutputStream(socket.getOutputStream());
        System.out.println("Enviando Peticion al Servidor.");
        oos.writeObject(this.generateData(data).toString());

        //read the server response message
        ois = new ObjectInputStream(socket.getInputStream());
        String message = (String) ois.readObject();
        System.out.println("Message: " + message);

        if(!message.equals("OK"))
            throw new ConnectException("No se ha podido realizar la accion");

        //close resources
        oos.close();
        ois.close();

        this.wait(100);
    }

    public JSONObject generateData(String data) {
        JSONObject array = new JSONObject();
        array.put("user", user.getUsername().replace("leverage://", ""));
        array.put("request", data);

        return array;
    }

    public void wait(int time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }
    }
}
