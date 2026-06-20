package com.example.goodfood;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity {

    FirebaseFirestore db;
    Button btnGuardar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        db = FirebaseFirestore.getInstance();
        btnGuardar = findViewById(R.id.btnGuardar);
        btnGuardar.setOnClickListener(v -> guardarDatos());
    }

    private void guardarDatos() {

        Map<String, Object> usuario = new HashMap<>();
        usuario.put("nombre", "Juan");
        usuario.put("edad", 29);
        usuario.put("ciudad", "San Martin de los Andes");

        db.collection("usuarios")
                .add(usuario)
                .addOnSuccessListener(documentReference -> {
                    Log.d("FIREBASE", "Documento guardado con ID: "
                            + documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    Log.e("FIREBASE", "Error al guardar", e);
                });
    }
}