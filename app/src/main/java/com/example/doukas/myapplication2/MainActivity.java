package com.example.doukas.myapplication2;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
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
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * Created by Doukas on 10/3/2018.
 *
 * Main Activity off SmartTaPanda app
 * It handles the connection with the server
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
    private TextView[] stat=new TextView[4];
    private int port = 8888;
    private Socket client;
    private PrintWriter printWriter;
    private BufferedReader in;
    private Dialog pd,loadingSCon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity_new);

        //initializing Buttons and ImageViews
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
        //set onClickListeners
        sw[0].setOnClickListener(new btnListener("1"));
        sw[1].setOnClickListener(new btnListener("2"));
        sw[2].setOnClickListener(new btnListener("3"));
        sw[3].setOnClickListener(new btnListener("4"));

        /** Dialog (spinning circle)
         *  Prevents the user from doing anything till the connection is establised
         */
        loadingSCon = new Dialog(MainActivity.this, android.R.style.Theme_Black);
        View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.remove_border, null);
        loadingSCon.requestWindowFeature(Window.FEATURE_NO_TITLE);
        loadingSCon.getWindow().setBackgroundDrawableResource(R.color.transparent);
        loadingSCon.setContentView(view);
        /** Override onCancel
         *  On back button closes the activity, not just the dialog
         */
        loadingSCon.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                MainActivity.super.finish();
            }
        });
        loadingSCon.show(); //starts dialog

        /** Connection Thread
         *  Handles the connection with the server
         *
         *  runs all the time the mainActivity is open
         *  and waits for responses from the server
         */
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //Gets the preferences from LoginActivity
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());//get prefs
                    String myIp = prefs.getString("MYIPADDRESS", "defaultStringIfNothingFound");
                    String pass =  prefs.getString("PASS","defaultStringIfNothingFound");
                    if (pass.equals("")) pass="null";//if no password is given then it replace it with "null"

                    client=new Socket(myIp,port);//opens the connection(socket) withe server
                    in = new BufferedReader(new InputStreamReader(client.getInputStream()));    //start a bufferReader for inputStream
                    printWriter = new PrintWriter(client.getOutputStream(),true);   //start printWriter for outputStream

                    //closes the dialog (spinning circle) when the connection is successful
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(loadingSCon!=null){
                                loadingSCon.hide();
                                loadingSCon.dismiss();
                            }
                        }
                    });

                    //sends the pass to connect (on local connection sends a null string and nothing happens)
                    printWriter.print(pass);
                    printWriter.flush();

                    //waits for a response from the server
                    String s0 = in.readLine();

                    //if the password is not correct
                    if(!s0.equals("Connected")){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this, R.string.wrong_password,
                                        Toast.LENGTH_LONG).show();

                            }
                        });
                        client.close();     //closes the connection
                        MainActivity.super.finish(); //return to previous Activity
                    }

                    /** Main Loop
                     *  loops till the connection is closed to keep the Connection Thread running
                     *  it reads data from server and do the necessary work
                     */
                    while (client.isConnected() && !client.isClosed() && isNetworkConnected()) {
                        try {
                            final String s = in.readLine(); //waits for server response

                            //if the response is the state string (always 4 bytes eg."0110")
                            if (s!=null && s.length() == 4) {

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        changeStatus(sw, priza, keraynos, s); //change the UI
                                        //closes the loading dialog (spinning circle)
                                        if(pd!=null){
                                            pd.hide();
                                            pd.dismiss();
                                        }
                                    }
                                });
                            }
                            else if (s.equals("")){     //on empty string (this happens sometimes when, the antennas packet is lost)
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(MainActivity.this, "Not Iniatialized properly. Try reconnecting.",
                                                Toast.LENGTH_LONG).show(); //prints error message
                                    }
                                });
                            }
                            else{   //everything else are error messages, so it prints the message the server send
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(MainActivity.this, s,
                                                Toast.LENGTH_LONG).show(); //prints the error message
                                        //closes the loading dialog (spinning circle)
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
                    //if the connection didn't start properly
                    if(client==null){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this, R.string.unable_to_connect,
                                        Toast.LENGTH_LONG).show(); //prints error message
                            }
                        });
                        MainActivity.super.finish(); //return to previous Activity
                    }
                    e.printStackTrace();
                }
            }
        },"ConnThread").start();


        //opens the activity with the starting animation
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

    /** Handles the connection when the mainActivity closes */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        //closes the connection if it is still up
        if(client!=null){
            if(!client.isClosed()){
                new endSocket().execute();
            }
        }
    }

    /** Method that changes UI, when a change happens to the switches
     *  takes a message from the server, that represends the switches and updates the UI
     *  eg."0100" : Means that the 2nd switch is powered and the others are off
     * @param b the Array of Buttons (UI elements)
     * @param priza The array of outlet-like ImageViews (UI elements)
     * @param keraynos The array of lightning-like ImageViews (UI elements)
     * @param m the message received from the server
     */
    private void changeStatus(Button[] b,ImageView[] priza,ImageView[] keraynos, String m){
        //change the string to a Char array to match the UI elements data structure
        char[] mArray = m.toCharArray();

        for (int i=0;i<b.length;i++){
            if(mArray[i] == '0'){   //if the switch is TURNED ON
                priza[i].setImageResource(R.drawable.ic_off);
                keraynos[i].setVisibility(View.INVISIBLE);
                b[i].setBackgroundResource(R.drawable.switch_btn_off);

            }
            else{
                priza[i].setImageResource(R.drawable.ic_onk);
                keraynos[i].setVisibility(View.VISIBLE);
                b[i].setBackgroundResource(R.drawable.switch_btn_on);
            }
        }

    }

    /** Custom button Listener
     *  it takes a number and start a AsyncTask(Thread) to send the number to the server
     */
    private class btnListener implements View.OnClickListener{
        String num;

        btnListener(String num) {
            this.num = num;
        }

        @Override
        public void onClick(View v) {
            btnPress btn= new btnPress();   //new AsyncTask
            ExecutorService ex = Executors.newSingleThreadExecutor();   //new Executor (hack for properly ending the thread after it finishes its job)
            btn.executeOnExecutor(ex,num);  //(without the executor the thread keeps staying on memory )
            ex.shutdown();  //properly ends the Thread
        }
    }

    /**
     *Function that reveal the activity with an animation
     * like the next activity screen expands from the button the
     * user presses
     */
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

    /**
     * Checks if you are connected to the network
     * @return True if connected false if not
     */
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }

    /**
     * Custom AsyncTask for button press
     * Handles the the sending of messages to the server
     * cause network connection cannot execute on the main thread
     */
    public class btnPress extends AsyncTask<String, Void, Void> {
        /**
         * Opens a dialog (spinning circle) till the app receives a respond from server
         */
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

        /**
         * Sends a string to the server
         * @param args the number(in string) u want to send to the server
         */
        @Override
        protected Void doInBackground(String... args) {
            try {
                //sends the data
                printWriter = new PrintWriter(client.getOutputStream(),true);
                printWriter.print("Switch:"+args[0]);
                printWriter.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }


            return null;
        }


        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }

    /**
     * Custom AsyncTask for closing the Socket (connection) on server side
     * Handles the the sending of messages to the server
     * cause network connection cannot execute on the main t
     */
    public class endSocket extends AsyncTask<Void, Void, Void> {


        @Override
        protected Void doInBackground(Void... voids) {
            try {
                //sends "off:" command to server
                printWriter = new PrintWriter(client.getOutputStream(),true);
                printWriter.print("off:");
                printWriter.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }


            return null;
        }


        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            try {
                //closes socket
                client.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
