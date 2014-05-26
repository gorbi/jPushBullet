package com.gorbi;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URL;
import java.util.Iterator;

public final class PushBullet {

    private HttpsURLConnection httpsURLConnection = null;

    private String apiKey = null, deviceNick = null, deviceIden = null;

    public void loadConfig(String configFile) throws IOException, ParseException {

        BufferedReader bufferedReader = new BufferedReader(new FileReader(configFile));
        String string;
        while ((string = bufferedReader.readLine()) != null) {
            if (!string.startsWith("#")) {
                if (string.contains("apiKey")) {
                    apiKey = string.split("=\"")[1].split("\"")[0];
                } else if (string.contains("deviceIden")) {
                    deviceIden = string.split("=\"")[1].split("\"")[0];
                } else if (string.contains("deviceNick")) {
                    deviceNick = string.split("=\"")[1].split("\"")[0];
                }
                if (apiKey != null && deviceIden != null) {
                    return;
                }
            }
        }
        bufferedReader.close();

        httpsURLConnection = (HttpsURLConnection) new URL("https://api.pushbullet.com/v2/devices").openConnection();

        httpsURLConnection.setRequestMethod("GET");
        httpsURLConnection.setRequestProperty("Authorization", "Basic " + new String(Base64.encodeBase64((apiKey+":").getBytes())));

        string = new BufferedReader(new InputStreamReader(httpsURLConnection.getInputStream())).readLine();
        httpsURLConnection.disconnect();

        JSONObject responseJSON = (JSONObject) new JSONParser().parse(string);

        JSONArray devicesArray = (JSONArray) responseJSON.get("devices");
        Iterator<JSONObject> iterator = (Iterator<JSONObject>) devicesArray.iterator();
        while(iterator.hasNext()) {
            JSONObject deviceCurrent = iterator.next();
            if(deviceCurrent.get("nickname").toString().contains(deviceNick)) {
                deviceIden = deviceCurrent.get("iden").toString();
                return;
            }
        }
    }

    private void push(String param) throws IOException{
        httpsURLConnection = (HttpsURLConnection) new URL("https://api.pushbullet.com/api/pushes").openConnection();

        httpsURLConnection.setRequestMethod("POST");
        httpsURLConnection.setRequestProperty("Authorization", "Basic " + new String(Base64.encodeBase64((apiKey + ":").getBytes())));

        httpsURLConnection.setDoOutput(true);
        DataOutputStream dataOutputStream = new DataOutputStream(httpsURLConnection.getOutputStream());
        dataOutputStream.writeBytes(param);
        dataOutputStream.close();

        httpsURLConnection.getInputStream();
        httpsURLConnection.disconnect();
    }

    public void pushNote(String title, String body) throws IOException{
        push("device_iden="+deviceIden+"&type=note&title="+title+"&body="+body);
    }

    public void pushLink(String title, String url) throws IOException{
        push("device_iden="+deviceIden+"&type=link&title="+title+"&url="+url);
    }

    public void pushLink(String title, String url, String body) throws IOException{
        push("device_iden="+deviceIden+"&type=link&title="+title+"&url="+url+"&body="+body);
    }

    public void pushAddress(String name, String address) throws IOException{
        push("device_iden="+deviceIden+"&type=address&name="+name+"&address="+address);
    }

    public void pushList(String title, String items[]) throws IOException{
        StringBuilder stringBuilder = new StringBuilder("device_iden="+deviceIden+"&type=list&title="+title+"&items="+items[0]);
        for(int i=1;i<items.length;i++)
            stringBuilder.append("&items=").append(items[i]);
        push(stringBuilder.toString());
    }

    public void pushFile(File file) throws IOException{

        if (file.length() >= 26214400)
            return;

        CredentialsProvider credsProvider = new BasicCredentialsProvider();

        CloseableHttpClient closeableHttpClient = HttpClients.custom().setDefaultCredentialsProvider(credsProvider).build();
        credsProvider.setCredentials(new AuthScope("api.pushbullet.com", 443), new UsernamePasswordCredentials(apiKey, null));

        HttpPost httpPost = new HttpPost("https://api.pushbullet.com/api/pushes");

        MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
        multipartEntityBuilder.addBinaryBody("file", file);
        multipartEntityBuilder.addTextBody("device_iden", deviceIden);
        multipartEntityBuilder.addTextBody("type", "file");
        httpPost.setEntity(multipartEntityBuilder.build());

        closeableHttpClient.execute(httpPost);

    }

}