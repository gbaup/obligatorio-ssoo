import com.example.SistemaControlAcceso;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.List;

public class SistemaControlAccesoTest {

    private String databasePath = "/Users/gastonbauer/Desktop/facultad/ssoo/obligatorio/src/main/java/com/example/Archivos/base_de_datos.csv";


    @Test
    public void testInicioDeClases() {
        System.out.println("Escenario de Prueba: Inicio de Clases");
        List<String> resultados = SistemaControlAcceso.ejecutarSimulacion(
                "/Users/gastonbauer/Desktop/facultad/ssoo/obligatorio/src/main/java/com/example/Archivos/inicio_de_clases.csv",
                databasePath,
                500, 1
        );

        assertNotNull(resultados);
        assertFalse(resultados.isEmpty());

        long totalAccesosPermitidos = resultados.stream().filter(r -> r.contains("Acceso permitido")).count();
        long totalAccesosDenegados = resultados.stream().filter(r -> r.contains("Acceso denegado")).count();

        System.out.printf("Total accesos permitidos: %d%n", totalAccesosPermitidos);
        System.out.printf("Total accesos denegados: %d%n", totalAccesosDenegados);

        assertTrue(totalAccesosPermitidos > 0);
        assertTrue(totalAccesosDenegados >= 0);
    }

    @Test
    public void testEventoVIP() {
        System.out.println("Escenario de Prueba: Evento VIP");
        List<String> resultados = SistemaControlAcceso.ejecutarSimulacion(
                "/Users/gastonbauer/Desktop/facultad/ssoo/obligatorio/src/main/java/com/example/Archivos/eventovip.csv",
                databasePath,
                500, 10
        );

        assertNotNull(resultados);
        assertFalse(resultados.isEmpty());

        long totalAccesosVIPPermitidos = resultados.stream().filter(r -> r.contains("Acceso permitido") && r.contains("VIP")).count();
        long totalAccesosVIPDenegados = resultados.stream().filter(r -> r.contains("Acceso denegado") && r.contains("VIP")).count();

        System.out.printf("Total accesos VIP permitidos: %d%n", totalAccesosVIPPermitidos);
        System.out.printf("Total accesos VIP denegados: %d%n", totalAccesosVIPDenegados);

        assertTrue(totalAccesosVIPPermitidos > 0);
        assertEquals(0, totalAccesosVIPDenegados);
    }

    @Test
    public void testCargaMaxima() {
        System.out.println("Escenario de Prueba: Carga Máxima");
        List<String> resultados = SistemaControlAcceso.ejecutarSimulacion(
                "/Users/gastonbauer/Desktop/facultad/ssoo/obligatorio/src/main/java/com/example/Archivos/cargamaxima.csv",
                databasePath,
                200, 20
        );

        assertNotNull(resultados);
        assertFalse(resultados.isEmpty());

        long totalAccesosPermitidos = resultados.stream().filter(r -> r.contains("Acceso permitido")).count();
        long totalAccesosDenegados = resultados.stream().filter(r -> r.contains("Acceso denegado")).count();

        System.out.printf("Total accesos permitidos: %d%n", totalAccesosPermitidos);
        System.out.printf("Total accesos denegados: %d%n", totalAccesosDenegados);

        assertTrue(totalAccesosPermitidos > 0);
        assertTrue(totalAccesosDenegados >= 0);
    }


}
