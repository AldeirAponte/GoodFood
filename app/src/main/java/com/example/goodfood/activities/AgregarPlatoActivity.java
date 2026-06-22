package com.example.goodfood.activities;

import com.example.goodfood.BuildConfig;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.goodfood.R;
import com.example.goodfood.models.Plato;
import com.bumptech.glide.Glide; // 🌟 Esencial para que no crashee la previsualización
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AgregarPlatoActivity extends AppCompatActivity {

    private EditText etNombre, etDescripcion, etPrecio;
    private ImageView imgPrevisualizacion;
    private Button btnSeleccionar, btnGuardar;


    private FirebaseFirestore mFirestore;
    private Uri rutaFotoLocal = null;
    private ActivityResultLauncher<Intent> galeriaLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agregar_plato);

        mFirestore = FirebaseFirestore.getInstance();
        inicializarCloudinary();

        etNombre = findViewById(R.id.etNombreNuevo);
        etDescripcion = findViewById(R.id.etDescripcionNuevo);
        etPrecio = findViewById(R.id.etPrecioNuevo);
        imgPrevisualizacion = findViewById(R.id.imgPrevisualizacion);
        btnSeleccionar = findViewById(R.id.btnSeleccionarFoto);
        btnGuardar = findViewById(R.id.btnGuardarPlato);

        // 🌟 LA SOLUCIÓN AL CRASHEO: Usamos Glide para mostrar la foto elegida.
        // Al volver de la galería, Android no se rompe intentando procesar la URI pesada.
        galeriaLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        rutaFotoLocal = result.getData().getData();

                        if (rutaFotoLocal != null) {
                            Glide.with(AgregarPlatoActivity.this)
                                    .load(rutaFotoLocal)
                                    .into(imgPrevisualizacion);
                        }
                    }
                }
        );

        btnSeleccionar.setOnClickListener(v -> abrirGaleria());
        btnGuardar.setOnClickListener(v -> comenzarProcesoDeGuardado());
    }

    private void inicializarCloudinary() {
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", BuildConfig.CLOUDINARY_CLOUD_NAME);
        config.put("api_key", BuildConfig.CLOUDINARY_API_KEY);
        config.put("api_secret", BuildConfig.CLOUDINARY_API_SECRET);
        try {
            MediaManager.init(this, config);
        } catch (IllegalStateException e) {
            // Ya inicializado
        }
    }

    private void abrirGaleria() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        galeriaLauncher.launch(intent);
    }

    private void comenzarProcesoDeGuardado() {
        String nombre = etNombre.getText().toString().trim();
        String descripcion = etDescripcion.getText().toString().trim();
        String precioStr = etPrecio.getText().toString().trim();

        if (nombre.isEmpty() || descripcion.isEmpty() || precioStr.isEmpty() || rutaFotoLocal == null) {
            Toast.makeText(this, "Por favor, completa todos los campos y selecciona una foto", Toast.LENGTH_SHORT).show();
            return;
        }

        double precio = Double.parseDouble(precioStr);

        Toast.makeText(this, "Subiendo imagen a Cloudinary...", Toast.LENGTH_SHORT).show();

        MediaManager.get().upload(rutaFotoLocal)
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {}

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {}

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        String urlImagenCloudinary = (String) resultData.get("secure_url");

                        // Mantenemos tu flujo original intacto
                        subirPlatoAFirebase(nombre, descripcion, precio, urlImagenCloudinary);
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        runOnUiThread(() -> Toast.makeText(AgregarPlatoActivity.this, "Error al subir a Cloudinary: " + error.getDescription(), Toast.LENGTH_LONG).show());
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {}
                }).dispatch();
    }

    private void subirPlatoAFirebase(String nombre, String descripcion, double precio, String urlCloudinary) {
        new Thread(() -> {
            String idPlato = mFirestore.collection("platos").document().getId();

            // 🌟 Mantenemos tu constructor original de 5 atributos tal cual como en tu BD
            Plato nuevoPlato = new Plato(idPlato, nombre, descripcion, precio, urlCloudinary);

            mFirestore.collection("platos").document(idPlato)
                    .set(nuevoPlato)
                    .addOnSuccessListener(aVoid -> {
                        runOnUiThread(() -> {
                            Toast.makeText(AgregarPlatoActivity.this, "¡Plato e imagen guardados con éxito!", Toast.LENGTH_SHORT).show();
                            finish();
                        });
                    })
                    .addOnFailureListener(e -> {
                        runOnUiThread(() -> Toast.makeText(AgregarPlatoActivity.this, "Error al guardar en Firestore", Toast.LENGTH_SHORT).show());
                    });
        }).start();
    }
}