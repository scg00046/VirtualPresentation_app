package es.ujaen.virtualpresentation.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
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

public class LoginActivity extends AppCompatActivity {

    private Connection conn;

    private EditText nusuario, pass;
    private Button btnlogin;
    private CheckBox recuerdame;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("LoginActivity", "iniciando la aplicación");

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        setTheme(R.style.AppTheme);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        SharedPreferences sf = getSharedPreferences("default", MODE_PRIVATE);
        String usuario = sf.getString("nombreusuario", "");

        if (!usuario.equals("")) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        } //else {

            nusuario = findViewById(R.id.username);
            pass = findViewById(R.id.password);
            btnlogin = findViewById(R.id.login);
            recuerdame = findViewById(R.id.remember);

            //conn = new Connection(getApplicationContext());
            conn = new Connection(LoginActivity.this);
            Log.i("LoginActivity_Conn", "Preparando la conexión");

            btnlogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    String usuario = nusuario.getText().toString().trim();
                    String password = pass.getText().toString().trim();
                    if (usuario.isEmpty() || password.isEmpty()) {
                        Log.i("LoginActivity_login", "No hay datos presentes");
                        Toast.makeText(getApplicationContext(), "Rellena todos los campos", "Rellena todos los campos".length()).show();
                    } else {
                        //conn = new Connection(getApplicationContext());
                        conn.login(usuario, password, recuerdame.isChecked());
                    }
                }
            });
        //}
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Preferences.deleteCredentials(getApplicationContext());
        super.onDestroy();
    }
}