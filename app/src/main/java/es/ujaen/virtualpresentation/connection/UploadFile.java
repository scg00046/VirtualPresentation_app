package es.ujaen.virtualpresentation.connection;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import net.gotev.uploadservice.MultipartUploadRequest;
import net.gotev.uploadservice.Placeholders;
import net.gotev.uploadservice.ServerResponse;
import net.gotev.uploadservice.UploadInfo;
import net.gotev.uploadservice.UploadNotificationAction;
import net.gotev.uploadservice.UploadNotificationConfig;
import net.gotev.uploadservice.UploadService;
import net.gotev.uploadservice.UploadServiceSingleBroadcastReceiver;
import net.gotev.uploadservice.UploadStatusDelegate;

import java.util.UUID;

import es.ujaen.virtualpresentation.BuildConfig;
import es.ujaen.virtualpresentation.R;
import es.ujaen.virtualpresentation.data.Constant;
import es.ujaen.virtualpresentation.data.User;

/**
 * Clase UploadFile, permite la subida de archivos al servidor
 * @author Sergio Caballero Garrido
 */
public class UploadFile {

    private Context context;
    private User usuario;
    private TextView messageText;

    private final String channelNotification =  "CanalNotificacion";

    /**
     * Constructor de la clase
     * @param context contexto de la aplicación
     * @param usuario usuario autenticado
     */
    public UploadFile(Context context, User usuario) {
        Log.d("UploadFile","Conexion creada");
        this.context = context;
        this.usuario = usuario;
        //this.messageText = messageText;
        createNotificationChannel();

        UploadService.NAMESPACE = BuildConfig.APPLICATION_ID;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel(
                    channelNotification,
                    "Notificacion",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            manager.createNotificationChannel(channel);
            Log.i("Upload", "createNotificationChannel: CREATED");
        }
    }

    /**
     * Subida de ficheros
     * @param uriFile uri del fichero a subir
     * @param filename nombre del fichero
     */
    public void subir(final String uriFile, final String filename){
        Log.i("upload", "subir: uri "+uriFile.toString()+" filename "+filename);
        Resources res = context.getResources();
        UploadNotificationConfig upNotification = new UploadNotificationConfig();
        upNotification.setTitleForAllStatuses(res.getString(R.string.not_up_title))
                .setNotificationChannelId(channelNotification)
                .setRingToneEnabled(false);
        //TODO revisar textos subida
        upNotification.getCompleted().message = res.getString(R.string.not_up_completed)+": "+filename;
        //upNotification.getCompleted().autoClear = true;
        upNotification.getError().message = res.getString(R.string.not_up_error);
        upNotification.getProgress().message = res.getString(R.string.not_up_progress)+ " (" +Placeholders.PROGRESS + ")";
        upNotification.getCancelled().message = "Cancelado"; //TODO (implementar cancelar)

        Log.i("upload", "subir: notificacion creada");
        try {

            final String uploadId = UUID.randomUUID().toString();
            Log.i("upload", "subir: inicio try UUID"+uploadId);
            new MultipartUploadRequest(context, uploadId, Constant.getUrlUser(usuario.getNombreusuario()))
                    .addFileToUpload(uriFile, "presentacion", filename)
                    .setNotificationConfig(upNotification)
                    .setMethod("PUT")
                    .addHeader(Constant.HEADER_AUT, usuario.getToken())
                    .setUtf8Charset()
                    .setMaxRetries(2)
                    .setDelegate(new UploadStatusDelegate() {
                        @Override
                        public void onProgress(Context context, UploadInfo uploadInfo) {
                            //messageText.setText(uploadInfo.getProgressPercent()+" % upload");
                        }
                        @Override
                        public void onError(Context context, UploadInfo uploadInfo, ServerResponse serverResponse, Exception e) {
                            //messageText.setTextColor(Color.RED);
                            //messageText.setText("Error: "+e.getMessage());
                            Log.e("UploadFile", "onError: " + uploadId + "\nResponse code: "+serverResponse.getHttpCode());
                            int codigoResp = serverResponse.getHttpCode();
                            String mensaje = "";
                            switch (codigoResp) {
                                case 400:
                                    mensaje = "No se han enviado datos";
                                    break;
                                case 401:
                                    mensaje = "Autenticación incorrecta";
                                    break;
                                case 403:
                                    mensaje = "Los datos enviados son incorrectos o incompletos";
                                    break;
                                case 409:
                                    mensaje = "La presentación ya existe";
                                    break;
                                case 500:
                                    mensaje = "Error en el servidor";
                                    break;
                                default:
                                    mensaje = "Se ha producido un error";
                                    break;
                            }
                            Toast.makeText(context,mensaje,Toast.LENGTH_SHORT).show();
                        }
                        @Override
                        public void onCompleted(Context context, UploadInfo uploadInfo, ServerResponse serverResponse) {
                            int codigoResp = serverResponse.getHttpCode();
                            if (codigoResp == 200) {
                                Toast.makeText(context,"Presentación subida correctamente.", Toast.LENGTH_SHORT).show();
                            }
                            Log.i("UploadFile", "Subida Completada: "+uploadId + "Respuesta: "+ codigoResp);
                        }
                        @Override
                        public void onCancelled(Context context, UploadInfo uploadInfo) {
                            //messageText.setTextColor(Color.RED);
                            //messageText.setText("Subida cancelada!");
                            Log.i("UploadFile", "Subida cancelada: "+uploadId);
                        }
                    })
                    .startUpload();
            Log.i("upload", "subir: antes del catch");
        } catch (Exception exc) {
            Log.e("UploadFile", exc.getMessage()+" "+exc.getLocalizedMessage());
            /*messageText.setTextColor(Color.RED);
            messageText.setText("Exception General: "+exc.getLocalizedMessage());*/
        }
    }
}
