package gov.nysenate.inventory.activity;

import gov.nysenate.inventory.android.ClearableAutoCompleteTextView;
import gov.nysenate.inventory.android.R;
import gov.nysenate.inventory.android.RequestTask;
import gov.nysenate.inventory.android.R.anim;
import gov.nysenate.inventory.android.R.id;
import gov.nysenate.inventory.android.R.layout;
import gov.nysenate.inventory.android.R.menu;
import gov.nysenate.inventory.model.Commodity;
import gov.nysenate.inventory.model.Location;
import gov.nysenate.inventory.util.TransactionParser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class Delivery1 extends SenateActivity
{
    static ClearableAutoCompleteTextView autoCompleteTextView1;// for location
                                                               // code
    public ArrayList<String> locCodeList = new ArrayList<String>();
    String URL = "";
    public String res = null;
    public String status = null;
    public String loc_code_str = null;
    public TextView loc_details;
    public String deliveryLocation = null;
    static Button btnDelivery1Cont;
    static Button btnDelivery1Cancel;
    TextView tvOfficeD;
    // TextView tvLocCdD;
    TextView tvDescriptD;
    public static ProgressBar progBarDelivery1;
    Activity currentActivity;
    String timeoutFrom = "delivery1";
    public final int LOCCODELIST_TIMEOUT = 101, LOCATIONDETAILS_TIMEOUT = 102;
    private int lastSize = 0;
    private List<Location> locations;

    boolean locationBeingTyped = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery1);
        registerBaseActivityReceiver();
        currentActivity = this;

        // Setup Data Textviews
        tvOfficeD = (TextView) this.findViewById(R.id.tvOfficeD);
        // tvLocCdD = (TextView)this.findViewById(R.id.tvLocCdD);
        tvDescriptD = (TextView) this.findViewById(R.id.tvDescriptD);

        // Setup ProgressBar

        progBarDelivery1 = (ProgressBar) this
                .findViewById(R.id.progBarDelivery1);

        // for origin dest code
        autoCompleteTextView1 = (ClearableAutoCompleteTextView) findViewById(R.id.autoCompleteTextView1);
        autoCompleteTextView1.setThreshold(1);
        autoCompleteTextView1
                .setOnItemClickListener(new AdapterView.OnItemClickListener()
                {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                            int position, long id) {
                        getLocationDetails();
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(
                                autoCompleteTextView1.getWindowToken(), 0);
                        autoCompleteTextView1.setSelection(0);
                        locationBeingTyped = false;
                    }
                });
        getLocCodeList();
        // code for textwatcher
        // for origin location code
        // loc_code = (EditText) findViewById(R.id.editText1);
        // loc_code.addTextChangedListener(filterTextWatcher);
        autoCompleteTextView1.addTextChangedListener(filterTextWatcher);
        // autoCompleteTextView2.addTextChangedListener(filterTextWatcher2);
        loc_details = (TextView) findViewById(R.id.textView2);
        // loc_details.findFocus(); we can use this to find focus
    }

    @Override
    protected void onResume() {
        super.onResume();
        btnDelivery1Cont = (Button) findViewById(R.id.btnDelivery1Cont);
        btnDelivery1Cont.getBackground().setAlpha(255);
        btnDelivery1Cancel = (Button) findViewById(R.id.btnDelivery1Cancel);
        btnDelivery1Cancel.getBackground().setAlpha(255);
        if (Delivery1.progBarDelivery1 == null) {
            Delivery1.progBarDelivery1 = (ProgressBar) this
                    .findViewById(R.id.progBarDelivery1);
        }
        Delivery1.progBarDelivery1.setVisibility(View.INVISIBLE);
    }

    @Override
    public void startTimeout(int timeoutType) {
        Intent intentTimeout = new Intent(this, LoginActivity.class);
        intentTimeout.putExtra("TIMEOUTFROM", timeoutFrom);
        startActivityForResult(intentTimeout, timeoutType);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i("onActivityResult", "start");

        switch (requestCode) {
        case LOCCODELIST_TIMEOUT:
            Log.i("onActivityResult", "LOCCODELIST WAS TIMED OUT");
            if (resultCode == RESULT_OK) {
                if (locationBeingTyped) {
                    autoCompleteTextView1.setText(autoCompleteTextView1
                            .getText());
                    autoCompleteTextView1.setSelection(autoCompleteTextView1
                            .getText().length());
                } else {
                    getLocCodeList();
                }
                break;
            } else {
                Log.i("onActivityResult", "TIMED OUT NOT OK");

            }
        case LOCATIONDETAILS_TIMEOUT:
            Log.i("onActivityResult", "LOCATIONDETAILS WAS TIMED OUT");
            if (resultCode == RESULT_OK) {
                getLocationDetails();
                new Timer().schedule(new TimerTask()
                {
                    @Override
                    public void run() {
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(
                                autoCompleteTextView1.getWindowToken(), 0);
                    }
                }, 50);
                break;
            } else {
                Log.i("onActivityResult", "TIMED OUT NOT OK");
            }
        }
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
            lastSize = autoCompleteTextView1.getText().toString().length();
        }

        @Override
        public void afterTextChanged(Editable s) {
            locationBeingTyped = true;
            int currentSize = autoCompleteTextView1.getText().toString()
                    .length();
            if (currentSize == 0 || currentSize < lastSize) {
                tvOfficeD.setText("N/A");
                tvDescriptD.setText("N/A");
            } /*
               * else if (textLength >= 3) { getLocationDetails(); }
               */
        }
    };

    public void noServerResponse() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        // set title
        alertDialogBuilder.setTitle(Html
                .fromHtml("<font color='#000055'>NO SERVER RESPONSE</font>"));

        // set dialog message
        alertDialogBuilder
                .setMessage(
                        Html.fromHtml("!!ERROR: There was <font color='RED'>NO SERVER RESPONSE</font>. <br/> Please contact STS/BAC."))
                .setCancelable(false)
                .setPositiveButton(Html.fromHtml("<b>Ok</b>"),
                        new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                // if this button is clicked, just close
                                // the dialog box and do nothing
                                Context context = getApplicationContext();

                                CharSequence text = "No action taken due to NO SERVER RESPONSE";
                                int duration = Toast.LENGTH_SHORT;

                                Toast toast = Toast.makeText(context, text,
                                        duration);
                                toast.setGravity(Gravity.CENTER, 0, 0);
                                toast.show();

                                dialog.dismiss();
                            }
                        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    public void continueButton(View view) {
        if (checkServerResponse(true) == OK) {
            Delivery1.btnDelivery1Cont.getBackground().setAlpha(45);
            int duration = Toast.LENGTH_SHORT;

            String currentLocation = Delivery1.autoCompleteTextView1.getText()
                    .toString();

            if (currentLocation.trim().length() == 0) {
                Toast toast = Toast.makeText(this.getApplicationContext(),
                        "!!ERROR: You must first pick a Delivery Location.",
                        duration);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();

            } else if (locCodeList.indexOf(currentLocation) == -1) {
                Delivery1.btnDelivery1Cont.getBackground().setAlpha(255);
                Toast toast = Toast
                        .makeText(
                                this.getApplicationContext(),
                                "!!ERROR: Location Code \""
                                        + currentLocation
                                        + "\" is either invalid or not currently a Delivery Location.",
                                duration);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();

            } else {
                progBarDelivery1.setVisibility(View.VISIBLE);
                Intent intent = new Intent(this, Delivery2.class);
                intent.putExtra("location", deliveryLocation); // for location
                                                               // code
                                                               // of delivery
                startActivity(intent);
                overridePendingTransition(R.anim.in_right, R.anim.out_left);
            }
        }
    }

    public void cancelButton(View view) {
        Delivery1.btnDelivery1Cancel.getBackground().setAlpha(45);
        finish();
        overridePendingTransition(R.anim.in_left, R.anim.out_right);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_delivery1, menu);
        return true;
    }

    public void getLocationDetails() {
        deliveryLocation = autoCompleteTextView1.getText().toString().trim();
        String barcodeNumberDetails[] = autoCompleteTextView1.getText()
                .toString().trim().split("-");
        String barcode_num = barcodeNumberDetails[0];// this will be
                                                     // passed to the
                                                     // server
        loc_code_str = barcodeNumberDetails[0];// this will be passed to
                                               // next activity with
                                               // intent
        // check network connection
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            // fetch data
            status = "yes";

            // Get the URL from the properties
            URL = LoginActivity.properties.get("WEBAPP_BASE_URL").toString();

            AsyncTask<String, String, String> resr1 = new RequestTask()
                    .execute(URL + "/LocationDetails?barcode_num="
                            + barcode_num);
            try {
                Log.i("Server URL return", URL
                        + "/LocationDetails?barcode_num=" + barcode_num);
                if (resr1 == null) {
                    Log.i("Server returned nothing", "resr1 is null for " + URL
                            + "/LocationDetails?barcode_num=" + barcode_num);
                }

                else if (resr1.get() == null) {
                    Log.i("Server returned nothing", "resr1.get() is null for "
                            + URL + "/LocationDetails?barcode_num="
                            + barcode_num);

                }
                try {
                    res = null;
                    res = resr1.get().trim().toString();
                    if (res == null) {
                        noServerResponse();
                        return;
                    } else if (res.indexOf("Session timed out") > -1) {
                        startTimeout(this.LOCATIONDETAILS_TIMEOUT);
                        return;
                    }

                } catch (NullPointerException e) {
                    noServerResponse();
                    return;
                }
                try {
                    JSONObject object = (JSONObject) new JSONTokener(res)
                            .nextValue();
                    if (object.getString("delocat").equalsIgnoreCase(
                            "Does not exist in system")) {
                        tvOfficeD.setText("N/A");
                        tvDescriptD.setText("N/A");
                    } else {
                        tvOfficeD.setText(object.getString("cdrespctrhd"));
                        // tvLocCdD.setText(
                        // object.getString("cdlocat"));
                        tvDescriptD.setText(object.getString("adstreet1")
                                .replaceAll("&#34;", "\"")
                                + " ,"
                                + object.getString("adcity").replaceAll(
                                        "&#34;", "\"")
                                + ", "
                                + object.getString("adstate").replaceAll(
                                        "&#34;", "\"")
                                + " "
                                + object.getString("adzipcode").replaceAll(
                                        "&#34;", "\""));
                    }

                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    tvOfficeD.setText("!!ERROR: " + e.getMessage());
                    // tvLocCdD.setText( "!!ERROR: "+e.getMessage());
                    tvDescriptD.setText("Please contact STS/BAC.");

                    e.printStackTrace();
                }

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
    }

    public void getLocCodeList() {
        // check network connection
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            // fetch data
            status = "yes";

            // Get the URL from the properties
            URL = LoginActivity.properties.get("WEBAPP_BASE_URL").toString();
            AsyncTask<String, String, String> resr1 = new RequestTask()
                    .execute(URL + "/LocCodeList?NATYPE=DELIVERY");
            try {
                try {
                    res = null;
                    res = resr1.get().trim().toString();
                    if (res == null) {
                        noServerResponse();
                        return;
                    } else if (res.indexOf("Session timed out") > -1) {
                        startTimeout(this.LOCCODELIST_TIMEOUT);
                        return;
                    }
                } catch (NullPointerException e) {
                    noServerResponse();
                    return;
                }

                locations = TransactionParser.parseMultipleLocations(res);
                for (Location loc: locations) {
                    locCodeList.add(loc.getLocationSummaryString());
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                        android.R.layout.simple_dropdown_item_1line,
                        locCodeList);

                autoCompleteTextView1.setAdapter(adapter);

                // for destination code

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

    }

    @Override
    public void commoditySelected(int rowSelected, Commodity commoditySelected) {
        // TODO Auto-generated method stub

    }

    /*
     * Pickup3 AsyncTask class RequestTask extends AsyncTask<String, String,
     * String>{
     * 
     * @Override protected String doInBackground(String... uri) { // First
     * Upload the Signature and get the nuxsign from the Server if
     * (requestTaskType.equalsIgnoreCase("Pickup")) { String NUXRRELSIGN = "";
     * 
     * ByteArrayOutputStream bs = new ByteArrayOutputStream(); Bitmap bitmap =
     * sign.getImage(); Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap,
     * 200, 40, true);
     * //System.out.println("SCALED SIZE:"+bitmap.getByteCount()+
     * " -> "+scaledBitmap.getByteCount()); for (int
     * x=0;x<scaledBitmap.getWidth();x++) { for (int
     * y=0;y<scaledBitmap.getHeight();y++) { String strColor =
     * String.format("#%06X", 0xFFFFFF & scaledBitmap.getPixel(x, y)); if
     * (strColor.equals("#000000")||scaledBitmap.getPixel(x,
     * y)==Color.TRANSPARENT) { //
     * System.out.println("********"+x+" x "+y+" SETTING COLOR TO WHITE");
     * scaledBitmap.setPixel(x, y, Color.WHITE); } } }
     * scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 90, bs); imageInByte =
     * bs.toByteArray(); String responseString = ""; try { URL url = new
     * URL(uri[0]);
     * 
     * HttpURLConnection conn = (HttpURLConnection) url.openConnection(); // Set
     * connection parameters. conn.setDoInput(true); conn.setDoOutput(true);
     * conn.setUseCaches(false);
     * 
     * //Set content type to PNG conn.setRequestProperty("Content-Type",
     * "image/jpg"); OutputStream outputStream = conn.getOutputStream();
     * OutputStream out = outputStream; // Write out the bytes of the content
     * string to the stream. out.write(imageInByte); out.flush(); out.close();
     * // Read response from the input stream. BufferedReader in = new
     * BufferedReader(new InputStreamReader(conn.getInputStream())); String
     * temp; while ((temp = in.readLine()) != null) { responseString += temp +
     * "\n"; } temp = null; in.close(); //
     * System.out.println("Server response:\n'" + responseString + "'"); int
     * nuxrsignLoc = responseString.indexOf("NUXRSIGN:"); if (nuxrsignLoc>-1) {
     * NUXRRELSIGN = responseString.substring(nuxrsignLoc+9).replaceAll("\r",
     * "").replaceAll("\n", ""); } else { NUXRRELSIGN =
     * responseString.replaceAll("\r", "").replaceAll("\n", ""); }
     * 
     * } catch (Exception e) { e.printStackTrace(); }
     * 
     * // Then post the rest of the information along with the NUXRSIGN
     * HttpClient httpclient = new DefaultHttpClient(); HttpResponse response;
     * responseString = null; try {
     * 
     * String pickupURL =
     * uri[1]+"&NUXRRELSIGN="+NUXRRELSIGN+"&DECOMMENTS="+DECOMMENTS;
     * System.out.println("pickupURL:"+pickupURL); response =
     * httpclient.execute(new HttpGet(pickupURL)); StatusLine statusLine =
     * response.getStatusLine(); if(statusLine.getStatusCode() ==
     * HttpStatus.SC_OK){ ByteArrayOutputStream out = new
     * ByteArrayOutputStream(); response.getEntity().writeTo(out); out.close();
     * responseString = out.toString(); } else{ //Closes the connection.
     * response.getEntity().getContent().close(); throw new
     * IOException(statusLine.getReasonPhrase()); } } catch
     * (ClientProtocolException e) { //TODO Handle problems.. } catch
     * (IOException e) { //TODO Handle problems.. } res=responseString; return
     * responseString; } else if
     * (requestTaskType.equalsIgnoreCase("EmployeeList")) { HttpClient
     * httpclient = new DefaultHttpClient(); HttpResponse response; String
     * responseString = null; try { response = httpclient.execute(new
     * HttpGet(uri[0])); StatusLine statusLine = response.getStatusLine(); if
     * (statusLine.getStatusCode() == HttpStatus.SC_OK) { ByteArrayOutputStream
     * out = new ByteArrayOutputStream(); response.getEntity().writeTo(out);
     * out.close(); responseString = out.toString(); } else { // Closes the
     * connection. response.getEntity().getContent().close(); throw new
     * IOException(statusLine.getReasonPhrase()); } } catch
     * (ClientProtocolException e) { // TODO Handle problems.. } catch
     * (IOException e) { // TODO Handle problems.. } res = responseString;
     * return responseString; } else { System.out.println
     * ("!!ERROR: Invalid requestTypeTask:"+requestTaskType); return
     * "!!ERROR: Invalid requestTypeTask:"+requestTaskType; } } }
     */

}
