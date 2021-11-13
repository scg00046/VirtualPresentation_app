package es.ujaen.virtualpresentation.activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import es.ujaen.virtualpresentation.R;
import es.ujaen.virtualpresentation.connection.Connection;
import es.ujaen.virtualpresentation.data.User;

/**
 * Activity de autenticación de usuario
 *
 * @author Sergio Caballero Garrido
 */
public class LoginActivity extends AppCompatActivity {

    private static EditText nusuario, pass;
    private Button btnlogin;
    private CheckBox recuerdame;

    private EditText reg_username, reg_pass, reg_name, reg_lastname, reg_email;
    private TextView reg_clean, registro;
    private static ProgressDialog loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("LoginActivity", "Iniciando activity autenticación");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        final Context context = LoginActivity.this;

        //Elementos de la vista activity_login
        nusuario = findViewById(R.id.username);
        pass = findViewById(R.id.password);
        btnlogin = findViewById(R.id.login);
        recuerdame = findViewById(R.id.remember);
        registro = findViewById(R.id.register);

        //Configuración spinner cargando
        loading = new ProgressDialog(context);
        loading.setMessage(getString(R.string.load_sending));
        loading.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        final Connection conn = new Connection(context);
        Log.i("LoginActivity_Conn", "Preparando la conexión");

        btnlogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String usuario = nusuario.getText().toString().trim();
                String password = pass.getText().toString().trim();
                if (usuario.isEmpty() || password.isEmpty()) {
                    Log.i("LoginActivity_login", "No hay datos presentes");
                    Toast.makeText(getApplicationContext(), getString(R.string.login_incomplete), Toast.LENGTH_LONG).show();
                } else {
                    loading.show();
                    conn.login(usuario, password, recuerdame.isChecked(), true);
                }
            }
        });

        registro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(getString(R.string.reg_title))
                        .setMessage(getString(R.string.reg_desc));

                LayoutInflater inflater = getLayoutInflater();

                View view = inflater.inflate(R.layout.alertdialog_register, null);
                builder.setView(view);
                //Elementos de la vista de registro
                reg_username = (EditText) view.findViewById(R.id.reg_username);
                reg_pass = (EditText) view.findViewById(R.id.reg_pass);
                reg_name = (EditText) view.findViewById(R.id.reg_name);
                reg_lastname = (EditText) view.findViewById(R.id.reg_lastname);
                reg_email = (EditText) view.findViewById(R.id.reg_email);
                reg_clean = (TextView) view.findViewById(R.id.reg_clean);

                reg_clean.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        reg_username.setText("");
                        reg_pass.setText("");
                        reg_name.setText("");
                        reg_lastname.setText("");
                        reg_email.setText("");
                    }
                });
                builder.setPositiveButton(getString(R.string.reg_send), null);
                builder.setNegativeButton(getString(R.string.btn_cancel), null);

                final AlertDialog dialog = builder.create();

                dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialogInterface) {
                        Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                        button.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View view) {
                                String nick = reg_username.getText().toString().trim();
                                String nombre = reg_name.getText().toString().trim();
                                String apell = reg_lastname.getText().toString().trim();
                                String pass = reg_pass.getText().toString().trim();
                                String mail = reg_email.getText().toString().trim();

                                if (nick.length() >= 2 && pass.length() >= 2 && nombre.length() >= 2) {
                                    loading.show();//muestra spinner de carga
                                    User u = new User(nick, nombre, apell);
                                    conn.createUser(u, pass, mail); //Registra el usuario
                                    dialog.dismiss();
                                } else {
                                    Toast.makeText(context, getString(R.string.reg_incomplete), Toast.LENGTH_SHORT).show();
                                }
                            }
                        }); //Botón registrar
                    }
                });//Fin dialog.setOnShowListener
                dialog.show();
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

    /**
     * Detiene el spinner de espera
     */
    public static void stopLoading() {
        if (loading != null) {
            loading.dismiss();
        }
    }

    /**
     * Autocompleta el campo de usuario para autenticarse después del registro
     *
     * @param username
     */
    public static void setUser(String username) {
        nusuario.setText(username);
        pass.setText("");
    }
}