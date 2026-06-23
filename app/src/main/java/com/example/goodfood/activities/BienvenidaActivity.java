package com.example.goodfood.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.goodfood.R;

public class BienvenidaActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bienvenida);

        Button btnEmpezar = findViewById(R.id.btnEmpezar);
        TextView tvLoginLink = findViewById(R.id.tvLoginLink);
        TextView tvAboutLink = findViewById(R.id.tvAboutLink);

        btnEmpezar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // ir a la pantalla de Registro
                Intent intent = new Intent(BienvenidaActivity.this, RegistroActivity.class);
                startActivity(intent);
            }
        });

        tvLoginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // ir a la pantalla de Login
                Intent intent = new Intent(BienvenidaActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        tvAboutLink.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                //ir a pantalla de Saber Mas
                Intent intent = new Intent(BienvenidaActivity.this, AboutActivity.class);
                startActivity(intent);
            }
        });
    }
}