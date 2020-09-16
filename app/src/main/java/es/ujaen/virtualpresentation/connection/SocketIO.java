package es.ujaen.virtualpresentation.connection;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

import es.ujaen.virtualpresentation.activities.PresentationActivity;
import es.ujaen.virtualpresentation.data.Constant;
import es.ujaen.virtualpresentation.data.Session;
import es.ujaen.virtualpresentation.data.User;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
//asyncTask<Parametros, progress, resultado>
public class SocketIO extends Thread {

    private Socket socket = null;
    private Activity activity;
    private Session sesion;
    private User usuario;

    public SocketIO(Activity activity, User usuario, Session sesion) {
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
            socket = IO.socket(Constant.SERVER/*,opts*/);

            socket.on(Socket.EVENT_CONNECT, onConnect);
            socket.on(Socket.EVENT_DISCONNECT, onDisconnect);
            socket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
            socket.on(sesion.getNombreSesion(), newMessageListner);
            //socket.on(Constant.ROOM_SOCKET, newMessageListner);
            socket.connect();    //Connect socket to server
            Thread.sleep(500);
            Log.i("SocketCrear","Socket creado, usuario: "+usuario.getNombreusuario()+", sesion: "+sesion.getNombreSesion());
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

    //TODO conectarse unicamente a la sesión creada (room definida)
    private Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            Log.d("SocketConnect", "onConnect: " + args);
        }
    };

    private Emitter.Listener newMessageListner = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String origen;
                    String mensaje;
                    try {
                        origen = data.getString("usuario");
                        mensaje = data.getString("mensaje");
                        if (origen.equals("web")){//TODO nombre de sesión + -web
                            if (mensaje.startsWith("Página")){
                                int pagina = Integer.parseInt(mensaje.split(" ")[1]);
                                PresentationActivity.setPaginaActual(pagina);
                            }else if ( mensaje.startsWith("Error") ){

                            }
                        }
                    } catch (JSONException e) {
                        return;
                    }
                    Log.i("SocketNewMessage", "Usuario: "+origen+" message: "+mensaje);
                }
            });
        }
    };

    public void sendMessage(/*View v*/String texto) {
        Log.i("SocketPreSend","Enviando mensaje ...");
        JSONObject mensaje = new JSONObject();
        try {
            mensaje.put("sesion", sesion.getNombreSesion());
            mensaje.put("usuario", usuario.getNombreusuario());
            mensaje.put("mensaje", texto);
            if (socket.connected()) {
                Log.i("SocketSend","Envia mensaje");
                socket.emit(Constant.ROOM_SOCKET, mensaje);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void sendNote(String texto) {
        Log.i("SocketPreSend","Enviando mensaje ...");
        JSONObject mensaje = new JSONObject();
        try {
            mensaje.put("sesion", sesion.getNombreSesion());
            mensaje.put("usuario", usuario.getNombreusuario()+"-nota");
            mensaje.put("mensaje", texto);
            if (socket.connected()) {
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
        socket.on(sesion.getNombreSesion(), new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.i("SocketReceive", "Recibido:: "+args);
            }
        });
    }
}
