package mx.labplc.codigo.taxis;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

/**
 * 
 * @author mikesaurio
 *
 */
public class ServicioGeolocalizacion extends Service implements Runnable{
	/**
	 * Declaraci—n de variables
	 */
	protected static TaxiActivity taxiActivity;
	private LocationManager mLocationManager;
	private MyLocationListener mLocationListener;
	private double latitud_inicial=0 ;
	private double longitud_inicial=0;
	private double distancia_acumulada=0;
	private Location currentLocation = null;
	private Thread thread;

    @Override
    public void onCreate() {
          Toast.makeText(this,"Servicio creado", Toast.LENGTH_SHORT).show();
          super.onCreate();
      	mLocationListener = new MyLocationListener();
		mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intenc, int flags, int idArranque) {
         // Toast.makeText(this,"Servicio arrancado "+ idArranque,Toast.LENGTH_SHORT).show();  
          obtenerSenalGPS();
          return START_STICKY;
    }


	@Override
    public void onDestroy() {
		if (mLocationManager != null)
			if (mLocationListener != null)
				mLocationManager.removeUpdates(mLocationListener);
		
        Toast.makeText(this,"Servicio detenido ",Toast.LENGTH_SHORT).show();
    	    super.onDestroy();
       
    }

    @Override
    public IBinder onBind(Intent intencion) {
          return null;
    }
    
    /**
     * handler
     */
    private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// mLocationManager.removeUpdates(mLocationListener);
			updateLocation(currentLocation);
		}
	};
	
	
	/**
	 * metodo para actualizar la localizaci—n
	 * 
	 * @param currentLocation
	 * @return void
	 */
	public void updateLocation(Location currentLocation) {
		if (currentLocation != null) {
			double latitud = Double.parseDouble(currentLocation.getLatitude() + "");
			double longitud = Double.parseDouble(currentLocation.getLongitude() + "");

			Location locationA = new Location("punto A");
			locationA.setLatitude(latitud_inicial);
			locationA.setLongitude(longitud_inicial);

			Location locationB = new Location("punto B");
			locationB.setLatitude(latitud);
			locationB.setLongitude(longitud);

			float distance = locationA.distanceTo(locationB);
			latitud_inicial = latitud;
			longitud_inicial = longitud;

			distancia_acumulada += distance;

			getApplicationContext().sendBroadcast(
					new Intent("key").putExtra("coordenadas", latitud + ";"
							+ longitud + ";" + distancia_acumulada + ""));

	
		}
	}

	
	/**
	 * Hilo de la aplicacion para cargar las cordenadas del usuario
	 */
	public void run() {
		if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			Looper.prepare();
			mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 4000, 0, mLocationListener);
			Looper.loop();
			Looper.myLooper().quit();
		} else {
			taxiActivity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(getApplicationContext(), "GPS apagado inesperadamente", Toast.LENGTH_LONG).show();			
				}
			});
		}
	}


	

	/**
	 * Metodo para Obtener la se–al del GPS
	 */
	private void obtenerSenalGPS() {
		thread = new Thread(this);
		thread.start();
	}

	/**
	 * Metodo para asignar las cordenadas del usuario
	 * */
	private void setCurrentLocation(Location loc) {
		currentLocation = loc;
	}

	/**
	 * Metodo para obtener las cordenadas del GPS
	 */
	private class MyLocationListener implements LocationListener {

		public void onLocationChanged(Location loc) {
			//Log.d("finura",loc.getAccuracy()+"");
			if (loc != null) {
				setCurrentLocation(loc);
				handler.sendEmptyMessage(0);
			}
		}

		/**
		 * metodo que revisa si el GPS esta apagado
		 */
		public void onProviderDisabled(String provider) {
			taxiActivity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(getApplicationContext(), "GPS apagado inesperadamente", Toast.LENGTH_LONG).show();			
				}
			});
		}

		// @Override
		public void onProviderEnabled(String provider) {
		}

		// @Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
		}
	}

}