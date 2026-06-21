package com.example.goodfood;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.goodfood.adapters.PlatoAdapter;
import com.example.goodfood.models.Plato;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TextView tvSaludo;
    private RecyclerView rvCatalogo;
    private PlatoAdapter adaptador;
    private List<Plato> listaPlatos;

    //variable global para el nombre del usuario
    private String nombreUsuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. Enlazar componentes de la interfaz
        tvSaludo = findViewById(R.id.tvSaludo);
        rvCatalogo = findViewById(R.id.rvCatalogo);

        if (getIntent().hasExtra("nombre_usuario")) {
            nombreUsuario = getIntent().getStringExtra("nombre_usuario");
        } else {
            nombreUsuario = "Cliente"; // Por si pasa algo raro y viene vacío
        }

        tvSaludo.setText("¡Hola, " + nombreUsuario + "!");

        // 2. Configurar el RecyclerView
        rvCatalogo.setLayoutManager(new LinearLayoutManager(this));
        listaPlatos = new ArrayList<>();
        adaptador = new PlatoAdapter(listaPlatos);
        rvCatalogo.setAdapter(adaptador);

        // 3. EJECUCION DE HILO EN SEGUNDO PLANO
        cargarMenu();
    }

    private void cargarMenu() {
        // Creamos un hilo secundario nativo de Java
        Thread cargarPlatos = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Simulamos una latencia de red de 2 segundos (el hilo duerme en el fondo)
                    Thread.sleep(2000);

                    // Cargamos una lista de prueba de platos saludables
                    listaPlatos.add(new Plato("1", "Ensalada Caesar de Pollo", "Pollo premium, lechuga orgánica, croutons integrales.", 4500.0, ""));
                    listaPlatos.add(new Plato("2", "Wrap Veggie de Palta", "Tortilla integral, palta, tomates cherry, espinaca.", 3800.0, ""));
                    listaPlatos.add(new Plato("3", "Salmón con Puré de Calabaza", "Filete de salmón rosado con puré cremoso light.", 8500.0, ""));
                    listaPlatos.add(new Plato("4", "Wok de Vegetales y Arroz Integral", "Mix de verduras salteadas con dados de tofu.", 4100.0, ""));

                    // CRUCIAL: Como modificamos la interfaz gráfica (UI), debemos volver al hilo principal
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // Le avisamos al adaptador que aparecieron datos nuevos para que los dibuje
                            adaptador.notifyDataSetChanged();
                            Toast.makeText(MainActivity.this, "Menú actualizado con éxito", Toast.LENGTH_SHORT).show();
                        }
                    });

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        // Iniciamos el hilo secundario
        cargarPlatos.start();
    }
}