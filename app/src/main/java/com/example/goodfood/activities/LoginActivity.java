package com.example.goodfood.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.goodfood.R;
import com.example.goodfood.MainActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnIngresar;
    private SignInButton btnGoogle;
    private TextView tvIrARegistro;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private ActivityResultLauncher<Intent> googleSignInLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        etEmail = findViewById(R.id.etLoginEmail);
        etPassword = findViewById(R.id.etLoginPassword);
        btnIngresar = findViewById(R.id.btnIngresar);
        tvIrARegistro = findViewById(R.id.tvIrARegistro);

        // Configuración obligatoria para Google Auth
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("365154847322-el7i3kfkft262i43tvc5r3laicb7uko8.apps.googleusercontent.com")
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        etEmail = findViewById(R.id.etLoginEmail);
        etPassword = findViewById(R.id.etLoginPassword);
        btnIngresar = findViewById(R.id.btnIngresar);
        tvIrARegistro = findViewById(R.id.tvIrARegistro);
        btnGoogle = findViewById(R.id.btnGoogleLogin);

        // Launcher que recibe la respuesta de la cuenta de Google seleccionada
        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                        try {
                            GoogleSignInAccount account = task.getResult(ApiException.class);
                            if (account != null) {
                                firebaseAuthWithGoogle(account.getIdToken());
                            }
                        } catch (ApiException e) {
                            Toast.makeText(this, "Error de Google: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        // Lógica del botón Ingresar
        btnIngresar.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Por favor, completa los campos", Toast.LENGTH_SHORT).show();
            } else {
                loguearUsuario(email, password);
            }
        });

        // Click para login con Google
        btnGoogle.setOnClickListener(v -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            googleSignInLauncher.launch(signInIntent);
        });

        // Por si entró acá pero no tiene cuenta, lo mandamos a la pantalla de Registro
        tvIrARegistro.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegistroActivity.class);
            startActivity(intent);
            finish();
        });
    }

    // LOGEAR USUARIO Y TRAER SU NOMBRE DE LA BASE DE DATOS
    private void loguearUsuario(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && mAuth.getCurrentUser() != null) {
                        String uid = mAuth.getCurrentUser().getUid();

                        com.google.firebase.firestore.FirebaseFirestore.getInstance()
                                .collection("usuarios")
                                .document(uid)
                                .get()
                                .addOnSuccessListener(documentSnapshot -> {
                                    String nombreReal = "Cliente";
                                    String rol = "cliente"; // Por defecto

                                    if (documentSnapshot.exists()) {
                                        if (documentSnapshot.contains("nombre")) {
                                            nombreReal = documentSnapshot.getString("nombre");
                                        }
                                        if (documentSnapshot.contains("rol")) {
                                            rol = documentSnapshot.getString("rol"); // Extraemos "admin" o "cliente"
                                        }
                                    }

                                    irAlInicio(nombreReal, rol);
                                })
                                .addOnFailureListener(e -> irAlInicio("Cliente", "cliente"));
                    } else {
                        Toast.makeText(LoginActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // METODOS PARA INICIAR SESION CON GOOGLE
    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful() && mAuth.getCurrentUser() != null) {
                        String uid = mAuth.getCurrentUser().getUid();

                        // 🌟 Vamos SIEMPRE a Firestore para traer el ROL real (cliente o admin)
                        com.google.firebase.firestore.FirebaseFirestore.getInstance()
                                .collection("usuarios")
                                .document(uid)
                                .get()
                                .addOnSuccessListener(documentSnapshot -> {
                                    String nombreReal = "Cliente";
                                    String rol = "cliente"; // Por defecto si pasa algo raro

                                    if (documentSnapshot.exists()) {
                                        if (documentSnapshot.contains("nombre")) {
                                            nombreReal = documentSnapshot.getString("nombre");
                                        }
                                        if (documentSnapshot.contains("rol")) {
                                            rol = documentSnapshot.getString("rol"); // Extraemos "admin" o "cliente"
                                        }
                                    } else {
                                        // Si el documento no existe en Firestore (ej: primer login con Google),
                                        // usamos el nombre que nos da el perfil de Google directamente
                                        if (mAuth.getCurrentUser().getDisplayName() != null) {
                                            nombreReal = mAuth.getCurrentUser().getDisplayName();
                                        }
                                    }

                                    // Pasamos ambos datos al inicio
                                    irAlInicio(nombreReal, rol);
                                })
                                .addOnFailureListener(e -> {
                                    // Si falla la red, por descarte entra como cliente genérico
                                    irAlInicio("Cliente", "cliente");
                                });
                    } else {
                        Toast.makeText(this, "Error de autenticación con Firebase", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void irAlInicio(String nombreUsuario, String rol) {
        Toast.makeText(LoginActivity.this, "¡Inicio de sesión exitoso!", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.putExtra("nombre_usuario", nombreUsuario);
        intent.putExtra("rol_usuario", rol);
        startActivity(intent);
        finish();
    }
}