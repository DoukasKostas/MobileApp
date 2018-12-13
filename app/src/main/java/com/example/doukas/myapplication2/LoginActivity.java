package com.example.doukas.myapplication2;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

public class LoginActivity extends AppCompatActivity {

    EditText ipaddress;
    EditText password;
    Button loginbtn;
    Button externalbtn;
    Button localbtn;
    LinearLayout loginlayout;
    LinearLayout logdiv;
    LinearLayout btndiv;
    private int port = 8888;
    private Socket client;
    private PrintWriter printWriter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkFirstRun();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());//get prefs
        String myIp = prefs.getString("MYIPADDRESS", "defaultStringIfNothingFound");//get prefs
        setContentView(R.layout.activity_login);
        Toolbar toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        ipaddress=findViewById(R.id.ipaddressfield);
        ipaddress.setText(myIp);
        password=findViewById(R.id.passwordfield);
        loginbtn=findViewById(R.id.loginbtn);
        externalbtn=findViewById(R.id.externalconbtn);
        localbtn=findViewById(R.id.localconbtn);
        ImageView logo = findViewById(R.id.Logo);
        logdiv= findViewById(R.id.loginDiv);
        btndiv= findViewById(R.id.buttonLayout);
        logdiv.setVisibility(View.GONE);
        Animation fromTop = AnimationUtils.loadAnimation(this,R.anim.fromtop);
        logo.setAnimation(fromTop);
        Animation fromBot = AnimationUtils.loadAnimation(this,R.anim.frombot);
        btndiv.setAnimation(fromBot);

        localbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                SharedPreferences.Editor prefEditor = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();//edit prefs
                prefEditor.putString("MYIPADDRESS", "192.168.1.120");//edit prefs
                prefEditor.apply();//edit prefs
                if(isNetworkConnected())
                    presentActivity(view);
                else
                    Toast.makeText(LoginActivity.this, "No Connection. Open wifi or data to connect",
                            Toast.LENGTH_LONG).show();
            }
        });

        externalbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                Animation Fadeout = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fade_out);
                Animation Fadein = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fade_in);
                btndiv.startAnimation(Fadeout);
                btndiv.setVisibility(View.GONE);
                logdiv.startAnimation(Fadein);
                logdiv.setVisibility(View.VISIBLE);
            }
        });

        loginbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                String ip=ipaddress.getText().toString();
                String pass=password.getText().toString();
                SharedPreferences.Editor prefEditor = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();//edit prefs
                prefEditor.putString("MYIPADDRESS", ip);//edit prefs
                prefEditor.putString("PASS",pass);
                prefEditor.apply();//edit prefs

                if(isNetworkConnected())
                    presentActivity(view);
                else
                    Toast.makeText(LoginActivity.this, R.string.no_internet_connection,
                            Toast.LENGTH_LONG).show();
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                alertSetEmail();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }


    public void presentActivity(View view) {
        ActivityOptionsCompat options = ActivityOptionsCompat.
                makeSceneTransitionAnimation(this, view, "transition");
        int revealX = (int) (view.getX()*2 + view.getWidth() / 2);
        int revealY = (int) (view.getY()*2 + view.getHeight()*2);

        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(MainActivity.EXTRA_CIRCULAR_REVEAL_X, revealX);
        intent.putExtra(MainActivity.EXTRA_CIRCULAR_REVEAL_Y, revealY);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());//get prefs
        String myIp = prefs.getString("MYIPADDRESS", "defaultStringIfNothingFound");//get prefs
        String myEmail = prefs.getString("MYEMAIL", "defaultStringIfNothingFound");//get prefs
        String pass = prefs.getString("PASS", "defaultStringIfNothingFound");//get prefs

        ActivityCompat.startActivity(this, intent, options.toBundle());
    }

    /**
     * Check if the App is running for the first time
     * If yes then it promotes you to right down a valid email
     * so the App can send u mails for the server IP
     */
    public void checkFirstRun() {
        boolean isFirstRun = getSharedPreferences("PREFERENCE", MODE_PRIVATE).getBoolean("isFirstRun", true);
        if (isFirstRun){
            alertSetEmail();
            getSharedPreferences("PREFERENCE", MODE_PRIVATE)
                    .edit()
                    .putBoolean("isFirstRun", false)
                    .apply();
            SharedPreferences.Editor prefEditor = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
            prefEditor.putString("MYIPADDRESS", "192.168.1.120");//set local ip
            prefEditor.apply();//edit prefs
        }
    }

    /**
     * Checks if the given string is a valid email address
     * @param email string you want to check
     * @return True if valid, False if not
     */
    boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    /**
     * Method that creates a Dialog Box to send a new email address to the PIserver
     */
    public void alertSetEmail(){

        final Dialog dialog = new Dialog(LoginActivity.this);
        dialog.setContentView(R.layout.setup_email);
        dialog.setTitle("Email Setup");
        Button confirmbtn = dialog.findViewById(R.id.confirmbtn);
        confirmbtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                EditText edit= dialog.findViewById(R.id.email_input);
                String e=edit.getText().toString();
                if (!isEmailValid(e)){
                    Toast toast = Toast.makeText(getApplicationContext(),"Email is invalid!!", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }
                else{
                    if(isNetworkConnected()) {


                    SharedPreferences.Editor prefEditor = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();//edit prefs
                    e="Email:"+e;
                    prefEditor.putString("MYEMAIL", e);//edit prefs
                    prefEditor.apply();//edit prefs
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());//get prefs
                                String myEmail = prefs.getString("MYEMAIL", "defaultStringIfNothingFound");
                                client=new Socket();
                                SocketAddress server = new InetSocketAddress("192.168.1.120",port);
                                client.connect(server);
                                printWriter = new PrintWriter(client.getOutputStream(),true);
                                BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                                printWriter.print(myEmail);
                                printWriter.flush();

                                in.readLine();
                                in.readLine();
                                final String s=in.readLine();

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getApplicationContext(),s, Toast.LENGTH_LONG).show();
                                    }
                                });


                                printWriter = new PrintWriter(client.getOutputStream(),true);
                                printWriter.print("off:");
                                printWriter.flush();
                                client.close();
                            }
                            catch (Exception e){
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getApplicationContext(),R.string.unable_to_connect, Toast.LENGTH_LONG).show();
                                    }
                                });

                                e.printStackTrace();
                            }
                        }
                    }).start();
                    dialog.dismiss();
                    }
                    else
                        Toast.makeText(LoginActivity.this, R.string.no_internet_connection,
                                Toast.LENGTH_LONG).show();
                }

            }
        });
        Button cancelbtn = dialog.findViewById(R.id.cancelbtn);
        cancelbtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                    dialog.dismiss();
            }
        });
        dialog.show();

    }
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }
}
