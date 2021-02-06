package es.ujaen.virtualpresentation.activities.ui.delete;

import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProviders;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import es.ujaen.virtualpresentation.R;
import es.ujaen.virtualpresentation.connection.Connection;
import es.ujaen.virtualpresentation.data.Preferences;
import es.ujaen.virtualpresentation.data.User;

public class DeleteFragment extends Fragment {

    private DeleteViewModel mViewModel;

    private static Spinner presList;
    private static Button delete;

    private static int colorAccent;
    private static int colorGrey;

    public static DeleteFragment newInstance() {
        return new DeleteFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Log.i("HomeFragment", "Iniciando Fragmento Home");
        final View root = inflater.inflate(R.layout.fragment_delete, container, false);
        final Context context = root.getContext();
        User user = Preferences.getUser(context);
        final Connection con = new Connection(context, user);

        presList = root.findViewById(R.id.presentationList);
        delete = root.findViewById(R.id.delete);

        colorAccent = root.getResources().getColor(R.color.colorAccent, context.getTheme());
        colorGrey = root.getResources().getColor(R.color.colorGrey, context.getTheme());

        List<String> def_spinner = new ArrayList<>();
        def_spinner.add("No hay presentaciones para " + user.getNombreusuario());
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,
                android.R.layout.simple_spinner_dropdown_item,
                Collections.unmodifiableList(def_spinner));
        presList.setAdapter(adapter);
        activateDelete(false);
        con.getPresentations(presList, 1);

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String presentacion = presList.getSelectedItem().toString();
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("¿Está seguro?");
                builder.setMessage("¿Desea eliminar la presentación: "+presentacion+"?");

                builder.setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(context, "Sí", Toast.LENGTH_SHORT).show();
                        con.deletePresentation(presentacion);
                    }
                });
                builder.setNegativeButton("No", null);
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(DeleteViewModel.class);
        // TODO: Use the ViewModel
    }

    /**
     * Activa o desactiva el botón para eliminar presentación
     * @param activar
     */
    public static void activateDelete(boolean activar){
        if (activar){
            delete.setClickable(true);
            delete.setEnabled(true);
            presList.setClickable(true);
            delete.setBackgroundTintList(ColorStateList.valueOf(colorAccent));
        }else {
            delete.setClickable(false);
            delete.setEnabled(false);
            presList.setClickable(false);
            delete.setBackgroundTintList(ColorStateList.valueOf(colorGrey));
        }
    }

}