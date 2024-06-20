import com.example.SistemaControlAcceso;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.List;

public class SistemaControlAccesoTest {

    @Test
    public void testInicioDeClases() {
        System.out.println("Escenario de Prueba: Inicio de Clases");
        List<String> resultados = SistemaControlAcceso.ejecutarSimulacion(
            "C:\\Users\\ignrd\\OneDrive\\Escritorio\\obligatorio\\src\\main\\java\\com\\example\\Archivos\\inicio_de_clases.csv",
            1000, 10 // Intervalo de captura y cantidad de hilos
        );

        assertNotNull(resultados);
        for (String resultado : resultados) {
            System.out.println(resultado);
        }
    }

    @Test
    public void testEventoVIP() {
        System.out.println("Escenario de Prueba: Evento VIP");
        List<String> resultados = SistemaControlAcceso.ejecutarSimulacion(
            "C:\\Users\\ignrd\\OneDrive\\Escritorio\\obligatorio\\src\\main\\java\\com\\example\\Archivos\\evento_vip.csv",
            1000, 10 // Intervalo de captura y cantidad de hilos
        );

        assertNotNull(resultados);
        for (String resultado : resultados) {
            System.out.println(resultado);
        }
    }

    @Test
    public void testCambioDeIluminacion() {
        System.out.println("Escenario de Prueba: Cambio de Iluminación");
        List<String> resultados = SistemaControlAcceso.ejecutarSimulacion(
            "C:\\Users\\ignrd\\OneDrive\\Escritorio\\obligatorio\\src\\main\\java\\com\\example\\Archivos\\cambio_de_iluminacion.csv",
            1000, 10 // Intervalo de captura y cantidad de hilos
        );

        assertNotNull(resultados);
        for (String resultado : resultados) {
            System.out.println(resultado);
        }
    }

    @Test
    public void testActualizacionBaseDeDatos() {
        System.out.println("Escenario de Prueba: Actualización de Base de Datos");
        List<String> resultados = SistemaControlAcceso.ejecutarSimulacion(
            "C:\\Users\\ignrd\\OneDrive\\Escritorio\\obligatorio\\src\\main\\java\\com\\example\\Archivos\\actualizacion_base_de_datos.csv",
            1000, 10 // Intervalo de captura y cantidad de hilos
        );

        assertNotNull(resultados);
        for (String resultado : resultados) {
            System.out.println(resultado);
        }
    }

    @Test
    public void testCargaMaxima() {
        System.out.println("Escenario de Prueba: Carga Máxima");
        List<String> resultados = SistemaControlAcceso.ejecutarSimulacion(
            "C:\\Users\\ignrd\\OneDrive\\Escritorio\\obligatorio\\src\\main\\java\\com\\example\\Archivos\\carga_maxima.csv",
            1000, 10 // Intervalo de captura y cantidad de hilos
        );

        assertNotNull(resultados);
        for (String resultado : resultados) {
            System.out.println(resultado);
        }
    }
}
