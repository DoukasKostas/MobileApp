package com.example.doukas.myapplication2;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

public class LoginActivity extends AppCompatActivity {

    EditText ipaddress;
    EditText password;
    Button loginbtn;
    private int port = 8888;
    private Socket client;
    private PrintWriter printWriter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkFirstRun();
        setContentView(R.layout.activity_login);
        Toolbar toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ipaddress=findViewById(R.id.ipaddressfield);
        password=findViewById(R.id.passwordfield);
        loginbtn=findViewById(R.id.loginbtn);
        loginbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String ip=ipaddress.getText().toString();
                SharedPreferences.Editor prefEditor = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this).edit();//edit prefs
                prefEditor.putString("MYIPADDRESS", ip);//edit prefs
                prefEditor.apply();//edit prefs
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {

                            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);//get prefs
                            String myIp = prefs.getString("MYIPADDRESS", "defaultStringIfNothingFound");
                            /*client=new Socket();
                            SocketAddress server = new InetSocketAddress(myIp,port);
                            client.connect(server);
                            printWriter = new PrintWriter(client.getOutputStream(),true);
                            printWriter.println("OK");
                            printWriter.flush();*/
                        }
                        catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }).start();
                presentActivity(view);
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
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);//get prefs
        String myEmail = prefs.getString("MYEMAIL", "defaultStringIfNothingFound");//get prefs
        String myIp = prefs.getString("MYIPADDRESS", "defaultStringIfNothingFound");//get prefs
        Toast.makeText(LoginActivity.this,myEmail+" connected to:"+myIp
                , Toast.LENGTH_LONG).show();//gia debug

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
                    Toast toast = Toast.makeText(LoginActivity.this,"Email is invalid!!", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }
                else{
                    SharedPreferences.Editor prefEditor = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this).edit();//edit prefs
                    e="Email:"+e;
                    prefEditor.putString("MYEMAIL", e);//edit prefs
                    prefEditor.apply();//edit prefs
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);//get prefs
                                String myEmail = prefs.getString("MYEMAIL", "defaultStringIfNothingFound");
                                client=new Socket();
                                SocketAddress server = new InetSocketAddress("192.168.1.120",port);
                                client.connect(server);
                                printWriter = new PrintWriter(client.getOutputStream(),true);
                                printWriter.println(myEmail);
                                printWriter.flush();
                            }
                            catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                    }).start();
                    dialog.dismiss();
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
}
