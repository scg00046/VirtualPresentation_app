package es.ujaen.virtualpresentation.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import es.ujaen.virtualpresentation.R;
import es.ujaen.virtualpresentation.connection.SocketIO;
import es.ujaen.virtualpresentation.data.Preferences;
import es.ujaen.virtualpresentation.data.Session;
import es.ujaen.virtualpresentation.data.Usuario;

public class PresentationActivity extends AppCompatActivity {
    private SocketIO socketIO;

    Button pmas, pmenos, zmas, zmenos, fin;
    EditText pagina;

    private String sesion;
    private int paginaActual = 1;
    private int paginaMax;

    private boolean finsesion = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //final boolean iniciosesion = true;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_presentation);

        sesion = getIntent().getStringExtra("sesion");
        Log.i("PresentationActivity", "Iniciando actividad presentación, con sesión:"+sesion);
        Context context = getApplicationContext();
        final Usuario u = Preferences.obtenerUsuario(context);
        final Session s = Preferences.obtenerSession(context, sesion);

        paginaMax = s.getPaginas();

        socketIO = new SocketIO(PresentationActivity.this, u, s);
//        Thread hilo =  socketIO; //TODO Revisar para que pueda hacerse en el fondo
//        hilo.start();

        pmas = findViewById(R.id.bt_mas);
        pmenos = findViewById(R.id.bt_menos);
        zmas = findViewById(R.id.bt_zoommas);
        zmenos = findViewById(R.id.bt_zoommenos);
        fin = findViewById(R.id.bt_finsesion);
        pagina = findViewById(R.id.text_page);

        pagina.setHint("Página: "+paginaActual);


        pmas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (paginaActual<=paginaMax){
                    socketIO.sendMessage("pmas");
                    paginaActual++;
                    pagina.setHint("Página: "+paginaActual);
                } else {
                    Toast.makeText(getApplicationContext(),"Ha llegado a la última página", Toast.LENGTH_LONG).show();
                }
            }
        });

        pmenos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (paginaActual>1){
                    socketIO.sendMessage("pmenos");
                    paginaActual--;
                    pagina.setHint("Página: "+paginaActual);
                } else {
                    Toast.makeText(getApplicationContext(),"Ha llegado al inicio", Toast.LENGTH_LONG).show();
                }
            }
        });

        zmas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                socketIO.sendMessage("zmas");
            }
        });

        zmenos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                socketIO.sendMessage("zmenos");
            }
        });

        fin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!finsesion) {
                    Thread hilo = socketIO;
                    hilo.start();
                    fin.setText("Finalizar sesión");
                    finsesion = true;
                } else {
                    onDestroy();
                    finsesion = false;
                }
            }
        });
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        //TODO recoger la página por la que va
        socketIO.interrupt();
        //socketIO.stopSesion();
    }


}