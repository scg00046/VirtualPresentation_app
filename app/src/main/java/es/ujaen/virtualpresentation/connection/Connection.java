package es.ujaen.virtualpresentation.connection;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import es.ujaen.virtualpresentation.activities.LoginActivity;
import es.ujaen.virtualpresentation.activities.MainActivity;
import es.ujaen.virtualpresentation.R;
import es.ujaen.virtualpresentation.activities.ui.delete.DeleteFragment;
import es.ujaen.virtualpresentation.activities.ui.home.HomeFragment;
import es.ujaen.virtualpresentation.data.Constant;
import es.ujaen.virtualpresentation.data.Preferences;
import es.ujaen.virtualpresentation.data.Presentations;
import es.ujaen.virtualpresentation.data.Session;
import es.ujaen.virtualpresentation.data.User;

/**
 * Clase Connection, realiza peticiones HTTP al servidor
 *
 * @author Sergio Caballero Garrido
 */
public class Connection {

    private Context context;
    private User user;

    /**
     * Constructor con el contexto, para el login
     *
     * @param context contexto de la aplicación
     */
    public Connection(Context context) {
        Log.d("Connection", "Conexion creada");
        this.context = context;
        handleSSLHandshake();
    }

    /**
     * Constructor con el contexto y usuario
     * Para obtener lista presentaciones, crear sesión y eliminar presentación
     *
     * @param context contexto de la aplicación
     * @param user    usuario autenticado
     */
    public Connection(Context context, User user) {
        Log.d("Connection", "Conexion creada");
        this.context = context;
        this.user = user;
        handleSSLHandshake();
    }

