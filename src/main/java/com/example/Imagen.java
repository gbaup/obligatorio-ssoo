package com.example;

public class Imagen implements Comparable<Imagen> {
    private String nombre;
    private boolean esVIP;
    private boolean procesada;
    private boolean accesoPermitido;
    private long timestamp;
    private int id;

    public Imagen(boolean esVIP) {
        this.nombre = "imagen_" + System.currentTimeMillis();
        this.esVIP = esVIP;
        this.procesada = false;
        this.timestamp = System.currentTimeMillis();
        this.id = SistemaControlAcceso.idGenerator.incrementAndGet();
    }

    public Imagen(String nombre, boolean esVIP, int id) {
        this.nombre = nombre;
        this.esVIP = esVIP;
        this.procesada = false;
        this.timestamp = System.currentTimeMillis();
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public boolean isEsVIP() {
        return esVIP;
    }

    public boolean isProcesada() {
        return procesada;
    }

    public void setProcesada(boolean procesada) {
        this.procesada = procesada;
    }

    public boolean isAccesoPermitido() {
        return accesoPermitido;
    }

    public void setAccesoPermitido(boolean accesoPermitido) {
        this.accesoPermitido = accesoPermitido;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int getId() {
        return id;
    }

    @Override
    public int compareTo(Imagen otra) {
        if (this.esVIP == otra.esVIP) {
            return Integer.compare(this.id, otra.id);
        }
        return this.esVIP ? -1 : 1;
    }
}