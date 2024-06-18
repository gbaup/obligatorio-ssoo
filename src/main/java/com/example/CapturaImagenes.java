package com.example;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;

public class CapturaImagenes extends Thread {
    private final BlockingQueue<Imagen> colaImagenes;
    private final int cantidadImagenes;
    private final int intervaloCaptura;
    private final CountDownLatch latch;

    public CapturaImagenes(BlockingQueue<Imagen> colaImagenes, int cantidadImagenes, int intervaloCaptura, CountDownLatch latch) {
        this.colaImagenes = colaImagenes;
        this.cantidadImagenes = cantidadImagenes;
        this.intervaloCaptura = intervaloCaptura;
        this.latch = latch;
        this.setName("CapturaImagenes");
    }

    @Override
    public void run() {
        try {
            for (int i = 0; i < cantidadImagenes || cantidadImagenes == -1; i++) {
                boolean esVIP = Math.random() < 0.1;
                Imagen imagenCapturada = new Imagen(esVIP);

                SistemaControlAcceso.colaImagenesSemaphore.acquire();
                try {
                    colaImagenes.put(imagenCapturada);
                    System.out.println("[" + Thread.currentThread().getName() + "] Capturando imagen: " + imagenCapturada.getNombre() + " (VIP: " + esVIP + ")");
                } finally {
                    SistemaControlAcceso.colaImagenesSemaphore.release();
                }

                Thread.sleep(intervaloCaptura);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            latch.countDown();
        }
    }
}