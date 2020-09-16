package es.ujaen.virtualpresentation.activities.ui.about;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import es.ujaen.virtualpresentation.R;

public class AboutViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public AboutViewModel() {
        mText = new MutableLiveData<>();
        //mText.setValue(String.valueOf(R.string.menu_about));
    }

    public LiveData<String> getText() {
        return mText;
    }
}