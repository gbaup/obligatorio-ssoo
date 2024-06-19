package com.example;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;


public class SistemaControlAcceso {
    public static final Semaphore colaImagenesSemaphore = new Semaphore(1);
    public static final Semaphore procesamientoSemaphore = new Semaphore(3); 
    public static final AtomicInteger idGenerator = new AtomicInteger();

    public static void main(String[] args) {
        boolean useRandomData = false; // Cambia esto a false para usar datos de archivo

        // if (useRandomData) {
        //     iniciarSistemaNormal();
        // } else {
        //     iniciarSistemaConDatosDeArchivo("C:\\Users\\ignrd\\OneDrive\\Escritorio\\obligatorio-ssoo\\src\\main\\java\\com\\example\\Archivos\\imagenes.csv");
        // }
        
        // Casos de prueba
        ejecutarCasosDePrueba();
    }

    public static void iniciarSistemaNormal() {
        BlockingQueue<Imagen> colaImagenes = new PriorityBlockingQueue<>(30, (i1, i2) -> {
            if (i1.isEsVIP() == i2.isEsVIP()) {
                return Integer.compare(i1.getId(), i2.getId());
            }
            return i1.isEsVIP() ? -1 : 1;
        });
        int cantidadImagenes = 30;  // Cambiar según la cantidad de imágenes que desees probar
        int intervaloCaptura = 1000;
        CountDownLatch latch = new CountDownLatch(3);
        List<Long> tiemposDeProcesamiento = new ArrayList<>();
        List<Long> tiemposDeEspera = new ArrayList<>();
        List<String> resultados = new ArrayList<>();

        Thread capturaImagenes = new CapturaImagenes(colaImagenes, cantidadImagenes, intervaloCaptura, latch);
        Thread reconocimientoFacial = new ReconocimientoFacial(colaImagenes, cantidadImagenes, latch, tiemposDeProcesamiento, tiemposDeEspera);
        Thread controlAcceso = new ControlAcceso(colaImagenes, cantidadImagenes, resultados, latch);

        capturaImagenes.start();
        reconocimientoFacial.start();
        controlAcceso.start();

        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.out.println("Sistema finalizado.");
    }

    /**
     * Inicia el sistema leyendo imágenes desde un archivo CSV.
     * 
     * @param filePath La ruta del archivo CSV.
     */
    public static void iniciarSistemaConDatosDeArchivo(String filePath) {
        BlockingQueue<Imagen> colaImagenes = new PriorityBlockingQueue<>(30, (i1, i2) -> {
            if (i1.isEsVIP() == i2.isEsVIP()) {
                return Integer.compare(i1.getId(), i2.getId());
            }
            return i1.isEsVIP() ? -1 : 1;
        });
        List<Imagen> imagenes = leerImagenesDeArchivo(filePath);
        if (imagenes == null || imagenes.isEmpty()) {
            System.err.println("Error al leer las imágenes del archivo o archivo vacío. Finalizando el programa.");
            return;
        }
        int cantidadImagenes = imagenes.size();
        CountDownLatch latch = new CountDownLatch(2);
        List<Long> tiemposDeProcesamiento = new ArrayList<>();
        List<Long> tiemposDeEspera = new ArrayList<>();
        List<String> resultados = new ArrayList<>();

        for (Imagen imagen : imagenes) {
            try {
                colaImagenes.put(imagen);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Error al agregar la imagen a la cola: " + imagen.getNombre());
            }
        }

        Thread reconocimientoFacial = new ReconocimientoFacial(colaImagenes, cantidadImagenes, latch, tiemposDeProcesamiento, tiemposDeEspera);
        Thread controlAcceso = new ControlAcceso(colaImagenes, cantidadImagenes, resultados, latch);

        reconocimientoFacial.start();
        controlAcceso.start();

        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        calcularYMostrarMetricas(tiemposDeProcesamiento, tiemposDeEspera, resultados);

        System.out.println("Sistema finalizado.");
    }

