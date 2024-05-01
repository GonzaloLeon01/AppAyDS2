import java.net.*;
import java.io.*;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
//para las colas


public class Server {

    //inicializar socket y input stream
    private ServerSocket serverSocket = null;
    private Cola<Cliente> cola= new Cola<>();
    private ArrayList<Cliente> clientesEnAtencion= new ArrayList<>();
    private ArrayList<Cliente> clientesAtendidos= new ArrayList<>();
    private Cliente primerCliente;

    private static final int PUERTO_TOTTEM2=2234;
    private static final int PUERTO_OPERADOR2=2300;
    private static final int PUERTO_TV2=2236;
    private static final int PUERTO_ADMIN2=2237;

    //Constructor con puerto
    public Server() {
        try {
            serverSocket = new ServerSocket(PUERTO_TOTTEM2);
            System.out.println("Server started");
            System.out.println("Waiting for a client...");


            TottemHandler tottemHandler = new TottemHandler(this);
            tottemHandler.start(); //Inicia el hilo para recibir datos del Tottem

            OperadorHandler operadorHandler = new OperadorHandler(this);
            operadorHandler.start(); // Inicia el hilo para manejar la comunicación con el operador

            EstadisticasHandler estadisticasHandler = new EstadisticasHandler(this);
            estadisticasHandler.start(); //Inicia el hilo para enviar datos a Estadisticas

            NotificacionHandler notificacionHandler = new NotificacionHandler(this);
            notificacionHandler.start(); // Inicia el hilo para enviar datos a Notificacion


            Timer timer = new Timer();

            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    System.out.println("Sigue aca");
                    System.out.println("Clientes en cola:"+cola.size());
                    System.out.println(clientesAtendidos);
                    System.out.println(clientesEnAtencion);
                }
            }, 0, 15000); // 5000 milisegundos = 5 segundos

        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    public void procesarSolicitudTottem(Socket cliente){
        TottemRequestHandler requestHandler = new TottemRequestHandler(this,cliente);
        requestHandler.start();
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
