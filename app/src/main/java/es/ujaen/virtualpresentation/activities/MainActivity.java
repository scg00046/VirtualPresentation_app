package es.ujaen.virtualpresentation.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import es.ujaen.virtualpresentation.R;
import es.ujaen.virtualpresentation.connection.Connection;
import es.ujaen.virtualpresentation.data.Preferences;
import es.ujaen.virtualpresentation.data.User;

/**
 * Activity principal, consta de varios fragmentos
 *
 * @author Sergio Caballero Garrido
 */
public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private static FloatingActionButton fab;
    private static NavController navController;
    private static Connection con;
    private static Activity activity;
    private static boolean alerta = true;

    private static User u;
    private static ProgressDialog loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //getIntent().getStringExtra("sesion");
        boolean qr = getIntent().getBooleanExtra("qr", false);
        String text = getIntent().getStringExtra("text");

        Context context = getApplicationContext();
        activity = this;
        u = Preferences.getUser(context);
        String nombre = u.getNombre() + " " + u.getApellidos();
        con = new Connection(context, u);


        fab = findViewById(R.id.fab);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        View navView = navigationView.getHeaderView(0);
        TextView textNombre = (TextView) navView.findViewById(R.id.nav_user_name);
        TextView textLogin = (TextView) navView.findViewById(R.id.nav_user_login);
        textNombre.setText(nombre);
        textLogin.setText(u.getNombreusuario());
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_qr, R.id.nav_upload, R.id.nav_delete, R.id.nav_about)
                .setDrawerLayout(drawer)
                .build();
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        if (qr) {
            navController.navigate(R.id.nav_qr);
            View view = findViewById(R.id.drawer_layout);
            Snackbar mySnackbar = Snackbar.make(view, text, Snackbar.LENGTH_LONG);
            mySnackbar.setBackgroundTint(getResources().getColor(R.color.colorAccent, getTheme()));
            mySnackbar.show();
        }

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navController.navigate(R.id.nav_qr);
            }
        });

        con.getSessions();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        alerta = true;
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            new Connection(this, u).logout();
            Toast.makeText(this, "Se ha cerrado la sesión", Toast.LENGTH_SHORT).show();
            Preferences.deleteCredentials(this);
            Preferences.removeAllSession(this);
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * Oculta el botón flotante para acceder al escaner QR
     */
    public static void hiddenFloatButton() {
        fab.setVisibility(View.GONE);
    }

    /**
     * Muestra el botón flotante para acceder al escaner QR
     */
    public static void showFloatButton() {
        fab.setVisibility(View.VISIBLE);
    }

    /**
     * Detiene el spinner de espera
     */
    public static void showHideLoading(Context context, boolean show) {
        if (show) {
            //Configuración spinner cargando
            loading = new ProgressDialog(context);
            loading.setMessage(context.getString(R.string.load_conecting));
            loading.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            loading.show();
        } else {
            if (loading != null) {
                loading.dismiss();
            }
        }
    }

    /**
     * Si el usuario eligió no recordar las credenciales, eliminará los datos de memoria
     * y se iniciará la actividad de login
     * En caso contrario, muestra una alerta solicitando nuevamente la contraseña para obtener
     * un nuevo token
     *
     * @param context
     */
    public static void requestPassword(final Context context) {
        SharedPreferences sp = context.getSharedPreferences("default", Context.MODE_PRIVATE);
        boolean guardar = sp.getBoolean("permanente", true);

        if (!guardar) {
            Preferences.removeAllSession(context);
            Preferences.deleteCredentials(context);
            Intent intent = new Intent(context, LoginActivity.class);
            context.startActivity(intent);
        } else {
            if (alerta) {
                alerta = false;
                AlertDialog.Builder builder = new AlertDialog.Builder(activity, R.style.Theme_AppCompat_Light_Dialog_Alert);
                builder.setTitle(context.getString(R.string.reqpass_title))
                        .setMessage(context.getString(R.string.reqpass_desc));

                LayoutInflater inflater = activity.getLayoutInflater();

                View view = inflater.inflate(R.layout.alertdialog_password, null);
                builder.setView(view);

                final EditText pass = (EditText) view.findViewById(R.id.alert_password);

                builder.setPositiveButton(context.getString(R.string.btn_send), null);
                builder.setNegativeButton(context.getString(R.string.action_logout), null);

                final AlertDialog dialog = builder.create();

                dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(final DialogInterface dialog) {

                        Button enviar = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                        Button cancelar = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_NEGATIVE);
                        enviar.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                con.login(u.getNombreusuario(), pass.getText().toString().trim(), true, false);
                                dialog.dismiss();
                            }
                        });
                        cancelar.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Preferences.removeAllSession(context);
                                Preferences.deleteCredentials(context);
                                Intent intent = new Intent(context, LoginActivity.class);
                                context.startActivity(intent);
                                dialog.dismiss();
                            }
                        });
                    } //Fin onShow
                }); //Fin dialog.setOnShowListener

                dialog.show();
            }
        }
    }
}