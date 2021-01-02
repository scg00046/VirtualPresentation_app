package es.ujaen.virtualpresentation.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import es.ujaen.virtualpresentation.R;
import es.ujaen.virtualpresentation.connection.Connection;
import es.ujaen.virtualpresentation.data.Preferences;
import es.ujaen.virtualpresentation.data.User;

/**
 * Activity de autenticación de usuario
 * @author Sergio Caballero Garrido
 */
public class LoginActivity extends AppCompatActivity {

    private EditText nusuario, pass;
    private Button btnlogin;
    private CheckBox recuerdame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("LoginActivity", "Iniciando activity autenticación");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Elementos de la vista activity_login
        nusuario = findViewById(R.id.username);
        pass = findViewById(R.id.password);
        btnlogin = findViewById(R.id.login);
        recuerdame = findViewById(R.id.remember);

        final Connection conn = new Connection(LoginActivity.this);
        Log.i("LoginActivity_Conn", "Preparando la conexión");

        btnlogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String usuario = nusuario.getText().toString().trim();
                String password = pass.getText().toString().trim();
                if (usuario.isEmpty() || password.isEmpty()) {
                    Log.i("LoginActivity_login", "No hay datos presentes");
                    Toast.makeText(getApplicationContext(), "Rellena todos los campos", Toast.LENGTH_LONG).show();
                } else {
                    conn.login(usuario, password, recuerdame.isChecked());
                }
            }
        });
    }

    @Override
    protected void onStop() {
        finish();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}