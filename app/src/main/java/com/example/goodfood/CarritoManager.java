package com.example.goodfood;

import java.util.HashMap;
import java.util.Map;

public class CarritoManager {
    private static CarritoManager instance;
    // Guarda: ID del plato -> Cantidad seleccionada
    private Map<String, Integer> itemsCarrito;

    private CarritoManager() {
        itemsCarrito = new HashMap<>();
    }

    public static synchronized CarritoManager getInstance() {
        if (instance == null) {
            instance = new CarritoManager();
        }
        return instance;
    }

    public void agregarProducto(String platoId, int cantidad) {
        if (itemsCarrito.containsKey(platoId)) {
            itemsCarrito.put(platoId, itemsCarrito.get(platoId) + cantidad);
        } else {
            itemsCarrito.put(platoId, cantidad);
        }
    }

    public int getCantidadTotal() {
        int total = 0;
        for (int cant : itemsCarrito.values()) {
            total += cant;
        }
        return total;
    }

    public Map<String, Integer> getItems() {
        return itemsCarrito;
    }

    public void vaciarCarrito() {
        itemsCarrito.clear();
    }
}