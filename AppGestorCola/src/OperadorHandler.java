import java.net.*;
import java.io.*;

public class OperadorHandler extends Thread {
    private Server server;
    private static final int PUERTO_OPERADOR = 1300;
    private ServerSocket operadorSocket;

    public OperadorHandler(Server server) {
        this.server = server;
    }

    @Override
    public void run() {
        try {
            try {
                operadorSocket = new ServerSocket(PUERTO_OPERADOR);
                System.out.println("OperadorHandler started");
                System.out.println("Waiting for operator...");
            } catch (IOException e) {
                System.err.println("Error al iniciar el socket del operador: " + e.getMessage());
            }



            while (true){
                //Acepta la conexion del operador
                Socket operadorCliente = operadorSocket.accept();
                System.out.println("Operador connected");
                System.out.println("Operador IP: " + operadorCliente.getInetAddress() + ", Puerto: " + operadorCliente.getPort());
                // Inicia un nuevo hilo para manejar la comunicación con este operador
                OperadorRequestHandler requestHandler = new OperadorRequestHandler(operadorCliente, server);
                requestHandler.start();
            }

        } catch (IOException e) {
            System.err.println("Error al aceptar la conexión del operador: " + e.getMessage());
        }
    }
}
