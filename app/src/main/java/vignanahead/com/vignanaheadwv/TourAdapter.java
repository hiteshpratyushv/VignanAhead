package vignanahead.com.vignanaheadwv;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

public class TourAdapter extends ArrayAdapter<Tour> {

    Context context;
    LayoutInflater inflater=null;
    private ArrayList<Tour> tours = new ArrayList<Tour>();
    View vi;
    int count;

    public TourAdapter(@NonNull Context context, ArrayList<Tour> tours) {
        super(context,R.layout.tour_list);
        this.context=context;
        inflater=LayoutInflater.from(context);
        this.tours=tours;
        count=0;
    }

    @Override
    public void add(@Nullable Tour object) {
        super.add(object);
        tours.add(object);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        vi=convertView;
        Log.d("getView","Inside getView");
        if(convertView==null)
            vi = inflater.inflate(R.layout.tour_list, null);
        Tour currentTour=tours.get(position);
        ((TextView)vi.findViewById(R.id.txt)).setText(currentTour.tourName);
        String imageLink = currentTour.tourImageLink;
        new DownloadImage().execute(imageLink);
        return vi;
    }

    private class DownloadImage extends AsyncTask<String,Void,Bitmap> {
        @Override
        protected Bitmap doInBackground(String... Url) {
            String imageUrl = Url[0];
            Log.d("Downloader","In Downloader");
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
            ((ImageView)vi.findViewById(R.id.img)).setImageBitmap(bitmap);
        }
    }
}
