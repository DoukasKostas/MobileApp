package com.example.doukas.myapplication2;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;


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
    private TextView stat1;
    private TextView stat2;
    private TextView stat3;
    private TextView stat4;
    private int port = 8888;
    private Socket client;
    private PrintWriter printWriter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);


        sw1=findViewById(R.id.switch1);
        sw2=findViewById(R.id.switch2);
        stat1=findViewById(R.id.status1);
        stat2=findViewById(R.id.status2);
        sw3=findViewById(R.id.switch3);
        sw3=findViewById(R.id.switch3);
        stat4=findViewById(R.id.status4);
        stat4=findViewById(R.id.status4);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    client=new Socket();
                    SocketAddress server = new InetSocketAddress("192.168.1.120",port);
                    client.connect(server);
                    printWriter = new PrintWriter(client.getOutputStream(),true);
                    sw1.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            new Thread(new Runnable() {
                                public void run() {
                                    printWriter.println("1");
                                    printWriter.flush();
                                }
                            }).start();

                            changeStatus(stat1);
                        }
                    });
                    sw2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            new Thread(new Runnable() {
                                public void run() {
                                    printWriter.println("2");
                                    printWriter.flush();
                                }
                            }).start();
                            changeStatus(stat2);
                        }
                    });
                    sw3.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            new Thread(new Runnable() {
                                public void run() {
                                    printWriter.println("3");
                                    printWriter.flush();
                                }
                            }).start();
                            changeStatus(stat3);
                        }
                    });
                    sw4.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            new Thread(new Runnable() {
                                public void run() {
                                    printWriter.println("4");
                                    printWriter.flush();
                                }
                            }).start();
                            changeStatus(stat4);
                        }
                    });
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();



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


    private void changeStatus(TextView v){
        String status=v.getText().toString();
        if(status.equals("On")){
            v.setBackgroundColor(Color.parseColor("#F44336"));
            v.setText("Off");
        }
        else{
            v.setBackgroundColor(Color.parseColor("#4CAF50"));
            v.setText("On");
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
}
