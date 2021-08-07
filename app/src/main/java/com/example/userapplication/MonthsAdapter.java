package com.example.userapplication;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

public class MonthsAdapter extends RecyclerView.Adapter<MonthsAdapter.MonthVH> {

    List<Months> monthsList;
    final List<Transactions> transactionsListUntouchable;
    List<Transactions> transactionsList;
    Context cxt;
    String month;

    public MonthsAdapter(List<Months> monthsList, List<Transactions> transactionsList, Context mContext) {
        this.monthsList = monthsList;
        this.transactionsList = new ArrayList<>(transactionsList);
        this.transactionsListUntouchable = new ArrayList<>(transactionsList);
        this.cxt = mContext;

    }

    @NonNull
    @NotNull
    @Override
    public MonthVH onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.month_year, parent, false);
        return new MonthVH(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull @NotNull MonthVH holder, int position) {


        Months months = monthsList.get(position);

        if(!months.getDate().equals("")) {
            //if months dont match remove
            Predicate<Transactions> condition;
            switch (months.getDate().substring(0, 2)) {
                case "01":
                    condition = transactions -> transactions.getDate_time().substring(16, 18).equals("01");
                    transactionsList.removeIf(condition.negate());
                    month = "January";
                    break;
                case "02":
                    condition = transactions -> transactions.getDate_time().substring(16, 18).equals("02");
                    transactionsList.removeIf(condition.negate());
                    month = "February";
                    break;
                case "03":
                    condition = transactions -> transactions.getDate_time().substring(16, 18).equals("03");
                    transactionsList.removeIf(condition.negate());
                    month = "March";
                    break;
                case "04":
                    condition = transactions -> transactions.getDate_time().substring(16, 18).equals("04");
                    transactionsList.removeIf(condition.negate());
                    month = "April";
                    break;
                case "05":
                    condition = transactions -> transactions.getDate_time().substring(16, 18).equals("05");
                    transactionsList.removeIf(condition.negate());
                    month = "May";
                    break;
                case "06":
                    condition = transactions -> transactions.getDate_time().substring(16, 18).equals("06");
                    transactionsList.removeIf(condition.negate());
                    month = "June";
                    break;
                case "07":
                    condition = transactions -> transactions.getDate_time().substring(16, 18).equals("07");
                    transactionsList.removeIf(condition.negate());
                    month = "July";
                    break;
                case "08":
                    condition = transactions -> transactions.getDate_time().substring(16, 18).equals("08");
                    transactionsList.removeIf(condition.negate());
                    month = "August";
                    break;
                case "09":
                    condition = transactions -> transactions.getDate_time().substring(16, 18).equals("09");
                    transactionsList.removeIf(condition.negate());
                    month = "September";
                    break;
                case "10":
                    condition = transactions -> transactions.getDate_time().substring(16, 18).equals("10");
                    transactionsList.removeIf(condition.negate());
                    month = "October";
                    break;
                case "11":
                    condition = transactions -> transactions.getDate_time().substring(16, 18).equals("11");
                    transactionsList.removeIf(condition.negate());
                    month = "November";
                    break;
                case "12":
                    condition = transactions -> transactions.getDate_time().substring(16, 18).equals("12");
                    transactionsList.removeIf(condition.negate());
                    month = "December";
                    break;
                default:
                    System.out.println("Eroooorrrrrrrrrrrrrrr");
                    break;
            }

            // if year doesnt match remove
            Predicate<Transactions> conditionYear = condition = transactions -> transactions.getDate_time().substring(11, 15).equals(months.getDate().substring(3));
            transactionsList.removeIf(conditionYear.negate());


            holder.date_timeTxt.setText(month + " " + months.getDate().substring(3));

            TransactionsAdapter childRecyclerViewAdapter = new TransactionsAdapter(transactionsList);
            holder.childRecyclerView.setAdapter(childRecyclerViewAdapter);
        }
        else holder.date_timeTxt.setText("No Transactions Made");



        resetTransactionList();





    }

    @Override
    public int getItemCount() {
        return monthsList.size();
    }

    public class MonthVH extends RecyclerView.ViewHolder {


        TextView date_timeTxt;
        LinearLayout linearLayout;
        public RecyclerView childRecyclerView;


        public MonthVH(@NonNull @NotNull View itemView) {
            super(itemView);

            date_timeTxt = itemView.findViewById(R.id.month_year);

            linearLayout = itemView.findViewById(R.id.linear_layout_month);
            childRecyclerView = itemView.findViewById(R.id.recyclerView);



            linearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Months months = monthsList.get(getAdapterPosition());
                   notifyItemChanged(getAdapterPosition());

                }
            });
        }
    }

    public void resetTransactionList(){
       transactionsList = new ArrayList<>(transactionsListUntouchable);

    }
}
