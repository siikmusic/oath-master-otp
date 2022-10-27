import net.link.oath.HOTP;
import spark.utils.IOUtils;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.util.*;
import java.util.List;

public class Client {
    static byte[] secret = {
            0x31, 0x32, 0x33, 0x34,
            0x35, 0x36, 0x37, 0x38,
            0x39, 0x30, 0x31, 0x32,
            0x33, 0x34, 0x35, 0x36,
            0x37, 0x38, 0x39, 0x30};

    public static void main(String[] args) throws IOException, URISyntaxException {
        int counter = 1;

        while(true){
            HOTP hotp = new HOTP(secret,6,false,-1,0);
            mailto(Collections.singletonList("siik@siiksounds.com"), "otp",
                    hotp.generate(counter));
            Scanner reader = new Scanner(System.in);  // Reading from System.in
            System.out.println("Enter the one time password ");
            int n = reader.nextInt(); // Scans the next token of the input as an int.
            URL url = new URL("http://147.175.182.155:4567/validate?otp="+n+"&counter="+counter);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("GET");
            connection.connect();


            int code = connection.getResponseCode();
            if(code == 200){
                InputStream in = connection.getInputStream();
                String body = IOUtils.toString(in);
                counter = Integer.parseInt(body);
                System.out.println("Authorized");
            } else{
                System.out.println("Wrong OTP password ");
            }
            System.out.println("Continue? Y/N ");
            String cont = reader.next(); // Scans the next token of the input as an int.
            if(Objects.equals(cont, "N")){
                reader.close();
                break;

            }

        }

    }


    public static void mailto(List<String> recipients, String subject,
                              String body) throws IOException, URISyntaxException {
        String uriStr = String.format("mailto:%s?subject=%s&body=%s",
                join(",", recipients), // use semicolon ";" for Outlook!
                urlEncode(subject),
                urlEncode(body));
        Desktop.getDesktop().browse(new URI(uriStr));
    }

    private static String urlEncode(String str) {
        try {
            return URLEncoder.encode(str, "UTF-8").replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public static String join(String sep, Iterable<?> objs) {
        StringBuilder sb = new StringBuilder();
        for(Object obj : objs) {
            if (sb.length() > 0) sb.append(sep);
            sb.append(obj);
        }
        return sb.toString();
    }


}
