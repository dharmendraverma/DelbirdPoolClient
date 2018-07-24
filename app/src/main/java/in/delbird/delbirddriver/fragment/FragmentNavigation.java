package in.delbird.delbirddriver.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import in.delbird.delbirddriver.R;
import in.delbird.delbirddriver.activities.AboutUs;
import in.delbird.delbirddriver.activities.History;
import in.delbird.delbirddriver.adapter.MenuAdapter;
import in.delbird.delbirddriver.controller.Constants;
import in.delbird.delbirddriver.customview.CircularImageView;
import com.squareup.picasso.Picasso;


/**
 * Created by Dharmendra on 5/25/15.
 */
public class FragmentNavigation extends Fragment implements AdapterView.OnItemClickListener {


    SharedPreferences preferences;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private ListView menuList;
    private MenuAdapter adapter;
    private View view;
    private CircularImageView driverPic;
    private TextView driverName;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.navigation_fragment, null);
        menuList = (ListView) view.findViewById(R.id.menu_list);
        driverPic = (CircularImageView) view.findViewById(R.id.driver_pic);
        driverName = (TextView) view.findViewById(R.id.driver_name);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        preferences = getActivity().getSharedPreferences(Constants.USER_DETAILS, Context.MODE_PRIVATE);
        adapter = new MenuAdapter(getActivity());
        menuList.setAdapter(adapter);
        menuList.setOnItemClickListener(this);

        driverName.setText(preferences.getString(Constants.DRIVER_NAME, ""));
        if (preferences.getString(Constants.PIC_URL, "").length() > 0)
            Picasso.with(getActivity()).load(preferences.getString(Constants.PIC_URL, "")).fit().placeholder(R.drawable.pro_pic).into(driverPic);
    }

    public void setUp(int fragmentID, DrawerLayout drawerLayout, final Toolbar toolbar) {

        mDrawerLayout = drawerLayout;
        mDrawerToggle = new ActionBarDrawerToggle(getActivity(), drawerLayout, toolbar, R.string.openDrawer, R.string.closeDrawer) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getActivity().invalidateOptionsMenu();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                getActivity().invalidateOptionsMenu();
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.syncState();
            }
        });
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        if (id == 0) {
            //History
            Intent intent = new Intent(getActivity(), History.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            getActivity().finish();
        } else if (id == 1) {
            // Help
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("message/rfc822");
            i.setClassName("com.google.android.gm", "com.google.android.gm.ComposeActivityGmail");
//        i.putExtra(Intent.EXTRA_EMAIL, new String[]{"support@taptapfooddelivery.com"});
            i.putExtra(Intent.EXTRA_EMAIL, new String[]{Constants.SUPPORT_MAIL_ID});
            i.putExtra(Intent.EXTRA_SUBJECT, "Hello Delbird support");
            i.putExtra(Intent.EXTRA_TEXT, "");
            startActivity(i);

        } else if (id == 2) {
            // About us
            Intent intent = new Intent(getActivity(), AboutUs.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            getActivity().finish();
        }

    }


}