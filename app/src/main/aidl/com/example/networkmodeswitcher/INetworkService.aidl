package com.example.networkmodeswitcher;

interface INetworkService {
    String setMode(int slotIndex, int subId, long mask, int legacyMode);
    int getMode(int subId);
    void destroy();
}
