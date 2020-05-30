package com.example.wallet.db;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;

public class MonthlyBudgetRepository {

    private MonthlyBudgetDao monthlyBudgetDao;
    private LiveData<List<MonthlyBudget>> allMonthlyBudgets;

    public MonthlyBudgetRepository(Application application) {
        WalletDatabase db = WalletDatabase.getDatabase(application);
        monthlyBudgetDao = db.getMonthlyBudgetDao();
        allMonthlyBudgets = monthlyBudgetDao.getAllMonthlyBudgets();
    }

    // Room executes all queries on a separate thread.
    // Observed LiveData will notify the observer when the data has changed.
    public LiveData<List<MonthlyBudget>> getAllMonthlyBudgets() {
        return allMonthlyBudgets;
    }

    // You must call this on a non-UI thread or your app will throw an exception. Room ensures
    // that you're not doing any long running operations on the main thread, blocking the UI.
    public void insertMonthlyBudget(MonthlyBudget monthlyBudget) {
        WalletDatabase.databaseWriteExecutor.execute(() -> {
            monthlyBudgetDao.insertMonthlyBudget(monthlyBudget);
        });
    }
    public void deleteMonthlyBudget(MonthlyBudget monthlyBudget) {
        WalletDatabase.databaseWriteExecutor.execute(() -> {
            monthlyBudgetDao.deleteMonthlyBudget(monthlyBudget);
        });
    }

    public void updateMonthlyBudget(MonthlyBudget monthlyBudget) {
        WalletDatabase.databaseWriteExecutor.execute(() -> {
            monthlyBudgetDao.updateMonthlyBudget(monthlyBudget);
        });
    }
}
