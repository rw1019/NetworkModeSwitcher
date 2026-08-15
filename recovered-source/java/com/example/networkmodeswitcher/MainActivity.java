package com.example.networkmodeswitcher;

import android.app.Activity;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.ServiceConnection;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.IBinder;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.util.TypedValue;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.radiobutton.MaterialRadioButton;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import rikka.shizuku.Shizuku;

/* JADX INFO: loaded from: classes3.dex */
public final class MainActivity extends Activity {
    private static final long MASK_2G = ((((bit(1) | bit(2)) | bit(4)) | bit(7)) | bit(11)) | bit(16);
    private static final long MASK_3G = 50055;
    private static final long MASK_4G = 316295;
    private static final long MASK_5G = 840583;
    private static final int PHONE_PERMISSION = 20;
    private static final int SHIZUKU_PERMISSION = 21;
    private volatile boolean rootAvailable;
    private INetworkService service;
    private LinearLayout simList;
    private TextView status;
    private int selectedSlot = 0;
    private final int[] subIdsBySlot = {-1, -1};
    private final ServiceConnection connection = new ServiceConnection() { // from class: com.example.networkmodeswitcher.MainActivity.1
        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName name, IBinder binder) {
            MainActivity.this.service = INetworkService.Stub.asInterface(binder);
            MainActivity.this.status.setText("Shizuku 已连接，可以切换网络");
        }

        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName name) {
            MainActivity.this.service = null;
            MainActivity.this.status.setText("Shizuku 连接已断开");
        }
    };

    @Override // android.app.Activity
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        buildUi();
        Shizuku.addRequestPermissionResultListener(new MainActivity$$ExternalSyntheticLambda6(this));
        ensurePhonePermission();
    }

    private void buildUi() {
        int p = dp(24);
        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);
        scrollView.setBackgroundColor(colorAttr(com.google.android.material.R.attr.colorSurface));
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(1);
        linearLayout.setPadding(p, dp(52), p, dp(28));
        scrollView.addView(linearLayout, new FrameLayout.LayoutParams(-1, -2));
        TextView title = text("网络制式切换", 30, colorAttr(com.google.android.material.R.attr.colorOnSurface));
        title.setTypeface(null, 1);
        linearLayout.addView(title);
        TextView subtitle = text("双卡移动网络控制", 15, colorAttr(com.google.android.material.R.attr.colorOnSurfaceVariant));
        subtitle.setPadding(0, dp(6), 0, dp(28));
        linearLayout.addView(subtitle);
        TextView simHeading = text("SIM 卡", 14, colorAttr(com.google.android.material.R.attr.colorOnSurfaceVariant));
        simHeading.setTypeface(null, 1);
        simHeading.setPadding(dp(4), 0, 0, dp(8));
        linearLayout.addView(simHeading);
        MaterialCardView simCard = card();
        this.simList = new LinearLayout(this);
        this.simList.setOrientation(1);
        this.simList.setPadding(dp(12), dp(8), dp(12), dp(8));
        simCard.addView(this.simList, new FrameLayout.LayoutParams(-1, -2));
        linearLayout.addView(simCard, new LinearLayout.LayoutParams(-1, -2));
        TextView modeHeading = text("最高网络制式", 14, colorAttr(com.google.android.material.R.attr.colorOnSurfaceVariant));
        modeHeading.setTypeface(null, 1);
        modeHeading.setPadding(dp(4), dp(26), 0, dp(8));
        linearLayout.addView(modeHeading);
        LinearLayout modeRow = new LinearLayout(this);
        modeRow.setOrientation(0);
        linearLayout.addView(modeRow, new LinearLayout.LayoutParams(-1, dp(56)));
        String[] labels = {"3G", "4G", "5G"};
        long[] masks = {MASK_3G, MASK_4G, MASK_5G};
        int[] legacyModes = {3, 9, 26};
        int i = 0;
        while (i < labels.length) {
            int p2 = p;
            MaterialButton b = new MaterialButton(this, null, com.google.android.material.R.attr.materialButtonOutlinedStyle);
            b.setText(labels[i]);
            b.setTextSize(16.0f);
            b.setCornerRadius(dp(18));
            TextView title2 = title;
            TextView simHeading2 = simHeading;
            final long mask = masks[i];
            TextView modeHeading2 = modeHeading;
            final int legacyMode = legacyModes[i];
            int[] legacyModes2 = legacyModes;
            b.setOnClickListener(new View.OnClickListener() { // from class: com.example.networkmodeswitcher.MainActivity$$ExternalSyntheticLambda12
                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    this.f$0.m52lambda$buildUi$0$comexamplenetworkmodeswitcherMainActivity(mask, legacyMode, view);
                }
            });
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, dp(56), 1.0f);
            lp.setMargins(i == 0 ? 0 : dp(5), 0, i == 2 ? 0 : dp(5), 0);
            modeRow.addView(b, lp);
            i++;
            title = title2;
            simHeading = simHeading2;
            p = p2;
            modeHeading = modeHeading2;
            legacyModes = legacyModes2;
        }
        MaterialCardView materialCardViewCard = card();
        materialCardViewCard.setCardBackgroundColor(colorAttr(com.google.android.material.R.attr.colorPrimaryContainer));
        LinearLayout statusBox = new LinearLayout(this);
        statusBox.setOrientation(1);
        statusBox.setPadding(dp(18), dp(14), dp(18), dp(14));
        TextView statusLabel = text("授权状态", 12, colorAttr(com.google.android.material.R.attr.colorOnPrimaryContainer));
        statusLabel.setTypeface(null, 1);
        statusBox.addView(statusLabel);
        this.status = text("正在检查权限…", 15, colorAttr(com.google.android.material.R.attr.colorOnPrimaryContainer));
        this.status.setPadding(0, dp(4), 0, 0);
        statusBox.addView(this.status);
        materialCardViewCard.addView(statusBox);
        LinearLayout.LayoutParams statusParams = new LinearLayout.LayoutParams(-1, -2);
        statusParams.setMargins(0, dp(26), 0, dp(20));
        linearLayout.addView(materialCardViewCard, statusParams);
        MaterialButton settingsButton = new MaterialButton(this);
        settingsButton.setText("弹出所选 SIM 的 Network mode");
        settingsButton.setTextSize(15.0f);
        settingsButton.setCornerRadius(dp(18));
        settingsButton.setTextColor(colorAttr(com.google.android.material.R.attr.colorOnPrimary));
        settingsButton.setBackgroundTintList(ColorStateList.valueOf(colorAttr(com.google.android.material.R.attr.colorPrimary)));
        settingsButton.setOnClickListener(new View.OnClickListener() { // from class: com.example.networkmodeswitcher.MainActivity$$ExternalSyntheticLambda13
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                this.f$0.m53lambda$buildUi$1$comexamplenetworkmodeswitcherMainActivity(view);
            }
        });
        linearLayout.addView(settingsButton, new LinearLayout.LayoutParams(-1, dp(56)));
        MaterialButton authButton = new MaterialButton(this, null, com.google.android.material.R.attr.materialButtonOutlinedStyle);
        authButton.setText("重新检测 Root / Shizuku");
        authButton.setCornerRadius(dp(18));
        authButton.setOnClickListener(new View.OnClickListener() { // from class: com.example.networkmodeswitcher.MainActivity$$ExternalSyntheticLambda1
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                this.f$0.m54lambda$buildUi$2$comexamplenetworkmodeswitcherMainActivity(view);
            }
        });
        LinearLayout.LayoutParams authParams = new LinearLayout.LayoutParams(-1, dp(56));
        authParams.setMargins(0, dp(10), 0, 0);
        linearLayout.addView(authButton, authParams);
        setContentView(scrollView);
    }

    /* JADX INFO: renamed from: lambda$buildUi$0$com-example-networkmodeswitcher-MainActivity, reason: not valid java name */
    /* synthetic */ void m52lambda$buildUi$0$comexamplenetworkmodeswitcherMainActivity(long mask, int legacyMode, View v) {
        applyMode(((MaterialButton) v).getText().toString(), mask, legacyMode);
    }

    /* JADX INFO: renamed from: lambda$buildUi$1$com-example-networkmodeswitcher-MainActivity, reason: not valid java name */
    /* synthetic */ void m53lambda$buildUi$1$comexamplenetworkmodeswitcherMainActivity(View v) {
        showNetworkModeDialog();
    }

    /* JADX INFO: renamed from: lambda$buildUi$2$com-example-networkmodeswitcher-MainActivity, reason: not valid java name */
    /* synthetic */ void m54lambda$buildUi$2$comexamplenetworkmodeswitcherMainActivity(View v) {
        detectAuthorization();
    }

    private void ensurePhonePermission() {
        if (checkSelfPermission("android.permission.READ_PHONE_STATE") != 0) {
            requestPermissions(new String[]{"android.permission.READ_PHONE_STATE"}, 20);
        } else {
            loadSims();
            detectAuthorization();
        }
    }

    private void loadSims() {
        this.simList.removeAllViews();
        SubscriptionManager manager = (SubscriptionManager) getSystemService(SubscriptionManager.class);
        List<SubscriptionInfo> infos = manager.getActiveSubscriptionInfoList();
        RadioGroup group = new RadioGroup(this);
        if (infos == null || infos.isEmpty()) {
            this.status.setText("未检测到已启用的 SIM 卡");
            return;
        }
        for (SubscriptionInfo info : infos) {
            MaterialRadioButton radio = new MaterialRadioButton(this);
            int slot = info.getSimSlotIndex();
            if (slot >= 0 && slot < this.subIdsBySlot.length) {
                this.subIdsBySlot[slot] = info.getSubscriptionId();
            }
            radio.setId(View.generateViewId());
            radio.setTag(Integer.valueOf(slot));
            radio.setText("SIM " + (slot + 1) + "  ·  " + ((Object) info.getDisplayName()));
            radio.setTextSize(17.0f);
            radio.setPadding(dp(8), dp(8), 0, dp(8));
            group.addView(radio, new RadioGroup.LayoutParams(-1, dp(56)));
        }
        this.selectedSlot = infos.get(0).getSimSlotIndex();
        group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() { // from class: com.example.networkmodeswitcher.MainActivity$$ExternalSyntheticLambda8
            @Override // android.widget.RadioGroup.OnCheckedChangeListener
            public final void onCheckedChanged(RadioGroup radioGroup, int i) {
                this.f$0.m57lambda$loadSims$3$comexamplenetworkmodeswitcherMainActivity(radioGroup, i);
            }
        });
        group.check(group.getChildAt(0).getId());
        this.simList.addView(group);
    }

    /* JADX INFO: renamed from: lambda$loadSims$3$com-example-networkmodeswitcher-MainActivity, reason: not valid java name */
    /* synthetic */ void m57lambda$loadSims$3$comexamplenetworkmodeswitcherMainActivity(RadioGroup radioGroup, int checkedId) {
        RadioButton checked = (RadioButton) radioGroup.findViewById(checkedId);
        if (checked != null) {
            this.selectedSlot = ((Integer) checked.getTag()).intValue();
        }
    }

    private void detectAuthorization() {
        this.status.setText("正在请求 Root 授权…");
        new Thread(new Runnable() { // from class: com.example.networkmodeswitcher.MainActivity$$ExternalSyntheticLambda10
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.m56x9c2a8bec();
            }
        }).start();
    }

    /* JADX INFO: renamed from: lambda$detectAuthorization$5$com-example-networkmodeswitcher-MainActivity, reason: not valid java name */
    /* synthetic */ void m56x9c2a8bec() {
        this.rootAvailable = testRoot();
        runOnUiThread(new Runnable() { // from class: com.example.networkmodeswitcher.MainActivity$$ExternalSyntheticLambda7
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.m55xe2b2fe4d();
            }
        });
    }

    /* JADX INFO: renamed from: lambda$detectAuthorization$4$com-example-networkmodeswitcher-MainActivity, reason: not valid java name */
    /* synthetic */ void m55xe2b2fe4d() {
        if (!this.rootAvailable) {
            this.status.setText("未获得 Root，正在尝试 Shizuku…");
            connectShizuku();
        } else {
            this.status.setText("Root 已授权，可以切换网络");
        }
    }

    private boolean testRoot() {
        try {
            Process process = new ProcessBuilder("su", "-c", "id").redirectErrorStream(true).start();
            StringBuilder output = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            while (true) {
                try {
                    String line = reader.readLine();
                    if (line == null) {
                        break;
                    }
                    output.append(line);
                } catch (Throwable th) {
                    try {
                        reader.close();
                    } catch (Throwable th2) {
                        th.addSuppressed(th2);
                    }
                    throw th;
                }
                return false;
            }
            reader.close();
            return process.waitFor() == 0 && output.toString().contains("uid=0");
        } catch (Exception e) {
            return false;
        }
    }

    private void connectShizuku() {
        if (!Shizuku.pingBinder()) {
            this.status.setText("请先安装并启动 Shizuku");
        } else if (Shizuku.checkSelfPermission() != 0) {
            Shizuku.requestPermission(21);
        } else {
            bindUserService();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onShizukuPermissionResult(int requestCode, int grantResult) {
        if (requestCode != 21 || grantResult != 0) {
            this.status.setText("需要 Shizuku 授权才能切换网络");
        } else {
            bindUserService();
        }
    }

    private void bindUserService() {
        Shizuku.UserServiceArgs args = new Shizuku.UserServiceArgs(new ComponentName(this, (Class<?>) NetworkUserService.class)).processNameSuffix("network").debuggable(BuildConfig.DEBUG).version(1);
        Shizuku.bindUserService(args, this.connection);
        this.status.setText("正在连接 Shizuku…");
    }

    private void applyMode(final String label, final long mask, final int legacyMode) {
        if (!this.rootAvailable && this.service == null) {
            detectAuthorization();
        } else {
            this.status.setText("正在切换 SIM " + (this.selectedSlot + 1) + " 到 " + label + "…");
            new Thread(new Runnable() { // from class: com.example.networkmodeswitcher.MainActivity$$ExternalSyntheticLambda2
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.m51lambda$applyMode$8$comexamplenetworkmodeswitcherMainActivity(mask, legacyMode, label);
                }
            }).start();
        }
    }

    /* JADX INFO: renamed from: lambda$applyMode$8$com-example-networkmodeswitcher-MainActivity, reason: not valid java name */
    /* synthetic */ void m51lambda$applyMode$8$comexamplenetworkmodeswitcherMainActivity(long mask, int legacyMode, final String label) {
        try {
            int subId = this.subIdsBySlot[this.selectedSlot];
            final String result = this.rootAvailable ? setModeAsRoot(this.selectedSlot, subId, mask, legacyMode) : this.service.setMode(this.selectedSlot, subId, mask, legacyMode);
            runOnUiThread(new Runnable() { // from class: com.example.networkmodeswitcher.MainActivity$$ExternalSyntheticLambda0
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.m49lambda$applyMode$6$comexamplenetworkmodeswitcherMainActivity(result, label);
                }
            });
        } catch (Exception e) {
            runOnUiThread(new Runnable() { // from class: com.example.networkmodeswitcher.MainActivity$$ExternalSyntheticLambda5
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.m50lambda$applyMode$7$comexamplenetworkmodeswitcherMainActivity(e);
                }
            });
        }
    }

    /* JADX INFO: renamed from: lambda$applyMode$6$com-example-networkmodeswitcher-MainActivity, reason: not valid java name */
    /* synthetic */ void m49lambda$applyMode$6$comexamplenetworkmodeswitcherMainActivity(String result, String label) {
        if ("OK".equals(result)) {
            this.status.setText("SIM " + (this.selectedSlot + 1) + " 已通过 " + (this.rootAvailable ? "Root" : "Shizuku") + " 切换到 " + label + " 档");
            Toast.makeText(this, "切换成功", 0).show();
        } else {
            this.status.setText("切换未通过电话服务校验\n" + result);
        }
    }

    /* JADX INFO: renamed from: lambda$applyMode$7$com-example-networkmodeswitcher-MainActivity, reason: not valid java name */
    /* synthetic */ void m50lambda$applyMode$7$comexamplenetworkmodeswitcherMainActivity(Exception e) {
        this.status.setText("切换失败：" + e.getMessage());
    }

    /* JADX WARN: Code duplicated, block: B:54:0x01b1  */
    private String setModeAsRoot(int slot, int subId, long mask, int legacyMode) {
        char c = 0;
        String[] variants = {"service call phone 94 i32 " + subId + " i32 0 i64 " + mask + " s16 com.android.shell", "service call phone 94 i32 " + subId + " i32 0 i64 " + mask + " s16 com.android.shell"};
        StringBuilder errors = new StringBuilder("ERROR\n");
        boolean phoneCommandSucceeded = false;
        int length = variants.length;
        int i = 0;
        while (i < length) {
            String command = variants[i];
            try {
                String[] strArr = new String[3];
                strArr[c] = "su";
                strArr[1] = "-c";
                strArr[2] = command;
                Process process = new ProcessBuilder(strArr).redirectErrorStream(true).start();
                StringBuilder output = new StringBuilder();
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                while (true) {
                    try {
                        String line = reader.readLine();
                        if (line == null) {
                            break;
                        }
                        output.append(line).append('\n');
                        i++;
                        c = 0;
                    } catch (Throwable th) {
                        try {
                            reader.close();
                        } catch (Throwable th2) {
                            th.addSuppressed(th2);
                        }
                        throw th;
                    }
                    errors.append(e).append('\n');
                }
                reader.close();
                int exit = process.waitFor();
                String normalized = output.toString().toLowerCase();
                if (exit == 0 && normalized.contains("00000001") && !normalized.contains("exception")) {
                    phoneCommandSucceeded = true;
                    break;
                }
                errors.append(command).append(": ").append((CharSequence) output);
                i++;
                c = 0;
            } catch (Exception e) {
                errors.append(e).append('\n');
            }
        }
        if (!phoneCommandSucceeded) {
            return errors.toString();
        }
        String key = "preferred_network_mode" + subId;
        try {
            if (new ProcessBuilder("su", "-c", "settings put global " + key + " " + legacyMode).redirectErrorStream(true).start().waitFor() == 0) {
                Process verify = new ProcessBuilder("su", "-c", "settings get global " + key).redirectErrorStream(true).start();
                BufferedReader reader2 = new BufferedReader(new InputStreamReader(verify.getInputStream()));
                try {
                    try {
                        String value = reader2.readLine();
                        reader2.close();
                        if (verify.waitFor() == 0) {
                            if (String.valueOf(legacyMode).equals(value != null ? value.trim() : "")) {
                                if (readCurrentModeFromBinder(subId) == legacyMode) {
                                    return "OK";
                                }
                            }
                        }
                    } catch (Throwable th3) {
                        try {
                            reader2.close();
                            throw th3;
                        } catch (Throwable th4) {
                            th3.addSuppressed(th4);
                            throw th3;
                        }
                    }
                } catch (Exception e2) {
                    e = e2;
                    errors.append(e).append('\n');
                }
            }
        } catch (Exception e3) {
            e = e3;
        }
        return errors.append("切换后的设置校验未通过").toString();
    }

    private void showNetworkModeDialog() {
        final int slot = this.selectedSlot;
        final int subId = this.subIdsBySlot[slot];
        this.status.setText("正在读取 SIM " + (slot + 1) + " 的 Network mode…");
        new Thread(new Runnable() { // from class: com.example.networkmodeswitcher.MainActivity$$ExternalSyntheticLambda9
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.m60x91c89010(subId, slot);
            }
        }).start();
    }

    /* JADX INFO: renamed from: lambda$showNetworkModeDialog$12$com-example-networkmodeswitcher-MainActivity, reason: not valid java name */
    /* synthetic */ void m60x91c89010(int subId, final int slot) {
        final int mode = this.rootAvailable ? readCurrentModeAsRoot(subId) : readCurrentModeFromService(subId);
        runOnUiThread(new Runnable() { // from class: com.example.networkmodeswitcher.MainActivity$$ExternalSyntheticLambda4
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.m59xd8510271(mode, slot);
            }
        });
    }

    /* JADX INFO: renamed from: lambda$showNetworkModeDialog$11$com-example-networkmodeswitcher-MainActivity, reason: not valid java name */
    /* synthetic */ void m59xd8510271(int mode, int slot) {
        int checked;
        if (mode == 26 || mode == 28) {
            checked = 0;
        } else {
            checked = (mode == 9 || mode == 12) ? 1 : -1;
        }
        String[] choices = {"5G/4G/3G/2G(auto connect)", "4G/3G/2G(auto connect)"};
        final AlertDialog dialog = new MaterialAlertDialogBuilder(this).setTitle((CharSequence) ("Select Network mode SIM " + (slot + 1))).setSingleChoiceItems((CharSequence[]) choices, checked, (DialogInterface.OnClickListener) null).setNegativeButton((CharSequence) "取消", (DialogInterface.OnClickListener) null).create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() { // from class: com.example.networkmodeswitcher.MainActivity$$ExternalSyntheticLambda11
            @Override // android.content.DialogInterface.OnShowListener
            public final void onShow(DialogInterface dialogInterface) {
                this.f$0.m58x1ed974d2(dialog, dialogInterface);
            }
        });
        dialog.show();
        this.status.setText("请选择 SIM " + (slot + 1) + " 的网络模式");
    }

    /* JADX INFO: renamed from: lambda$showNetworkModeDialog$10$com-example-networkmodeswitcher-MainActivity, reason: not valid java name */
    /* synthetic */ void m58x1ed974d2(final AlertDialog dialog, DialogInterface ignored) {
        dialog.getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() { // from class: com.example.networkmodeswitcher.MainActivity$$ExternalSyntheticLambda3
            @Override // android.widget.AdapterView.OnItemClickListener
            public final void onItemClick(AdapterView adapterView, View view, int i, long j) {
                this.f$0.m61xedae4ce2(dialog, adapterView, view, i, j);
            }
        });
    }

    /* JADX INFO: renamed from: lambda$showNetworkModeDialog$9$com-example-networkmodeswitcher-MainActivity, reason: not valid java name */
    /* synthetic */ void m61xedae4ce2(AlertDialog dialog, AdapterView parent, View view, int position, long id) {
        dialog.dismiss();
        if (position != 0) {
            applyMode("4G", MASK_4G, 9);
        } else {
            applyMode("5G", MASK_5G, 26);
        }
    }

    private int readCurrentModeAsRoot(int subId) {
        if (!this.rootAvailable || subId < 0) {
            return -1;
        }
        int binderMode = readCurrentModeFromBinder(subId);
        if (binderMode != -1) {
            return binderMode;
        }
        try {
            Process process = new ProcessBuilder("su", "-c", "settings get global preferred_network_mode" + subId).redirectErrorStream(true).start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            try {
                String value = reader.readLine();
                if (process.waitFor() != 0 || value == null) {
                    reader.close();
                    return -1;
                }
                int i = Integer.parseInt(value.trim());
                reader.close();
                return i;
            } catch (Throwable th) {
                try {
                    reader.close();
                } catch (Throwable th2) {
                    th.addSuppressed(th2);
                }
                throw th;
            }
        } catch (Exception e) {
        }
        return -1;
    }

    private int readCurrentModeFromBinder(int subId) {
        try {
            Process process = new ProcessBuilder("su", "-c", "service call phone 93 i32 " + subId + " i32 0").redirectErrorStream(true).start();
            StringBuilder output = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            while (true) {
                try {
                    String line = reader.readLine();
                    if (line == null) {
                        break;
                    }
                    output.append(line);
                } catch (Throwable th) {
                    try {
                        reader.close();
                    } catch (Throwable th2) {
                        th.addSuppressed(th2);
                    }
                    throw th;
                }
                return -1;
            }
            reader.close();
            if (process.waitFor() == 0) {
                String result = output.toString().toLowerCase();
                if (result.contains("000cd387")) {
                    return 26;
                }
                return result.contains("0004d387") ? 9 : -1;
            }
            return -1;
        } catch (Exception e) {
            return -1;
        }
    }

    private int readCurrentModeFromService(int subId) {
        try {
            if (this.service != null) {
                return this.service.getMode(subId);
            }
            return -1;
        } catch (Exception e) {
            return -1;
        }
    }

    @Override // android.app.Activity
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
        super.onRequestPermissionsResult(requestCode, permissions, results);
        if (requestCode == 20 && results.length > 0 && results[0] == 0) {
            loadSims();
            detectAuthorization();
        } else {
            this.status.setText("需要电话权限来识别 SIM 1 和 SIM 2");
        }
    }

    @Override // android.app.Activity
    protected void onDestroy() {
        Shizuku.removeRequestPermissionResultListener(new MainActivity$$ExternalSyntheticLambda6(this));
        super.onDestroy();
    }

    private TextView text(String value, int sp, int color) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(sp);
        view.setTextColor(color);
        return view;
    }

    private MaterialCardView card() {
        MaterialCardView card = new MaterialCardView(this);
        card.setRadius(dp(18));
        card.setCardElevation(0.0f);
        card.setStrokeWidth(dp(1));
        card.setStrokeColor(colorAttr(com.google.android.material.R.attr.colorOutlineVariant));
        card.setCardBackgroundColor(colorAttr(com.google.android.material.R.attr.colorSurfaceContainerLow));
        return card;
    }

    private int colorAttr(int attr) {
        TypedValue value = new TypedValue();
        getTheme().resolveAttribute(attr, value, true);
        return value.data;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private static long bit(int networkType) {
        return 1 << (networkType - 1);
    }
}
