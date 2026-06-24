package com.example.goodfood.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.goodfood.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class PerfilActivity extends AppCompatActivity {

    private TextView tvNombre, tvEmail;
    private EditText etDireccion, etTelefono;
    private ImageView btnEditarDireccion, btnEditarTelefono, btnVolver;
    private Button btnGuardar, btnCerrarSesion;
    private ImageView ivRolIcono;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String usuarioId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Inicializar Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        // Enlazar vistas
        tvNombre = findViewById(R.id.tvPerfilNombre);
        tvEmail = findViewById(R.id.tvPerfilEmail);
        btnVolver = findViewById(R.id.btnVolver);
        etDireccion = findViewById(R.id.etPerfilDireccion);
        etTelefono = findViewById(R.id.etPerfilTelefono);
        ivRolIcono = findViewById(R.id.ivPerfilRolIcono);
        btnGuardar = findViewById(R.id.btnGuardarDatos);
        btnCerrarSesion = findViewById(R.id.btnCerrarSesion);

        // Enlazar los nuevos botones de lápiz
        btnEditarDireccion = findViewById(R.id.btnEditarDireccion);
        btnEditarTelefono = findViewById(R.id.btnEditarTelefono);

        if (user != null) {
            usuarioId = user.getUid();
            tvEmail.setText(user.getEmail());

            // Primero ponemos el nombre de Firebase Auth como auxilio temporal
            if (user.getDisplayName() != null && !user.getDisplayName().isEmpty()) {
                tvNombre.setText(user.getDisplayName());
            } else {
                tvNombre.setText("Cargando...");
            }

            // Cargar datos guardados previamente en Firestore (si existen)
            cargarDatosGuardados();
        }

        // Lógica de los Lápices de Edición
        btnEditarDireccion.setOnClickListener(v -> {
            etDireccion.setEnabled(true); // Habilitamos escritura
            etDireccion.requestFocus();   // Llevamos el cursor ahí
            btnGuardar.setVisibility(View.VISIBLE); // Mostramos botón guardar
        });

        btnEditarTelefono.setOnClickListener(v -> {
            etTelefono.setEnabled(true);  // Habilitamos escritura
            etTelefono.requestFocus();    // Llevamos el cursor ahí
            btnGuardar.setVisibility(View.VISIBLE); // Mostramos botón guardar
        });

        // Listener para Guardar Datos
        btnGuardar.setOnClickListener(v -> guardarDatosEnFirestore());

        // Listener para Cerrar Sesión
        btnCerrarSesion.setOnClickListener(v -> cerrarSesion());

        // Botón Volver
        btnVolver.setOnClickListener(v -> finish());
    }

    private void cargarDatosGuardados() {
        db.collection("usuarios").document(usuarioId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // 1. Cargamos el Nombre real desde Firestore
                        String nombreFirestore = documentSnapshot.getString("nombre");
                        if (nombreFirestore != null && !nombreFirestore.isEmpty()) {
                            tvNombre.setText(nombreFirestore);
                        }

                        // 2. Cargamos el Rol (Admin / Cliente) y lo sumamos al diseño
                        String rol = documentSnapshot.getString("rol");
                        if (rol != null) {
                            ivRolIcono.setVisibility(View.VISIBLE); // Hacemos visible el icono de rol

                            if (rol.equalsIgnoreCase("admin") || rol.equalsIgnoreCase("administrador")) {
                                // Setea el icono nativo de herramientas/configuración para el Administrador
                                ivRolIcono.setImageResource(android.R.drawable.ic_menu_preferences);
                                // Le aplicamos un color verde oscuro que combine con GoodFood
                                ivRolIcono.setColorFilter(android.graphics.Color.parseColor("#0A4D34"));
                            } else {
                                // Setea el icono nativo de persona/cuenta para el Cliente estándar
                                ivRolIcono.setImageResource(android.R.drawable.ic_menu_myplaces);
                                // Le aplicamos un color verde más claro/brillante
                                ivRolIcono.setColorFilter(android.graphics.Color.parseColor("#2E7D32"));
                            }
                        }

                        // 3. Cargamos los campos de texto normales
                        String direccion = documentSnapshot.getString("direccion");
                        String telefono = documentSnapshot.getString("telefono");
                        if (direccion != null) etDireccion.setText(direccion);
                        if (telefono != null) etTelefono.setText(telefono);
                    }
                });
    }

    private void guardarDatosEnFirestore() {
        String direccion = etDireccion.getText().toString().trim();
        String telefono = etTelefono.getText().toString().trim();

        Map<String, Object> datosUsuario = new HashMap<>();
        datosUsuario.put("direccion", direccion);
        datosUsuario.put("telefono", telefono);

        db.collection("usuarios").document(usuarioId).set(datosUsuario, com.google.firebase.firestore.SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(PerfilActivity.this, "Datos guardados correctamente", Toast.LENGTH_SHORT).show();

                    // Dejar la interfaz limpia otra vez
                    etDireccion.setEnabled(false); // Volver a bloquear
                    etTelefono.setEnabled(false);  // Volver a bloquear
                    btnGuardar.setVisibility(View.GONE); // Desaparecer el botón guardar de forma estética
                })
                .addOnFailureListener(e -> Toast.makeText(PerfilActivity.this, "Error al guardar los datos", Toast.LENGTH_SHORT).show());
    }

    private void cerrarSesion() {
        mAuth.signOut();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build();
        GoogleSignIn.getClient(this, gso).signOut().addOnCompleteListener(task -> {
            Toast.makeText(PerfilActivity.this, "Sesión cerrada", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(PerfilActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}