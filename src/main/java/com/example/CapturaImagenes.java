package com.example;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;

public class CapturaImagenes extends Thread {
    private  BlockingQueue<Imagen> colaPrioridad;
    private  BlockingQueue<Imagen> colaProcesamiento;
    private  CountDownLatch latch;
    private  int intervaloCaptura;

    public CapturaImagenes(BlockingQueue<Imagen> colaPrioridad, BlockingQueue<Imagen> colaProcesamiento, CountDownLatch latch,int intervaloCaptura) {
        this.colaPrioridad = colaPrioridad;
        this.colaProcesamiento = colaProcesamiento;
        this.latch = latch;
        this.intervaloCaptura = intervaloCaptura;
        this.setName("CapturaImagenes");
    }

    public void siCapturaImagenes(BlockingQueue<Imagen> colaPrioridad, BlockingQueue<Imagen> colaProcesamiento, CountDownLatch latch, int intervaloCaptura) {
        this.colaPrioridad = colaPrioridad;
        this.colaProcesamiento = colaProcesamiento;
        this.latch = latch;
        this.intervaloCaptura = intervaloCaptura;
        this.setName("CapturaImagenes");
    }

    @Override
    public void run() {
        try {
            while (!colaPrioridad.isEmpty()) {
                Imagen imagen = colaPrioridad.take();
                colaProcesamiento.put(imagen);
                System.out.println("[" + Thread.currentThread().getName() + "] Capturando imagen: " + imagen.getNombre() + " (VIP: " + imagen.isEsVIP() + ")");
                Thread.sleep(intervaloCaptura); 
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            latch.countDown();
        }
    }
}
