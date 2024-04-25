import java.io.IOException;
import java.net.Socket;

public class TottemHandler extends Thread {
    private Server server;

    public TottemHandler(Server server) {
        this.server = server;
    }

    @Override
    public void run() {
        while (true) {
            try {

                Socket cliente = server.getServerSocket().accept();
                System.out.println("Client connected");

                server.procesarSolicitudTottem(cliente);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
