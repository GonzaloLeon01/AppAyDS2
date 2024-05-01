import java.net.Socket;
import java.io.*;


public class Monitor {
    private int numeroPuertoPrimario;
    private int numeroPuertoSecundario;
    private boolean primaryAlive;
    private boolean secondaryAlive;
    public Monitor(int numeroPuertoPrimario, int numeroPuertoSecundario) {
        this.numeroPuertoPrimario = numeroPuertoPrimario;
        this.numeroPuertoSecundario = numeroPuertoSecundario;
    }

    //Cada 30 segundos monitorea el estado de los servidores
    public void startMonitoring() {
        while (true) {
            checkServers();
            try {
                // Dormir durante 30 segundos antes de la próxima verificación
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    private void checkServers() {
        this.primaryAlive = checkServer( numeroPuertoPrimario); //true si hay conexion, false si no hay conexion
        this.secondaryAlive = checkServer( numeroPuertoSecundario); //true si hay conexion, false si no hay conexion

        if (primaryAlive && secondaryAlive) {
            System.out.println("Ambos servers estan encendidos.");
        } else if (primaryAlive) {
            System.out.println("Server primario encendido. Server secundario apagado.");
            sendStatusToServer(numeroPuertoPrimario, 1);
            //le avisamos que es el principal al servidor Primario
        } else if (secondaryAlive) {
            System.out.println("Server secundario encendido. Server primario apagado.");
            sendStatusToServer(numeroPuertoSecundario, 1);
            //Le avisamos que es el principal al servidor Secundario
        } else {
            System.out.println("Both servers are down.");
            //aca q pingo se hace
        }
    }


    private boolean checkServer( int serverPort) {
        try (Socket socket = new Socket("127.0.0.1",serverPort)) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private void sendStatusToServer(int serverPort, int status) {
        Socket socket=null;
        try {
            socket = new Socket("127.0.0.1", serverPort);
            DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
            outputStream.writeInt(status);
            System.out.println("Sent status to server: " + status);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public static void main(String[] args) {
        int numeroPuertoPrimario = 1500; // Cambiar al puerto del servidor principal
        int numeroPuertoSecundario = 2500; // Cambiar al puerto del servidor secundario

        Monitor monitor = new Monitor(numeroPuertoPrimario, numeroPuertoSecundario);
        monitor.startMonitoring();
    }
}
