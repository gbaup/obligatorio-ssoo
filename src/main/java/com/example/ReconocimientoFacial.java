package com.example;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;


public class ReconocimientoFacial extends Thread {
    private final BlockingQueue<Imagen> colaImagenes;
    private final int cantidadImagenes;
    private final CountDownLatch latch;
    private final List<Long> tiemposDeProcesamiento;
    private final List<Long> tiemposDeEspera;

    public ReconocimientoFacial(BlockingQueue<Imagen> colaImagenes, int cantidadImagenes, CountDownLatch latch, List<Long> tiemposDeProcesamiento, List<Long> tiemposDeEspera) {
        this.colaImagenes = colaImagenes;
        this.cantidadImagenes = cantidadImagenes;
        this.latch = latch;
        this.tiemposDeProcesamiento = tiemposDeProcesamiento;
        this.tiemposDeEspera = tiemposDeEspera;
        this.setName("ReconocimientoFacial");
    }

    @Override
    public void run() {
        try {
            while (cantidadImagenes == -1 || tiemposDeProcesamiento.size() < cantidadImagenes) {
                Imagen imagen = colaImagenes.take();
                if (!imagen.isProcesada()) {
                    SistemaControlAcceso.procesamientoSemaphore.acquire();
                    try {
                        long tiempoInicioProcesamiento = System.currentTimeMillis();
                        long tiempoEnCola = tiempoInicioProcesamiento - imagen.getTimestamp();
                        tiemposDeEspera.add(tiempoEnCola);
                        System.out.println("[" + Thread.currentThread().getName() + "] Procesando imagen: " + imagen.getNombre() + " (VIP: " + imagen.isEsVIP() + ")");
                        // Simula tiempo de procesamiento entre 50 y 150 ms
                        int tiempoProcesamiento = 50 + (int) (Math.random() * 100); // Reducir tiempo de simulación
                        Thread.sleep(tiempoProcesamiento);
                        long tiempoFinProcesamiento = System.currentTimeMillis();
                        tiemposDeProcesamiento.add(tiempoFinProcesamiento - tiempoInicioProcesamiento);
                        boolean accesoPermitido = Math.random() > 0.2;
                        imagen.setProcesada(true);
                        imagen.setAccesoPermitido(accesoPermitido);

                        SistemaControlAcceso.colaImagenesSemaphore.acquire();
                        try {
                            colaImagenes.put(imagen);
                        } finally {
                            SistemaControlAcceso.colaImagenesSemaphore.release();
                        }
                    } finally {
                        SistemaControlAcceso.procesamientoSemaphore.release();
                    }
                } else {
                    SistemaControlAcceso.colaImagenesSemaphore.acquire();
                    try {
                        colaImagenes.put(imagen);
                    } finally {
                        SistemaControlAcceso.colaImagenesSemaphore.release();
                    }
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("ReconocimientoFacial interrumpido.");
        } finally {
            latch.countDown();
        }
    }
}