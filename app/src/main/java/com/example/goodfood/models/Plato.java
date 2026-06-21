package com.example.goodfood.models;

public class Plato {
    private String id;
    private String nombre;
    private String descripcion;
    private double precio;
    private String urlImagen; // Para cuando le metamos fotos reales

    // Constructor vacío obligatorio para que Firebase pueda mapear los datos
    public Plato() {}

    // Constructor completo
    public Plato(String id, String nombre, String descripcion, double precio, String urlImagen) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.urlImagen = urlImagen;
    }

    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public double getPrecio() { return precio; }
    public void setPrecio(double precio) { this.precio = precio; }

    public String getUrlImagen() { return urlImagen; }
    public void setUrlImagen(String urlImagen) { this.urlImagen = urlImagen; }
}