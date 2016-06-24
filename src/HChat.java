import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class HChat {

	public String serverIP = "localhost";
	public String port = "1883";	
	public String clientId     = "HChatSample";
	public String qos ="1";
	public String topic = "Test";
	public String content = "Message from HChat";
	public String userName;
	public String password;
	
	HChat()
	{
		serverIP = new String("localhost");
		port = new String("1883");	
		clientId     = new String("HChatSample");
		qos = new String("1");
		topic = new String("Test");
		content = new String("Message from HChat");
		userName = new String("");
		password = new String("");		
	}
	
	HChat(String Ip, String portNum, String uName, String passwordStr)
	{
		serverIP = Ip;
		port = portNum;
		userName = uName;
		password = passwordStr;
		clientId     = new String("HChatSample");
		qos = new String("1");
		topic = new String("Test");
		content = new String("Message from HChat");		
	}
	
	@Override
    public String toString(){
        return "serverIP="+this.serverIP+"::port="+this.port+"::userName="+this.userName+"::password="+this.password;
    }

    public static HChat ReadConfigFile() throws IOException {
    	   
        String fileName = "D:/Work/hchat.config";
        Path path = Paths.get(fileName);
        Scanner scanner = new Scanner(path);
        scanner.useDelimiter(System.getProperty("line.separator"));
        //parse line to get Emp Object
        HChat hChat = parseCSVLine(scanner.next());
        System.out.println(hChat.toString());
        scanner.close();
        return hChat;
    }
     
    private static HChat parseCSVLine(String line) {
         Scanner scanner = new Scanner(line);
         scanner.useDelimiter("\\s*,\\s*");
         String Ip = scanner.next();
         String port = scanner.next();
         String username = scanner.next();
		 String password = scanner.next();
		 scanner.close();
		 //HChat hChat = new HChat();
         return new HChat(Ip, port, username, password);
    }
    
    byte[] TestHeader(String fileNameWithPath)
    {
    	try {
		File file = new File(fileNameWithPath);
		int fileSize = (int)file.length();
		FileHeader header = new FileHeader();
		header.SetFileName(fileNameWithPath);
		header.SetFileSize(fileSize);
		return header.build();
    	}
    	catch (IOException ex) {
    		ex.printStackTrace();
    	}
    	return null;
    }
    
    void ParseHeader(byte[] headerArray)
    {
      String FileName = "";
	  FileHeader header = new FileHeader();
	  if( header.Parse(headerArray) == true)
	  {
		  FileName = header.GetFileName();
		  System.out.println("FileName=" + FileName);
		  return;
	  }
    }
    
	 public static void main(String[] args) throws IOException{
		  HChat hChatObj = ReadConfigFile();
		  HChatClient publishHbObj = new HChatClient("HeartBeat Thread");
		  FileTransferThread publishFileTransferObj = new FileTransferThread("File Thread");
		  ChatThread publishChatObj = new ChatThread("Chat Thread");
		  HChatSubscribe subscribeChatObj = new HChatSubscribe();
		  
		  //subscribeChatObj.hChat.serverIP = "192.168.1.134";
		  subscribeChatObj.hChat.serverIP = hChatObj.serverIP;
		  subscribeChatObj.hChat.qos = "1";
		  subscribeChatObj.hChat.topic = "Subscribe";
		  subscribeChatObj.hChat.clientId = "HSubscribe";		  
		  subscribeChatObj.SubscribeMqtt();
		  //subscribeFileTransferObj.SubscribeMqtt();
		  
		  publishHbObj.hChat.serverIP = hChatObj.serverIP;
		  publishHbObj.hChat.topic = "HeartBeat";
		  publishHbObj.hChat.clientId = "HBt";
		  publishHbObj.hChat.qos = "0";
		  publishFileTransferObj.hChat.serverIP = hChatObj.serverIP;
		  publishFileTransferObj.hChat.topic = "File";
		  publishFileTransferObj.hChat.clientId = "FileTx";
		  publishFileTransferObj.hChat.qos = "1";
		  publishChatObj.hChat.serverIP = hChatObj.serverIP;
		  publishChatObj.hChat.topic = "Chat";
		  publishChatObj.hChat.clientId = "ChatTx";
		  publishChatObj.hChat.qos = "1";
		  //hbThreadObj.KeepAliveMqtt();
		  publishHbObj.start();
		  //fileThreadObj.start();
		  publishChatObj.EnableChat(true);
		  publishChatObj.start();
		  // threadObj.SendBinaryFile("D:/LargeFile.iso");
		  publishFileTransferObj.SendBinaryFile("D:/Bala.jpg");
		  
/*
		 HChat hChat = new HChat();
		 byte[] header = hChat.TestHeader("D:/Bala.jpg");
		 if( header != null)
		 {
			 hChat.ParseHeader(header);
		 }
*/		 
	  }
	
}

class FileHeader {
	private String BOF = "BOF->>>>";
	private byte[] FileName = new byte[32];
	private byte[] FileSize = new byte[4];
	
	public String GetFileName()
	{
		byte[] fileName = null;
		int i=0;
		
		for( i=0; i < 32; i++)
		{
			if (FileName[i] == 0)
				break;
		}
		
		if( i > 0 && i <= 32)
			fileName = new byte[i];
		
		System.arraycopy(FileName,
				0,
				fileName,
                 0,
                 i);
		
		return new String(fileName);
	}
	
	public int GetFileSize()
	{
		return fromByteArray(FileSize);
	}
	
	public void SetFileName(String Filename)
	{
		int filenameLen = 32;
		if(Filename.length() > 32)
			filenameLen = 32;
		else
			filenameLen = Filename.length();
		System.arraycopy(Filename.getBytes(),
				0,
				FileName,
                 0,
                 filenameLen);
	}
	
	public void SetFileSize(int size )
	{
		byte[] baFileSize = toByteArray(size);
		System.arraycopy(baFileSize, 
				0,
				FileSize,
                 0,
                 4);	
	}
	
/*	private byte[] intToBytes(int value) {
		return new byte[] {
				    (byte) ((value >>> 24) & 0xff),
				    (byte) ((value >>> 16) & 0xff),
				    (byte) ((value >>> 8) & 0xff),
				    (byte) (value & 0xff),
				  };
	}
*/	
	
	byte[] toByteArray(int value) {
	     return  ByteBuffer.allocate(4).putInt(value).array();
	}

	int fromByteArray(byte[] bytes) {
	     return ByteBuffer.wrap(bytes).getInt();
	}
	// packing an array of 4 bytes to an int, big endian
	
	byte[] build() throws IOException
	{
		ByteArrayOutputStream BOS = new ByteArrayOutputStream(BOF.length()+FileName.length+FileSize.length);
		BOS.write(BOF.getBytes());
		BOS.write(FileName);
		BOS.write(FileSize);
		return BOS.toByteArray();
	}
	
	Boolean Parse(byte[] header)
	{
		if(header.length == 44) {
		byte[] BOFHeader = new byte[8];
		System.arraycopy(header, 
				0,
				BOFHeader,
	            0,
	            8);
		String BOFStr = new String(BOFHeader);
		if( BOFStr.compareTo(BOF) != 0)
			return false;
		
		System.arraycopy(header, 
				8,
				 FileName,
                 0,
                 32);
		System.arraycopy(header , 
				40,
				FileSize,
                 0,
                 4);
		
		return true;
		}
		return false;
	}
} 