import java.net.*;
import java.io.*;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
//para las colas


public class Server {

    private ServerSocket serverSocket = null;
    private Cola<Cliente> cola= new Cola<>();
    private ArrayList<Cliente> clientesEnAtencion= new ArrayList<>();
    private ArrayList<Cliente> clientesAtendidos= new ArrayList<>();
    private Cliente primerCliente;

    private static final int PUERTO_TOTTEM=1234;
    private static final int PUERTO_OPERADOR=1300;
    private static final int PUERTO_TV=1236;
    private static final int PUERTO_ADMIN=1237;
    private static final int PUERTO_MONITOR=1500;
    private static final int PUERTO_SERVIDOR=2500;
    private boolean esPrimario=false;     //devuelve 1 si es principal el Monitor o devuelve 0 si es secundario.


    public Server() {

        //Cada 30 segundos recibe mensaje del Monitor
        recibirMensajeMonitor();

        //Si el Monitor le dice que es primario entonces
        if (esPrimario) { //Si es principal
            iniciarServidorPrimario();
        } else { //es secundario
            System.out.println("Servidor secundario en espera...");

            // El servidor secundario está en espera hasta que reciba un mensaje del Monitor para volver a convertirse en primario
            while (!esPrimario) {
                try {
                    //recibirColaDelServidorPrincipal();
                    //Aqui escuchar servidor principal
                    Thread.sleep(1000); // Esperar 1 segundo antes de verificar nuevamente
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // Verificar si se ha convertido en primario
                if (esPrimario) {
                    System.out.println("El servidor secundario se ha convertido en principal.");
                    iniciarServidorPrimario(); // Llama al método para iniciar el servidor primario
                }
            }
        }
    }

    private void iniciarServidorPrimario(){
        try {
            serverSocket = new ServerSocket(PUERTO_TOTTEM);
            System.out.println("Server started");
            System.out.println("Waiting for a client...");

            TottemHandler tottemHandler = new TottemHandler(this);
            tottemHandler.start(); // Inicia el hilo para recibir datos del Tottem

            OperadorHandler operadorHandler = new OperadorHandler(this);
            operadorHandler.start(); // Inicia el hilo para manejar la comunicación con el operador

            EstadisticasHandler estadisticasHandler = new EstadisticasHandler(this);
            estadisticasHandler.start(); // Inicia el hilo para enviar datos a Estadisticas

            NotificacionHandler notificacionHandler = new NotificacionHandler(this);
            notificacionHandler.start(); // Inicia el hilo para enviar datos a Notificacion

            //enviarColaAServidorSecundario();

            Timer timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    System.out.println("Clientes en cola:" + cola.size());
                    System.out.println("Clientes atendidos " + clientesAtendidos);
                    System.out.println("Clientes en atencion " + clientesEnAtencion);
                    System.out.println("estado ="+esPrimario);
                }
            }, 0, 15000); // 5000 milisegundos = 5 segundos

            //Tengo que abrir un hilo para comunicarme con el servidor B para sincronizarlo.

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void procesarSolicitudTottem(Socket cliente){
        TottemRequestHandler requestHandler = new TottemRequestHandler(this,cliente);
        requestHandler.start();
    }


