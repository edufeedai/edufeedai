/*
 * EduFeedAi is an automated system for retrieving, evaluating, and generating AI-based feedback for student submissions in vocational education.
 *
 * Copyright (C) 2026 Arturo Candela
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.github.edufeedai.model;

import java.security.DigestException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class DigestMD5 implements Digest {
    MessageDigest messageDigest = null;
    @Override
    public String digest(String message) throws DigestException {
        try {
            messageDigest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new DigestException(e);
        }
        byte[] hashBytes = messageDigest.digest(message.getBytes());
        StringBuilder sb = new StringBuilder();
        for (byte b : hashBytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
