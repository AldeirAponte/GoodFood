package com.example.goodfood.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.goodfood.CarritoManager;
import com.example.goodfood.R;
import com.example.goodfood.adapters.CarritoAdapter;
import com.example.goodfood.models.Plato;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CarritoActivity extends AppCompatActivity {

    private ImageView btnVolver;
    private RecyclerView rvCarrito;
    private TextView tvTotal;
    private Button btnConfirmar;
    private TextView tvCarritoVacio;

    private List<Plato> todosLosPlatos;
    private CarritoAdapter adaptador;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_carrito);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        btnVolver = findViewById(R.id.btnVolverCarrito);
        rvCarrito = findViewById(R.id.rvCarrito);
        tvTotal = findViewById(R.id.tvTotalCarrito);
        btnConfirmar = findViewById(R.id.btnConfirmarPedido);
        tvCarritoVacio = findViewById(R.id.tvCarritoVacio);

        btnVolver.setOnClickListener(v -> finish());

        if (getIntent().hasExtra("lista_platos_catalogo")) {
            todosLosPlatos = (List<Plato>) getIntent().getSerializableExtra("lista_platos_catalogo");
        } else {
            todosLosPlatos = new ArrayList<>();
        }

        rvCarrito.setLayoutManager(new LinearLayoutManager(this));
        Map<String, Integer> items = CarritoManager.getInstance().getItems();

        // Lógica de control de pantalla vacía
        if (items.isEmpty()) {
            mostrarPantallaVacia();
        } else {
            tvCarritoVacio.setVisibility(View.GONE);
            rvCarrito.setVisibility(View.VISIBLE);
            btnConfirmar.setEnabled(true);
            btnConfirmar.setAlpha(1.0f);

            // CAMBIO 1: Le pasamos "this::verificarEstadoCarrito" como tercer parámetro
            adaptador = new CarritoAdapter(items, todosLosPlatos, this::verificarEstadoCarrito);
            rvCarrito.setAdapter(adaptador);
        }

        btnConfirmar.setOnClickListener(v -> {
            if (CarritoManager.getInstance().getCantidadTotal() > 0) {
                // Deshabilitamos temporalmente para evitar doble clic erróneo
                btnConfirmar.setEnabled(false);
                btnConfirmar.setText("Procesando...");

                // CAMBIO 2: Solo llamamos a la función de Firebase.
                // Los Toast y el finish() se ejecutan dentro de ella cuando el servidor responde ".addOnSuccessListener"
                guardarPedidoEnFirebase();
            } else {
                Toast.makeText(this, "El carrito está vacío", Toast.LENGTH_SHORT).show();
            }
        });

        calcularTotalReal();
    }

    private void guardarPedidoEnFirebase() {
        // 1. Obtener el usuario actual logueado para saber quién compra
        com.google.firebase.auth.FirebaseUser usuarioAuth = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
        String clienteId = (usuarioAuth != null) ? usuarioAuth.getUid() : "anonimo";
        String nombreCliente = (usuarioAuth != null && usuarioAuth.getDisplayName() != null) ? usuarioAuth.getDisplayName() : "Cliente GoodFood";

        // 2. Calcular el total a pagar actual para guardarlo en el documento
        double total = 0;
        Map<String, Integer> items = CarritoManager.getInstance().getItems();
        for (Map.Entry<String, Integer> entry : items.entrySet()) {
            for (Plato p : todosLosPlatos) {
                if ((p.getId() != null && p.getId().equals(entry.getKey())) || (p.getNombre() != null && p.getNombre().equals(entry.getKey()))) {
                    total += (p.getPrecio() * entry.getValue());
                    break;
                }
            }
        }

        // 3. Crear el mapa estructurado con los datos del pedido
        Map<String, Object> pedido = new java.util.HashMap<>();
        pedido.put("clienteId", clienteId);
        pedido.put("nombreCliente", nombreCliente);
        pedido.put("items", items); // Guarda: {"id_del_plato": cantidad}
        pedido.put("total", total);
        pedido.put("estado", "pendiente"); // Estado inicial del pedido
        pedido.put("fecha", com.google.firebase.firestore.FieldValue.serverTimestamp());

        // 4. Subir a la colección "pedidos" en Firestore
        com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("pedidos")
                .add(pedido)
                .addOnSuccessListener(documentReference -> {
                    // mensaje de exito!!
                    Toast.makeText(CarritoActivity.this, "¡Pedido éxitoso!", Toast.LENGTH_LONG).show();
                    // Vaciamos el changuito local
                    CarritoManager.getInstance().vaciarCarrito();
                    finish();
                })
                .addOnFailureListener(e -> {
                    // Si hay un error de red o permisos:
                    Toast.makeText(CarritoActivity.this, "Error al procesar el pedido: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    // Re-activamos el botón para que intente de nuevo
                    btnConfirmar.setEnabled(true);
                    btnConfirmar.setText("Confirmar Pedido");
                });
    }

    private void calcularTotalReal() {
        double total = 0;
        Map<String, Integer> items = CarritoManager.getInstance().getItems();

        for (Map.Entry<String, Integer> entry : items.entrySet()) {
            String id = entry.getKey();
            int cantidad = entry.getValue();

            for (Plato p : todosLosPlatos) {
                if ((p.getId() != null && p.getId().equals(id)) || (p.getNombre() != null && p.getNombre().equals(id))) {
                    total += (p.getPrecio() * cantidad);
                    break;
                }
            }
        }

        tvTotal.setText(String.format(Locale.US, "$%.2f", total));
    }

    // Este metodo se ejecuta automáticamente cada vez que el usuario toca Restar o Eliminar
    private void verificarEstadoCarrito() {
        Map<String, Integer> items = CarritoManager.getInstance().getItems();

        // Recalcular el monto de la tarjeta de abajo
        calcularTotalReal();

        // Si borro los platos del carrito
        if (items.isEmpty()) {
            mostrarPantallaVacia();
        }
    }

    private void mostrarPantallaVacia() {
        tvCarritoVacio.setVisibility(View.VISIBLE);
        rvCarrito.setVisibility(View.GONE);
        btnConfirmar.setEnabled(false);
        btnConfirmar.setAlpha(0.5f);
        tvTotal.setText("$0.00");
    }
}