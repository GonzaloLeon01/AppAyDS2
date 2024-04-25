import java.util.Queue;
import java.util.LinkedList;

public class Cola<Cliente> {
    private Queue<Cliente> cola;

    public Cola(){
        this.cola = new LinkedList<>();
    }

    //Metodo para agregar un elemento al final de la cola
    public void agregarAlFinal(Cliente cliente){
        cola.offer(cliente);
    }

    //Metodo para sacar de la cola el primer cliente y devolver el cliente
    public Cliente sacarCola(){
        return cola.poll();
    }

    //Metodo para ver que cliente esta primero
    public Cliente verPrimero(){
        return cola.peek();
    }

    //Metodo para checkear si la cola esta vacia
    public boolean isEmpty(){
        return cola.isEmpty();
    }

    //Metodo para obtener el tamanio de la cola
    public int size(){
        return cola.size();
    }

    //Metodo para imprimir la cola
    public void imprimirCola(){
        System.out.println(cola);
    }
}
