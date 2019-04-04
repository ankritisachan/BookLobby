package com.example.acer.booklobby;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

import java.util.HashSet;
import java.util.Set;

public class EnableInternetReceiver extends BroadcastReceiver {

    protected Set<InternetStateReceiver> listeners;
    protected Boolean connected;

    public EnableInternetReceiver() {
        listeners = new HashSet<InternetStateReceiver>();
        connected = null;
    }

    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getExtras() == null)
            return;

        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = manager.getActiveNetworkInfo();

        if (ni != null && ni.getState() == NetworkInfo.State.CONNECTED) {
            connected = true;
        } else if (intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, Boolean.FALSE)) {
            connected = false;
        }

        notifyStateToAll();
    }

    private void notifyStateToAll() {
        for (InternetStateReceiver listener : listeners)
            notifyState(listener);
    }

    private void notifyState(InternetStateReceiver listener) {
        if (connected == null || listener == null)
            return;

        if (connected == true)
            listener.networkAvailable();
        else
            listener.networkUnavailable();
    }

    public void addListener(InternetStateReceiver l) {
        listeners.add(l);
        notifyState(l);
    }

    public void removeListener(InternetStateReceiver l) {
        listeners.remove(l);
    }

    public interface InternetStateReceiver {
        public void networkAvailable();
        public void networkUnavailable();
    }
}