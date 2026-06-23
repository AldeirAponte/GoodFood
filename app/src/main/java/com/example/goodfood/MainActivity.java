package com.example.goodfood;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
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
    private List<Plato> listaPlatos;         // Contiene TODOS los platos de la BD
    private List<Plato> listaFiltrada;       // La que se muestra en pantalla

    private EditText etBuscar;
    private String categoriaActual = "Todos";

    // Botones de categorías
    private TextView btnCatTodos, btnCatVegano, btnCatCeliacos, btnCatProteicos;

    private com.google.android.material.floatingactionbutton.FloatingActionButton fabAgregarPlato;
    private String rolUsuario;
    private String nombreUsuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // 1. Enlazar componentes de la interfaz de usuario
        tvSaludo = findViewById(R.id.tvSaludo);
        rvCatalogo = findViewById(R.id.rvCatalogo);
        fabAgregarPlato = findViewById(R.id.fabAgregarPlato);
        etBuscar = findViewById(R.id.etBuscar); // Enlazamos el buscador

        // Enlazar botones del menú horizontal
        btnCatTodos = findViewById(R.id.btnCatTodos);
        btnCatVegano = findViewById(R.id.btnCatVegano);
        btnCatCeliacos = findViewById(R.id.btnCatCeliacos);
        btnCatProteicos = findViewById(R.id.btnCatProteicos);

        // 2. Capturar los extras
        if (getIntent().hasExtra("nombre_usuario") && getIntent().getStringExtra("nombre_usuario") != null) {
            nombreUsuario = getIntent().getStringExtra("nombre_usuario");
        } else {
            //Si el Intent perdió el extra, se lo pedimos directo a Firebase
            com.google.firebase.auth.FirebaseUser usuarioActual = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
            if (usuarioActual != null && usuarioActual.getDisplayName() != null && !usuarioActual.getDisplayName().isEmpty()) {
                nombreUsuario = usuarioActual.getDisplayName();
            } else {
                nombreUsuario = "Cliente"; // Último recurso si no hay nadie logueado
            }
        }

        // misma logica en rol
        if (getIntent().hasExtra("rol_usuario") && getIntent().getStringExtra("rol_usuario") != null) {
            rolUsuario = getIntent().getStringExtra("rol_usuario");
        } else {
            rolUsuario = "cliente";
        }

        tvSaludo.setText("¡Hola, " + nombreUsuario + "!");

        // 3. Validación de Rol para Administrador
        if ("admin".equals(rolUsuario)) {
            fabAgregarPlato.setVisibility(View.VISIBLE);
        } else {
            fabAgregarPlato.setVisibility(View.GONE);
        }

        fabAgregarPlato.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AgregarPlatoActivity.class);
            startActivity(intent);
        });

        // 4. Configurar el RecyclerView con la lista filtrada
        rvCatalogo.setLayoutManager(new LinearLayoutManager(this));
        listaPlatos = new ArrayList<>();
        listaFiltrada = new ArrayList<>();

        adaptador = new PlatoAdapter(listaFiltrada);
        rvCatalogo.setAdapter(adaptador);

        // 5. Configurar los listeners para cada botón de categoría
        btnCatTodos.setOnClickListener(v -> {
            categoriaActual = "Todos";
            aplicarFiltrosCombinados();
        });
        btnCatVegano.setOnClickListener(v -> {
            categoriaActual = "Vegano";
            aplicarFiltrosCombinados();
        });
        btnCatCeliacos.setOnClickListener(v -> {
            categoriaActual = "Celíacos";
            aplicarFiltrosCombinados();
        });
        btnCatProteicos.setOnClickListener(v -> {
            categoriaActual = "Proteicos";
            aplicarFiltrosCombinados();
        });

        // 6. CONFIGURAR EL BUSCADOR EN TIEMPO REAL
        etBuscar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Cada vez que el usuario escribe o borra una letra, se ejecuta esto de forma instantánea
                aplicarFiltrosCombinados();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // 🌟 7. EN ESTA PARTE EXACTA VA LA BARRA DE NAVEGACIÓN INFERIOR (Al final del onCreate)
        com.google.android.material.bottomnavigation.BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);

        // Dejar seleccionado "Home" por defecto al arrancar
        bottomNavigation.setSelectedItemId(R.id.nav_home);

        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                return true;
            } else if (id == R.id.nav_search) {
                etBuscar.requestFocus(); // Manda el foco al buscador si tocan la lupa
                return true;
            } else if (id == R.id.nav_orders) {
                Toast.makeText(this, "Abriendo Mis Pedidos...", Toast.LENGTH_SHORT).show();
                return true;
            } else if (id == R.id.nav_profile) {
                Toast.makeText(this, "Perfil de " + nombreUsuario, Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });
    } // En esta llave cierra el onCreate

    @Override
    protected void onResume() {
        super.onResume();
        cargarMenu();
    }

    private void cargarMenu() {
        Thread cargarPlatos = new Thread(new Runnable() {
            @Override
            public void run() {
                com.google.firebase.firestore.FirebaseFirestore.getInstance()
                        .collection("platos")
                        .get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    listaPlatos.clear();

                                    if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                                        for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                                            Plato plato = doc.toObject(Plato.class);
                                            if (plato != null) {
                                                listaPlatos.add(plato);
                                            }
                                        }
                                    } else {
                                        Toast.makeText(MainActivity.this, "Catálogo vacío. Cargando menú local.", Toast.LENGTH_SHORT).show();
                                        listaPlatos.add(new Plato("1", "Ensalada Caesar de Pollo", "Pollo premium, lechuga orgánica, croutons integrales.", "Vegano", "4.7", "10 MIN", 4500.0, "https://images.unsplash.com/photo-1512621776951-a57141f2eefd?w=500"));
                                        listaPlatos.add(new Plato("2", "Wrap Veggie de Palta", "Tortilla integral, palta, tomates cherry, espinaca.", "Celíacos", "4.6", "15 MIN", 3800.0, "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=500"));
                                        listaPlatos.add(new Plato("3", "Wok de Fideos y Vegetales", "Mix de verduras salteadas con fideos integrales.", "Proteicos", "4.8", "20 MIN", 4800.0, "https://images.unsplash.com/photo-1563379091339-03b21ab4a4f8?w=500"));
                                    }

                                    // Al cargar los datos por primera vez, aplicamos el filtro inicial
                                    aplicarFiltrosCombinados();
                                }
                            });
                        })
                        .addOnFailureListener(e -> {
                            runOnUiThread(() -> Toast.makeText(MainActivity.this, "Error de red al cargar platos", Toast.LENGTH_SHORT).show());
                        });
            }
        });
        cargarPlatos.start();
    }

    // Combina de forma inteligente el filtro del botón y el texto del buscador
    private void aplicarFiltrosCombinados() {
        listaFiltrada.clear();
        String textoBusqueda = etBuscar.getText().toString().toLowerCase().trim();

        for (Plato p : listaPlatos) {
            // Paso A: Validar si coincide con la categoría seleccionada en los botones
            boolean coincideCategoria = categoriaActual.equals("Todos") ||
                    (p.getTipo() != null && p.getTipo().equalsIgnoreCase(categoriaActual));

            // Paso B: Validar si coincide con lo escrito en el buscador (busca en nombre y en descripción)
            boolean coincideTexto = textoBusqueda.isEmpty() ||
                    (p.getNombre() != null && p.getNombre().toLowerCase().contains(textoBusqueda)) ||
                    (p.getDescripcion() != null && p.getDescripcion().toLowerCase().contains(textoBusqueda));

            // Si cumple ambas condiciones, entra al catálogo visible
            if (coincideCategoria && coincideTexto) {
                listaFiltrada.add(p);
            }
        }

        // Refrescar la pantalla
        adaptador.notifyDataSetChanged();

        // Actualizar el color del botón activo
        actualizarEstiloBotones(categoriaActual);
    }

    private void actualizarEstiloBotones(String categoriaSeleccionada) {
        int colorInactivoFondo = android.graphics.Color.parseColor("#A3F5C9");
        int colorInactivoTexto = android.graphics.Color.parseColor("#0A4D34");

        btnCatTodos.setBackgroundTintList(android.content.res.ColorStateList.valueOf(colorInactivoFondo));
        btnCatTodos.setTextColor(colorInactivoTexto);

        btnCatVegano.setBackgroundTintList(android.content.res.ColorStateList.valueOf(colorInactivoFondo));
        btnCatVegano.setTextColor(colorInactivoTexto);

        btnCatCeliacos.setBackgroundTintList(android.content.res.ColorStateList.valueOf(colorInactivoFondo));
        btnCatCeliacos.setTextColor(colorInactivoTexto);

        btnCatProteicos.setBackgroundTintList(android.content.res.ColorStateList.valueOf(colorInactivoFondo));
        btnCatProteicos.setTextColor(colorInactivoTexto);

        int colorActivoFondo = android.graphics.Color.parseColor("#0A4D34");
        int colorActivoTexto = android.graphics.Color.parseColor("#FFFFFF");

        switch (categoriaSeleccionada) {
            case "Todos":
                btnCatTodos.setBackgroundTintList(android.content.res.ColorStateList.valueOf(colorActivoFondo));
                btnCatTodos.setTextColor(colorActivoTexto);
                break;
            case "Vegano":
                btnCatVegano.setBackgroundTintList(android.content.res.ColorStateList.valueOf(colorActivoFondo));
                btnCatVegano.setTextColor(colorActivoTexto);
                break;
            case "Celíacos":
                btnCatCeliacos.setBackgroundTintList(android.content.res.ColorStateList.valueOf(colorActivoFondo));
                btnCatCeliacos.setTextColor(colorActivoTexto);
                break;
            case "Proteicos":
                btnCatProteicos.setBackgroundTintList(android.content.res.ColorStateList.valueOf(colorActivoFondo));
                btnCatProteicos.setTextColor(colorActivoTexto);
                break;
        }
    }
}