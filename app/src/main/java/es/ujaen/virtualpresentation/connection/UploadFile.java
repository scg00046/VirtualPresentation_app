package es.ujaen.virtualpresentation.connection;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import net.gotev.uploadservice.MultipartUploadRequest;
import net.gotev.uploadservice.ServerResponse;
import net.gotev.uploadservice.UploadInfo;
import net.gotev.uploadservice.UploadNotificationConfig;
import net.gotev.uploadservice.UploadStatusDelegate;

import java.util.UUID;

import es.ujaen.virtualpresentation.data.Constant;

public class UploadFile {

    private Context context;

    private TextView messageText;

    public UploadFile(Context context/*, TextView messageText*/) {
        Log.d("UploadFile","Conexion creada");
        this.context = context;
        //this.messageText = messageText;
    }

    public void subir(final Uri uriFile, final String filename){
        try {
            //filenameGaleria = getFilename();
            final String uploadId = UUID.randomUUID().toString();
            new MultipartUploadRequest(context, uploadId, Constant.getUrlUser("admin")) //TODO obtener de forma automática
                    //.addFileToUpload(uriFile.getPath(), "sampleFile")
                    .addFileToUpload(uriFile.getPath(), "presentacion", filename)
                    //.addParameter("filename", filename)
                    .setNotificationConfig(new UploadNotificationConfig())
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
                            Log.i("UploadFile", "Error de subida: "+uploadId+"\nExcepción: "+e.getLocalizedMessage());
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
