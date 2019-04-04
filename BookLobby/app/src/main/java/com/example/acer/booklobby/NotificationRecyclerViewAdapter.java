package com.example.acer.booklobby;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class NotificationRecyclerViewAdapter extends RecyclerView.Adapter<NotificationRecyclerViewAdapter.NotificationViewHolder> {

    private Context context;
    private ArrayList<Transaction> transactions;

    public NotificationRecyclerViewAdapter(Context context, ArrayList<Transaction> transactions) {
        this.context = context;
        this.transactions = transactions;
        Log.d("CHECK", "CHECK+++++++++++IN NOTIFICATION RECYCLER++++" + transactions.size());
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        Log.d("CHECK", "CHECK+++++++++++IN NOTIFICATION RECYCLER ON CREATE VIEW++++" + transactions.size());
        View view = LayoutInflater.from(context).inflate(R.layout.notification_recycler_view_adapter, viewGroup, false);
        NotificationViewHolder notificationViewHolder = new NotificationViewHolder(view, context, transactions);

        return notificationViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final NotificationViewHolder notificationViewHolder, int i) {

        Log.d("CHECK", "CHECK+++++++++++IN BIND VIEW OF NOTIFICATION RECYCLER" + i + "------------");
        final int pos = i;
        final Transaction transaction = transactions.get(i);
        notificationViewHolder.bookName.setText("   " + transaction.getBookName());
        notificationViewHolder.borrowerEmail.setText("Borrower's Email : " + transaction.getBorrower());
        notificationViewHolder.borrowingDate.setText("Borrowing Date : " + transaction.getBorrowDate());
        notificationViewHolder.venue.setText("Venue : " + transaction.getSelectedLocation());
    }

    @Override
    public int getItemCount() {

        Log.d("CHECK", "CHECK++++++++++++++++++" + transactions.size());
        return transactions.size();
    }

    public class NotificationViewHolder extends RecyclerView.ViewHolder {

        final Context context;
        TextView bookName, borrowerEmail, borrowingDate, venue, returnBack;
        Button accept, decline, tick;
        EditText otp;

        ArrayList<Transaction> transactions;

        public NotificationViewHolder(@NonNull View itemView, final Context context, final ArrayList<Transaction> transactions) {
            super(itemView);
            Log.d("CHECK", "CHECK+++++++++++IN NOTIFICATION VIEW HOLDER++++");
            bookName = itemView.findViewById(R.id.bookName);
            borrowerEmail = itemView.findViewById(R.id.borrowerEmail);
            borrowingDate = itemView.findViewById(R.id.borrowingDate);
            venue = itemView.findViewById(R.id.venue);
            accept = itemView.findViewById(R.id.accept);
            decline = itemView.findViewById(R.id.decline);
            otp = itemView.findViewById(R.id.otp);
            tick = itemView.findViewById(R.id.tick);
            returnBack = itemView.findViewById(R.id.retBack);

            this.context = context;
            this.transactions = transactions;
            final Transaction transaction = transactions.get(getAdapterPosition() + 1);
            accept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    otp.setVisibility(View.VISIBLE);
                    tick.setVisibility(View.VISIBLE);
                    tick.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            final String otpEdit = otp.getText().toString();
                            if (otpEdit.equals(transaction.getOtp())) {
                                //update accepted and due date

                                DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
                                Date date = new Date();
                                Calendar c = Calendar.getInstance();
                                c.setTime(date);
                                c.add(Calendar.DATE, transaction.getRentDays());
                                Date date1 = c.getTime();

                                FirebaseFirestore db = FirebaseFirestore.getInstance();
                                db.collection("transactions").document(transaction.getTransactionId()).update("status", "Accepted");
                                db.collection("transactions").document(transaction.getTransactionId()).update("dueDate", dateFormat.format(date1));

                                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                builder.setTitle("Book Lent !")
                                        .setMessage("Borrower Verified. You can lend your book now.")
                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
//                                                transactions.remove(getAdapterPosition());
//                                                notifyDataSetChanged();
                                                accept.setVisibility(View.GONE);
                                                decline.setVisibility(View.GONE);
                                                otp.setVisibility(View.GONE);
                                                tick.setVisibility(View.GONE);
                                                returnBack.setVisibility(View.VISIBLE);
                                            }
                                        });
                                AlertDialog alertDialog = builder.create();
                                alertDialog.show();

                            } else {
                                Toast.makeText(context, "INCORRECT OTP !", Toast.LENGTH_LONG).show();
                            }

                        }
                    });
                }
            });

            decline.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Confirm ?")
                            .setMessage("Are you sure you want to decline this borrow request ?")
                            .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.cancel();
                                }
                            })
                            .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                                    db.collection("books").document(transaction.getBookId()).update("borrowerId", "");
                                    db.collection("transactions").document(transaction.getTransactionId())
                                            .update("status", "Declined").addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            transactions.remove(getAdapterPosition());
                                            notifyDataSetChanged();
                                            Toast.makeText(context, "Book request declined successfully !", Toast.LENGTH_LONG).show();
                                        }
                                    });
                                }
                            });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }
            });

            returnBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Confirm ?")
                            .setMessage("Are you sure your book is returned ?")
                            .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.cancel();
                                }
                            })
                            .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                                    db.collection("books").document(transaction.getBookId()).update("borrowerId", "");
                                    db.collection("transactions").document(transaction.getTransactionId()).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            transactions.remove(getAdapterPosition());
                                            notifyDataSetChanged();
                                            Toast.makeText(context, "You are doing a great job !!! :)", Toast.LENGTH_LONG).show();
                                        }
                                    });
                                }
                            });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }
            });
        }
    }
}
