package com.example.acer.booklobby;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    //private ProgressBar progressBar;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private HomeRecyclerViewAdapter homeRecyclerViewAdapter;
    private static ArrayList<Books> books;
    private static ArrayList<Books> temp;
    //private Books book;

    public HomeFragment() {
        // Required empty public constructor
        books= new ArrayList<>();
        temp=new ArrayList<>();
        //private Books book;

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Bundle bundle=getArguments();
        final String queryText;
        if(bundle!=null)
        { Log.e("query","here");
            queryText=bundle.getString("queryText");
            Log.e("query",queryText);
        }
        else
            queryText="";
        Log.d("CHECK", "CHECK+=================in home fragment");
        final View view = inflater.inflate(R.layout.fragment_home, container, false);
        final String userId= FirebaseAuth.getInstance().getCurrentUser().getEmail();
        LinearLayout noBooks=(LinearLayout)view.findViewById(R.id.noBooks);
        noBooks.setVisibility(View.GONE);
        db.collection("books")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            books.clear();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                if(!userId.equals(document.getString("ownerId")) && (document.getString("borrowerId")==null || document.getString("borrowerId").equals(""))) {
                                    Books book = new Books();
                                    book.setBookId(document.getId());
                                    book.setAuthorName(document.getString("author"));
                                    Log.e("ERROR", "ERROR============" + document.getString("name"));
                                    book.setBookName(document.getString("name"));
                                    Log.e("ERROR", "ERROR============" + document.getString("ownerId"));
                                    book.setOwnerName(document.getString("ownerId"));
                                    book.setCategory(document.getString("category"));
                                    book.setRentDays(document.getLong("rentDays").intValue());
                                    book.setRentRate(document.getDouble("rentRate"));
                                    book.setFree(document.getBoolean("isFree"));
                                    book.setImageURL(document.getString("imagePath"));
                                    book.setSearchKeywords((ArrayList)document.get("searchKeywords"));
                                    books.add(book);
                                }
                                Log.d("CHECK", "inside complete+=========size========"+books.size());
                                //Log.d("LOG", document.getId() + " => " + document.getData());
                            }
                            if(!queryText.equals(""))
                            {

                                ArrayList<String> queryWords=new ArrayList<String>(Arrays.asList(queryText.split("\\s+")));
                               for(Books b:books)
                               {
                                   for(String t:b.getSearchKeywords())
                                   {
                                       int c=0;
                                       Log.e("searchkeywords",t);
                                       for(String r:queryWords) {
                                           Log.e("queryword",r);
                                           String p=t.toLowerCase();
                                           String q=r.toLowerCase();
                                           if (p.contains(q) || q.contains(p)) {
                                               Log.e("books","here");
                                               temp.add(b);

                                               c = 1;
                                               break;
                                           }
                                           if (c == 1)
                                               break;
                                       }
                                   }
                               }
                            }
                            else
                            {
                                temp=books;
                            }
                            if(temp!=null && temp.size()>0) {
                                //progressBar = view.findViewById(R.id.progress);
                                recyclerView = view.findViewById(R.id.allBooks);
                                layoutManager = new GridLayoutManager(getContext(), 2);
                                //
                                recyclerView.setLayoutManager(layoutManager);

                                homeRecyclerViewAdapter = new HomeRecyclerViewAdapter(temp, getActivity());
                                recyclerView.setHasFixedSize(true);
                                recyclerView.setAdapter(homeRecyclerViewAdapter);
                                //progressBar.setVisibility(View.INVISIBLE);
                            }
                            else
                            {

                                LinearLayout noBooks=(LinearLayout)view.findViewById(R.id.noBooks);
                                noBooks.setVisibility(View.VISIBLE);
                            }
                        } else {
                            Log.d("ERROR", "Error getting documents: ", task.getException());
                        }
                    }
                });

        Log.d("CHECK", "outside complete+=========size========"+books.size());


        Log.d("CHECK", "CHECK+=========size========"+books.size());


        return view;
    }

}
