import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class ServerHttp {

	public static void main(String argv[]) throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException  
	{
		BufferedReader in = null;
		PrintStream out = null;
		Socket socket = null;
		String message;
		byte[] plaintext;
		String algo="AES";
		String mode="ECB"; // ECB, CBC, OFB, CFB, etc
		String padding="PKCS5Padding"; // PKCS5Padding or NoPadding		
		String htmlmessage="";
		byte[] key=hexStringToBytes("11223344556677881122334455667788");
		byte[] iv=null;
		String algo_mode_padding=algo+'/'+mode+'/'+padding;
		ServerSocket server = new ServerSocket(8080); 
		System.out.println("Listening for connection on port 8080 ...."); 
		while (true) 
		{ 
			try (Socket socketHttp = server.accept()) 
			{ 
				socket = new Socket("localhost", 4000);
				in = new BufferedReader(
						new InputStreamReader(socket.getInputStream()));
				out = new PrintStream(socket.getOutputStream(), true);
				
				//inizializzo il cipher per decriptare i messaggi
				Cipher cipher=Cipher.getInstance(algo_mode_padding);
				SecretKey secret_key=new SecretKeySpec(key,algo);
				cipher.init(Cipher.DECRYPT_MODE,secret_key);
				
				//leggo dal server l'html criptato
				while ((message = in.readLine()) != null)
				{
					byte[] ciphertext=hexStringToBytes(message);
					plaintext=cipher.doFinal(ciphertext);
					htmlmessage = new String(plaintext);
					System.out.println(htmlmessage);
				}
			
				
				out.close();
				in.close();
				
				String httpResponse = "HTTP/1.1 200 OK\r\n\r\n" + htmlmessage; 
				socketHttp.getOutputStream().write(httpResponse.getBytes("UTF-8")); 
			}  
		} 
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
