package com.xingtingkai.wallet;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.DatePicker;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableList;
import com.xingtingkai.wallet.db.viewmodel.TransactionViewModel;
import com.xingtingkai.wallet.db.viewmodel.TypeViewModel;
import com.xingtingkai.wallet.helper.DateFormatter;
import com.xingtingkai.wallet.helper.DatePickerFragment;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class AddEditRepeatTransactionActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    protected static final String EXTRA_ID =
            "com.example.wallet.EXTRA_ID";
    protected static final String EXTRA_NAME =
            "com.example.wallet.EXTRA_NAME";
    protected static final String EXTRA_TYPENAME =
            "com.example.wallet.EXTRA_TYPENAME";
    protected static final String EXTRA_VALUE =
            "com.example.wallet.EXTRA_VALUE";
    protected static final String EXTRA_INSTANT =
            "com.example.wallet.EXTRA_INSTANT";
    protected static final String EXTRA_ZONE_ID =
            "com.example.wallet.EXTRA_ZONE_ID";
    protected static final String EXTRA_FREQUENCY =
            "com.example.wallet.EXTRA_FREQUENCY";
    protected static final String EXTRA_REPEAT =
            "com.example.wallet.EXTRA_REPEAT";
    protected static final String EXTRA_RECURRING_ID =
            "com.example.wallet.EXTRA_RECURRING_ID";
    protected static final String EXTRA_IS_EXPENSE_TYPE =
            "com.example.wallet.EXTRA_IS_EXPENSE_TYPE";
    protected static final String EXTRA_OPERATION =
            "com.example.wallet.EXTRA_OPERATION";

    private AutoCompleteTextView editTextName;
    private TextInputEditText editTextValue;
    private TextInputEditText editTextDate;
    private TextInputEditText editTextRepeat;

    private TextInputLayout textInputLayoutValue;
    private TextInputLayout textInputLayoutName;
    private TextInputLayout textInputLayoutRepeat;

    private static long epochSeconds;

    private Spinner spinnerFrequency;
    private ArrayAdapter<CharSequence> adapterFrequency;

    private Spinner spinnerType;

    private RadioButton radioButtonExpense;
    private RadioButton radioButtonIncome;

    private static String type;

    private static int frequency;

    private static ImmutableList<String> expenseTypeList;
    private static ImmutableList<String> incomeTypeList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_repeat_transaction);

        radioButtonExpense = findViewById(R.id.radio_expense);
        radioButtonIncome = findViewById(R.id.radio_income);

        editTextName = findViewById(R.id.edit_text_name);
        editTextName.setThreshold(1);
        editTextValue = findViewById(R.id.edit_text_value);
        editTextDate = findViewById(R.id.edit_text_dateTime);
        spinnerFrequency = findViewById(R.id.spinner);
        spinnerType = findViewById(R.id.spinner_repeat_type);
        editTextRepeat = findViewById(R.id.edit_text_num_repeat);

        textInputLayoutValue = findViewById(R.id.edit_text_num_value_input_layout);
        textInputLayoutName = findViewById(R.id.edit_text_num_name_input_layout);
        textInputLayoutRepeat = findViewById(R.id.edit_text_num_repeat_input_layout);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_close);
        }

        // on click
        editTextDate.setOnClickListener((View v) -> {
            Instant instant = Instant.ofEpochSecond(epochSeconds);
            ZoneId zoneId = ZoneId.systemDefault();
            DialogFragment datePicker = new DatePickerFragment(ZonedDateTime.ofInstant(instant, zoneId));
            datePicker.show(getSupportFragmentManager(), "date picker");
        });

        adapterFrequency = ArrayAdapter.createFromResource(this,
                R.array.frequency_array, android.R.layout.simple_spinner_item);
        adapterFrequency.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFrequency.setAdapter(adapterFrequency);
        spinnerFrequency.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String frequencyString = parent.getItemAtPosition(position).toString();
                Integer frequencyUnboxed = getFrequencyMap().get(frequencyString);

                if (frequencyUnboxed != null) {
                    frequency = frequencyUnboxed;
                } else {
                    // default to monthly
                    frequency = 12;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        expenseTypeList = new ImmutableList.Builder<String>().build();
        incomeTypeList = new ImmutableList.Builder<String>().build();
        initViewModel();

        // bring focus to edit text and show keyboard
        if (editTextValue.requestFocus()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }

        /*
         submit form when clicked 'enter' on soft keyboard
         on editor action
        */
        editTextRepeat.setOnEditorActionListener((TextView v, int actionId, KeyEvent event) -> {
            boolean handled = false;
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                if (!violateInputValidation()) {
                    Intent intent = extractInputToIntent("save");

                    setResult(RESULT_OK, intent);
                    finish();
                }
                handled = true;
            }
            return handled;
        });

        extractIntent();
    }

    private void initViewModel() {

        TransactionViewModel transactionViewModel = new ViewModelProvider(this).get(TransactionViewModel.class);
        TypeViewModel typeViewModel = new ViewModelProvider(this).get(TypeViewModel.class);

        Future<List<String>> suggestionsFutureList = transactionViewModel.getAllTransactionNameString();
        Future<List<String>> futureIncomeList = typeViewModel.getAllIncomeTypesString();
        Future<List<String>> futureExpenseList = typeViewModel.getAllExpenseTypesString();

        try {
            // get income first, since there is probably less income types
            List<String> tempIncomeTypes = futureIncomeList.get();
            List<String> tempExpenseTypes = futureExpenseList.get();
            List<String> suggestionsList = suggestionsFutureList.get();

            deepCopySuggestions(suggestionsList);

            deepCopyList(tempExpenseTypes, tempIncomeTypes);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    private void deepCopySuggestions(List<String> tempNameSuggestions) {

        ImmutableList<String> nameSuggestions = ImmutableList.copyOf(tempNameSuggestions);

        ArrayAdapter<String> nameSuggestionsAdapter =
                new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, nameSuggestions);
        editTextName.setAdapter(nameSuggestionsAdapter);
    }

    private void deepCopyList(List<String> tempExpenseTypes, List<String> tempIncomeTypes) {

        expenseTypeList = ImmutableList.copyOf(tempExpenseTypes);
        incomeTypeList = ImmutableList.copyOf(tempIncomeTypes);

        boolean isExpenseType = updateRadioButton();
        ArrayAdapter<String> adapterType = showSpinnerType(isExpenseType);
        extractIntentToSpinner(adapterType);
    }

    private boolean updateRadioButton() {

        Intent intent = getIntent();

        if (intent.getBooleanExtra(EXTRA_IS_EXPENSE_TYPE, true)) {
            radioButtonExpense.setChecked(true);
            return true;
        } else {
            radioButtonIncome.setChecked(true);
            return false;
        }
    }

    private ArrayAdapter<String> showSpinnerType(boolean isExpenseType) {

        ArrayAdapter<String> adapterType = initAdapterType(isExpenseType);
        spinnerType.setAdapter(adapterType);

        spinnerType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                type = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        return adapterType;
    }

    private void extractIntentToSpinner(ArrayAdapter<String> adapterType){

        Intent intent = getIntent();

        if (intent.hasExtra(EXTRA_ID)) {

            type = intent.getStringExtra(EXTRA_TYPENAME);

            if (adapterType == null) {
                initAdapterType(true);
            }

            int selectionPositionType = adapterType.getPosition(type);
            spinnerType.setSelection(selectionPositionType);
        }
    }

    private void extractIntent() {

        Intent intent = getIntent();
        if (intent.hasExtra(EXTRA_ID)) {
            editTextName.setText(intent.getStringExtra(EXTRA_NAME));

            epochSeconds = intent.getLongExtra(EXTRA_INSTANT, 0L);
            Instant instant = Instant.ofEpochSecond(epochSeconds);
            ZoneId zoneId = ZoneId.systemDefault();
            String formattedDate = DateFormatter.formatToDateString(instant, zoneId);
            editTextDate.setText(formattedDate);

            String value = getString(R.string.single_string_param, intent.getDoubleExtra(EXTRA_VALUE, 1) + "");
            editTextValue.setText(value);

            /*
             place cursor on the right side
              only for the first edit text
            */
            if (editTextValue.getText() != null && editTextValue.getText().length() > 0 ) {
                editTextValue.setSelection(editTextValue.getText().length());
            }

            String numOfRepeat = getString(R.string.single_string_param, intent.getIntExtra(EXTRA_REPEAT, 1) + "");
            editTextRepeat.setText(numOfRepeat);

            frequency = intent.getIntExtra(EXTRA_FREQUENCY, 12);
            String frequencyString = getFrequencyMap().inverse().get(frequency);

            // default to monthly
            if (frequencyString == null) {
                frequencyString = "Monthly";
            }

            int selectionPosition = adapterFrequency.getPosition(frequencyString);
            spinnerFrequency.setSelection(selectionPosition);
        } else {
            setTodayDate(editTextDate);
            radioButtonExpense.setChecked(true);
        }
    }

    // it takes a while to copy types, so the list might be null
    private ArrayAdapter<String> initAdapterType(boolean isExpenseType) {

        ArrayAdapter<String> adapterType;

        if (isExpenseType) {
            adapterType = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, expenseTypeList);
        } else {
            adapterType = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, incomeTypeList);
        }
        adapterType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        return adapterType;
    }

    private boolean violateInputValidation() {

        String name = editTextName.getText().toString().trim();

        Editable valueEditable = editTextValue.getText();
        String valueString = "";

        if (valueEditable != null) {
            valueString = valueEditable.toString().trim();
        }
        double value = 0;

        Editable repeatEditable = editTextRepeat.getText();
        String repeatString = "";

        if (repeatEditable != null) {
            repeatString = repeatEditable.toString().trim();
        }

        int repeat = -1;

        boolean violate = false;

        if (!valueString.isEmpty()) {
            value = Double.parseDouble(valueString);
        }

        if (!repeatString.isEmpty()) {
            repeat = Integer.parseInt(repeatString);
        }

        if (name.isEmpty()) {
            textInputLayoutName.setError("Please enter a name.");
            violate = true;
        } else {
            textInputLayoutName.setError("");
        }

        if (value == 0) {
            textInputLayoutValue.setError("Please enter an amount.");
            violate = true;
        } else if (value < 0) {
            textInputLayoutValue.setError("Please enter an amount greater than 0.");
            violate = true;
        } else {
            textInputLayoutValue.setError("");
        }

        if (repeat < 0 || repeat > 30) {
            textInputLayoutRepeat.setError("Please enter a value between 0 and 30.");
            violate = true;
        } else {
            textInputLayoutRepeat.setError("");
        }

        return violate;
    }

    private Intent extractInputToIntent(String operation) {

        String name = editTextName.getText().toString().trim();

        Editable valueEditable = editTextValue.getText();
        String valueString = "";

        if (valueEditable != null) {
            valueString = valueEditable.toString().trim();
        }
        double value = 0;

        Editable repeatEditable = editTextRepeat.getText();
        String repeatString = "";

        if (repeatEditable != null) {
            repeatString = repeatEditable.toString().trim();
        }
        int repeat = -1;

        if (!valueString.isEmpty()) {
            value = Double.parseDouble(valueString);
        }

        if (!repeatString.isEmpty()) {
            repeat = Integer.parseInt(repeatString);
        }

        /*
        bypass check for delete
        force value to be within acceptable range, since user is going to delete anyway
        */
        if (operation.equals("delete")) {
            if (repeat < 0 || repeat > 30) {
                repeat = 0;
            }

            if (value < 0) {
                value = 0;
            }
        }
        return createIntent(epochSeconds, value, name, type, frequency, repeat, operation);
    }

    private Intent createIntent(Long instantEpochSeconds, double value, String name, String typeName, int frequency, int repeat, String operation) {

        Intent intent = new Intent();
        intent.putExtra(EXTRA_NAME, name);
        intent.putExtra(EXTRA_TYPENAME, typeName);
        intent.putExtra(EXTRA_INSTANT, instantEpochSeconds);
        intent.putExtra(EXTRA_VALUE, value);
        intent.putExtra(EXTRA_FREQUENCY, frequency);
        intent.putExtra(EXTRA_REPEAT, repeat);

        boolean isExpenseType = radioButtonExpense.isChecked();

        intent.putExtra(EXTRA_IS_EXPENSE_TYPE, isExpenseType);

        intent.putExtra(EXTRA_OPERATION, operation);

        long id = getIntent().getLongExtra(EXTRA_ID, -1);
        if (id != -1) {
            intent.putExtra(EXTRA_ID, id);
        }

        String recurringId = getIntent().getStringExtra(EXTRA_RECURRING_ID);

        if (recurringId == null) {
            recurringId = "";
        }

        intent.putExtra(EXTRA_RECURRING_ID, recurringId);

        String zoneId = intent.getStringExtra(EXTRA_ZONE_ID);
        if (zoneId == null) {
            zoneId = ZoneId.systemDefault().getId();
        }

        intent.putExtra(EXTRA_ZONE_ID, zoneId);

        return intent;
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
                if (!violateInputValidation()) {
                    Intent intent = extractInputToIntent("save");

                    setResult(RESULT_OK, intent);
                    finish();
                }
                return true;
            case R.id.delete_item:
                Intent intent = extractInputToIntent("delete");

                setResult(RESULT_OK, intent);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onRadioButtonClicked(View view) {

        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch (view.getId()) {
            case R.id.radio_expense:
                if (checked) {
                    showSpinnerType(true);
                    break;
                }
            case R.id.radio_income:
                if (checked) {
                    showSpinnerType(false);
                    break;
                }
        }
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

        LocalDate localDate = LocalDate.of(year, month + 1, dayOfMonth);
        LocalTime localTime = LocalTime.now();
        ZoneId zoneId = ZoneId.systemDefault();

        ZonedDateTime zonedDateTime = ZonedDateTime.of(localDate, localTime, zoneId);
        Instant instant = zonedDateTime.toInstant();
        epochSeconds = instant.getEpochSecond();

        editTextDate.setText(DateFormatter.formatToDateString(instant, zoneId));
    }

    private void setTodayDate(TextInputEditText editTextDate) {

        Instant instant = Instant.now();
        epochSeconds = instant.getEpochSecond();
        ZoneId zoneId = ZoneId.systemDefault();

        String formattedDate = DateFormatter.formatToDateString(instant, zoneId);
        editTextDate.setText(formattedDate);
    }

    ImmutableBiMap<String, Integer> getFrequencyMap() {

        return ImmutableBiMap.of(
                getString(R.string.Annually), 1,
                getString(R.string.Biannually), 2,
                getString(R.string.Quarterly), 4,
                getString(R.string.Monthly), 12
        );
    }
}
