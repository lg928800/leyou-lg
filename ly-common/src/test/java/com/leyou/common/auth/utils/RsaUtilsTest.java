package com.leyou.common.auth.utils;

import org.junit.Test;

import static org.junit.Assert.*;

public class RsaUtilsTest {
    private static final String privateFilePath = "D:\\ideawork\\leyou-lg\\ssh\\id_rsa";
    private static final String publicFilePath = "D:\\ideawork\\leyou-lg\\ssh\\id_rsa.pub";
    @Test
    public void generateKey() throws Exception {

        RsaUtils.generateKey(publicFilePath,privateFilePath,"hello world",0);
    }
}