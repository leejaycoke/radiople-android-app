package com.kindabear.radiople.ui;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v14.preference.PreferenceFragment;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.preference.PreferenceScreen;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.kindabear.radiople.R;
import com.kindabear.radiople.RadiopleApplication;
import com.kindabear.radiople.network.ApiUrlBuilder;
import com.kindabear.radiople.network.CommonResponse;
import com.kindabear.radiople.network.CommonResponseListener;
import com.kindabear.radiople.network.GsonRequest;
import com.kindabear.radiople.response.Setting;
import com.kindabear.radiople.service.PushAgent;
import com.kindabear.radiople.service.SessionService;
import com.kindabear.radiople.service.UserService;

import java.util.HashMap;
import java.util.Map;

public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    private final static String TAG = "SettingsFragment";

    private UserService mUserService = null;
    private SessionService mSessionService = null;

    private SharedPreferences mSharedPreference = null;

    private RequestQueue mRequestQueue = null;

    private ProgressDialog mLoadingDialog = null;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        mRequestQueue = Volley.newRequestQueue(getActivity());

        mUserService = ((RadiopleApplication) getActivity().getApplicationContext()).getUserService();
        mSessionService = ((RadiopleApplication) getActivity().getApplicationContext()).getSessionService();

        mLoadingDialog = new ProgressDialog(getActivity());
        mLoadingDialog.setMessage(getString(R.string.please_wait));
        mLoadingDialog.setCancelable(false);

        PreferenceManager manager = getPreferenceManager();
        manager.setSharedPreferencesName("settings");

        addPreferencesFromResource(R.xml.settings);

        mSharedPreference = manager.getSharedPreferences();
        mSharedPreference.registerOnSharedPreferenceChangeListener(this);

        if (mUserService.exists()) {
            Preference logoutPreference = findPreference("logout");
            logoutPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    showLogoutDialog();
                    return false;
                }
            });
        } else {
            PreferenceScreen screen = getPreferenceScreen();
            screen.removePreference(findPreference("etc"));
            screen.removePreference(findPreference("push"));
        }

        Map<String, ?> allSettings = mSharedPreference.getAll();
        for (String key : allSettings.keySet()) {
            Preference preference = findPreference(key);
            if (preference != null && preference instanceof ListPreference) {
                preference.setSummary(((ListPreference) preference).getEntry());
            }
        }
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(getActivity())
                .setCancelable(false)
                .setTitle(R.string.logout)
                .setMessage(R.string.do_you_want_to_logout)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mUserService.deleteAll();
                        mSessionService.deleteAll();

                        Intent pushAgent = new Intent(getActivity(), PushAgent.class);
                        pushAgent.setAction(PushAgent.Action.STOP);
                        getActivity().startService(pushAgent);

                        getActivity().finish();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .show();
    }

    private void showMobileNetworkWarningDialog() {
        new AlertDialog.Builder(getActivity())
                .setCancelable(false)
                .setTitle(R.string.warning)
                .setMessage(R.string.warning_mobile_network)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SwitchPreference preference = (SwitchPreference) findPreference("allow_mobile_network");
                        preference.setChecked(false);
                    }
                })
                .show();
    }

    private void notifyPushChanged(final String key, final boolean isOn) {
        mLoadingDialog.show();

        String url = new ApiUrlBuilder().addPath("v1", "user", String.valueOf(mUserService.getId()), "setting").toString();
        GsonRequest<Setting> request = new GsonRequest<Setting>(getActivity(), Request.Method.PUT, url, Setting.class,
                new Response.Listener<Setting>() {
                    @Override
                    public void onResponse(Setting response) {
                        mLoadingDialog.dismiss();
                    }
                },
                new CommonResponseListener() {
                    @Override
                    public void onCommonResponse(int statusCode, CommonResponse common) {
                        mSharedPreference.unregisterOnSharedPreferenceChangeListener(SettingsFragment.this);
                        SwitchPreference preference = (SwitchPreference) findPreference(key);
                        preference.setChecked(!isOn);
                        mSharedPreference.registerOnSharedPreferenceChangeListener(SettingsFragment.this);

                        mLoadingDialog.dismiss();

                        Toast.makeText(getActivity(), common.displayMessage, Toast.LENGTH_SHORT).show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        mSharedPreference.unregisterOnSharedPreferenceChangeListener(SettingsFragment.this);
                        SwitchPreference preference = (SwitchPreference) findPreference(key);
                        preference.setChecked(!isOn);
                        mSharedPreference.registerOnSharedPreferenceChangeListener(SettingsFragment.this);

                        mLoadingDialog.dismiss();

                        Toast.makeText(getActivity(), getString(R.string.network_error_message), Toast.LENGTH_SHORT).show();
                    }
                });

        HashMap<String, String> params = new HashMap<String, String>();
        params.put(key, isOn ? "1" : "0");

        request.setParams(params);

        mRequestQueue.add(request);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("allow_mobile_network") && sharedPreferences.getBoolean(key, true)) {
            showMobileNetworkWarningDialog();
        } else if (key.equals("activity_after_finish") || key.equals("skip_time")) {
            ListPreference preference = (ListPreference) findPreference(key);
            preference.setSummary(preference.getEntry());
        } else if (key.equals("all_push") || key.equals("subscription_push") || key.equals("user_push")) {
            notifyPushChanged(key, sharedPreferences.getBoolean(key, false));
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSharedPreference.unregisterOnSharedPreferenceChangeListener(this);
    }
}
