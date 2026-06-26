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
import com.example.goodfood.activities.PerfilActivity;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TextView tvSaludo;
    private RecyclerView rvCatalogo;
    private PlatoAdapter adaptador;
    private List<Plato> listaPlatos;  // Contiene TODOS los platos de la BD
    private List<Plato> listaFiltrada; // La que se muestra en pantalla

    private EditText etBuscar;
    private String categoriaActual = "Todos";

    // Botones de categorías
    private TextView btnCatTodos, btnCatVegano, btnCatCeliacos, btnCatProteicos;

    // variables para el carrito
    private View btnVerCarrito;
    private TextView tvContadorCarrito;

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

        // Enlazamos los componentes de la interfaz de usuario
        tvSaludo = findViewById(R.id.tvSaludo);
        rvCatalogo = findViewById(R.id.rvCatalogo);
        fabAgregarPlato = findViewById(R.id.fabAgregarPlato);
        etBuscar = findViewById(R.id.etBuscar);

        btnVerCarrito = findViewById(R.id.btnVerCarrito);
        tvContadorCarrito = findViewById(R.id.tvContadorCarrito);

        btnCatTodos = findViewById(R.id.btnCatTodos);
        btnCatVegano = findViewById(R.id.btnCatVegano);
        btnCatCeliacos = findViewById(R.id.btnCatCeliacos);
        btnCatProteicos = findViewById(R.id.btnCatProteicos);

        // capturamos los intent
        if (getIntent().hasExtra("nombre_usuario") && getIntent().getStringExtra("nombre_usuario") != null) {
            nombreUsuario = getIntent().getStringExtra("nombre_usuario");
        } else {
            com.google.firebase.auth.FirebaseUser usuarioActual = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
            if (usuarioActual != null && usuarioActual.getDisplayName() != null && !usuarioActual.getDisplayName().isEmpty()) {
                nombreUsuario = usuarioActual.getDisplayName();
            } else {
                nombreUsuario = "Cliente";
            }
        }

        if (getIntent().hasExtra("rol_usuario") && getIntent().getStringExtra("rol_usuario") != null) {
            rolUsuario = getIntent().getStringExtra("rol_usuario");
        } else {
            rolUsuario = "cliente";
        }

        tvSaludo.setText("¡Hola, " + nombreUsuario + "!");

        // validar rol para Administrador
        if ("admin".equals(rolUsuario)) {
            fabAgregarPlato.setVisibility(View.VISIBLE);
        } else {
            fabAgregarPlato.setVisibility(View.GONE);
        }

        fabAgregarPlato.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AgregarPlatoActivity.class);
            startActivity(intent);
        });

        // ir al carrito de compras
        btnVerCarrito.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, com.example.goodfood.activities.CarritoActivity.class);
            intent.putExtra("lista_platos_catalogo", new ArrayList<>(listaPlatos));
            startActivity(intent);
        });

        // mostramos la lists filtrada
        rvCatalogo.setLayoutManager(new LinearLayoutManager(this));
        listaPlatos = new ArrayList<>();
        listaFiltrada = new ArrayList<>();

        adaptador = new PlatoAdapter(listaFiltrada, rolUsuario);
        rvCatalogo.setAdapter(adaptador);

        // logica de botones para cada categoría
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

        // logica para el buscador
        etBuscar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                aplicarFiltrosCombinados();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // barra inferior con los botones
        com.google.android.material.bottomnavigation.BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);
        bottomNavigation.setSelectedItemId(R.id.nav_home);

        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                return true;
            } else if (id == R.id.nav_search) {
                // activamos el buscador de arriba
                etBuscar.requestFocus();

                // desplegamos el teclado
                android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.showSoftInput(etBuscar, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
                }
                return true;
            } else if (id == R.id.nav_orders) {
                Intent intent = new Intent(MainActivity.this, com.example.goodfood.activities.PedidosActivity.class);
                intent.putExtra("lista_platos_catalogo", new ArrayList<>(listaPlatos));
                startActivity(intent);
                return true;
            } else if (id == R.id.nav_profile) {
                Intent intent = new Intent(MainActivity.this, PerfilActivity.class);
                startActivity(intent);
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarMenu();
        actualizarInterfazCarrito();
        com.google.android.material.bottomnavigation.BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);
        if (bottomNavigation != null) {
            bottomNavigation.setSelectedItemId(R.id.nav_home);
        }
    }

    // contador de platos en el carrito de compras
    public void actualizarInterfazCarrito() {
        int cantidadTotal = CarritoManager.getInstance().getCantidadTotal();
        if (cantidadTotal > 0) {
            tvContadorCarrito.setText(String.valueOf(cantidadTotal));
            tvContadorCarrito.setVisibility(View.VISIBLE);
        } else {
            tvContadorCarrito.setVisibility(View.GONE);
        }
    }

    // cargamos el menu
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
                                                if (plato.getId() == null || plato.getId().isEmpty()) {
                                                    plato.setId(doc.getId());
                                                }
                                                listaPlatos.add(plato);
                                            }
                                        }
                                    } else {
                                        Toast.makeText(MainActivity.this, "Catálogo vacío. Cargando menú local.", Toast.LENGTH_SHORT).show();
                                        listaPlatos.add(new Plato("1", "Ensalada Caesar de Pollo", "Pollo premium, lechuga orgánica, croutons integrales.", "Vegano", "4.7", "10 MIN", 4500.0, "https://images.unsplash.com/photo-1512621776951-a57141f2eefd?w=500"));
                                        listaPlatos.add(new Plato("2", "Wrap Veggie de Palta", "Tortilla integral, palta, tomates cherry, espinaca.", "Celíacos", "4.6", "15 MIN", 3800.0, "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=500"));
                                        listaPlatos.add(new Plato("3", "Wok de Fideos y Vegetales", "Mix de verduras salteadas con fideos integrales.", "Proteicos", "4.8", "20 MIN", 4800.0, "https://images.unsplash.com/photo-1563379091339-03b21ab4a4f8?w=500"));
                                    }
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

    private void aplicarFiltrosCombinados() {
        listaFiltrada.clear();
        String textoBusqueda = etBuscar.getText().toString().toLowerCase().trim();

        for (Plato p : listaPlatos) {
            boolean coincideCategoria = categoriaActual.equals("Todos") ||
                    (p.getTipo() != null && p.getTipo().equalsIgnoreCase(categoriaActual));

            boolean coincideTexto = textoBusqueda.isEmpty() ||
                    (p.getNombre() != null && p.getNombre().toLowerCase().contains(textoBusqueda)) ||
                    (p.getDescripcion() != null && p.getDescripcion().toLowerCase().contains(textoBusqueda));

            if (coincideCategoria && coincideTexto) {
                listaFiltrada.add(p);
            }
        }

        adaptador.notifyDataSetChanged();
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