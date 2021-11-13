package es.ujaen.virtualpresentation.connection;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import es.ujaen.virtualpresentation.activities.MainActivity;
import es.ujaen.virtualpresentation.activities.PresentationActivity;
import es.ujaen.virtualpresentation.data.Constant;
import es.ujaen.virtualpresentation.data.Preferences;
import es.ujaen.virtualpresentation.data.Session;
import es.ujaen.virtualpresentation.data.User;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import okhttp3.OkHttpClient;


/**
 * Clase SocketIO, conexión con el servidor para comunicación mediante socketIO
 *
 * @author Sergio Caballero Garrido
 */
public class SocketIO extends Thread {

    private Socket socket = null;
    private Activity activity;
    private Session sesion;
    private User usuario;
    private String nombreSesion;

    public SocketIO(Activity activity, User usuario, Session sesion) {
        this.activity = activity;
        this.usuario = usuario;
        this.sesion = sesion;
        this.nombreSesion = sesion.getSesionCodigo();

        start();
    }

    @Override
    public synchronized void start() { //TODO revisar hilo
        super.start();
        Log.i("SocketCrear", "Creando del socket...");
        IO.setDefaultOkHttpWebSocketFactory(okHttpClient());
        IO.setDefaultOkHttpCallFactory(okHttpClient());
        IO.Options opts = new IO.Options();
        opts.path = Constant.URL_SOCKETIO;
        opts.reconnection = true;
        opts.reconnectionAttempts = 5;
        opts.secure = true;
        opts.callFactory = okHttpClient();
        opts.webSocketFactory = okHttpClient();

        try {
            Log.i("SOCKET", "start: try");
            socket = IO.socket(Constant.SERVER, opts);

            socket.on(Socket.EVENT_CONNECT, onConnect); //Evento Conexión
            socket.on(Socket.EVENT_DISCONNECT, onDisconnect); //Evento desconexión
            socket.on(Socket.EVENT_CONNECT_ERROR, onConnectError); //Error de conexión
            socket.on(nombreSesion, newMessageListner); //Recepción de mensajes
            socket.connect();    //Conexión del socket con el servidor
            Thread.sleep(500);
            Log.i("SocketCrear", "Socket creado, usuario: " + usuario.getNombreusuario() + ", sesion: " + nombreSesion + " socket activo " + isConnected());
            checkConnection();
            Log.i("SOCKET", "finish");
        } catch (URISyntaxException | InterruptedException e) {
            e.printStackTrace();
            Log.e("SocketConnectError", "onConnectError: " + e.getMessage());
            PresentationActivity.snackbarReconnection("No se ha podido conectar", "Salir");
        }
    }

    @Override
    public void interrupt() {
        super.interrupt();
        stopSesion();
    }


    private Emitter.Listener onConnectError = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            Log.e("SocketConnectError", "onConnectError: " + args);
        }
    };

    private Emitter.Listener onDisconnect = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            if (activity != null)
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Log.d("SocketDisconnect", "onDisconnect: " + args);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
        }
    };

    private Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            Log.d("SocketConnect", "onConnect: " + args);
        }
    };

    /**
     * Recepción de mensajes (room de la sesión abierta)
     */
    private Emitter.Listener newMessageListner = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    try {
                        String origen = data.getString("usuario");
                        String mensaje = data.getString("mensaje");
                        Log.i("SocketNewMessage", "Usuario: " + origen + " message: " + mensaje);
                        String usuarioWeb = "web-" + usuario.getNombreusuario();
                        if (origen.equals(usuarioWeb)) {
                            if (mensaje.startsWith("Página")) {
                                String[] msj = mensaje.split(":");
                                if (msj[0].equals("Página inicial"))
                                    PresentationActivity.buttonsClickable(true);
                                int pagina = Integer.parseInt(msj[1]);
                                PresentationActivity.setPage(pagina);
                            } else if (mensaje.equals("Comando no reconocido")) {
                                Toast.makeText(activity, "Comando no reconocido", Toast.LENGTH_SHORT).show();
                            } else if (mensaje.equals("salir")) {
                                Preferences.refreshSession(activity.getApplicationContext(), sesion, PresentationActivity.getPaginaActual());
                                Intent intent = new Intent(activity, MainActivity.class);
                                intent.putExtra("qr", true);
                                intent.putExtra("text", "Se ha cerrado la sesión desde el navegador");
                                activity.startActivity(intent);
                                activity.finish();
                            }
                        }
                    } catch (JSONException e) {
                        return;
                    }
                } //Fin run()
            }); //Fin runOnUiThread
        } //Fin call()
    }; //Fin listener


    /**
     * Comprueba que se ha conectado el socket, en caso negativo preguntará si desea reintentarlo
     */
    private void checkConnection() {
        if (!isConnected()) {
            PresentationActivity.snackbarReconnection("No se ha conectado correctamente", "Reintentar");
        } else {
            startSession();
        }
    }

    /**
     * Inicia la sesión
     */
    public void startSession() {
        sendMessage("OK");
        int numero = sesion.getPaginaInicio();
        Log.i("Socket", "startSession: pagina numero" + numero);
        try {
            Thread.sleep(500);
            if (numero > 1) {
                sendMessage("pnum-" + numero);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Envío de mensajes (comandos) al servidor
     *
     * @param texto
     */
    public void sendMessage(String texto) {
        Log.i("SocketPreSend", "Enviando mensaje ...");
        JSONObject mensaje = new JSONObject();
        try {
            mensaje.put("sesion", nombreSesion);
            mensaje.put("usuario", usuario.getNombreusuario());
            mensaje.put("mensaje", texto);
            if (socket.connected()) { //Comprueba que está conectado el socket
                Log.i("SocketSend", "Envia mensaje");
                socket.emit(Constant.ROOM_SOCKET, mensaje);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Envío de notas al servidor
     *
     * @param fijarnota
     * @param texto
     */
    public void sendNote(boolean fijarnota, String texto, int pagina) {
        Log.i("SocketPreSend", "Enviando mensaje ...");
        JSONObject mensaje = new JSONObject();
        try {
            mensaje.put("sesion", nombreSesion);
            mensaje.put("usuario", usuario.getNombreusuario());
            mensaje.put("fijar", fijarnota);
            mensaje.put("nota", texto);
            mensaje.put("pagina", pagina);
            if (socket.connected()) { //Comprueba que está conectado el socket
                Log.i("SocketSend", "Envia mensaje");
                socket.emit(Constant.ROOM_SOCKET, mensaje);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void stopSesion() {
        Log.i("SocketStop", "Desconexión del socket");
        if (socket != null) {
            socket.disconnect();
            socket = null;
        }
    }

    public void setConnect() {
        socket.connect();
    }

    public boolean isConnected() {
        return socket.connected();
    }

    public void setListening() {
        socket.on(nombreSesion, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.i("SocketReceive", "Recibido:: " + args);
            }
        });
    }

    /**
     * Permite la conexión mediante HTTPS
     * @return
     */
    private OkHttpClient okHttpClient() {

        HostnameVerifier myHostnameVerifier = new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        };
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            }

            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            }

            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        }};

        SSLContext mySSLContext = null;
        try {
            mySSLContext = SSLContext.getInstance("TLS");
            try {
                mySSLContext.init(null, trustAllCerts, null);
            } catch (KeyManagementException e) {
                e.printStackTrace();
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        OkHttpClient okHttpClient = new OkHttpClient.Builder().hostnameVerifier(myHostnameVerifier).sslSocketFactory(mySSLContext.getSocketFactory()).build();
        return okHttpClient;
    }
}
