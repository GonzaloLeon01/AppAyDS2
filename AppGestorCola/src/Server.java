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
    private boolean esPrimario;     //devuelve 1 si es principal el Monitor o devuelve 0 si es secundario.


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

            Timer timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    System.out.println("Clientes en cola:" + cola.size());
                    System.out.println("Clientes atendidos " + clientesAtendidos);
                    System.out.println("Clientes en atencion " + clientesEnAtencion);
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
            this.esPrimario = (mensaje == 1);
            System.out.println("Valor de esPrimario actualizado a: " + esPrimario);
        } catch (EOFException e) {
            // Ignorar la excepción EOFException y continuar esperando conexiones del monitor
        } catch (IOException e) {
            e.printStackTrace();
        }
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
