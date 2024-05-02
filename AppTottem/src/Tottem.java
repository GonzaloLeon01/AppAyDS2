import java.io.*;
import java.net.*;

public class Tottem {

    private Socket serverSocket = null;
    private DataInputStream entrada=null;
    private DataOutputStream salida =null;

    private int numeroPuertoPrimario;

    private static final int PUERTO_TOTTEM=1234;
    private static final int PUERTO_MONITOR=1500;
    private static final int PUERTO_MONITOR_A_TOTTEM=1700;
    private int cantidadClientes;

    //Constructor para colocar ip y puerto
    /*public Tottem(String address) {
        //-------Conexion con Monitor------
        // Hilo para escuchar mensajes del monitor
        Thread monitorThread = new Thread(() -> {
            try {
                System.out.println("Conectando con monitor");
                Socket monitorSocket = new Socket(address, PUERTO_MONITOR_A_TOTTEM);
                System.out.println("Conexion exitosa con monitor");

                DataInputStream monitorInput = new DataInputStream(monitorSocket.getInputStream());

                while (true) {
                    // Lee el puerto del servidor primario del monitor
                    this.numeroPuertoPrimario = monitorInput.readInt(); // Lee el puerto del servidor primario
                    System.out.println("Puerto del servidor primario recibido: " + numeroPuertoPrimario);

                    // Espera un tiempo antes de volver a leer
                    Thread.sleep(10000); // 10 segundos
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        });
        monitorThread.start();

        // Hilo para comunicarse con el servidor
        Thread serverThread = new Thread(() -> {
            try {
                while (true) {
                    if (serverSocket == null || serverSocket.isClosed()) {
                        serverSocket = new Socket(address, numeroPuertoPrimario);
                        System.out.println("Conexión establecida con el servidor en el puerto: " + numeroPuertoPrimario);
                        //Abre flujo de entrada para recibir datos del servidor
                        entrada = new DataInputStream(serverSocket.getInputStream());
                        //Abre flujo de salida para enviar datos al servidor
                        salida = new DataOutputStream(serverSocket.getOutputStream());

                        //String para leer un mensaje de la entrada

                        String line = "";
                        //keep reading until "Over" is input
                        while (!line.equals("Over") ){
                            try {
                                DataInputStream input = new DataInputStream(System.in);
                                line = input.readLine();
                                salida.writeUTF(line); //mando dni al Servidor
                                //Leo del servidor
                                cantidadClientes = entrada.readInt();
                                System.out.println(cantidadClientes);

                            }
                            catch (IOException i){
                                System.out.println(i);
                            }
                        }

                    }

                    // Realiza las operaciones de comunicación con el servidor aquí
                    // Por ejemplo, enviar y recibir datos

                    Thread.sleep(1000); // Espera un segundo antes de volver a intentar la conexión
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        });
        serverThread.start();
    }

     */

    // Constructor para colocar ip y puerto
    public Tottem(String address) {
        try {


            while (true) {
                System.out.println("Conectando con el Monitor...");
                Socket monitorSocket = new Socket(address, PUERTO_MONITOR_A_TOTTEM);
                System.out.println("Conexión exitosa con el Monitor.");

                DataInputStream monitorInput = new DataInputStream(monitorSocket.getInputStream());
                System.out.println("VUELVE");
                // Lee el puerto del servidor primario del Monitor
                this.numeroPuertoPrimario = monitorInput.readInt();
                System.out.println("Puerto del servidor primario recibido: " + numeroPuertoPrimario);

                // Establece conexión con el servidor
                //if (serverSocket == null || serverSocket.isClosed()) {
                    System.out.println("El numero del puerto es: " + numeroPuertoPrimario);
                    serverSocket = new Socket(address, numeroPuertoPrimario);
                    System.out.println("Conexión establecida con el servidor en el puerto: " + numeroPuertoPrimario);

                    // Abre flujo de entrada para recibir datos del servidor
                    entrada = new DataInputStream(serverSocket.getInputStream());
                    // Abre flujo de salida para enviar datos al servidor
                    salida = new DataOutputStream(serverSocket.getOutputStream());

                    // Realiza las operaciones de comunicación con el servidor aquí
                    // Por ejemplo, enviar y recibir datos
                    //String para leer un mensaje de la entrada

                    String line = "";
                    //keep reading until "Over" is input
                    while (!line.equals("Over")) {
                        try {
                            DataInputStream input = new DataInputStream(System.in);
                            line = input.readLine();
                            salida.writeUTF(line); //mando dni al Servidor
                            //Leo del servidor
                            cantidadClientes = entrada.readInt();
                            System.out.println(cantidadClientes);

                        } catch (IOException i) {
                            System.out.println(i);
                            break;
                        }
                    }

                // Espera antes de volver a leer
                Thread.sleep(5000); // 5 segundos
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            // Cierre de recursos si es necesario
            try {
                if (entrada != null) entrada.close();
                if (salida != null) salida.close();
                if (serverSocket != null) serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public static void main(String[] args) {
        Tottem tottem = new Tottem("127.0.0.1");
    }
}
