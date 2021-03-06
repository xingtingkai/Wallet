package com.xingtingkai.wallet.db;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.xingtingkai.wallet.db.dao.CarryOverDao;
import com.xingtingkai.wallet.db.dao.MonthlyBudgetDao;
import com.xingtingkai.wallet.db.dao.TransactionDao;
import com.xingtingkai.wallet.db.dao.TypeDao;
import com.xingtingkai.wallet.db.entity.CarryOver;
import com.xingtingkai.wallet.db.entity.MonthlyBudget;
import com.xingtingkai.wallet.db.entity.Transaction;
import com.xingtingkai.wallet.db.entity.Type;

import java.io.File;
import java.time.ZonedDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(version = 1, entities = {MonthlyBudget.class, Transaction.class, Type.class, CarryOver.class})
@TypeConverters({Converters.class})
public abstract class WalletDatabase extends RoomDatabase {

    abstract public MonthlyBudgetDao getMonthlyBudgetDao();

    abstract public TransactionDao getTransactionDao();

    abstract public TypeDao getTypeDao();

    abstract public CarryOverDao getCarryOverDao();

    // marking the instance as volatile to ensure atomic access to the variable
    public static volatile WalletDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public static WalletDatabase getDatabase(final Context context) {

        if (INSTANCE == null) {
            synchronized (WalletDatabase.class) {
                if (INSTANCE == null) {

                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            WalletDatabase.class, "WalletDatabase.db")
                            .addCallback(sRoomDatabaseCallback)
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }

        return INSTANCE;
    }

    /**
     * Override the onOpen method to populate the database.
     * For this sample, we clear the database every time it is created or opened.
     * <p>
     * If you want to populate the database only when the database is created for the 1st time,
     * override RoomDatabase.Callback()#onCreate
     */
    private static RoomDatabase.Callback sRoomDatabaseCallback = new RoomDatabase.Callback() {
        @Override
        public void onOpen(@NonNull SupportSQLiteDatabase db) {
            super.onOpen(db);

            // If you want to keep data through app restarts,
            // comment out the following block
            databaseWriteExecutor.execute(() -> {
                // one db should only have one instance of a carry over
//                CarryOverDao carryOverDao = INSTANCE.getCarryOverDao();
//                if (carryOverDao.getAllCarryOverList().size() == 0) {
//                    carryOverDao.insertCarryOver(CarryOver.create(1L, false));
//                }

                /*
                 db creates 10 years worth of monthly budgets if there are no more upcoming budgets
                 takes at least 2 minutes to populate whole list
                 */

                ZonedDateTime zonedDateTime = ZonedDateTime.now();

                int year = zonedDateTime.getYear();
                int month = zonedDateTime.getMonth().getValue();

                MonthlyBudgetDao monthlyBudgetDao = INSTANCE.getMonthlyBudgetDao();

                if (monthlyBudgetDao.getAllFutureMonthlyBudgetsList(year, month).size() == 0) {

                    int budget = 0;
                    int numOfYearsToCreate = 10;
                    int totalMonthlyBudgets = numOfYearsToCreate * 12;

                    for (int i = 0; i < totalMonthlyBudgets; i++) {

                        year = zonedDateTime.getYear();
                        month = zonedDateTime.getMonth().getValue();

                        MonthlyBudget monthlyBudget = MonthlyBudget.create(0, budget, year, month);
                        monthlyBudgetDao.insertMonthlyBudget(monthlyBudget);

                        zonedDateTime = zonedDateTime.plusMonths(1);
                    }
                }
            });
        }
    };

    public static void addTypes() {

        databaseWriteExecutor.execute(() -> {
            TypeDao typeDao = INSTANCE.getTypeDao();

            // id is 0, but sqlite auto generates an id at insertion
            typeDao.insertType(Type.create(0, "Food", true));
            typeDao.insertType(Type.create(0,"Transport", true));
            typeDao.insertType(Type.create(0,"Others", true));
            typeDao.insertType(Type.create(0,"Allowance", false));
        });
    }

    // only prepopulates when there is no existing database
    public static WalletDatabase prepopulateDB(final Context context, File file) {

        // takes some time to copy the table
        if (INSTANCE != null) {
            synchronized (WalletDatabase.class) {
                if (INSTANCE != null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            WalletDatabase.class, "WalletDatabase.db")
                            .createFromFile(file)
                            .addCallback(sRoomDatabaseCallback)
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    /*
     run using another thread so main thread wont lock up
     but room doesn't let us know when it finishes
     */
    public static void deleteAllData() {
        databaseWriteExecutor.execute(() -> INSTANCE.clearAllTables());
    }
}
