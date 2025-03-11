package com.stephanofer.customCosmeticsVelocity;

public class PlayerPrefixData {

    private final String prefixRpg;
    private final String prefixGlobal;

    public PlayerPrefixData(String prefixRpg, String prefixGlobal){
        this.prefixRpg = prefixRpg;
        this.prefixGlobal = prefixGlobal;
    }

    public String getPrefixRpg() {
        return prefixRpg;
    }

    public String getPrefixGlobal() {
        return prefixGlobal;
    }
}
