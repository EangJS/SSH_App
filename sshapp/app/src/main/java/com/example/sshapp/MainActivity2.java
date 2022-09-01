package com.example.sshapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import java.io.InputStream;

public class MainActivity2 extends AppCompatActivity {
    private TextView textView;
    String state;
    private Button WOL;
    private TextView response;
    private ImageView imageView;
    private EditText custom;
    private Button send;
    private TextView host;
    private Button VSC;
    private Button twsl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        twsl = (Button) findViewById(R.id.twsl);
        send = (Button) findViewById(R.id.send);
        custom = (EditText) findViewById(R.id.custom);
        imageView = (ImageView) findViewById(R.id.imageView);
        textView =  (TextView) findViewById(R.id.Status);
        response = (TextView) findViewById(R.id.Response);
        WOL = (Button) findViewById(R.id.WOL);
        state = (String) MainActivity.status.getText();
        host = (TextView) findViewById(R.id.host);
        VSC = (Button) findViewById(R.id.VSC);
        textView.setText(state);
        host.setText(MainActivity.current_host.getText().toString());
        Window window = this.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
// finally change the color
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.Transparent));

        if(state.equals("Connected")){
            imageView.setImageResource(android.R.drawable.presence_online);
        }
        else{
            imageView.setImageResource(android.R.drawable.presence_offline);
        }
        Vibrator vib = (Vibrator) getSystemService(this.VIBRATOR_SERVICE);

        send.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("StaticFieldLeak")
            @Override
            public void onClick(View v) {
                vib.vibrate(10);
                response.setText("");
                new AsyncTask<Integer, Void, Void>(){
                    @Override
                    protected Void doInBackground(Integer... params) {
                        try {
                            String cmd = custom.getText().toString();
                            if(MainActivity.current_host.getText().toString().equals("eugenerasp.tplinkdns.com")){
                                execute_cmd(cmd,22);
                                cmd = "";
                            }
                            else{
                                execute_cmd(cmd,10500);
                                cmd = "";
                            }
                            vib.vibrate(5);
                        } catch (Exception e) {
                            e.printStackTrace();
                            response.post(new Runnable() {

                                public void run() {
                                    response.setText("Error");
                                }
                            });
                        }
                        return null;
                    }
                }.execute(1);
            }
        });

        twsl.setOnClickListener(new View.OnClickListener() {
            @Override
            @SuppressLint("StaticFieldLeak")
            public void onClick(View v) {
                vib.vibrate(10);
                new AsyncTask<Integer, Void, Void>(){
                    @Override
                    protected Void doInBackground(Integer... params) {
                        try {
                            execute_cmd("wsl --terminate Ubuntu",22);
                            vib.vibrate(5);
                        } catch (Exception e) {
                            e.printStackTrace();
                            response.post(new Runnable() {

                                public void run() {
                                    response.setText("Error");
                                }
                            });
                        }
                        return null;
                    }
                }.execute(1);

            }
        });

        VSC.setOnClickListener(new View.OnClickListener() {
            @Override
            @SuppressLint("StaticFieldLeak")
            public void onClick(View v) {
                vib.vibrate(10);
                new AsyncTask<Integer, Void, Void>(){
                    @Override
                    protected Void doInBackground(Integer... params) {
                        try {
                            response.setText("Check http://192.168.68.108:3000/");
                            response.setClickable(true);
                            execute_cmd("./startremotevsc.ps1",22);
                            vib.vibrate(5);
                        } catch (Exception e) {
                            e.printStackTrace();
                            response.post(new Runnable() {

                                public void run() {
                                    response.setText("Error");
                                }
                            });
                        }
                        return null;
                    }
                }.execute(1);

            }
        });


        WOL.setOnClickListener(new View.OnClickListener() {
            @Override
            @SuppressLint("StaticFieldLeak")
            public void onClick(View v){
                vib.vibrate(10);
                new AsyncTask<Integer, Void, Void>(){
                    @Override
                    protected Void doInBackground(Integer... params) {
                        try {
                            execute_cmd("sudo etherwake 18:C0:4D:E5:16:1F",10500);
                            vib.vibrate(5);
                        } catch (Exception e) {
                            e.printStackTrace();
                            response.post(new Runnable() {

                                public void run() {
                                    response.setText("Error");
                                }
                            });
                        }
                        return null;
                    }
                }.execute(1);
            }
        });
    }

    public void execute_cmd(String command,int port) {
        try{

            Session session = MainActivity.session;
            Channel channel=session.openChannel("exec");
            ((ChannelExec)channel).setCommand(command);
            channel.setInputStream(null);
            ((ChannelExec)channel).setErrStream(System.err);
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
            if(!command.equals("./startremotevsc.ps1")){
                channel.disconnect();
            }
            //channel.disconnect();
            //session.disconnect();
            System.out.println("DONE");
            response.post(new Runnable() {
                public void run() {
                    response.setText(builder.toString());
                }
            });
        }catch(Exception e){
            System.out.println(e.toString());
            response.post(new Runnable() {
                public void run() {
                    response.setText(e.toString());
                }
            });
            e.printStackTrace();
        }

    }



}