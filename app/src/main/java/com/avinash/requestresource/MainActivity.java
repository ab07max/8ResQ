package com.avinash.requestresource;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.TextView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    boolean isRotate = false;
    private static final String TAG = "In Main Activity ";
    private FloatingActionButton fabMic;
    private FloatingActionButton fabCall;
    private ArrayList<Requests> requestList;
    //static fields
    private static String userRole;
    private static String userEmail;

    public void setUserRole(String role){
        this.userRole = role;
    }

    public static void setUserEmail(String userEmail) {
        MainActivity.userEmail = userEmail;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        fabMic = (FloatingActionButton) findViewById(R.id.fabMic);
        fabCall = (FloatingActionButton) findViewById(R.id.fabCall);
        ViewAnimation.init((FloatingActionButton) findViewById(R.id.fabCall));
        ViewAnimation.init((FloatingActionButton) findViewById(R.id.fabMic));


        setSupportActionBar(toolbar);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FloatingActionButton fab = findViewById(R.id.fab);

        fabCall.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(View v) {
                Intent callIntent = new Intent(Intent.ACTION_DIAL);
                callIntent.setData(Uri.parse("tel:4703579315"));
                startActivity(callIntent);
            }
        });

        fabMic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent newRequestIntent = new Intent(getApplicationContext(), NewRequest.class);
                startActivity(newRequestIntent);
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isRotate = ViewAnimation.rotateFab(view, !isRotate);
                if (isRotate) {
                    ViewAnimation.showIn(fabCall);
                    ViewAnimation.showIn(fabMic);
                } else {
                    ViewAnimation.showOut(fabCall);
                    ViewAnimation.showOut(fabMic);
                }
            }
        });
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_list, R.id.nav_logout)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        View headerView = navigationView.getHeaderView(0);
        TextView navUsername = (TextView) headerView.findViewById(R.id.app_username);
        navUsername.setText("Hey User!");
        TextView navEmail = (TextView) headerView.findViewById(R.id.app_email);
        navEmail.setText(this.userEmail);
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideFABbuttons();
        getQueryResults();
    }

    public void hideFABbuttons(){
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        isRotate = ViewAnimation.rotateFab(fab, false);
        FloatingActionButton fabCall = (FloatingActionButton) findViewById(R.id.fabCall);
        FloatingActionButton fabMic = (FloatingActionButton) findViewById(R.id.fabMic);
        ViewAnimation.showOut(fabCall);
        ViewAnimation.showOut(fabMic);
    }

    private void retrieveRequests() {
        getQueryResults();
    }

    public void getQueryResults() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("requests")
                .whereEqualTo("userID", currentUser.getUid()).get()
                .addOnCompleteListener(new OnCompleteListener < QuerySnapshot > () {
                    @Override
                    public void onComplete(@NonNull Task < QuerySnapshot > task) {
                        if (task.isSuccessful()) {
                            ArrayList < Requests > requests = new ArrayList < Requests > ();
                            for (QueryDocumentSnapshot document: task.getResult()) {
                                Requests request = new Requests();
                                request.setQuantity(Integer.parseInt((document.get("quantity").toString())));
                                request.setCompleted(Boolean.parseBoolean(document.get("completed").toString()));
                                request.setTitle(document.get("title").toString());
                                request.setDescription(document.get("description").toString());
                                request.setUserID(document.get("userID").toString());
                                request.setPriority(document.get("priority").toString());
                                request.setComments(document.get("comments").toString());
                                requests.add(request);
                                Log.d(TAG, document.getId() + " => " + document.getData());
                            }
                            updateUI(requests, "requestslist");
                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
                    }
                });
    }

    private void updateUI(ArrayList < Requests > requests, String UITag) {
        if (UITag.equals("requestslist")) {
            //set adapter and list out the requests.
            requestList = (ArrayList<Requests>) requests.clone();
            System.out.println("data arrived");
        } else {

        }
    }

    public ArrayList<Requests> returnRequests(){
        return requestList;
    }

    @Override
    public void onBackPressed() {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent settingsActivityIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingsActivityIntent);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration) ||
                super.onSupportNavigateUp();
    }

    public String getUserRole() {
        return this.userRole;
    }
}