package com.example.wallet;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class AddEditMonthlyBudgetActivity extends AppCompatActivity {

    public static final String EXTRA_ID =
            "com.example.wallet.EXTRA_ID";
    public static final String EXTRA_OPERATION =
            "com.example.wallet.EXTRA_OPERATION";
    public static final String EXTRA_BUDGET =
            "com.example.wallet.EXTRA_BUDGET";
    public static final String EXTRA_YEAR =
            "com.example.wallet.EXTRA_YEAR";
    public static final String EXTRA_MONTH =
            "com.example.wallet.EXTRA_MONTH";

    private TextView textViewYear;
    private TextView textViewMonth;
    private EditText editTextBudget;

    // cannot add or delete, only can save
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_monthly_budget);

        textViewYear = findViewById(R.id.textView_year);
        textViewMonth = findViewById(R.id.textView_month);
        editTextBudget = findViewById(R.id.edit_text_budget);

        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close);

        extractIntent();
    }

    private void extractIntent() {
        Intent intent = getIntent();
        if (intent.hasExtra(EXTRA_ID)) {
            textViewYear.setText(intent.getIntExtra(EXTRA_YEAR, 0) + "");
            textViewMonth.setText(intent.getIntExtra(EXTRA_MONTH, 0) + "");
            editTextBudget.setText(intent.getDoubleExtra(EXTRA_BUDGET, 0) + "");
        }
    }

    private void createOrSaveMonthlyBudget(String operation) {

        int year = Integer.parseInt(textViewYear.getText().toString());
        int month = Integer.parseInt(textViewMonth.getText().toString());
        String budgetString = editTextBudget.getText().toString();
        double budget = 0;

        if (!budgetString.isEmpty()) {
            budget = Double.parseDouble(budgetString);
        }

        if (budget == 0) {
            Toast.makeText(this, "Please insert a budget", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent newMonthlyBudget = createIntent(budget, year, month, operation);

        setResult(RESULT_OK, newMonthlyBudget);
        finish();
    }

    /*
    if delete a type that a transaction uses, it won't crash.
    the spinner will just set the type to the top option as default
     */
    private void deleteMonthlyBudget() {

        int year = Integer.parseInt(textViewYear.getText().toString());
        int month = Integer.parseInt(textViewMonth.getText().toString());
        String budgetString = editTextBudget.getText().toString();
        double budget = 0;

        if (!budgetString.isEmpty()) {
            budget = Double.parseDouble(budgetString);
        }

        Intent oldMonthlyBudget = createIntent(budget, year, month, "delete");

        setResult(RESULT_OK, oldMonthlyBudget);
        finish();
    }

    private Intent createIntent(double budget, int year, int month, String operation) {

        Intent monthlyBudget = new Intent();
        monthlyBudget.putExtra(EXTRA_BUDGET, budget);
        monthlyBudget.putExtra(EXTRA_YEAR, year);
        monthlyBudget.putExtra(EXTRA_MONTH, month);
        monthlyBudget.putExtra(EXTRA_OPERATION, operation);

        Long id = getIntent().getLongExtra(EXTRA_ID, -1);
        if (id != -1) {
            monthlyBudget.putExtra(EXTRA_ID, id);
        }

        return monthlyBudget;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.add_transaction_menu, menu);

        MenuItem delete = menu.findItem(R.id.delete_transaction);
        delete.setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save_transaction:

                new AlertDialog.Builder(this)
                        .setTitle("Update")
                        .setMessage("Do you want to set this amount as a default budget?")
                        .setPositiveButton("yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                createOrSaveMonthlyBudget("save all");
                            }
                        })
                        .setNegativeButton("no", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                createOrSaveMonthlyBudget("save");
                            }
                        })
                        .setNeutralButton("cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .create().show();

                return true;
            case R.id.delete_transaction:
                deleteMonthlyBudget();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}