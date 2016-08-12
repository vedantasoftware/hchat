import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.Thread;

public class HChatClient extends Thread implements MqttCallback  {

	HChat hChat;
	private String threadName;
    private static int count = 0;
	
	public HChatClient() {
		// TODO Auto-generated constructor stub
		hChat = new HChat();
	}
	
	HChatClient( String name){
        threadName = name;
    	hChat = new HChat();
        System.out.println("Creating " +  threadName );
    }

	@Override
	public void connectionLost(Throwable cause) {
		// TODO Auto-generated method stub

	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken token) {
		// TODO Auto-generated method stub

	}

	@Override
	public void messageArrived(String topic, MqttMessage message) throws MqttException {
		// TODO Auto-generated method stub

	}
	
   public void run(){
	    	
        while(true){
            try{
	                synchronized(hChat){
	            		KeepAliveMqtt();
                }
                Thread.sleep(2000);
	            } catch (InterruptedException iex) {
	                System.out.println("Exception in thread: "+iex.getMessage());
	                Thread.currentThread().interrupt();
	            }
        }
	    	
    }
	 
	    
    public void KeepAliveMqtt()
	{
		try {
			    //String topic        = "test-topic";
			    //String content      = "Message from Subscribe1";
			    int iQos             = Integer.parseInt(hChat.qos);
			    // String broker       = "tcp://iot.eclipse.org:1883";
			    //String broker       = "tcp://192.168.1.134:1883";
				String broker       = "tcp://" + hChat.serverIP + ":" + hChat.port;
			    //String clientId     = "JavaSample1";
				String clientId     = hChat.clientId;
			      
			 //   MqttDefaultFilePersistence dataStore = new MqttDefaultFilePersistence(tmpDir); 
		        MemoryPersistence dataStore = new MemoryPersistence();
		
			    //String uri = System.getenv("CLOUDMQTT_URL");
			    // Construct the connection options object that contains connection parameters 
			    // such as cleanSession and LWT
			    MqttConnectOptions conOpt = new MqttConnectOptions();
			    conOpt.setCleanSession(true);
			  //  conOpt.setUserName(this.userName);	   
			   // conOpt.setPassword(this.password.toCharArray());
		
			    // Construct an MQTT blocking mode client
			    MqttClient client = new MqttClient(broker, clientId, dataStore);
		
			    // Connect to the MQTT server
			    client.connect(conOpt);
		
			    // Create and configure a message
			    String MessageContent = hChat.content  + Integer.toString(count++, 10);
			    
			    MqttMessage message = new MqttMessage(MessageContent.getBytes());
			    message.setQos(iQos);
		
			    if ( hChat.topic != null) {
			    	client.publish(hChat.topic, message); // Blocking publish
			    	System.out.println("Message content is " + hChat.content);
			    }
			} catch (Exception ex) {
				ex.printStackTrace();
		}
	}
}

class  FileTransferThread extends HChatClient{
	
	FileTransferThread(String name)
	{
		super(name);
	}
		
    public void run(){
    	SendBinaryFile("D:/Bala.mp4");
    }

