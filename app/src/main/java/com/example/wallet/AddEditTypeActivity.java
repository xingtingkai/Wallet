package com.example.wallet;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class AddEditTypeActivity extends AppCompatActivity {

    protected static final String EXTRA_ID =
            "com.example.wallet.EXTRA_ID";
    protected static final String EXTRA_NAME =
            "com.example.wallet.EXTRA_NAME";
    protected static final String EXTRA_IS_EXPENSE_TYPE =
            "com.example.wallet.EXTRA_IS_EXPENSE_TYPE";
    protected static final String EXTRA_ORIGINAL_IS_EXPENSE_TYPE =
            "com.example.wallet.EXTRA_ORIGINAL_IS_EXPENSE_TYPE";
    protected static final String EXTRA_OPERATION =
            "com.example.wallet.EXTRA_OPERATION";

    private TextInputEditText editTextType;
    private RadioButton radioButtonExpense;
    private RadioButton radioButtonIncome;

    private TextInputLayout textInputLayoutName;

    private boolean isExpenseType;
    private boolean originalIsExpenseType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_type);

        editTextType = findViewById(R.id.edit_text_type);
        radioButtonExpense = findViewById(R.id.radio_expense);
        radioButtonIncome = findViewById(R.id.radio_income);

        // bring focus to edit text and show keybaord
        if (editTextType.requestFocus()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }

        // submit form when clicked 'enter' on soft keyboard
        editTextType.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    createOrSaveType();
                    handled = true;
                }
                return handled;
            }
        });

        textInputLayoutName = findViewById(R.id.edit_text_type_input_layout);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_close);
        }

        extractIntent();
    }

    private void extractIntent() {
        Intent intent = getIntent();
        if (intent.hasExtra(EXTRA_ID)) {
            editTextType.setText(intent.getStringExtra(EXTRA_NAME));

            if (intent.getBooleanExtra(EXTRA_IS_EXPENSE_TYPE, true)) {
                radioButtonExpense.setChecked(true);
                isExpenseType = true;
                originalIsExpenseType = true;
            } else {
                radioButtonIncome.setChecked(true);
                isExpenseType = false;
                originalIsExpenseType = false;
            }
            // set as default option
        } else {
            radioButtonExpense.setChecked(true);
            isExpenseType = true;
            originalIsExpenseType = true;
        }
    }

    private void createOrSaveType() {

        String name = editTextType.getText().toString().trim();

        boolean violateInputValidation = false;

        if (name.isEmpty()) {
            textInputLayoutName.setError("Please insert a name.");
            violateInputValidation = true;
        } else if (name.length() >= 100) {
            textInputLayoutName.setError("Please insert a name less than 100 characters.");
            violateInputValidation = true;
        } else {
            textInputLayoutName.setError("");
        }

        if (violateInputValidation) {
            return;
        }

        Intent newType = createIntent(name, "save");

        setResult(RESULT_OK, newType);
        finish();
    }

    /*
    if delete a type that a transaction uses, it won't crash.
    the spinner will just set the type to the top option as default
     */
    private void deleteType() {

        String name = editTextType.getText().toString().trim();

        if (name.length() >= 100) {
            name = "";
        }

        Intent oldType = createIntent(name, "delete");

        setResult(RESULT_OK, oldType);
        finish();
    }

    private Intent createIntent(String name, String operation) {

        Intent type = new Intent();
        type.putExtra(EXTRA_NAME, name);
        type.putExtra(EXTRA_OPERATION, operation);

        type.putExtra(EXTRA_IS_EXPENSE_TYPE, isExpenseType);
        type.putExtra(EXTRA_ORIGINAL_IS_EXPENSE_TYPE, originalIsExpenseType);

        Long id = getIntent().getLongExtra(EXTRA_ID, -1);
        if (id != -1) {
            type.putExtra(EXTRA_ID, id);
        }

        return type;
    }

    public void onRadioButtonClicked(View view) {

        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch (view.getId()) {
            case R.id.radio_expense:
                if (checked)
                    isExpenseType = true;
                break;
            case R.id.radio_income:
                if (checked)
                    isExpenseType = false;
                break;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.add_item_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save_item:
                createOrSaveType();
                return true;
            case R.id.delete_item:
                deleteType();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}