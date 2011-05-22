package com.platinummonkey.irssifusion;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.AdapterContextMenuInfo;
//Content Provider imports
import static android.provider.BaseColumns._ID;
import static com.platinummonkey.irssifusion.Constants.TABLE_NAME;
import static com.platinummonkey.irssifusion.Constants.CONTENT_URI;
import static com.platinummonkey.irssifusion.Constants.TIME;
import static com.platinummonkey.irssifusion.Constants.SEND;
import static com.platinummonkey.irssifusion.Constants.SERVER;
import static com.platinummonkey.irssifusion.Constants.TYPE;
import static com.platinummonkey.irssifusion.Constants.TOPIC;
import static com.platinummonkey.irssifusion.Constants.NICK;
import static com.platinummonkey.irssifusion.Constants.ADDRESS;
import static com.platinummonkey.irssifusion.Constants.MESSAGE;
import static com.platinummonkey.irssifusion.Constants.HILIGHT;
import static com.platinummonkey.irssifusion.Constants.NICKLIST;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.SimpleCursorAdapter;
//import android.util.Log;

public class irssifusion extends Activity {
	private static String[] FROM = { _ID, TIME, SEND, SERVER, TYPE, TOPIC, NICK, ADDRESS, MESSAGE, HILIGHT, NICKLIST, };
	//private static int[] TO = { R.id.rowid, R.id.time, R.id.send, R.id.server, R.id.type, R.id.topic, R.id.nick, R.id.address, R.id.message, R.id.hilight, R.id.nicklist, };
	private static String ORDER_BY = TIME + " DESC";

//    /** Called when the activity is first created. */
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.main);
//    }
//}
//
//public class ClientActivity extends Activity {

    //private String serverIpAddress = "64.85.168.188";
    
    //private boolean connected = false;
    
	TextView txtServerChannel;
	TextView txtChannelTopic;
	ListView listMessages;
	protected ArrayList<Privmsg> privMsgs = new ArrayList<Privmsg>();
    
	Socket testSocket = null;
	//DataOutputStream out = null;
	//DataInputStream in = null;
	PrintWriter out = null;
	BufferedReader in = null;
	String plaintext;
	private MessagesData channelMessages;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.channel);
        
        // start the service if it didn't start on boot
        startService(new Intent(IrssiFusionService.class.getName()));
        
        startServer();
        
        channelMessages = new MessagesData(this);
        
        try {
        	//addEvent("Hello, Android!" );
        	Cursor cursor = getMessages();
        	channelTest(cursor);
        	//showEvents(cursor);
        } finally {
        	channelMessages.close();
        }
        
        txtServerChannel = (TextView) findViewById(R.id.serverchannel);
        txtChannelTopic = (TextView) findViewById(R.id.channeltopic);
        listMessages = (ListView) findViewById(R.id.ListView01);
        
        listMessages.setOnItemClickListener(new OnItemClickListener() {
        	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        		arg1.showContextMenu();
        	}
        });
        
        //ListView listMessages = getListView();
        //listMessages.setTextFilterEnabled(true); // type filtering
        /*btnChannel = (Button) findViewById(R.id.btnChannel);
        btnChannel.setOnClickListener(new Button.OnClickListener() {
        	public void onClick(View v) {
        		examineXMLFile();
        	}
        });
        
        btnQuery = (Button) findViewById(R.id.btnQuery);
        btnQuery.setOnClickListener(new Button.OnClickListener() {
        	public void onClick(View v) {
        		examineJSONFile();
        	}
        });
     */
      //startServer();
      //channelTest();
      
      
