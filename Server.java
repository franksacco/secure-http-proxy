import java.io.*;
import java.net.*;
import java.util.*;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
 
public class Server extends Thread
{
	private ServerSocket Server;
	public static void main(String argv[]) throws Exception
	{
		new Server();
	}
	public Server() throws Exception
	{
		Server = new ServerSocket(4007);
		System.out.println("Il Server è in attesa sulla porta 4002.");
		this.start();
	}
	
	public void run()
	{
		while(true)
		{
			try {
				System.out.println("In attesa di Connessione.");
				Socket client = Server.accept();
				System.out.println("Connessione accettata da: "+client.getInetAddress());
				Connect c = new Connect(client);
				}
			catch(Exception e) {}
		}
	}
}
	 
class Connect extends Thread
{
	private Socket client = null;
	BufferedReader in = null;
	PrintStream out = null;
	byte[] plaintext;
	String algo="AES";
	String mode="ECB"; // ECB, CBC, OFB, CFB, etc
	String padding="PKCS5Padding"; // PKCS5Padding or NoPadding				
	byte[] key=hexStringToBytes("11223344556677881122334455667788");
	byte[] iv=null;
	String algo_mode_padding=algo+'/'+mode+'/'+padding;
	
	public Connect() {}
	
	public Connect(Socket clientSocket)
	{
		client = clientSocket;
		try
		{
			in = new BufferedReader(
					new InputStreamReader(client.getInputStream()));
			out = new PrintStream(client.getOutputStream(), true);
		}
		catch(Exception e1)
		{
			try 
			{ 
				client.close(); 
			}
			catch(Exception e) 
			{ 
				System.out.println(e.getMessage());
			}
			return;
		}
	
		this.start();
	}
	
	public void run()
	{
		try
		{
			URL yahoo = new URL("http://localhost/networksecurity/home.htm");
	        URLConnection yc = yahoo.openConnection();
	        BufferedReader input = new BufferedReader(
	                                new InputStreamReader(
	                                yc.getInputStream()));
	        String text="", inputLine;
	        
	        Cipher cipher=Cipher.getInstance(algo_mode_padding);
			SecretKey secret_key=new SecretKeySpec(key,algo);
			//IvParameterSpec iv_spec=(iv!=null)? new IvParameterSpec(iv) : null;
			cipher.init(Cipher.ENCRYPT_MODE,secret_key);

	        while ((inputLine = input.readLine()) != null) 
	            //System.out.println(inputLine);
	        		//message=message+inputLine;
	        {
	        		text += inputLine;	
	        }
	        
	        byte[] ciphertext = cipher.doFinal(text.getBytes());
    			out.println(bytesToHexString(ciphertext));
	        out.flush();
	        input.close();
	        System.out.println(text);
	        System.out.println(bytesToHexString(ciphertext));
			//out.println("Generico messaggio per il Client");
			//out.flush();
	// chiude gli stream e le connessioni
			out.close();
			in.close();
			client.close();
		}
		catch(Exception e) {}
	}
	
	public static String bytesToHexString(byte[] buf) {
		StringBuffer sb=new StringBuffer();
		for (int i=0; i<buf.length; i++) sb.append(Integer.toHexString((buf[i]>>4)&0x0f)).append(Integer.toHexString(buf[i]&0x0f));
		return sb.toString();
	}
	
	public static byte[] hexStringToBytes(String str) {
		byte[] buf=new byte[str.length()/2];
		for (int i=0; i<buf.length; i++) buf[i]=(byte)Integer.parseInt(str.substring(i*2,i*2+2),16);
		return buf;
	}
	
}