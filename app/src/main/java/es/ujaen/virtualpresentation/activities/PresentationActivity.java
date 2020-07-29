package es.ujaen.virtualpresentation.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;

import es.ujaen.virtualpresentation.R;
import es.ujaen.virtualpresentation.connection.SocketIO;
import es.ujaen.virtualpresentation.data.Preferences;
import es.ujaen.virtualpresentation.data.Session;
import es.ujaen.virtualpresentation.data.Usuario;

public class PresentationActivity extends AppCompatActivity {
    private SocketIO socketIO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_presentation);
        Context context = getApplicationContext();
        Usuario u = Preferences.obtenerUsuario(context);
        Session s = Preferences.obtenerSession(context, "nuevaSesion"); //TODO recoger del intent

        socketIO = new SocketIO(PresentationActivity.this, u, s);
        Thread hilo =  socketIO; //TODO Revisar para que pueda hacerse en el fondo
        hilo.start();

    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        socketIO.interrupt();
        //socketIO.stopSesion();
    }


}