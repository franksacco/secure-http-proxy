import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
 
public class Client
{
	public static void main(String argv[])
	{
		BufferedReader in = null;
		PrintStream out = null;
		Socket socket = null;
		String message;
		byte[] plaintext;
		String algo="AES";
		String mode="ECB"; // ECB, CBC, OFB, CFB, etc
		String padding="PKCS5Padding"; // PKCS5Padding or NoPadding				
		byte[] key=hexStringToBytes("11223344556677881122334455667788");
		byte[] iv=null;
		String algo_mode_padding=algo+'/'+mode+'/'+padding;
		try
		{
			// open a socket connection
			socket = new Socket("localhost", 4007);
			// Apre i canali I/O
			in = new BufferedReader(
					new InputStreamReader(socket.getInputStream()));
			out = new PrintStream(socket.getOutputStream(), true);
			// Legge dal server
		
			//inizializzo il cipher per decriptare i messaggi
			Cipher cipher=Cipher.getInstance(algo_mode_padding);
			SecretKey secret_key=new SecretKeySpec(key,algo);
			//IvParameterSpec iv_spec=(iv!=null)? new IvParameterSpec(iv) : null;
			cipher.init(Cipher.DECRYPT_MODE,secret_key);
			String criptato="";

			while ((message = in.readLine()) != null)
			{
				byte[] ciphertext=hexStringToBytes(message);
				plaintext=cipher.doFinal(ciphertext);
				System.out.println(bytesToHexString(plaintext));
				criptato=criptato+"\n"+message;
			}
			
			
			System.out.println("");
			System.out.println(criptato);
			
			out.close();
			in.close();
		}
		catch(Exception e) { System.out.println(e.getMessage());}
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