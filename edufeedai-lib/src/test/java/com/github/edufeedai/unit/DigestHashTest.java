/*
 * EduFeedAi - Automated AI-based feedback for student submissions in vocational education.
 * Copyright (c) 2026 EduFeedAi Contributors
 * SPDX-License-Identifier: MIT
 */

package com.github.edufeedai.unit;

import org.junit.jupiter.api.Test;

import com.github.edufeedai.model.Digest;
import com.github.edufeedai.model.DigestMD5;
import com.github.edufeedai.model.DigestSHA1;
import com.github.edufeedai.model.DigestSHA256;
import com.github.edufeedai.model.DigestSHA512;

import java.security.DigestException;
import static org.junit.jupiter.api.Assertions.*;

public class DigestHashTest {
    @Test
    void testSHA1() throws DigestException {
        Digest d = new DigestSHA1();
        String hash1 = d.digest("hola mundo");
        String hash2 = d.digest("hola mundo");
        String hash3 = d.digest("otro mensaje");
        assertNotNull(hash1);
        assertEquals(hash1, hash2);
        assertNotEquals(hash1, hash3);
        assertEquals(40, hash1.length());
        assertEquals("459567d3bde4418b7fe302ff9809c4b0befaf7dd", hash1);
    }

    @Test
    void testSHA256() throws DigestException {
        Digest d = new DigestSHA256();
        String hash1 = d.digest("hola mundo");
        String hash2 = d.digest("hola mundo");
        String hash3 = d.digest("otro mensaje");
        assertNotNull(hash1);
        assertEquals(hash1, hash2);
        assertNotEquals(hash1, hash3);
        assertEquals(64, hash1.length());
        assertEquals("0b894166d3336435c800bea36ff21b29eaa801a52f584c006c49289a0dcf6e2f", hash1);
    }

    @Test
    void testSHA512() throws DigestException {
        Digest d = new DigestSHA512();
        String hash1 = d.digest("hola mundo");
        String hash2 = d.digest("hola mundo");
        String hash3 = d.digest("otro mensaje");
        assertNotNull(hash1);
        assertEquals(hash1, hash2);
        assertNotEquals(hash1, hash3);
        assertEquals(128, hash1.length());
        assertEquals("e361ecc31f2aac2066a3103d3b14dc63b5984b028f9f2d09dee67460ce2702bc81673acf58109b553324852c62a227d9a75d4c2f686580270fe143048f47c33c", hash1);
    }

    @Test
    void testMD5() throws DigestException {
        Digest d = new DigestMD5();
        String hash1 = d.digest("hola mundo");
        String hash2 = d.digest("hola mundo");
        String hash3 = d.digest("otro mensaje");
        assertNotNull(hash1);
        assertEquals(hash1, hash2);
        assertNotEquals(hash1, hash3);
        assertEquals(32, hash1.length());
        assertEquals("0ad066a5d29f3f2a2a1c7c17dd082a79", hash1);
    }
}
