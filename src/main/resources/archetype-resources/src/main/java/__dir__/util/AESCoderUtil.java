package ${package}.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.Charset;
import java.util.UUID;

@Slf4j
public class AESCoderUtil {

    private static final int DEFAULT_KEY_LENGTH = 16;

    public static String encode(String content) {
        return encode(null, content);
    }

    /*
     * 加密
     */
    public static String encode(String keyCoder, String content) {
        try {
            if (StringUtils.isBlank(keyCoder)) {
                keyCoder = UUID.randomUUID().toString().substring(0, DEFAULT_KEY_LENGTH);
            }
            SecretKey key = new SecretKeySpec(keyCoder.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] byteEncode = content.getBytes(Charset.defaultCharset());
            byte[] byteAES = cipher.doFinal(byteEncode);
            String aesEncode = new String(Base64.encodeBase64(byteAES));
            return keyCoder + aesEncode;
        } catch (Exception e) {
            log.error("AESEncode error.", e);
        }

        return null;
    }

    public static String decode(String content) {
        return decode(null, content);
    }

    /*
     * 解密
     */
    public static String decode(String keyCoder, String content) {
        try {
            if (StringUtils.isBlank(keyCoder)) {
                keyCoder = content.substring(0, DEFAULT_KEY_LENGTH);
                content = content.substring(DEFAULT_KEY_LENGTH);
            }
            SecretKey key = new SecretKeySpec(keyCoder.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] byte_content = Base64.decodeBase64(content);
            byte[] byte_decode = cipher.doFinal(byte_content);
            String AES_decode = new String(byte_decode, Charset.defaultCharset());
            return AES_decode;
        } catch (Exception e) {
            log.error("AESDecode error.", e);
        }
        return null;
    }

    public static void main(String[] args) {
        String value = "jdbc:mysql://localhost:3306/test?characterEncoding=utf8&useSSL=true";
        System.out.println(AESCoderUtil.encode(value));
        System.out.println(AESCoderUtil.encode("root"));
        System.out.println(AESCoderUtil.encode("123456"));
    }
}