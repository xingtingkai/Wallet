package com.example.wallet;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import com.example.wallet.helper.DateFormatter;
import com.example.wallet.helper.CustomAlertDialog;
import com.example.wallet.helper.DatePickerFragment;
import com.example.wallet.helper.FrequencyStringConverter;

import java.util.Calendar;
import java.util.Date;

public class AddEditRepeatTransactionActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener{

    public static final String EXTRA_ID =
            "com.example.wallet.EXTRA_ID";
    public static final String EXTRA_NAME =
            "com.example.wallet.EXTRA_NAME";
    public static final String EXTRA_TYPENAME =
            "com.example.wallet.EXTRA_TYPENAME";
    public static final String EXTRA_VALUE =
            "com.example.wallet.EXTRA_VALUE";
    public static final String EXTRA_DATE =
            "com.example.wallet.EXTRA_DATE";
    public static final String EXTRA_FREQUENCY =
            "com.example.wallet.EXTRA_FREQUENCY";
    public static final String EXTRA_REPEAT =
            "com.example.wallet.EXTRA_REPEAT";
    public static final String EXTRA_RECURRING_ID =
            "com.example.wallet.EXTRA_RECURRING_ID";
    public static final String EXTRA_OPERATION =
            "com.example.wallet.EXTRA_OPERATION";

    private EditText editTextName;
    private EditText editTextTypeName;
    private EditText editTextValue;
    private EditText editTextDate;
    private EditText editTextRepeat;
    private Spinner spinner;
    private int frequency;

    private ArrayAdapter<CharSequence> spinnerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_repeat_transaction);

        editTextName = findViewById(R.id.edit_text_name);
        editTextTypeName = findViewById(R.id.edit_text_type_name);
        editTextValue = findViewById(R.id.edit_text_value);
        editTextDate = findViewById(R.id.edit_text_dateTime);
        spinner = findViewById(R.id.spinner);
        editTextRepeat = findViewById(R.id.edit_text_num_repeat);

        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close);

        editTextDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Calendar calendar = convertEditTextToCalendar(editTextDate);
                DialogFragment datePicker = new DatePickerFragment(calendar);
                datePicker.show(getSupportFragmentManager(), "date picker");
            }
        });

        spinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.frequency_array, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String frequencyString = parent.getItemAtPosition(position).toString();
                frequency = FrequencyStringConverter.convertFrequencyStringToInt(frequencyString);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        Intent intent = getIntent();
        if (intent.hasExtra(EXTRA_ID)) {
            editTextName.setText(intent.getStringExtra(EXTRA_NAME));
            editTextTypeName.setText(intent.getStringExtra(EXTRA_TYPENAME));
            editTextDate.setText(intent.getStringExtra(EXTRA_DATE));
            editTextValue.setText(intent.getDoubleExtra(EXTRA_VALUE, 1) + "");
            editTextRepeat.setText(intent.getIntExtra(EXTRA_REPEAT, 1) + "");

            frequency = intent.getIntExtra(EXTRA_FREQUENCY, 12);
            String frequencyString = FrequencyStringConverter.convertFrequencyIntToString(frequency);
            int selectionPosition= spinnerAdapter.getPosition(frequencyString);
            spinner.setSelection(selectionPosition);
        } else {
            setTodayDate(editTextDate);
        }
    }

    private Calendar convertEditTextToCalendar(EditText editTextDate) {

        Calendar calendar = Calendar.getInstance();
        String dateString = editTextDate.getText().toString();
        Date date = DateFormatter.formatStringToDate(dateString);
        calendar.setTime(date);

        return calendar;
    }

    private void createOrSaveTransaction() {

        String name = editTextName.getText().toString().trim();
        String typeName = editTextTypeName.getText().toString().trim();
        String dateString = editTextDate.getText().toString();
        String valueString = editTextValue.getText().toString();
        double value = 0;

        String repeatString = editTextRepeat.getText().toString();

        int repeat = -1;

        if (!valueString.isEmpty()) {
            value = Double.parseDouble(valueString);
        }

        if (!repeatString.isEmpty()) {
            repeat = Integer.parseInt(repeatString);
        }

        if (name.isEmpty() || typeName.isEmpty() || value == 0 || frequency == 0  || repeat == -1 ) {
            Toast.makeText(this, "Please insert a name or typeName or value or frequency or repeat", Toast.LENGTH_SHORT).show();
            return;
        }

        if (repeat < 0 || repeat > 30) {
            CustomAlertDialog customAlertDialog = new CustomAlertDialog("Repeat must be within 0 and 30");
            customAlertDialog.show(getSupportFragmentManager(), "alert dialog");
        } else {
            Intent newTransaction = createIntent(dateString, value, name, typeName, frequency, repeat, "save");

            setResult(RESULT_OK, newTransaction);
            finish();
        }
    }

    private void deleteTransaction(){

        String name = editTextName.getText().toString().trim();
        String typeName = editTextTypeName.getText().toString().trim();
        String dateString = editTextDate.getText().toString();
        String valueString = editTextValue.getText().toString();
        double value = 0;

        String repeatString = editTextRepeat.getText().toString();

        int repeat = 0;

        if (!valueString.isEmpty()) {
            value = Double.parseDouble(valueString);
        }

        if (!repeatString.isEmpty()) {
            repeat = Integer.parseInt(repeatString);
        }

        if (repeat < 0 || repeat > 30) {
            CustomAlertDialog customAlertDialog = new CustomAlertDialog("Repeat must be within 0 and 30");
            customAlertDialog.show(getSupportFragmentManager(), "alert dialog");
        } else {
            Intent oldTransaction = createIntent(dateString, value, name, typeName, frequency, repeat, "delete");

            setResult(RESULT_OK, oldTransaction);
            finish();
        }
    }

    private Intent createIntent(String dateString, double value, String name, String typeName, int frequency, int repeat, String operation) {

        Intent transaction = new Intent();
        transaction.putExtra(EXTRA_NAME, name);
        transaction.putExtra(EXTRA_TYPENAME, typeName);
        transaction.putExtra(EXTRA_DATE, dateString);
        transaction.putExtra(EXTRA_VALUE, value);
        transaction.putExtra(EXTRA_FREQUENCY, frequency);
        transaction.putExtra(EXTRA_REPEAT, repeat);
        transaction.putExtra(EXTRA_OPERATION, operation);

        Long id = getIntent().getLongExtra(EXTRA_ID, -1);
        if (id != -1) {
            transaction.putExtra(EXTRA_ID, id);
        }

        String recurringId = getIntent().getStringExtra(EXTRA_RECURRING_ID);
        transaction.putExtra(EXTRA_RECURRING_ID, recurringId);

        return transaction;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.add_transaction_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save_transaction:
                createOrSaveTransaction();
                return true;
            case R.id.delete_transaction:
                deleteTransaction();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

        String dateString = DateFormatter.formatDateToString(calendar.getTime());
        editTextDate.setText(dateString);
    }

    private void setTodayDate(EditText editTextDate) {

        Calendar calendar = Calendar.getInstance();
        String dateString = DateFormatter.formatDateToString(calendar.getTime());
        editTextDate.setText(dateString);
    }
}