	public void SendBinaryFile(String fileNameWithPath)
	{
   		synchronized(hChat){
		try {

			    int iQos             = Integer.parseInt(hChat.qos);
				String broker       = "tcp://" + hChat.serverIP + ":" + hChat.port;
			    //String clientId     = "JavaSample1";
				String clientId     = hChat.clientId;
			      
		        MemoryPersistence dataStore = new MemoryPersistence();
		
			    // Construct the connection options object that contains connection parameters 
			    // such as cleanSession and LWT
			    MqttConnectOptions conOpt = new MqttConnectOptions();
			    conOpt.setCleanSession(true);
			  //  conOpt.setUserName(this.userName);	   
			   // conOpt.setPassword(this.password.toCharArray());
		
			    // Construct an MQTT blocking mode client
			    MqttClient client = new MqttClient(broker, clientId, dataStore);
		
			    // Connect to the MQTT server
			    client.connect(conOpt);
		
			    // Create and configure a message
			    byte[] result = null;
			    MqttMessage message = null;
			    InputStream input = null;
			    try {
				File file = new File(fileNameWithPath);
				int fileSize = (int)file.length();
				int totalBytesRead = 0;

				FileHeader header = new FileHeader();
				header.SetFileName(fileNameWithPath);
				header.SetFileSize(fileSize);
				byte[] headerByteArray = header.build();
				
				// Publish Header separately 
				message = new MqttMessage(headerByteArray);
				message.setQos(iQos);
				client.publish(hChat.topic, message);

				//result = new byte[(int)file.length()+ EOF.length];
				//result = new byte[(int)file.length()];
				
				result = new byte[1024*4];
				input = new BufferedInputStream(new FileInputStream(file));
				while(totalBytesRead < fileSize){
					System.out.println("TotalBytesRead:" + totalBytesRead);
					System.out.println("fileSize:" + fileSize);
					//input.read() returns -1, 0, or more :
					int bytesRead = input.read(result, 0, result.length); 
					if (bytesRead > 0){
						totalBytesRead = totalBytesRead + bytesRead;
						System.out.println("Bytes Read from File:" + fileNameWithPath +" is:" + bytesRead);
						
						byte[] partialResult = new byte[bytesRead];
						System.arraycopy(result, 
								0,
								partialResult,
				                 0,
				                 bytesRead);
						
						message = new MqttMessage(partialResult);
						message.setQos(iQos);
						
					    if ( hChat.topic != null) {
					    	client.publish(hChat.topic, message); // Blocking publish
					    	//System.out.println("Message content is " + publishObj.content);
					    }
					}
				    Thread.sleep(100);
				}
				/*
				// Add EOF to be published
			    int count = 0;
			    while(EOF.length < count) {
			    	result[totalBytesRead+count] = EOF[count++];
			    }
				*/
			    }
			    finally {
				System.out.println("Closing input stream.");
				input.close();
			    }
			}
			catch (FileNotFoundException ex) {
			   	System.out.println("File not found.");
			 }
			catch (IOException ex) {
			}
			catch (Exception ex) {
				ex.printStackTrace();
	        } 
		}
	}
}

class  ChatThread extends HChatClient{
	public boolean chatMessageFlag = false;
	public void EnableChat(boolean bEnableChat) {
		chatMessageFlag = bEnableChat;
	}
	ChatThread(String name)
	{
		super(name);
	}
		
    public void run(){
	    	
    	while(true){
            try{
                synchronized(hChat){
                	if(chatMessageFlag == true){
                		SendChatMessage();
                		chatMessageFlag = false;
                	}
                }
                Thread.sleep(500);
            } catch (InterruptedException iex) {
                System.out.println("Exception in thread: "+iex.getMessage());
                Thread.currentThread().interrupt();
            }
        }
    }
	    
    void SendChatMessage()
    {
    	try {
		    //String topic        = "test-topic";
		    //String content      = "Message from Subscribe1";
		    int iQos             = Integer.parseInt(hChat.qos);
		    // String broker       = "tcp://iot.eclipse.org:1883";
		    //String broker       = "tcp://192.168.1.134:1883";
			String broker       = "tcp://" + hChat.serverIP + ":" + hChat.port;
		    //String clientId     = "JavaSample1";
			String clientId     = hChat.clientId;
			      
		 //   MqttDefaultFilePersistence dataStore = new MqttDefaultFilePersistence(tmpDir); 
	        MemoryPersistence dataStore = new MemoryPersistence();
		
		    //String uri = System.getenv("CLOUDMQTT_URL");
		    // Construct the connection options object that contains connection parameters 
		    // such as cleanSession and LWT
		    MqttConnectOptions conOpt = new MqttConnectOptions();
		    conOpt.setCleanSession(true);
		  //  conOpt.setUserName(this.userName);	   
		   // conOpt.setPassword(this.password.toCharArray());
		
		    // Construct an MQTT blocking mode client
		    MqttClient client = new MqttClient(broker, clientId, dataStore);
		
		    // Connect to the MQTT server
		    client.connect(conOpt);
		
		    // Create and configure a message
		    String MessageContent = hChat.content;
			    
		    MqttMessage message = new MqttMessage(MessageContent.getBytes());
		    message.setQos(iQos);
		
		    if ( hChat.topic != null) {
		    	client.publish(hChat.topic, message); // Blocking publish
		    	System.out.println("Message content is " + hChat.content);
		    }
		} catch (Exception ex) {
			ex.printStackTrace();
		}
    }
}	
