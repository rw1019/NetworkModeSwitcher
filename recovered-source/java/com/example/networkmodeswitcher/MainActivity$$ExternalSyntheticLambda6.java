package com.example.networkmodeswitcher;

import rikka.shizuku.Shizuku;

/* JADX INFO: compiled from: D8$$SyntheticClass */
/* JADX INFO: loaded from: classes3.dex */
public final /* synthetic */ class MainActivity$$ExternalSyntheticLambda6 implements Shizuku.OnRequestPermissionResultListener {
    public final /* synthetic */ MainActivity f$0;

    public /* synthetic */ MainActivity$$ExternalSyntheticLambda6(MainActivity mainActivity) {
        this.f$0 = mainActivity;
    }

    @Override // rikka.shizuku.Shizuku.OnRequestPermissionResultListener
    public final void onRequestPermissionResult(int i, int i2) {
        this.f$0.onShizukuPermissionResult(i, i2);
    }
}
