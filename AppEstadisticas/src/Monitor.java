import java.io.*;
import java.net.*;
import java.time.Duration;
import java.util.ArrayList;

public class Monitor {

    private Socket socket = null;
    private ObjectInputStream entrada=null;

    private static final int PUERTO_ADMIN=1237;
    private ArrayList<Cliente> listaClientesAtendidasRecibida = new ArrayList<Cliente>();
    private static final long INTERVALO_ENVIO_MS = 5000; // Intervalo de envío en milisegundos (5 segundos en este ejemplo)

    private int cantidadAtendidos=0;
    private Duration tiempoMinEspera;
    private Duration tiempoMaxEspera;
    private Duration tiempoPromedioEspera;

    public Monitor(String address, int port) {
        //establece una conexion
        try{
            socket = new Socket(address,port);
            System.out.println("Conexión establecida correctamente.");
            // Ciclo para recibir múltiples envíos del servidor
            while (true) {
                try {
                    //Flujo de entrada desde el servidor
                    entrada = new ObjectInputStream(socket.getInputStream());
                    System.out.println("Flujo de entrada creado correctamente.");
                    // El servidor me manda lista de clientes ya atendidos cuando se agrega un nuevo cliente
                    listaClientesAtendidasRecibida = (ArrayList<Cliente>) entrada.readObject();
                    System.out.println("Clientes recibidos: " + listaClientesAtendidasRecibida);

                    // Esperar el intervalo de tiempo antes de recibir los datos nuevamente
                    try {
                        Thread.sleep(INTERVALO_ENVIO_MS);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }

                    if (listaClientesAtendidasRecibida.size()!=0){
                        tiempoMinEspera = listaClientesAtendidasRecibida.get(0).calcularDuracionVisita();
                        tiempoMaxEspera = listaClientesAtendidasRecibida.get(0).calcularDuracionVisita();
                        Duration sumaEspera=Duration.ZERO;
                        for (int i=0; i<listaClientesAtendidasRecibida.size(); i++) {
                            String dni =listaClientesAtendidasRecibida.get(i).getDni();
                            int nroCaja = listaClientesAtendidasRecibida.get(i).getNumeroCaja();
                            Duration espera= listaClientesAtendidasRecibida.get(i).calcularDuracionVisita();
                            if (espera.compareTo(tiempoMinEspera) < 0)
                                this.tiempoMinEspera=espera;
                            if (espera.compareTo(tiempoMaxEspera) > 0)
                                this.tiempoMaxEspera=espera;

                            sumaEspera = sumaEspera.plus(espera);
                        }
                        cantidadAtendidos = listaClientesAtendidasRecibida.size();
                        tiempoPromedioEspera = sumaEspera.dividedBy(cantidadAtendidos);
                    }
                } catch (ClassNotFoundException e) {
                    System.err.println("Error: No se pudo encontrar la clase Cliente.");
                    e.printStackTrace();
                }
            }

        }
        catch (UnknownHostException u){
            System.err.println("Error: El host es desconocido.");
            return;
        }
        catch (IOException i){
            System.err.println("Error de entrada/salida al conectar con el servidor.");
            return;
        } finally {
            // Cerrar la conexión
            try {
                if (entrada != null)
                    entrada.close();
                if (socket != null)
                    socket.close();
            } catch (IOException e) {
                System.err.println("Error al cerrar la conexión.");
                e.printStackTrace();
            }
        }
    }

    public int getCantidadAtendidos() {
        return cantidadAtendidos;
    }

    public void setCantidadAtendidos(int cantidadAtendidos) {
        this.cantidadAtendidos = cantidadAtendidos;
    }

    public Duration getTiempoMinEspera() {
        return tiempoMinEspera;
    }

    public void setTiempoMinEspera(Duration tiempoMinEspera) {
        this.tiempoMinEspera = tiempoMinEspera;
    }

    public Duration getTiempoMaxEspera() {
        return tiempoMaxEspera;
    }

    public void setTiempoMaxEspera(Duration tiempoMaxEspera) {
        this.tiempoMaxEspera = tiempoMaxEspera;
    }

    public Duration getTiempoPromedioEspera() {
        return tiempoPromedioEspera;
    }

    public void setTiempoPromedioEspera(Duration tiempoPromedioEspera) {
        this.tiempoPromedioEspera = tiempoPromedioEspera;
    }

    public static void main(String[] args) {
        Monitor monitor = new Monitor("127.0.0.1",PUERTO_ADMIN);
    }
}
