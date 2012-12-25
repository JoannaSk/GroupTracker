package pl.grouptracker;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.Drawable;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;


public class MapItemizedOverlay extends ItemizedOverlay<OverlayItem>
{
    //member variables
    private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
    private Context mContext;
    private int mTextSize;


    public MapItemizedOverlay(Drawable defaultMarker, Context context, int textSize)
    {
        super(boundCenterBottom(defaultMarker));
        mContext = context;
        mTextSize = textSize;
    }


    //In order for the populate() method to read each OverlayItem, it will make a request to createItem(int)
    // define this method to properly read from our ArrayList
    @Override
    protected OverlayItem createItem(int i)
    {
        return mOverlays.get(i);
    }


    @Override
    public int size()
    {
        return mOverlays.size();
    }

    @Override
    protected boolean onTap(int index)
    {
        OverlayItem item = mOverlays.get(index);

        //Do stuff here when you tap, i.e. :
        AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
        dialog.setTitle(item.getTitle());
        dialog.setMessage(item.getSnippet());
        dialog.show();

        //return true to indicate we've taken care of it
        return true;
    }

    @Override
    public void draw(android.graphics.Canvas canvas, MapView mapView, boolean shadow)
    {
        super.draw(canvas, mapView, shadow);

        if (shadow == false)
        {
            //cycle through all overlays
            for (int index = 0; index < mOverlays.size(); index++)
            {
                OverlayItem item = mOverlays.get(index);

                // Converts lat/lng-Point to coordinates on the screen
                GeoPoint point = item.getPoint();
                Point ptScreenCoord = new Point() ;
                mapView.getProjection().toPixels(point, ptScreenCoord);

                //Paint
                Paint paint = new Paint();
                paint.setTextAlign(Paint.Align.CENTER);
                paint.setTextSize(mTextSize);
                paint.setARGB(255, 0, 0, 0); // alpha, r, g, b (Black, semi see-through)

                //show text to the right of the icon
                canvas.drawText(item.getTitle(), ptScreenCoord.x, ptScreenCoord.y+mTextSize, paint);
            }
        }
    }


    public void addOverlay(OverlayItem overlay)
    {
        mOverlays.add(overlay);
        populate();
    }


    public void removeOverlay(OverlayItem overlay)
    {
        mOverlays.remove(overlay);
        populate();
    }


    public void clear()
    {
        mOverlays.clear();
        populate();
    }

}
