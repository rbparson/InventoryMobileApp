package gov.nysenate.inventory.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import android.os.Parcel;
import android.os.Parcelable;

public class AutoSaveItem implements Parcelable
{
    private int nuxractivity;
    private String naactivity;
    private int nuxracttype;
    private Date dttxnorigin;
    private Date dttxnupdate;
    private String deactype;
    private String cdlocat;
    private String cdloctype;
    private String cdrespctrhd;
    private String adstreet1;
    private String adcity;
    private String adstate;
    private String adzipcode;
    private String descript;
    private String locationEntry;
    private String locationToEntry;
    private String deactypeto;
    private String cdlocatto;
    private String cdloctypeto;
    private String cdrespctrhdto;
    private String adstreet1to;
    private String adcityto;
    private String adstateto;
    private String adzipcodeto;
    private String descriptto;
    private SimpleDateFormat sdf = new SimpleDateFormat("MMddyyhhmmssa");

    public AutoSaveItem() {

    }

    public int getNuxractivity() {
        return nuxractivity;
    }

    public void setNuxractivity(int nuxractivity) {
        this.nuxractivity = nuxractivity;
    }

    public String getNaactivity() {
        return naactivity;
    }

    public void SetNaactivity(String naactivity) {
        this.naactivity = naactivity;
    }

    public int getNuxracttype() {
        return nuxracttype;
    }

    public void setNuxracttype(int nuxracttype) {
        this.nuxracttype = nuxracttype;
    }

    public String getDeactype() {
        return deactype;
    }

    public void setDeactype(String deactype) {
        this.deactype = deactype;
    }

    public Date getDttxnorigin() {
        return dttxnorigin;
    }

    public void setDttxnorigin(Date dttxnorigin) {
        this.dttxnorigin = dttxnorigin;
    }

    public Date getDttxnupdate() {
        return dttxnupdate;
    }

    public void setDttxnupdate(Date dttxnupdate) {
        this.dttxnupdate = dttxnupdate;
    }

    public String getCdlocat() {
        return cdlocat;
    }

    public void setCdlocat(String cdlocat) {
        this.cdlocat = cdlocat;
    }

    public String getCdloctype() {
        return cdloctype;
    }

    public void setCdloctype(String cdloctype) {
        this.cdloctype = cdloctype;
    }

    public String getCdrespctrhd() {
        return cdrespctrhd;
    }

    public void setCdrespctrhd(String cdrespctrhd) {
        this.cdrespctrhd = cdrespctrhd;
    }

    public String getAdstreet1() {
        return adstreet1;
    }

    public void setAdstreet1(String adstreet1) {
        this.adstreet1 = adstreet1;
    }

    public String getAdcity() {
        return adcity;
    }

    public void setAdcity(String adcity) {
        this.adcity = adcity;
    }

    public String getAdzipcode() {
        return adzipcode;
    }

    public void setAdzipcode(String adzipcode) {
        this.adzipcode = adzipcode;
    }

    public String getAdstate() {
        return adcity;
    }

    public void setAdstate(String adstate) {
        this.adstate = adstate;
    }

    public String getDescript() {
        return descript;
    }

    public void setDescript(String descript) {
        this.descript = descript;
    }

    public String getLocationEntry() {
        return this.locationEntry;
    }

    public void setLocationEntry(String locationEntry) {
        this.locationEntry = locationEntry;
    }

    public String getLocationToEntry() {
        return this.locationToEntry;
    }

    public void setLocationToEntry(String locationToEntry) {
        this.locationToEntry = locationToEntry;
    }

    public String getCdlocatto() {
        return cdlocatto;
    }

    public void setCdlocatto(String cdlocatto) {
        this.cdlocatto = cdlocatto;
    }

    public String getCdloctypeto() {
        return cdloctypeto;
    }

    public void setCdloctypeto(String cdloctypeto) {
        this.cdloctypeto = cdloctypeto;
    }

    public String getCdrespctrhdto() {
        return cdrespctrhdto;
    }

