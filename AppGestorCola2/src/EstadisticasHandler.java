import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.ArrayList;

public class EstadisticasHandler extends Thread {
    private Server server;
    private static final int PUERTO_ADMIN = 2237;
    private static final long INTERVALO_ENVIO_MS = 5000; // Intervalo de envío en milisegundos (5 segundos en este ejemplo)


    public EstadisticasHandler(Server server) {
        this.server = server;
    }

    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(PUERTO_ADMIN);
            System.out.println("Servidor esperando conexiones...");

            while (true) {
                Socket cliente = serverSocket.accept();
                System.out.println("Admin connected");

                while (true) {
                    if (cliente.isClosed()){
                        System.out.println("El cliente se ha desconectado.");
                        break;
                    }

                    ArrayList<Cliente> listaClientes = server.getClientesAtendidos();

                    try {
                        ObjectOutputStream outputStream = new ObjectOutputStream(cliente.getOutputStream());
                        outputStream.writeObject(listaClientes);
                        outputStream.flush(); // Forzar el envío de datos
                    } catch (IOException e) {
                        System.out.println("Error al enviar la lista de clientesAtendidos a Estadisticas: " + e.getMessage());
                        cliente.close();
                        break;
                    }

                    // Esperar el intervalo de tiempo antes de enviar los datos nuevamente
                    try {
                        Thread.sleep(INTERVALO_ENVIO_MS);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

