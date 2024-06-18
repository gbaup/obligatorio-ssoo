package com.example;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CopyOnWriteArrayList;

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
            int controladas = 0;
            while (cantidadImagenes == -1 || controladas < cantidadImagenes) {
                Imagen imagen = colaImagenes.take();

                if (imagen.isProcesada() && !imagenesProcesadas.contains(imagen)) {
                    if (imagen.isAccesoPermitido()) {
                        System.out.println("[" + Thread.currentThread().getName() + "] Acceso permitido para: " + imagen.getNombre() + " (VIP: " + imagen.isEsVIP() + ")");
                    } else {
                        System.out.println("[" + Thread.currentThread().getName() + "] Acceso denegado para: " + imagen.getNombre() + " (VIP: " + imagen.isEsVIP() + ")");
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
                Thread.sleep(100);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            latch.countDown();
        }
    }
}
