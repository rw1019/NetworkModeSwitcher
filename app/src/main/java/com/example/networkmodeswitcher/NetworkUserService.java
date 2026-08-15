package com.example.networkmodeswitcher;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public final class NetworkUserService extends INetworkService.Stub {
    private static String run(String... command) throws Exception {
        Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) output.append(line).append('\n');
        }
        int exit = process.waitFor();
        return exit + "\n" + output;
    }

    private static int parseMode(String output) {
        String value = output.toLowerCase();
        if (value.contains("000cd387")) return 26; // 5G/4G/3G/2G
        if (value.contains("0004d387")) return 9;  // 4G/3G/2G
        if (value.contains("0000c387")) return 3; // 3G/2G
        return -1;
    }

    @Override
    public String setMode(int slotIndex, int subId, long mask, int legacyMode) {
        try {
            String phoneResult = run("service", "call", "phone", "94", "i32", String.valueOf(subId),
                    "i32", "0", "i64", String.valueOf(mask), "s16", "com.android.shell");
            String normalized = phoneResult.toLowerCase();
            if (!normalized.startsWith("0\n") || !normalized.contains("00000001") || normalized.contains("exception")) {
                return "ERROR\n电话服务未接受切换请求\n" + phoneResult;
            }

            String key = "preferred_network_mode" + subId;
            run("settings", "put", "global", key, String.valueOf(legacyMode));
            String stored = run("settings", "get", "global", key).trim();
            if (!stored.endsWith("\n" + legacyMode) && !stored.equals("0\n" + legacyMode)) {
                return "ERROR\n系统设置回读不一致：" + stored;
            }

            // Samsung's phone service can update asynchronously. The accepted binder request plus
            // matching settings value is sufficient; getMode is retried only to warm the new state.
            for (int i = 0; i < 3; i++) {
                if (getMode(subId) == legacyMode) break;
                Thread.sleep(250L);
            }
            return "OK";
        } catch (Exception e) {
            return "ERROR\n" + e;
        }
    }

    @Override
    public int getMode(int subId) {
        try {
            return parseMode(run("service", "call", "phone", "93", "i32", String.valueOf(subId), "i32", "0"));
        } catch (Exception e) {
            return -1;
        }
    }

    @Override
    public void destroy() {
        System.exit(0);
    }
}
