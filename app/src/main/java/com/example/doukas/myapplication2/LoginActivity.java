package com.example.doukas.myapplication2;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

/**
 *The starting activity of the login screen of the App
 */
public class LoginActivity extends AppCompatActivity {
    /**
     * ipaddress is the field of the ip address you input
     * password is the field of the password you input
     * loginbtn is the button for the login
     * externalbtn is the button for the external connection
     * localbtn is the button for the local connection
     * logdiv is the Division of login that presents itself when
     *        you press the external connection option
     * btndiv is the button division that gives you at the start
     *        the external or local connection option
     * port is the port that is used for the socket connection
     * client is the client object of the socket connection
     * printWriter standard printwriter used to send the
     *             server messages
     */
    EditText ipaddress;
    EditText password;
    Button loginbtn;
    Button externalbtn;
    Button localbtn;
    LinearLayout logdiv;
    LinearLayout btndiv;
    private int port = 8888;
    private Socket client;
    private PrintWriter printWriter;


    /**
     * The create function of the activity's life cycle
     * the initialization of most variables used in this
     * activity and button functions
     * @param savedInstanceState the saved instance of
     *                           the activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //checks if the App runs for the first time
        checkFirstRun();
        //gets the PreferenceManager to get or edit the Apps preferences
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        //gets the ip address saved in preferences
        String myIp = prefs.getString("MYIPADDRESS", "defaultStringIfNothingFound");
        //sets the layout of the activity
        setContentView(R.layout.activity_login);
        //sets toolbar
        Toolbar toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        //sets the variables with the modules from the layout
        ipaddress=findViewById(R.id.ipaddressfield);
        //sets the ip from preferences that is saved for future uses of the app so you dont need to type
        //it every time
        ipaddress.setText(myIp);
        password=findViewById(R.id.passwordfield);
        loginbtn=findViewById(R.id.loginbtn);
        externalbtn=findViewById(R.id.externalconbtn);
        localbtn=findViewById(R.id.localconbtn);
        ImageView logo = findViewById(R.id.Logo);
        logdiv= findViewById(R.id.loginDiv);
        btndiv= findViewById(R.id.buttonLayout);
        //hides the login division at the start of the activity
        logdiv.setVisibility(View.GONE);
        //animation for the logo to came from the top of the screen
        Animation fromTop = AnimationUtils.loadAnimation(this,R.anim.fromtop);
        logo.setAnimation(fromTop);
        //animation for the button division to came from the bottom of the screen
        Animation fromBot = AnimationUtils.loadAnimation(this,R.anim.frombot);
        btndiv.setAnimation(fromBot);

        //function of the local connection button
        localbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                SharedPreferences.Editor prefEditor = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
                prefEditor.putString("MYIPADDRESS", "192.168.1.120");//set local ip address of the server
                prefEditor.apply();//apply ip address in App preferences
                if(isNetworkConnected())//checks connectivity
                    presentActivity(view);//presents next activity
                else
                    //toast for error message
                    Toast.makeText(LoginActivity.this, R.string.no_internet_connection,
                            Toast.LENGTH_LONG).show();
            }
        });
        //function of the external connection button
        externalbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                //sets animations
                Animation Fadeout = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fade_out);
                Animation Fadein = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fade_in);
                //Button division fade out to reveal the login division
                btndiv.startAnimation(Fadeout);
                btndiv.setVisibility(View.GONE);
                logdiv.startAnimation(Fadein);
                logdiv.setVisibility(View.VISIBLE);
            }
        });
        //function of the login button
        loginbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                //Sets ip address and password in the application preferences for the next activity to use
                String ip=ipaddress.getText().toString();
                String pass=password.getText().toString();
                SharedPreferences.Editor prefEditor = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
                prefEditor.putString("MYIPADDRESS", ip);
                prefEditor.putString("PASS",pass);
                prefEditor.apply();

                if(isNetworkConnected())//checks connectivity
                    presentActivity(view);//presents next activity
                else
                    //toast for error message
                    Toast.makeText(LoginActivity.this, R.string.no_internet_connection,
                            Toast.LENGTH_LONG).show();
            }
        });

    }

    /**
     *Function to populate the menu bar
     * @param menu the menu object
     * @return true
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return true;
    }

    /**
     *Function that interacts with the menu objects
     * and sets their functionality
     * @param item The items of the menu
     * @return true
     */
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

