package es.ujaen.virtualpresentation.connection;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

import es.ujaen.virtualpresentation.data.Session;
import es.ujaen.virtualpresentation.data.Usuario;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
//asyncTask<Parametros, progress, resultado>
public class SocketIO extends Thread {

    private Socket socket = null;
    private Activity activity;
    private Session sesion;
    private Usuario usuario;

    public SocketIO(Activity activity, Usuario usuario, Session sesion) {
        this.activity = activity;
        this.usuario = usuario;
        this.sesion = sesion;
        //start();
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
    public synchronized void start() {
        super.start();
        Log.i("SocketCrear","Creando del socket...");
        //IO.Options opts = new IO.Options();

        try {
            socket = IO.socket("http://192.168.1.10:8080"/*,opts*/); //TODO agregar a constantes

            socket.on(Socket.EVENT_CONNECT, onConnect);
            socket.on(Socket.EVENT_DISCONNECT, onDisconnect);
            socket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
            socket.on(sesion.getNombreSesion(), newMessageListner);
            socket.connect();    //Connect socket to server
            Thread.sleep(2000);
            Log.i("SocketCrear","Socket creado, usuario: "+usuario.getNombreusuario()+", sesion: "+sesion.getNombreSesion());
            sendMessage("OK");
            Log.i("SocketCrear","Socket abierto (Ok enviado)");
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
            Log.d("TAG", "onConnectError: " + args);
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
                            Log.d("TAG", "onDisconnect: " + args);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
        }
    };

    //TODO conectarse unicamente a la sesión creada (room definida)
    private Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            Log.d("TAG", "onConnect: " + args);
        }
    };

    private Emitter.Listener newMessageListner = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            Log.d("TAG", "newMessageListner: " + args);
        }
    };

    public void sendMessage(/*View v*/String texto) { //TODO realizar comprobación para que no vuelva a enviar varias veces
        Log.i("SocketPreSend","Enviando mensaje ...");
        JSONObject mensaje = new JSONObject();
        try {
            mensaje.put("usuario", usuario.getNombreusuario());
            mensaje.put("mensaje", texto);
            if (socket.connected()) {
                Log.i("SocketSend","Envia mensaje");

                socket.emit(sesion.getNombreSesion(),mensaje);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public void stopSesion(){
        if (socket != null) {
            socket.disconnect();
            socket = null;
        }
    }

    public boolean isConnected(){
        return socket.connected();
    }

}
