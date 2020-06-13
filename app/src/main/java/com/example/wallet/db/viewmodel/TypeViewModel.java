package com.example.wallet.db.viewmodel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.wallet.db.entity.Type;
import com.example.wallet.db.repository.TypeRepository;

import java.util.List;

public class TypeViewModel extends AndroidViewModel {

    private TypeRepository typeRepository;
    // Using LiveData and caching what getAlphabetizedWords returns has several benefits:
    // - We can put an observer on the data (instead of polling for changes) and only update the
    //   the UI when the data actually changes.
    // - Repository is completely separated from the UI through the ViewModel.
    private LiveData<List<String>> allTypesString;
    private LiveData<List<Type>> allTypes;

    public TypeViewModel(Application application) {
        super(application);
        typeRepository = new TypeRepository(application);
        allTypesString = typeRepository.getAllTypesString();
        allTypes = typeRepository.getAllTypes();
    }

    public LiveData<List<String>> getAllTypesString() {
        return allTypesString;
    }

    public LiveData<List<Type>> getAllTypes() {
        return allTypes;
    }

    public LiveData<List<Type>> getAllExpenseTypes() {
        return typeRepository.getAllExpenseTypes();
    }

    public LiveData<List<Type>> getAllIncomeTypes() {
        return typeRepository.getAllIncomeTypes();
    }

    public void insertType(Type type) {
        typeRepository.insertType(type);
    }

    public void updateType(Type type) {
        typeRepository.updateType(type);
    }

    public void deleteType(Type type) {
        typeRepository.deleteType(type);
    }
}
