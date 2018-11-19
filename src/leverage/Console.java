package leverage;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

public class Console {
    private final DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd -- HH:mm:ss");
    private boolean enabled = true;
    private PrintWriter writer;
    private final File log;

    public Console() {
        System.out.println("Iniciando Consola del Launcher...");
        File[] logFiles = Kernel.APPLICATION_LOGS.listFiles();
        if (logFiles != null && logFiles.length > 0) {
            final int KEEP_OLD_LOGS = 10;
            Arrays.sort(logFiles, Collections.reverseOrder());
            int count = 0;
            for (File f : logFiles) {
                String name = f.getName();
                if (name.startsWith("leverage-unclosed")) {
                    if (f.delete()) {
                        System.out.println("Se ha borrado con exito el archivo desbloqueado de Logs: " + name);
                    } else {
                        System.out.println("Ha fallado la eliminacion del archivo desbloqueado de Logs: " + name);
                    }
                } else if (name.startsWith("leverage")) {
                    if (count == KEEP_OLD_LOGS - 1) {
                        if (f.delete()) {
                            System.out.println("Se ha borrado con exito el antiguo archivo de Logs:  " + name);
                        } else {
                            System.out.println("Fallido la Eliminacion del Archivo de Logs: " + name);
                        }
                    } else {
                        count++;
                    }
                }
            }
        }
        this.log = new File(Kernel.APPLICATION_LOGS, "leverage-unclosed-" + System.currentTimeMillis() + ".log");
        try {
            this.writer = new PrintWriter(this.log){
                @Override
                public void println(boolean x) {
                    System.out.println(x);
                    super.println(x);
                }

                @Override
                public void println(char x) {
                    System.out.println(x);
                    super.println(x);
                }

                @Override
                public void println(int x) {
                    System.out.println(x);
                    super.println(x);
                }

                @Override
                public void println(long x) {
                    System.out.println(x);
                    super.println(x);
                }

                @Override
                public void println(float x) {
                    System.out.println(x);
                    super.println(x);
                }

                @Override
                public void println(double x) {
                    System.out.println(x);
                    super.println(x);
                }

                @Override
                public void println(char[] x) {
                    System.out.println(x);
                    super.println(x);
                }

                @Override
                public void println(String x) {
                    System.out.println(x);
                    super.println(x);
                }

                @Override
                public void println(Object x) {
                    System.out.println(x);
                    super.println(x);
                }

                @Override
                public void println() {
                    super.println();
                    super.flush();
                }
            };
        } catch (IOException ex) {
            this.enabled = false;
        }
    }

    /**
     * Prints something to the output channel
     * @param info The Object to be printed
     */
    public final void print(Object info) {
        if (this.enabled) {
            this.writer.println('[' + this.dateFormat.format(new Date()) + "] " + info);
        }
    }

    /**
     * Gets the console writer
     * @return The console writer
     */
    public PrintWriter getWriter() {
        return this.writer;
    }

    /**
     * Closes the log file
     */
    public final void close() {
        if (this.enabled) {
            this.writer.close();
            File target = new File(Kernel.APPLICATION_LOGS, this.log.getName().replace("-unclosed", ""));
            this.log.renameTo(target);
        }
    }
}
