package com.example.acer.booklobby;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ManageMyBooksRecyclerViewAdapter extends RecyclerView.Adapter<ManageMyBooksRecyclerViewAdapter.BookViewHolder> {

    private ArrayList<Books> books;
    private Context context;
    public ManageMyBooksRecyclerViewAdapter(ArrayList<Books> books,Context context){
        this.books = books;
        this.context=context;
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.manage_books_recyclerview_adapter, viewGroup, false);
        BookViewHolder bookViewHolder = new BookViewHolder(view,context,books);

        return bookViewHolder;
    }

    @Override
    public void onBindViewHolder(BookViewHolder viewHolder, int i) {

        Books book = books.get(i);
        Picasso.get()
                .load(book.getImageURL())
                .fit()
                .placeholder(R.drawable.book)
                .centerCrop()
                .into(viewHolder.bookImage);
        //bookViewHolder.bookImage.setImageResource(R.drawable.book);
        Log. d("CHECK","Check+====================================="+book.getBookName());
        viewHolder.bookName.setText(book.getBookName());
    }

    @Override
    public int getItemCount() {
        return books.size();
    }

    public  class BookViewHolder extends RecyclerView.ViewHolder {

        ImageView bookImage;
        TextView bookName;
        Button delete;
        ArrayList<Books> books;

        public BookViewHolder(@NonNull View itemView, final Context context, final ArrayList<Books> books) {
            super(itemView);
            bookImage = itemView.findViewById(R.id.bookImage);
            bookName = itemView.findViewById(R.id.bookName);
            delete = itemView.findViewById(R.id.deleteBook);
            this.books=books;
            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                    public void onClick(View view) {
                        Log.d("CHECK", "CHECK++++++++++++Onclick");
                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        Books book = books.get(getAdapterPosition());
                        DocumentReference bookIdRef = db.collection("books").document(book.getBookId());
                        bookIdRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                            }
                        });
                        books.remove(getAdapterPosition());
                        notifyDataSetChanged();


                    }
            });

        }


    }
}
