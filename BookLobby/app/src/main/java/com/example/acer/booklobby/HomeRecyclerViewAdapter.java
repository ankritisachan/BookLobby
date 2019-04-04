package com.example.acer.booklobby;

import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.Serializable;
import java.util.ArrayList;

public class HomeRecyclerViewAdapter extends RecyclerView.Adapter<HomeRecyclerViewAdapter.BookViewHolder> {

    private ArrayList<Books> books;
    private Context context;

    public HomeRecyclerViewAdapter(ArrayList<Books> books, Context context){
        this.books = books;
        this.context=context;
        Log.d("CHECK", "CHECK+================="+this.books.size());
        Log.d("CHECK", "CHECK+=================in home recycler viewadapter constructor");

    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        Log.d("CHECK","oncreate");
        View view = LayoutInflater.from(context).inflate(R.layout.home_recycler_view_adapter, viewGroup, false);
        BookViewHolder bookViewHolder = new BookViewHolder(view, context, books);


        return bookViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull BookViewHolder bookViewHolder, int i) {
        Log.d("CHECK", "CHECK+=================in home recycler viewadapter bind view holder");
        Books book = books.get(i);
        Picasso.get()
                .load(book.getImageURL())
                .fit()
                .placeholder(R.drawable.book)
                .centerCrop()
                .into(bookViewHolder.bookImage);
        //bookViewHolder.bookImage.setImageResource(R.drawable.book);
        Log. d("CHECK","Check+====================================="+book.getBookName());
        bookViewHolder.bookName.setText(book.getBookName());
    }

    @Override
    public int getItemCount() {
        Log.d("CHECK", "CHECK+=================in home recycler viewadapter getcount"+books.size());
        return books.size();
    }

    public static class BookViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView bookImage;
        TextView bookName;
        Context context;
        ArrayList<Books> books;

        public BookViewHolder(@NonNull View itemView, Context context, ArrayList<Books> books) {
            super(itemView);
            bookImage = itemView.findViewById(R.id.bookImage);
            bookName = itemView.findViewById(R.id.bookName);
            itemView.setOnClickListener(this);
            this.context = context;
            this.books = books;

        }

        @Override
        public void onClick(View view) {
            Log.d("CHECK", "CHECK++++++++++++Onclick");
            Books book = books.get(getAdapterPosition());
            Intent intent = new Intent(context, BookDetailsActivity.class);
            Log.e("context", context.toString());
            String callingActivity="";
            if(context.toString().contains("Navigation"))
                callingActivity="navigation";
            else if(context.toString().contains("Lent"))
                callingActivity="lent";
            else if(context.toString().contains("Borrowed"))
                callingActivity="borrowed";
                Log.e("callingActivity","borrowed");
            intent.putExtra("BookObject", (Serializable) book);
            intent.putExtra("callingActivity",callingActivity);
            context.startActivity(intent);
        }
    }
}
