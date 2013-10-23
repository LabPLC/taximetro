package mx.labplc.codigo.taxis;
import io.socket.SocketIO;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
/**
 * 
 * @author mikesaurio
 *
 */
public class TaxiActivity extends Activity {
	
	
	/**
	 * Declaraci—n de variables
	 */
	TextView tvCoordenadas;//se mostrar‡n las coordenadas y la distancia acumulada
	SocketIO socket;//socket para la conecci—n con el servidor

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_taxi);
		socket=new SocketConnection().connection();
		
		/**
		 * instancias y escuchas
		 */
		tvCoordenadas = (TextView) findViewById(R.id.tvCoordenadas);
		Button btnRunService = (Button) findViewById(R.id.btnRunService);
		btnRunService.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				/**
				 * Se inicia el servicio de geolocalizaci—n
				 */
				ServicioGeolocalizacion.taxiActivity = TaxiActivity.this;
				startService(new Intent(TaxiActivity.this,ServicioGeolocalizacion.class));
			}
		});
		Button btnStopService = (Button) findViewById(R.id.btnStopService);
		btnStopService.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				/**
				 * Se detiene el servicio de geolocalizaci—n
				 */
				stopService(new Intent(TaxiActivity.this,ServicioGeolocalizacion.class));
				tvCoordenadas.setText(getString(R.string.esperando));//regresamos a texto default
			}
		});
	}

	/**
	 * manejo de transmiciones
	 */
	private BroadcastReceiver onBroadcast = new BroadcastReceiver() {
		@Override
		public void onReceive(Context ctxt, Intent i) {
			
				//blanqueamos el texto de las coordenadas si esta el texto default
				if (tvCoordenadas.getText().equals(getString(R.string.esperando))) {
					tvCoordenadas.setText("");
				}
				
				String datos = i.getStringExtra("coordenadas");//obtenemos las coordenadas envidas del servicioGeolocalizaci—n
				String[] tokens = datos.split(";");//separamos por tocken
				tvCoordenadas.append("latitud: " + tokens[0]+ " longitud: " + tokens[1] + " distancia: "+ tokens[2]);
				tvCoordenadas.append("\n");//agregamos salto de linea
	
				JSONObject cadena = new JSONObject(); // Creamos un objeto de tipo JSON
				try {
					// Le asignamos los datos que necesitemos
					cadena.put("lat", tokens[0]); //latitud
					cadena.put("lng", tokens[1]);//longitud

				} catch (JSONException e) {
					e.printStackTrace();
				}

				//generamos la conexi—n con el servidor y mandamos las coordenads
				socket.emit("locmsg", cadena);
		}
	};

	@Override
	protected void onPause() {
		unregisterReceiver(onBroadcast);
		super.onPause();
	}

	@Override
	protected void onResume() {
		registerReceiver(onBroadcast, new IntentFilter("key"));
		super.onResume();
	}
}
