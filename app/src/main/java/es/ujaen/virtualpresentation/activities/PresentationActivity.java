package es.ujaen.virtualpresentation.activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import es.ujaen.virtualpresentation.R;
import es.ujaen.virtualpresentation.connection.SocketIO;
import es.ujaen.virtualpresentation.data.Preferences;
import es.ujaen.virtualpresentation.data.Session;
import es.ujaen.virtualpresentation.data.User;

public class PresentationActivity extends AppCompatActivity {
    private SocketIO socketIO;

    private Button pmas, pmenos, zmas, zmenos, fin;
    private ImageButton subir, bajar, izquierda, derecha;
    static EditText pagina;

    private String sesion;
    private static int paginaActual = 1;
    private static int paginaMax;

    private boolean finsesion = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_presentation);

        sesion = getIntent().getStringExtra("sesion");
        Log.i("PresentationActivity", "Iniciando actividad presentación, con sesión:" + sesion);
        //final Context context = getApplicationContext();
        final Context context = this;
        final User u = Preferences.getUser(context);
        final Session s = Preferences.getSession(context, sesion);
        final int paginaOK = getResources().getColor(R.color.colorAccent);

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

        subir = findViewById(R.id.bt_up);
        bajar = findViewById(R.id.bt_down);
        izquierda = findViewById(R.id.bt_left);
        derecha = findViewById(R.id.bt_right);

        buttonsClickable(false);

        //Página específica
        pagina.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if ((keyEvent.getAction() == KeyEvent.ACTION_DOWN) &&
                        (i == KeyEvent.KEYCODE_ENTER)) {
                    int numero = Integer.parseInt(pagina.getText().toString().trim());
                    Log.i("Pagina", "Pagina: " + numero);
                    //Toast.makeText(getApplicationContext(), "Nueva página: "+numero, Toast.LENGTH_SHORT).show();
                    if (numero >= 1 && numero <= paginaMax) {
                        pagina.setBackgroundTintList(ColorStateList.valueOf(paginaOK));
                        socketIO.sendMessage("pnum-" + numero);
                    } else {
                        pagina.setBackgroundTintList(ColorStateList.valueOf(Color.RED));
                        //Toast.makeText(context, "Número de página inválido", Toast.LENGTH_SHORT).show();
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


        //finalizar/empezar sesion
        fin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { //TODO revisar
                if (!finsesion) { //Iniciar sesión
                    Thread hilo = socketIO;
                    hilo.start(); //Activa la sesión
                    fin.setText(R.string.present_btn_finish);
                    //fin.setBackgroundColor(Color.RED);
                    buttonsClickable(true);
                    finsesion = true;
                } else { //Finalizar sesión
                    fin.setText(R.string.present_btn_start);
                    //fin.setBackgroundColor(Color.parseColor("#4CAF50"));
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("¿Está seguro?");
                    builder.setMessage("¿Desea finalizar la sesión?");

                    builder.setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            socketIO.sendMessage("FIN");
                            socketIO.interrupt();
                            finsesion = false;
                            finish();
                        }
                    });
                    builder.setNegativeButton("No", null);
                    AlertDialog dialog = builder.create();
                    dialog.show();

                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(PresentationActivity.this, MainActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        socketIO.sendMessage("FIN");
        socketIO.interrupt();
        //socketIO.stopSesion();
       /* Intent intent = new Intent(PresentationActivity.this, MainActivity.class);
        startActivity(intent);*/
    }

    public static void setPaginaActual(int p) {
        paginaActual = p;
        pagina.setHint(paginaActual + "/" + paginaMax);
    }

    private void buttonsClickable(boolean click) {
        pmas.setEnabled(click);
        pagina.setEnabled(click);
        pmenos.setEnabled(click);
        zmas.setEnabled(click);
        zmenos.setEnabled(click);
        subir.setEnabled(click);
        bajar.setEnabled(click);
        izquierda.setEnabled(click);
        derecha.setEnabled(click);
    }
}