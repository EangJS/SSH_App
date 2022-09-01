package com.example.sshapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    TextView textViews;
    Button button;
    Button power;
    Button toggle;
    public static TextView current_host;
    Button poweroff;
    public static TextView status;
    public static ImageView stat_img;
    Button connect;
    public static Session session;
    Button disconnect;
    ImageView more;
    int switcher = 0;
    public static ArrayList<String>hosts = new ArrayList<String>(
            Arrays.asList("192.168.68.108","eugenerasp.tplinkdns.com")
    );
    public static ArrayList<String>passwords = new ArrayList<String>(
            Arrays.asList("eangJS21","(*l{p]:,k&(x*$F<+c{J{[)cIHF[LxyKW]p*55wkMS;US9{W3:6!,tZZt[VYI&A")
    );
    public static ArrayList<String>users = new ArrayList<String>(
            Arrays.asList("eugen","pi")
    );
    public static ArrayList<Integer>ports = new ArrayList<Integer>(
            Arrays.asList(22,10500)
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        power = (Button) findViewById(R.id.power);
        button = (Button) findViewById(R.id.button);
        poweroff = (Button) findViewById(R.id.poweroff);
        textViews = (TextView) findViewById(R.id.Response);
        current_host = (TextView) findViewById(R.id.current_host);
        textViews.setMovementMethod(new ScrollingMovementMethod());
        toggle = (Button) findViewById(R.id.toggle);
        status = (TextView) findViewById(R.id.Status);
        current_host.setText("Host "+ hosts.get(switcher));
        stat_img = (ImageView) findViewById(R.id.imageView);
        connect = (Button) findViewById(R.id.connect);
        disconnect = (Button) findViewById(R.id.disconnect);
        more = (ImageView) findViewById(R.id.more);


        //TinyDB tinyDB = new TinyDB(this);
       // tinyDB.putListString("sample",users);

        Window window = this.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
// finally change the color
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.Transparent));
        Vibrator vibe = (Vibrator) getSystemService(this.VIBRATOR_SERVICE);

        more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openActivity2();
            }
        });

        power.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vibe.vibrate(10);
                ExecutorService executor2 = Executors.newSingleThreadExecutor();
                executor2.execute(new Runnable() {
                    @Override
                    public void run() {
                        try{
                            sendMagic();
                            textViews.setText("Magic Packet Sent to EugeneZen");
                        }
                        catch(Exception e){
                            e.printStackTrace();
                            textViews.setText(e.toString());
                        }
                    }
                });
            }
        });

        toggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vibe.vibrate(10);
                textViews.setText("");
                if(switcher == hosts.size()-1) {
                    switcher = 0;
                }
                else{
                    switcher++;
                }
                current_host.setText(hosts.get(switcher));
            }
        });
        poweroff.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                vibe.vibrate(10);
                ExecutorService executor = Executors.newSingleThreadExecutor();
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        try{
                            if(users.get(switcher).equals("pi")){
                                //textViews.setText(users.get(switcher));
                                executeSSHcommand("sudo poweroff",session);
                            }
                            else{
                                executeSSHcommand("shutdown /s /f /t 5", session);

                            }
                            session.disconnect();
                            stat_img.setImageResource(android.R.drawable.presence_offline);
                            status.setText("Disconnected");
                            session = null;

                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                });

            }
        });

        disconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vibe.vibrate(10);
                if(session != null) {
                    session.disconnect();
                    stat_img.setImageResource(android.R.drawable.presence_offline);
                    status.setText("Disconnected");
                    textViews.setText("");
                    session = null;
                }
            }
        });

        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vibe.vibrate(10);
                if(session != null){
                    session.disconnect();
                    session = null;
                }
                session = connect(vibe);
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                vibe.vibrate(10);
                ExecutorService executor2 = Executors.newSingleThreadExecutor();
                executor2.execute(new Runnable() {
                    @Override
                    public void run() {
                        try{
                            //System.out.println(executeSSHcommand("ls"));
                            //textViews.setText(executeSSHcommand("ls"));
                            executeSSHcommand("ls",session);
                            vibe.vibrate(5);
                        }
                        catch(Exception e){
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    public void openActivity2(){
        Intent intent = new Intent(this,MainActivity2.class);
        startActivity(intent);
    }

    public Session connect(Vibrator vv){
        String host = hosts.get(switcher);
        String user = users.get(switcher);
        String password = passwords.get(switcher);
        int port = ports.get(switcher);
        //textViews.post(new Runnable() {
        //    public void run() {
        //        textViews.setText("Connecting to host...");
        //    }
        //});
        //textViews.setText("Connecting...");
        ExecutorService executor3 = Executors.newSingleThreadExecutor();
        executor3.execute(new Runnable() {
                              @Override
                              public void run() {
                                  try {

                                      java.util.Properties config = new java.util.Properties();
                                      config.put("StrictHostKeyChecking", "no");
                                      JSch jsch = new JSch();
                                      session = jsch.getSession(user, host, port);
                                      session.setPassword(password);
                                      session.setConfig(config);
                                      status.post(new Runnable() {
                                          public void run() {
                                              status.setText("Connecting...");
                                          }
                                      });
                                      //  showCountTextView.setText("Start");

                                      session.connect();
                                      vv.vibrate(5);

                                      stat_img.post(new Runnable() {
                                                        public void run() {
                                                            stat_img.setImageResource(android.R.drawable.presence_online);
                                                        }
                                                    });

                                      //status.setText("Connected");
                                      status.post(new Runnable() {
                                          public void run() {
                                              status.setText("Connected");
                                          }
                                      });

                                  }
                                  catch(JSchException e){
                                      e.printStackTrace();
                                      if (isAuthenticationFailure(e)) {
                                          status.post(new Runnable() {
                                              public void run() {
                                                  status.setText("Auth Fail");
                                              }
                                          });

                                      } else if (e.getMessage().equals("java.net.NoRouteToHostException: Host unreachable")) {
                                          status.post(new Runnable() {
                                              public void run() {
                                                  status.setText("Host Unreachable");
                                              }
                                          });
                                      }
                                  }


                              }
                          });
        return session;


    }
    private boolean isAuthenticationFailure(JSchException ee){
        return ee.getMessage().equals("Auth fail"); //$NON-NLS-1$
    }

    public String executeSSHcommand(String command,Session session) {
        String host = hosts.get(switcher);
        String user = users.get(switcher);
        String password = passwords.get(switcher);
        int port = ports.get(switcher);
        //textViews.post(new Runnable() {
            //    public void run() {
                //        textViews.setText("Connecting to host...");
                //    }
            //});
        try {



            Channel channel = session.openChannel("exec");
            ((ChannelExec) channel).setCommand(command);
            channel.setInputStream(null);
            ((ChannelExec) channel).setErrStream(System.err);

            InputStream in = channel.getInputStream();
            channel.connect();
            byte[] tmp = new byte[1024];
            StringBuilder builder = new StringBuilder();
            while (true) {
                while (in.available() > 0) {
                    int i = in.read(tmp, 0, 1024);
                    if (i < 0) break;
                    System.out.print(new String(tmp, 0, i));
                    builder.append(new String(tmp,0,i));
                    //textView.setText(new String(tmp, 0, i));

                }
                if (channel.isClosed()) {
                    System.out.println("exit-status: " + channel.getExitStatus());
                    break;
                }
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                }
            }
            channel.disconnect();
            //session.disconnect();
            //status.post(new Runnable() {
            //    public void run() {
            //        status.setText("Disconnected");
            //    }
            //});

            //status.setText("Disconnected");
            //System.out.println("DONE");
            //textView.setText(builder.toString());
            textViews.post(new Runnable() {
                public void run() {
                    textViews.setText(builder.toString());
                }
            });
            return builder.toString();
        } catch (Exception e) {
            e.printStackTrace();
            //textView.setText(e.toString());
            return e.toString();
        }
    }

    private void sendMagic(){
        int PORT = 9;

        String ipStr = "192.168.68.255";
        String macStr = "18:C0:4D:E5:16:1F";
        try {
            byte[] macBytes = getMacBytes(macStr);
            byte[] bytes = new byte[6 + 16 * macBytes.length];
            for (int i = 0; i < 6; i++) {
                bytes[i] = (byte) 0xff;
            }
            for (int i = 6; i < bytes.length; i += macBytes.length) {
                System.arraycopy(macBytes, 0, bytes, i, macBytes.length);
            }

            InetAddress address = InetAddress.getByName(ipStr);
            DatagramPacket packet = new DatagramPacket(bytes, bytes.length, address, PORT);

            try (DatagramSocket socket = new DatagramSocket()) {
                socket.send(packet);
            }catch (Exception e){
                System.out.println(e.toString());
            }

            System.out.println("Wake-on-LAN packet sent.");
        }
        catch (Exception e) {
            System.out.println("Failed to send Wake-on-LAN packet: " + e);
            System.exit(1);
        }

    }

    public static byte[] getMacBytes(String macStr) throws IllegalArgumentException {
        byte[] bytes = new byte[6];
        String[] hex = macStr.split("(\\:|\\-)");
        if (hex.length != 6) {
            throw new IllegalArgumentException("Invalid MAC address.");
        }
        try {
            for (int i = 0; i < 6; i++) {
                bytes[i] = (byte) Integer.parseInt(hex[i], 16);
            }
        }
        catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid hex digit in MAC address.");
        }
        return bytes;
    }




}