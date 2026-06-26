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

    // variables para editar los platos
    private Plato platoEdicion = null;
    private boolean esModoEdicion = false;

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

        // DETECTAMOS SI NOS ENVIARON UN PLATO PARA EDITAR
        if (getIntent().hasExtra("plato_a_editar")) {
            platoEdicion = (Plato) getIntent().getSerializableExtra("plato_a_editar");
            if (platoEdicion != null) {
                esModoEdicion = true;
                autocompletarCamposEdicion(opcionesCategorias);
            }
        }

        btnSeleccionar.setOnClickListener(v -> abrirGaleria());
        btnGuardar.setOnClickListener(v -> comenzarProcesoDeGuardado());
    }

    // METODO AUXILIAR PARA AUTOCOMPLETAR LOS CAMPOS SI ES EDICIÓN
    private void autocompletarCamposEdicion(String[] opcionesCategorias) {
        etNombre.setText(platoEdicion.getNombre());
        etDescripcion.setText(platoEdicion.getDescripcion());
        etPrecio.setText(String.valueOf(platoEdicion.getPrecio()));
        btnGuardar.setText("Actualizar Plato");

        // Seleccionar la categoría correcta en el Spinner
        if (platoEdicion.getTipo() != null) {
            for (int i = 0; i < opcionesCategorias.length; i++) {
                if (opcionesCategorias[i].equalsIgnoreCase(platoEdicion.getTipo())) {
                    spinnerTipo.setSelection(i);
                    break;
                }
            }
        }

        // Cargar la foto actual de Cloudinary en la previsualización
        if (platoEdicion.getUrlImagen() != null && !platoEdicion.getUrlImagen().isEmpty()) {
            Glide.with(this)
                    .load(platoEdicion.getUrlImagen())
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .into(imgPrevisualizacion);
        }
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

        // Permitimos que la rutaFotoLocal sea null (significa que deja la foto vieja)
        if (nombre.isEmpty() || descripcion.isEmpty() || precioStr.isEmpty() || (!esModoEdicion && rutaFotoLocal == null)) {
            Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        if (spinnerTipo.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Por favor, seleccione una categoría válida para el plato", Toast.LENGTH_SHORT).show();
            return;
        }

        String tipo = spinnerTipo.getSelectedItem().toString();
        double precio = Double.parseDouble(precioStr);

        // VERIFICAMOS SI SE VA A SUBIR UNA NUEVA FOTO O SI USA LA ANTERIOR
        if (rutaFotoLocal != null) {
            // El usuario seleccionó una foto nueva (o es un plato nuevo) -> Se sube a Cloudinary
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
                            subirPlatoAFirebase(nombre, descripcion, tipo, precio, urlImagenCloudinary);
                        }

                        @Override
                        public void onError(String requestId, ErrorInfo error) {
                            runOnUiThread(() -> Toast.makeText(AgregarPlatoActivity.this, "Error al subir imagen: " + error.getDescription(), Toast.LENGTH_LONG).show());
                        }

                        @Override
                        public void onReschedule(String requestId, ErrorInfo error) {}
                    }).dispatch();
        } else {
            // Modo edición sin cambiar la foto -> Usamos la URL que ya tenía el platoEdicion
            subirPlatoAFirebase(nombre, descripcion, tipo, precio, platoEdicion.getUrlImagen());
        }
    }

    private void subirPlatoAFirebase(String nombre, String descripcion, String tipo, double precio, String urlCloudinary) {
        new Thread(() -> {
            String idPlato;
            String ratingStr;
            String tiempoStr;

            if (esModoEdicion) {
                // MANTENEMOS LOS DATOS ORIGINALES DEL PLATO VIEJO
                idPlato = platoEdicion.getId();
                ratingStr = platoEdicion.getRating();
                tiempoStr = platoEdicion.getTiempo();
            } else {
                // Generamos ID y datos aleatorios por única vez solo si es un plato nuevo
                idPlato = mFirestore.collection("platos").document().getId();

                Random random = new Random();
                double ratingRandom = 4.0 + (1.0 * random.nextDouble());
                ratingStr = String.format(Locale.US, "%.1f", ratingRandom);

                int[] tiemposMinutos = {10, 15, 20, 25};
                int tiempoRandom = tiemposMinutos[random.nextInt(tiemposMinutos.length)];
                tiempoStr = tiempoRandom + " MIN";
            }

            // Creamos o actualizamos el objeto plato con el ID correspondiente
            Plato platoFinal = new Plato(idPlato, nombre, descripcion, tipo, ratingStr, tiempoStr, precio, urlCloudinary);

            mFirestore.collection("platos").document(idPlato)
                    .set(platoFinal)
                    .addOnSuccessListener(aVoid -> {
                        runOnUiThread(() -> {
                            String mensaje = esModoEdicion ? "¡Plato actualizado con éxito!" : "¡Plato guardado con éxito!";
                            Toast.makeText(AgregarPlatoActivity.this, mensaje, Toast.LENGTH_SHORT).show();
                            finish();
                        });
                    })
                    .addOnFailureListener(e -> {
                        runOnUiThread(() -> Toast.makeText(AgregarPlatoActivity.this, "Error al guardar en Firestore", Toast.LENGTH_SHORT).show());
                    });
        }).start();
    }
}