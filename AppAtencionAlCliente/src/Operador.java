import java.io.*;
import java.net.*;

public class Operador {

    private static final int PUERTO_MONITOR_A_OPERADOR=1701;
    private int numeroPuertoPrimario;
    private Cliente cliente;
    private int numeroCaja;
    private ObjectOutputStream salida;
    private Socket serverSocket;
    private ObjectInputStream entrada=null;




    public Operador(String address){
        while (true){
            System.out.println("Conectando con el Monitor...");
            Socket monitorSocket;
            DataInputStream monitorInput;
            try {
                monitorSocket = new Socket(address,PUERTO_MONITOR_A_OPERADOR);
                System.out.println("Conexion exitosa con el Monitor");
                monitorInput = new DataInputStream(monitorSocket.getInputStream());
                //Lee el puerto del servidor primario del Monitor
                this.numeroPuertoPrimario = monitorInput.readInt();
                System.out.println("Puerto del servidor primario recibido: " + numeroPuertoPrimario);
            } catch (IOException e) {
                System.err.println("No se puede conectar con el monitor");
            }
            System.out.println("El numero del puerto es: " + numeroPuertoPrimario);


            //Establecer conexion con el servidor
            while (true) {
                try {
                    serverSocket = new Socket(address, numeroPuertoPrimario);
                    System.out.println("Conexión establecida con el servidor en el puerto: " + numeroPuertoPrimario);
                    System.out.println("Operador connected");

                    System.out.println("Creando flujo de salida desde el servidor...");
                    salida = new ObjectOutputStream(serverSocket.getOutputStream());
                    System.out.println("Flujo de salida desde el servidor creado correctamente.");
                    System.out.println("Creando flujo de entrada desde el servidor...");
                    entrada = new ObjectInputStream(serverSocket.getInputStream());
                    System.out.println("Flujo de entrada desde el servidor creado correctamente.");

                    llamarCliente();

                } catch (IOException e) {
                    System.err.println("No se puede conectar con el servidor");
                    break;
                }
            }

        }

        /*
                try {
            if (entrada != null) entrada.close();
            if (serverSocket != null) serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
         */

    }



    public void llamarCliente() {
        // Enviar solicitud al servidor para obtener un objeto Cliente
        try {
            //Le envia al servidor una solicitud llamar cliente
            //salida.writeObject("LLAMAR_CLIENTE");
            salida.writeObject(getNumeroDeCaja());
            salida.flush();

            //El servidor luego de recibir esa solicitud le envia el cliente
            Cliente clienteRecibido = (Cliente) entrada.readObject();
            //Al cliente enviado le asigna el numero de caja
            if (clienteRecibido!=null){
                clienteRecibido.setNumeroCaja(this.numeroCaja);
            }
            this.cliente=clienteRecibido;
            System.out.println(clienteRecibido);

            try {
                Thread.sleep(15000); // 15 segundos
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            despacharCliente(clienteRecibido);

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


    public String getNumeroDeCaja(){
        return String.valueOf(this.numeroCaja);
    }
    public void setNumeroDeCaja(String numeroCaja){
        this.numeroCaja = Integer.parseInt(numeroCaja);
        System.out.println(numeroCaja);
    }

    public Cliente getClienteAtendido(){
        return this.cliente;
    }

    public static void main(String[] args) {
        Operador operador = new Operador("127.0.0.1");
    }
}
