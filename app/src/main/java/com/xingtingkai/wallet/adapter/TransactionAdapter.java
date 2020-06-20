package com.xingtingkai.wallet.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.xingtingkai.wallet.R;
import com.xingtingkai.wallet.db.entity.Transaction;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

public class TransactionAdapter extends ListAdapter<Transaction, TransactionAdapter.TransactionViewHolder> {

    class TransactionViewHolder extends RecyclerView.ViewHolder {
        private final TextView textViewName;
        private final TextView textViewValue;
        private final TextView textView_date;
        private final TextView textView_dayOfdate;
        private final TextView textView_totalAmount;

        private TransactionViewHolder(View itemView) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.textView_name);
            textViewValue = itemView.findViewById(R.id.textView_value);
            textView_date = itemView.findViewById(R.id.textView_date);
            textView_dayOfdate = itemView.findViewById(R.id.textView_day_of_date);
            textView_totalAmount = itemView.findViewById(R.id.textView_totalAmount);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (listener != null && position != RecyclerView.NO_POSITION) {
                        listener.onItemClick(getItem(position));
                    }
                }
            });
        }

        public void showHeader(String date, String day, String totalAmount) {

            if (textView_date != null) {
                textView_date.setText(date);
            }

            if (textView_totalAmount != null) {
                textView_totalAmount.setText(totalAmount);
            }

            if (textView_dayOfdate != null) {
                textView_dayOfdate.setText(day);
            }

            textView_date.setVisibility(View.VISIBLE);
            textView_totalAmount.setVisibility(View.VISIBLE);
            textView_dayOfdate.setVisibility(View.VISIBLE);
        }

        /*
        don't remove view otherwise it will cause problems causing it to be null
        changing view to INVISIBLE still takes up space, so GONE is the solution
         */
        public void removeHeader() {

            textView_date.setVisibility(View.GONE);

            textView_totalAmount.setVisibility(View.GONE);

            textView_dayOfdate.setVisibility(View.GONE);
        }
    }

    private final LayoutInflater mInflater;
    private OnItemClickListener listener;

    private List<Transaction> transactions;

    private boolean isFirst;

    public TransactionAdapter(Context context) {
        super(DIFF_CALLBACK);
        mInflater = LayoutInflater.from(context);
    }

    private static final DiffUtil.ItemCallback<Transaction> DIFF_CALLBACK = new DiffUtil.ItemCallback<Transaction>() {
        @Override
        public boolean areItemsTheSame(Transaction oldItem, Transaction newItem) {
            return oldItem.getTransactionId() == newItem.getTransactionId();
        }

        @Override
        public boolean areContentsTheSame(Transaction oldItem, Transaction newItem) {
            return oldItem.getName().equals(newItem.getName()) &&
                    oldItem.getValue() == newItem.getValue() &&
                    oldItem.getTypeName().equals(newItem.getTypeName()) &&
                    oldItem.getDate().equals(newItem.getDate()) &&
                    oldItem.isRepeat() == newItem.isRepeat() &&
                    oldItem.isExpenseTransaction() == newItem.isExpenseTransaction();
        }
    };

    @Override
    public TransactionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.recyclerview_transaction, parent, false);
        TransactionViewHolder holder = new TransactionViewHolder(itemView);
        return holder;
    }

    @Override
    public void onBindViewHolder(TransactionViewHolder holder, int position) {
        Transaction transaction = getItem(position);
        holder.textViewName.setText(transaction.getName());
        holder.textViewValue.setText((int) transaction.getValue() + "");

//        if (!transaction.isExpenseTransaction()) {
//            holder.textViewName.setTextColor(Color.parseColor("#00ff9b"));
//            holder.textViewValue.setTextColor(Color.parseColor("#00ff9b"));
//        }

        if (!transaction.isExpenseTransaction()) {
            holder.textViewName.setTypeface(holder.textViewName.getTypeface(), Typeface.BOLD);
            holder.textViewValue.setTypeface(holder.textViewValue.getTypeface(), Typeface.BOLD);
        } else {
            holder.textViewName.setTypeface(holder.textViewName.getTypeface(), Typeface.NORMAL);
            holder.textViewValue.setTypeface(holder.textViewValue.getTypeface(), Typeface.NORMAL);
        }

        updateHeader(transaction, holder);
    }

    public interface OnItemClickListener {
        void onItemClick(Transaction transaction);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void passTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    private void updateHeader(Transaction transaction, TransactionViewHolder holder) {

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(transaction.getDate());

        SimpleDateFormat formatterDate = new SimpleDateFormat("dd");
        String formattedDate = formatterDate.format(calendar.getTime());

        SimpleDateFormat formatterDay = new SimpleDateFormat("EEE");
        String formattedDay = formatterDay.format(calendar.getTime());

        this.isFirst = false;

        if (transactions != null || !transactions.isEmpty()) {

            String totalAmount = calculateTotalAmountInADay(transaction);

            if (this.isFirst) {
                holder.showHeader(formattedDate, formattedDay, totalAmount);
            }
        }

        if (!this.isFirst) {
            holder.removeHeader();
        }
    }

    private String calculateTotalAmountInADay(Transaction transaction) {

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(transaction.getDate());

        SimpleDateFormat formatter = new SimpleDateFormat("ddMMyyyy");
        String formattedDate = formatter.format(calendar.getTime());

        double totalValue = 0;

        int count = 0;

        StringBuilder stringBuilder = new StringBuilder();

        BigDecimal totalValue1 = new BigDecimal(0);

        for (int i = 0; i < transactions.size(); i++) {
            // only count expenses, don't minus income from expenses
            if (transactions.get(i).isExpenseTransaction()) {
                if (formatter.format(transactions.get(i).getDate()).equals(formattedDate)) {

                    totalValue += transactions.get(i).getValue();

                    if (count == 0 && transactions.get(i).getTransactionId() == transaction.getTransactionId()) {
                        this.isFirst = true;
                    }
                    count++;

                    if (i + 1 == transactions.size() || (i + 1 < transactions.size() && !formatter.format(transactions.get(i + 1).getDate()).equals(formattedDate))) {
                        break;
                    }
                }
            }
        }

        // round up to 2.d.p
        BigDecimal totalValueBd = new BigDecimal(totalValue).setScale(2, RoundingMode.HALF_UP);

        stringBuilder.append(totalValueBd.toBigInteger());

        return stringBuilder.toString();
    }
}