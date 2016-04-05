package com.kindabear.radiople.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class PushAgentMonitor extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent pushAgentIntent = new Intent(context, PushAgent.class);
        pushAgentIntent.setAction(PushAgent.Action.RUN);
        context.startService(pushAgentIntent);
    }
}