    /**
     *Function that reveal the next activity with an animation
     * like the next activity screen expands from the button the
     * user presses
     * @param view the button that the animation starts
     */
    public void presentActivity(View view) {
        ActivityOptionsCompat options = ActivityOptionsCompat.
                makeSceneTransitionAnimation(this, view, "transition");
        //gets the position of the button
        int revealX = (int) (view.getX()*2 + view.getWidth() / 2);
        int revealY = (int) (view.getY()*2 + view.getHeight()*2);
        //construct the intent for the next activity with the x and y to start the animation
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(MainActivity.EXTRA_CIRCULAR_REVEAL_X, revealX);
        intent.putExtra(MainActivity.EXTRA_CIRCULAR_REVEAL_Y, revealY);

        ActivityCompat.startActivity(this, intent, options.toBundle());
    }

    /**
     * Check if the App is running for the first time
     * If yes then it asks you to write down a valid email
     * so the App can send you mails for the server IP
     */
    public void checkFirstRun() {
        boolean isFirstRun = getSharedPreferences("PREFERENCE", MODE_PRIVATE).getBoolean("isFirstRun", true);
        if (isFirstRun){
            alertSetEmail();//pops up the email dialog
            //changes the boolean to false so the app knows it isnt a first time launch
            getSharedPreferences("PREFERENCE", MODE_PRIVATE)
                    .edit()
                    .putBoolean("isFirstRun", false)
                    .apply();
            SharedPreferences.Editor prefEditor = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
            prefEditor.putString("MYIPADDRESS", "192.168.1.120");//set local ip
            prefEditor.apply();
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
     *Function of the email dialog box that pops up in the first launch of the app
     * or if you press the set email option in the menu
     */
    public void alertSetEmail(){
        //initialization of the dialog
        final Dialog dialog = new Dialog(LoginActivity.this);
        dialog.setContentView(R.layout.setup_email);
        dialog.setTitle("Email Setup");
        Button confirmbtn = dialog.findViewById(R.id.confirmbtn);
        //Function of the confirm button
        confirmbtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                EditText edit= dialog.findViewById(R.id.email_input);//gets email from edittext box
                String e=edit.getText().toString();
                if (!isEmailValid(e)){//check if mail is valid

                    //toast with error message for invalid email
                    Toast toast = Toast.makeText(getApplicationContext(),"Email is invalid!!", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }
                else{
                    if(isNetworkConnected()) {
                    //sets email in the preferences of the aplication
                    SharedPreferences.Editor prefEditor = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
                    e="Email:"+e;
                    prefEditor.putString("MYEMAIL", e);
                    prefEditor.apply();
                    //Thread for the local connection with the server to send the email
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                //gets email from preferences
                                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());//get prefs
                                String myEmail = prefs.getString("MYEMAIL", "defaultStringIfNothingFound");
                                //creates socket connection locally with the server
                                client=new Socket();
                                SocketAddress server = new InetSocketAddress("192.168.1.120",port);
                                client.connect(server);
                                printWriter = new PrintWriter(client.getOutputStream(),true);
                                BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                                //sends email to server
                                printWriter.print(myEmail);
                                printWriter.flush();

                                in.readLine();
                                in.readLine();
                                final String s=in.readLine();
                                //thread that waits conformation from the server that the email is send
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getApplicationContext(),s, Toast.LENGTH_LONG).show();
                                    }
                                });

                                //disconnects client from the server and closes connection
                                printWriter = new PrintWriter(client.getOutputStream(),true);
                                printWriter.print("off:");
                                printWriter.flush();
                                client.close();
                            }
                            //error message when you cant connect to the server
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
        //set and functionality of the cancel button that dismiss the dialog
        Button cancelbtn = dialog.findViewById(R.id.cancelbtn);
        cancelbtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                    dialog.dismiss();
            }
        });
        dialog.show();
    }

    /**
     *Checks if you are connected to the network
     * @return True if connected false if not
     */
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }
}
