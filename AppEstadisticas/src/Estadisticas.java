import java.io.*;
import java.net.*;
import java.time.Duration;
import java.util.ArrayList;

public class Estadisticas {

    private static final int PUERTO_MONITOR_A_ADMIN=1703;
    private ArrayList<Cliente> listaClientesAtendidasRecibida = new ArrayList<Cliente>();
    private int numeroPuertoPrimario;
    private static final long INTERVALO_ENVIO_MS = 5000; // Intervalo de envío en milisegundos (5 segundos en este ejemplo)

    //Para hacer estadisticas
    private int cantidadAtendidos=0;
    private Duration tiempoMinEspera;
    private Duration tiempoMaxEspera;
    private Duration tiempoPromedioEspera;


    public Estadisticas(String address){
        Socket serverSocket = null;
        ObjectInputStream entrada=null;
            while (true) {
                System.out.println("Conectando con el Monitor...");
                Socket monitorSocket = null;
                DataInputStream monitorInput=null;
                try {
                    monitorSocket = new Socket(address,PUERTO_MONITOR_A_ADMIN);
                    System.out.println("Conexion exitosa con el Monitor");
                    monitorInput = new DataInputStream(monitorSocket.getInputStream());
                    //Lee el puerto del servidor primario del Monitor
                    this.numeroPuertoPrimario = monitorInput.readInt();
                    System.out.println("Puerto del servidor primario recibido: " + numeroPuertoPrimario);
                } catch (IOException e) {
                    System.err.println("No se puede conectar con el monitor");
                }
                System.out.println("El numero del puerto es: " + numeroPuertoPrimario);


                //Establecer conexion con el servidor para recibir lista de clientesAtendidos

                try {
                    serverSocket = new Socket(address, numeroPuertoPrimario);
                    System.out.println("Conexión establecida con el servidor en el puerto: " + numeroPuertoPrimario);
                    //Flujo de entrada desde el servidor
                    entrada = new ObjectInputStream(serverSocket.getInputStream());
                    System.out.println("Flujo de entrada creado correctamente.");
                    // El servidor me manda lista de clientes ya atendidos cuando se agrega un nuevo cliente

                    try {
                        listaClientesAtendidasRecibida = (ArrayList<Cliente>) entrada.readObject();
                    } catch (ClassNotFoundException e) {
                        System.out.println(e.getMessage());
                    }
                    System.out.println("Clientes recibidos: " + listaClientesAtendidasRecibida);

                    for (int i=0; i<listaClientesAtendidasRecibida.size();i++) {
                        System.out.println(listaClientesAtendidasRecibida.get(i));
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

                    // Esperar el intervalo de tiempo antes de recibir los datos nuevamente
                    try {
                        Thread.sleep(INTERVALO_ENVIO_MS);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }

                } catch (IOException e) {
                    System.err.println("No se puede conectar con el servidor");

                }

                try {
                    if (entrada != null) entrada.close();
                    if (serverSocket != null) serverSocket.close();
                } catch (IOException e) {
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
        Estadisticas estadisticas = new Estadisticas("127.0.0.1");
    }
}
