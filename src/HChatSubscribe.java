import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class HChatSubscribe implements MqttCallback {

	HChat hChat;
	String FileName = "Test";
	public HChatSubscribe() {
		// TODO Auto-generated constructor stub
		hChat = new HChat();
	}

	public void SubscribeMqtt()
	{
		try {
		    int iQos             = Integer.parseInt(hChat.qos);
			String broker       = "tcp://" + hChat.serverIP + ":" + hChat.port;
		      
	        MemoryPersistence dataStore = new MemoryPersistence();

		    //String uri = System.getenv("CLOUDMQTT_URL");
		    // Construct the connection options object that contains connection parameters 
		    // such as cleanSession and LWT
		    MqttConnectOptions conOpt = new MqttConnectOptions();
		    conOpt.setCleanSession(true);
		  //  conOpt.setUserName(this.userName);	   
		   // conOpt.setPassword(this.password.toCharArray());

		    // Construct an MQTT blocking mode client
		    MqttClient client = new MqttClient(broker, hChat.clientId, dataStore);
		    
		    // Set this wrapper as the callback handler
		    client.setCallback(this);
		    // Connect to the MQTT server
		    client.connect(conOpt);

		    // Subscribe to the requested topic
		    // The QoS specified is the maximum level that messages will be sent to the client at. 
		    // For instance if QoS 1 is specified, any messages originally published at QoS 2 will 
		    // be downgraded to 1 when delivering to the client but messages published at 1 and 0 
		    // will be received at the same level they were published at. 
		    //log("Subscribing to topic \""+topic+"\" qos "+qos);
		    if ( hChat.topic != null)
		    {
		    	System.out.println("Subscribing to topic " + hChat.topic + " qos "+iQos);
		    	client.subscribe( hChat.topic, iQos);
		    	System.out.println("Subscribing to topic " + "Chat" + " qos "+iQos);
		    	client.subscribe("Chat", iQos);
		    	System.out.println("Subscribing to topic " + "File" + " qos "+iQos);
		    	client.subscribe("File", iQos);
		    }
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	@Override
	public void connectionLost(Throwable cause) {
		   // Called when the connection to the server has been lost.
	    // An application may choose to implement reconnection
	    // logic at this point. This sample simply exits.
	    //log("Connection to " + brokerUrl + " lost!" + cause);
		  System.out.println("Connection to " + hChat.serverIP + ":"+ hChat.port + " lost!" + cause);
	    System.exit(1);
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken token) {
	    // Called when a message has been delivered to the
	    // server. The token passed in here is the same one
	    // that was passed to or returned from the original call to publish.
	    // This allows applications to perform asynchronous 
	    // delivery without blocking until delivery completes.
	    //
	    // This sample demonstrates asynchronous deliver and 
	    // uses the token.waitForCompletion() call in the main thread which
	    // blocks until the delivery has completed. 
	    // Additionally the deliveryComplete method will be called if 
	    // the callback is set on the client
	    // 
	    // If the connection to the server breaks before delivery has completed
	    // delivery of a message will complete after the client has re-connected.
	    // The getPendingTokens method will provide tokens for any messages
	    // that are still to be delivered.
	}

	@Override
	public void messageArrived(String topic, MqttMessage message) throws Exception {
		 // Called when a message arrives from the server that matches any
	    // subscription made by the client		
	    //String time = new Timestamp(System.currentTimeMillis()).toString();
		/*	  
	    System.out.println( "Topic:\t" + topic + 
	        //"  Consumed Message:\t" + new String(message.getPayload()) +
	        "  QoS:\t" + message.getQos());
	    */  
		  if(topic.compareTo("File") == 0)
		  {
			  if(message.getPayload().length == 44 )
			  {
				  FileHeader header = new FileHeader();
				  if( header.Parse(message.getPayload()) == true)
				  {
					  FileName = header.GetFileName();
					  FileName += "x";
					  System.out.println("FileName in Subscribe=" + FileName);
					  CreateFile(FileName);
					  return;
				  }
			  }
	
			  WriteToFile(message.getPayload(),FileName);
		  }
		  else if (topic.compareTo("HeartBeat") == 0){
			  System.out.println( "Topic:\t" + topic + 
				        "  HeartBeat Received...Consumed Message:\t" + new String(message.getPayload()) +
				        "  QoS:\t" + message.getQos());
				    	  
		  }
		  else {
			  System.out.println( "Topic:\t" + topic + 
				        "  Consumed Message:\t" + new String(message.getPayload()) +
				        "  QoS:\t" + message.getQos());
		  }
	}
	
	  void WriteToFile(byte[] aInput, String aOutputFileName){
		  System.out.println("Writing binary file...");
		  try {
		      OutputStream output = null;
		      try {
		        output = new BufferedOutputStream(new FileOutputStream(aOutputFileName, true));
		        output.write(aInput);
		      }
		      finally {
		        output.close();
		      }
		  }
		  catch(FileNotFoundException ex){
			  System.out.println("File not found.");
		  }
		  catch(IOException ex){
			  System.out.println(ex);
		  }
	  }
	  
	  void CreateFile(String aOutputFileName) {
		  System.out.println("Writing binary file...");
		  try {
		      OutputStream output = null;
		      try {
		        output = new BufferedOutputStream(new FileOutputStream(aOutputFileName));
		      }
		      finally {
		        output.close();
		      }
		  }
		  catch(FileNotFoundException ex){
			  System.out.println("File not found.");
		  }
		  catch(IOException ex){
			  System.out.println(ex);
		  }		  
	  }
}
