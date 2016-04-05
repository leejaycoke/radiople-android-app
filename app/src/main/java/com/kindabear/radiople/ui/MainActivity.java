package com.kindabear.radiople.ui;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.AppCompatButton;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.kindabear.radiople.R;
import com.kindabear.radiople.network.ImageUrlHelper;
import com.kindabear.radiople.service.UserService;
import com.kindabear.radiople.view.sizingimageview.CircleSizingImageView;
import com.squareup.picasso.Picasso;


public class MainActivity extends BaseActivity {

    private final static String TAG = "MainActivity";

    private DrawerLayout mDrawerLayout = null;
    private NavigationView mNavigationView = null;
    private ActionBarDrawerToggle mDrawerToggle = null;
    private View mNavigationHeaderView = null;

    private final static int USER_ACCESSABLE_MENU_IDS[] = {R.id.navigation_notification, R.id.navigation_subscription_list};

    private int mCurrentUserState = 0;

    private UserService mUserService = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        mUserService = new UserService(this);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerlayout);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.app_name, R.string.app_name);
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        mNavigationView = (NavigationView) findViewById(R.id.navigation);
        mNavigationHeaderView = mNavigationView.inflateHeaderView(R.layout.navigation_header);

        refreshNavigationView();

        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.navigation_settings) {
                    startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                } else if (menuItem.getItemId() == R.id.navigation_subscription_list) {
                    startActivity(new Intent(MainActivity.this, SubscriptionActivity.class));
                } else if (menuItem.getItemId() == R.id.navigation_notification) {
                    startActivity(new Intent(MainActivity.this, NotificationActivity.class));
                } else if (menuItem.getItemId() == R.id.navigation_history) {
                    startActivity(new Intent(MainActivity.this, EpisodeHistoryActivity.class));
                }
                return false;
            }
        });

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tablayout);

        ViewPager viewpager = (ViewPager) findViewById(R.id.viewpager);
        viewpager.setAdapter(new MainFragmentAdapter(getSupportFragmentManager()));
        tabLayout.setupWithViewPager(viewpager);
    }

    private void refreshNavigationView() {
        if (mUserService.exists()) {
            if (mCurrentUserState == UserState.UNKNOWN || mCurrentUserState == UserState.GUEST) {
                setUserNavigationState();
            }
        } else {
            if (mCurrentUserState == UserState.UNKNOWN || mCurrentUserState == UserState.USER) {
                setGuestNavigationState();
            }
        }
    }

    private void setUserNavigationState() {
        mCurrentUserState = UserState.USER;
        mNavigationHeaderView.findViewById(R.id.guest_view).setVisibility(View.GONE);
        mNavigationHeaderView.findViewById(R.id.user_view).setVisibility(View.VISIBLE);
        mNavigationHeaderView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, UserActivity.class));
            }
        });

        CircleSizingImageView profileImageView = (CircleSizingImageView) mNavigationHeaderView.findViewById(R.id.imageview_profile);
        String coverImage = mUserService.getProfileImage();

        if (coverImage != null) {
            Picasso.with(MainActivity.this).load(ImageUrlHelper.create(mUserService.getProfileImage(), 500, 500)).fit().into(profileImageView);
        } else {
            Picasso.with(MainActivity.this).load(R.drawable.ic_default_profile).fit().into(profileImageView);
        }

        TextView nicknameTextView = (TextView) mNavigationHeaderView.findViewById(R.id.textview_nickname);
        nicknameTextView.setText(mUserService.getNickname());

        for (int menuId : USER_ACCESSABLE_MENU_IDS) {
            mNavigationView.getMenu().findItem(menuId).setEnabled(true);
        }
    }

    private void setGuestNavigationState() {
        mCurrentUserState = UserState.GUEST;
        mNavigationHeaderView.findViewById(R.id.user_view).setVisibility(View.GONE);
        mNavigationHeaderView.findViewById(R.id.guest_view).setVisibility(View.VISIBLE);
        mNavigationHeaderView.setOnClickListener(null);

        AppCompatButton btnRegister = (AppCompatButton) mNavigationHeaderView.findViewById(R.id.button_signup);
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SignupActivity.class));
            }
        });

        AppCompatButton btnLogin = (AppCompatButton) mNavigationHeaderView.findViewById(R.id.button_login);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
            }
        });

        for (int menuId : USER_ACCESSABLE_MENU_IDS) {
            mNavigationView.getMenu().findItem(menuId).setEnabled(false);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                break;
            case R.id.action_search:
                startActivity(new Intent(this, SearchActivity.class));
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawers();
            return;
        }

        super.onBackPressed();
    }

    public class MainFragmentAdapter extends FragmentPagerAdapter {

        private final String[] TITLES = new String[]{getString(R.string.good_news), getString(R.string.ranking), getString(R.string.category)};
        private final int PAGE_COUNT = 3;

        public MainFragmentAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return PAGE_COUNT;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new NewsFragment();
                case 1:
                    return new RankingFragment();
                default:
                    return new CategoryFragment();
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return TITLES[position];
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        refreshNavigationView();
    }

    private class UserState {

        public final static int UNKNOWN = 0;

        public final static int USER = 1;

        public final static int GUEST = 2;

    }
}
