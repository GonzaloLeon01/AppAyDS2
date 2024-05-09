import java.io.*;
import java.net.*;

public class Operador {
    private Socket socket;
    private ObjectOutputStream salida;
    private ObjectInputStream entrada;
    private static final int PUERTO_OPERADOR = 1300;
    private Cliente cliente;
    private static int numeroCajaGlobal=0;
    private int numeroCaja;

    public Operador(String address, int port) {
        try {
            System.out.println("Conectando al servidor...");
            socket = new Socket(address, port);
            System.out.println("Socket value: " + socket);
            System.out.println("Operador connected");

            System.out.println("Creando flujo de salida desde el servidor...");
            salida = new ObjectOutputStream(socket.getOutputStream());
            System.out.println("Flujo de salida desde el servidor creado correctamente.");
            System.out.println("Creando flujo de entrada desde el servidor...");
            entrada = new ObjectInputStream(socket.getInputStream());
            System.out.println("Flujo de entrada desde el servidor creado correctamente.");

        } catch (IOException e) {
            System.err.println("Error al conectar con el operador: " + e.getMessage());
        }

        this.numeroCaja=numeroCajaGlobal;
        numeroCajaGlobal++;
        llamarCliente();

    }

    public void llamarCliente() {
        // Enviar solicitud al servidor para obtener un objeto Cliente
        try {
            //Le envia al servidor una solicitud llamar cliente
            //salida.writeObject("LLAMAR_CLIENTE");
            salida.writeObject(this.numeroCaja);
            salida.flush();

            //El servidor luego de recibir esa solicitud le envia el cliente
            Cliente clienteRecibido = (Cliente) entrada.readObject();
            //Al cliente enviado le asigna el numero de caja
            if (clienteRecibido!=null){
                clienteRecibido.setNumeroCaja(this.numeroCaja);
            }
            this.cliente=clienteRecibido;
            System.out.println(clienteRecibido);
            //despacharCliente(clienteRecibido);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void despacharCliente(Cliente cliente) {
        // Enviar objeto Cliente al servidor
        try {
            //Le envia al servidor una solicitud Despachar Cliente
            salida.writeObject("DESPACHAR_CLIENTE");

            //EL servidor cuando reciba esa solicitud
            // tiene que estar preparado para recibir el cliente
            salida.writeObject(cliente);
            System.out.println(cliente);
            salida.flush();
            this.cliente=null;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    public static void main(String[] args) {
        Operador operador = new Operador("127.0.0.1", PUERTO_OPERADOR);
    }
}
