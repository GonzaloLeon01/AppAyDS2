import java.io.*;
import java.net.*;

public class Tottem {

    private Socket socket = null;
    private DataInputStream entrada=null;
    private DataOutputStream salida =null;

    private int numeroPuertoPrimario;

    private static final int PUERTO_TOTTEM=1234;
    private int cantidadClientes;

    //Constructor para colocar ip y puerto
    public Tottem(String address) {
        //establece una conexion
        try{

            //Tiene que haber un ciclo, porque si se cae el servidor primario, tiene que mandar al secundario
            //Tiene que haber una conexion permanente con AppMonitor
            //Siempre antes de mandar String dni al servidor, pregunta a que servidor mandar.

            //Antes de aca tiene que haber una conexion con AppMonitor, la cual le va a otorgar el numeroPuertoPrimario

            numeroPuertoPrimario=PUERTO_TOTTEM;
            socket = new Socket(address,numeroPuertoPrimario);

            System.out.println("El tottem se conecto al servidor");

            //Abre flujo de entrada para recibir datos del servidor
            entrada = new DataInputStream(socket.getInputStream());

            //Abre flujo de salida para enviar datos al servidor
            salida = new DataOutputStream(socket.getOutputStream());

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
        catch (UnknownHostException u){
            System.out.println(u);
        }
        catch (IOException i){
            System.out.println(i);
        }

        //tengo que cerrar conexion y abrir de nuevo ya que va a haber un while true

    }

    public static void main(String[] args) {
        //Antes estaba asi
        //Tottem tottem = new Tottem("127.0.0.1",PUERTO_TOTTEM);
        //Pero ahora esta asi
        Tottem tottem = new Tottem("127.0.0.1");
    }
}
