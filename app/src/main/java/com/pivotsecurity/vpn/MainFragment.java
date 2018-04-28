package com.pivotsecurity.vpn;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;

import de.blinkt.openvpn.core.OpenVPNService;

import android.net.Uri;
import android.net.VpnService;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.telecom.ConnectionService;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.pivotsecurity.vpn.R;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;
import de.blinkt.openvpn.core.VpnStatus;
import static android.content.Context.MODE_PRIVATE;

public class MainFragment extends Fragment
{
    ProgressBar progressBar;
    TextView text, textmsg;
    String ip, city, state,country, isp;
    JSONObject jsonObject;
    Button secure;
    private Button btnLogin;
    private Button btnSignup;
    private Button btnLogout;
    private TextView username;
    private EditText password;
    boolean isFirst;
    boolean st;
    protected ConnectionService _connectionService;

    protected VpnService vpnService;
    OpenVPNService mService;


    SharedPreferences sharedPreferences;
    be.appfoundry.progressbutton.ProgressButton progressButton;

    public MainFragment() {
    }
    public static MainFragment newInstance(String param1, String param2) {
        MainFragment fragment = new MainFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        text = (TextView) view.findViewById(R.id.textview);
        textmsg=(TextView) view.findViewById(R.id.iptext);

        progressButton = (be.appfoundry.progressbutton.ProgressButton) view.findViewById(R.id.progressButton);
        progressButton.setIndeterminate(true);
        progressButton.setAnimationStep(3);
        progressButton.setAnimationDelay(5);
        progressButton.setStartDegrees(270);

        btnSignup = (Button) view.findViewById(R.id.btnSignup);
        btnLogin = (Button) view.findViewById(R.id.btnLogin);
        username = (TextView) view.findViewById(R.id.username);
        password = (EditText) view.findViewById(R.id.password);
        btnLogout = (Button) view.findViewById(R.id.btnLogout);


        MyAs myAs=new MyAs();
        myAs.execute();

        st = VpnStatus.isVPNActive();
        if (VpnStatus.isVPNActive()==true) {
            progressButton.setIcon(getResources().getDrawable(R.drawable.sk));
            progressButton.setColor(Color.WHITE);
            progressButton.stopAnimating();
            progressButton.setStrokeColor(Color.GREEN);
            setVisibulity(true);
        }else{
            setVisibulity(false);
            btnLogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Log.d("Main View", " Login pressed..... ");

                }
            });
            btnSignup.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Uri uri = Uri.parse("https://www.pivotsecurity.com");
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                }
            });
        }

        sharedPreferences = getActivity().getSharedPreferences("is_first", MODE_PRIVATE);
        isFirst = sharedPreferences.getBoolean("is_first", true);
        VpnStatus.initLogCache(getActivity().getCacheDir());

        progressButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressButton.startAnimating();

                if (isFirst) {
                    //Toast.makeText(getActivity(), "Connecting", Toast.LENGTH_LONG).show();
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("is_first", false);
                    editor.apply();
                    startVpn();

                    class New1 extends Thread {

                        public void run() {
                            try {
                                Thread.sleep(10000);
                                MyAs m = new MyAs();
                                m.execute();
                                //text.setText("Secure");
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    New1 n = new New1();
                    n.start();
                }

                st = VpnStatus.isVPNActive();
                if (st == false)
                {
                        startVpn();
                        progressButton.setIcon(getResources().getDrawable(R.drawable.sk));
                        progressButton.setColor(Color.WHITE);
                        progressButton.stopAnimating();
                        progressButton.setStrokeColor(Color.GREEN);
                        //textmsg.setText("YOUR CONNECTION IS SECURE");
//                        textmsg.setTextColor(Color.GREEN);

                    class New1 extends Thread {

                            public void run() {
                                try {
                                    Thread.sleep(10000);
                                    MyAs m = new MyAs();
                                    m.execute();
                                    //text.setText("Secure");
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        New1 n = new New1();
                        n.start();
                } else if (st == true) {
                    progressButton.setIcon(getResources().getDrawable(R.drawable.sk));
                    progressButton.setColor(getResources().getColor(R.color.colorPrimary));
                    progressButton.setIcon(getResources().getDrawable(R.drawable.bnnn));
                    progressButton.stopAnimating();
                    progressButton.setStrokeColor(Color.parseColor("#cccccc"));
                    startVpn();

                    class New1 extends Thread {

                        public void run() {
                            try {
                                Thread.sleep(10000);
                                MyAs m2 = new MyAs();
                                m2.execute();
                                //text.setText("Secure");
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    New1 n2 = new New1();
                    n2.start();
                }

                Log.d("connectionstate",String.valueOf(VpnStatus.isVPNActive()));
        }
        });

        return view;
    }

    private void startVpn() {
        Log.d("Main Activity","Connection initiated ..");
        try {
                InputStream conf = getActivity().getAssets().open("client.ovpn");// your own file in /assets/client.bin
                InputStreamReader isr = new InputStreamReader(conf);
                BufferedReader br = new BufferedReader(isr);
                String config = "";
                String line;
                while (true) {
                    line = br.readLine();
                    if (line == null) break;
                    config += line + "\n";
                }
                br.readLine();
                de.blinkt.openvpn.OpenVpnApi.startVpn(getActivity(), config, null, null);
            Log.d("Main Activity","Connection Successful ..");
            setVisibulity(true);

        }
        catch (IOException | RemoteException e) {
            e.printStackTrace();
        }
    }
    private void setVisibulity(boolean bStatus){
        if (bStatus){
            btnLogin.setVisibility(View.GONE);
            btnSignup.setVisibility(View.GONE);
            username.setVisibility(View.GONE);
            password.setVisibility(View.GONE);
            btnLogout.setVisibility(View.VISIBLE);
        }else{
            btnLogin.setVisibility(View.VISIBLE);
            btnSignup.setVisibility(View.VISIBLE);
            username.setVisibility(View.VISIBLE);
            password.setVisibility(View.VISIBLE);
            btnLogout.setVisibility(View.GONE);
        }
    }


    class MyAs extends AsyncTask<String, Void, String> {

        String s = null;

        @Override
        protected void onPostExecute(String s)
        {
            super.onPostExecute(s);
            if(city==null || state==null || country==null)
            {
                textmsg.setText(" No internet connection");
         //       secure.setEnabled(false);
            }

            else
            {
                if (VpnStatus.isVPNActive()==false) {
                    textmsg.setText("\nYOUR CONNECTION IS\n           UNSECURE");
                    textmsg.setTextColor(Color.RED);

                }
                else
                {
                    textmsg.setText("\nYOUR CONNECTION IS\n             SECURE");
                    textmsg.setTextColor(Color.GREEN);

                }
                text.setText(" IP : " + ip + "\n ISP : " + isp);
            }
            //mapView.animate();
        }

        @Override
        protected String doInBackground(String... params) {

            try
            {
                URL whatismyip = new URL("http://checkip.amazonaws.com");
                BufferedReader input = new BufferedReader(new InputStreamReader(whatismyip.openStream()));
                ip = input.readLine();

                HttpURLConnection connection = (HttpURLConnection) new URL("http://ip-api.com/json/" + ip).openConnection();
                connection.setReadTimeout(3000);
                connection.setConnectTimeout(3000);
                connection.setRequestMethod("GET");
                connection.setDoInput(true);
                connection.connect();
                int responseCode = connection.getResponseCode();
                if (responseCode != HttpsURLConnection.HTTP_OK) {
                    throw new IOException("HTTP error code: " + responseCode);
                }
                InputStream in = new java.io.BufferedInputStream(connection.getInputStream());
                //s=readStream(in,10000);
                s=convertStreamToString(in);
                jsonObject=new JSONObject(s);

                city=jsonObject.getString("city");
                isp=jsonObject.getString("org");
                state=jsonObject.getString("regionName");
                country=jsonObject.getString("country");

                Log.d("MainActivity", "Call reached here");

                if (in != null) {
                    // Converts Stream to String with max length of 500.
                    Log.d("MainActivity call 2", s);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return s;
        }

        private String convertStreamToString(InputStream is) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();

            String line;
            try {
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append('\n');
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return sb.toString();
        }

    }

}





