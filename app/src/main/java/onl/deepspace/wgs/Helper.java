package onl.deepspace.wgs;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by Dennis on 18.02.2016.
 */
public class Helper {
    public static String LOGTAG = "Deepspace";
    public static String PW = "password";
    public static String EMAIL = "userEmail";

    public static void purchaseNoAd(Activity activity){
        ArrayList<String> skuList = new ArrayList<String>();
        skuList.add("wgs_app_remove_ads");;
        Bundle querySkus = new Bundle();
        querySkus.putStringArrayList("ITEM_ID_LIST", skuList);

        Bundle skuDetails = new Bundle();

        try {
            skuDetails = PortalActivity.mService.getSkuDetails(3,
                    activity.getPackageName(), "inapp", querySkus);
        }
        catch (RemoteException e){
            Log.e(LOGTAG, e.getLocalizedMessage());
        }

        Log.v(LOGTAG, skuDetails.toString());
    }

    public static void sendNotification(Context activity, String title, String message){
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(activity)
                        .setSmallIcon(R.mipmap.appicon)
                        .setContentTitle(title)
                        .setContentText(message);
        Intent resultIntent = new Intent(activity, LoginActivity.class);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(activity, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setAutoCancel(true);
        mBuilder.setContentIntent(resultPendingIntent);

        int mNotificationId = 001;

        NotificationManager mNotifyMgr = (NotificationManager) activity.getSystemService(activity.NOTIFICATION_SERVICE);
        mNotifyMgr.notify(mNotificationId, mBuilder.build());
    }

    public static String getPw(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        return sharedPref.getString(PW, "");
    }

    public static String getEmail(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        return sharedPref.getString(EMAIL, "");
    }

    public static void setPw(Context context, String pw) {
        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(PW, pw);
        editor.apply();
    }

    public static void setEmail(Context context, String email) {
        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(EMAIL, email);
        editor.apply();
    }

    /**
     * Get the text id for the specified subject
     * @param subject The subject to get the id for
     * @return The id of the string resource
     */
    public static int getSubjectId(String subject) {
        int id = 0;

        subject = subject.replaceAll("[0-9]", "");

        subject = subject.toUpperCase();

        switch(subject) {
            case "D": id = R.string.german; break;
            case "M": id = R.string.maths; break;
            case "E": id = R.string.english; break;
            case "L": id = R.string.latin; break;
            case "PH": id = R.string.physics; break;
            case "INF": id = R.string.informatics; break;
            case "WR": id = R.string.economyNLaw; break;
            case "GEO": id = R.string.geographie; break;
            case "SM/SW": id = R.string.sports; break;
            case "C": id = R.string.chemistry; break;
            case "B": id = R.string.biology; break;
            case "G": id = R.string.history; break;
            case "SK": id = R.string.socialEdu; break;
            case "SOG": id = R.string.socialBaseEdu; break;
            case "ETH/EV/K": id = R.string.religion; break;
            case "F": id = R.string.french; break;
            case "S": id = R.string.spain; break;
            case "DRG": id = R.string.theatre; break;
            case "CHOR": id = R.string.choir; break;
            case "ORCH": id = R.string.orchestra; break;
            case "NT": id = R.string.NT; break;
            case "MU": id = R.string.music; break;
            case "KU": id = R.string.arts; break;
            case "PSY": id = R.string.psychology; break;
            case "BCP": id = R.string.bioChemPrak; break;
            case "ROB": id = R.string.robotic; break;
            case "IM": id = R.string.intMaths; break;
            case "ID": id = R.string.intGerman; break;
            case "IE": id = R.string.intEnglish; break;
            case "IF": id = R.string.intFrench; break;
            case "IL": id = R.string.intLatin; break;
            case "IPH": id = R.string.intPhysics; break;
            case "IC": id = R.string.intChemistry; break;
        }
        return id;
    }
}
