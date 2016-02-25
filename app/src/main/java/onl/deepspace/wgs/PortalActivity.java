package onl.deepspace.wgs;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.android.vending.billing.IInAppBillingService;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;

import onl.deepspace.wgs.PortalUpdate.AlarmReceiver;

public class PortalActivity extends AppCompatActivity {

    public static final int PICK_CHILD_REQUEST = 1;

    AlarmReceiver mAlarm = new AlarmReceiver();
    JSONObject timetable, representation;
    JSONArray mChildren;
    AdView mAdView;

    static ServiceConnection mServiceConn;
    static IInAppBillingService mService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //EXTRAS verarbeitung
        TimetableFragment.setActivity(this);
        RepresentationFragment.setActivity(this);
        setContentView(R.layout.activity_portal);

        //Admob
        if(!Helper.getHasNoAds(getBaseContext())){
            mAdView = (AdView) findViewById(R.id.adView);
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);
        }

        //Inapp billing

        mServiceConn = new ServiceConnection() {
            @Override
            public void onServiceDisconnected(ComponentName name) {
                mService = null;
            }

            @Override
            public void onServiceConnected(ComponentName name,
                                           IBinder service) {
                mService = IInAppBillingService.Stub.asInterface(service);
            }
        };
        Intent serviceIntent =
                new Intent("com.android.vending.billing.InAppBillingService.BIND");
        serviceIntent.setPackage("com.android.vending");
        bindService(serviceIntent, mServiceConn, Context.BIND_AUTO_CREATE);

        //
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if(toolbar != null)
            setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the mActivity.
        /*
      The {@link android.support.v4.view.PagerAdapter} that will provide
      fragments for each of the sections. We use a
      {@link FragmentPagerAdapter} derivative, which will keep every
      loaded fragment in memory. If this becomes too memory intensive, it
      may be best to switch to a
      {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
        SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        ViewPager mViewPager = (ViewPager) findViewById(R.id.container);
        if(mViewPager != null)
            mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);

        if(tabLayout != null && mViewPager != null)
            tabLayout.setupWithViewPager(mViewPager);

        //Alarm Manager
        if(Helper.getEmail(this) != null & Helper.getPw(this) != null)
            mAlarm.setAlarm(this);

        //EXTRAS verarbeitung
        TimetableFragment.setActivity(this);
        RepresentationFragment.setActivity(this);

        Bundle extras = getIntent().getExtras();
        try {
            mChildren = new JSONArray(extras.getString(Helper.API_RESULT_CHILDREN));

            MenuItem selectChild = (MenuItem) findViewById(R.id.action_select_child);
            if (mChildren.length() < 2) {
                selectChild.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
            } else {
                selectChild.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
            }

            int childIndex = Helper.getChildIndex(this);
            selectChild(childIndex);

        }
        catch (JSONException e) {
            Log.e(Helper.LOGTAG, e.toString());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1001) {
            int responseCode = data.getIntExtra("RESPONSE_CODE", 0);
            String purchaseData = data.getStringExtra("INAPP_PURCHASE_DATA");
            String dataSignature = data.getStringExtra("INAPP_DATA_SIGNATURE");

            if (resultCode == RESULT_OK) {
                try {
                    JSONObject jo = new JSONObject(purchaseData);
                    String sku = jo.getString("productId");
                    if(sku.equalsIgnoreCase("wgs_app_remove_ads")) {
                        Helper.setHasNoAds(this, true);
                        mAdView.setVisibility(View.INVISIBLE);
                        Snackbar.make(findViewById(R.id.main_content), "Werbung Entfernt! Danke für deinen Kauf.", Snackbar.LENGTH_LONG).show();
                    }
                    else{
                        Toast.makeText(PortalActivity.this, "Your request was not set. Please contact the developer!", Toast.LENGTH_SHORT).show();
                    }
                }
                catch (JSONException e) {
                    Toast.makeText(PortalActivity.this, "Failed to parse purchase.", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }
            else {
                Toast.makeText(PortalActivity.this, "Failed to parse purchase.", Toast.LENGTH_LONG).show();
            }
        }
        if (requestCode == PICK_CHILD_REQUEST) {
            int childIndex = data.getIntExtra(Helper.CHILD_INDEX, 0);

            //TODO switch user
            Helper.setChildIndex(this, childIndex);
            selectChild(childIndex);
        }
    }

    private void selectChild(int index) {
        try {
            JSONObject child = mChildren.getJSONObject(index);

            JSONObject timetable = child.getJSONObject(Helper.API_RESULT_TIMETABLE);
            JSONObject representations = child.getJSONObject(Helper.API_RESULT_REPRESENTATION);

            TimetableFragment.timetable = timetable;
            RepresentationFragment.representation = representations;
            TimetableFragment.setTimetable(timetable);
            RepresentationFragment.setRepresentations(representations);
        } catch(JSONException e) {
            Log.e(Helper.LOGTAG, e.getMessage());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mService != null) {
            unbindService(mServiceConn);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_portal, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if(Helper.getHasNoAds(getBaseContext())) {
            MenuItem mi = menu.findItem(R.id.action_remads);
            mi.setVisible(false);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        /*if (id == R.id.action_settings) {
            return true;
        }*/
        if (id == R.id.action_select_child) {
            Intent intent = new Intent(this, SelectChildActivity.class);
            //TODO put String array extra with names of the children
            ArrayList<String> childrenNames = new ArrayList<>();

            try {
                for (int i = 0; i < mChildren.length(); i++) {
                    String name = mChildren.getString(i);
                    childrenNames.add(name);
                }
            } catch (JSONException e) {
                Log.e(Helper.LOGTAG, e.getMessage());
            }

            intent.putExtra(Helper.CHILDREN, childrenNames);
            startActivityForResult(intent, PICK_CHILD_REQUEST);
        }
        if(id == R.id.action_about) {
            Intent intent = new Intent(this, AboutActivity.class);
            startActivity(intent);
            return true;
        }

        if(id == R.id.action_logout) {
            Helper.setEmail(this, "");
            Helper.setPw(this, "");
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }

        if(id == R.id.action_remads){
            Helper.purchaseNoAd(this);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0: return new RepresentationFragment();
                case 1: return new TimetableFragment();
            }
            return null;
        }

        @Override
        public int getCount() {
            // Show 2 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Vertretungsplan";
                case 1:
                    return "Stundenplan";
            }
            return null;
        }
    }
}
