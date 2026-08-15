package com.example.networkmodeswitcher;

import androidx.core.app.NotificationCompat;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/* JADX INFO: loaded from: classes3.dex */
public final class NetworkUserService extends INetworkService.Stub {
    /* JADX WARN: Code duplicated, block: B:61:0x01cf  */
    @Override // com.example.networkmodeswitcher.INetworkService
    public String setMode(int slotIndex, int subId, long mask, int legacyMode) throws Throwable {
        Throwable th;
        boolean z = true;
        String[][] commands = {new String[]{NotificationCompat.CATEGORY_SERVICE, NotificationCompat.CATEGORY_CALL, "phone", "94", "i32", String.valueOf(subId), "i32", "0", "i64", String.valueOf(mask), "s16", "com.android.shell"}, new String[]{NotificationCompat.CATEGORY_SERVICE, NotificationCompat.CATEGORY_CALL, "phone", "94", "i32", String.valueOf(subId), "i32", "0", "i64", String.valueOf(mask), "s16", "com.android.shell"}};
        StringBuilder errors = new StringBuilder();
        boolean phoneCommandSucceeded = false;
        int length = commands.length;
        int i = 0;
        while (i < length) {
            String[] command = commands[i];
            try {
                Process process = new ProcessBuilder(command).redirectErrorStream(z).start();
                StringBuilder output = new StringBuilder();
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
                while (true) {
                    try {
                        String line = reader.readLine();
                        if (line == null) {
                            break;
                        }
                        StringBuilder output2 = output;
                        try {
                            output2.append(line).append('\n');
                            output = output2;
                        } catch (Throwable th2) {
                            th = th2;
                            try {
                                reader.close();
                            } catch (Throwable th3) {
                                th.addSuppressed(th3);
                            }
                            throw th;
                        }
                    } catch (Throwable th4) {
                        th = th4;
                    }
                }
                StringBuilder output3 = output;
                reader.close();
                int exit = process.waitFor();
                String normalized = output3.toString().toLowerCase();
                if (exit == 0 && normalized.contains("00000001") && !normalized.contains("exception")) {
                    phoneCommandSucceeded = true;
                    break;
                }
                errors.append(String.join(" ", command)).append(": ").append((CharSequence) output3);
                i++;
                z = true;
            } catch (Exception e) {
                errors.append(e).append('\n');
            }
        }
        if (!phoneCommandSucceeded) {
            return "ERROR\n" + ((Object) errors);
        }
        String key = "preferred_network_mode" + subId;
        try {
            if (new ProcessBuilder("settings", "put", "global", key, String.valueOf(legacyMode)).redirectErrorStream(true).start().waitFor() == 0) {
                Process verify = new ProcessBuilder("settings", "get", "global", key).redirectErrorStream(true).start();
                BufferedReader reader2 = new BufferedReader(new InputStreamReader(verify.getInputStream(), StandardCharsets.UTF_8));
                try {
                    String value = reader2.readLine();
                    reader2.close();
                    if (verify.waitFor() == 0) {
                        if (String.valueOf(legacyMode).equals(value != null ? value.trim() : "")) {
                            try {
                                if (getMode(subId) == legacyMode) {
                                    return "OK";
                                }
                            } catch (Exception e2) {
                                e = e2;
                                errors.append(e).append('\n');
                            }
                        }
                    }
                } catch (Throwable th5) {
                    try {
                        try {
                            reader2.close();
                            throw th5;
                        } catch (Exception e3) {
                            e = e3;
                            errors.append(e).append('\n');
                            return "ERROR\n" + ((Object) errors);
                        }
                    } catch (Throwable th6) {
                        th5.addSuppressed(th6);
                        throw th5;
                    }
                }
            }
        } catch (Exception e4) {
            e = e4;
        }
        return "ERROR\n" + ((Object) errors);
    }

    @Override // com.example.networkmodeswitcher.INetworkService
    public int getMode(int subId) {
        try {
            Process process = new ProcessBuilder(NotificationCompat.CATEGORY_SERVICE, NotificationCompat.CATEGORY_CALL, "phone", "93", "i32", String.valueOf(subId), "i32", "0").redirectErrorStream(true).start();
            StringBuilder output = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
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

    @Override // com.example.networkmodeswitcher.INetworkService
    public void destroy() {
        System.exit(0);
    }
}
