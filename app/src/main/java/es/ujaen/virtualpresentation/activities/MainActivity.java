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

public class MainActivity extends AppCompatActivity {

    Connection conn;

    EditText nusuario, pass;
    Button btnlogin;
    CheckBox recuerdame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("MainActivity", "iniciando la aplicación");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*SharedPreferences sf = getSharedPreferences("default", MODE_PRIVATE);
        String usuario = sf.getString("nombreusuario","");
        if (!usuario.equals("")){
            Intent intent = new Intent(this, QR.class);
            //Intent intent = new Intent(this, Websocket.class);
            startActivity(intent);
        }*/

        nusuario = findViewById(R.id.username);
        pass = findViewById(R.id.password);
        btnlogin = findViewById(R.id.login);
        recuerdame = findViewById(R.id.remember);

        //conn = new Connection(getApplicationContext());
        conn = new Connection(MainActivity.this);


        btnlogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String usuario = nusuario.getText().toString().trim();
                String password =pass.getText().toString().trim();
                if (usuario.isEmpty() || password.isEmpty()){
                    Log.i("MainActivity botonlogin", "No hay datos presentes");
                    Toast.makeText(getApplicationContext(), "Rellena todos los campos", "Rellena todos los campos".length()).show();
                } else {
                    //conn = new Connection(getApplicationContext());
                    conn.login(usuario, password,recuerdame.isChecked());
                }
            }
        });
    }
}