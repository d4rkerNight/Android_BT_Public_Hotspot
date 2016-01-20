package in.linuxlog.btpublichotspot;

import android.util.Base64;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;

import static in.linuxlog.btpublichotspot.BTVariables.Vars;

public class BTSession extends MainActivity{

  public Integer checkConn(String[] params){

    Integer res = 0;
    URL url;
    SSLContext sc;
    Integer responseCode;
    InputStream inputStream;
    HttpsURLConnection conn = null;

    try{
      // TODO
      String urlParams =
        "username=" + URLEncoder.encode(params[0], "UTF-8") +
        "&password=" + URLEncoder.encode(params[1], "UTF-8");
      // Create SSL Connection
      sc = SSLContext.getInstance("TLS");
      sc.init(null, null, new java.security.SecureRandom());
      String userpass = params[0] + ":" + params[1];
      String basicAuth = "Basic " + Base64.encodeToString(userpass.getBytes(), Base64.DEFAULT);
      // Create Connection
      url = new URL("https://www.btopenzone.com:8443/tbbLogon");
      conn = (HttpsURLConnection) url.openConnection();
      conn.setReadTimeout(10000);
      conn.setConnectTimeout(15000);
      conn.setSSLSocketFactory(sc.getSocketFactory());
      conn.setRequestProperty("Authorization", basicAuth);
      conn.setRequestMethod("POST");
      conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
      conn.setRequestProperty("Content-Language", "en-US");
      conn.setRequestProperty("Content-Length", "" +
        Integer.toString(urlParams.getBytes().length));
      conn.setUseCaches(false);
      conn.setDoInput(true);
      conn.setDoOutput(false);
      //Send Request
      DataOutputStream dataOutStream = new DataOutputStream(conn.getOutputStream());
      dataOutStream.writeBytes(urlParams);
      dataOutStream.flush();
      dataOutStream.close();
      // Get Response
      responseCode = conn.getResponseCode();
      if(responseCode >= 400 && responseCode <= 499){
        // TODO error 400 404
        inputStream = conn.getErrorStream();
      }
      else{
        inputStream = conn.getInputStream();
      }
      BufferedReader rd = new BufferedReader(new InputStreamReader(inputStream));
      String line;
      StringBuilder response = new StringBuilder();
      while((line = rd.readLine()) != null){
        response.append(line);
        response.append('\r');
      }
      rd.close();
      String result = response.toString();
      // Check response
      if(result.indexOf("Please check you have entered your Username/Password correctly") > 0){
        switch(params[2]){
          case "chk":
            res = 0; // display nothing
            break;
          case "empty":
            res = 1; // display login error
            break;
        }
      }else if(result.indexOf("Logged in with BT Wi-fi") > 0){
        switch(params[2]){
          case "chk":
            res = 3; // display already logged in // TODO change back to 3
            break;
          case "empty":
            res = 4; // display saved session
            break;
          case "sess":
            res = 2; // display logged in
            break;
        }
      }
    }catch(IOException | NoSuchAlgorithmException | KeyManagementException e){
      e.printStackTrace();
      Vars.stackTrace = Log.getStackTraceString(e);
      res = 5;
    }finally {
      // Close connection
      if(conn != null){
        conn.disconnect();
      }
    }
    return res;
  }
}
