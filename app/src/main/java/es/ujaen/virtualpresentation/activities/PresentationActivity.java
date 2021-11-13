package es.ujaen.virtualpresentation.activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

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
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

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
    private static EditText pagina, nota, tituloUrl;
    private static CheckBox fijarnota, enlace;

    private static final int[] controles = {
            R.id.pr_bt_up,
            R.id.pr_bt_down,
            R.id.pr_bt_left,
            R.id.pr_bt_right};
    private static final int[] botones = {
            R.id.pr_bt_next,
            R.id.pr_bt_previous,
            R.id.pr_bt_zoom_more,
            R.id.pr_bt_zoom_less,
            R.id.pr_bt_zoom_reset,
            R.id.pr_bt_open_notes,
            R.id.pr_bt_delete_notes};

    private static Session s;
    private String nombreSesion;
    private static int paginaActual = 1;
    private static int paginaMax;
    private static int colorAccent, colorDisabled;

    private Context context;
    private static Context stContext;
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
        stContext = this;
        final User u = Preferences.getUser(context);
        s = Preferences.getSession(context, nombreSesion);
        s.setCodigo(codigo);
        con = new Connection(context, u);
        colorAccent = getResources().getColor(R.color.colorAccent, getTheme());
        colorDisabled = getResources().getColor(R.color.colorDisabled, getTheme());

        paginaMax = s.getPaginas();

        pmas = (Button) findViewById(R.id.pr_bt_next);
        pmenos = (Button) findViewById(R.id.pr_bt_previous);
        zmas = (Button) findViewById(R.id.pr_bt_zoom_more);
        zmenos = (Button) findViewById(R.id.pr_bt_zoom_less);
        zinicial = (Button) findViewById(R.id.pr_bt_zoom_reset);
        pagina = (EditText) findViewById(R.id.pr_num_page);

        subir = (ImageButton) findViewById(R.id.pr_bt_up);
        bajar = (ImageButton) findViewById(R.id.pr_bt_down);
        izquierda = (ImageButton) findViewById(R.id.pr_bt_left);
        derecha = (ImageButton) findViewById(R.id.pr_bt_right);

        muestranotas = (Button) findViewById(R.id.pr_bt_open_notes);
        eliminanotas = (Button) findViewById(R.id.pr_bt_delete_notes);
        nota = (EditText) findViewById(R.id.pr_note);
        enviaNota = (ImageButton) findViewById(R.id.pr_bt_send_notes);
        fijarnota = (CheckBox) findViewById(R.id.pr_check_fix_note);
        enlace = (CheckBox) findViewById(R.id.pr_check_url);
        tituloUrl = (EditText) findViewById(R.id.pr_title_link);

        view = (View) findViewById(R.id.activityPresentation);

        socketIO = new SocketIO(PresentationActivity.this, u, s);
        buttonsClickable(false);

        //Para el teclado
        final InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

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
                    checkPage();
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
                }
                checkPage();
            }
        });
        //Retroceder
        pmenos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (paginaActual > 1) {
                    socketIO.sendMessage("pmenos");
                    paginaActual--;
                }
                checkPage();
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
                String titulo = tituloUrl.getText().toString().trim();
                if (nuevaNota.length() > 0 && ((enlace.isChecked() && titulo.length() > 0) || !enlace.isChecked())) {
                    if (enlace.isChecked()) {
                        nuevaNota = "<a href='" + nuevaNota + "'>" + titulo + "</a>";
                        tituloUrl.getText().clear();
                        enlace.setChecked(false);
                        tituloUrl.setVisibility(View.INVISIBLE);
                        nota.setHint(getString(R.string.present_edit_note));
                    } else {
                        nuevaNota = nuevaNota.replaceAll(System.getProperty("line.separator"), "<br>");
                    }
                    Log.i("EnviaNota", "Nota: '" + nuevaNota + "'");
                    socketIO.sendNote(fijarnota.isChecked(), nuevaNota, paginaActual);
                    fijarnota.setChecked(false);
                    nota.getText().clear();
                    inputMethodManager.hideSoftInputFromWindow(enviaNota.getWindowToken(), 0); //cierra el teclado
                }
            }
        });
        enlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (enlace.isChecked()) {
                    tituloUrl.setVisibility(View.VISIBLE);
                    nota.setHint(getString(R.string.present_edit_url));
                } else {
                    tituloUrl.setVisibility(View.INVISIBLE);
                    nota.setHint(getString(R.string.present_edit_note));
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
        if (socketIO.isConnected()) {
            socketIO.sendMessage("FIN");
        }
        Intent intent = new Intent(PresentationActivity.this, MainActivity.class);
        intent.putExtra("qr", true);
        intent.putExtra("text", "Ha finalizado la sesión correctamente");
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
        checkPage();
    }

    public static int getPaginaActual() {
        return paginaActual;
    }

    public static void buttonsClickable(boolean click) {
        if (!click) {
            pagina.setBackgroundTintList(ColorStateList.valueOf(Color.RED));
            nota.setBackground(ContextCompat.getDrawable(stContext, R.drawable.btn_rounded_disabled));
            enviaNota.setColorFilter(colorDisabled);
        } else {
            pagina.setBackgroundTintList(ColorStateList.valueOf(colorAccent));
            nota.setBackground(ContextCompat.getDrawable(stContext, R.drawable.btn_rounded));
            enviaNota.setColorFilter(colorAccent);
        }

        pagina.setEnabled(click);
        nota.setEnabled(click);
        enviaNota.setEnabled(click);
        fijarnota.setEnabled(click);
        enlace.setEnabled(click);
        //Controles para mover la diapositiva
        for (int id : controles) {
            ImageButton btn = (ImageButton) view.findViewById(id);
            if (click) {
                btn.setBackground(ContextCompat.getDrawable(stContext, R.drawable.btn_rounded));
                btn.setColorFilter(colorAccent);
            } else {
                btn.setBackground(ContextCompat.getDrawable(stContext, R.drawable.btn_rounded_disabled));
                btn.setColorFilter(colorDisabled);
            }
            btn.setEnabled(click);
        }
        // Resto de controles
        for (int id : botones) {
            Button btn = (Button) view.findViewById(id);
            if (click) {
                btn.setBackground(ContextCompat.getDrawable(stContext, R.drawable.btn_round));
            } else {
                btn.setBackground(ContextCompat.getDrawable(stContext, R.drawable.btn_round_disabled));
            }
            btn.setEnabled(click);
        }
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
            mySnackbar.setAction(accion, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(stContext, MainActivity.class);
                    stContext.startActivity(intent);
                    socketIO.interrupt();
                    //finish();
                }
            });
        } else if (accion.equals("Reintentar")) {
            mySnackbar.setAction(accion, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    socketIO.setConnect();
                    try {
                        Thread.sleep(1500);
                        if (socketIO.isConnected()) {
                            socketIO.startSession();
                            buttonsClickable(true);
                        } else {
                            socketIO.interrupt();
                            socketIO.stopSesion();
                            socketIO = null;
                            Intent intent = new Intent(stContext, MainActivity.class);
                            intent.putExtra("qr", true);
                            intent.putExtra("text", "No se ha podido conectar al servidor");
                            stContext.startActivity(intent);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        mySnackbar.show();
    }

    private static void checkPage() {
        //Botón avanzar
        if (paginaActual == paginaMax) {
            pmas.setBackground(ContextCompat.getDrawable(stContext, R.drawable.btn_round_disabled));
            pmas.setEnabled(false);
        } else {
            pmas.setBackground(ContextCompat.getDrawable(stContext, R.drawable.btn_round));
            pmas.setEnabled(true);
        }
        //Botón retroceder
        if (paginaActual == 1) {
            pmenos.setBackground(ContextCompat.getDrawable(stContext, R.drawable.btn_round_disabled));
            pmenos.setEnabled(false);
        } else {
            pmenos.setBackground(ContextCompat.getDrawable(stContext, R.drawable.btn_round));
            pmenos.setEnabled(true);
        }
    }


}