import java.io.*;
import java.net.*;
import java.time.Duration;
import java.util.ArrayList;

public class TV {

    private static final int PUERTO_MONITOR_A_TV=1702;
    private ArrayList<Cliente> listaClientesEnAtencionRecibida = new ArrayList<Cliente>();
    private int numeroPuertoPrimario;
    private static final long INTERVALO_ENVIO_MS = 5000; // Intervalo de envío en milisegundos (5 segundos en este ejemplo)



    public TV(String address){
        Socket serverSocket = null;
        ObjectInputStream entrada=null;
        while (true) {
            System.out.println("Conectando con el Monitor...");
            Socket monitorSocket = null;
            DataInputStream monitorInput=null;
            try {
                monitorSocket = new Socket(address,PUERTO_MONITOR_A_TV);
                System.out.println("Conexion exitosa con el Monitor");
                monitorInput = new DataInputStream(monitorSocket.getInputStream());
                //Lee el puerto del servidor primario del Monitor
                this.numeroPuertoPrimario = monitorInput.readInt();
                System.out.println("Puerto del servidor primario recibido: " + numeroPuertoPrimario);
            } catch (IOException e) {
                System.err.println("No se puede conectar con el monitor");
            }
            System.out.println("El numero del puerto es: " + numeroPuertoPrimario);


            //Establecer conexion con el servidor para recibir lista de clientesAtendidos

            try {
                serverSocket = new Socket(address, numeroPuertoPrimario);
                System.out.println("Conexión establecida con el servidor en el puerto: " + numeroPuertoPrimario);
                //Flujo de entrada desde el servidor
                entrada = new ObjectInputStream(serverSocket.getInputStream());
                System.out.println("Flujo de entrada creado correctamente.");
                // El servidor me manda lista de clientes ya atendidos cuando se agrega un nuevo cliente

                try {
                    listaClientesEnAtencionRecibida = (ArrayList<Cliente>) entrada.readObject();
                } catch (ClassNotFoundException e) {
                    System.out.println(e.getMessage());
                }
                System.out.println("Clientes recibidos: " + listaClientesEnAtencionRecibida);


                // Esperar el intervalo de tiempo antes de recibir los datos nuevamente
                try {
                    Thread.sleep(INTERVALO_ENVIO_MS);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

            } catch (IOException e) {
                System.err.println("No se puede conectar con el servidor");

            }

            try {
                if (entrada != null) entrada.close();
                if (serverSocket != null) serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        TV tv = new TV("127.0.0.1");
    }

}
