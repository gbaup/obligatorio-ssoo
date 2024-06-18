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

        if (useRandomData) {
            iniciarSistemaNormal();
        } else {
            iniciarSistemaConDatosDeArchivo("C:\\Users\\ignrd\\OneDrive\\Escritorio\\obligatorio\\src\\main\\java\\com\\example\\imagenes.csv");
        }

        // Ejecutar casos de prueba
        // ejecutarCasosDePrueba();
    }

    public static void iniciarSistemaNormal() {
        BlockingQueue<Imagen> colaImagenes = new PriorityBlockingQueue<>(30, (i1, i2) -> {
            if (i1.isEsVIP() == i2.isEsVIP()) {
                return Integer.compare(i1.getId(), i2.getId());
            }
            return i1.isEsVIP() ? -1 : 1;
        });
        int cantidadImagenes = 30;  // Cantidad de imagenes que se desea probar de forma aleatoria
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

    public static void iniciarSistemaConDatosDeArchivo(String filePath) {
        BlockingQueue<Imagen> colaImagenes = new PriorityBlockingQueue<>(30, (i1, i2) -> {
            if (i1.isEsVIP() == i2.isEsVIP()) {
                return Integer.compare(i1.getId(), i2.getId());
            }
            return i1.isEsVIP() ? -1 : 1;
        });
        List<Imagen> imagenes = leerImagenesDeArchivo(filePath);
        int cantidadImagenes = imagenes.size();
        CountDownLatch latch = new CountDownLatch(2);
        List<Long> tiemposDeProcesamiento = new ArrayList<>();
        List<Long> tiemposDeEspera = new ArrayList<>();
        List<String> resultados = new ArrayList<>();

        for (Imagen imagen : imagenes) {
            colaImagenes.add(imagen);
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
                String nombre = values[0];
                boolean esVIP = Boolean.parseBoolean(values[1]);
                imagenes.add(new Imagen(nombre, esVIP, idGenerator.incrementAndGet()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return imagenes;
    }

    public static void ejecutarCasosDePrueba() {
        System.out.println("Iniciando casos de prueba...");

        // Caso de Prueba 1: Inicio de Clases
        System.out.println("Caso de Prueba 1: Inicio de Clases");
        List<String> resultados1 = ejecutarSimulacion(10, 1000);

        // Caso de Prueba 2: Evento VIP
        System.out.println("Caso de Prueba 2: Evento VIP");
        List<String> resultados2 = ejecutarSimulacion(5, 500);

        // Caso de Prueba 3: Cambio de Iluminación
        System.out.println("Caso de Prueba 3: Cambio de Iluminación");
        List<String> resultados3 = ejecutarSimulacion(7, 1500);

        // Caso de Prueba 4: Actualización de Base de Datos
        System.out.println("Caso de Prueba 4: Actualización de Base de Datos");
        List<String> resultados4 = ejecutarSimulacion(8, 1200);

        // Caso de Prueba 5: Carga Máxima
        System.out.println("Caso de Prueba 5: Carga Máxima");
        List<String> resultados5 = ejecutarSimulacion(15, 800);

        System.out.println("------------------------------");
        System.out.println("Casos de prueba finalizados.");
        imprimirResultados("Caso de Prueba 1: Inicio de Clases", resultados1);
        imprimirResultados("Caso de Prueba 2: Evento VIP", resultados2);
        imprimirResultados("Caso de Prueba 3: Cambio de Iluminación", resultados3);
        imprimirResultados("Caso de Prueba 4: Actualización de Base de Datos", resultados4);
        imprimirResultados("Caso de Prueba 5: Carga Máxima", resultados5);
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
    
            // Calcula el promedio de tiempo de procesamiento
            double sumaTiempos = 0;
            for (Long tiempo : tiemposDeProcesamiento) {
                sumaTiempos += tiempo;
            }
            double promedioTiempoProcesamiento = sumaTiempos / tiemposDeProcesamiento.size();
            resultados.add(String.format("Promedio de tiempo de procesamiento: %.2f ms", promedioTiempoProcesamiento));
    
            // Calcula el promedio de tiempo de espera en la cola
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
    

    public static void imprimirResultados(String casoDePrueba, List<String> resultados) {
        System.out.println(casoDePrueba + " - Resultados:");
        for (String resultado : resultados) {
            System.out.println(resultado);
        }
    }
}
