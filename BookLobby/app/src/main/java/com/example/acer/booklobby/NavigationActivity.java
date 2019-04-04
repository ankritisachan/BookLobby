package com.example.acer.booklobby;

import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class NavigationActivity extends AppCompatActivity implements EnableInternetReceiver.InternetStateReceiver {

    private DrawerLayout dl;
    private ActionBarDrawerToggle abdt;
    private static FirebaseFirestore db;
    private static String userID;
    private EnableInternetReceiver enableInternetReceiver;
    private static int borrowRequests=0;
    private static CircleImageView user_photo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);
        dl = (DrawerLayout) findViewById(R.id.dl);
        abdt = new ActionBarDrawerToggle(this, dl, R.string.Close, R.string.Open);
        abdt.setDrawerIndicatorEnabled(true);
        dl.addDrawerListener(abdt);
        abdt.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        enableInternetReceiver = new EnableInternetReceiver();
        enableInternetReceiver.addListener(this);
        this.registerReceiver(enableInternetReceiver, new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));
        // getActionBar().setDisplayHomeAsUpEnabled(true);
        //getActionBar().setHomeButtonEnabled(true);
        userID = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        db = FirebaseFirestore.getInstance();

        final FragmentManager fragmentManager = getSupportFragmentManager();
        f_search search = new f_search();
        fragmentManager.beginTransaction().replace(R.id.fragmentSearch, search).commit();

        HomeFragment home = new HomeFragment();
        fragmentManager.beginTransaction().replace(R.id.fragmentMain, home).commit();

        getRequestData();
        final NavigationView nav_view = (NavigationView) findViewById(R.id.nav_view);
        getUserData();


        nav_view.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                int id = menuItem.getItemId();

                switch (id) {
                    case (R.id.profile):
                        //Toast.makeText(NavigationActivity.this, "Profile is under construction", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(NavigationActivity.this, ViewProfileActivity.class));

                        dl.closeDrawers();
                        break;
                    case (R.id.manage):
                        startActivity(new Intent(NavigationActivity.this, ManageMyBooksActivity.class));

                        dl.closeDrawers();
                        break;
                    case (R.id.lent_book):
                        startActivity(new Intent(NavigationActivity.this, LentBooksActivity.class));

                        dl.closeDrawers();
                        break;
                    case (R.id.borrowed_book):
                        startActivity(new Intent(NavigationActivity.this, BorrowedBooksActivity.class));

                        dl.closeDrawers();
                        break;
                    case (R.id.notification):
                        startActivity(new Intent(NavigationActivity.this, NotificationActivity.class));

                        dl.closeDrawers();
                        break;
                    case (R.id.change_password):
                        startActivity(new Intent(NavigationActivity.this, ChangePasswordActivity.class));

                        dl.closeDrawers();
                        break;

                    case (R.id.logout):
                        FirebaseAuth.getInstance().signOut();
                        SaveSession.clearEmail(getApplicationContext());
                        startActivity(new Intent(NavigationActivity.this, LoginActivity.class));
                        finish();
                        break;

                }


                return true;
            }
        });

    }

  /*  protected void onPostCreate(Bundle savedInstanceState) {

        super.onPostCreate(savedInstanceState);
        abdt.syncState();
    }

    public void onConfigurationChanged(Configuration nc)
    {
        super.onConfigurationChanged(nc);
        abdt.onConfigurationChanged(nc);
    }
*/

   /* private void searchViewCode()
    {
        searchView = (MaterialSearchView) findViewById(R.id.search_view);
        searchView.setSuggestions(getResources().getStringArray(R.array.query_suggestions));
        searchView.setEllipsize(true);
        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Toast.makeText(getApplicationContext(),query,Toast.LENGTH_LONG).show();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        searchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {

            }

            @Override
            public void onSearchViewClosed() {

            }
        });
    }*/

    public boolean onOptionsItemSelected(MenuItem item) {
        return abdt.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    public void getUserData() {


        db.collection("users")
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Log.d("db error", document.getId() + " => " + document.getData());
                        if (document.getData().get("email").equals(userID)) {
                            //Toast.makeText(NavigationActivity.this, document.getData().get("name").toString(), Toast.LENGTH_SHORT).show();
                            //NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
                            //Menu menu = navigationView.getMenu();
                            //MenuItem name = menu.findItem(R.id.name);
                            // name.setTitle(document.getData().get("name").toString());
                            user_photo=(CircleImageView)findViewById(R.id.user_photo);
                            TextView txt_name = (TextView) findViewById(R.id.name);
                            txt_name.setText("Hi " + document.getData().get("name").toString() + "!");
                            if(document.getData().get("imagePath")!=null)
                            {
                                Picasso.get()
                                        .load(document.getData().get("imagePath").toString())
                                        .fit()
                                        .placeholder(R.drawable.profile_picture)
                                        .centerCrop()
                                        .into(user_photo);
                            }
                        }
                    }
                }
            }

        });
 /*   public boolean onOptionsItemSelected(MenuItem item)
    {
        switch(item.getItemId())
        {
            case R.id.action_search:
                return true;
                default:
                    return super.onOptionsItemSelected(item);
        }
    }*/
    }
    private  void getRequestData(){
      borrowRequests=0;
        db.collection("transactions")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        for(QueryDocumentSnapshot document : task.getResult()) {

                            if(userID.equals(document.getString("owner")) && (document.getString("status").equalsIgnoreCase("Unverified"))){
                                Log.e("here","here");
                                borrowRequests=borrowRequests+1;
                                Log.e("n1",String.valueOf(borrowRequests));
                            }
                        }
                        if(borrowRequests>0) {
                            NavigationView nav_view = (NavigationView) findViewById(R.id.nav_view);
                            Menu menu = nav_view.getMenu();
                            Log.e("req", String.valueOf(borrowRequests));
                            MenuItem notification = menu.findItem(R.id.notification);
                            String s1 = "Notifications  (" + borrowRequests + ")";
                            SpannableString s = new SpannableString(s1);
                            s.setSpan(new ForegroundColorSpan(ContextCompat.getColor(NavigationActivity.this, R.color.red)), 0, s.length(), 0);
                            notification.setTitle(s);
                        }
                    }
                });


    }
    @Override
    public void onResume() {
        super.onResume();
        registerReceiver(enableInternetReceiver, new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    public void onPause() {
        unregisterReceiver(enableInternetReceiver);
        super.onPause();
    }

    @Override
    public void networkAvailable() {
        Log.e("nc","Internet available!!");

    }

    @Override
    public void networkUnavailable() {
        Log.e("nc","Internet not available");
        Toast.makeText(this,"net not connected",Toast.LENGTH_SHORT).show();

    }
}
