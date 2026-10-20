package com.pdc.learning.util;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

/**
 * PBKDF2WithHmacSHA256 加盐哈希。
 *
 * <p>参数按需求固定：210,000 次迭代、16 字节盐、256 位派生密钥，salt/hash 分列以 Base64 保存。
 * 比较使用 {@link MessageDigest#isEqual(byte[], byte[])}，避免时序侧信道。</p>
 */
public class PasswordHasher {

    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int ITERATIONS = 210_000;
    private static final int SALT_BYTES = 16;
    private static final int KEY_BITS = 256;

    private final SecureRandom random = new SecureRandom();

    public String generateSalt() {
        byte[] salt = new byte[SALT_BYTES];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    public String hash(String password, String salt) {
        if (password == null || salt == null) {
            throw new IllegalArgumentException("密码与盐不能为空");
        }
        try {
            byte[] saltBytes = Base64.getDecoder().decode(salt);
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), saltBytes, ITERATIONS, KEY_BITS);
            SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
            byte[] derived = factory.generateSecret(spec).getEncoded();
            spec.clearPassword();
            return Base64.getEncoder().encodeToString(derived);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | IllegalArgumentException e) {
            throw new IllegalStateException("密码哈希失败", e);
        }
    }

    public boolean verify(String rawPassword, String salt, String expectedHash) {
        if (rawPassword == null || salt == null || expectedHash == null) {
            return false;
        }
        try {
            String actual = hash(rawPassword, salt);
            return MessageDigest.isEqual(
                    Base64.getDecoder().decode(actual),
                    Base64.getDecoder().decode(expectedHash));
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
