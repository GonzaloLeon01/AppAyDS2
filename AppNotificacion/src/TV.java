import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class TV {

    private Socket socket = null;
    private ObjectInputStream entrada=null;

    private static final int PUERTO_TV=1236;
    private ArrayList<Cliente> listaClientesEnAtencionRecibida = new ArrayList<Cliente>();
    private static final long INTERVALO_ENVIO_MS = 5000; // Intervalo de envío en milisegundos (5 segundos en este ejemplo)



    public TV(String address, int port) {
        //establece una conexion
        try{
            socket = new Socket(address,port);
            System.out.println("Conexión establecida correctamente.");

            // Ciclo para recibir múltiples envíos del servidor
            while (true) {
                try {
                    //Flujo de entrada desde el servidor
                    entrada = new ObjectInputStream(socket.getInputStream());
                    System.out.println("Flujo de entrada creado correctamente.");
                    // El servidor me manda lista de clientes en atencion
                    listaClientesEnAtencionRecibida = (ArrayList<Cliente>) entrada.readObject();
                    System.out.println("Clientes recibidos: " + listaClientesEnAtencionRecibida);



                    // Esperar el intervalo de tiempo antes de recibir los datos nuevamente
                    try {
                        Thread.sleep(INTERVALO_ENVIO_MS);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }

                } catch (ClassNotFoundException e) {
                    System.err.println("Error: No se pudo encontrar la clase Cliente.");
                    e.printStackTrace();
                }
            }


        }
        catch (UnknownHostException u){
            System.err.println("Error: El host es desconocido.");
            return;
        }
        catch (IOException i){
            System.err.println("Error de entrada/salida al conectar con el servidor.");
            return;
        } finally {
            // Cerrar la conexión
            try {
                if (entrada != null)
                    entrada.close();
                if (socket != null)
                    socket.close();
            } catch (IOException e) {
                System.err.println("Error al cerrar la conexión.");
                e.printStackTrace();
            }
        }



    }



    public static void main(String[] args) {
        TV tv = new TV("127.0.0.1",PUERTO_TV);
    }
}
