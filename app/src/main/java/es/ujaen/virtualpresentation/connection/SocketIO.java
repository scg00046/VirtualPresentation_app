package es.ujaen.virtualpresentation.connection;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

import es.ujaen.virtualpresentation.activities.MainActivity;
import es.ujaen.virtualpresentation.activities.PresentationActivity;
import es.ujaen.virtualpresentation.data.Constant;
import es.ujaen.virtualpresentation.data.Preferences;
import es.ujaen.virtualpresentation.data.Session;
import es.ujaen.virtualpresentation.data.User;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import static es.ujaen.virtualpresentation.activities.PresentationActivity.buttonsClickable;
import static es.ujaen.virtualpresentation.activities.PresentationActivity.snackbarReconnection;

/**
 * Clase SocketIO, conexión con el servidor para comunicación mediante socketIO
 *  @author Sergio Caballero Garrido
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
        /*Log.i("SocketCrear","Creando del socket...");
        try {
            socket = IO.socket("http://192.168.1.10:8080");

            socket.on(Socket.EVENT_CONNECT, onConnect);
            socket.on(Socket.EVENT_DISCONNECT, onDisconnect);
            socket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
            socket.on(sesion.getNombreSesion(), newMessageListner);
            socket.connect();    //Connect socket to server

            Log.i("SocketCrear","Creado el socket");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }*/
    }

    @Override
    public synchronized void start() { //TODO revisar hilo
        super.start();
        Log.i("SocketCrear","Creando del socket...");
        //IO.Options opts = new IO.Options();

        try {
            Log.i("SOCKET", "start: try");
            socket = IO.socket(Constant.SERVER/*,opts*/);

            socket.on(Socket.EVENT_CONNECT, onConnect); //Evento Conexión
            socket.on(Socket.EVENT_DISCONNECT, onDisconnect); //Evento desconexión
            socket.on(Socket.EVENT_CONNECT_ERROR, onConnectError); //Error de conexión
            socket.on(nombreSesion, newMessageListner); //Recepción de mensajes
            //socket.on(Constant.ROOM_SOCKET, newMessageListner);
            socket.connect();    //Conexión del socket con el servidor
            Thread.sleep(500);
            Log.i("SocketCrear","Socket creado, usuario: "+usuario.getNombreusuario()+", sesion: "+nombreSesion);
            do {
                checkConnection();
            } while (!isConnected());
            Log.i("SOCKET", "finish");
            //sendMessage("OK");
            //Log.i("SocketCrear","Socket abierto (Ok enviado)");
            //setListening();
        } catch (URISyntaxException | InterruptedException e) {
            e.printStackTrace();
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

    private Emitter.Listener onDisconnect = new Emitter.Listener(){
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
                        Log.i("SocketNewMessage", "Usuario: "+origen+" message: "+mensaje);
                        String usuarioWeb = "web-"+usuario.getNombreusuario();
                        if (origen.equals(usuarioWeb)){
                            if (mensaje.startsWith("Página")){
                                int pagina = Integer.parseInt(mensaje.split(" ")[1]);
                                PresentationActivity.setPage(pagina);
                            }else if ( mensaje.equals("Comando no reconocido") ){//TODO recepción de errores
                                Toast.makeText(activity, "Comando no reconocido", Toast.LENGTH_SHORT).show();
                            } else if (mensaje.equals("salir")){
                                Preferences.refreshSession(activity.getApplicationContext(), sesion, PresentationActivity.getPaginaActual());
                                snackbarReconnection("Se ha cerrado la presentación del navegador", "Salir");
                                Intent intent = new Intent(activity, MainActivity.class);
                                intent.putExtra("qr", true);
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
    private void checkConnection(){
        if (!isConnected()){
            snackbarReconnection("No se ha conectado correctamente", "Reintentar");
        } else {
            startSession();
        }
    }

    /**
     * Inicia la sesión
     */
    public void startSession(){
        sendMessage("OK");
        int numero = sesion.getPaginaInicio(); //No lo recibe correctamente por el tiempo de espera
        Log.i("Socket", "startSession: pagina numero"+numero);
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
     * @param texto
     */
    public void sendMessage(String texto) {
        Log.i("SocketPreSend","Enviando mensaje ...");
        JSONObject mensaje = new JSONObject();
        try {
            mensaje.put("sesion", nombreSesion);
            mensaje.put("usuario", usuario.getNombreusuario());
            mensaje.put("mensaje", texto);
            if (socket.connected()) { //Comprueba que está conectado el socket
                Log.i("SocketSend","Envia mensaje");
                socket.emit(Constant.ROOM_SOCKET, mensaje);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Envío de notas al servidor
     * @param fijarnota
     * @param texto
     */
    public void sendNote( boolean fijarnota, String texto) { //TODO revisar parámetros a enviar por socket
        Log.i("SocketPreSend","Enviando mensaje ...");
        JSONObject mensaje = new JSONObject();
        try {
            mensaje.put("sesion", nombreSesion);
            //mensaje.put("usuario", usuario.getNombreusuario()+"-nota");
            mensaje.put("usuario", usuario.getNombreusuario());
            mensaje.put("fijar", fijarnota);
            mensaje.put("nota", texto);
            if (socket.connected()) { //Comprueba que está conectado el socket
                Log.i("SocketSend","Envia mensaje");
                socket.emit(Constant.ROOM_SOCKET, mensaje);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void stopSesion(){
        Log.i("SocketStop", "Desconexión del socket");
        if (socket != null) {
            socket.disconnect();
            socket = null;
        }
    }

    public boolean isConnected(){
        return socket.connected();
    }

    public void setListening () {
        socket.on(nombreSesion, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.i("SocketReceive", "Recibido:: "+args);
            }
        });
    }
}
