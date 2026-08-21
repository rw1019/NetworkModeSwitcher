package com.example.networkmodeswitcher;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.IBinder;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;

import rikka.shizuku.Shizuku;

public final class MainActivity extends Activity {
    private static final long MASK_3G = 50055L;   // 0x0000c387
    private static final long MASK_4G = 316295L;  // 0x0004d387
    private static final long MASK_5G = 840583L;  // 0x000cd387
    private static final int PHONE_PERMISSION = 20;
    private static final int SHIZUKU_PERMISSION = 21;

    private final int[] subIdsBySlot = {-1, -1};
    private final Button[] modeButtons = new Button[3];
    private final Button[] nrModeButtons = new Button[3];
    private int selectedSlot;
    private LinearLayout simList;
    private TextView networkState;
    private TextView nrModeState;
    private LinearLayout nrModeSection;
    private TextView status;
    private int colorBackground, colorSurface, colorSurfaceVariant, colorPrimary;
    private int colorOnPrimary, colorText, colorMuted, colorOutline, colorStatus;
    private volatile boolean rootAvailable;
    private INetworkService service;
    private final Shizuku.OnRequestPermissionResultListener shizukuPermissionListener =
            this::onShizukuPermissionResult;

    private final ServiceConnection connection = new ServiceConnection() {
        @Override public void onServiceConnected(ComponentName name, IBinder binder) {
            service = INetworkService.Stub.asInterface(binder);
            status.setText("Shizuku 已连接，可以切换网络");
            refreshSelectedNetworkState();
        }
        @Override public void onServiceDisconnected(ComponentName name) {
            service = null;
            status.setText("Shizuku 连接已断开");
        }
    };

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        buildUi();
        Shizuku.addRequestPermissionResultListener(shizukuPermissionListener);
        ensurePhonePermission();
    }

    @Override protected void onResume() {
        super.onResume();
        if (subIdsBySlot[0] >= 0 || subIdsBySlot[1] >= 0) refreshSelectedNetworkState();
    }

    private void buildUi() {
        boolean dark = (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)
                == Configuration.UI_MODE_NIGHT_YES;
        colorBackground = dark ? Color.rgb(18, 18, 22) : Color.rgb(247, 247, 252);
        colorSurface = dark ? Color.rgb(30, 30, 36) : Color.rgb(255, 255, 255);
        colorSurfaceVariant = dark ? Color.rgb(50, 50, 59) : Color.rgb(232, 232, 240);
        colorPrimary = dark ? Color.rgb(184, 196, 255) : Color.rgb(65, 87, 150);
        colorOnPrimary = dark ? Color.rgb(28, 48, 101) : Color.WHITE;
        colorText = dark ? Color.rgb(232, 225, 229) : Color.rgb(29, 27, 32);
        colorMuted = dark ? Color.rgb(202, 196, 204) : Color.rgb(73, 69, 79);
        colorOutline = dark ? Color.rgb(147, 143, 153) : Color.rgb(121, 116, 126);
        colorStatus = dark ? Color.rgb(38, 48, 72) : Color.rgb(225, 229, 255);
        getWindow().setStatusBarColor(colorBackground);
        getWindow().setNavigationBarColor(colorBackground);
        getWindow().getDecorView().setSystemUiVisibility(dark ? 0
                : View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        int p = dp(24);
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(colorBackground);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(p, dp(52), p, dp(28));
        scroll.addView(root, new FrameLayout.LayoutParams(-1, -2));

        TextView title = text("网络制式切换", 32, colorText);
        title.setTypeface(null, 1);
        root.addView(title);
        TextView subtitle = text("双卡移动网络控制", 15, colorMuted);
        subtitle.setPadding(0, dp(6), 0, dp(28));
        root.addView(subtitle);

        TextView simHeading = text("SIM 卡", 14, colorMuted);
        simHeading.setTypeface(null, 1);
        simHeading.setPadding(dp(4), 0, 0, dp(8));
        root.addView(simHeading);
        LinearLayout simCard = card();
        simList = new LinearLayout(this);
        simList.setOrientation(LinearLayout.VERTICAL);
        simList.setPadding(dp(12), dp(8), dp(12), dp(8));
        simCard.addView(simList, new FrameLayout.LayoutParams(-1, -2));
        root.addView(simCard, new LinearLayout.LayoutParams(-1, -2));

        networkState = text("当前网络：正在读取…", 15, colorPrimary);
        networkState.setTypeface(null, 1);
        LinearLayout.LayoutParams networkParams = new LinearLayout.LayoutParams(-1, -2);
        networkParams.setMargins(dp(4), dp(14), dp(4), 0);
        root.addView(networkState, networkParams);

        TextView modeHeading = text("最高网络制式", 14, colorMuted);
        modeHeading.setTypeface(null, 1);
        modeHeading.setPadding(dp(4), dp(22), 0, dp(8));
        root.addView(modeHeading);
        LinearLayout modeRow = new LinearLayout(this);
        root.addView(modeRow, new LinearLayout.LayoutParams(-1, dp(56)));
        String[] labels = {"3G", "4G", "5G"};
        long[] masks = {MASK_3G, MASK_4G, MASK_5G};
        int[] modes = {3, 9, 26};
        for (int i = 0; i < labels.length; i++) {
            Button button = new Button(this);
            button.setText(labels[i]);
            button.setTextSize(16);
            button.setAllCaps(false);
            final int index = i;
            button.setOnClickListener(v -> applyMode(labels[index], masks[index], modes[index]));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, dp(56), 1f);
            lp.setMargins(i == 0 ? 0 : dp(5), 0, i == 2 ? 0 : dp(5), 0);
            modeRow.addView(button, lp);
            modeButtons[i] = button;
        }

        nrModeSection = new LinearLayout(this);
        nrModeSection.setOrientation(LinearLayout.VERTICAL);
        nrModeSection.setVisibility(View.GONE);
        root.addView(nrModeSection, new LinearLayout.LayoutParams(-1, -2));
        TextView nrHeading = text("5G 组网模式", 14, colorMuted);
        nrHeading.setTypeface(null, 1);
        nrHeading.setPadding(dp(4), dp(22), 0, dp(8));
        nrModeSection.addView(nrHeading);
        nrModeState = text("正在识别当前上网卡…", 14, colorPrimary);
        nrModeState.setPadding(dp(4), 0, dp(4), dp(8));
        nrModeSection.addView(nrModeState);
        LinearLayout nrModeRow = new LinearLayout(this);
        nrModeSection.addView(nrModeRow, new LinearLayout.LayoutParams(-1, dp(56)));
        String[] nrLabels = {"自动", "NSA", "SA"};
        for (int i = 0; i < nrLabels.length; i++) {
            Button button = new Button(this);
            button.setText(nrLabels[i]);
            button.setTextSize(15);
            button.setAllCaps(false);
            final int nrMode = i;
            button.setOnClickListener(v -> applyNrMode(nrMode));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, dp(56), 1f);
            lp.setMargins(i == 0 ? 0 : dp(5), 0, i == 2 ? 0 : dp(5), 0);
            nrModeRow.addView(button, lp);
            nrModeButtons[i] = button;
        }

        LinearLayout statusCard = card();
        statusCard.setBackgroundTintList(ColorStateList.valueOf(colorStatus));
        LinearLayout statusBox = new LinearLayout(this);
        statusBox.setOrientation(LinearLayout.VERTICAL);
        statusBox.setPadding(dp(18), dp(14), dp(18), dp(14));
        TextView statusLabel = text("授权与操作状态", 12, colorPrimary);
        statusLabel.setTypeface(null, 1);
        statusBox.addView(statusLabel);
        status = text("正在检查权限…", 15, colorText);
        status.setPadding(0, dp(4), 0, 0);
        statusBox.addView(status);
        statusCard.addView(statusBox);
        LinearLayout.LayoutParams statusParams = new LinearLayout.LayoutParams(-1, -2);
        statusParams.setMargins(0, dp(26), 0, dp(20));
        root.addView(statusCard, statusParams);

        Button refresh = new Button(this);
        refresh.setText("刷新当前网络状态");
        refresh.setTextSize(15);
        refresh.setTextColor(Color.WHITE);
        refresh.setBackgroundTintList(ColorStateList.valueOf(colorPrimary));
        refresh.setTextColor(colorOnPrimary);
        refresh.setOnClickListener(v -> refreshSelectedNetworkState());
        root.addView(refresh, new LinearLayout.LayoutParams(-1, dp(56)));

        Button auth = new Button(this);
        auth.setText("重新检测 Root / Shizuku");
        auth.setOnClickListener(v -> detectAuthorization());
        LinearLayout.LayoutParams authParams = new LinearLayout.LayoutParams(-1, dp(56));
        authParams.setMargins(0, dp(10), 0, 0);
        root.addView(auth, authParams);
        setContentView(scroll);
    }

    private void ensurePhonePermission() {
        if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != getPackageManager().PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_PHONE_STATE}, PHONE_PERMISSION);
        } else {
            loadSims();
            detectAuthorization();
        }
    }

    private void loadSims() {
        simList.removeAllViews();
        SubscriptionManager manager = getSystemService(SubscriptionManager.class);
        List<SubscriptionInfo> infos = manager.getActiveSubscriptionInfoList();
        if (infos == null || infos.isEmpty()) {
            status.setText("未检测到已启用的 SIM 卡");
            return;
        }
        RadioGroup group = new RadioGroup(this);
        for (SubscriptionInfo info : infos) {
            int slot = info.getSimSlotIndex();
            if (slot >= 0 && slot < subIdsBySlot.length) subIdsBySlot[slot] = info.getSubscriptionId();
            RadioButton radio = new RadioButton(this);
            radio.setId(View.generateViewId());
            radio.setTag(slot);
            radio.setText("SIM " + (slot + 1) + "  ·  " + info.getDisplayName());
            radio.setTextSize(17);
            radio.setPadding(dp(8), dp(8), 0, dp(8));
            group.addView(radio, new RadioGroup.LayoutParams(-1, dp(56)));
        }
        selectedSlot = infos.get(0).getSimSlotIndex();
        group.setOnCheckedChangeListener((g, checkedId) -> {
            RadioButton checked = g.findViewById(checkedId);
            if (checked != null) {
                selectedSlot = (Integer) checked.getTag();
                refreshSelectedNetworkState();
            }
        });
        group.check(group.getChildAt(0).getId());
        simList.addView(group);
    }

    private void refreshSelectedNetworkState() {
        refreshNrModeState();
        int slot = selectedSlot;
        int subId = slot >= 0 && slot < subIdsBySlot.length ? subIdsBySlot[slot] : -1;
        if (subId < 0) return;
        String actual = readActualGeneration(subId);
        networkState.setText("SIM " + (slot + 1) + " 当前网络：" + actual + " · 最高制式：读取中…");
        setHighlightedMode(-1);
        new Thread(() -> {
            int preferred = rootAvailable ? readCurrentModeAsRoot(subId) : readCurrentModeFromService(subId);
            runOnUiThread(() -> {
                if (slot != selectedSlot) return;
                String max = modeLabel(preferred);
                networkState.setText("SIM " + (slot + 1) + " 当前网络：" + readActualGeneration(subId) + " · 最高制式：" + max);
                setHighlightedMode(preferred);
            });
        }).start();
    }

    private void refreshNrModeState() {
        int dataSubId = SubscriptionManager.getDefaultDataSubscriptionId();
        int dataSlot = slotForSubId(dataSubId);
        if (dataSlot < 0) {
            nrModeSection.setVisibility(View.GONE);
            setHighlightedNrMode(-1);
            return;
        }
        String actual = readActualGeneration(dataSubId);
        boolean on5g = "5G".equals(actual);
        nrModeSection.setVisibility(on5g ? View.VISIBLE : View.GONE);
        for (Button button : nrModeButtons) button.setEnabled(on5g);
        if (!on5g) return;
        int savedMode = getSharedPreferences("nr_mode", MODE_PRIVATE)
                .getInt("stack_" + (dataSlot + 1), 0);
        String configured = savedMode == 2 ? "仅 SA" : savedMode == 1 ? "仅 NSA" : "SA+NSA 自动";
        nrModeState.setText("当前上网卡：SIM " + (dataSlot + 1) + " · " + configured);
        setHighlightedNrMode(savedMode);
    }

    private int slotForSubId(int subId) {
        for (int i = 0; i < subIdsBySlot.length; i++) if (subIdsBySlot[i] == subId) return i;
        return -1;
    }

    private void setHighlightedNrMode(int mode) {
        for (int i = 0; i < nrModeButtons.length; i++) {
            boolean active = i == mode;
            nrModeButtons[i].setTextColor(active ? colorOnPrimary : colorText);
            nrModeButtons[i].setBackgroundTintList(ColorStateList.valueOf(
                    active ? colorPrimary : colorSurfaceVariant));
        }
    }

    private void applyNrMode(int mode) {
        if (!rootAvailable) {
            status.setText("SA/NSA 是三星基带设置，需要 Root 授权");
            return;
        }
        int dataSubId = SubscriptionManager.getDefaultDataSubscriptionId();
        int dataSlot = slotForSubId(dataSubId);
        if (dataSlot < 0) {
            status.setText("无法识别当前上网卡，请先在系统设置中选择移动数据卡");
            return;
        }
        String label = mode == 2 ? "仅 SA" : mode == 1 ? "仅 NSA" : "SA+NSA 自动";
        status.setText("正在把当前上网卡 SIM " + (dataSlot + 1) + " 设置为" + label + "…");
        new Thread(() -> {
            try {
                setSamsungNrMode(dataSlot, mode);
                getSharedPreferences("nr_mode", MODE_PRIVATE).edit()
                        .putInt("stack_" + (dataSlot + 1), mode).apply();
                runOnUiThread(() -> {
                    status.setText("SIM " + (dataSlot + 1) + " 已设置为" + label
                            + "，网络会重新驻留；是否连上取决于当地基站支持");
                    setHighlightedNrMode(mode);
                    status.postDelayed(this::refreshSelectedNetworkState, 3500L);
                });
            } catch (Exception e) {
                runOnUiThread(() -> status.setText("SA/NSA 设置失败：" + e.getMessage()));
            }
        }).start();
    }

    private void setSamsungNrMode(int dataSlot, int mode) throws Exception {
        int stackY = dataSlot == 0 ? 525 : 630;
        int modeY = mode == 0 ? 630 : mode == 1 ? 735 : 840;
        String component = "com.sec.android.RilServiceModeApp/.ServiceModeApp";
        String command = "am force-stop com.sec.android.RilServiceModeApp; "
                + "am start --user 0 -n " + component + " --es keyString 27663368378; sleep 1; "
                + "input tap 700 630; sleep 1; input tap 700 525; sleep 1; input tap 700 525; sleep 1; "
                + "input tap 700 1365; sleep 1; input tap 700 525; sleep 1; input tap 700 " + stackY + "; sleep 1; "
                + "input keyevent 4; sleep 1; input tap 700 630; sleep 1; input tap 700 " + modeY + "; sleep 1; "
                + "am start --user 0 -n com.example.networkmodeswitcher/.MainActivity";
        runRoot(command);
    }

    private String readActualGeneration(int subId) {
        try {
            TelephonyManager tm = getSystemService(TelephonyManager.class).createForSubscriptionId(subId);
            int type = tm.getDataNetworkType();
            if (type == TelephonyManager.NETWORK_TYPE_UNKNOWN) type = tm.getVoiceNetworkType();
            switch (type) {
                case TelephonyManager.NETWORK_TYPE_NR: return "5G";
                case TelephonyManager.NETWORK_TYPE_LTE:
                case TelephonyManager.NETWORK_TYPE_IWLAN: return "4G";
                case TelephonyManager.NETWORK_TYPE_UMTS:
                case TelephonyManager.NETWORK_TYPE_EVDO_0:
                case TelephonyManager.NETWORK_TYPE_EVDO_A:
                case TelephonyManager.NETWORK_TYPE_HSDPA:
                case TelephonyManager.NETWORK_TYPE_HSUPA:
                case TelephonyManager.NETWORK_TYPE_HSPA:
                case TelephonyManager.NETWORK_TYPE_EVDO_B:
                case TelephonyManager.NETWORK_TYPE_EHRPD:
                case TelephonyManager.NETWORK_TYPE_HSPAP:
                case TelephonyManager.NETWORK_TYPE_TD_SCDMA: return "3G";
                case TelephonyManager.NETWORK_TYPE_GPRS:
                case TelephonyManager.NETWORK_TYPE_EDGE:
                case TelephonyManager.NETWORK_TYPE_CDMA:
                case TelephonyManager.NETWORK_TYPE_1xRTT:
                case TelephonyManager.NETWORK_TYPE_IDEN:
                case TelephonyManager.NETWORK_TYPE_GSM: return "2G";
                default: return "未驻网";
            }
        } catch (Exception e) {
            return "暂时无法读取";
        }
    }

    private void setHighlightedMode(int mode) {
        int selected = mode == 3 ? 0 : mode == 9 || mode == 12 ? 1 : mode == 26 || mode == 28 ? 2 : -1;
        for (int i = 0; i < modeButtons.length; i++) {
            boolean active = i == selected;
            modeButtons[i].setSelected(active);
            modeButtons[i].setTextColor(active ? colorOnPrimary : colorText);
            modeButtons[i].setBackgroundTintList(ColorStateList.valueOf(
                    active ? colorPrimary : colorSurfaceVariant));
        }
    }

    private String modeLabel(int mode) {
        if (mode == 3) return "3G";
        if (mode == 9 || mode == 12) return "4G";
        if (mode == 26 || mode == 28) return "5G";
        return "未知";
    }

    private void applyMode(String label, long mask, int legacyMode) {
        if (!rootAvailable && service == null) {
            status.setText("尚未获得切换权限，正在重新检测…");
            detectAuthorization();
            return;
        }
        int slot = selectedSlot;
        int subId = subIdsBySlot[slot];
        status.setText("正在切换 SIM " + (slot + 1) + " 到 " + label + "…");
        new Thread(() -> {
            try {
                int previousMode = rootAvailable ? readCurrentModeAsRoot(subId) : readCurrentModeFromService(subId);
                String result = rootAvailable ? setModeAsRoot(subId, mask, legacyMode)
                        : service.setMode(slot, subId, mask, legacyMode);
                runOnUiThread(() -> {
                    if ("OK".equals(result)) {
                        status.setText("SIM " + (slot + 1) + " 已切换到 " + label + " 档，正在等待驻网刷新");
                        setHighlightedMode(legacyMode);
                        Toast.makeText(this, "切换成功", Toast.LENGTH_SHORT).show();
                        status.postDelayed(this::refreshSelectedNetworkState, 1200L);
                    } else {
                        status.setText("切换失败\n" + result);
                        refreshSelectedNetworkState();
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> status.setText("切换失败：" + e.getMessage()));
            }
        }).start();
    }

    private void detectAuthorization() {
        status.setText("正在请求 Root 授权…");
        new Thread(() -> {
            rootAvailable = testRoot();
            runOnUiThread(() -> {
                if (rootAvailable) {
                    status.setText("Root 已授权，可以切换网络");
                    refreshSelectedNetworkState();
                } else {
                    status.setText("未获得 Root，正在尝试 Shizuku…");
                    connectShizuku();
                }
            });
        }).start();
    }

    private boolean testRoot() {
        try {
            Process p = new ProcessBuilder("su", "-c", "id").redirectErrorStream(true).start();
            String output = readOutput(p);
            return p.waitFor() == 0 && output.contains("uid=0");
        } catch (Exception e) { return false; }
    }

    private void connectShizuku() {
        if (!Shizuku.pingBinder()) status.setText("请先安装并启动 Shizuku");
        else if (Shizuku.checkSelfPermission() != getPackageManager().PERMISSION_GRANTED)
            Shizuku.requestPermission(SHIZUKU_PERMISSION);
        else bindUserService();
    }

    private void onShizukuPermissionResult(int requestCode, int grantResult) {
        if (requestCode == SHIZUKU_PERMISSION && grantResult == getPackageManager().PERMISSION_GRANTED) bindUserService();
        else status.setText("需要 Shizuku 授权才能切换网络");
    }

    private void bindUserService() {
        Shizuku.UserServiceArgs args = new Shizuku.UserServiceArgs(new ComponentName(this, NetworkUserService.class))
                .processNameSuffix("network").debuggable(true).version(2);
        Shizuku.bindUserService(args, connection);
        status.setText("正在连接 Shizuku…");
    }

    private String setModeAsRoot(int subId, long mask, int legacyMode) {
        try {
            String phone = runRoot("service call phone 94 i32 " + subId + " i32 0 i64 " + mask + " s16 com.android.shell");
            String normalized = phone.toLowerCase();
            if (!normalized.contains("00000001") || normalized.contains("exception")) return "ERROR\n" + phone;
            String key = "preferred_network_mode" + subId;
            runRoot("settings put global " + key + " " + legacyMode);
            String stored = runRoot("settings get global " + key).trim();
            if (!stored.equals(String.valueOf(legacyMode))) return "ERROR\n系统设置回读不一致：" + stored;
            for (int i = 0; i < 3; i++) {
                if (readCurrentModeAsRoot(subId) == legacyMode) break;
                Thread.sleep(250L);
            }
            return "OK";
        } catch (Exception e) { return "ERROR\n" + e; }
    }

    private int readCurrentModeAsRoot(int subId) {
        if (!rootAvailable || subId < 0) return -1;
        try {
            int binder = parseMode(runRoot("service call phone 93 i32 " + subId + " i32 0"));
            if (binder != -1) return binder;
            return Integer.parseInt(runRoot("settings get global preferred_network_mode" + subId).trim());
        } catch (Exception e) { return -1; }
    }

    private int readCurrentModeFromService(int subId) {
        try { return service == null ? -1 : service.getMode(subId); }
        catch (Exception e) { return -1; }
    }

    private static int parseMode(String output) {
        String value = output.toLowerCase();
        if (value.contains("000cd387")) return 26;
        if (value.contains("0004d387")) return 9;
        if (value.contains("0000c387")) return 3;
        return -1;
    }

    private String runRoot(String command) throws Exception {
        Process p = new ProcessBuilder("su", "-c", command).redirectErrorStream(true).start();
        String output = readOutput(p);
        if (p.waitFor() != 0) throw new IllegalStateException(output);
        return output;
    }

    private static String readOutput(Process p) throws Exception {
        StringBuilder out = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
            String line; while ((line = reader.readLine()) != null) out.append(line).append('\n');
        }
        return out.toString();
    }

    @Override public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
        super.onRequestPermissionsResult(requestCode, permissions, results);
        if (requestCode == PHONE_PERMISSION && results.length > 0 && results[0] == getPackageManager().PERMISSION_GRANTED) {
            loadSims(); detectAuthorization();
        } else status.setText("需要电话权限来识别 SIM 1 和 SIM 2");
    }

    @Override protected void onDestroy() {
        Shizuku.removeRequestPermissionResultListener(shizukuPermissionListener);
        super.onDestroy();
    }

    private TextView text(String value, int sp, int color) {
        TextView view = new TextView(this); view.setText(value); view.setTextSize(sp); view.setTextColor(color); return view;
    }
    private LinearLayout card() {
        LinearLayout view = new LinearLayout(this);
        GradientDrawable background = new GradientDrawable();
        background.setColor(colorSurface);
        background.setCornerRadius(dp(24));
        background.setStroke(dp(1), colorOutline);
        view.setBackground(background);
        return view;
    }
    private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }
}
