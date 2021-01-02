package es.ujaen.virtualpresentation.connection;

import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import net.gotev.uploadservice.MultipartUploadRequest;
import net.gotev.uploadservice.Placeholders;
import net.gotev.uploadservice.ServerResponse;
import net.gotev.uploadservice.UploadInfo;
import net.gotev.uploadservice.UploadNotificationConfig;
import net.gotev.uploadservice.UploadStatusDelegate;

import java.util.UUID;

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
    }

    /**
     * Subida de ficheros
     * @param uriFile uri del fichero a subir
     * @param filename nombre del fichero
     */
    public void subir(final Uri uriFile, final String filename){
        Resources res = context.getResources();
        UploadNotificationConfig upNotification = new UploadNotificationConfig();
        upNotification.setTitle(res.getString(R.string.not_up_title)) //TODO revisar textos subida
                .setCompletedMessage(res.getString(R.string.not_up_completed))
                .setInProgressMessage(res.getString(R.string.not_up_progress)+ Placeholders.PROGRESS) //Subiendo ... 50%
                .setErrorMessage(res.getString(R.string.not_up_error))
                .setRingToneEnabled(false);

        try {
            final String uploadId = UUID.randomUUID().toString();
            new MultipartUploadRequest(context, uploadId, Constant.getUrlUser(usuario.getNombreusuario()))
                    .addFileToUpload(uriFile.getPath(), "presentacion", filename)
                    .addParameter("id", String.valueOf(usuario.getId()))
                    .setNotificationConfig(upNotification)
                    .setMethod("PUT")
                    .setUtf8Charset()
                    .setMaxRetries(1)
                    .setDelegate(new UploadStatusDelegate() {
                        @Override
                        public void onProgress(UploadInfo uploadInfo) {
                            //messageText.setText(uploadInfo.getProgressPercent()+" % upload");
                        }
                        @Override
                        public void onError(UploadInfo uploadInfo, Exception e) {
                            //messageText.setTextColor(Color.RED);
                            //messageText.setText("Error: "+e.getMessage());
                            Log.i("UploadFile", "Error de subida: "+uploadId+"\nExcepción: "+e.getLocalizedMessage()
                                    +"\nError Mensaje"+e.getMessage());
                        }
                        @Override
                        public void onCompleted(UploadInfo uploadInfo, ServerResponse serverResponse) {
                            int codigoResp = serverResponse.getHttpCode();
                            String mensaje = "";
                            switch (codigoResp) {
                                case 200:
                                    mensaje = "Presentación subida correctamente.";
                                    break;
                                case 400:
                                    mensaje = "No se han enviado datos";
                                    break;
                                case 403:
                                    mensaje = "Usuario no registrado";
                                    break;
                                case 406:
                                    mensaje = "La presentación ya existe";
                                    break;
                                case 500:
                                    mensaje = "Error en el registro";
                                    break;
                                default:
                                    mensaje = "Respuesta desconocida";
                                    break;
                            }
                            Toast.makeText(context,mensaje,Toast.LENGTH_SHORT).show();
                            Log.i("UploadFile", "Subida Completada: "+uploadId + "Respuesta: "+ codigoResp);
                        }
                        @Override
                        public void onCancelled(UploadInfo uploadInfo) {
                            //messageText.setTextColor(Color.RED);
                            //messageText.setText("Subida cancelada!");
                            Log.i("UploadFile", "Subida cancelada: "+uploadId);
                        }
                    })
                    .startUpload();
        } catch (Exception exc) {
            Log.e("UploadFile", exc.getMessage()+" "+exc.getLocalizedMessage());
            /*messageText.setTextColor(Color.RED);
            messageText.setText("Exception General: "+exc.getLocalizedMessage());*/
        }
    }
}