//      listMessages.setOnItemClickListener(new OnItemLongClickListener() {
//    	    public void onItemClick(AdapterView<?> parent, View view,
//    	        int position, long id) {
//    	      // When clicked, show a toast with the TextView text
//    	      Toast.makeText(getApplicationContext(), ((TextView) view).getText(),
//    	          Toast.LENGTH_SHORT).show();
//    	    }
//      });
    }
    
    private Cursor getMessages() {
    	// Perform a managed query. The Activity will handle closing
    	// and re-querying the cursor when needed.
    	SQLiteDatabase db = channelMessages.getReadableDatabase();
    	Cursor cursor = db.query(TABLE_NAME, FROM, null, null, null,
    	null, ORDER_BY); // TABLE_NAME, FROM, WHERE, GROUP_BY, HAVING, ORDER_BY
    	startManagingCursor(cursor);
    	return cursor;
    }
    
    public void startServer() {
		// starts server
		try {
			System.out.println("Connecting to Server");
			testSocket = new Socket("platinummonkey.com", 5555);
			//out = new DataOutputStream(testSocket.getOutputStream());
			//in = new DataInputStream(testSocket.getInputStream());
			out = new PrintWriter(testSocket.getOutputStream(), true);
         	in = new BufferedReader(new InputStreamReader(testSocket.getInputStream()));
         	txtServerChannel.setText("Server connected");
		} catch (UnknownHostException e) {
			txtServerChannel.setText("Failed connecting " + e);
		} catch (IOException e) {
			txtServerChannel.setText("IO exception in connecting " + e);
		}
	}
    
    private void refreshList() {
    	listMessages.setAdapter(new ArrayAdapter<Privmsg>(this, R.layout.channel_list_item, privMsgs));
    }
    
    public void channelTest(Cursor cursor) {
    	
//    	// Stuff them all into a big string
//    	StringBuilder builder = new StringBuilder(
//    	"Saved events:\n" );
//    	while (cursor.moveToNext()) {
//    	// Could use getColumnIndexOrThrow() to get indexes
//    	long id = cursor.getLong(0);
//    	long time = cursor.getLong(1);
//    	String title = cursor.getString(2);
//    	builder.append(id).append(": " );
//    	builder.append(time).append(": " );
//    	builder.append(title).append("\n" );
//    	}
//    	// Display on the screen
//    	TextView text = (TextView) findViewById(R.id.text);
//    	text.setText(builder);

    	
    	listMessages.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
    		public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
    			menu.setHeaderTitle("ContextMenu");
    			menu.add(0, 1, 1, "Something should happen");
    		}
    	});
    	
    	try {
    		// test server stuff
    		if (testSocket != null && out != null && in != null) {
   			 	try {
   			 		//System.out.println("Sending \"test\" to server");
   			 		//System.out.println("Sent: test");
   			 		out.println("test");
   			 		//System.out.println("Waiting for Server to reply");
   			 		String responseLine;
   			 		//responseLine = in.readLine(); //.replaceAll("\\\\n", "");
   			 		responseLine = in.readLine();
   			 		//System.out.println("Recieved from Server: " + responseLine + " - length: " + responseLine.length());
   			 		//System.out.println("\nEscaped: "+ responseLine.replaceAll("\\", "\\\\"));
   			 		plaintext = responseLine;
   			 		//System.out.println("Recieved from Server: " + plaintext);
   			 		//System.out.println("Closing Connection");
   			 		out.close();
   			 		in.close();
   			 		testSocket.close();
   			 	} catch (UnknownHostException e) {
   			 		//System.err.println("Trying to connect to unknown host:" + e);
   			 		plaintext = "failure" + e;
   			 	} catch (IOException e) {
   			 		//System.err.println("IOException: " + e);
   			 		plaintext = "failure" + e;
   			 	}
   		 	}
    		// end test server stuff
    		String jString = plaintext;
    		//String jString = "{\"server\": \"freenode\",  \"channel\": \"#testchannel\", \"messages\": [ {\"time\": \"timestamp\", \"nick\": \"testNick\", \"message\": \"message 1 this is the message\", \"address\": \"somenick@someplace.com\"}, {\"time\": \"timestamp\", \"nick\": \"otherNick\", \"message\": \"message 2 this is the message\", \"address\": \"othernick@someplace.com\"} ] }";
    		JSONObject jObject = new JSONObject(jString);
    		String serverChannelString = jObject.getString("server") + " | " + jObject.getString("channel");
    		txtServerChannel.setText(serverChannelString);
    		JSONArray messagesArray = jObject.getJSONArray("messages");
    		txtChannelTopic.setText("There are " + messagesArray.length() + "messages");
    		String [] messages_arr = new String[messagesArray.length()];
    		int i;
    		for (i=0;i<messagesArray.length();i++) {
    			JSONObject privmsg = messagesArray.getJSONObject(i);
    			String msgNick = privmsg.getString("nick").toString();
    			String msgMessage = privmsg.getString("message").toString();
    			String msgTimestamp = privmsg.getString("time").toString();
    			String msgAddress = privmsg.getString("address").toString();
    			privMsgs.add(new Privmsg(msgTimestamp, msgNick, msgAddress, msgMessage));
    			messages_arr[i] = "[" + msgTimestamp + "] " + msgNick + " | " + msgMessage;
    		}
    		refreshList();
    		//listMessages.setAdapter(new ArrayAdapter<String>(this, R.layout.channel_list_item, messages_arr));
    	} catch (Exception je) {
    		txtChannelTopic.setText("failure " + je);
    	}
    }
    
    public boolean onContextItemSelected(MenuItem aItem) {
    	AdapterContextMenuInfo info = (AdapterContextMenuInfo) aItem.getMenuInfo();
    	
    	switch (aItem.getItemId()) {
    		case 1:
    			Privmsg privmsgContexted = (Privmsg) listMessages.getAdapter().getItem(info.position);
    			privMsgs.remove(privmsgContexted);
    			refreshList();
    			return true;
    	}
    	
    	return false;
    }
    
    protected class Privmsg {
    	protected String nick;
    	protected String address;
    	protected String message;
    	protected String time;
    	
    	protected Privmsg(String time, String nick, String address, String message) {
    		this.time = time;
    		this.nick = nick;
    		this.address = address;
    		this.message = message;
    	}
    	
    	public String toString() {
    		return "[" + time + "] " + nick + " | " + message;
    	}
    }
}