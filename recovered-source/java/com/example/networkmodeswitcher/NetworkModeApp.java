package com.example.networkmodeswitcher;

import android.app.Application;
import com.google.android.material.color.DynamicColors;

/* JADX INFO: loaded from: classes3.dex */
public final class NetworkModeApp extends Application {
    @Override // android.app.Application
    public void onCreate() {
        super.onCreate();
        DynamicColors.applyToActivitiesIfAvailable(this);
    }
}
