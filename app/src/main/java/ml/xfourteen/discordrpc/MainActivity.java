package ml.xfourteen.discordrpc;

import android.app.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import javax.net.ssl.*;
import org.apache.http.conn.ssl.*;
import javax.security.cert.*;
import com.neovisionaries.ws.client.*;
import java.net.*;
import org.json.simple.parser.*;
import org.json.simple.*;
import java.io.*;


public class MainActivity extends Activity 
{
	
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }
	public void onButtonClicked(View view){
		final TextView logger = findViewById(R.id.logs);
		final String token = ((EditText)findViewById(R.id.token)).getText().toString();
		if(token.equals("")){
			Toast.makeText(this, "Token not specified.", Toast.LENGTH_LONG).show();
			return;
		}
		final String name = ((EditText)findViewById(R.id.name)).getText().toString();
		if(name.equals("")){
			Toast.makeText(this, "Name not specified.", Toast.LENGTH_LONG).show();
			return;
		}
		final boolean show_start = ((Switch)findViewById(R.id.show_start)).isChecked();
		final String details = ((EditText)findViewById(R.id.details)).getText().toString();
		final String state = ((EditText)findViewById(R.id.state)).getText().toString();
		AsyncTask.execute(new Runnable(){
			@Override
			public void run(){
				try {
					WebSocketFactory factory = new WebSocketFactory();
					factory.setVerifyHostname(false);
					final WebSocket ws = factory.createSocket(new URI("wss://gateway.discord.gg?v=6&encoding=json"));
					ws.addListener(new WebSocketAdapter(){
						@Override
						public void onTextMessage(WebSocket websocket, String message) throws Exception{
							JSONParser parser = new JSONParser();
							JSONObject payload = (JSONObject)parser.parse(message);
							long op = payload.get("op");
							if(op == 10){
								ws.sendText("{\"op\":2, \"d\":{\"token\":\""+token+"\", \"properties\":{\"$os\": \"Windows\", \"$browser\": \"Discord for PC\"}}}");
							}
							String t = (String)payload.get("t");
							if(t.equals("READY")){
								logger.setText("Ready. Payload will be sent. Presence status should appear in ~2-5s. If not working - try to click multiple times or reopen app.");
								long now = System.currentTimeMillis();
								ws.sendText("{\"d\":{\"activities\":[{\"name\":\""+name+"\",\"type\":0,\"created_at\":0"+(show_start?",\"timestamps\":{\"start\":"+now+"}":"")+",\"details\":"+(details.equals("")?"null":"\""+details+"\"")+",\"state\":"+(state.equals("")?"null":"\""+state+"\"")+"}],\"afk\":null,\"since\":"+now+",\"status\":\"online\"},\"op\":3}");
							}
						}
					});
					ws.connect();
				}catch(Exception e){
					logger.setText("Error: "+e.toString());
				}
			}
		});
	}
}