    public void setCdrespctrhdto(String cdrespctrhdto) {
        this.cdrespctrhdto = cdrespctrhdto;
    }

    public String getAdstreet1to() {
        return adstreet1to;
    }

    public void setAdstreet1to(String adstreet1to) {
        this.adstreet1to = adstreet1to;
    }

    public String getAdcityto() {
        return adcityto;
    }

    public void setAdcityto(String adcityto) {
        this.adcityto = adcityto;
    }

    public String getAdzipcodeto() {
        return adzipcodeto;
    }

    public void setAdzipcodeto(String adzipcodeto) {
        this.adzipcodeto = adzipcodeto;
    }

    public String getAdstateto() {
        return adstateto;
    }

    public void setAdstateto(String adstateto) {
        this.adstateto = adstateto;
    }

    public String getDescriptto() {
        return descriptto;
    }

    public void setDescriptto(String descriptto) {
        this.descriptto = descriptto;
    }

    // ---------- Code for Parcelable interface ----------

    /*
     * @Override public int describeContents() { return 0; }
     * 
     * @Override public void writeToParcel(Parcel dest, int flags) { String
     * dttxnoriginString = sdf.format(this.dttxnorigin); String
     * dttxnupdateString = sdf.format(this.dttxnupdate);
     * 
     * dest.writeStringArray(new String[]{new
     * String(String.valueOf(this.nuxractivity)), this.naactivity,
     * String.valueOf(this.nuxracttype), dttxnoriginString, dttxnupdateString,
     * this.deactype, this.cdlocat, this.cdloctype, this.cdrespctrhd,
     * this.adstreet1, this.adcity, this.adstate, this.adstate, this.adzipcode,
     * this.descript}); }
     * 
     * public static final Parcelable.Creator CREATOR = new Parcelable.Creator()
     * { public AutoSaveItem createFromParcel(Parcel in) { try { return new
     * AutoSaveItem(in); } catch (ParseException e) { // TODO Auto-generated
     * catch block e.printStackTrace(); } return null; }
     * 
     * public AutoSaveItem[] newArray(int size) { return new AutoSaveItem[size];
     * } };
     */

    public AutoSaveItem(Parcel in) {
        readFromParcel(in);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(nuxractivity);
        dest.writeInt(nuxracttype);
        dest.writeLong(dttxnorigin.getTime());
        dest.writeLong(dttxnupdate.getTime());
        dest.writeString(naactivity);
        dest.writeString(deactype);
        dest.writeString(cdlocat);
        dest.writeString(cdloctype);
        dest.writeString(cdrespctrhd);
        dest.writeString(adstreet1);
        dest.writeString(adcity);
        dest.writeString(adstate);
        dest.writeString(adzipcode);
        dest.writeString(descript);
        dest.writeString(locationEntry);
        dest.writeString(locationToEntry);
    }

    public void readFromParcel(Parcel in) {
        nuxractivity = in.readInt();
        nuxracttype = in.readInt();
        try {
            dttxnorigin = new Date();
            dttxnorigin.setTime(in.readLong());
        } catch (NullPointerException e) {
            dttxnorigin = null;
            e.printStackTrace();
        }
        try {
            dttxnupdate = new Date();
            dttxnupdate.setTime(in.readLong());
        } catch (NullPointerException e) {
            dttxnupdate = null;
            e.printStackTrace();
        }
        naactivity = in.readString();
        deactype = in.readString();
        cdlocat = in.readString();
        cdloctype = in.readString();
        cdrespctrhd = in.readString();
        adstreet1 = in.readString();
        adcity = in.readString();
        adstate = in.readString();
        adzipcode = in.readString();
        descript = in.readString();
        locationEntry = in.readString();
        locationToEntry = in.readString();
    }

    public static final Parcelable.Creator<AutoSaveItem> CREATOR = new Parcelable.Creator<AutoSaveItem>()
    {
        @Override
        public AutoSaveItem createFromParcel(Parcel in) {
            return new AutoSaveItem(in);
        }

        @Override
        public AutoSaveItem[] newArray(int size) {
            return new AutoSaveItem[size];
        }
    };

}
