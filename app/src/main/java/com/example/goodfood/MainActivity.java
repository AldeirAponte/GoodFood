package com.example.goodfood;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.goodfood.adapters.PlatoAdapter;
import com.example.goodfood.models.Plato;
import com.example.goodfood.activities.AgregarPlatoActivity;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TextView tvSaludo;
    private RecyclerView rvCatalogo;
    private PlatoAdapter adaptador;
    private List<Plato> listaPlatos;
    private com.google.android.material.floatingactionbutton.FloatingActionButton fabAgregarPlato;
    private String rolUsuario;
    private String nombreUsuario; // Variable única global

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // para ocultar la barra superior
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // 1. Enlazar componentes de la interfaz de usuario
        tvSaludo = findViewById(R.id.tvSaludo);
        rvCatalogo = findViewById(R.id.rvCatalogo);
        fabAgregarPlato = findViewById(R.id.fabAgregarPlato);

        // 2. Capturar los extras de forma limpia (Evitamos duplicación)
        if (getIntent().hasExtra("nombre_usuario")) {
            nombreUsuario = getIntent().getStringExtra("nombre_usuario");
        } else {
            nombreUsuario = "Cliente";
        }
        rolUsuario = getIntent().getStringExtra("rol_usuario");

        // Configurar el saludo en pantalla
        tvSaludo.setText("¡Hola, " + nombreUsuario + "!");

        // 3. Validación de Rol para Administrador
        if ("admin".equals(rolUsuario)) {
            fabAgregarPlato.setVisibility(View.VISIBLE);
        } else {
            fabAgregarPlato.setVisibility(View.GONE);
        }

        // Configurar el click para ir a la pantalla de agregar plato
        fabAgregarPlato.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AgregarPlatoActivity.class);
            startActivity(intent);
        });

        // 4. Configurar el RecyclerView
        rvCatalogo.setLayoutManager(new LinearLayoutManager(this));
        listaPlatos = new ArrayList<>();
        adaptador = new PlatoAdapter(listaPlatos);
        rvCatalogo.setAdapter(adaptador);

        // 5. Ejecución del hilo secundario para traer el menú
        //cargarMenu();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 🌟 Cada vez que el Admin vuelva de agregar un plato,
        // este metodo se dispara y recarga la lista automáticamente desde Firestore
        cargarMenu();
    }

    private void cargarMenu() {
        // Hilo secundario para buscar en internet sin congelar la app
        Thread cargarPlatos = new Thread(new Runnable() {
            @Override
            public void run() {
                // Consultamos directamente la base de datos real de Firestore
                com.google.firebase.firestore.FirebaseFirestore.getInstance()
                        .collection("platos")
                        .get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            // Volvemos al hilo principal (UI Thread) para actualizar el RecyclerView
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    listaPlatos.clear();

                                    // Si Firestore tiene platos reales cargados, los añade
                                    if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                                        for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                                            Plato plato = doc.toObject(Plato.class);
                                            if (plato != null) {
                                                listaPlatos.add(plato);
                                            }
                                        }
                                    } else {
                                        // PLAN DE RESPALDO: Si la base de datos está vacía, muestra platos locales con imágenes reales
                                        Toast.makeText(MainActivity.this, "Catálogo vacío. Cargando menú local.", Toast.LENGTH_SHORT).show();
                                        listaPlatos.add(new Plato("1", "Ensalada Caesar de Pollo", "Pollo premium, lechuga orgánica, croutons integrales.", 4500.0, "https://images.unsplash.com/photo-1512621776951-a57141f2eefd?w=500"));
                                        listaPlatos.add(new Plato("2", "Wrap Veggie de Palta", "Tortilla integral, palta, tomates cherry, espinaca.", 3800.0, "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=500"));
                                        listaPlatos.add(new Plato("3", "Wok de Fideos y Vegetales", "Mix de verduras salteadas con fideos integrales.", 4800.0, "https://images.unsplash.com/photo-1563379091339-03b21ab4a4f8?w=500"));
                                    }

                                    // Avisamos al adaptador que dibuje los cambios en la pantalla
                                    adaptador.notifyDataSetChanged();
                                }
                            });
                        })
                        .addOnFailureListener(e -> {
                            // Manejo seguro por si falla internet por completo
                            runOnUiThread(() -> Toast.makeText(MainActivity.this, "Error de red al cargar platos", Toast.LENGTH_SHORT).show());
                        });
            }
        });

        // Iniciamos el hilo secundario
        cargarPlatos.start();
    }
}