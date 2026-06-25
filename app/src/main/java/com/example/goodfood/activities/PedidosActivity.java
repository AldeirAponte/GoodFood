package com.example.goodfood.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.goodfood.R;
import com.example.goodfood.adapters.PedidosAdapter;
import com.example.goodfood.models.Plato;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PedidosActivity extends AppCompatActivity {

    private ImageView btnVolver;
    private RecyclerView rvPedidos;
    private TextView tvVacios;

    // 🌟 SOLUCIÓN 1: Inicializamos la lista acá mismo para evitar el NullPointerException
    private List<Map<String, Object>> listaDeMisPedidos = new ArrayList<>();
    private List<Plato> todosLosPlatos;
    private PedidosAdapter adaptador;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pedidos);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        btnVolver = findViewById(R.id.btnVolverPedidos);
        rvPedidos = findViewById(R.id.rvPedidos);
        tvVacios = findViewById(R.id.tvPedidosVacios);

        btnVolver.setOnClickListener(v -> finish());

        if (getIntent().hasExtra("lista_platos_catalogo")) {
            todosLosPlatos = (List<Plato>) getIntent().getSerializableExtra("lista_platos_catalogo");
        } else {
            todosLosPlatos = new ArrayList<>();
        }

        cargarMisPedidosDesdeFirebase();
    }

    private void cargarMisPedidosDesdeFirebase() {
        com.google.firebase.auth.FirebaseUser usuarioAuth = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
        if (usuarioAuth == null) {
            Toast.makeText(this, "Tenés que iniciar sesión para ver tus órdenes", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String miUid = usuarioAuth.getUid();
        com.google.firebase.firestore.FirebaseFirestore db = com.google.firebase.firestore.FirebaseFirestore.getInstance();

        db.collection("usuarios").document(miUid).get()
                .addOnSuccessListener(documentSnapshot -> {

                    final boolean esAdmin = documentSnapshot.exists() &&
                            "admin".equalsIgnoreCase(documentSnapshot.getString("rol"));

                    com.google.firebase.firestore.Query queryBase = db.collection("pedidos");

                    if (!esAdmin) {
                        queryBase = queryBase.whereEqualTo("clienteId", miUid);
                    }

                    queryBase.get()
                            .addOnSuccessListener(queryDocumentSnapshots -> {
                                // Ahora sí, .clear() funciona seguro porque la lista ya existe
                                listaDeMisPedidos.clear();

                                if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {

                                    // 🌟 SOLUCIÓN 2: REEMPLAZAMOS EL COMENTARIO POR EL BUCLE REAL QUE LLENA LOS PEDIDOS
                                    for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                                        Map<String, Object> datosPedido = doc.getData();
                                        if (datosPedido != null) {
                                            datosPedido.put("id_documento", doc.getId());
                                            listaDeMisPedidos.add(datosPedido);
                                        }
                                    }

                                    tvVacios.setVisibility(View.GONE);
                                    rvPedidos.setVisibility(View.VISIBLE);
                                } else {
                                    tvVacios.setText(esAdmin ? "📋 No hay pedidos globales en el sistema." : "📋 Todavía no realizaste ningún pedido.");
                                    tvVacios.setVisibility(View.VISIBLE);
                                    rvPedidos.setVisibility(View.GONE);
                                }

                                if (rvPedidos.getLayoutManager() == null) {
                                    rvPedidos.setLayoutManager(new LinearLayoutManager(PedidosActivity.this));
                                }

                                adaptador = new PedidosAdapter(listaDeMisPedidos, todosLosPlatos, esAdmin);
                                rvPedidos.setAdapter(adaptador);

                                if (adaptador != null) {
                                    adaptador.notifyDataSetChanged();
                                }
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(PedidosActivity.this, "Error al cargar órdenes", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(PedidosActivity.this, "Error al verificar permisos de usuario", Toast.LENGTH_SHORT).show();
                });
    }

    private boolean rolEqualsAdmin(com.google.firebase.firestore.DocumentSnapshot doc) {
        if(doc.exists()) {
            String r = doc.getString("rol");
            return r != null && r.equalsIgnoreCase("admin");
        }
        return false;
    }
}