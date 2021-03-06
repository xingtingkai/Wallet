package com.xingtingkai.wallet.db.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.sqlite.db.SupportSQLiteQuery;

import com.xingtingkai.wallet.db.WalletDatabase;
import com.xingtingkai.wallet.db.dao.TransactionDao;
import com.xingtingkai.wallet.db.entity.Transaction;

import java.util.List;
import java.util.concurrent.Future;

public class TransactionRepository {

    private TransactionDao transactionDao;

    public TransactionRepository(Application application) {
        WalletDatabase db = WalletDatabase.getDatabase(application);
        transactionDao = db.getTransactionDao();
    }

    /*
    Room executes all queries on a separate thread.
    Observed LiveData will notify the observer when the data has changed.
     */
    public LiveData<List<Transaction>> getExpenseRecurringTransactions(long millisecondsToday) {
        return transactionDao.getExpenseRecurringTransactions(millisecondsToday);
    }

    public LiveData<List<Transaction>> getIncomeRecurringTransactions(long millisecondsToday) {
        return transactionDao.getIncomeRecurringTransactions(millisecondsToday);
    }

    public LiveData<List<Transaction>> getAllTransactionsInAMonth(long millisecondsStart, long millisecondsEnd) {
        return transactionDao.getAllTransactionsInAMonth(millisecondsStart, millisecondsEnd);
    }

    public LiveData<List<Transaction>> getAllTransactionsInAMonthView(long millisecondsStart, long millisecondsEnd) {
        return transactionDao.getAllTransactionsInAMonthView(millisecondsStart, millisecondsEnd);
    }

    public LiveData<List<Transaction>> searchAllTransactions(String searchName) {
        return transactionDao.searchAllTransactions(searchName);
    }

    /*
     You must call this on a non-UI thread or your app will throw an exception. Room ensures
     that you're not doing any long running operations on the main thread, blocking the UI.
    */
    public void insertTransaction(Transaction transaction) {
        WalletDatabase.databaseWriteExecutor.execute(() ->
                transactionDao.insertTransaction(transaction));
    }
    public void deleteTransaction(Transaction transaction) {
        WalletDatabase.databaseWriteExecutor.execute(() ->
                transactionDao.deleteTransaction(transaction));
    }

    public void updateTransaction(Transaction transaction) {
        WalletDatabase.databaseWriteExecutor.execute(() ->
                transactionDao.updateTransaction(transaction));
    }

    public void deleteFutureRecurringTransactions(String transactionRecurringId, long milliseconds) {
        WalletDatabase.databaseWriteExecutor.execute(() ->
                transactionDao.deleteFutureRecurringTransactions(transactionRecurringId, milliseconds));
    }

    public Future<Integer> checkpoint(SupportSQLiteQuery supportSQLiteQuery) {
        return WalletDatabase.databaseWriteExecutor
                .submit(() -> transactionDao.checkpoint(supportSQLiteQuery));
    }

    public Future<List<String>> getAllTransactionNameString() {
        return WalletDatabase.databaseWriteExecutor
                .submit(transactionDao::getAllTransactionNameString);
    }

    public Future<Double> calculateExpensesInAMonth(long epochSecondsStart, long epochSecondsEnd){
        return WalletDatabase.databaseWriteExecutor
                .submit(() -> transactionDao.calculateExpensesInAMonth(epochSecondsStart, epochSecondsEnd));
    }
}
