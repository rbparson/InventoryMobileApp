package gov.nysenate.inventory.adapter;

import gov.nysenate.inventory.activity.SenateActivity;
import gov.nysenate.inventory.android.AutosaveFoundDialog;
import gov.nysenate.inventory.android.R;
import gov.nysenate.inventory.listener.OnItemDoubleTapListener;
import gov.nysenate.inventory.model.AutoSaveItem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class AutosaveListViewAdapter extends ArrayAdapter<AutoSaveItem>
        implements OnItemDoubleTapListener
{

    Context context;
    SenateActivity senateActivity;
    AutosaveFoundDialog autosaveFoundDialog;
    List<AutoSaveItem> allItems;
    List<AutoSaveItem> items = null;
    AutoSaveItem currentAutoSave = null;
    String autosaveType;
    int rowSelected = -1;

    public AutosaveListViewAdapter(Context context,
            SenateActivity senateActivity, int resourceId, String autosaveType,
            List<AutoSaveItem> items, AutosaveFoundDialog autosaveFoundDialog) {
        super(context, resourceId, items);
        this.context = context;
        this.allItems = items;
        this.senateActivity = senateActivity;
        this.autosaveFoundDialog = autosaveFoundDialog;
        // this.items = allItems;
        filterOn(autosaveType);
    }

    /* private view holder class */
    private class ViewHolder
    {
        RelativeLayout rlAutosaveRow;
        // TextView commodityListNucnt;
        TextView tvDttxnorigin;
        TextView tvCdlocat;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        AutoSaveItem rowItem = null;
        if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            convertView = mInflater.inflate(R.layout.row_autosave, null);
            holder = new ViewHolder();
            holder.rlAutosaveRow = (RelativeLayout) convertView
                    .findViewById(R.id.rlAutosaveRow);
            holder.tvDttxnorigin = (TextView) convertView
                    .findViewById(R.id.tvDttxnorigin);
            holder.tvCdlocat = (TextView) convertView
                    .findViewById(R.id.tvCdlocat);
            holder.tvDttxnorigin.setFocusable(false);
            holder.tvCdlocat.setFocusable(false);
            /*
             * if ( holder.tvDttxnorigin == null) { System.out.println
             * ("holder.tvDttxnorigin IS STILL NULL (A) "); }
             */

            /*
             * holder.commodityListNucnt = (TextView) convertView
             * .findViewById(R.id.commodityListNucnt);
             */
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        if (position > -1 && items != null && position < items.size()) {
            convertView.setEnabled(true);
            convertView.setFocusable(true);
            if (convertView.isEnabled()) {
                System.out.println("  " + position + ": IS ENABLED");
            } else {
                System.out.println("  !!!!!" + position + ": IS NOT ENABLED");
            }

            rowItem = items.get(position);

            // holder.commodityListNucnt.setText(rowItem.getNucnt());
            SimpleDateFormat sdf = new SimpleDateFormat("mm/dd/yy hh:MM:ss a");
            String dttxnoriginCur = "N/A";
            try {
                dttxnoriginCur = sdf.format(rowItem.getDttxnorigin());
                // System.out.println(position+" dttxnoriginCur:"+dttxnoriginCur+" FROM "+rowItem.getDttxnorigin());
            } catch (Exception e) {
                e.printStackTrace();
            }

            /*
             * if (holder==null) { System.out.println ("holder is null"); } else
             * if ( holder.tvDttxnorigin == null) {
             * 
             * System.out.println ("holder.tvDttxnorigin is null"); } else if
             * (dttxnoriginCur==null) { System.out.println
             * ("dttxnoriginCur is null"); } else {
             */
            holder.tvDttxnorigin.setText(dttxnoriginCur);
            // }
            System.out.println("autosaveListadapter "+position+":"+rowItem.getNuxractivity() + ", location:"
                    + rowItem.getCdlocat() + "-" + rowItem.getCdloctype()
                    + ", descript:" + rowItem.getDescript()+", adstreet1:"+rowItem.getAdstreet1());
            holder.tvCdlocat.setText(rowItem.getDescript());
        } else {
            // holder.commodityListNucnt.setText("");
            holder.tvDttxnorigin.setText("");
            holder.tvCdlocat.setText("");
        }

        if (position == this.rowSelected) {
            holder.rlAutosaveRow.setBackgroundColor(context.getResources()
                    .getColor(R.color.yellow));
        } else if (position % 2 > 0) {
            holder.rlAutosaveRow.setBackgroundColor(context.getResources()
                    .getColor(R.color.white));
        } else {
            holder.rlAutosaveRow.setBackgroundColor(context.getResources()
                    .getColor(R.color.blueveryverylight));
        }
        holder.tvCdlocat.setClickable(false);

        final int currentPosition = position;
        currentAutoSave = rowItem;
        OnClickListener onClickListener = new OnClickListener()
        {

            @Override
            public void onClick(View arg0) {
                System.out.println(currentPosition + " RESTORING AUTOSAVE ON "
                        + items.get(currentPosition).getDttxnupdate());
                senateActivity.restoreAutoSave(items.get(currentPosition));
                if (autosaveFoundDialog != null) {
                    autosaveFoundDialog.dismiss();
                }
            }
        };

        holder.tvCdlocat.setOnClickListener(onClickListener);
        holder.tvDttxnorigin.setClickable(false);
        holder.tvDttxnorigin.setOnClickListener(onClickListener);
        return convertView;
    }

    public AutoSaveItem getAutosaveAt(int y) {
        return items.get(y);
    }

    public void filterOn(String autosaveType) {
        items = new ArrayList<AutoSaveItem>();
        for (int x = 0; x < allItems.size(); x++) {
            System.out.println("AutosaveListViewAdapter FILTER:" + x + ": "
                    + allItems.get(x).getDeactype() + " = " + autosaveType);
            if (autosaveType == null || autosaveType.trim().length() == 0
                    || allItems.get(x).getDeactype().equals(autosaveType)) {
                items.add(allItems.get(x));
            }
        }
        // No Need to notify data changed because unselectRow() does this.
        unselectRow();
    }

    public void unselectRow() {
        setRowSelected(-1);
    }

    public void setRowSelected(int rowSelected) {
        this.rowSelected = rowSelected;
        this.notifyDataSetChanged();
    }

    public int getRowSelected() {
        return this.rowSelected;
    }

    public void clearData() {
        items = new ArrayList<AutoSaveItem>();
        rowSelected = -1;
        this.notifyDataSetChanged();
    }

    @Override
    public void OnDoubleTap(AdapterView parent, View view, int position, long id) {
        System.out.println("Double Clicked on " + position + ": "
                + items.get(position).getDttxnorigin());
    }

    @Override
    public void OnSingleTap(AdapterView parent, View view, int position, long id) {
        // Do nothing on Single Tap (for now)
        System.out.println("Clicked on " + position + ": "
                + items.get(position).getDttxnorigin());
    }

}