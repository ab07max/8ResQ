package com.avinash.requestresource;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;

/**
 * A fragment representing a list of Items.
 */
public class ItemFragment extends Fragment {

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 1;
    private String TAG = "in Item Fragment";
    RecyclerView recyclerView;
    private ProgressDialog nDialog;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser user = mAuth.getCurrentUser();

    private ArrayList<Requests> allRequests;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ItemFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static ItemFragment newInstance(int columnCount) {
        ItemFragment fragment = new ItemFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_item_list, container, false);




        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            recyclerView = (RecyclerView) view;

            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
            MainActivity mainActivity = (MainActivity) getActivity();
            mainActivity.hideFABbuttons();

            nDialog = new ProgressDialog(getActivity());

            nDialog.setIndeterminate(false);
            nDialog.setCancelable(true);


            recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getActivity(),
                    recyclerView, new ClickListener() {
                @Override
                public void onClick(View view, final int position) {
                    //Values are passing to activity & to fragment as well
//                    Toast.makeText(getActivity(), "Single Click on :"+allRequests.get(position).getTitle(),
//                            Toast.LENGTH_SHORT).show();

                    Intent newRequestIntent = new Intent(getContext(), NewRequest.class);
                    newRequestIntent.putExtra("action", "view");
                    newRequestIntent.putExtra("userID",allRequests.get(position).getUserID());
                    newRequestIntent.putExtra("title",allRequests.get(position).getTitle());
                    newRequestIntent.putExtra("description",allRequests.get(position).getDescription());
                    newRequestIntent.putExtra("comments",allRequests.get(position).getComments());
                    newRequestIntent.putExtra("quantity",Integer.toString(allRequests.get(position).getQuantity()));
                    newRequestIntent.putExtra("priority",allRequests.get(position).getPriority());
                    newRequestIntent.putExtra("requestID",allRequests.get(position).getRequestID());
                    newRequestIntent.putExtra("type",allRequests.get(position).getType());
                    newRequestIntent.putExtra("completed",allRequests.get(position).isCompleted());
                    startActivity(newRequestIntent);
                    getActivity().finish();
                }

                @Override
                public void onLongClick(View view, int position) {
//                    Toast.makeText(getActivity(), "Long press on position :"+position,
//                            Toast.LENGTH_LONG).show();
                }
            }));


        }
        return view;
    }

    public static interface ClickListener{
        public void onClick(View view,int position);
        public void onLongClick(View view,int position);
    }

    class RecyclerTouchListener implements RecyclerView.OnItemTouchListener{

        private ClickListener clicklistener;
        private GestureDetector gestureDetector;

        public RecyclerTouchListener(Context context, final RecyclerView recycleView, final ClickListener clicklistener){

            this.clicklistener=clicklistener;
            gestureDetector=new GestureDetector(context,new GestureDetector.SimpleOnGestureListener(){
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    View child=recycleView.findChildViewUnder(e.getX(),e.getY());
                    if(child!=null && clicklistener!=null){
                        clicklistener.onLongClick(child,recycleView.getChildAdapterPosition(child));
                    }
                }
            });
        }

        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
            View child=rv.findChildViewUnder(e.getX(),e.getY());
            if(child!=null && clicklistener!=null && gestureDetector.onTouchEvent(e)){
                clicklistener.onClick(child,rv.getChildAdapterPosition(child));
            }

            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {

        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }
    }

    public void getQueryResults() {
        nDialog.show();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("requests")
                .whereEqualTo("userID", currentUser.getUid()).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task< QuerySnapshot > task) {
                        nDialog.dismiss();
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
                                request.setType(document.get("type").toString());
                                request.setRequestID(document.getId().toString());
                                requests.add(request);
                                allRequests  = requests;
                                Log.d(TAG, document.getId() + " => " + document.getData());
                            }
                            recyclerView.setAdapter(new MyItemRecyclerViewAdapter(requests));
                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
                    }
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        MainActivity mainActivity = (MainActivity) getActivity();
        if(mainActivity.getUserRole().equals("staff")) {
            getQueryResults();
        }else{
            getQueryResultsForAdmin();
        }
        mainActivity.hideFABbuttons();
    }

    private void getQueryResultsForAdmin() {
        nDialog.show();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("requests")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task< QuerySnapshot > task) {
                        nDialog.dismiss();
                        if (task.isSuccessful()) {
                            ArrayList < Requests > requests = new ArrayList < Requests > ();
                            for (QueryDocumentSnapshot document: task.getResult()) {
                                Requests request = new Requests();
                                request.setQuantity((Integer.parseInt(document.get("quantity").toString())));
                                request.setCompleted(Boolean.parseBoolean(document.get("completed").toString()));
                                request.setTitle(document.get("title").toString());
                                request.setDescription(document.get("description").toString());
                                request.setUserID(document.get("userID").toString());
                                request.setPriority(document.get("priority").toString());
                                request.setComments(document.get("comments").toString());
                                request.setType(document.get("type").toString());
                                request.setRequestID(document.getId().toString());
                                requests.add(request);
                                allRequests = requests;
                                Log.d(TAG, document.getId() + " => " + document.getData());
                            }
                            recyclerView.setAdapter(new MyItemRecyclerViewAdapter(requests));
                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
                    }
                });
    }

}