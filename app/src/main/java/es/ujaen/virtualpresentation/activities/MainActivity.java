package es.ujaen.virtualpresentation.activities;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
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
 * @author Sergio Caballero Garrido
 */
public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;

    private static FloatingActionButton fab;

    public String usuario;

    private User u;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //getIntent().getStringExtra("sesion");
        boolean qr = getIntent().getBooleanExtra("qr", false);
        String text = getIntent().getStringExtra("text");


        u = Preferences.getUser(getApplicationContext());
        String nombre = u.getNombre() + " " + u.getApellidos();
        //SharedPreferences sf = getSharedPreferences("default", MODE_PRIVATE);
        //String usuario = sf.getString("nombre", "");

        fab = findViewById(R.id.fab);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        /*List<String> defSpinner = new ArrayList<>();
        defSpinner.add("No hay presentaciones para " + usuario);
        Spinner presList = findViewById(R.id.presentationList);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_dropdown_item,
                Collections.unmodifiableList(defSpinner));
        presList.setAdapter(adapter);
        presList.setClickable(false);*/

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
        final NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        if (qr){
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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_logout){
            new Connection(this, u).logout();
            Toast.makeText(this, "Se ha cerrado la sesión", Toast.LENGTH_SHORT).show();
            Preferences.deleteCredentials(this);
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

    public static void hiddenFloatButton (){
        fab.setVisibility(View.GONE);
    }

    public static void showFloatButton (){
        fab.setVisibility(View.VISIBLE);
    }
}