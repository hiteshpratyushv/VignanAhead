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
import android.view.Menu;
import android.view.MenuItem;
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
    Bitmap[] images;
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
        pd.setMessage("Loading Virtual Tours.....");
        pd.show();
        Tours.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                toursDS=dataSnapshot;
                Log.d(TAG,"toursDone");
                images = new Bitmap[(int)toursDS.getChildrenCount()];
                for (int i=1;i<=toursDS.getChildrenCount();i++)
                {
                    final int id = i;
                    String tourName =(String) toursDS.child(i+"").getValue();
                    StorageRef.child(tourName+".jpg").getDownloadUrl()
                            .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    new DownloadImage().execute(uri.toString(),(id-1)+"");
                                }});
                }
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
                Log.d(TAG, "Starting List Creation");
                adapter = new TourAdapter(context, tours);
                tourList.setAdapter(adapter);
                for (int i = 1; i <= toursDS.getChildrenCount(); i++) {
                    final String tourName = (String) toursDS.child(i + "").getValue();
                    url[i - 1] = (String) linksDS.child(tourName).getValue();
                    Tour newTour = new Tour();
                    newTour.tourName = tourName;
                    newTour.tourImage = images[i - 1];
                    adapter.add(newTour);
                    adapter.notifyDataSetChanged();
                }
                Log.d(TAG, "List Created");
                pd.dismiss();
            }
        },15000);

        tourList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                intent.putExtra("URL",url[i]);
                startActivity(intent);
            }
        });
    }

    private class DownloadImage extends AsyncTask<String,Void,Bitmap> {
        int id;
        @Override
        protected Bitmap doInBackground(String... Url) {
            String idS=Url[1];
            id = Integer.parseInt(idS);
            String imageUrl = Url[0];
            Bitmap bitmap = null;
            try{
                InputStream is=new URL(imageUrl).openStream() ;
                bitmap = BitmapFactory.decodeStream(is);
            }catch (Exception e){
                e.printStackTrace();
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            images[id]=bitmap;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_list,menu);
        return  true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.info :
                Intent intent = new Intent(getApplicationContext(),InfoActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
