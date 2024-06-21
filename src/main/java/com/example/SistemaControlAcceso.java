package com.example;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

public class SistemaControlAcceso {
    public static final Semaphore colaImagenesSemaphore = new Semaphore(1);
    public static final Semaphore procesamientoSemaphore = new Semaphore(10); // Incrementar para mayor concurrencia
    public static final AtomicInteger idGenerator = new AtomicInteger();
    public static Set<String> imagenesPermitidas = new HashSet<>();

    public static void main(String[] args) {
        try {
            System.out.println("Iniciando sistema...");
            cargarImagenesPermitidas("/Users/gastonbauer/Desktop/facultad/ssoo/obligatorio/src/main/java/com/example/Archivos/base_de_datos.csv");
            iniciarSistemaConDatosDeArchivo("/Users/gastonbauer/Desktop/facultad/ssoo/obligatorio/src/main/java/com/example/Archivos/imagenes.csv");
            System.out.println("---------------------------------------------");

            //ejecutarCasosDePrueba();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void cargarImagenesPermitidas(String filePath) {
        try {
            String[] lineas = ManejadorArchivosGenerico.leerArchivo(filePath, false);
            for (String linea : lineas) {
                imagenesPermitidas.add(linea.trim().split(",")[0]);
            }
        } catch (Exception e) {
            System.err.println("Error al cargar imágenes permitidas: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void iniciarSistemaConDatosDeArchivo(String filePath) {
        BlockingQueue<Imagen> colaPrioridad = new PriorityBlockingQueue<>(30, (i1, i2) -> {
            if (i1.isEsVIP() == i2.isEsVIP()) {
                return Integer.compare(i1.getId(), i2.getId());
            }
            return i1.isEsVIP() ? -1 : 1;
        });

        BlockingQueue<Imagen> colaProcesamiento = new PriorityBlockingQueue<>(30, (i1, i2) -> {
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
        CountDownLatch latch = new CountDownLatch(3); // Ajustado a 3 hilos principales
        List<Long> tiemposDeProcesamiento = new ArrayList<>();
        List<Long> tiemposDeEspera = new ArrayList<>();
        List<String> resultados = new ArrayList<>();

        System.out.println("Inicializando colas de imágenes...");
        for (Imagen imagen : imagenes) {
            colaPrioridad.add(imagen);
        }

        Thread capturaImagenes = new CapturaImagenes(colaPrioridad, colaProcesamiento, latch, 500); // Reducir intervalo de captura
        Thread reconocimientoFacial = new ReconocimientoFacial(colaProcesamiento, cantidadImagenes, latch, tiemposDeProcesamiento, tiemposDeEspera);
        Thread controlAcceso = new ControlAcceso(colaProcesamiento, cantidadImagenes, resultados, latch);

        System.out.println("Iniciando hilos...");
        capturaImagenes.start();
        reconocimientoFacial.start();
        controlAcceso.start();

        try {
            latch.await();
            System.out.println("Todos los hilos han terminado.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Error en latch.await()");
        }

        // Calcular y mostrar métricas
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
                    firstLine = false; // Ignorar la primera línea del archivo
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
                imagenes.add(new Imagen(nombre, esVIP));
            }
        } catch (IOException e) {
            System.err.println("Error al leer las imágenes del archivo: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
        return imagenes;
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

    public static List<String> ejecutarSimulacion(String filePath, String database, int intervaloCaptura, int cantidadHilos) {
        System.out.println("Iniciando sistema...");
        cargarImagenesPermitidas(database);
        BlockingQueue<Imagen> colaPrioridad = new PriorityBlockingQueue<>(30, (i1, i2) -> {
            if (i1.isEsVIP() == i2.isEsVIP()) {
                return Integer.compare(i1.getId(), i2.getId());
            }
            return i1.isEsVIP() ? -1 : 1;
        });

        BlockingQueue<Imagen> colaProcesamiento = new PriorityBlockingQueue<>(30, (i1, i2) -> {
            if (i1.isEsVIP() == i2.isEsVIP()) {
                return Integer.compare(i1.getId(), i2.getId());
            }
            return i1.isEsVIP() ? -1 : 1;
        });

        List<Imagen> imagenes = leerImagenesDeArchivo(filePath);
        if (imagenes == null || imagenes.isEmpty()) {
            System.err.println("Error al leer las imágenes del archivo o archivo vacío. Finalizando el programa.");
            return null;
        }
        int cantidadImagenes = imagenes.size();
        CountDownLatch latch = new CountDownLatch(cantidadHilos * 2 + 1); // Ajustar a la cantidad de hilos (capturaImagenes y reconocimientoFacial) + 1 (controlAcceso)
        List<Long> tiemposDeProcesamiento = new ArrayList<>();
        List<Long> tiemposDeEspera = new ArrayList<>();
        List<String> resultados = new ArrayList<>();

        System.out.println("Inicializando colas de imágenes...");
        for (Imagen imagen : imagenes) {
            colaPrioridad.add(imagen);
        }

        List<Thread> hilosCaptura = new ArrayList<>();
        for (int i = 0; i < cantidadHilos; i++) {
            Thread capturaImagenes = new CapturaImagenes(colaPrioridad, colaProcesamiento, latch, intervaloCaptura);
            hilosCaptura.add(capturaImagenes);
            capturaImagenes.start();
        }

        List<Thread> hilosReconocimiento = new ArrayList<>();
        for (int i = 0; i < cantidadHilos; i++) {
            Thread reconocimientoFacial = new ReconocimientoFacial(colaProcesamiento, cantidadImagenes / cantidadHilos, latch, tiemposDeProcesamiento, tiemposDeEspera);
            hilosReconocimiento.add(reconocimientoFacial);
            reconocimientoFacial.start();
        }

        Thread controlAcceso = new ControlAcceso(colaProcesamiento, cantidadImagenes, resultados, latch);
        controlAcceso.start();

        try {
            latch.await();
            System.out.println("Todos los hilos han terminado.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Error en latch.await()");
        }

        // Calcular y mostrar métricas
        calcularYMostrarMetricas(tiemposDeProcesamiento, tiemposDeEspera, resultados);

        System.out.println("Sistema finalizado.");

        return resultados;
    }
}