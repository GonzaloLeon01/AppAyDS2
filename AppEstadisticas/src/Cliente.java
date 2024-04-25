import java.io.Serializable;
import java.time.Duration;
import java.time.LocalTime;

public class Cliente implements Serializable {
    private String dni;
    private LocalTime tiempoDeLLegada;
    private LocalTime tiempoDeSalida;
    private int numeroCaja; //Se la asigna el Operador cuando lo atiende

    // Constructor
    public Cliente(String dni, LocalTime tiempoDeLLegada) {
        this.dni = dni;
        this.tiempoDeLLegada = tiempoDeLLegada;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public LocalTime getTiempoDeLLegada() {
        return tiempoDeLLegada;
    }

    public void setTiempoDeLLegada(LocalTime tiempoDeLLegada) {
        this.tiempoDeLLegada = tiempoDeLLegada;
    }

    public LocalTime getTiempoDeSalida() {
        return tiempoDeSalida;
    }

    public void setTiempoDeSalida(LocalTime tiempoDeSalida) {
        this.tiempoDeSalida = tiempoDeSalida;
    }

    // Método para calcular la duración de la visita
    public Duration calcularDuracionVisita() {
        return Duration.between(tiempoDeLLegada,tiempoDeSalida);
    }

    public int getNumeroCaja() {
        return numeroCaja;
    }

    public void setNumeroCaja(int numeroCaja) {
        this.numeroCaja = numeroCaja;
    }

    @Override
    public String toString() {
        return "Cliente{" +
                "dni='" + dni + '\'' +
                ", tiempoDeLLegada=" + tiempoDeLLegada +
                ", tiempoDeSalida=" + tiempoDeSalida +
                '}';
    }
}
