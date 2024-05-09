import java.net.ServerSocket;
import java.net.Socket;
import java.io.*;


public class Monitor {
    private int numeroPuertoServidorA;
    private int numeroPuertoServidorB;

    private boolean primaryAlive;
    private boolean secondaryAlive;

    private static final int PUERTO_TOTTEM1=1234;
    private static final int PUERTO_OPERADOR1=1300;
    private static final int PUERTO_TV1=1236;
    private static final int PUERTO_ADMIN1=1237;
    private static final int PUERTO_TOTTEM2=2234;
    private static final int PUERTO_OPERADOR2=2300;
    private static final int PUERTO_TV2=2236;
    private static final int PUERTO_ADMIN2=2237;

   // private static final int PUERTO_MONITOR=1500;

    private static final int PUERTO_MONITOR_A_TOTTEM=1700;
    private static final int PUERTO_MONITOR_A_OPERADOR=1701;
    private static final int PUERTO_MONITOR_A_TV=1702;
    private static final int PUERTO_MONITOR_A_ADMIN=1703;


    private int puertoServidorPrimario=-1;

    public Monitor(int numeroPuertoServidorA, int numeroPuertoServidorB) {
        this.numeroPuertoServidorA = numeroPuertoServidorA;
        this.numeroPuertoServidorB = numeroPuertoServidorB;
    }

    //Cada 30 segundos monitorea el estado de los servidores
    public void startMonitoring() {
        while (true) {
            // Inicia el hilo para escuchar conexiones de los Tottens
            Thread listenThread = new Thread(this::escucharConexionesTottem);
            listenThread.start();

            Thread listenThread2 = new Thread(this::escucharConexionesEstadistica);
            listenThread2.start();

            Thread listenThread3 = new Thread(this::escucharConexionesTV);
            listenThread3.start();

            System.out.println("Monitoreando estado servidores... ");
            checkServers();
            try {
                // Dormir durante  segundos antes de la próxima verificación
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }



    private void escucharConexionesTV(){
        try (ServerSocket serverSocket = new ServerSocket(PUERTO_MONITOR_A_TV)) {
            System.out.println("Esperando conexión del TV...");
            while (true) {
                Socket socket = serverSocket.accept(); // Espera a que se conecte un Tottem
                System.out.println("TV conectada desde " + socket.getInetAddress() + ":" + socket.getPort());

                // Envía el nuevo puerto del servidor primario al Tottem
                if (puertoServidorPrimario!=-1){
                    try (DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream())) {
                        if (puertoServidorPrimario==numeroPuertoServidorA){
                            outputStream.writeInt(PUERTO_TV1);
                            System.out.println("Puerto del servidor primario enviado a TV " + PUERTO_TV1);
                        }
                        else if (puertoServidorPrimario==numeroPuertoServidorB){
                            outputStream.writeInt(PUERTO_TV2);
                            System.out.println("Puerto del servidor primario enviado a TV: " + PUERTO_TV2);

                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    //Esto capaz sacar
                    socket.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void escucharConexionesEstadistica(){
        try (ServerSocket serverSocket = new ServerSocket(PUERTO_MONITOR_A_ADMIN)) {
            System.out.println("Esperando conexión del admin...");
            while (true) {
                Socket socket = serverSocket.accept(); // Espera a que se conecte un Tottem
                System.out.println("Admin conectado desde " + socket.getInetAddress() + ":" + socket.getPort());

                // Envía el nuevo puerto del servidor primario al Tottem
                if (puertoServidorPrimario!=-1){
                    try (DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream())) {
                        if (puertoServidorPrimario==numeroPuertoServidorA){
                            outputStream.writeInt(PUERTO_ADMIN1);
                            System.out.println("Puerto del servidor primario enviado al admin: " + PUERTO_ADMIN1);
                        }
                        else if (puertoServidorPrimario==numeroPuertoServidorB){
                            outputStream.writeInt(PUERTO_ADMIN2);
                            System.out.println("Puerto del servidor primario enviado al admin: " + PUERTO_ADMIN2);

                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    //Esto capaz sacar
                    socket.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void escucharConexionesTottem() {
        try (ServerSocket serverSocket = new ServerSocket(PUERTO_MONITOR_A_TOTTEM)) {
            System.out.println("Esperando conexión del Tottem...");
            while (true) {
                Socket socket = serverSocket.accept(); // Espera a que se conecte un Tottem
                System.out.println("Cliente (Tottem) conectado desde " + socket.getInetAddress() + ":" + socket.getPort());

                // Envía el nuevo puerto del servidor primario al Tottem
                if (puertoServidorPrimario!=-1){
                    try (DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream())) {
                        if (puertoServidorPrimario==numeroPuertoServidorA){
                            outputStream.writeInt(PUERTO_TOTTEM1);
                            System.out.println("Puerto del servidor primario enviado al Tottem: " + PUERTO_TOTTEM1);
                        }
                        else if (puertoServidorPrimario==numeroPuertoServidorB){
                            outputStream.writeInt(PUERTO_TOTTEM2);
                            System.out.println("Puerto del servidor primario enviado al Tottem: " + PUERTO_TOTTEM2);

                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    //Esto capaz sacar
                    socket.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void checkServers() {
        this.primaryAlive = checkServer( numeroPuertoServidorA); //true si hay conexion, false si no hay conexion
        this.secondaryAlive = checkServer( numeroPuertoServidorB); //true si hay conexion, false si no hay conexion

        if (primaryAlive && secondaryAlive) {
            System.out.println("Ambos servers estan encendidos.");
            sendStatusToServer(numeroPuertoServidorA, 3);
            sendStatusToServer(numeroPuertoServidorB, 3);
        } else if (primaryAlive && !secondaryAlive) {
            System.out.println("Server A encendido. Server B apagado.");
            //Le avisa al server A que se vuelva primario
            sendStatusToServer(numeroPuertoServidorA, 1);
            //Le avisa al server B que es secundario
            sendStatusToServer(numeroPuertoServidorB, 0);
            //Setea el atributo de la clase Monitor puertoServidorActual
            setPuertoServidorPrimario(numeroPuertoServidorA);

        } else if (secondaryAlive && !primaryAlive) {
            System.out.println("Server B encendido. Server A apagado.");
            //Le avisa al server B que se vuelva primario
            sendStatusToServer(numeroPuertoServidorB, 1);
            //Le avisa al server A que es secundario
            sendStatusToServer(numeroPuertoServidorA, 0);
            //Setea el atributo de la clase Monitor puertoServidorActual
            setPuertoServidorPrimario(numeroPuertoServidorB);
        } else {
            System.out.println("Both servers are down.");
        }
    }


    private boolean checkServer( int serverPort) {
        try (Socket socket = new Socket("127.0.0.1",serverPort)) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    //Le envia a cada servidor un mensaje para que se vuelvan primario/secundario.
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


    public void setPuertoServidorPrimario(int puertoServidorPrimario) {
        this.puertoServidorPrimario = puertoServidorPrimario;
    }

    public static void main(String[] args) {
        int numeroPuertoServidorA = 1500;
        int numeroPuertoServidorB = 2500;

        Monitor monitor = new Monitor(numeroPuertoServidorA, numeroPuertoServidorB);
        monitor.startMonitoring();
    }


}
