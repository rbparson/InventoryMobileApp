package gov.nysenate.inventory.activity;

import gov.nysenate.inventory.activity.VerScanActivity.verList;
import gov.nysenate.inventory.adapter.InvListViewAdapter;
import gov.nysenate.inventory.android.ClearableEditText;
import gov.nysenate.inventory.android.MsgAlert;
import gov.nysenate.inventory.android.R;
import gov.nysenate.inventory.android.RequestTask;
import gov.nysenate.inventory.android.R.anim;
import gov.nysenate.inventory.android.R.id;
import gov.nysenate.inventory.android.R.layout;
import gov.nysenate.inventory.android.R.menu;
import gov.nysenate.inventory.android.R.raw;
import gov.nysenate.inventory.model.InvItem;
import gov.nysenate.inventory.model.Location;
import gov.nysenate.inventory.model.Transaction;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
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
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class Pickup2Activity extends SenateActivity
{
    public ClearableEditText senateTagTV;
    private TextView pickupCountTV;
    private TextView originSummary;
    private TextView destinationSummary;
    private String res = null;
    String status = null;
    private ListView pickedUpItemsLV;
    private boolean testResNull = false;
    private ArrayList<InvItem> scannedItems = new ArrayList<InvItem>();
    private ArrayList<InvItem> inactivatedItems = new ArrayList<InvItem>();
    private ArrayList<InvItem> newItems = new ArrayList<InvItem>();
    private ArrayList<InvItem> inTransitItems = new ArrayList<InvItem>();
    private ArrayAdapter<InvItem> adapter;
    private int pickupCount;
    private Location origin;
    private Location destination;
    static Button continueBtn;
    static Button cancelBtn;
    static ProgressBar progBarPickup2;
    String timeoutFrom = "pickup2";
    int count;
    public final int ITEMDETAILS_TIMEOUT = 101;
    public final int SENTAG_NOT_FOUND = 2001, INACTIVE_SENTAG = 2002,
            SENTAG_IN_TRANSIT = 2003;
    String className = this.getClass().getSimpleName();
    String URL = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pickup2);
        registerBaseActivityReceiver();
        try {
            autosave = getIntent().getParcelableExtra("autosave");
            if (autosave != null) {
                System.out.println("****PICKUP2ACTIVITY: TRYING TO RESTORE....");
            }
        } catch (Exception e) {
            e.printStackTrace();
            autosave = null;
            System.out.println("****PICKUP2ACTIVITY: NOTHING TO RESTORE.");
        }        

        origin = getIntent().getParcelableExtra("origin");
        destination = getIntent().getParcelableExtra("destination");
        pickedUpItemsLV = (ListView) findViewById(R.id.listView1);
        senateTagTV = (ClearableEditText) findViewById(R.id.etNusenate);
        senateTagTV.addTextChangedListener(senateTagTextWatcher);
        pickupCount = 0;
        adapter = new InvListViewAdapter(this, R.layout.invlist_item,
                scannedItems);
        pickedUpItemsLV.setAdapter(adapter);
        progBarPickup2 = (ProgressBar) findViewById(R.id.progBarPickup2);
        pickupCountTV = (TextView) findViewById(R.id.tv_count_pickup2);
        pickupCountTV.setText(Integer.toString(pickupCount));
        originSummary = (TextView) findViewById(R.id.tv_origin_pickup2);
        originSummary.setText(origin.getAdstreet1());
        destinationSummary = (TextView) findViewById(R.id.tv_destination_pickup2);
        destinationSummary.setText(destination.getAdstreet1());
        continueBtn = (Button) findViewById(R.id.btnPickup2Cont);
        continueBtn.getBackground().setAlpha(255);
        cancelBtn = (Button) findViewById(R.id.btnPickup2Cancel);
        cancelBtn.getBackground().setAlpha(255);

        
        
        try {
            autosave = getIntent().getParcelableExtra("autosave");
        } catch (Exception e) {
            autosave = null;
        }

        try {
            ContentValues values = new ContentValues();
            values.put("naactivity", className);
            values.put("dttxnupdate", MenuActivity.invSaveDB.getNow());
            values.put("natxnupduser", LoginActivity.nauser);

            long rowcnt = MenuActivity.invSaveDB.update("AM12ACTIVITY", values,
                    "nuxractivity = ?",
                    new String[] { Pickup1.nuxractivity });
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        try {
            Pickup1.progBarPickup1.setVisibility(View.INVISIBLE);
        } catch (Exception e) {
            e.printStackTrace();
        }

        handleAutosave();

    }

    @Override
    protected void onResume() {
        super.onResume();
        continueBtn = (Button) findViewById(R.id.btnPickup2Cont);
        continueBtn.getBackground().setAlpha(255);
        cancelBtn = (Button) findViewById(R.id.btnPickup2Cancel);
        cancelBtn.getBackground().setAlpha(255);
        if (progBarPickup2 == null) {
            progBarPickup2 = (ProgressBar) this
                    .findViewById(R.id.progBarPickup2);
        }
        progBarPickup2.setVisibility(View.INVISIBLE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
       /* case R.id.menu_test_null:
            item.setChecked(!item.isChecked());
            testResNull = item.isChecked();
            return true;*/
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    private final TextWatcher senateTagTextWatcher = new TextWatcher()
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
            if (senateTagTV.getText().toString().length() >= 6) {
                String barcode_num = senateTagTV.getText().toString().trim();
                int flag = 0;
                boolean barcodeFound = false;

                // If the item is already scanned then display a
                // toster"Already Scanned"
                if (findBarcode(barcode_num) > -1) { // TODO: findBarcode() call
                    // display toster
                    barcodeFound = true;
                    Context context = getApplicationContext();
                    CharSequence text = "Already Scanned  ";
                    int duration = Toast.LENGTH_SHORT;

                    Toast toast = Toast.makeText(context, text, duration);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }

                // if it is not already scanned and does not exist in the
                // list(location)
                // then add it to list and append new item to its description
                if ((flag == 0) && (barcodeFound == false)) {
                    int returnedStatus = -1;
                    returnedStatus = getItemDetails();
                    if (returnedStatus == SERVER_SESSION_TIMED_OUT) {
                        return;
                    }
                }

                // notify the adapter that the data in the list is changed and
                // refresh the view
                updateChanges();
            }
        }
    };

    public void handleAutosave() {
        if (this.autosave != null) {
            currentlyRestoringAutosave = true;
            handleAutosaveSenateTags();

            if (!this.autosave.getNaactivity().equalsIgnoreCase(className)) {
                continueButton(null);
            }
        }
        currentlyRestoringAutosave = false;
    }

    public void handleAutosaveSenateTags() {
        ArrayList<InvItem> savedSenateTags = new ArrayList();
        if (this.autosave != null && this.autosave.getNuxractivity() > -1) {
            Cursor cursor = MenuActivity.invSaveDB
                    .rawQuery(
                            "SELECT a.nusenate, a.cdcond, a.decommodityf, a.cdlocatfrom, a.cdloctypefrom, a.cdlocatto, a.cdloctypeto, a.cdcategory, a.cdcommodity, a.decomments FROM ad12pickupinv a WHERE a.nuxractivity = ?",
                            new String[] { String.valueOf(this.autosave
                                    .getNuxractivity()) });
            // Cursor cursor =
            // this.invSaveDB.rawQuery("SELECT a.nuxractivity, a.naactivity, a.nuxracttype, a.dttxnorigin, a.dttxnupdate, b.deacttype FROM AM12ACTIVITY a, AL112ACTTYPE b WHERE a.natxnorguser = ? AND a.nuxracttype = b.nuxracttype",
            // new String[]{LoginActivity.nauser});
            // looping through all rows and adding to list
            System.out.println ("handleAutosaveSenateTags");
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    // System.out.println("DETAIL RECORD");
                    // System.out.println("..........COLUMN1:"
                    // + cursor.getString(0));
                    InvItem curInvItem = new InvItem();
                    curInvItem.setNusenate(cursor.getString(0));
                    curInvItem.setType(cursor.getString(1));
                    curInvItem.setDecommodityf(cursor.getString(2));
                    curInvItem.setCdlocat(cursor.getString(3));
                    String cdloctype = cursor.getString(4);
                    curInvItem.setCdloctype(cdloctype);
                    curInvItem.setCdcategory(cursor.getString(5));
                    curInvItem.setCdcommodity(cursor.getString(6));
                    curInvItem.setDecomments(cursor.getString(7));
                    System.out
                            .println("-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=- handleAutosaveSenateTags pull autoSave "
                                    + curInvItem.getNusenate()
                                    + ": "
                                    + curInvItem.getCdlocat()
                                    + "-"
                                    + curInvItem.getCdloctype()
                                    + " cdloctype:"
                                    + cdloctype
                                    + ": TYPE:("
                                    + curInvItem.getType() + ")");

                    // String nusenateCur = cursor.getString(0);
                    savedSenateTags.add(curInvItem);
                    // handleCurrentSenateTag(nusenateCur);

                } while (cursor.moveToNext());
            }

            if (savedSenateTags != null && savedSenateTags.size() > 0) {
                restoreSenateTags(savedSenateTags);
            }
        }
    }

    public void restoreSenateTags(ArrayList<InvItem> savedSenateTags) {
        restoreSenateTags(savedSenateTags, true);
    }

    public void restoreSenateTags(ArrayList<InvItem> savedSenateTags,
            boolean checkTimeout) {
        JSONArray jsArray = new JSONArray(
                this.getSenateTagList(savedSenateTags));
        VerList vl = new VerList();

        BasicNameValuePair nameValuePair = new BasicNameValuePair(
                "nusenateList", jsArray.toString());

        System.out.println ("restoreSenateTags Before URL");
        URL = LoginActivity.properties.get("WEBAPP_BASE_URL").toString();
        // check network connection
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        System.out.println("savedSenateTags COUNT:" + savedSenateTags.size());
        if (networkInfo != null && networkInfo.isConnected()) {
            inactivatedItems = new ArrayList<InvItem>();
            newItems = new ArrayList<InvItem>();
            inTransitItems = new ArrayList<InvItem>();
            // fetch data
            status = "yes";
            // int barcode= Integer.parseInt(barcode_num);
            // scannedItems.add(barcode);

            AsyncTask<String, String, String> resr1 = new RequestTask(
                    nameValuePair).execute(URL + "/ItemDetails");

            try {
                if (testResNull) { // Testing Purposes Only
                    resr1 = null;
                    Log.i("TEST RESNULL", "RES SET TO NULL");
                }

                res = null;
                res = resr1.get().trim().toString();

                if (res == null) {
                    if (savedSenateTags != null && savedSenateTags.size() > 0
                            && savedSenateTags.get(0) != null) {
                        noServerResponse(savedSenateTags.get(0).getNusenate());
                    } else {
                        noServerResponse("======");
                    }
                    return;
                } else if (checkTimeout
                        && res.indexOf("Session timed out") > -1) {
                    startTimeout(ITEMDETAILS_TIMEOUT);
                    return;
                }
                
                System.out.println ("*********************restoreSenateTags RES:"+res);

                JSONArray jsonArray = new JSONArray(res);
                count = jsonArray.length();
                // numItems = jsonArray.length();

                // this will populate the lists from the JSON array coming from
                // server
                System.out.println ("*********************restoreSenateTags COUNT:"+count);
                for (int i = 0; i < jsonArray.length(); i++) {
                   System.out.println("restoreSenateTags detail record:" + i);
                    String cdstatus = null;
                    try {
                        JSONObject jo = new JSONObject();
                        jo = jsonArray.getJSONObject(i);
                        

                        vl.NUSENATE = jo.getString("nusenate");
                        System.out.println("restoreSenateTags detail record:" + i+" Senate Tag#:"+vl.NUSENATE);
                        vl.CDCATEGORY = jo.getString("cdcategory");
                        vl.DECOMMODITYF = jo.getString("decommodityf");
                        vl.CDLOCAT = jo.getString("cdlocatto");
                        System.out.println("restoreSenateTags detail record:" + i+"(A1) Senate Tag#:"+vl.NUSENATE);

                        vl.CDCATEGORY = jo.getString("cdcategory");
                        vl.DECOMMODITYF = jo.getString("decommodityf")
                                .replaceAll("&#34;", "\"");
                        System.out.println("restoreSenateTags detail record:" + i+"(A5) Senate Tag#:"+vl.NUSENATE);
                        vl.CDLOCATTO = jo.getString("cdlocatto");
                        vl.CDLOCTYPETO = jo.getString("cdloctypeto");
                        vl.ADSTREET1 = jo.getString("adstreet1to").replaceAll(
                                "&#34;", "\"");
                        System.out.println("restoreSenateTags detail record:" + i+"(A10) Senate Tag#:"+vl.NUSENATE);
                        vl.DTISSUE = jo.getString("dtissue");
                        vl.CDLOCAT = jo.getString("cdlocatto");
                        vl.CDINTRANSIT = jo.getString("cdintransit");
                        vl.CDSTATUS = jo.getString("cdstatus");
                        System.out.println("restoreSenateTags detail record:" + i+"(A15) Senate Tag#:"+vl.NUSENATE);

                        if (origin.getCdlocat().equalsIgnoreCase(vl.CDLOCAT)) {
                            vl.CONDITION = "EXISTING";
                        } else if (destination.getCdlocat().equalsIgnoreCase(
                                vl.CDLOCAT)) {
                            vl.DECOMMODITYF = vl.DECOMMODITYF + "\n**Already in: "
                                    + vl.CDLOCAT;
                        } else {
                            vl.CONDITION = "DIFFERENT LOCATION";
                            vl.DECOMMODITYF = vl.DECOMMODITYF + "\n**Found in: "
                                    + vl.CDLOCAT;
                        }
                        
                        
                        //InvItem savedInvItem = null;

                        // System.out.println
                        // ("restoreSenateTags "+vl.NUSENATE+": "+vl.CDLOCAT+"-"+vl.CDLOCTYPE);

                        // 3/15/13 BH Coded below to use InvItem Objects to
                        // display
                        // the list.

                        System.out.println("restoreSenateTags detail record:" + i+"(A12) Senate Tag#:"+vl.NUSENATE);
                       InvItem invItem = new InvItem(vl.NUSENATE,
                                vl.CDCATEGORY, jo.getString("type"),
                                vl.DECOMMODITYF, vl.CDLOCATTO);
                        invItem.setCdloctype(vl.CDLOCTYPETO);
                        invItem.setCdcategory(vl.CDCATEGORY);
                        System.out.println("restoreSenateTags detail record:" + i+"(A20) Senate Tag#:"+vl.NUSENATE);

                        // System.out.println("[BEFORE:"+vl.NUSENATE+":"+invItem.getType()+"]");

                        if (cdstatus != null && cdstatus.equalsIgnoreCase("I")) {
                            // Inactive
                            invItem.setCdstatus(cdstatus);
                            invItem.setType("INACTIVE");
                            inactivatedItems.add(invItem);
                            // System.out.println("[AFTERA:"+vl.NUSENATE+":"+invItem.getType()+"]");
                        } else if (vl.CDLOCAT == null || vl.CDLOCTYPETO == null) {
                            invItem.setType("NEW");
                            newItems.add(invItem);
                            // System.out.println("[AFTERB:"+vl.NUSENATE+":"+invItem.getType()+"]");
                        } else if (vl.CDLOCAT.equalsIgnoreCase(origin
                                .getCdlocat())
                                && vl.CDLOCTYPETO.equalsIgnoreCase(origin
                                        .getCdloctype())) {
                            invItem.setType("EXISTING");
                            scannedItems.add(invItem);
                            // System.out.println("[AFTERC:"+vl.NUSENATE+":"+invItem.getType()+"]");
                        } else {
                            scannedItems.add(invItem);
                        }

                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                    catch (NullPointerException e) {
                        System.out.println("restoreSenateTags NULLPOINTER EXCEPTION detail record :" + i+"(A20) Senate Tag#:"+vl.NUSENATE);
                        e.printStackTrace();
                        
                        // noServerResponse(nusenate);
                        return;
                    }
                }
                updateChanges();
                if (inactivatedItems.size() > 0 || newItems.size() > 0
                        || inTransitItems.size() > 0) {

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    public void autoSaveItem(InvItem curInvItem) {
        if (!this.currentlyRestoringAutosave) {
/*
 *     CREATE TABLE ad12pickupinv(
            nuxrpickuploc INTEGER PRIMARY KEY AUTOINCREMENT,
            nuxractivity INTEGER NOT NULL,   
            nusenate TEXT NOT NULL, 
            cdcond TEXT NOT NULL,
            cdcategory TEXT,
            cdintransit TEXT NOT NULL, 
            Decommodityf Text Not Null, 
            Cdlocatfrom Text Not Null, 
            Cdloctypefrom Text Not Null,  
            Cdlocatto Text Not Null, 
            cdloctypeto TEXT NOT NULL,  
            cdcommodity TEXT,
            decomments TEXT,
            dttxnorigin TEXT NOT NULL,
            natxnorguser TEXT NOT NULL, 
            dttxnupdate TEXT NOT NULL,
            Natxnupduser Text Not Null
         );    
 */
            
            try {
                ContentValues values = new ContentValues();
                values.put("nusenate", curInvItem.getNusenate());
                values.put("nuxractivity",
                        Integer.parseInt(Pickup1.nuxractivity));
                values.put("cdcond", curInvItem.getType());
                values.put("cdcategory", curInvItem.getCdcategory());
                values.put("cdintransit", curInvItem.getCdintransit());
                values.put("decommodityf", curInvItem.getDecommodityf());               
                values.put("Cdlocatfrom", curInvItem.getCdlocat());
                values.put("Cdloctypefrom", curInvItem.getCdloctype());
                values.put("Cdlocatto", destination.getCdlocat());
                values.put("cdloctypeto", destination.getCdloctype());

                values.put("cdcommodity", curInvItem.getCdcommodity());
                values.put("decomments", curInvItem.getDecomments());
                System.out.println("Autosaving " + curInvItem.getNusenate()
                        + ": " + curInvItem.getCdlocat() + "-"
                        + curInvItem.getCdloctype() + " -> "
                        + destination.getCdlocat() + "-"
                        + destination.getCdloctype()
                        + " COND:"
                        + curInvItem.getType());
                values.put("dttxnorigin", MenuActivity.invSaveDB.getNow());
                values.put("natxnorguser", LoginActivity.nauser);
                values.put("dttxnupdate", MenuActivity.invSaveDB.getNow());
                values.put("natxnupduser", LoginActivity.nauser);

                long rowsInserted = MenuActivity.invSaveDB.insert("ad12pickupinv",
                        values);

            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }


    public ArrayList<String> getSenateTagList(ArrayList<InvItem> invItemList) {
        ArrayList<String> returnList = new ArrayList<String>();

        for (int x = 0; x < invItemList.size(); x++) {
            InvItem curInvItem = invItemList.get(x);
            returnList.add(curInvItem.getNusenate());
        }

        return returnList;
    }

    public void updateChanges() {
        adapter.notifyDataSetChanged();
        pickupCount = scannedItems.size();
        pickupCountTV.setText(Integer.toString(pickupCount));
        pickedUpItemsLV.setAdapter(adapter);
        try {
            senateTagTV.setText("");
        } catch (NullPointerException e) { // TODO: when does senateTagTV not
                                           // get initialized??
            senateTagTV = (ClearableEditText) findViewById(R.id.etNusenate);
            e.printStackTrace();
        }
    }

    public void barcodeDidNotExist(final String barcode_num) {
        Log.i("TESTING", "****Senate Tag# DidNotExist MESSAGE");
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        // set title
        alertDialogBuilder.setTitle(Html
                .fromHtml("<font color='#000055'>Senate Tag#: " + barcode_num
                        + " DOES NOT EXIST IN SFMS</font>"));

        playSound(R.raw.error);
        // set dialog message
        alertDialogBuilder
                .setMessage(
                        Html.fromHtml("!!ERROR: Senate Tag#: <b>"
                                + barcode_num
                                + "</b> does not exist in SFMS. <b>This item WILL NOT be recorded as being PICKED UP!</b> "
                                + "If you physically MOVE the item please report the original location, intended new "
                                + "location and a detailed description of the item to Inventory Control Mgnt."))
                .setCancelable(false)
                .setPositiveButton(Html.fromHtml("<b>OK</b>"),
                        new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                // 5/24/13 BH Coded below to use InvItem Objects
                                // to
                                // display
                                // the list.
                                /*
                                 * VerList vl = new VerList(); vl.NUSENATE =
                                 * barcode_num; vl.CDCATEGORY = "";
                                 * vl.DECOMMODITYF =
                                 * " ***NOT IN SFMS***  New Item";
                                 * vl.CDINTRANSIT = "";
                                 * 
                                 * InvItem invItem = new InvItem(vl.NUSENATE,
                                 * vl.CDCATEGORY, "NEW", vl.DECOMMODITYF,
                                 * vl.CDLOCAT); invList.add(invItem);
                                 * 
                                 * list.add(vl); StringBuilder s_new = new
                                 * StringBuilder(); //
                                 * s_new.append(vl.NUSENATE); since the desc
                                 * coming from // server already contains
                                 * barcode number we wont add it // again //
                                 * s_new.append(" ");
                                 * s_new.append(vl.CDCATEGORY);
                                 * s_new.append(" ");
                                 * s_new.append(vl.DECOMMODITYF);
                                 * 
                                 * // display toster Context context =
                                 * getApplicationContext(); CharSequence text =
                                 * s_new; int duration = Toast.LENGTH_SHORT;
                                 * 
                                 * Toast toast = Toast.makeText(context, text,
                                 * duration); toast.setGravity(Gravity.CENTER,
                                 * 0, 0); toast.show();
                                 * 
                                 * // dispList.add(s_new); // this list will
                                 * display the // contents // on screen
                                 * scannedItems.add(invItem);
                                 * allScannedItems.add(invItem);
                                 * newItems.add(invItem);// to keep
                                 * 
                                 * list.add(vl); etNusenate.setText("");
                                 * dialog.dismiss();
                                 */
                                // if this button is clicked, just close
                                // the dialog box and do nothing
                                Context context = getApplicationContext();

                                CharSequence text = "Senate Tag#: "
                                        + barcode_num + " was NOT added";
                                int duration = Toast.LENGTH_SHORT;

                                Toast toast = Toast.makeText(context, text,
                                        duration);
                                toast.setGravity(Gravity.CENTER, 0, 0);
                                toast.show();

                                senateTagTV.setText("");

                                dialog.dismiss();

                            }
                        })
        /*
         * .setNegativeButton(Html.fromHtml("<b>No</b>"), new
         * DialogInterface.OnClickListener() {
         * 
         * @Override public void onClick(DialogInterface dialog, int id) { // if
         * this button is clicked, just close // the dialog box and do nothing
         * Context context = getApplicationContext();
         * 
         * CharSequence text = "Barcode#: " + barcode_num + " was NOT added";
         * int duration = Toast.LENGTH_SHORT;
         * 
         * Toast toast = Toast.makeText(context, text, duration);
         * toast.setGravity(Gravity.CENTER, 0, 0); toast.show();
         * 
         * etNusenate.setText("");
         * 
         * dialog.dismiss(); } })
         */;

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    public void barcodeIntransit(final VerList vl) {
        playSound(R.raw.error);
        new MsgAlert(
                this,
                "Senate Tag#: " + vl.NUSENATE + " IS ALREADY IN TRANSIT",
                "Senate Tag#: <b>"
                        + vl.NUSENATE
                        + "   "
                        + vl.DECOMMODITYF
                        + "</b> has  already been picked up and was marked as <font color='RED'><b>IN TRANSIT</b></font> and cannot be picked up.");
        senateTagTV.setText("");
    }

    public void noServerResponse(final String barcode_num) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        // set title
        alertDialogBuilder.setTitle(Html
                .fromHtml("<font color='#000055'>Senate Tag#: " + barcode_num
                        + " DOES NOT EXIST IN SFMS</font>"));
        playSound(R.raw.error);
        // set dialog message
        alertDialogBuilder
                .setMessage(
                        Html.fromHtml("!!ERROR: There was <font color='RED'><b>NO SERVER RESPONSE</b></font>. Senate Tag#:<b>"
                                + barcode_num
                                + "</b> will be <b>IGNORED</b>.<br/> Please contact STS/BAC."))
                .setCancelable(false)
                .setPositiveButton(Html.fromHtml("<b>Ok</b>"),
                        new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                // if this button is clicked, just close
                                // the dialog box and do nothing
                                Context context = getApplicationContext();

                                CharSequence text = "Senate Tag#: "
                                        + barcode_num + " was NOT added";
                                int duration = Toast.LENGTH_SHORT;

                                Toast toast = Toast.makeText(context, text,
                                        duration);
                                toast.setGravity(Gravity.CENTER, 0, 0);
                                toast.show();

                                senateTagTV.setText("");

                                dialog.dismiss();
                            }
                        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    public void errorMessage(final String barcode_num, final String title,
            final String message) {
        Log.i("TESTING", "****errorMessgae MESSAGE");
        playSound(R.raw.error);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        // set title
        alertDialogBuilder.setTitle(Html.fromHtml("<font color='#000055'>"
                + title + "</font>"));

        // set dialog message
        alertDialogBuilder
                .setMessage(Html.fromHtml(message))
                .setCancelable(false)
                .setPositiveButton(Html.fromHtml("<b>Ok</b>"),
                        new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                // if this button is clicked, just close
                                // the dialog box and do nothing
                                Context context = getApplicationContext();

                                CharSequence text = "Senate Tag#: "
                                        + barcode_num + " was NOT added";
                                int duration = Toast.LENGTH_SHORT;

                                Toast toast = Toast.makeText(context, text,
                                        duration);
                                toast.setGravity(Gravity.CENTER, 0, 0);
                                toast.show();

                                senateTagTV.setText("");

                                dialog.dismiss();
                            }
                        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    public int findBarcode(String barcode_num) {
        for (int x = 0; x < scannedItems.size(); x++) {
            if (scannedItems.get(x).getNusenate().equals(barcode_num)) {
                return x;
            }
        }
        return -1;
    }

    public ArrayList<String> getJSONArrayList(ArrayList<InvItem> invList) {
        ArrayList<String> returnArray = new ArrayList<String>();
        if (invList != null) {
            for (int x = 0; x < invList.size(); x++) {
                returnArray.add(invList.get(x).toJSON());
            }
        }

        return returnArray;
    }

    public ArrayList<InvItem> getInvItemArrayList(ArrayList<String> invList) {
        ArrayList<InvItem> returnArray = new ArrayList<InvItem>();
        if (invList != null) {
            for (int x = 0; x < invList.size(); x++) {
                String curInvJson = invList.get(x);
                InvItem currentInvItem = new InvItem();
                currentInvItem.parseJSON(curInvJson);
                returnArray.add(currentInvItem);
            }
        }

        return returnArray;
    }

    // 3/15/13 Work in progress. Not fully implemented yet
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        // Save UI state changes to the savedInstanceState.
        // This bundle will be passed to onCreate if the process is
        // killed and restarted.
        savedInstanceState.putString("savedOriginLoc", origin.getAdstreet1());
        savedInstanceState
                .putString("savedDestLoc", destination.getAdstreet1());
        savedInstanceState.putStringArrayList("savedScannedItems",
                getJSONArrayList(scannedItems));
    }

    // 3/15/13 Work in progress. Not fully implemented yet
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // Restore UI state from the savedInstanceState.
        // This bundle has also been passed to onCreate.
        origin.setAdstreet1(savedInstanceState.getString("savedOriginLoc"));
        scannedItems = getInvItemArrayList(savedInstanceState
                .getStringArrayList("savedScannedItems"));
        TextView TextView2 = (TextView) findViewById(R.id.textView2);
        TextView2.setText("Origin : " + origin.getAdstreet1() + "\n"
                + "Destination : " + destination.getAdstreet1());

    }

    public void continueButton(View view) {
        Log.i("continueButton", "BEFORE checkServerResponse(true)");

        if (scannedItems.size() < 1) {
            CharSequence text = "!!ERROR: You must first scan an item to pickup";
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(this, text, duration);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
            return;
        }

        if (checkServerResponse(true) == OK) {
            progBarPickup2.setVisibility(View.VISIBLE);
            continueBtn.getBackground().setAlpha(70);
            Intent intent = new Intent(this, Pickup3.class);
            Transaction trans = new Transaction();
            trans.setOrigin(origin);
            trans.setDestination(destination);
            trans.setPickupItems(scannedItems);
            intent.putExtra("pickup", trans.toJson());
            startActivity(intent);
            overridePendingTransition(R.anim.in_right, R.anim.out_left);
        }
    }

    public void cancelButton(View view) {
        Log.i("cancelButton", "BEFORE checkServerResponse(true)");
        if (checkServerResponse(true) == OK) {
            cancelBtn.getBackground().setAlpha(70);
            finish();
            overridePendingTransition(R.anim.in_left, R.anim.out_right);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_pickup2, menu);
        return true;
    }

    public class VerList
    {
        String NUSENATE;
        String CDCATEGORY;
        String DECOMMODITYF;
        String CDLOCATTO;
        String CDLOCTYPETO;
        String ADSTREET1;
        String DTISSUE;
        String CDLOCAT;
        String CDINTRANSIT;
        String CONDITION;
        String CDSTATUS;
    }

    @Override
    public void startTimeout(int timeoutType) {
        Intent intentTimeout = new Intent(this, LoginActivity.class);
        intentTimeout.putExtra("TIMEOUTFROM", timeoutFrom);
        startActivityForResult(intentTimeout, timeoutType);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
        case ITEMDETAILS_TIMEOUT:
            if (resultCode == RESULT_OK) {
                if (getItemDetails() != SERVER_SESSION_TIMED_OUT) {
                    updateChanges();
                }
            }
            break;
        }
    }

    public int getItemDetails() {
        String barcode_num = senateTagTV.getText().toString().trim();
        String barcode_number = barcode_num;
        VerList vl = new VerList();

        // check network connection
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            // fetch data
            status = "yes";
            // int barcode= Integer.parseInt(barcode_num);
            // scannedItems.add(barcode);
            // Get the URL from the properties
            URL = LoginActivity.properties.get("WEBAPP_BASE_URL").toString();

            AsyncTask<String, String, String> resr1 = new RequestTask()
                    .execute(URL + "/ItemDetails?barcode_num=" + barcode_num);
            System.out.println("URL CALL:" + URL + "/ItemDetails?barcode_num="
                    + barcode_num);
            try {
                res = null;
                res = resr1.get().trim().toString();
                if (testResNull) { // Testing Purposes Only
                    res = null;
                    resr1 = null;
                    Log.i("TEST RESNULL", "RES SET TO NULL");
                }
                System.out.println("URL RESULT:" + res);

                // add it to list and displist and scanned items
                JSONObject object = null;
                if (res == null) {
                    // Log.i("TESTING", "A CALL noServerResponse");
                    noServerResponse(barcode_num);
                    return NO_SERVER_RESPONSE;
                } else if (res.toUpperCase().contains(
                        "DOES NOT EXIST IN SYSTEM")) {
                    // Log.i("TESTING",
                    // "A CALL barcodeDidNotExist");
                    barcodeDidNotExist(barcode_num);
                    return SENTAG_NOT_FOUND;
                } else if (res.indexOf("Session timed out") > -1) {
                    startTimeout(ITEMDETAILS_TIMEOUT);
                    return SERVER_SESSION_TIMED_OUT;
                } else {
                    try {
                        object = (JSONObject) new JSONTokener(res).nextValue();
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                    vl.NUSENATE = barcode_number;
                    vl.CDCATEGORY = object.getString("cdcategory");
                    vl.DECOMMODITYF = object.getString("decommodityf")
                            .replaceAll("&#34;", "\"");
                    vl.CDLOCATTO = object.getString("cdlocatto");
                    vl.CDLOCTYPETO = object.getString("cdloctypeto");
                    vl.ADSTREET1 = object.getString("adstreet1to").replaceAll(
                            "&#34;", "\"");
                    vl.DTISSUE = object.getString("dtissue");
                    vl.CDLOCAT = object.getString("cdlocatto");
                    vl.CDINTRANSIT = object.getString("cdintransit");
                    vl.CDSTATUS = object.getString("cdstatus");

                    if (vl.CDSTATUS.equalsIgnoreCase("I")) {
                        errorMessage(
                                barcode_num,
                                "Senate Tag#: " + barcode_num
                                        + " has been Inactivated.",
                                "!!ERROR: Senate Tag#: <b>"
                                        + barcode_num
                                        + "</b>"
                                        + " has been Inactivated.<br><br>"
                                        + " <b>This item will not be recorded as being picked up!</b><br><br>"
                                        + " The <b>\""
                                        + vl.DECOMMODITYF
                                        + "\"</b>  must be brought back into the Senate Tracking System by management via the"
                                        + " \"Inventory Record Adjustment E/U\". If you physically MOVE the item please report "
                                        + " Tag# and new location to Inventory Control Mgnt.");
                        return INACTIVE_SENTAG;
                    }

                    if (vl.CDINTRANSIT != null
                            && vl.CDINTRANSIT.equalsIgnoreCase("Y")) {
                        barcodeIntransit(vl);
                        return SENTAG_IN_TRANSIT;
                    }

                    if (origin.getCdlocat().equalsIgnoreCase(vl.CDLOCAT)) {
                        vl.CONDITION = "EXISTING";
                        playSound(R.raw.ok);
                    } else if (destination.getCdlocat().equalsIgnoreCase(
                            vl.CDLOCAT)) {
                        playSound(R.raw.honk);
                        vl.DECOMMODITYF = vl.DECOMMODITYF + "\n**Already in: "
                                + vl.CDLOCAT;
                    } else {
                        playSound(R.raw.warning);
                        vl.CONDITION = "DIFFERENT LOCATION";
                        vl.DECOMMODITYF = vl.DECOMMODITYF + "\n**Found in: "
                                + vl.CDLOCAT;
                    }
                }

            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (ExecutionException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            status = "yes1";
        } else {
            // display error
            status = "no";
        }
        String invStatus;

        if (vl.CDLOCATTO == null || vl.CDLOCATTO.trim().length() == 0) {
            invStatus = "NOT IN SFMS";
        }
        // This is what should be expected. Trying to move the
        else if (vl.CDLOCATTO.equalsIgnoreCase(origin.getCdlocat())) {
            invStatus = vl.CONDITION;
        } else if (vl.CDLOCATTO.equalsIgnoreCase(destination.getCdlocat())) { //
            invStatus = "AT DESTINATION";
        } else {
            invStatus = "Found in: " + vl.CDLOCATTO;
        }

        // 5/24/13 BH Coded below to use InvItem Objects to display
        // the list.
        InvItem invItem = new InvItem(vl.NUSENATE, vl.CDCATEGORY, invStatus,
                vl.DECOMMODITYF, vl.CDLOCAT);
        StringBuilder s_new = new StringBuilder();
        // s_new.append(vl.NUSENATE); since the desc coming from
        // server already contains barcode number we wont add it
        // again
        // s_new.append(" ");
        s_new.append(vl.CDCATEGORY);
        s_new.append(" ");
        s_new.append(vl.DECOMMODITYF);

        // display toster
        Context context = getApplicationContext();
        CharSequence text = s_new;
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, text, duration);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();

        scannedItems.add(invItem);
        autoSaveItem(invItem);
        return OK;
    }

}
