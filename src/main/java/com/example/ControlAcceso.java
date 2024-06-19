package com.example;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CopyOnWriteArrayList;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;

/**
 * Clase que simula el control de acceso.
 * 
 * Extiende Thread y decide si las imágenes procesadas tienen acceso permitido o denegado.
 */
public class ControlAcceso extends Thread {
    private final BlockingQueue<Imagen> colaImagenes;
    private final int cantidadImagenes;
    private final List<Imagen> imagenesProcesadas;
    private final List<String> resultados;
    private final CountDownLatch latch;

    public ControlAcceso(BlockingQueue<Imagen> colaImagenes, int cantidadImagenes, List<String> resultados, CountDownLatch latch) {
        this.colaImagenes = colaImagenes;
        this.cantidadImagenes = cantidadImagenes;
        this.resultados = resultados;
        this.latch = latch;
        this.imagenesProcesadas = new CopyOnWriteArrayList<>();
        this.setName("ControlAcceso");
    }

    @Override
    public void run() {
        try {

            List<String> rechazados = Files.lines(Paths.get("/Users/gastonbauer/Desktop/facultad/ssoo/obligatorio/src/main/java/com/example/Archivos/rechazados.txt")).collect(Collectors.toList());
//            String[] rechazados = ManejadorArchivosGenerico.leerArchivo("/Users/gastonbauer/Desktop/facultad/ssoo/obligatorio/src/main/java/com/example/Archivos/rechazados.txt", true);

            int controladas = 0;
            while (cantidadImagenes == -1 || controladas < cantidadImagenes) {
                Imagen imagen = colaImagenes.take();

                if (rechazados.contains(imagen.getNombre())) {
                    resultados.add("Acceso denegado para: " + imagen.getNombre() + " (VIP: " + imagen.isEsVIP() + ") - Usuario previamente rechazado");
                    System.out.println("[" + Thread.currentThread().getName() + "] Acceso denegado para: " + imagen.getNombre() + " (VIP: " + imagen.isEsVIP() + ") - Usuario previamente rechazado");
                    continue;
                }

                if (imagen.isProcesada() && !imagenesProcesadas.contains(imagen)) {
                    if (imagen.isAccesoPermitido()) {
                        resultados.add("Acceso permitido para: " + imagen.getNombre() + " (VIP: " + imagen.isEsVIP() + ")");
                        System.out.println("[" + Thread.currentThread().getName() + "] Acceso permitido para: " + imagen.getNombre() + " (VIP: " + imagen.isEsVIP() + ")");
                    } else {
                        resultados.add("Acceso denegado para: " + imagen.getNombre() + " (VIP: " + imagen.isEsVIP() + ")");
                        System.out.println("[" + Thread.currentThread().getName() + "] Acceso denegado para: " + imagen.getNombre() + " (VIP: " + imagen.isEsVIP() + ")");
//                  Descomentar esto si queremos que se registren los rechazados
//                        registrarRechazado(imagen.getNombre());
                    }
                    imagenesProcesadas.add(imagen);
                    controladas++;
                } else {
                    SistemaControlAcceso.colaImagenesSemaphore.acquire();
                    try {
                        colaImagenes.put(imagen);
                    } finally {
                        SistemaControlAcceso.colaImagenesSemaphore.release();
                    }
                }
                Thread.sleep(50);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("ControlAcceso interrumpido.");
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            latch.countDown();
        }
    }

    private synchronized void registrarRechazado(String nombre) {
        ManejadorArchivosGenerico.escribirArchivo("/Users/gastonbauer/Desktop/facultad/ssoo/obligatorio/src/main/java/com/example/Archivos/rechazados.txt", new String[] { nombre });
    }
}