    //Implementa en un hilo aparte la recepcion de mensajes del monitor
    public void recibirMensajeMonitor() {
        Thread monitorThread = new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(PUERTO_MONITOR)) {
                System.out.println("Servidor de mensajes del monitor iniciado. Esperando conexión...");

                while (true) {
                    Socket socket = serverSocket.accept();
                    System.out.println("Cliente (Monitor) conectado desde " + socket.getInetAddress() + ":" + socket.getPort());

                    // Manejar el mensaje enviado por el monitor
                    handleMonitorMessage(socket);

                    // Cerrar el socket del cliente
                    socket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        monitorThread.start();
    }

    private void handleMonitorMessage(Socket socket) {
        try (DataInputStream inputStream = new DataInputStream(socket.getInputStream())) {
            // Leer el entero enviado por el monitor
            int mensaje = inputStream.readInt();
            System.out.println("Mensaje recibido del monitor: " + mensaje);

            // Actualizar el valor de esPrimario según el mensaje recibido
            if (mensaje == 1)
                esPrimario=true;
            else if (mensaje == 0)
                esPrimario=false;
            else if (mensaje == 3 && !esPrimario)
                esPrimario=false;
            else if (mensaje == 3 && esPrimario)
                esPrimario=true;
            //this.esPrimario = (mensaje == 1);
            System.out.println("Valor de esPrimario actualizado a: " + esPrimario);
            if (esPrimario)
                enviarColaAServidorSecundario();
            else
                recibirColaDelServidorPrincipal();
        } catch (EOFException e) {
            // Ignorar la excepción EOFException y continuar esperando conexiones del monitor
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // Método para enviar la cola actual al servidor secundario
    private void enviarColaAServidorSecundario() {
        Thread enviarColaThread = new Thread(() -> {
            while (true) {
                if (esPrimario) {
                    try {
                        try {
                            Thread.sleep(3000); // Esperar 5 segundos antes de intentar nuevamente
                            System.out.println("No puede conectarse al servidor secundario xd");
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                        }
                        System.out.println("TRATANDO DE ENVIAR COLA PRINCIPAL");
                        Socket socket = new Socket("127.0.0.1", 3100);
                        ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
                        outputStream.writeObject(cola); // Envía la cola actual al servidor secundario
                        System.out.println("XXXXxCola enviada al servidor secundario.XXXXXx");
                        outputStream.close();
                        socket.close();
                        break; // Si se envía con éxito, salir del bucle
                    } catch (IOException e) {
                        System.err.println("Error al intentar enviar la cola al servidor secundario: " + e.getMessage());
                        // Esperar un tiempo antes de intentar nuevamente
                        try {
                            Thread.sleep(5000); // Esperar 5 segundos antes de intentar nuevamente
                            System.out.println("No puede conectarse al servidor secundario xd");
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
                else{
                    System.err.println("No tiene sentido que un secundario envie cola a primario KKK");
                    break;
                }

            }
        });
        enviarColaThread.start();
    }


    // Método para recibir la cola del servidor principal
    private void recibirColaDelServidorPrincipal() {
        Thread recibirColaThread = new Thread(() -> {
            ServerSocket serverSocket=null;
            while (true) {
                if (!esPrimario){
                    try {
                        System.out.println("TRATANDO DE RECIBIR COLA PRINCIPAL");
                        if (serverSocket!=null)
                            serverSocket.close();
                        else {
                            serverSocket = new ServerSocket(3000);
                            System.out.println("XXXXXXXXXXxEsperando la cola del servidor principal...XXXXXXXXXx");
                            Socket socket = serverSocket.accept();
                            System.out.println("Conexion con la cola del servidor principal...");
                            ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
                            Cola<Cliente> colaRecibida = (Cola<Cliente>) inputStream.readObject();
                            cola = colaRecibida; // Actualiza la cola del servidor secundario con la cola recibida del servidor principal
                            System.out.println("Cola recibida del servidor principal.");
                            inputStream.close();
                            socket.close();
                            serverSocket.close();
                            break; // Si se recibe con éxito, salir del bucle
                        }
                    } catch (IOException | ClassNotFoundException e) {
                        System.err.println("Error al intentar recibir la cola del servidor principal: " + e.getMessage());
                        // Esperar un tiempo antes de intentar nuevamente
                        try {
                            Thread.sleep(5000); // Esperar 5 segundos antes de intentar nuevamente
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
                else {
                    System.err.println("No tiene sentido que un primario reciba cola  si es primario KKK");
                    break;
                }

            }
        });
        recibirColaThread.start();
    }


    public static void main(String[] args) {
        Server server = new Server();
    }

    public Cola<Cliente> getCola() {
        return cola;
    }

    public ArrayList<Cliente> getClientesEnAtencion() {
        return clientesEnAtencion;
    }

    public ArrayList<Cliente> getClientesAtendidos() {
        return clientesAtendidos;
    }


    public Cliente getPrimerCliente() {
        return cola.sacarCola();
    }

    public ServerSocket getServerSocket() {
        return serverSocket;
    }
}
