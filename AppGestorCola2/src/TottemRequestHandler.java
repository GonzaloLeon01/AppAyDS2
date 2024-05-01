import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.time.LocalTime;

public class TottemRequestHandler extends Thread{
    private Server server;
    private Socket cliente;
    private DataInputStream entrada;
    private DataOutputStream salida;

    public TottemRequestHandler(Server server, Socket cliente) {
        this.server = server;
        this.cliente = cliente;
        try{
            entrada = new DataInputStream(cliente.getInputStream());
            salida = new DataOutputStream(cliente.getOutputStream());
        } catch (IOException e){
            System.err.println("Error al inicializar los flujos de entrada/salida");
        }
    }


    @Override
    public void run() {
        try {

            String dniRecibido = "";

            while (true) {

                dniRecibido = entrada.readUTF();
                if (dniRecibido.equals("Over")) {
                    break;
                }
                Cliente nuevoCliente = new Cliente(dniRecibido, LocalTime.now()); //crea cliente
                server.getCola().agregarAlFinal(nuevoCliente); //agrega el cliente a la cola
                salida.writeInt(server.getCola().size()); //le devuelve el size al tottem
                System.out.println(server.getCola().size());
            }
        } catch (IOException e) {
            // Manejar la excepción de manera adecuada
            System.err.println("Error durante la comunicación con el cliente: " + e.getMessage());
        } finally {
            // Cerrar flujos de entrada y salida y el socket del cliente
            try {
                //if (entrada != null) entrada.close();
                //if (salida != null) salida.close();
                if (cliente != null) cliente.close();
            } catch (IOException ex) {
                System.err.println("Error al cerrar los flujos y el socket del cliente: " + ex.getMessage());
            }
        }
    }

}


