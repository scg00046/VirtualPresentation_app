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

public class UploadFile {

    private Context context;
    private User usuario;
    private TextView messageText;

    public UploadFile(Context context, User usuario) {
        Log.d("UploadFile","Conexion creada");
        this.context = context;
        this.usuario = usuario;
        //this.messageText = messageText;
    }

    public void subir(final Uri uriFile, final String filename){
        Resources res = context.getResources();
        UploadNotificationConfig upNotification = new UploadNotificationConfig();
        upNotification.setTitle(res.getString(R.string.not_up_title))
                .setCompletedMessage(res.getString(R.string.not_up_completed)+ Placeholders.UPLOAD_RATE + " (" + Placeholders.PROGRESS + ")")
                .setInProgressMessage(res.getString(R.string.not_up_progress)+ Placeholders.ELAPSED_TIME)
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
                    .setMaxRetries(2)
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
                            //messageText.setText("Imagen subida");
                            Toast.makeText(context,"Presentación subida correctamente.",Toast.LENGTH_SHORT).show();
                            Log.i("UploadFile", "Subida Completada: "+uploadId);
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
