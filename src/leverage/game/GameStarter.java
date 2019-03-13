package leverage.game;

import leverage.proxy.URLHandler;

import java.lang.reflect.Method;
import java.net.URL;

public class GameStarter {

    public static void main(String[] args) {
        System.out.println("GameStarter lanzandose con " + args.length + " argumentos.");
        URL.setURLStreamHandlerFactory(new URLHandler());
        System.out.println("Cargando URL Handler.");
        if (args.length == 0) {
            System.err.println("Numero invalido en los Argumentos.");
            System.exit(-1);
        }
        String mainClass = args[0];
        String[] gameArgs = new String[args.length - 1];
        System.arraycopy(args, 1, gameArgs, 0, args.length - 1);
        try {
            Class<?> gameClass = Class.forName(mainClass);
            Method method = gameClass.getMethod("main", String[].class);
            method.invoke(null, (Object) gameArgs);
        } catch (Exception ex) {
            System.out.println("Ha fallado el Inicio del Juego.");
            ex.printStackTrace();
            System.exit(-1);
        }
    }
}