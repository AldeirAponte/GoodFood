package com.example.goodfood.activities;

import com.example.goodfood.BuildConfig;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.goodfood.R;
import com.example.goodfood.models.Plato;
import com.bumptech.glide.Glide;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public class AgregarPlatoActivity extends AppCompatActivity {

    private EditText etNombre, etDescripcion, etPrecio;
    private Spinner spinnerTipo;
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
        spinnerTipo = findViewById(R.id.spinnerTipo);
        imgPrevisualizacion = findViewById(R.id.imgPrevisualizacion);
        btnSeleccionar = findViewById(R.id.btnSeleccionarFoto);
        btnGuardar = findViewById(R.id.btnGuardarPlato);

        // Spinner con las categorías
        String[] opcionesCategorias = {"Seleccione una categoría...", "Vegano", "Celíacos", "Proteicos"};
        ArrayAdapter<String> adapterSpinner = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, opcionesCategorias);
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTipo.setAdapter(adapterSpinner);

        // Usamos Glide para mostrar la foto elegida.
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

        // controlamos que seleccione un tipo / categoria
        if (spinnerTipo.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Por favor, seleccione una categoría válida para el plato", Toast.LENGTH_SHORT).show();
            return;
        }

        String tipo = spinnerTipo.getSelectedItem().toString();

        double precio = Double.parseDouble(precioStr);

        Toast.makeText(this, "Subiendo imagen...", Toast.LENGTH_SHORT).show();

        MediaManager.get().upload(rutaFotoLocal)
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {}

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {}

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        String urlImagenCloudinary = (String) resultData.get("secure_url");

                        // Pasamos también la categoría elegida
                        subirPlatoAFirebase(nombre, descripcion, tipo, precio, urlImagenCloudinary);
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        runOnUiThread(() -> Toast.makeText(AgregarPlatoActivity.this, "Error al subir imagen: " + error.getDescription(), Toast.LENGTH_LONG).show());
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {}
                }).dispatch();
    }

    private void subirPlatoAFirebase(String nombre, String descripcion, String tipo, double precio, String urlCloudinary) {
        new Thread(() -> {
            String idPlato = mFirestore.collection("platos").document().getId();

            // LOGICA ALEATORIA
            Random random = new Random();

            // Rating aleatorio entre 4.0 y 5.0
            double ratingRandom = 4.0 + (1.0 * random.nextDouble());
            String ratingStr = String.format(Locale.US, "%.1f", ratingRandom);

            // Tiempo aleatorio entre 10, 15, 20 o 25 minutos
            int[] tiemposMinutos = {10, 15, 20, 25};
            int tiempoRandom = tiemposMinutos[random.nextInt(tiemposMinutos.length)];
            String tiempoStr = tiempoRandom + " MIN";

            // cramos el objeto nuevoPlato de la clase Plato
            Plato nuevoPlato = new Plato(idPlato, nombre, descripcion, tipo, ratingStr, tiempoStr, precio, urlCloudinary);

            mFirestore.collection("platos").document(idPlato)
                    .set(nuevoPlato)
                    .addOnSuccessListener(aVoid -> {
                        runOnUiThread(() -> {
                            Toast.makeText(AgregarPlatoActivity.this, "¡Plato guardado con éxito!", Toast.LENGTH_SHORT).show();
                            finish();
                        });
                    })
                    .addOnFailureListener(e -> {
                        runOnUiThread(() -> Toast.makeText(AgregarPlatoActivity.this, "Error al guardar en Firestore", Toast.LENGTH_SHORT).show());
                    });
        }).start();
    }
}