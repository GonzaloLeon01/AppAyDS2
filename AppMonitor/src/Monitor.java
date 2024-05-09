import java.net.ServerSocket;
import java.net.Socket;
import java.io.*;


public class Monitor {
    private int numeroPuertoPrimario;
    private int numeroPuertoSecundario;
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

    private static final int PUERTO_MONITOR=1500;

    private static final int PUERTO_MONITOR_A_TOTTEM=1700;
    private static final int PUERTO_MONITOR_A_OPERADOR=1701;
    private static final int PUERTO_MONITOR_A_TV=1702;
    private static final int PUERTO_MONITOR_A_ADMIN=1703;


    private int puertoServidorActual;

    public Monitor(int numeroPuertoPrimario, int numeroPuertoSecundario) {
        this.numeroPuertoPrimario = numeroPuertoPrimario;
        this.numeroPuertoSecundario = numeroPuertoSecundario;
    }

    //Cada 30 segundos monitorea el estado de los servidores
    public void startMonitoring() {
        while (true) {
            // Inicia el hilo para escuchar conexiones de los Tottens
            Thread listenThread = new Thread(this::escucharConexionesTottem);
            listenThread.start();

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


    private void checkServers() {
        this.primaryAlive = checkServer( numeroPuertoPrimario); //true si hay conexion, false si no hay conexion
        this.secondaryAlive = checkServer( numeroPuertoSecundario); //true si hay conexion, false si no hay conexion

        if (primaryAlive && secondaryAlive) {
            System.out.println("Ambos servers estan encendidos.");
            sendStatusToServer(numeroPuertoPrimario, 3);
            sendStatusToServer(numeroPuertoSecundario, 3);
        } else if (primaryAlive) {
            System.out.println("Server primario encendido. Server secundario apagado.");
            sendStatusToServer(numeroPuertoPrimario, 1);
            sendStatusToServer(numeroPuertoSecundario, 0);
            avisaATodos(primaryAlive,secondaryAlive);
            setPuertoServidorActual(numeroPuertoPrimario);
            //le avisamos que es el principal al servidor Primario
        } else if (secondaryAlive) {
            System.out.println("Server secundario encendido. Server primario apagado.");
            sendStatusToServer(numeroPuertoSecundario, 1);
            sendStatusToServer(numeroPuertoPrimario, 0);
            avisaATodos(primaryAlive,secondaryAlive);
            setPuertoServidorActual(numeroPuertoSecundario);
            //Le avisamos que es el principal al servidor Secundario
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

    private void avisaATodos(boolean primaryAlive,boolean secondaryAlive){
        if (primaryAlive){
            System.out.println("Avisa a tottem de que se tiene que conectar al primario");
            //enviarNuevoPuertoTottem(PUERTO_TOTTEM1);
            //enviarNuevoPuertoOperador(PUERTO_OPERADOR1);
            //enviarNuevoPuertoAdmin(PUERTO_ADMIN1);
            //enviarNuevoPuertoTV(PUERTO_TV1);
        } else if (secondaryAlive) {
            System.out.println("Avisa a tottem de que se tiene que conectar al secundario");
            enviarNuevoPuertoTottem(PUERTO_TOTTEM2);
            //enviarNuevoPuertoOperador(PUERTO_OPERADOR2);
            //enviarNuevoPuertoAdmin(PUERTO_ADMIN2);
            //enviarNuevoPuertoTV(PUERTO_TV2);
        }
    }

    private void escucharConexionesTottem() {
        try (ServerSocket serverSocket = new ServerSocket(PUERTO_MONITOR_A_TOTTEM)) {
            System.out.println("Esperando conexión del Tottem...");
            while (true) {
                Socket socket = serverSocket.accept(); // Espera a que se conecte un Tottem
                System.out.println("Cliente (Tottem) conectado desde " + socket.getInetAddress() + ":" + socket.getPort());

                // Envía el nuevo puerto del servidor primario al Tottem
                try (DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream())) {
                    if (puertoServidorActual==PUERTO_MONITOR){
                        outputStream.writeInt(PUERTO_TOTTEM1);
                        System.out.println("Puerto del servidor primario enviado al Tottem: " + PUERTO_TOTTEM1);
                    }
                    else{
                        outputStream.writeInt(PUERTO_TOTTEM2);
                        System.out.println("Puerto del servidor primario enviado al Tottem: " + PUERTO_TOTTEM2);

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void enviarNuevoPuertoTottem(int puertoNuevoServidorPrimario) {
        ServerSocket serverSocket=null;
        Socket socket = null;
        DataOutputStream outputStream=null;
        try {
            // Abre el socket del servidor para escuchar conexiones del Tottem
            serverSocket = new ServerSocket(PUERTO_MONITOR_A_TOTTEM);
            System.out.println("Esperando conexión del Tottem...");
            // Acepta la conexión del Tottem
            socket = serverSocket.accept(); // Bloquea la ejecución hasta que se reciba una conexión
            System.out.println("Cliente (Tottem) conectado desde " + socket.getInetAddress() + ":" + socket.getPort());

            // Abre el flujo de salida para enviar el nuevo puerto al Tottem
            outputStream = new DataOutputStream(socket.getOutputStream());
            outputStream.writeInt(puertoNuevoServidorPrimario);
            System.out.println("Puerto del servidor primario enviado al Tottem: " + puertoNuevoServidorPrimario);

        } catch (IOException e) {
            //Manejar las excepciones de E/S
            System.err.println("Error de E/S al comunicarse con el Tottem:");
            e.printStackTrace();
        } finally {
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                 System.err.println("Error al cerrar el socket del servidor:");
                 e.printStackTrace();
                }
             }/*
            // Cierra los recursos
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    System.err.println("Error al cerrar el flujo de salida:");
                    e.printStackTrace();
                }
            }
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    System.err.println("Error al cerrar el socket:");
                    e.printStackTrace();
                }
            }
*/
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


/*

    private void enviarNuevoPuertoOperador(int puertoNuevoServidorPrimario) {
        try (ServerSocket serverSocket = new ServerSocket(puertoNuevoServidorPrimario)) {
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Cliente (Tottem) conectado desde " + socket.getInetAddress() + ":" + socket.getPort());

                // Envía el nuevo puerto del servidor primario al tottem
                try (DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream())) {
                    outputStream.writeInt(puertoNuevoServidorPrimario);
                    System.out.println("Puerto del servidor primario enviado al Tottem: " + puertoNuevoServidorPrimario);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void enviarNuevoPuertoAdmin(int puertoNuevoServidorPrimario) {
        try (ServerSocket serverSocket = new ServerSocket(puertoNuevoServidorPrimario)) {
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Cliente (Tottem) conectado desde " + socket.getInetAddress() + ":" + socket.getPort());

                // Envía el nuevo puerto del servidor primario al tottem
                try (DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream())) {
                    outputStream.writeInt(puertoNuevoServidorPrimario);
                    System.out.println("Puerto del servidor primario enviado al Tottem: " + puertoNuevoServidorPrimario);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void enviarNuevoPuertoTV(int puertoNuevoServidorPrimario) {
        try (ServerSocket serverSocket = new ServerSocket(puertoNuevoServidorPrimario)) {
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Cliente (Tottem) conectado desde " + socket.getInetAddress() + ":" + socket.getPort());

                // Envía el nuevo puerto del servidor primario al tottem
                try (DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream())) {
                    outputStream.writeInt(puertoNuevoServidorPrimario);
                    System.out.println("Puerto del servidor primario enviado al Tottem: " + puertoNuevoServidorPrimario);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
*/

    public static void main(String[] args) {
        int numeroPuertoPrimario = 1500; // Cambiar al puerto del servidor principal
        int numeroPuertoSecundario = 2500; // Cambiar al puerto del servidor secundario

        Monitor monitor = new Monitor(numeroPuertoPrimario, numeroPuertoSecundario);
        monitor.startMonitoring();
    }

    public void setPuertoServidorActual(int puertoServidorActual) {
        this.puertoServidorActual = puertoServidorActual;
    }
}
