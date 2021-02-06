package es.ujaen.virtualpresentation.activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import es.ujaen.virtualpresentation.R;
import es.ujaen.virtualpresentation.connection.Connection;
import es.ujaen.virtualpresentation.connection.SocketIO;
import es.ujaen.virtualpresentation.data.Preferences;
import es.ujaen.virtualpresentation.data.Session;
import es.ujaen.virtualpresentation.data.User;

public class PresentationActivity extends AppCompatActivity {
    private static SocketIO socketIO;

    private static Button pmas, pmenos, zmas, zmenos, muestranotas, zinicial, eliminanotas;
    private static ImageButton subir, bajar, izquierda, derecha, enviaNota;
    private static EditText pagina, nota;
    private static CheckBox fijarnota;

    private static Session s;
    private String nombreSesion;
    private static int paginaActual = 1;
    private static int paginaMax;
    private static int colorAccent;

    private Context context;
    private static View view;
    private static Connection con;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_presentation);

        nombreSesion = getIntent().getStringExtra("sesion");
        String codigo = getIntent().getStringExtra("codigo");
        Log.i("PresentationActivity", "Iniciando actividad presentación, con sesión:" + nombreSesion);
        //final Context context = getApplicationContext();
        context = this;
        final User u = Preferences.getUser(context);
        s = Preferences.getSession(context, nombreSesion);
        s.setCodigo(codigo);
        con = new Connection(context, u);
        colorAccent = getResources().getColor(R.color.colorAccent, getTheme());

        paginaMax = s.getPaginas();

        socketIO = new SocketIO(PresentationActivity.this, u, s);
//        Thread hilo =  socketIO; //TODO Revisar para que pueda hacerse en el fondo
//        hilo.start();

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
        fijarnota = findViewById(R.id.pr_check_fix_note);

        view = findViewById(R.id.activityPresentation);

        //Para el teclado
        final InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        //Comprobación de conexión
//        if (!socketIO.isConnected()){
//            snackbarReconnection("No se ha conectado correctamente", "Reintentar");
//        } else {
//            socketIO.sendMessage("OK");
//            int numero = s.getPaginaInicio(); //No lo recibe correctamente por el tiempo de espera
//            try {
//                Thread.sleep(200);
//                if (numero > 1) {
//                    socketIO.sendMessage("pnum-" + numero);
//                }
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }

        //Página específica
        pagina.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_DONE) {
                    int numero = Integer.parseInt(pagina.getText().toString().trim());
                    Log.i("Pagina", "Pagina obtenida " + numero);
                    if (numero >= 1 && numero <= paginaMax) { //Número correcto
                        pagina.setBackgroundTintList(ColorStateList.valueOf(colorAccent));
                        socketIO.sendMessage("pnum-" + numero);
                        inputMethodManager.hideSoftInputFromWindow(pagina.getWindowToken(), 0); //Cierra teclado
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
                if (paginaActual < paginaMax) {
                    socketIO.sendMessage("pmas");
                    paginaActual++;
                } else {
                    Toast.makeText(context, "Ha llegado a la última página", Toast.LENGTH_LONG).show();
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
                    Toast.makeText(context, "Ha llegado al inicio", Toast.LENGTH_LONG).show();
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
                if (nuevaNota.length() > 0) {
                    nuevaNota = nuevaNota.replaceAll(System.getProperty("line.separator"), "<br>");
                    Log.i("EnviaNota", "Nota: '" + nuevaNota + "'");
                    socketIO.sendNote(fijarnota.isChecked(), nuevaNota);
                    fijarnota.setChecked(false);
                    nota.getText().clear();
                    inputMethodManager.hideSoftInputFromWindow(enviaNota.getWindowToken(), 0); //cierra el teclado
                }
            }
        });
    }

    @Override
    public void onBackPressed() {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("¿Está seguro?");
        builder.setMessage("¿Desea finalizar la sesión?");

        builder.setPositiveButton("Sí", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Preferences.refreshSession(context, s, paginaActual);
                salir();
            }
        }).setNeutralButton("Salir y eliminar sesión", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                con.deleteSession(s.getNombreSesion());
                salir();
            }
        }).setNegativeButton("No", null);
        AlertDialog dialog = builder.create();
        dialog.show();

    }

    private void salir() {
        super.onBackPressed();
        socketIO.sendMessage("FIN");
        Intent intent = new Intent(PresentationActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (socketIO.isConnected()) {
            socketIO.sendMessage("FIN");
        }
        socketIO.interrupt();
        socketIO.stopSesion();
        socketIO = null;
        super.onDestroy();
    }

    public static void setPage(int p) {
        paginaActual = p;
        pagina.setHint(paginaActual + "/" + paginaMax);
        s.setPaginaInicio(paginaActual);
    }

    public static int getPaginaActual() {
        return paginaActual;
    }

    public static void buttonsClickable(boolean click) {
        if (!click) {
            pagina.setBackgroundTintList(ColorStateList.valueOf(Color.RED));
            //nota.setBackgroundTintList(ColorStateList.valueOf(Color.RED));
        } else {
            pagina.setBackgroundTintList(ColorStateList.valueOf(colorAccent));
            //nota.setBackgroundTintList(ColorStateList.valueOf(colorAccent));
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
        fijarnota.setEnabled(click);
    }

    /**
     * Método para mostrar un Snackbar con el que se podrá volver a conectar a la presentación
     *
     * @param titulo descripción del error a mostrar
     * @param accion texto de acción a realizar
     */
    public static void snackbarReconnection(String titulo, final String accion) {
        buttonsClickable(false);
        final Snackbar mySnackbar = Snackbar.make(view, titulo, Snackbar.LENGTH_INDEFINITE);
        if (accion.equals("Salir")) {
            buttonsClickable(false);
        } else if (accion.equals("Reintentar")) {
            mySnackbar.setAction(accion, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (socketIO.isConnected()) {
                        socketIO.startSession();
//                        socketIO.sendMessage("OK");
//                        int numero = s.getPaginaInicio();
//                        try {
//                            Thread.sleep(200); //Tiempo de espera necesario entre peticiones
//                            if (numero > 1) {
//                                socketIO.sendMessage("pnum-" + numero);
//                            }
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
                        buttonsClickable(true);
                    }
                }
            });
        }
        mySnackbar.show();
    }


}