    public static List<Imagen> leerImagenesDeArchivo(String filePath) {
        List<Imagen> imagenes = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean firstLine = true;
            while ((line = br.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue;
                }
                String[] values = line.split(",");
                if (values.length != 2) {
                    System.err.println("Formato de línea incorrecto: " + line);
                    continue;
                }
                String nombre = values[0];
                boolean esVIP;
                try {
                    esVIP = Boolean.parseBoolean(values[1]);
                } catch (Exception e) {
                    System.err.println("Error al parsear VIP en la línea: " + line);
                    continue;
                }
                imagenes.add(new Imagen(nombre, esVIP, idGenerator.incrementAndGet()));
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return imagenes;
    }

    public static void realizarPrueba(String filePath) {
        System.out.println("Iniciando prueba con archivo: " + filePath);
        iniciarSistemaConDatosDeArchivo(filePath);
        System.out.println("Prueba con archivo " + filePath + " finalizada.");
        System.out.println("---------------------------------------------");
    }

    public static void ejecutarCasosDePrueba() {
        System.out.println("Iniciando casos de prueba...");
//
//        System.out.println("Escenario de Prueba: Inicio de Clases");
//        // Configura y ejecuta el escenario de prueba
//        iniciarSistemaConDatosDeArchivo("/Users/gastonbauer/Desktop/facultad/ssoo/obligatorio/src/main/java/com/example/Archivos/inicio_de_clases.csv");
//        System.out.println("Prueba de Inicio de Clases finalizada.");
//        System.out.println("---------------------------------------------");
//
//        System.out.println("Escenario de Prueba: Evento VIP");
//        // Configura y ejecuta el escenario de prueba
//        iniciarSistemaConDatosDeArchivo("/Users/gastonbauer/Desktop/facultad/ssoo/obligatorio/src/main/java/com/example/Archivos/evento_vip.csv");
//        System.out.println("Prueba de Evento VIP finalizada.");
//        System.out.println("---------------------------------------------");
//
//        System.out.println("Escenario de Prueba: Cambio de Iluminación");
//        // Configura y ejecuta el escenario de prueba
//        iniciarSistemaConDatosDeArchivo("/Users/gastonbauer/Desktop/facultad/ssoo/obligatorio/src/main/java/com/example/Archivos/cambio_de_iluminacion.csv");
//        System.out.println("Prueba de Cambio de Iluminación finalizada.");
//        System.out.println("---------------------------------------------");
//
//        // Caso de Prueba 4: Actualización de Base de Datos
//        System.out.println("Escenario de Prueba: Actualización de Base de Datos");
//        // Configura y ejecuta el escenario de prueba
//        iniciarSistemaConDatosDeArchivo("/Users/gastonbauer/Desktop/facultad/ssoo/obligatorio/src/main/java/com/example/Archivos/actualizacion_base_de_datos.csv");
//        System.out.println("Prueba de Actualización de Base de Datos finalizada.");
//        System.out.println("---------------------------------------------");
//
//        // Caso de Prueba 5: Carga Máxima
//        System.out.println("Escenario de Prueba: Carga Máxima");
//        // Configura y ejecuta el escenario de prueba
//        iniciarSistemaConDatosDeArchivo("/Users/gastonbauer/Desktop/facultad/ssoo/obligatorio/src/main/java/com/example/Archivos/carga_maxima.csv");
//        System.out.println("Prueba de Carga Máxima finalizada.");
//        System.out.println("---------------------------------------------");

        // Caso de Prueba 6: Reintentos de gente rechazada
        System.out.println("Escenario de Prueba: Reintentos de gente rechazada");
        // Configura y ejecuta el escenario de prueba
        iniciarSistemaConDatosDeArchivo("/Users/gastonbauer/Desktop/facultad/ssoo/obligatorio/src/main/java/com/example/Archivos/reintentos_gente_rechazada.csv");
        System.out.println("Prueba de Reintentos de gente rechazada finalizada.");
        System.out.println("---------------------------------------------");
        System.out.println("Casos de prueba finalizados.");
    }

    public static List<String> ejecutarSimulacion(int cantidadImagenes, int intervaloCaptura) {
        List<String> resultados = new ArrayList<>();
        List<Long> tiemposDeProcesamiento = new ArrayList<>();
        List<Long> tiemposDeEspera = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(3);
    
        try {
            BlockingQueue<Imagen> colaImagenes = new PriorityBlockingQueue<>();
            CapturaImagenes capturaImagenes = new CapturaImagenes(colaImagenes, cantidadImagenes, intervaloCaptura, latch);
            ReconocimientoFacial reconocimientoFacial = new ReconocimientoFacial(colaImagenes, cantidadImagenes, latch, tiemposDeProcesamiento, tiemposDeEspera);
            ControlAcceso controlAcceso = new ControlAcceso(colaImagenes, cantidadImagenes, resultados, latch);
    
            capturaImagenes.start();
            reconocimientoFacial.start();
            controlAcceso.start();
    
            latch.await();
    
            double sumaTiempos = 0;
            for (Long tiempo : tiemposDeProcesamiento) {
                sumaTiempos += tiempo;
            }
            double promedioTiempoProcesamiento = sumaTiempos / tiemposDeProcesamiento.size();
            resultados.add(String.format("Promedio de tiempo de procesamiento: %.2f ms", promedioTiempoProcesamiento));
    
            double sumaEspera = 0;
            for (Long tiempo : tiemposDeEspera) {
                sumaEspera += tiempo;
            }
            double promedioTiempoEspera = sumaEspera / tiemposDeEspera.size();
            resultados.add(String.format("Promedio de tiempo de espera en la cola: %.2f ms", promedioTiempoEspera));
    
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return resultados;
    }

    public static void calcularYMostrarMetricas(List<Long> tiemposDeProcesamiento, List<Long> tiemposDeEspera, List<String> resultados) {
        double tiempoPromedioProcesamiento = tiemposDeProcesamiento.stream().mapToLong(Long::longValue).average().orElse(0.0);
        double tiempoPromedioEspera = tiemposDeEspera.stream().mapToLong(Long::longValue).average().orElse(0.0);
        long totalProcesadasPorMinuto = (long) (60000.0 / tiempoPromedioProcesamiento);

        System.out.println("Métricas:");
        System.out.printf("Tiempo de procesamiento promedio por imagen: %.2f ms%n", tiempoPromedioProcesamiento);
        System.out.printf("Tiempo de espera en la cola de prioridad: %.2f ms%n", tiempoPromedioEspera);
        System.out.printf("Número de imágenes procesadas por minuto: %d%n", totalProcesadasPorMinuto);

        long totalAccesosPermitidos = resultados.stream().filter(r -> r.contains("Acceso permitido")).count();
        long totalAccesosDenegados = resultados.stream().filter(r -> r.contains("Acceso denegado")).count();
        System.out.printf("Total accesos permitidos: %d%n", totalAccesosPermitidos);
        System.out.printf("Total accesos denegados: %d%n", totalAccesosDenegados);
    }
}
