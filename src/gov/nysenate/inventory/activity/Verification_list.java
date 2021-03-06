package gov.nysenate.inventory.activity;

import gov.nysenate.inventory.android.R;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

public class Verification_list extends Activity
{

    public EditText loc_code;
    public TextView loc_details;
    public String res = null;
    public String status = null;
    ArrayList<Integer> scannedItems = new ArrayList<Integer>();
    ImageButton buttonVerifyListContinue;
    ImageButton buttonVerifyListCancel;
    ProgressBar progressBarVerifyList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification_list);

        // code for textwatcher

        loc_code = (EditText) findViewById(R.id.preferencePWD);
        loc_code.addTextChangedListener(filterTextWatcher);
        loc_details = (TextView) findViewById(R.id.textView2);
        buttonVerifyListContinue = (ImageButton) findViewById(R.id.buttonVerifyListContinue);
        buttonVerifyListCancel = (ImageButton) findViewById(R.id.buttonVerifyListCancel);
        progressBarVerifyList = (ProgressBar) findViewById(R.id.progressBarVerifyList);

    }

    private TextWatcher filterTextWatcher = new TextWatcher()
    {

        @Override
        public void onTextChanged(CharSequence s, int start, int before,
                int count) {
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                int after) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (loc_code.getText().toString().length() >= 6) {
                // loc_details.setText(loc_code.getText().toString());

                String barcode_num = loc_code.getText().toString().trim();

                // check network connection
                ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                if (networkInfo != null && networkInfo.isConnected()) {
                    // fetch data
                    status = "yes";
                    int barcode = Integer.parseInt(barcode_num);
                    scannedItems.add(barcode);

                    // Get the URL from the properties
                    String URL = LoginActivity.properties
                            .get("WEBAPP_BASE_URL").toString();
                    if (! URL.endsWith("/")) {
                        URL += "/";
                    }                       

                    AsyncTask<String, String, String> resr1 = new RequestTask()
                            .execute(URL + "ItemDetails?barcode_num="
                                    + barcode_num);
                    try {
                        res = resr1.get().trim().toString();

                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    status = "yes1";
                } else {
                    // display error
                    status = "no";
                }
                // loc_details.setText(res);
                loc_details.append("\n" + res);
                loc_code.setText("");
            }
        }
    };

    class RequestTask extends AsyncTask<String, String, String>
    {

        @Override
        protected String doInBackground(String... uri) {
            HttpClient httpclient = new DefaultHttpClient();
            HttpResponse response;
            String responseString = null;
            try {
                response = httpclient.execute(new HttpGet(uri[0]));
                StatusLine statusLine = response.getStatusLine();
                if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    response.getEntity().writeTo(out);
                    out.close();
                    responseString = out.toString();
                } else {
                    // Closes the connection.
                    response.getEntity().getContent().close();
                    throw new IOException(statusLine.getReasonPhrase());
                }
            } catch (ClientProtocolException e) {
                // TODO Handle problems..
            } catch (IOException e) {
                // TODO Handle problems..
            }
            res = responseString;
            return responseString;

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_verification2, menu);
        return true;
    }

    public void okButton(View view) {
        float alpha = 0.45f;
        AlphaAnimation alphaUp = new AlphaAnimation(alpha, alpha);
        alphaUp.setFillAfter(true);
        buttonVerifyListContinue.startAnimation(alphaUp);
        progressBarVerifyList.setVisibility(View.VISIBLE);
        Intent intent = new Intent(this, VerScanActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.in_right, R.anim.out_left);

    }

    public void cancelButton(View view) {

        float alpha = 0.45f;
        AlphaAnimation alphaUp = new AlphaAnimation(alpha, alpha);
        alphaUp.setFillAfter(true);
        buttonVerifyListCancel.startAnimation(alphaUp);
        Intent intent = new Intent(this, VerScanActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.in_left, R.anim.out_right);

    }
}
