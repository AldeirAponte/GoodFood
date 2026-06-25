package com.example.goodfood.models;

// 🌟 Al agregar "implements java.io.Serializable", Java ya sabe cómo empaquetar
// este objeto para mandarlo entre pantallas a través de los Intents.
public class Plato implements java.io.Serializable {
    private String id;
    private String nombre;
    private String descripcion;
    private String tipo;
    private String rating;
    private String tiempo;
    private double precio;
    private String urlImagen;

    // Constructor vacío obligatorio para Firebase
    public Plato() {}

    // Constructor completo
    public Plato(String id, String nombre, String descripcion, String tipo, String rating, String tiempo, double precio, String urlImagen) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.tipo = tipo;
        this.rating = rating;
        this.tiempo = tiempo;
        this.precio = precio;
        this.urlImagen = urlImagen;
    }

    // Getters y Setters (quedan exactamente iguales)
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getRating() { return rating; }
    public void setRating(String rating) { this.rating = rating; }

    public String getTiempo() { return tiempo; }
    public void setTiempo(String tiempo) { this.tiempo = tiempo; }

    public double getPrecio() { return precio; }
    public void setPrecio(double precio) { this.precio = precio; }

    public String getUrlImagen() { return urlImagen; }
    public void setUrlImagen(String urlImagen) { this.urlImagen = urlImagen; }
}