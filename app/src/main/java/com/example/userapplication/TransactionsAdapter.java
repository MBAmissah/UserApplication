package com.example.userapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class TransactionsAdapter extends RecyclerView.Adapter<TransactionsAdapter.TransactionVH> {

    List<Transactions> transactionsList;

    public TransactionsAdapter(List<Transactions> transactionsList) {
        this.transactionsList = transactionsList;
    }

    @NonNull
    @NotNull
    @Override
    public TransactionVH onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row, parent, false);
        return new TransactionVH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull TransactionVH holder, int position) {

        Transactions transactions = transactionsList.get(position);
        holder.date_timeTxt.setText(transactions.getDate_time());
        holder.statusTxt.setText(transactions.getStatus());
        holder.channelTxt.setText(transactions.getChannel());
        holder.amount_histTxt.setText(transactions.getAmount_hist());
        holder.referenceTxt.setText(transactions.getReference());

        boolean isExpandable = transactionsList.get(position).isExpandable();
        holder.expandableLayout.setVisibility(isExpandable ? View.VISIBLE : View.GONE);

    }

    @Override
    public int getItemCount() {
        return transactionsList.size();
    }

    public class TransactionVH extends RecyclerView.ViewHolder {


        TextView date_timeTxt, statusTxt, channelTxt, amount_histTxt, referenceTxt;
        LinearLayout linearLayout;
        RelativeLayout expandableLayout;


        public TransactionVH(@NonNull @NotNull View itemView) {
            super(itemView);

            date_timeTxt = itemView.findViewById(R.id.date_time);
            statusTxt = itemView.findViewById(R.id.status);
            channelTxt = itemView.findViewById(R.id.channel);
            amount_histTxt = itemView.findViewById(R.id.amount_hist);
            referenceTxt = itemView.findViewById(R.id.reference);

            linearLayout = itemView.findViewById(R.id.linear_layout);
            expandableLayout = itemView.findViewById(R.id.expandable_layout);



            linearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Transactions transactions = transactionsList.get(getAdapterPosition());
                    transactions.setExpandable(!transactions.isExpandable());
                    notifyItemChanged(getAdapterPosition());

                }
            });
        }
    }
}
