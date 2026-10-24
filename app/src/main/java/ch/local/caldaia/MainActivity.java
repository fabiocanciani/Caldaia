package ch.local.caldaia;
import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.widget.*;
import java.net.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
public class MainActivity extends Activity {
 TextView status; EditText ip, deviceId;
 void label(LinearLayout root,String s){TextView t=new TextView(this);t.setText(s);t.setTextSize(18);root.addView(t);}
 void button(LinearLayout root,String s,Runnable r){Button b=new Button(this);b.setText(s);root.addView(b);b.setOnClickListener(v->r.run());}
 public void onCreate(Bundle b){super.onCreate(b);LinearLayout root=new LinearLayout(this);root.setPadding(28,36,28,20);root.setOrientation(1);ScrollView sc=new ScrollView(this);sc.addView(root);setContentView(sc);
 label(root,"Caldaia — Sonoff THS");
 label(root,"IP Sonoff");ip=new EditText(this);ip.setSingleLine(true);ip.setText("192.168.10.154");root.addView(ip);
 label(root,"Device ID eWeLink");deviceId=new EditText(this);deviceId.setSingleLine(true);deviceId.setHint("Inserisci il Device ID");root.addView(deviceId);
 status=new TextView(this);status.setTextSize(16);status.setTextColor(Color.DKGRAY);status.setText("Pronto. I comandi LAN sono sperimentali.");root.addView(status);
 button(root,"VERIFICA CONNESSIONE",()->check());
 button(root,"ACCENDI",()->send("on"));
 button(root,"SPEGNI",()->send("off"));
 label(root,"Attenzione: il firmware 3.6.3 potrebbe non supportare il protocollo LAN. Un comando inviato non significa che la caldaia sia accesa o spenta. Verifica sul dispositivo.");
 }
 void show(String s){runOnUiThread(()->status.setText(s));}
 void check(){String host=ip.getText().toString().trim();new Thread(()->{try(Socket s=new Socket()){s.connect(new InetSocketAddress(host,8081),3500);show("Porta 8081 raggiungibile; comando LAN non confermato.");}catch(Exception e){show("Connessione non riuscita: "+e.getMessage());}}).start();}
 void send(String state){String host=ip.getText().toString().trim();String id=deviceId.getText().toString().trim();
 if(id.isEmpty()){show("Inserisci il Device ID eWeLink.");return;}
 new Thread(()->{HttpURLConnection c=null;try{
 c=(HttpURLConnection)new URL("http://"+host+":8081/zeroconf/switch").openConnection();
 c.setConnectTimeout(4000);c.setReadTimeout(5000);c.setRequestMethod("POST");c.setDoOutput(true);c.setRequestProperty("Content-Type","application/json");
 String payload="{\"deviceid\":\""+id.replace("\"","") +"\",\"data\":{\"switch\":\""+state+"\"}}";
 try(OutputStream o=c.getOutputStream()){o.write(payload.getBytes(StandardCharsets.UTF_8));}
 int code=c.getResponseCode();InputStream stream=code>=400?c.getErrorStream():c.getInputStream();
 String response="";if(stream!=null){ByteArrayOutputStream buff=new ByteArrayOutputStream();byte[] a=new byte[512];int n;while((n=stream.read(a))!=-1&&buff.size()<4096)buff.write(a,0,n);response=buff.toString("UTF-8");stream.close();}
 show("HTTP "+code+"\n"+response+"\nControlla fisicamente lo stato del relè.");
 }catch(Exception e){show("Comando non confermato: "+e.getMessage());}finally{if(c!=null)c.disconnect();}}).start();
 }
}