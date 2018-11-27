package com.example.doukas.myapplication2;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.animation.AccelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
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
    private Button[] sw=new Button[4];
    private ImageView[] priza = new ImageView[4];
    private ImageView[] keraynos = new ImageView[4];
//    private Button sw1;
//    private Button sw2;
//    private Button sw3;
//    private Button sw4;
    private TextView[] stat=new TextView[4];
    private int port = 8888;
    private Socket client;
    private PrintWriter printWriter;
    private BufferedReader in;
    private Dialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity_new);


        sw[0]=findViewById(R.id.switch1);
        sw[1]=findViewById(R.id.switch2);
        sw[2]=findViewById(R.id.switch3);
        sw[3]=findViewById(R.id.switch4);
        priza[0]=findViewById(R.id.priza1);
        priza[1]=findViewById(R.id.priza2);
        priza[2]=findViewById(R.id.priza3);
        priza[3]=findViewById(R.id.priza4);
        keraynos[0]=findViewById(R.id.k1);
        keraynos[1]=findViewById(R.id.k2);
        keraynos[2]=findViewById(R.id.k3);
        keraynos[3]=findViewById(R.id.k4);
        sw[0].setOnClickListener(new btnListener("1"));
        sw[1].setOnClickListener(new btnListener("2"));
        sw[2].setOnClickListener(new btnListener("3"));
        sw[3].setOnClickListener(new btnListener("4"));
//        sw1=findViewById(R.id.switch1);
//        sw2=findViewById(R.id.switch2);
//        sw3=findViewById(R.id.switch3);
//        sw4=findViewById(R.id.switch4);
//        stat[0]=findViewById(R.id.status1);
//        stat[1]=findViewById(R.id.status2);
//        stat[2]=findViewById(R.id.status3);
//        stat[3]=findViewById(R.id.status4);
//        sw1.setOnClickListener(new btnListener("1"));
//        sw2.setOnClickListener(new btnListener("2"));
//        sw3.setOnClickListener(new btnListener("3"));
//        sw4.setOnClickListener(new btnListener("4"));



        new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());//get prefs
                    String myIp = prefs.getString("MYIPADDRESS", "defaultStringIfNothingFound");
                    String pass =  prefs.getString("PASS","defaultStringIfNothingFound");
                    if (pass.equals("")) pass="null";
                    client=new Socket(myIp,port);
//                    SocketAddress server = new InetSocketAddress(myIp,port);
//
//                    client.connect(server);

                    in = new BufferedReader(new InputStreamReader(client.getInputStream()));
//                    System.out.println("Message Received: " + in.readLine());
//                    changeStatus(stat,in.readLine());
//                    printWriter = new PrintWriter(client.getOutputStream(),true);
                    printWriter = new PrintWriter(client.getOutputStream(),true);
                    printWriter.print(pass);
                    printWriter.flush();
                    String s0 = in.readLine();
                    if(!s0.equals("Connected")){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this, "Wrong Password! Try again.",
                                        Toast.LENGTH_LONG).show();

                            }
                        });
                        client.close();
                        MainActivity.super.finish();
                    }

                    while (client.isConnected() && !client.isClosed() && isNetworkConnected()) {


                            try {

                                final String s = in.readLine();


                                if (s!=null && s.length() == 4) {

                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            changeStatus(sw, priza, keraynos, s);
                                            if(pd!=null){
                                                pd.hide();
                                                pd.dismiss();
                                            }
                                        }
                                    });
                                } else if (s.equals("")){
                                    Toast.makeText(MainActivity.this, "Not Iniatialized properly. Try reconnecting.",
                                            Toast.LENGTH_LONG).show();
                                }
                                else{
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(MainActivity.this, s,
                                                    Toast.LENGTH_LONG).show();
                                            if(pd!=null){
                                                pd.hide();
                                                pd.dismiss();
                                            }
                                        }
                                    });
                                }

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }


                }
                catch (Exception e){
                    if(client==null){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this, "Unable to connect to server. Try again",
                                        Toast.LENGTH_LONG).show();
                            }
                        });
                                MainActivity.super.finish();
                    }
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

//    @Override

//    protected void onStop() {
//        super.onStop();
//        new endSocket().execute();
//    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(client!=null){
            if(!client.isClosed()){
                new endSocket().execute();
            }
        }
    }
    private void backToLoginActivity(){
        Intent i = new Intent(MainActivity.this,LoginActivity.class);
        startActivity(i);
    }

    private void changeStatus(Button[] b,ImageView[] priza,ImageView[] keraynos, String m){
        char[] mArray = m.toCharArray();

        for (int i=0;i<b.length;i++){
            if(mArray[i] == '0'){
                priza[i].setImageResource(R.drawable.ic_off);
                keraynos[i].setVisibility(View.INVISIBLE);
                b[i].setBackgroundResource(R.drawable.switch_btn_off);
//                b[i].setText("Off");
            }
            else{
                priza[i].setImageResource(R.drawable.ic_onk);
                keraynos[i].setVisibility(View.VISIBLE);
                b[i].setBackgroundResource(R.drawable.switch_btn_on);
//                b[i].setText("On");
            }
        }

    }

    private class btnListener implements View.OnClickListener{
        String num;

        public btnListener(String num) {
            this.num = num;
        }

        @Override
        public void onClick(View v) {
            btnPress btn= new btnPress();
            ExecutorService ex = Executors.newSingleThreadExecutor();
            btn.executeOnExecutor(ex,num);
            ex.shutdown();
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

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
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

    public class btnPress extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd = new Dialog(MainActivity.this, android.R.style.Theme_Black);
            View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.remove_border, null);
            pd.requestWindowFeature(Window.FEATURE_NO_TITLE);
            pd.getWindow().setBackgroundDrawableResource(R.color.transparent);
            pd.setContentView(view);
            pd.show();

        }

        @Override
        protected Void doInBackground(String... args) {
            try {
                printWriter = new PrintWriter(client.getOutputStream(),true);
                printWriter.print("Switch:"+args[0]);
                printWriter.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }

//            String message = null;
//            try {
//                in = new BufferedReader(new InputStreamReader(client.getInputStream()));
//                message = in.readLine();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            return message;
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
           // printWriter.close();
        }
    }

    public class endSocket extends AsyncTask<Void, Void, Void> {


        @Override
        protected Void doInBackground(Void... voids) {
            try {
                printWriter = new PrintWriter(client.getOutputStream(),true);
                printWriter.print("off:");
                printWriter.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }

//            String message = null;
//            try {
//                in = new BufferedReader(new InputStreamReader(client.getInputStream()));
//                message = in.readLine();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            return message;
            return null;
        }


        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            try {
                client.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