    /**
     * Autenticación con el servidor
     *
     * @param usuario
     * @param password
     * @param guardar
     */
    public void login(final String usuario, final String password, final boolean guardar, final boolean next) {
        StringRequest stringRequest;
        stringRequest = new StringRequest(Request.Method.POST, Constant.URL_LOGIN,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        Log.i("Login OK", "Se ha logeado correctamente. " + response);
                        User u;
                        try {
                            u = new User(new JSONObject(response));
                            Preferences.saveCredentials(context, u, guardar);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        if (next) {
                            Toast.makeText(context, context.getString(R.string.con_login_ok), Toast.LENGTH_LONG).show();
                            LoginActivity.stopLoading();
                            Intent intent = new Intent(context, MainActivity.class); //Pasa al activity principal
                            context.startActivity(intent);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Si se recibe un código de error
                Log.e("ConnectionError", "Error al iniciar sesión" + error);
                if (next) LoginActivity.stopLoading();
                if (error.networkResponse != null) { //Conexion realizada pero con respuesta de error
                    int statusCode = error.networkResponse.statusCode;
                    Log.i("ConnectionError", "Codigo error http: " + error.networkResponse.statusCode);
                    if (statusCode == 401) {
                        Toast.makeText(context, context.getString(R.string.con_login_wrongup), Toast.LENGTH_SHORT).show();
                    } else {
                        responseCode(statusCode);
                    }
                } else { //Error al realizar la conexión
                    Toast.makeText(context, context.getString(R.string.con_error_connection), Toast.LENGTH_SHORT).show();
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                // En este metodo se hace el envio de valores de la aplicacion al servidor
                Map<String, String> parametros = new Hashtable<String, String>();
                parametros.put("user", usuario);
                parametros.put("password", getHash(password));
                return parametros;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.add(stringRequest);
    }

    /**
     * Obtiene la lista de presentaciones almacenadas en el servidor
     *
     * @param presList spinner para completarlo con las presentaciones recibidas
     */
    public void getPresentations(final Spinner presList, final int fragmento) {
        final int[] statusCode = new int[1];
        StringRequest stringRequest = new StringRequest(Request.Method.GET, Constant.getUrlUser(user.getNombreusuario()),
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) { //Respuesta sin errores
                        Log.i("GetPresentaciones", "(" + statusCode[0] + ")" + response);
                        if (statusCode[0] == 200 || statusCode[0] == 304) {
                            try {
                                user.presentationsJSONtoList(new JSONArray(response));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            Log.i("GetPresentacionesList", user.getListaPresentacionesString().toString());
                            ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,
                                    android.R.layout.simple_spinner_dropdown_item, user.getListaPresentacionesString());
                            presList.setAdapter(adapter);
                            MainActivity.showHideLoading(context, false);
                            if (fragmento == 0) {
                                //Activa el botón para crear la sesión
                                HomeFragment.activateSendSession(true);
                            } else if (fragmento == 1) {
                                //Activa el botón para eliminar la presentación
                                DeleteFragment.activateDelete(true);
                            }
                            //Log.i("GetPresentacionesList", "Tamaño lista: " + user.getListaPresentacionesString().size());
                        } else {
                            Toast.makeText(context, context.getString(R.string.con_nopresentations), Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // En caso de tener algun error en la obtencion de los datos
                Log.w("ConnectionError", "Error al pedir las presentaciones, " + error.toString());
                MainActivity.showHideLoading(context, false);
                if (error.networkResponse != null) { //Conexion realizada pero con respuesta de error
                    int statusCode = error.networkResponse.statusCode;
                    responseCode(statusCode);
                } else { //Error en la conexión
                    Toast.makeText(context, context.getString(R.string.con_error_connection), Toast.LENGTH_SHORT).show();
                }
            }
        }) {
            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                statusCode[0] = response.statusCode;
                return super.parseNetworkResponse(response);
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put(Constant.HEADER_AUT, "Bearer " + user.getToken());
                return headers;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.add(stringRequest);
    }

    /**
     * Obtiene la lista de sesiones almacenadas en el servidor y las almacena en la memoria local
     */
    public void getSessions() {
        final int[] statusCode = new int[1];

        JsonArrayRequest jsonReq = new JsonArrayRequest(Request.Method.GET, Constant.getUrlSessionUser(user.getNombreusuario()),
                null, new Response.Listener<JSONArray>() {

            @Override
            public void onResponse(JSONArray response) { //Respuesta sin errores
                Log.i("GetSesiones", "(" + statusCode[0] + ")" + response);
                if (statusCode[0] == 200 || statusCode[0] == 304) {

                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject s = response.getJSONObject(i);
                            Session session = new Session(
                                    s.getString("usuario"),
                                    s.getString("sesion"),
                                    s.getString("presentacion"),
                                    s.getInt("paginas"));

                            if (!Preferences.exitsSession(context, session.getNombreSesion())) {
                                Preferences.saveSession(context, session);
                            } else {
                                Session sesionSaved = Preferences.getSession(context, session.getNombreSesion());
                                if (!sesionSaved.getPresentacion().equals(session.getPresentacion())) {
                                    Preferences.refreshSession(context, session, 1);
                                }
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // En caso de tener algun error en la obtencion de los datos
                Log.w("ConnectionError", "Error al pedir las presentaciones, " + error.toString());
                if (error.networkResponse != null) { //Conexion realizada pero con respuesta de error
                    int statusCode = error.networkResponse.statusCode;
                    responseCode(statusCode);
                } else { //Error en la conexión
                    //Toast.makeText(context, context.getString(R.string.con_error_connection), Toast.LENGTH_SHORT).show();
                }
            }
        }) {
            @Override
            protected Response<JSONArray> parseNetworkResponse(NetworkResponse response) {
                statusCode[0] = response.statusCode;
                return super.parseNetworkResponse(response);
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put(Constant.HEADER_AUT, "Bearer " + user.getToken());
                return headers;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.add(jsonReq);
    }

    /**
     * Método para crear una sesión en el servidor
     *
     * @param sesion       nombre de la sesión a crear
     * @param presentacion nombre de la presentación
     * @param view         vista para usar snackbar
     */
    public void createSession(final String sesion, final String presentacion, final View view) {
        StringRequest stringRequest;
        final int[] statusCode = new int[1];
        stringRequest = new StringRequest(Request.Method.POST, Constant.getUrlSessionUser(user.getNombreusuario()),
                new Response.Listener<String>() {

                    @SuppressLint("ResourceAsColor")
                    @Override
                    public void onResponse(String response) {

                        Log.i("ConnectionSession", "(" + statusCode[0] + ") Se ha creado la sesión. " + response);
                        String mensaje = "";
                        switch (response) {
                            case "Creada":
                                mensaje = context.getString(R.string.con_sess_created, sesion);
                                break;
                            case "Sin cambios":
                                mensaje = context.getString(R.string.con_sess_nochanges, sesion);
                                break;
                            case "Actualizada":
                                mensaje = context.getString(R.string.con_sess_updated, sesion);
                                break;
                        }
                        if (savePreferencesSession(sesion, presentacion, view)) {
                            Snackbar.make(view, mensaje, Snackbar.LENGTH_LONG).show();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.w("ConnectionError", "Error al crear sesión" + error);
                if (error.networkResponse != null) { //Conexion realizada pero con respuesta de error
                    int statusCode = error.networkResponse.statusCode;
                    responseCode(statusCode);
                } else { //Error en la conexión
                    Toast.makeText(context, context.getString(R.string.con_error_connection), Toast.LENGTH_SHORT).show();
                }
            }//Fin on error response
        }) {
            @Override
            protected Map<String, String> getParams() {//Parámetros de la petición

                Map<String, String> parametros = new Hashtable<String, String>();
                parametros.put("sesion", sesion);
                parametros.put("presentacion", presentacion);
                return parametros;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put(Constant.HEADER_AUT, "Bearer " + user.getToken());
                return headers;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.add(stringRequest);
    }

    @SuppressLint("ResourceAsColor")
    private boolean savePreferencesSession(String sesion, String presentacion, View view) {
        boolean resultado;
        Presentations present = user.getPresentationByName(presentacion);
        if (present != null) {
            Session session = new Session(user.getNombreusuario(), sesion, presentacion, present.getPaginas());
            Preferences.saveSession(context, session);
            TextView descripcion = (TextView) view.findViewById(R.id.sessionDescription);
            String url = Constant.getUrlUser(user.getNombreusuario()) + "/" + sesion;
            descripcion.setText(url);
            resultado = true;
        } else {
            Snackbar.make(view, "No existe la presentación", Snackbar.LENGTH_LONG)
                    .setBackgroundTint(R.color.colorErrorBackg)
                    .show();
            resultado = false;
        }
        return resultado;
    }

    /**
     * Método para borrar una presentación del servidor
     *
     * @param presentacion nombre de la presentación a eliminar
     */
    public void deletePresentation(final String presentacion, final Spinner presList) {
        Log.i("DeleteParams", "Presentacion: " + presentacion);
        StringRequest stringRequest;
        stringRequest = new StringRequest(Request.Method.DELETE, Constant.getUrlUser(user.getNombreusuario()),
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        Log.i("DeletePresentation", "Se ha eliminado correctamente. " + response);
                        Toast.makeText(context, context.getString(R.string.con_del_present), Toast.LENGTH_SHORT).show();
                        List<String> pres = user.getListaPresentacionesString();
                        pres.remove(presentacion);
                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,
                                android.R.layout.simple_spinner_dropdown_item, pres);
                        presList.setAdapter(adapter);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // En caso de tener algun error en la obtencion de los datos
                Log.e("ConnectionError", "Error al iniciar sesión" + error);
                if (error.networkResponse != null) { //Conexion realizada pero con respuesta de error
                    int statusCode = error.networkResponse.statusCode;
                    Log.i("ConnectionError", "Codigo error http: " + statusCode);
                    responseCode(statusCode);
                } else { //Error al realizar la conexión
                    Toast.makeText(context, context.getString(R.string.con_error_connection), Toast.LENGTH_SHORT).show();
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put(Constant.HEADER_AUT, "Bearer " + user.getToken());
                headers.put("presentacion", presentacion);
                return headers;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.add(stringRequest);
    }

    /**
     * Elimina una sesión creada del servidor
     *
     * @param sesion
     */
    public void deleteSession(final String sesion) {
        StringRequest stringRequest = new StringRequest(Request.Method.DELETE,
                Constant.getUrlSessionUser(user.getNombreusuario()),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(context, context.getString(R.string.con_sess_delete), Toast.LENGTH_SHORT).show();
                        Log.i("DeleteSession", "Se ha eliminado correctamente. " + response);
                        Preferences.deleteSession(context, sesion);
                    }

                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // En caso de tener algun error en la obtencion de los datos
                Log.e("ConnectionError", "Error al iniciar sesión" + error);
                if (error.networkResponse != null) { //Conexion realizada pero con respuesta de error
                    int statusCode = error.networkResponse.statusCode;
                    Log.i("ConnectionError", "Codigo error http: " + statusCode);
                    responseCode(statusCode);
                } else { //Error al realizar la conexión
                    Toast.makeText(context, context.getString(R.string.con_error_connection), Toast.LENGTH_SHORT).show();
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                //return super.getHeaders();
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put(Constant.HEADER_AUT, "Bearer " + user.getToken());
                headers.put("sesion", sesion);
                return headers;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.add(stringRequest);
    }

    public void createUser(final User u, final String pass, final String mail) {
        StringRequest stringRequest = new StringRequest(Request.Method.PUT, Constant.URL_LOGIN,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response.equals("OK")) {
                            Toast.makeText(context, context.getString(R.string.con_user_register), Toast.LENGTH_SHORT).show();
                            Log.i("CreateUser", "Se ha registrado correctamente. " + response);
                            LoginActivity.stopLoading();
                            LoginActivity.setUser(u.getNombreusuario());
                        }

                    }

                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // En caso de tener algun error en la obtencion de los datos
                Log.e("ConnectionError", "Error al iniciar sesión" + error);
                LoginActivity.stopLoading();
                if (error.networkResponse != null) { //Conexion realizada pero con respuesta de error
                    int statusCode = error.networkResponse.statusCode;
                    Log.i("ConnectionError", "Codigo error http: " + statusCode);
                    responseCode(statusCode);
                } else { //Error al realizar la conexión
                    Toast.makeText(context, context.getString(R.string.con_error_connection), Toast.LENGTH_SHORT).show();
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {

                Map<String, String> parametros = new Hashtable<String, String>();
                parametros.put("nombreusuario", u.getNombreusuario());
                parametros.put("password", getHash(pass));
                parametros.put("nombre", u.getNombre());
                parametros.put("apellidos", u.getApellidos());
                parametros.put("email", mail);
                return parametros;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.add(stringRequest);
    }

    public void logout() {

        StringRequest stringRequest = new StringRequest(Request.Method.GET, Constant.URL_LOGIN,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) { //Respuesta sin errores
                        Log.i("LogOut", "Respuesta: " + response);
                        //Toast.makeText(context, "Sesión de usuario cerrada", Toast.LENGTH_SHORT).show();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // En caso de tener algun error en la obtencion de los datos
                Log.w("ConnectionError", "Error al pedir las presentaciones, " + error.toString());
                if (error.networkResponse != null) { //Conexion realizada pero con respuesta de error
                    int statusCode = error.networkResponse.statusCode;
                } else { //Error en la conexión
                    //Toast.makeText(context, context.getString(R.string.con_error_connection), Toast.LENGTH_SHORT).show();
                }
            }
        }) {
            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                return super.parseNetworkResponse(response);
            }

            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put(Constant.HEADER_AUT, "Bearer " + user.getToken());
                return headers;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.add(stringRequest);
    }

    private void responseCode(int statusCode) {

        Intent intent = new Intent(context, LoginActivity.class);
        switch (statusCode) {
            case 400:
                Toast.makeText(context, "No se han enviado los datos necesarios", Toast.LENGTH_SHORT).show();
                break;
            case 403:
                Toast.makeText(context, "Los datos enviados son incorrectos o incompletos", Toast.LENGTH_SHORT).show();
                break;
            case 409:
                Toast.makeText(context, "El usuario ya existe", Toast.LENGTH_SHORT).show();
                break;
            case 401:
            case 419:
                if (statusCode == 401) {
                    Toast.makeText(context, context.getString(R.string.con_err_wrongauth), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, context.getString(R.string.con_err_authexp), Toast.LENGTH_SHORT).show();
                }
                try {
                    Thread.sleep(2000);
                    MainActivity.requestPassword(context);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                break;
            case 500:
                Toast.makeText(context, context.getString(R.string.con_err_server), Toast.LENGTH_SHORT).show();
                break;
            default:
                Toast.makeText(context, context.getString(R.string.con_err_unk), Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private String getHash(String password) {
        StringBuilder sb = new StringBuilder();
        String pass = "";
        try {
            byte[] plaintext = password.getBytes("UTF-8");

            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(plaintext);
            //Convertir array de bytes a string
            for (int i = 0; i < digest.length; i++) {
                final String hex = Integer.toHexString(0xff & digest[i]);
                if (hex.length() == 1)
                    sb.append('0');
                sb.append(hex);
            }
            pass = sb.toString();
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return pass;
    }


    @SuppressLint("TrulyRandom")
    public static void handleSSLHandshake() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }

                @Override
                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            }};

            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String arg0, SSLSession arg1) {
                    return true;
                }
            });
        } catch (Exception ignored) {
        }
    }
}
