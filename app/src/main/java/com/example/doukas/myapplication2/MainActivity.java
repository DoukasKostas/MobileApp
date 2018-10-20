package com.example.doukas.myapplication2;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * Created by Doukas on 10/3/2018.
 */

public class MainActivity extends AppCompatActivity {
    public static final String EXTRA_CIRCULAR_REVEAL_X = "EXTRA_CIRCULAR_REVEAL_X";
    public static final String EXTRA_CIRCULAR_REVEAL_Y = "EXTRA_CIRCULAR_REVEAL_Y";

    View rootLayout;

    private int revealX;
    private int revealY;
    private Button sw1;
    private Button sw2;
    private Button sw3;
    private Button sw4;
    private TextView[] stat=new TextView[4];
    private int port = 8888;
    private Socket client;
    private PrintWriter printWriter;
    private BufferedReader in;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);


        sw1=findViewById(R.id.switch1);
        sw2=findViewById(R.id.switch2);
        sw3=findViewById(R.id.switch3);
        sw4=findViewById(R.id.switch4);
        stat[0]=findViewById(R.id.status1);
        stat[1]=findViewById(R.id.status2);
        stat[2]=findViewById(R.id.status3);
        stat[3]=findViewById(R.id.status4);



        new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());//get prefs
                    String myIp = prefs.getString("MYIPADDRESS", "defaultStringIfNothingFound");
                    client=new Socket();
                    SocketAddress server = new InetSocketAddress(myIp,port);
                    client.connect(server);

                    in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                    System.out.println("Message Received: " + in.readLine());
                    changeStatus(stat,in.readLine());
                    printWriter = new PrintWriter(client.getOutputStream(),true);
                    sw1.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            /*new Thread(new Runnable() {
                                public void run() {
                                    printWriter.print("Switch:1");
                                    printWriter.flush();
                                    String message = null;
                                    try {
                                        message = in.readLine();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    System.out.println("Message Received: " + message);

                                }
                            }).start();*/
                            //btnThread btn1=new btnThread();
                            //btn1.start();
                            btnPress btn1= new btnPress();
                            ExecutorService ex = Executors.newSingleThreadExecutor();
                            btn1.executeOnExecutor(ex,"1");
                            ex.shutdown();
                            //changeStatus(stat1);
                        }
                    });
                    sw2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            /*new Thread(new Runnable() {
                                public void run() {
                                    printWriter.print("Switch:2");
                                    printWriter.flush();
                                    try {
                                        System.out.println(in.readLine());
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }

                                }
                            }).start();*/
                            btnPress btn2= new btnPress();
                            ExecutorService ex = Executors.newSingleThreadExecutor();
                            btn2.executeOnExecutor(ex,"2");
                            ex.shutdown();
                         //   changeStatus(stat2);
                        }
                    });
                    sw3.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            /*new Thread(new Runnable() {
                                public void run() {
                                    printWriter.print("Switch:3");
                                    printWriter.flush();
                                    try {
                                        System.out.println(in.readLine());
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }

                                }

                            }).start();*/
                            btnPress btn3= new btnPress();
                            ExecutorService ex = Executors.newSingleThreadExecutor();
                            btn3.executeOnExecutor(ex,"3");
                            ex.shutdown();
                           // changeStatus(stat3);
                        }
                    });
                    sw4.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            /*new Thread(new Runnable() {
                                public void run() {
                                    printWriter.print("Switch:4");
                                    printWriter.flush();
                                    try {
                                        System.out.println(in.readLine());
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }

                                }
                            }).start();*/
                            btnPress btn4= new btnPress();
                            ExecutorService ex = Executors.newSingleThreadExecutor();
                            btn4.executeOnExecutor(ex,"4");
                            ex.shutdown();
                            //changeStatus(stat4);
                        }
                    });
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            while (client.isConnected()) {
                                try {
                                    final String s=in.readLine();
                                    System.out.println("printed in continious thread"+s);
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            changeStatus(stat, s);
                                        }
                                    });

                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    },"RefreshThread").start();
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
        },"ConnThread").start();



        final Intent intent = getIntent();

        rootLayout = findViewById(R.id.mainactivity);


        rootLayout.setVisibility(View.INVISIBLE);

        revealX = intent.getIntExtra(EXTRA_CIRCULAR_REVEAL_X, 0);
        revealY = intent.getIntExtra(EXTRA_CIRCULAR_REVEAL_Y, 0);


        ViewTreeObserver viewTreeObserver = rootLayout.getViewTreeObserver();
        if (viewTreeObserver.isAlive()) {
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    revealActivity(revealX, revealY);
                    rootLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            });
        }
    }


    private void changeStatus(TextView[] v,String m){
        char[] mArray = m.toCharArray();
        for (int i=0;i<v.length;i++){
            if(mArray[i] == '0'){
                v[i].setBackgroundColor(Color.parseColor("#F44336"));
                v[i].setText("Off");
            }
            else{
                v[i].setBackgroundColor(Color.parseColor("#4CAF50"));
                v[i].setText("On");
            }
        }

    }

    protected void revealActivity(int x, int y) {

        float finalRadius = (float) (Math.max(rootLayout.getWidth(), rootLayout.getHeight()) * 1.1);

        // create the animator for this view (the start radius is zero)
        Animator circularReveal = ViewAnimationUtils.createCircularReveal(rootLayout, x, y, 0, finalRadius);
        circularReveal.setDuration(400);
        circularReveal.setInterpolator(new AccelerateInterpolator());

        // make the view visible and start the animation
        rootLayout.setVisibility(View.VISIBLE);
        circularReveal.start();

    }


    protected void unRevealActivity() {
        float finalRadius = (float) (Math.max(rootLayout.getWidth(), rootLayout.getHeight()) * 1.1);
        Animator circularReveal = ViewAnimationUtils.createCircularReveal(
                rootLayout, revealX, revealY, finalRadius, 0);

        circularReveal.setDuration(400);
        circularReveal.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                rootLayout.setVisibility(View.INVISIBLE);
                finish();
            }
        });


        circularReveal.start();
    }

    public class btnPress extends AsyncTask <String,Void,String>{

        @Override
        protected void onPreExecute() {

            //super.onPreExecute();

        }

        @Override
        protected String doInBackground(String... args) {
            printWriter.print("Switch:"+args[0]);
            printWriter.flush();
            String message = null;
            try {
                in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                message = in.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return message;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            System.out.println("Message Received: " + s);
            changeStatus(stat,s);
            try {
                super.finalize();
                this.cancel(true);

            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }
    }


}
