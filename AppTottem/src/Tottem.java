import java.io.*;
import java.net.*;


public class Tottem {

    private Socket socket = null;
    private DataInputStream entrada=null;
    private DataOutputStream salida =null;

    private static final int PUERTO_TOTTEM=1234;

    private int cantidadClientes;
    //Constructor para colocar ip y puerto

    public Tottem(String address, int port) {
        //establece una conexion
        try{

            socket = new Socket(address,port);
            System.out.println("Connected");

            //Flujo de entrada desde el servidor
            entrada = new DataInputStream(socket.getInputStream());

            //Flujo de salida hacia el servidor
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



    }

    public static void main(String[] args) {
        //al ejecutar el jar, ya setea la ip y el puerto
        Tottem tottem = new Tottem("127.0.0.1",PUERTO_TOTTEM);
    }
}
