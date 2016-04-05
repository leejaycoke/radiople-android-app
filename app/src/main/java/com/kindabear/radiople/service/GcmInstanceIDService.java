package com.kindabear.radiople.service;

import android.content.Intent;

import com.google.android.gms.iid.InstanceIDListenerService;

public class GcmInstanceIDService extends InstanceIDListenerService {

    @Override
    public void onTokenRefresh() {
        Intent intent = new Intent(this, PushAgent.class);
        intent.setAction(PushAgent.Action.TOKEN_REFRESHED);
        startService(intent);
    }

}
