package vignanahead.com.vignanaheadwv;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;

public class ListActivity extends AppCompatActivity {
    static final String TAG = "Firebase";
    Context context = this;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference Links = database.getReference("Links");
    DatabaseReference Tours = database.getReference("Tours");
    StorageReference StorageRef = FirebaseStorage.getInstance().getReference();
    ListView tourList;
    boolean tourComp=false;
    ProgressDialog pd;
    TourAdapter adapter;
    ArrayList<Tour> tours;
    String[] url=new String[100];
    DataSnapshot toursDS,linksDS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        tourList=(ListView)findViewById(R.id.tourList);
        tours =new ArrayList<Tour>();
        pd = new ProgressDialog(this);
        pd.setMessage("Fetching.....");
        pd.show();
        Tours.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                toursDS=dataSnapshot;
                Log.d(TAG,"toursDone");
                tourComp=true;
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
        Links.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                linksDS=dataSnapshot;
                Log.d(TAG,"linksDone");
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });


         new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG,"Starting List Creation");
                adapter = new TourAdapter(context,tours);
                tourList.setAdapter(adapter);
                for(int i=1;i<=toursDS.getChildrenCount();i++)
                {
                    final String tourName = (String) toursDS.child(i+"").getValue();
                    final String[] imageUri = new String[1];
                    url[i-1] =(String) linksDS.child(tourName).getValue();

                    StorageRef.child(tourName+".jpg").getDownloadUrl()
                            .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    imageUri[0]=uri.toString();
                                }});
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Tour newTour = new Tour();
                            newTour.tourName=tourName;
                            newTour.tourImageLink=imageUri[0];
                            adapter.add(newTour);
                            adapter.notifyDataSetChanged();
                        }
                    },3000);
                }
                Log.d(TAG,"List Created");
                pd.dismiss();
            }
        },3000);

        tourList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                intent.putExtra("URL",url[i]);
                startActivity(intent);
            }
        });
    }

}
