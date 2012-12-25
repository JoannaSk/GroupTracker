package pl.grouptracker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import pl.grouptracker.network.AndroidClient;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.Drawable;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

import android.view.Menu;
import android.widget.TextView;

public class MainActivity extends MapActivity {
	private MapController controller;
	private String message;
	private MapView myMap;
	private MapItemizedOverlay itemizedOverlay;
	private List<Overlay> mapOverlays;
	private Drawable drawable;
	private NameAndLocation nameAndLocation;
	private AndroidClient serverConnection;
	private BlockingQueue<NameAndLocation> queue;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        Intent intent = getIntent();
        message = intent.getStringExtra(UsernameActivity.EXTRA_MESSAGE);

        myMap = (MapView) findViewById(R.id.mvMain); 
        myMap.setBuiltInZoomControls(true);
                
        MyLocationOverlay myLocationOverlay = new MyLocationOverlay(this, myMap);
        myMap.getOverlays().add(myLocationOverlay);
        myMap.postInvalidate();
        //myLocationOverlay.enableMyLocation();
        
        controller = myMap.getController();
        controller.setZoom(10);
        myMap.invalidate();
        
        mapOverlays = myMap.getOverlays();
        drawable = this.getResources().getDrawable(R.drawable.green_marker);
        itemizedOverlay = new MapItemizedOverlay(drawable, this, 20);
        mapOverlays.add(itemizedOverlay);
        
        // Sending
        queue = new LinkedBlockingQueue<NameAndLocation>();
        serverConnection = new AndroidClient(queue);
        Thread senderThread = new Thread(serverConnection);
        senderThread.start(); 

        // Receiving
        Thread getOthersLocations;
        getOthersLocations = new Thread() {
        	@Override
        	public void run() {
        		while(true) {
        			try {
						nameAndLocation = serverConnection.getNewLocationAndName();
						MainActivity.this.runOnUiThread(new Runnable() {
							@Override
							public void run() {
			        		    GeoPoint geoPoint = new GeoPoint((int)nameAndLocation.getLatitude(), (int)nameAndLocation.getLongitude());
			        			itemizedOverlay.addOverlay(new OverlayItem(geoPoint, nameAndLocation.getName(), nameAndLocation.getName() + " location"));
			        			myMap.invalidate();
			        			controller.setCenter(geoPoint);
							}
						});
					} catch (Exception e) {
						e.printStackTrace();
					}
        		}
        	}
       };
       getOthersLocations.start();
    
                
        LocationManager manager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
        LocationListener listener = new LocationListener() {

			@Override
			public void onLocationChanged(Location location) {
				MainActivity.this.changeLocation(location);
			}

			@Override
			public void onProviderDisabled(String arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onProviderEnabled(String arg0) { }

			@Override
			public void onStatusChanged(String arg0, int arg1, Bundle arg2) { }
        };
        
        manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, listener);
    }
    
    public void changeLocation(Location location) {
		GeoPoint gPoint = new GeoPoint((int)(location.getLatitude()*1E6), (int)(location.getLongitude()*1E6));
		OverlayItem overlayItem = new OverlayItem(gPoint, message, "my location");
		itemizedOverlay.clear();
		itemizedOverlay.addOverlay(overlayItem);
		controller.setCenter(gPoint);
		myMap.invalidate();	
		try {
			queue.put(new NameAndLocation(message, (int)(location.getLongitude()*1E6), (int)(location.getLatitude()*1E6)));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
    }
 
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}
}
