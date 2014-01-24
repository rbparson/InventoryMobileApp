package gov.nysenate.inventory.activity;

import gov.nysenate.inventory.db.sqlite.SQLiteDatabaseHelper;
import gov.nysenate.inventory.util.VersionUtils;
import gov.nysenate.inventory.model.AutoSaveItem;
import gov.nysenate.inventory.android.AutosaveFoundDialog;

import gov.nysenate.inventory.adapter.CustomListViewAdapter;
import gov.nysenate.inventory.android.InvApplication;
import gov.nysenate.inventory.android.R;
import gov.nysenate.inventory.listener.AutosaveFoundDialogListener;
import gov.nysenate.inventory.model.RowItem;
import gov.nysenate.inventory.util.Toasty;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v4.app.NavUtils;
import android.text.Html;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

public class MenuActivity extends SenateActivity implements
        OnItemClickListener, AutosaveFoundDialogListener
{
    private static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;

    private ListView mList;

    public String res = null;

    String URL = ""; // this will be initialized once in onCreate() and used for
    // all server calls.

    public static String[] titles;

    public static String[] descriptions = new String[] {
            "Scan an item and show information",
            "Perform Inventory Verification for a Senate Location",
            "Move Items from one location to another", "Logout of this UserID" };

    public static Integer[] images;

    static SQLiteDatabaseHelper invSaveDB;

    Set<String> autosaveTypes = new HashSet<String>();
    ArrayList<AutoSaveItem> autosaveList = new ArrayList<AutoSaveItem>();
    AutosaveFoundDialog autosaveFoundDialog;

    ListView listView;
    List<RowItem> rowItems;

    android.app.FragmentManager fragmentManager = this.getFragmentManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        registerBaseActivityReceiver();

        // Does user have access to Verification
        InvApplication app = ((InvApplication) getApplicationContext());
        int cdseclevel = app.getCdseclevel();
        if (cdseclevel == 1) {
            titles = new String[] { "Search", "Verification", "Move Items",
                    "Logout" };
            images = new Integer[] { R.drawable.ssearch, R.drawable.sverify,
                    R.drawable.smove, R.drawable.slogout };
        } else {
            titles = new String[] { "Search", "Move Items", "Logout" };
            images = new Integer[] { R.drawable.ssearch, R.drawable.smove,
                    R.drawable.slogout };
        }

        rowItems = new ArrayList<RowItem>();
        for (int i = 0; i < titles.length; i++) {
            RowItem item = new RowItem(images[i], titles[i]);
            rowItems.add(item);
        }

        listView = (ListView) findViewById(R.id.list);
        CustomListViewAdapter adapter = new CustomListViewAdapter(this,
                R.layout.list_item, rowItems);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);

        // Setup Local Database
        try {
            System.out.println("BEFORE INVSAVEDB");
            this.invSaveDB = new SQLiteDatabaseHelper(getApplicationContext(),
                    "invSaveDB", null,
                    VersionUtils.getVersionCode(getApplicationContext()));

            // this.invSaveDB.resetDatabase(this.invSaveDB.getWritableDatabase());

            System.out.println("BEFORE REAL CHECK");
            Cursor cursor = this.invSaveDB
                    .rawQuery(
                              " SELECT a.nuxractivity, a.naactivity, a.nuxracttype, a.dttxnorigin, a.dttxnupdate, b.deacttype, c.cdlocat, c.cdloctype, c.cdrespctrhd, c.adstreet1, c.adcity, c.adstate, c.adzipcode, c.descript, c.locationEntry, c.locationToEntry FROM AM12ACTIVITY a, AL112ACTTYPE b, AM12VERIFY c  WHERE a.natxnorguser = ? AND a.nuxracttype = b.nuxracttype AND c.nuxractivity = a.nuxractivity"
                            + " UNION"
                            + " SELECT a.nuxractivity, a.naactivity, a.nuxracttype, a.dttxnorigin, a.dttxnupdate, b.deacttype, c.cdlocatto, c.cdloctypeto, c.cdrespctrhdto, c.adstreet1to, c.adcityto, c.adstateto, c.adzipcodeto, c.descriptfrom, c.locationtoEntry, c.locationToEntry FROM AM12ACTIVITY a, AL112ACTTYPE b, AM12PICKUP c  WHERE a.natxnorguser = ? AND a.nuxracttype = b.nuxracttype AND c.nuxractivity = a.nuxractivity"
                            ,
                            new String[] { LoginActivity.nauser, LoginActivity.nauser });
            // Cursor cursor =
            // this.invSaveDB.rawQuery("SELECT a.nuxractivity, a.naactivity, a.nuxracttype, a.dttxnorigin, a.dttxnupdate, b.deacttype FROM AM12ACTIVITY a, AL112ACTTYPE b WHERE a.natxnorguser = ? AND a.nuxracttype = b.nuxracttype",
            // new String[]{LoginActivity.nauser});
            // looping through all rows and adding to list
            if (cursor != null && cursor.moveToFirst()) {
                do {

                    System.out.print("**********AUTOSAVE CURSOR: ");
                    for (int x = 1; x < cursor.getColumnCount(); x++) {
                        if (x > 1) {
                            System.out.print(", ");
                        }
                        System.out.print("(COLUMN:"+x+")"+cursor.getString(x));
                    }
                    
                    String deactype = cursor.getString(5);
                    AutoSaveItem currentAutoSave = new AutoSaveItem();
                    currentAutoSave.setNuxractivity(cursor.getInt(0));
                    currentAutoSave.SetNaactivity(cursor.getString(1));
                    currentAutoSave.setNuxracttype(cursor.getInt(2));
                    currentAutoSave.setDeactype(deactype);
                    currentAutoSave.setCdlocat(cursor.getString(6));
                    currentAutoSave.setCdloctype(cursor.getString(7));
                    currentAutoSave.setCdrespctrhd(cursor.getString(8));
                    currentAutoSave.setAdstreet1(cursor.getString(9));
                    currentAutoSave.setAdcity(cursor.getString(10));
                    currentAutoSave.setAdstate(cursor.getString(11));
                    currentAutoSave.setAdzipcode(cursor.getString(12));
                    currentAutoSave.setDescript(cursor.getString(13));
                    currentAutoSave.setLocationEntry(cursor.getString(14));
                    currentAutoSave.setLocationToEntry(cursor.getString(15));
                    System.out.println("AutoSaveLoop: deactype:"+deactype+", nuxractivity:"+currentAutoSave.getNuxractivity() + ", naactivity:"
                            + currentAutoSave.getNaactivity() + ", descript:"
                            + currentAutoSave.getDescript());
                    System.out.println("AutoSaveLoop: Street1:"+currentAutoSave.getAdstreet1());

                    try {
                        currentAutoSave.setDttxnorigin(invSaveDB.getDate(cursor
                                .getString(3)));
                    } catch (ParseException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    try {
                        currentAutoSave.setDttxnupdate(invSaveDB.getDate(cursor
                                .getString(4)));
                    } catch (ParseException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    autosaveList.add(currentAutoSave);
                    autosaveTypes.add(deactype);

                } while (cursor.moveToNext());
            }

            System.out.println("BEFORE TEST COUNT");

            Cursor cursorCnt = this.invSaveDB.rawQuery(
                    "SELECT COUNT(*)FROM AM12PICKUP c ", null);
            if (cursorCnt != null && cursorCnt.moveToFirst()) {
                do {
                    System.out.println("AM12PICKUP COUNT: "
                            + cursorCnt.getInt(0));
                } while (cursorCnt.moveToNext());
            }

            System.out.println("BEFORE TEST AM12VERIFY SELECT");

            Cursor cursorTest = this.invSaveDB
                    .rawQuery(
                            "SELECT * FROM AM12PICKUP a ",
                            null);
            if (cursorTest != null && cursorTest.moveToFirst()) {
                do {
                    System.out.println("AM12PICKUP: " +cursorTest.getColumnName(0) +":" + cursorTest.getString(0)
                            + ", " + cursorTest.getColumnName(1) +":" + cursorTest.getString(1) + ", "
                            + cursorTest.getColumnName(2) +":" +cursorTest.getString(2) + ", "
                            + cursorTest.getColumnName(3) +":" +cursorTest.getString(3) + ", "
                            + cursorTest.getColumnName(4) +":" +cursorTest.getString(4));
                } while (cursorTest.moveToNext());
            }

            cursorTest = this.invSaveDB
                    .rawQuery(
                            "SELECT * FROM AM12ACTIVITY a ",
                            null);
            if (cursorTest != null && cursorTest.moveToFirst()) {
                do {
                    System.out.println("AM12ACTIVITY: " +cursorTest.getColumnName(0) +":" + cursorTest.getString(0)
                            + ", " +cursorTest.getColumnName(1) +":" + cursorTest.getString(1) + ", "
                            + cursorTest.getColumnName(2) +":" + cursorTest.getString(2) + ", "
                            + cursorTest.getColumnName(3) +":" + cursorTest.getString(3) + ", "
                            + cursorTest.getColumnName(4) +":" + cursorTest.getString(4));
                } while (cursorTest.moveToNext());
            }
            cursorTest = this.invSaveDB
                    .rawQuery(
                            "SELECT * FROM AL112ACTTYPE a ",
                            null);
            if (cursorTest != null && cursorTest.moveToFirst()) {
                do {
                    System.out.println("AL112ACTTYPE: " +cursorTest.getColumnName(0) +":" + cursorTest.getString(0)
                            + ", " +cursorTest.getColumnName(1) +":" + cursorTest.getString(1));
                } while (cursorTest.moveToNext());
            }
            cursorTest = this.invSaveDB
                    .rawQuery(
                            "SELECT * FROM ad12pickupinv a ",
                            null);
            if (cursorTest != null && cursorTest.moveToFirst()) {
                do {
                    System.out.println("ad12pickupinv: " +cursorTest.getColumnName(0) +":" + cursorTest.getString(0)
                            + ", " +cursorTest.getColumnName(1) +":" + cursorTest.getString(1) + ", "
                            + cursorTest.getColumnName(2) +":" + cursorTest.getString(2) + ", "
                            + cursorTest.getColumnName(3) +":" + cursorTest.getString(3) + ", "
                            + cursorTest.getColumnName(4) +":" + cursorTest.getString(4));
                    } while (cursorTest.moveToNext());
            }
            
            cursorTest = this.invSaveDB
                    .rawQuery(
                            " SELECT a.nuxractivity, a.naactivity, a.nuxracttype, a.dttxnorigin, a.dttxnupdate, b.deacttype, c.cdlocatto, c.cdloctypeto, c.cdrespctrhdto, c.adstreet1to, c.adcityto, c.adstateto, c.adzipcodeto, c.descriptto, c.locationtoEntry, c.locationToEntry FROM AM12ACTIVITY a, AL112ACTTYPE b, AM12PICKUP c  WHERE a.natxnorguser = ? AND a.nuxracttype = b.nuxracttype AND c.nuxractivity = a.nuxractivity"
                            ,
                            null);
            if (cursorTest != null && cursorTest.moveToFirst()) {
                do {
                    System.out.println("TEST Pickup Data: " +cursorTest.getColumnName(0) +":" + cursorTest.getString(0)
                            + ", " +cursorTest.getColumnName(1) +":" + cursorTest.getString(1) + ", "
                            + cursorTest.getColumnName(2) +":" + cursorTest.getString(2) + ", "
                            + cursorTest.getColumnName(3) +":" + cursorTest.getString(3) + ", "
                            + cursorTest.getColumnName(4) +":" + cursorTest.getString(4));
                    } while (cursorTest.moveToNext());
            }

            
            
            cursorTest = this.invSaveDB
                    .rawQuery(
                            " SELECT COUNT(*) FROM AM12ACTIVITY a, AL112ACTTYPE b, AM12PICKUP c  WHERE a.natxnorguser = ? AND a.nuxracttype = b.nuxracttype AND c.nuxractivity = a.nuxractivity"
                            ,
                            new String[] { LoginActivity.nauser });
            if (cursorTest != null && cursorTest.moveToFirst()) {
                do {
                    System.out.println("TEST Pickup Data COUNT: " + cursorTest.getInt(0));
                    } while (cursorTest.moveToNext());
            }
            
            // this.invSaveDB.onCreate(this.invSaveDB.getWritableDatabase()); //
            // Run one time

        } catch (NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println("AFTER TEST SELECT autosaveList.size():"+autosaveList.size());

        if (autosaveList.size() > 0) {
            askUserAboutAutoSave();
        }

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
            long id) {
        RowItem curRow = rowItems.get(position);
        if (curRow.getTitle().equalsIgnoreCase("Search")) {
            if (checkServerResponse(true) == OK) {
                this.search(view);
            }
        } else if (curRow.getTitle().equalsIgnoreCase("Verification")) {
            if (checkServerResponse(true) == OK) {
                this.verify(view);
            }
        } else if (curRow.getTitle().equalsIgnoreCase("Move Items")) {
            this.addItem(view);
        } else if (curRow.getTitle().equalsIgnoreCase("Logout")) {
            // Log.i("MENU LOGOUT", "Calling logout");
            this.logout(view);
        }

        /*
         * Toast toast = Toast.makeText(getApplicationContext(), "TEST Item " +
         * (position + 1) + ": " + rowItems.get(position), Toast.LENGTH_SHORT);
         * toast.setGravity(Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL, 0, 0);
         * toast.show();
         */
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
        case R.id.menu_clear_autosave:
            clearAutoSaves();
            return true;
        case android.R.id.home:
            final Activity currentActivity = this;
            // 1. Instantiate an AlertDialog.Builder with its constructor
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            // 2. Chain together various setter methods to set the dialog
            // characteristics
            builder.setTitle(Html
                    .fromHtml("<font color='#000055'>Log Out</font>"));
            builder.setMessage("Do you really want to log out?");
            // Add the buttons
            builder.setPositiveButton(Html.fromHtml("<b>Yes</b>"),
                    new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            backToParent();
                        }
                    });
            builder.setNegativeButton(Html.fromHtml("<b>No</b>"),
                    new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog
                        }
                    });

            // 3. Get the AlertDialog from create()
            AlertDialog dialog = builder.create();
            dialog.show();

            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    public void backToParent() {
        Toasty.displayCenteredMessage(this, "Logging Out", Toast.LENGTH_SHORT);
        NavUtils.navigateUpFromSameTask(this);
        finish();
        overridePendingTransition(R.anim.in_left, R.anim.out_right);
    }

    public void search(View view) {

        Intent intent = new Intent(this, SearchActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.in_right, R.anim.out_left);
        // overridePendingTransition(R.anim.slide_in_left,
        // R.anim.slide_out_left);

    }

    public void addItem(View view) {
        Intent intent = new Intent(this, Move.class);
        startActivity(intent);
        overridePendingTransition(R.anim.in_right, R.anim.out_left);

        // overridePendingTransition(R.anim.slide_in_left,
        // R.anim.slide_out_left);
    }

    public void verify(View view) {
        verify(view, null);
    }

    public void verify(View view, AutoSaveItem autoSaveItem) {
        Intent intent = new Intent(this, Verification.class);
        if (autoSaveItem != null) {
            System.out.println("****MAINACTIVITY: Passing autosave");
            intent.putExtra("autosave", autoSaveItem);
        }
        startActivity(intent);
        overridePendingTransition(R.anim.in_right, R.anim.out_left);
        // overridePendingTransition(R.anim.slide_in_left,
        // R.anim.slide_out_left);

    }

    public void pickup(View view) {
        pickup(view, null);
    }

    public void pickup(View view, AutoSaveItem autoSaveItem) {
        Intent intent = new Intent(this, Pickup1.class);
        if (autoSaveItem != null) {
            System.out.println("****MAINACTIVITY: Passing autosave");
            intent.putExtra("autosave", autoSaveItem);
        }
        startActivity(intent);
        overridePendingTransition(R.anim.in_right, R.anim.out_left);
    }
    
    
    public void location(View view) {
        Intent intent = new Intent(this, LocationActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.in_right, R.anim.out_left);

        // overridePendingTransition(R.anim.slide_in_left,
        // R.anim.slide_out_left);
        // we are passing the intent to the activity again ,
        // see instead if we can restart the activity to
        // avoid the data being passed to the login activity
    }

    public void noServerResponse() {
        noServerResponse(null);
    }

    public void noServerResponse(final String barcode_num) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        StringBuilder title = new StringBuilder();
        title.append("<font color='#000055'>");
        if (barcode_num != null && barcode_num.trim().length() > 0) {
            title.append("Barcode#: ");
            title.append(barcode_num);
            title.append(" ");
        }
        title.append("NO SERVER RESPONSE");
        title.append("</font>");

        StringBuilder msg = new StringBuilder();
        msg.append("!!ERROR: There was <font color='RED'><b>NO SERVER RESPONSE</b></font>.");
        if (barcode_num != null && barcode_num.trim().length() > 0) {
            msg.append(" Senate Tag#:<b>");
            msg.append(barcode_num);
            msg.append("</b> will be <b>IGNORED</b>.");
        }
        msg.append("<br/> Please contact STS/BAC.");

        // set title
        alertDialogBuilder.setTitle(Html.fromHtml(title.toString()));

        // set dialog message
        alertDialogBuilder
                .setMessage(Html.fromHtml(msg.toString()))
                .setCancelable(false)
                .setPositiveButton(Html.fromHtml("<b>Ok</b>"),
                        new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                // if this button is clicked, just close
                                // the dialog box and do nothing
                                if (barcode_num != null
                                        && barcode_num.trim().length() > 0) {
                                    Context context = getApplicationContext();

                                    CharSequence text = "Senate Tag#: "
                                            + barcode_num + " was NOT added";
                                    int duration = Toast.LENGTH_SHORT;

                                    Toast toast = Toast.makeText(context, text,
                                            duration);
                                    toast.setGravity(Gravity.CENTER, 0, 0);
                                    toast.show();
                                }
                                ;
                                dialog.dismiss();
                            }
                        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    public void askUserAboutAutoSave() {
        final Activity currentActivity = this;
        // 1. Instantiate an AlertDialog.Builder with its constructor
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // 2. Chain together various setter methods to set the dialog
        // characteristics
        String plural = "";
        String haveHas = "has";

        if (autosaveList.size() > 1) {
            plural = "s";
            haveHas = "have";
        }

        builder.setTitle(Html.fromHtml("<font color='#000055'>Autosave"
                + plural + "</font>"));
        builder.setMessage("***WARNING: Found "
                + autosaveList.size()
                + " Autosave"
                + plural
                + " that "
                + haveHas
                + " not been committed. Would you like to restore an Autosave? (Y/N)");
        // Add the buttons
        builder.setPositiveButton(Html.fromHtml("<b>Yes</b>"),
                new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        choseAutoSaveToRestore(
                                "Restore Autosave",
                                "Please select a type of Autosave then select an autosave to restore. Click on the OK button to restore or cancel to cancel the restore.");
                    }
                });
        builder.setNegativeButton(Html.fromHtml("<b>No</b>"),
                new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });

        // 3. Get the AlertDialog from create()
        AlertDialog dialog = builder.create();
        dialog.show();

    }

    public void choseAutoSaveToRestore(String title, String message) {
        autosaveFoundDialog = new AutosaveFoundDialog(this, title, message,
                autosaveTypes, autosaveList, null);
        autosaveFoundDialog.addListener(this);
        autosaveFoundDialog.setRetainInstance(true);
        autosaveFoundDialog.show(fragmentManager, "fragment_name");
        // AutosaveListViewAdapter lvAutosavesAdapter = new
        // AutosaveListViewAdapter(this, R.layout.row_autosave,
        // autosaveTypes.toArray()[0].toString(), autosaveList);
        // CommodityListViewAdapter lvTestAdapter = new
        // CommodityListViewAdapter(this, R.layout.commoditylist_row,
        // autosaveFoundDialog.testList, null);
        // autosaveFoundDialog.lvTestAdapter = lvTestAdapter;
        // autosaveFoundDialog.lvAutosavesAdapter =lvAutosavesAdapter;
        /*
         * if (autosaveFoundDialog.lvAutosaves==null) {
         * System.out.println("autosaveFoundDialog.lvAutosaves IS NULL"); } else
         * if (lvAutosavesAdapter==null) {
         * System.out.println("lvAutosavesAdapter IS NULL"); } else {
         * autosaveFoundDialog.lvAutosaves.setAdapter(lvAutosavesAdapter);
         * autosaveFoundDialog.lvTest.setAdapter(lvTestAdapter); }
         */

    }

    public void clearAutoSaves() {
        clearAutoSaves(null);
    }

    public void clearAutoSaves(View v) {
        this.invSaveDB.resetDatabase(this.invSaveDB.getWritableDatabase());
    }

    public String stripHtml(String html) {
        return Html.fromHtml(html).toString();
    }

    /*
     * 
     * Testing Code above
     */

    public void logout(View view) {
        final Intent intent = new Intent(this, LoginActivity.class);

        // 1. Instantiate an AlertDialog.Builder with its constructor
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // 2. Chain together various setter methods to set the dialog
        // characteristics
        builder.setTitle(Html.fromHtml("<font color='#000055'>Log out</font>"));
        builder.setMessage("Do you really want to log out?");

        // Add the buttons
        builder.setPositiveButton(Html.fromHtml("<b>Yes</b>"),
                new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                        overridePendingTransition(R.anim.in_left,
                                R.anim.out_right);
                        Toast toast = Toast.makeText(getApplicationContext(),
                                "logging out", Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();
                    }
                });
        builder.setNegativeButton(Html.fromHtml("<b>No</b>"),
                new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });

        // 3. Get the AlertDialog from create()
        AlertDialog dialog = builder.create();

        dialog.show();
    }

    @Override
    public void onBackPressed() {
        final Activity currentActivity = this;
        // 1. Instantiate an AlertDialog.Builder with its constructor
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // 2. Chain together various setter methods to set the dialog
        // characteristics
        builder.setTitle(Html.fromHtml("<font color='#000055'>Log Out</font>"));
        builder.setMessage("Do you really want to log out?");
        // Add the buttons
        builder.setPositiveButton(Html.fromHtml("<b>Yes</b>"),
                new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        backToParent();
                    }
                });
        builder.setNegativeButton(Html.fromHtml("<b>No</b>"),
                new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });

        // 3. Get the AlertDialog from create()
        AlertDialog dialog = builder.create();
        dialog.show();

    }

    @Override
    public void restoreAutoSave(AutoSaveItem autoSave) {
        if (autoSave.getDeactype().equalsIgnoreCase("VERIFICATION")) {
            System.out.println("************CALLING RESTORED VERIFICATION");
            verify(null, autoSave);
        }
        else if (autoSave.getDeactype().equalsIgnoreCase("PICKUP")) {
            System.out.println("************CALLING RESTORED PICKUP");
            pickup(null, autoSave);
        }

    }

    /**
     * Fire an intent to start the speech recognition activity.
     */
    private void startVoiceRecognitionActivity() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                "Speech recognition demo");
        startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
    }

    @Override
    public void onAutosaveOKButtonClicked(String deacttype, int nuxracttype,
            String naactivity, int nuxractivity) {
        // TODO Auto-generated method stub

    }

}
