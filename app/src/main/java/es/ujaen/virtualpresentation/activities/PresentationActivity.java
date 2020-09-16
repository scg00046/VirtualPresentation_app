package es.ujaen.virtualpresentation.activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import es.ujaen.virtualpresentation.R;
import es.ujaen.virtualpresentation.connection.SocketIO;
import es.ujaen.virtualpresentation.data.Preferences;
import es.ujaen.virtualpresentation.data.Session;
import es.ujaen.virtualpresentation.data.User;

public class PresentationActivity extends AppCompatActivity {
    private SocketIO socketIO;

    private Button pmas, pmenos, zmas, zmenos, fin, muestranotas, zinicial, eliminanotas;
    private ImageButton subir, bajar, izquierda, derecha, enviaNota;
    private static EditText pagina;
    private EditText nota;

    private Session s;
    private String nombreSesion;
    private static int paginaActual = 1;
    private static int paginaMax;

    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_presentation);

        nombreSesion = getIntent().getStringExtra("sesion");
        Log.i("PresentationActivity", "Iniciando actividad presentación, con sesión:" + nombreSesion);
        //final Context context = getApplicationContext();
        context = this;
        final User u = Preferences.getUser(context);
        s = Preferences.getSession(context, nombreSesion);
        final int colorpaginaOK = getResources().getColor(R.color.colorAccent);

        paginaMax = s.getPaginas();

        socketIO = new SocketIO(PresentationActivity.this, u, s);
        Thread hilo =  socketIO; //TODO Revisar para que pueda hacerse en el fondo
        hilo.start();

        pmas = findViewById(R.id.pr_bt_next);
        pmenos = findViewById(R.id.pr_bt_previous);
        zmas = findViewById(R.id.pr_bt_zoom_more);
        zmenos = findViewById(R.id.pr_bt_zoom_less);
        zinicial = findViewById(R.id.pr_bt_zoom_reset);
        pagina = findViewById(R.id.pr_num_page);

        subir = findViewById(R.id.pr_bt_up);
        bajar = findViewById(R.id.pr_bt_down);
        izquierda = findViewById(R.id.pr_bt_left);
        derecha = findViewById(R.id.pr_bt_right);

        muestranotas = findViewById(R.id.pr_bt_open_notes);
        eliminanotas = findViewById(R.id.pr_bt_delete_notes);
        nota = findViewById(R.id.pr_note);
        enviaNota = findViewById(R.id.pr_bt_send_notes);

        View view = findViewById(R.id.activityPresentation);

        //Comprobación de conexión
        if (!socketIO.isConnected()){
            buttonsClickable(false);
            Snackbar mySnackbar = Snackbar.make(view,
                    "No se ha conectado correctamente", Snackbar.LENGTH_INDEFINITE);
            mySnackbar.setAction("Reintentar", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(socketIO.isConnected()){
                        socketIO.sendMessage("OK");
                        int numero = s.getPaginaInicio();
                        try {
                            Thread.sleep(200); //Tiempo de espera necesario entre peticiones
                            if (numero > 1) {
                                socketIO.sendMessage("pnum-" + numero);
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        buttonsClickable(true);
                    }
                }
            });
            mySnackbar.show();
        } else {
            socketIO.sendMessage("OK");
            int numero = s.getPaginaInicio(); //No lo recibe correctamente por el tiempo de espera
            try {
                Thread.sleep(200);
                if (numero > 1) {
                    socketIO.sendMessage("pnum-" + numero);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //Toast.makeText(context, "Conectado", Toast.LENGTH_SHORT).show();
        }

        //Página específica
        pagina.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_DONE){
                    int numero = Integer.parseInt(pagina.getText().toString().trim());
                    Log.i("Pagina", "Pagina obtenida "+numero);
                    if (numero >= 1 && numero <= paginaMax) { //Número correcto
                        pagina.setBackgroundTintList(ColorStateList.valueOf(colorpaginaOK));
                        socketIO.sendMessage("pnum-" + numero);
                    } else {
                        pagina.setBackgroundTintList(ColorStateList.valueOf(Color.RED));
                    }
                    pagina.getText().clear();
                    return true;
                }
                return false;
            }
        });

        //Avanzar
        pmas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (paginaActual <= paginaMax) {
                    socketIO.sendMessage("pmas");
                    paginaActual++;
                } else {
                    Toast.makeText(getApplicationContext(), "Ha llegado a la última página", Toast.LENGTH_LONG).show();
                }
            }
        });
        //Retroceder
        pmenos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (paginaActual > 1) {
                    socketIO.sendMessage("pmenos");
                    paginaActual--;
                } else {
                    Toast.makeText(getApplicationContext(), "Ha llegado al inicio", Toast.LENGTH_LONG).show();
                }
            }
        });
        //Aumentar zoom
        zmas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                socketIO.sendMessage("zmas");
            }
        });
        //Disminuir zoom
        zmenos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                socketIO.sendMessage("zmenos");
            }
        });
        //Reiniciar zoom y posición
        zinicial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                socketIO.sendMessage("zinicial");
            }
        });
        //Subir
        subir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                socketIO.sendMessage("subir");
            }
        });
        //bajar
        bajar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                socketIO.sendMessage("bajar");
            }
        });
        //Izquierda
        izquierda.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                socketIO.sendMessage("izquierda");
            }
        });
        //Derecha
        derecha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                socketIO.sendMessage("derecha");
            }
        });

        //Mostrar/ocultar lista de notas
        muestranotas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                socketIO.sendMessage("muestraNotas");
            }
        });
        //Elimina las notas enviadas
        eliminanotas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                socketIO.sendMessage("eliminaNotas");
            }
        });
        //Envía nota
        enviaNota.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String nuevaNota = nota.getText().toString().trim();
                if (nuevaNota.length()>0){
                    nuevaNota = nuevaNota.replaceAll(System.getProperty("line.separator"),"<br>");
                    Log.i("EnviaNota", "Nota: '"+nuevaNota+"'");
                    socketIO.sendNote(nuevaNota);
                    nota.getText().clear();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {

        final boolean[] salir = new boolean[1];
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("¿Está seguro?");
        builder.setMessage("¿Desea finalizar la sesión?");

        builder.setPositiveButton("Sí", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Preferences.refreshSession(context, s, paginaActual);
                socketIO.sendMessage("FIN");
                Intent intent = new Intent(PresentationActivity.this, MainActivity.class);
                startActivity(intent);
                salir[0] = true;
            }
        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                salir[0] = false;
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();

        if (salir[0]){
            super.onBackPressed();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        socketIO.interrupt();
        socketIO.stopSesion();
        socketIO = null;
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public static void setPaginaActual(int p) {
        paginaActual = p;
        pagina.setHint(paginaActual + "/" + paginaMax);
    }

    private void buttonsClickable(boolean click) {
        if (!click){
            pagina.setBackgroundTintList(ColorStateList.valueOf(Color.RED));
        } else {
            pagina.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
        }
        pmas.setEnabled(click);
        pagina.setEnabled(click);
        pmenos.setEnabled(click);
        zmas.setEnabled(click);
        zmenos.setEnabled(click);
        subir.setEnabled(click);
        bajar.setEnabled(click);
        izquierda.setEnabled(click);
        derecha.setEnabled(click);
        zinicial.setEnabled(click);
        muestranotas.setEnabled(click);
        eliminanotas.setEnabled(click);
        nota.setEnabled(click);
        enviaNota.setEnabled(click);
    }
}