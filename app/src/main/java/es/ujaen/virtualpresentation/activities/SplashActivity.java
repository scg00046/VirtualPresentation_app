package es.ujaen.virtualpresentation.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import es.ujaen.virtualpresentation.R;
import es.ujaen.virtualpresentation.data.Preferences;

/**
 * Activity presentación de la aplicación
 * @author Sergio Caballero Garrido
 */
public class SplashActivity extends AppCompatActivity {

    private static int SPLASH_SCREEN_TIME = 2000;

    private Animation top, botton;
    private ImageView logo;
    private TextView titulo, autor, tfg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("SplashActivity", "Iniciando la aplicación");
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);

        //Animaciones
        top = AnimationUtils.loadAnimation(this, R.anim.top_animation);
        botton = AnimationUtils.loadAnimation(this, R.anim.bottom_animation);
        //Elementos
        titulo = findViewById(R.id.title_app);
        autor = findViewById(R.id.splash_author);
        tfg = findViewById(R.id.splash_tfg);
        logo = findViewById(R.id.splash_logo);
        //Asignar animaciones a elementos
        logo.setAnimation(top);
        titulo.setAnimation(top);
        tfg.setAnimation(botton);
        autor.setAnimation(botton);

        //Recopilación de datos guardados
        SharedPreferences sf = getSharedPreferences("default", MODE_PRIVATE);
        String usuario = sf.getString("nombreusuario", "");
        boolean recordar = sf.getBoolean("permanente",false); //TODO revisar que funciona

        final Intent intent;
        if (usuario.equals("") || !recordar) { //Usuario vacío o con la opción no recordar
            Preferences.deleteCredentials(this); //Elimina los posibles datos de usuario anterior
            intent = new Intent(this, LoginActivity.class);
        } else {
            intent = new Intent(SplashActivity.this, MainActivity.class);
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(intent);
                finish();
            }
        }, SPLASH_SCREEN_TIME);
    }
}