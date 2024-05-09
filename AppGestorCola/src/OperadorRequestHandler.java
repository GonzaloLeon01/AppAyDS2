import java.net.*;
import java.io.*;
import java.time.LocalTime;

public class OperadorRequestHandler extends Thread {
    private Socket operadorCliente;
    private Server server;
    private ObjectInputStream entrada;
    private ObjectOutputStream salida;

    public OperadorRequestHandler(Socket operadorCliente, Server server) {
        this.operadorCliente = operadorCliente;
        this.server = server;
    }

    @Override
    public void run() {
        try {
            // Inicializa los flujos de entrada y salida
            entrada = new ObjectInputStream(operadorCliente.getInputStream());
            salida = new ObjectOutputStream(operadorCliente.getOutputStream());

            // Loop principal
            while (true) {
                // Leer la solicitud del operador
                String solicitud = null;
                try {
                    System.out.println(entrada);
                    solicitud = (String) entrada.readObject();
                } catch (IOException exc) {
                    // Manejar la excepción de lectura
                    System.err.println("Error al leer la solicitud del operador: " + exc.getMessage());
                    break; // Sale del bucle y termina el hilo
                } catch (ClassNotFoundException exc) {
                    // Manejar la excepción de clase no encontrada
                    System.err.println("Clase no encontrada al leer la solicitud del operador: " + exc.getMessage());
                    break; // Sale del bucle y termina el hilo
                }



                // Manejar la solicitud
                if (solicitud.length() <= 3) {
                    // Lógica para enviar un cliente al operador
                    Cliente clientePrimero = server.getCola().sacarCola();
                    System.out.println("Cliente sacado de la cola: " + clientePrimero);
                    if (clientePrimero != null) {
                        clientePrimero.setTiempoDeSalida(LocalTime.now());
                        clientePrimero.setNumeroCaja(Integer.parseInt(solicitud));
                        // Agregamos el cliente a la lista de clientes en atención
                        server.getClientesEnAtencion().add(clientePrimero); // para mostrar en NOTIFACION que seria el arreglo de clientesEnAtencion
                        // Enviamos el cliente al operador
                        try {
                            salida.writeObject(clientePrimero);
                            salida.flush();
                        } catch (IOException exc) {
                            System.err.println("Error al enviar el cliente al operador: " + exc.getMessage());
                            break; // Sale del bucle y termina el hilo                        }
                        }
                    }
                } else if (solicitud.equals("DESPACHAR_CLIENTE")) {
                    // Lógica para recibir un cliente del operador y hacer algo con él
                    Cliente clienteModificado = null;
                    try {
                        clienteModificado = (Cliente) entrada.readObject();
                    } catch (IOException exc) {
                        System.err.println("Error al leer el cliente del operador: " + exc.getMessage());
                        break; // Sale del bucle y termina el hilo

                    } catch (ClassNotFoundException exc) {
                        // Manejar la excepción de clase no encontrada
                        System.err.println("Clase no encontrada al leer el cliente del operador: " + exc.getMessage());
                        break; // Sale del bucle y termina el hilo
                    }
                    if (clienteModificado != null) {
                        // Agregamos el cliente modificado a la lista de clientes atendidos
                        server.getClientesAtendidos().add(clienteModificado); //para mostrar en ESTADISTCIAS que seria el arreglo de clientesAtendidos
                        //server.getClientesEnAtencion().remove(server.getClientesEnAtencion().get(0));
                        Cliente clienteEncontrado = null;
                        for (Cliente encontrado : server.getClientesEnAtencion()) {
                            if (encontrado.getDni().equals(clienteModificado.getDni())) {
                                clienteEncontrado = encontrado;
                                break;
                            }
                        }
                        if(clienteEncontrado!=null)
                            server.getClientesEnAtencion().remove(clienteEncontrado);

                    }
                } else {
                    // Manejar otras solicitudes si es necesario
                }
            }

        } catch (IOException e) {
            System.err.println("Error durante la comunicación con el operador: " + e.getMessage());
        } finally {
            try {
                if (operadorCliente != null) {
                    operadorCliente.close();
                }
            } catch (IOException ex) {
                System.err.println("Error al cerrar el socket del operador: " + ex.getMessage());
            }
        }
    }
}
