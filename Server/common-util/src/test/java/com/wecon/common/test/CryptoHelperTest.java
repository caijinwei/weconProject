package com.wecon.common.test;

import com.wecon.common.util.CryptoHelper;
import org.apache.commons.codec.binary.Base64;
import org.junit.Assert;
import org.junit.Test;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;

/**
 * Created by fengbing on 2015/12/15.
 */
public class CryptoHelperTest
{

    @Test
    public void testTriplAndBase64() throws Exception
    {
        String key = "67656E697573646F776E6B657967656E697573646F776E6B";
        String source = "http://bcs.91.com/wisedown/data/wisegame/f14107016694005c/tongchenghunlianjiaoyouyue_20.apk";
        //System.out.println(CryptoHelper.aesEncrypt(source, key));


        //new Base64().encodeToString(CryptoHelper.tripleDesEncrypt(source, key));
        String result = CryptoHelper.tripleDesEncryptToBase64(source, key, "");

        System.out.println(result);


        String ss = CryptoHelper.tripleDesDecryptFromBase64ToString(result, key, "");
        System.out.println(ss);

//        String resultBase64 = new Base64().encodeAsString(result);
//        System.out.println(resultBase64);
//
//        byte[] dResult = new Base64().decode("/fOwSCSpui3XbNw+6ZTPAiOukremUAq5B8d8/xA+6O8DCAQNP0+gAdq6aqOLRrv7RzR1rGcdcU1dknUlpyBgD1s2zxlHwiLyhZsK/f2uLumexff8Id08CWfPUQFQ5JmE".getBytes("utf-8"));
//        String dResultStr = CryptoHelper.tripleDesDecrypt(dResult, key, key.substring(0, 8), "utf-8");
//        System.out.println(dResultStr);
    }

    @Test
    public void testBase64() throws Exception
    {
        String auth = "fengling:2015Test!";

        String authBase64 = new Base64().encodeAsString(auth.getBytes("utf-8"));

        String authBase64_1 = new sun.misc.BASE64Encoder().encode(auth.getBytes("utf-8"));

        Assert.assertEquals(authBase64, authBase64_1);
    }

    @Test
    public void hash_HMAC_Sha1_Base64() throws Exception
    {
        String sucResult = "BQGTM2iFMqXEJAw8YABk9J2cm3I=";
        String computerResult = "";

        computerResult = CryptoHelper.hash_HMAC_Sha1_Base64("200x100/felinkagres/0165655.jpg", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
        Assert.assertEquals(sucResult, computerResult);

        computerResult = CryptoHelper.hash_HMAC_Sha1_Base64("我的富大龙发疯的疯的发", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
        Assert.assertEquals("MGTSKQJ8p4SIUHspPxk3Ho97hNA=", computerResult);
    }

    @Test
    public void hash_HMAC_Sha1_Base64UrlSafe() throws Exception
    {
        String expectResult = "BQGTM2iFMqXEJAw8YABk9J2cm3I=";
        String actualResult = "";

        actualResult = CryptoHelper.hash_HMAC_Sha1_Base64UrlSafe("200x100/felinkagres/0165655.jpg", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
        Assert.assertEquals(expectResult, actualResult);

        actualResult = CryptoHelper.hash_HMAC_Sha1_Base64UrlSafe("GetActivityPageProtobufReturn?platform=iPhone&fw=iPhone_7_0_0&actype=0&showPlace=2&pageIndex=1&pageSize=2147483647&count=0&clientVersion=5.1.2.1&chl=91assistant_Iosphone22&check=E04A5E782F8E7DAC261D77FEED1232A5&clientIp=&projectId=1800&cpu=&imsi=&cuid=96219def20140511b940d862b33980b6edd32d37&clienttype=pxl&requestId=0&imei=69b4402022f4633f11fceeb5b3df42d83cbf1f47", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
        Assert.assertNotEquals("kGx2nnVOVygRm/R1wJZBOYcS8pM=", actualResult);
        Assert.assertEquals("kGx2nnVOVygRm_R1wJZBOYcS8pM=", actualResult);

    }

    @Test
    public void aesEncrypt() throws NoSuchPaddingException, InvalidAlgorithmParameterException,
            UnsupportedEncodingException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException,
            InvalidKeyException
    {
        String expectResult = "VlQ+I3sMWMMSAKwX3Esf/SmIVc0PRdIGN7p9nMUGIYPs5ykVAad9NsHRDWzjl7ZI";
        String actualResult = "";
        actualResult = CryptoHelper.aesEncrypt("testa7b94c7edfec5312b5960a8f21d0c83c", "0123456789012345");
        Assert.assertEquals(expectResult, actualResult);

        actualResult = CryptoHelper.aesEncrypt("testa7b94c7edfec5312b5960a8f21d0c83c", "0123456789012345", "0123456789012345");
        Assert.assertEquals(expectResult, actualResult);

        actualResult = CryptoHelper.aesEncrypt("testa7b94c7edfec5312b5960a8f21d0c83c", "0123456789012345",
                "0123456789012345", "utf-8", "AES/CBC/PKCS5Padding");
        Assert.assertEquals(expectResult, actualResult);

        actualResult = CryptoHelper.aesEncrypt("testa7b94c7edfec5312b5960a8f21d0c83c", "0123456789012345",
                "0123456789012345", "gb2312", "AES/CBC/PKCS5Padding");
        Assert.assertEquals(expectResult, actualResult);

        actualResult = CryptoHelper.aesEncrypt("这个是一段中文", "0123456789012345",
                "0123456789012345", "utf-8", "AES/CBC/PKCS5Padding");
        Assert.assertEquals("ebE0JqsHF07NASF/w+VeLsNrXvQ1trPqfUNeoHekj6Y=", actualResult);

        actualResult = CryptoHelper.aesEncrypt("这个是一段中文", "0123456789012345",
                "0123456789012345", "gb2312", "AES/CBC/PKCS5Padding");
        Assert.assertNotEquals("ebE0JqsHF07NASF/w+VeLsNrXvQ1trPqfUNeoHekj6Y=", actualResult);


        System.out.println(CryptoHelper.aesEncrypt("1231414124131231", "140AD97C4A011FD7", "1234567800000000"));
    }

    @Test
    public void aesEncryptTest()
    {
        String fromStr = "testiD";
        String key = "testiD";
        String iv = "12345678";
        String source = "1231414124131231";
        System.out.println(toMd5(fromStr + key));
        System.out.println(encode(source, fromStr, key, iv));

        String signSource = "apilevel=18&bdi_cid=9177265119920&bdi_imei=jBsNnCkjxDsnCaRbetUPtw%253D%253D&bdi_imsi=NWEzYjI4N2YyYjEzYmVmOA%3D%3D&bdi_loc=5YyX5Lqs5biC&bdi_mac=YWM6YmM6MzI6OWE6YmY6MzM%3D&bdi_uip=127.0.0.1&brand=OPSSON&class=g&cname=WS%3AYY&cpack=monkey&ct=1452249585&cver=2.0&dpi=300&from=1014104s&model=Q1&os_version=4.3&pn=0&pver=3&resolution=720_1280&rn=10&token=jingyan&type=app&uid=712ACBBE63076AEC76BE860AQDEWE880&word=%E6%AC%A2%E4%B9%90%E6%96%97%E5%9C%B0%E4%B8%BB&bdi_bear=WF";
        System.out.println(toMd5(signSource));
    }

    /**
     * 加密，先aes，再base64再urlencode
     *
     * @param rawStr
     * @param key
     * @param iv
     * @return
     */
    public static String encode(String rawStr, String from, String key, String iv)
    {
        if (null == rawStr || null == key || rawStr.isEmpty() || key.isEmpty())
        {
            System.err.println("加密的必要参数为空，加密失败！");
            return null;
        }
        byte[] aesBytes = AESEncode(rawStr, from, key, iv);
        if (null == aesBytes)
        {
            System.err.println("aes加密失败");
            return null;
        }
        String base64Encode = java.util.Base64.getEncoder().encodeToString(aesBytes);
        String urlEncodeStr = URLEncoder.encode(base64Encode);
        return urlEncodeStr;
    }

    /**
     * 对字符串进行aes加密
     *
     * @param rawStr
     * @param key
     * @param iv
     * @return
     */
    private static byte[] AESEncode(String rawStr, String from, String key, String iv)
    {
        try
        {
            IvParameterSpec ivParam = null;
            if (null != iv && !iv.isEmpty())
            {
                byte[] ivbytes = generateIV(iv);
                StringBuilder sb = new StringBuilder();
                sb.append("iv=");
                for (byte val : ivbytes)
                {
                    sb.append(val).append(",");
                }
                System.out.println(sb.toString());
                ivParam = new IvParameterSpec(generateIV(iv));
            }
            String strMd5 = toMd5(from + key);
            if (null == strMd5)
            {
                System.err.println("生成key失败！");
                return null;
            }
            byte[] strBytes = strMd5.getBytes("utf-8");
            StringBuilder sb = new StringBuilder();
            sb.append("key=");
            for (byte val : strBytes)
            {
                sb.append(val).append(",");
            }
            System.out.println(sb.toString());
            SecretKeySpec cipherKey = new SecretKeySpec(strBytes, strBytes.length / 2, strBytes.length / 2, "AES");
            Cipher encodeCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            if (null != ivParam)
            {
                encodeCipher.init(Cipher.ENCRYPT_MODE, cipherKey, ivParam);
            }
            else
            {
                encodeCipher.init(Cipher.ENCRYPT_MODE, cipherKey);
            }
            return encodeCipher.doFinal(rawStr.getBytes("utf-8"));
        }
        catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }
        catch (NoSuchPaddingException e)
        {
            e.printStackTrace();
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }
        catch (InvalidKeyException e)
        {
            e.printStackTrace();
        }
        catch (InvalidAlgorithmParameterException e)
        {
            e.printStackTrace();
        }
        catch (IllegalBlockSizeException e)
        {
            e.printStackTrace();
        }
        catch (BadPaddingException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] generateIV(String iv)
    {
        if (null == iv || iv.isEmpty())
        {
            return null;
        }
        try
        {
            byte[] ivBytes = iv.getBytes("utf-8");
            return Arrays.copyOf(ivBytes, 16);
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 获取字符串的md5
     *
     * @param str
     * @return
     */
    public static String toMd5(String str)
    {
        try
        {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] bytes = md5.digest(str.getBytes("utf-8"));
            if (null != bytes)
            {
                return parseByte2HexStr(bytes);
            }
        }
        catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 将二进制转换成16进制
     *
     * @param buf
     * @return
     */
    public static String parseByte2HexStr(byte buf[])
    {
        if (null == buf)
        {
            return null;
        }
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < buf.length; i++)
        {
            String hex = Integer.toHexString(buf[i] & 0xFF);
            if (hex.length() == 1)
            {
                hex = '0' + hex;
            }
            sb.append(hex.toUpperCase());
        }
        return sb.toString();
    }

    @Test
    public void aesEncryptBytes() throws NoSuchPaddingException, InvalidAlgorithmParameterException,
            NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, UnsupportedEncodingException
    {
        byte[] expectResult = new Base64().decode("VlQ+I3sMWMMSAKwX3Esf/SmIVc0PRdIGN7p9nMUGIYPs5ykVAad9NsHRDWzjl7ZI");
        byte[] actualResult = null;

        actualResult = CryptoHelper.aesEncryptBytes("testa7b94c7edfec5312b5960a8f21d0c83c".getBytes("utf-8"),
                "0123456789012345".getBytes("utf-8"), "0123456789012345".getBytes("utf-8"));
        Assert.assertArrayEquals(expectResult, actualResult);


        actualResult = CryptoHelper.aesEncryptBytes("testa7b94c7edfec5312b5960a8f21d0c83c".getBytes("utf-8"),
                "0123456789012345".getBytes("utf-8"), "0123456789012345".getBytes("utf-8"), "AES/CBC/PKCS5Padding");
        Assert.assertArrayEquals(expectResult, actualResult);

        actualResult = CryptoHelper.aesEncryptBytes("testa7b94c7edfec5312b5960a8f21d0c83c".getBytes("gb2312"),
                "0123456789012345".getBytes("gb2312"), "0123456789012345".getBytes("gb2312"), "AES/CBC/PKCS5Padding");
        Assert.assertArrayEquals(expectResult, actualResult);

        actualResult = CryptoHelper.aesEncryptBytes("这个是一段中文".getBytes("utf-8"),
                "0123456789012345".getBytes("utf-8"), "0123456789012345".getBytes("utf-8"), "AES/CBC/PKCS5Padding");
        Assert.assertArrayEquals(new Base64().decode("ebE0JqsHF07NASF/w+VeLsNrXvQ1trPqfUNeoHekj6Y="), actualResult);
    }

    @Test
    public void aesDecrypt() throws NoSuchPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException,
            IllegalBlockSizeException, BadPaddingException, InvalidKeyException, IOException
    {
        String expectResult = "testa7b94c7edfec5312b5960a8f21d0c83c";
        String actualResult = "";
        actualResult = CryptoHelper.aesDecrypt("VlQ+I3sMWMMSAKwX3Esf/SmIVc0PRdIGN7p9nMUGIYPs5ykVAad9NsHRDWzjl7ZI", "0123456789012345");
        Assert.assertEquals(expectResult, actualResult);

        actualResult = CryptoHelper.aesDecrypt("VlQ+I3sMWMMSAKwX3Esf/SmIVc0PRdIGN7p9nMUGIYPs5ykVAad9NsHRDWzjl7ZI", "0123456789012345", "0123456789012345");
        Assert.assertEquals(expectResult, actualResult);

        actualResult = CryptoHelper.aesDecrypt("VlQ+I3sMWMMSAKwX3Esf/SmIVc0PRdIGN7p9nMUGIYPs5ykVAad9NsHRDWzjl7ZI", "0123456789012345",
                "0123456789012345", "utf-8", "AES/CBC/PKCS5Padding");
        Assert.assertEquals(expectResult, actualResult);

        actualResult = CryptoHelper.aesDecrypt("VlQ+I3sMWMMSAKwX3Esf/SmIVc0PRdIGN7p9nMUGIYPs5ykVAad9NsHRDWzjl7ZI", "0123456789012345",
                "0123456789012345", "gb2312", "AES/CBC/PKCS5Padding");
        Assert.assertEquals(expectResult, actualResult);

        actualResult = CryptoHelper.aesDecrypt("ebE0JqsHF07NASF/w+VeLsNrXvQ1trPqfUNeoHekj6Y=", "0123456789012345",
                "0123456789012345", "utf-8", "AES/CBC/PKCS5Padding");
        Assert.assertEquals("这个是一段中文", actualResult);

        actualResult = CryptoHelper.aesDecrypt("ebE0JqsHF07NASF/w+VeLsNrXvQ1trPqfUNeoHekj6Y=", "0123456789012345",
                "0123456789012345", "gb2312", "AES/CBC/PKCS5Padding");
        Assert.assertNotEquals("这个是一段中文", actualResult);
    }

    @Test
    public void aesDecryptBytes() throws UnsupportedEncodingException, NoSuchPaddingException, InvalidAlgorithmParameterException,
            NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException
    {
        byte[] expectResult = "testa7b94c7edfec5312b5960a8f21d0c83c".getBytes("utf-8");
        byte[] actualResult = null;

        actualResult = CryptoHelper.aesDecryptBytes(new Base64().decode("VlQ+I3sMWMMSAKwX3Esf/SmIVc0PRdIGN7p9nMUGIYPs5ykVAad9NsHRDWzjl7ZI")
                , "0123456789012345".getBytes("utf-8"), "0123456789012345".getBytes("utf-8"));
        Assert.assertArrayEquals(expectResult, actualResult);

        actualResult = CryptoHelper.aesDecryptBytes(new Base64().decode("VlQ+I3sMWMMSAKwX3Esf/SmIVc0PRdIGN7p9nMUGIYPs5ykVAad9NsHRDWzjl7ZI")
                , "0123456789012345".getBytes("utf-8"), "0123456789012345".getBytes("utf-8"), "AES/CBC/PKCS5Padding");
        Assert.assertArrayEquals(expectResult, actualResult);


    }

    @Test
    public void desDecryptChannel() throws NoSuchPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, IOException, InvalidKeySpecException
    {
        String desKey = "5a@4a$0e";
        String desIv = "e2Eb9A82";

        String chl = "QuC4H625/+c31YOTw4vk7A==";
        //"i8la7oOn00VgXzUETRrnfsA3ZTG5pDFc";

        byte[] key = desKey.getBytes("utf-8");
        byte[] iv = desIv.getBytes("utf-8");
        byte[] encryptSourceChl = "fb4b267a90bf4a18".getBytes("utf-8");
        byte[] encryptResult = CryptoHelper.desEncrypt(encryptSourceChl, key, iv);
        System.out.println(new Base64().encodeAsString(encryptResult));

        byte[] byteChl = new Base64().decode(chl);
        String desResult = new String(CryptoHelper.desDecrypt(byteChl, key, iv));
        System.out.println(desResult);
    }

    public static void main(String[] args)
    {
//		desEncrypt("test1234567890123456789", "12345678");
        desEncrypt("这是一个des的加密解密", "12345678");

        System.out.println("test1234567890123456789".replace('1', 'a'));
//
        tripleDesEncrypt("http://appres.moborobo.com/android/soft/2015/4/30/b863dac7ad66493d8cfba198c0f2aac4/9f51787bf639485980baf03d659e052e.apk", "2702fcca25875b3a2d01d54ff02f4826");
//		tripleDesEncrypt("这是一个3des的加密解密", "abcdefgh12345678abcdefgh");

        //aesEncrypt("testa7b94c7edfec5312b5960a8f21d0c83c", "0123456789012345");
        //aesEncrypt("testa7b94c7edfec5312b5960a8f21d0c83c", "0123456789123456");
        //aesEncrypt("testa7b94c7edfec5312b5960a8f21d0c83c", "0123456789234567");

//		aesEncryptAndDecryptIv("testa7b94c7edfec5312b5960a8f21d0c83c", "0123456789012345", "0123456789012345");
//		aesEncryptAndDecryptIv("testa7b94c7edfec5312b5960a8f21d0c83c", "0123456789012345", "0123456789012345");
//		aesEncryptAndDecryptIv("testa7b94c7edfec5312b5960a8f21d0c83c", "0123456789012345", "0123456789012345");

        //aesDecryptIv("VlQ+I3sMWMMSAKwX3Esf/SmIVc0PRdIGN7p9nMUGIYPs5ykVAad9NsHRDWzjl7ZI", "0123456789012345", "0123456789012345");


//		aesEncryptByte("testa7b94c7edfec5312b5960a8f21d0c83c", "0123456789012345");
//		aesEncryptByte("testa7b94c7edfec5312b5960a8f21d0c83c", "0123456789012345");
//		aesEncryptByte("testa7b94c7edfec5312b5960a8f21d0c83c", "0123456789012345");
//
//		aesEncryptByteIv("testa7b94c7edfec5312b5960a8f21d0c83c", "0123456789012345", "0123456789012345");
//		aesEncryptByteIv("testa7b94c7edfec5312b5960a8f21d0c83c", "0123456789012345", "0123456789012345");
//		aesEncryptByteIv("testa7b94c7edfec5312b5960a8f21d0c83c", "0123456789012345", "0123456789012345");


//        toHMAC_Sha1_Base64("200x100/felinkagres/0165655.jpg", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
//
//        toHMAC_Sha1_Base64("我的富大龙发疯的疯的发", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
//
//        toHMAC_Sha1_Base64("GetActivityPageProtobufReturn?platform=iPhone&fw=iPhone_8_0_0&actype=0&showPlace=2&pageIndex=1&pageSize=2147483647&count=0&clientVersion=5.1.1&chl=91assistant_Iosphone26&check=99B69DA1D8311AF1A81F09244BB8B8C3&clientIp=&projectId=1800&cpu=&imsi=&cuid=3bd57f44dab033cb14b89a43e4a5d78dd71faf70&clienttype=ipa&requestId=0&imei=3fd7cdf5efacfeba47d693674d2ee24a35b8d2b4", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
//        toHMAC_Sha1_Base64("GetItuneTemplateAdvertProtobufReturn?platform=iPhone&fw=iPhone_7_1_0&placeIds=2001&clientVersion=1.5&IsHao123=False&chl=iMGAs5ltDHqfB7qvZMF0hw==&check=34AD61DDC3409A55F372075691955BC6&clientIp=172.17.148.52&projectId=6800&NoCmsTag=False&getInstall=0&cpu=&imsi=&isappstore=False&cuid=a58901895013e70161729d2c037343d3d9bb9705&osv=7.1.2&requestId=5550454471579018990&imei=383DD6B0-BAFB-4F05-8A61-189B6808BBC4", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
//        toHMAC_Sha1_Base64("GetTagItuneAdvertBothProtobufReturn?platform=iPhone&fw=iPhone_7_1_0&edittagid=2556&adtagid=0&check=A3BABDEE191E9366F13DFA61C8F573F2&clientIp=172.17.148.52&projectid=6800&getInstall=0&cpu=&imsi=&isappstore=False&testymd=&cuid=a58901895013e70161729d2c037343d3d9bb9705&osv=7.1.2&requestId=4851933050194495060&clientVersion=1.5&imei=383DD6B0-BAFB-4F05-8A61-189B6808BBC4", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
//        toHMAC_Sha1_Base64("GetTagItuneAdvertBothProtobufReturn?platform=iPhone&fw=iPhone_7_1_0&edittagid=846&adtagid=1075&check=EAF0FB4A0DC3EE5120840022C4907EB8&clientIp=172.17.148.52&projectid=6800&getInstall=0&cpu=&imsi=&isappstore=False&testymd=&cuid=a58901895013e70161729d2c037343d3d9bb9705&osv=7.1.2&requestId=5304861331077407151&clientVersion=1.5&imei=383DD6B0-BAFB-4F05-8A61-189B6808BBC4", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
//        toHMAC_Sha1_Base64("GetTagItuneAdvertBothProtobufReturn?platform=iPhone&fw=iPhone_7_1_0&edittagid=846&adtagid=1075&check=EAF0FB4A0DC3EE5120840022C4907EB8&clientIp=172.17.148.52&projectid=6800&getInstall=0&cpu=&imsi=&isappstore=False&testymd=&cuid=a58901895013e70161729d2c037343d3d9bb9705&osv=7.1.2&requestId=5601893709361552751&clientVersion=1.5&imei=383DD6B0-BAFB-4F05-8A61-189B6808BBC4", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
//        toHMAC_Sha1_Base64("GetTagItuneAdvertProtobufReturn?platform=iPhone&fw=iPhone_7_1_0&edittagid=2508&check=9DFBD4C3069C946A6CC59A3BA608F73C&clientIp=172.17.148.52&projectid=6800&oldMode=False&count=12&getInstall=0&cpu=&imsi=&isappstore=False&cuid=a58901895013e70161729d2c037343d3d9bb9705&osv=7.1.2&requestId=4640247849058475301&clientVersion=1.5&imei=383DD6B0-BAFB-4F05-8A61-189B6808BBC4", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
//        toHMAC_Sha1_Base64("GetTagItuneAdvertBothProtobufReturn?platform=iPhone&fw=iPhone_8_0_0&edittagid=0&adtagid=2276&check=1D09672174F2A6632AD9E230547771B7&clientIp=&projectid=6800&getInstall=0&cpu=0&imsi=&isappstore=False&testymd=&cuid=&osv=&requestId=0&imei=5fb26dc8-33f0-4423-a356-5ef3e2faeb4d", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
//        toHMAC_Sha1_Base64("GetTagItuneAdvertBothProtobufReturn?platform=iPhone&fw=iPhone_8_0_0&edittagid=0&adtagid=2276&check=1D09672174F2A6632AD9E230547771B7&clientIp=&projectid=6800&getInstall=0&cpu=0&imsi=&isappstore=False&testymd=&cuid=&osv=&requestId=0&imei=5fb26dc8-33f0-4423-a356-5ef3e2faeb4d", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
//        toHMAC_Sha1_Base64("GetItuneTemplateAdvertProtobufReturn?platform=iPhone&fw=iPhone_9_0_2&placeIds=2001&clientVersion=1.3&IsHao123=False&chl=1011080t&check=02155563A3767F9AD6642E83C8DBD928&clientIp=172.17.150.144&projectId=6800&NoCmsTag=False&getInstall=0&cpu=&imsi=&isappstore=False&cuid=5b4e4d21bb3bd81ad56ae7b34d837d0ba2436ed3&osv=9.0.2&requestId=5659265269211061781&imei=312320C9-3417-4258-9CF4-DF54AB902344", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
//        toHMAC_Sha1_Base64("GetTagItuneAdvertProtobufReturn?platform=iPhone&fw=iPhone_9_0_2&edittagid=2508&check=82F5F2809BB5F273B92F5EDDBF6D0B1F&clientIp=172.17.150.144&projectid=6800&oldMode=False&count=12&getInstall=0&cpu=&imsi=&isappstore=False&cuid=5b4e4d21bb3bd81ad56ae7b34d837d0ba2436ed3&osv=9.0.2&requestId=5632406921025922938&clientVersion=1.3&imei=312320C9-3417-4258-9CF4-DF54AB902344", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
//        toHMAC_Sha1_Base64("GetTagItuneAdvertBothProtobufReturn?platform=iPhone&fw=iPhone_9_0_2&edittagid=846&adtagid=1075&check=2DD04087ADA60B51E4C15B5F998FB342&clientIp=172.17.150.144&projectid=6800&getInstall=0&cpu=&imsi=&isappstore=False&testymd=&cuid=5b4e4d21bb3bd81ad56ae7b34d837d0ba2436ed3&osv=9.0.2&requestId=4864526425288611898&clientVersion=1.3&imei=312320C9-3417-4258-9CF4-DF54AB902344", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
//        toHMAC_Sha1_Base64("GetTagItuneAdvertBothProtobufReturn?platform=iPhone&fw=iPhone_9_0_2&edittagid=2556&adtagid=0&check=2BF5442C2FC44D84E48C82FDC729E2BF&clientIp=172.17.150.144&projectid=6800&getInstall=0&cpu=&imsi=&isappstore=False&testymd=&cuid=5b4e4d21bb3bd81ad56ae7b34d837d0ba2436ed3&osv=9.0.2&requestId=5153183876780697672&clientVersion=1.3&imei=312320C9-3417-4258-9CF4-DF54AB902344", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
//        toHMAC_Sha1_Base64("GetTagItuneAdvertBothProtobufReturn?platform=iPhone&fw=iPhone_9_0_2&edittagid=846&adtagid=1075&check=2DD04087ADA60B51E4C15B5F998FB342&clientIp=172.17.150.144&projectid=6800&getInstall=0&cpu=&imsi=&isappstore=False&testymd=&cuid=5b4e4d21bb3bd81ad56ae7b34d837d0ba2436ed3&osv=9.0.2&requestId=4925480476746781965&clientVersion=1.3&imei=312320C9-3417-4258-9CF4-DF54AB902344", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
//        toHMAC_Sha1_Base64("GetTagItuneAdvertBothProtobufReturn?platform=iPhone&fw=iPhone_9_0_0&edittagid=0&adtagid=2276&check=EEEBE5BEDC91E7B34464B9DBD3F72D4C&clientIp=&projectid=6800&getInstall=0&cpu=0&imsi=&isappstore=False&testymd=&cuid=&osv=&requestId=0&imei=312320c9-3417-4258-9cf4-df54ab902344", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
//        toHMAC_Sha1_Base64("GetTagItuneAdvertBothProtobufReturn?platform=iPhone&fw=iPhone_9_0_0&edittagid=0&adtagid=2276&check=EEEBE5BEDC91E7B34464B9DBD3F72D4C&clientIp=&projectid=6800&getInstall=0&cpu=0&imsi=&isappstore=False&testymd=&cuid=&osv=&requestId=0&imei=312320c9-3417-4258-9cf4-df54ab902344", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
//        toHMAC_Sha1_Base64("GetItuneTemplateAdvertProtobufReturn?platform=iPhone&fw=iPhone_9_0_2&placeIds=2001&clientVersion=2.5.7&IsHao123=False&chl=jZg3GleBqu8bs14RkWZYOsGGj4HzUz6l&check=02155563A3767F9AD6642E83C8DBD928&clientIp=172.17.150.144&projectId=6800&NoCmsTag=False&getInstall=0&cpu=&imsi=&isappstore=False&cuid=3045560f4e46a65c57ea764c727ad912cb2b1f3c&osv=9.0.2&requestId=5425071485841027205&imei=312320C9-3417-4258-9CF4-DF54AB902344", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
//        toHMAC_Sha1_Base64("GetTagItuneAdvertProtobufReturn?platform=iPhone&fw=iPhone_9_0_2&edittagid=2508&check=82F5F2809BB5F273B92F5EDDBF6D0B1F&clientIp=172.17.150.144&projectid=6800&oldMode=False&count=12&getInstall=0&cpu=&imsi=&isappstore=False&cuid=3045560f4e46a65c57ea764c727ad912cb2b1f3c&osv=9.0.2&requestId=5301468227364741676&clientVersion=2.5.7&imei=312320C9-3417-4258-9CF4-DF54AB902344", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
//        toHMAC_Sha1_Base64("GetTagItuneAdvertBothProtobufReturn?platform=iPhone&fw=iPhone_9_0_2&edittagid=2556&adtagid=0&check=2BF5442C2FC44D84E48C82FDC729E2BF&clientIp=172.17.150.144&projectid=6800&getInstall=0&cpu=&imsi=&isappstore=False&testymd=&cuid=3045560f4e46a65c57ea764c727ad912cb2b1f3c&osv=9.0.2&requestId=5197546127637486405&clientVersion=2.5.7&imei=312320C9-3417-4258-9CF4-DF54AB902344", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
//        toHMAC_Sha1_Base64("GetTagItuneAdvertBothProtobufReturn?platform=iPhone&fw=iPhone_9_0_2&edittagid=846&adtagid=1075&check=2DD04087ADA60B51E4C15B5F998FB342&clientIp=172.17.150.144&projectid=6800&getInstall=0&cpu=&imsi=&isappstore=False&testymd=&cuid=3045560f4e46a65c57ea764c727ad912cb2b1f3c&osv=9.0.2&requestId=4733590324106245279&clientVersion=2.5.7&imei=312320C9-3417-4258-9CF4-DF54AB902344", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
//        toHMAC_Sha1_Base64("GetTagItuneAdvertBothProtobufReturn?platform=iPhone&fw=iPhone_9_0_2&edittagid=846&adtagid=1075&check=2DD04087ADA60B51E4C15B5F998FB342&clientIp=172.17.150.144&projectid=6800&getInstall=0&cpu=&imsi=&isappstore=False&testymd=&cuid=3045560f4e46a65c57ea764c727ad912cb2b1f3c&osv=9.0.2&requestId=5039758927907574415&clientVersion=2.5.7&imei=312320C9-3417-4258-9CF4-DF54AB902344", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
//        toHMAC_Sha1_Base64("GetTagItuneAdvertBothProtobufReturn?platform=iPhone&fw=iPhone_9_0_0&edittagid=0&adtagid=2276&check=94CE309AC825B4B78DB88DDF59A6F258&clientIp=&projectid=6800&getInstall=0&cpu=0&imsi=&isappstore=False&testymd=&cuid=&osv=&requestId=0&imei=d9a027cf-00e2-40c5-8649-405a29861186", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
//        toHMAC_Sha1_Base64("GetItuneTemplateAdvertProtobufReturn?platform=iPhone&fw=iPhone_9_0_2&placeIds=2001&clientVersion=2.5.7&IsHao123=False&chl=jZg3GleBqu8bs14RkWZYOsGGj4HzUz6l&check=02155563A3767F9AD6642E83C8DBD928&clientIp=172.17.150.144&projectId=6800&NoCmsTag=False&getInstall=0&cpu=&imsi=&isappstore=False&cuid=3045560f4e46a65c57ea764c727ad912cb2b1f3c&osv=9.0.2&requestId=5263723615826530471&imei=312320C9-3417-4258-9CF4-DF54AB902344", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
//        toHMAC_Sha1_Base64("GetTagItuneAdvertProtobufReturn?platform=iPhone&fw=iPhone_9_0_2&edittagid=2508&check=82F5F2809BB5F273B92F5EDDBF6D0B1F&clientIp=172.17.150.144&projectid=6800&oldMode=False&count=12&getInstall=0&cpu=&imsi=&isappstore=False&cuid=3045560f4e46a65c57ea764c727ad912cb2b1f3c&osv=9.0.2&requestId=4971024057345989604&clientVersion=2.5.7&imei=312320C9-3417-4258-9CF4-DF54AB902344", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
//        toHMAC_Sha1_Base64("GetTagItuneAdvertBothProtobufReturn?platform=iPhone&fw=iPhone_9_0_2&edittagid=2556&adtagid=0&check=2BF5442C2FC44D84E48C82FDC729E2BF&clientIp=172.17.150.144&projectid=6800&getInstall=0&cpu=&imsi=&isappstore=False&testymd=&cuid=3045560f4e46a65c57ea764c727ad912cb2b1f3c&osv=9.0.2&requestId=5325923185879651601&clientVersion=2.5.7&imei=312320C9-3417-4258-9CF4-DF54AB902344", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
//        toHMAC_Sha1_Base64("GetTagItuneAdvertBothProtobufReturn?platform=iPhone&fw=iPhone_9_0_2&edittagid=846&adtagid=1075&check=2DD04087ADA60B51E4C15B5F998FB342&clientIp=172.17.150.144&projectid=6800&getInstall=0&cpu=&imsi=&isappstore=False&testymd=&cuid=3045560f4e46a65c57ea764c727ad912cb2b1f3c&osv=9.0.2&requestId=5059268850408289647&clientVersion=2.5.7&imei=312320C9-3417-4258-9CF4-DF54AB902344", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
//        toHMAC_Sha1_Base64("GetTagItuneAdvertBothProtobufReturn?platform=iPhone&fw=iPhone_9_0_2&edittagid=846&adtagid=1075&check=2DD04087ADA60B51E4C15B5F998FB342&clientIp=172.17.150.144&projectid=6800&getInstall=0&cpu=&imsi=&isappstore=False&testymd=&cuid=3045560f4e46a65c57ea764c727ad912cb2b1f3c&osv=9.0.2&requestId=5697352716524259643&clientVersion=2.5.7&imei=312320C9-3417-4258-9CF4-DF54AB902344", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
//        toHMAC_Sha1_Base64("GetTagItuneAdvertBothProtobufReturn?platform=iPhone&fw=iPhone_9_0_0&edittagid=0&adtagid=2276&check=EEEBE5BEDC91E7B34464B9DBD3F72D4C&clientIp=&projectid=6800&getInstall=0&cpu=0&imsi=&isappstore=False&testymd=&cuid=&osv=&requestId=0&imei=312320c9-3417-4258-9cf4-df54ab902344", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
//        toHMAC_Sha1_Base64("GetTagItuneAdvertBothProtobufReturn?platform=iPhone&fw=iPhone_9_0_0&edittagid=0&adtagid=2276&check=EEEBE5BEDC91E7B34464B9DBD3F72D4C&clientIp=&projectid=6800&getInstall=0&cpu=0&imsi=&isappstore=False&testymd=&cuid=&osv=&requestId=0&imei=312320c9-3417-4258-9cf4-df54ab902344", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
//        toHMAC_Sha1_Base64("GetTagItuneAdvertBothProtobufReturn?platform=iPhone&fw=iPhone_9_0_0&edittagid=0&adtagid=2276&check=94CE309AC825B4B78DB88DDF59A6F258&clientIp=&projectid=6800&getInstall=0&cpu=0&imsi=&isappstore=False&testymd=&cuid=&osv=&requestId=0&imei=d9a027cf-00e2-40c5-8649-405a29861186", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
//        toHMAC_Sha1_Base64("GetTagItuneAdvertBothProtobufReturn?platform=iPhone&fw=iPhone_9_0_0&edittagid=0&adtagid=2276&check=EEEBE5BEDC91E7B34464B9DBD3F72D4C&clientIp=&projectid=6800&getInstall=0&cpu=0&imsi=&isappstore=False&testymd=&cuid=&osv=&requestId=0&imei=312320c9-3417-4258-9cf4-df54ab902344", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
//        toHMAC_Sha1_Base64("GetActivityPageProtobufReturn?platform=iPhone&fw=iPhone_8_0_0&actype=0&showPlace=2&pageIndex=1&pageSize=2147483647&count=0&clientVersion=5.1.1&chl=91assistant_Iosphone26&check=99B69DA1D8311AF1A81F09244BB8B8C3&clientIp=&projectId=1800&cpu=&imsi=&cuid=3bd57f44dab033cb14b89a43e4a5d78dd71faf70&clienttype=pxl&requestId=0&imei=3fd7cdf5efacfeba47d693674d2ee24a35b8d2b4", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
//        toHMAC_Sha1_Base64("GetActivityPageProtobufReturn?platform=iPhone&fw=iPhone_8_0_0&actype=0&showPlace=2&pageIndex=1&pageSize=2147483647&count=0&clientVersion=5.1.1&chl=91assistant_Iosphone26&check=99B69DA1D8311AF1A81F09244BB8B8C3&clientIp=&projectId=1800&cpu=&imsi=&cuid=3bd57f44dab033cb14b89a43e4a5d78dd71faf70&clienttype=ipa&requestId=0&imei=3fd7cdf5efacfeba47d693674d2ee24a35b8d2b4", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
//        toHMAC_Sha1_Base64("GetTagItuneAdvertBothProtobufReturn?platform=iPhone&fw=iPhone_9_0_0&edittagid=0&adtagid=2276&check=FA6697DF5FEACCD4D7A3AEE23F793A0F&clientIp=&projectid=6800&getInstall=0&cpu=0&imsi=&isappstore=False&testymd=&cuid=&osv=&requestId=0&imei=3bd2331c-95d8-43a6-970f-177d82c81e09", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
//        toHMAC_Sha1_Base64("GetTagItuneAdvertBothProtobufReturn?platform=iPhone&fw=iPhone_9_0_0&edittagid=0&adtagid=2276&check=FA6697DF5FEACCD4D7A3AEE23F793A0F&clientIp=&projectid=6800&getInstall=0&cpu=0&imsi=&isappstore=False&testymd=&cuid=&osv=&requestId=0&imei=3bd2331c-95d8-43a6-970f-177d82c81e09", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
//        toHMAC_Sha1_Base64("GetTagItuneAdvertBothProtobufReturn?platform=iPhone&fw=iPhone_9_0_0&edittagid=0&adtagid=2276&check=EEEBE5BEDC91E7B34464B9DBD3F72D4C&clientIp=&projectid=6800&getInstall=0&cpu=0&imsi=&isappstore=False&testymd=&cuid=&osv=&requestId=0&imei=312320c9-3417-4258-9cf4-df54ab902344", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
//        toHMAC_Sha1_Base64("GetTagItuneAdvertBothProtobufReturn?platform=iPhone&fw=iPhone_9_0_0&edittagid=0&adtagid=2276&check=FA6697DF5FEACCD4D7A3AEE23F793A0F&clientIp=&projectid=6800&getInstall=0&cpu=0&imsi=&isappstore=False&testymd=&cuid=&osv=&requestId=0&imei=3bd2331c-95d8-43a6-970f-177d82c81e09", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
//        toHMAC_Sha1_Base64("GetTagItuneAdvertBothProtobufReturn?platform=iPhone&fw=iPhone_9_0_0&edittagid=0&adtagid=2276&check=FA6697DF5FEACCD4D7A3AEE23F793A0F&clientIp=&projectid=6800&getInstall=0&cpu=0&imsi=&isappstore=False&testymd=&cuid=&osv=&requestId=0&imei=3bd2331c-95d8-43a6-970f-177d82c81e09", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
//        toHMAC_Sha1_Base64("GetActivityPageProtobufReturn?platform=iPhone&fw=iPhone_7_0_0&actype=0&showPlace=2&pageIndex=1&pageSize=2147483647&count=0&clientVersion=5.2.0&chl=91assistant_Iosphone27&check=E04A5E782F8E7DAC261D77FEED1232A5&clientIp=&projectId=1800&cpu=&imsi=&cuid=96219def20140511b940d862b33980b6edd32d37&clienttype=pxl&requestId=0&imei=69b4402022f4633f11fceeb5b3df42d83cbf1f47", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
//        toHMAC_Sha1_Base64("GetActivityPageProtobufReturn?platform=iPhone&fw=iPhone_7_0_0&actype=0&showPlace=2&pageIndex=1&pageSize=2147483647&count=0&clientVersion=5.2.0&chl=91assistant_Iosphone27&check=E04A5E782F8E7DAC261D77FEED1232A5&clientIp=&projectId=1800&cpu=&imsi=&cuid=96219def20140511b940d862b33980b6edd32d37&clienttype=ipa&requestId=0&imei=69b4402022f4633f11fceeb5b3df42d83cbf1f47", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
//        toHMAC_Sha1_Base64("GetItuneTemplateAdvertProtobufReturn?platform=iPhone&fw=iPhone_5_1_0&placeIds=3001&clientVersion=2.5.2.1&IsHao123=False&chl=jZg3GleBqu8bs14RkWZYOsGGj4HzUz6l&check=2EF835A5322A55F8DA412C69A5ACF5A8&clientIp=172.17.148.52&projectId=6800&NoCmsTag=False&getInstall=0&cpu=&imsi=&isappstore=False&cuid=fd467bf7587dd29995400c1bd727d99e37a98954&osv=5.1.1&requestId=5154080669299960560&imei=f6a6cb52a8f2e1ac7e3ffa38c89b088357b89efe", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
//        toHMAC_Sha1_Base64("GetTagItuneAdvertBothProtobufReturn?platform=iPhone&fw=iPhone_5_1_0&edittagid=846&adtagid=1075&check=6E57A3053D2863A040925699B9F2A49C&clientIp=172.17.148.52&projectid=6800&getInstall=0&cpu=&imsi=&isappstore=False&testymd=&cuid=fd467bf7587dd29995400c1bd727d99e37a98954&osv=5.1.1&requestId=5071724629471707904&clientVersion=2.5.2.1&imei=f6a6cb52a8f2e1ac7e3ffa38c89b088357b89efe", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
//        toHMAC_Sha1_Base64("GetTagItuneAdvertProtobufReturn?platform=iPhone&fw=iPhone_5_1_0&edittagid=2508&check=22BE4AA10E1A08667C1CA7DC194BB95D&clientIp=172.17.148.52&projectid=6800&oldMode=False&count=12&getInstall=0&cpu=&imsi=&isappstore=False&cuid=fd467bf7587dd29995400c1bd727d99e37a98954&osv=5.1.1&requestId=5101670767530506715&clientVersion=2.5.2.1&imei=f6a6cb52a8f2e1ac7e3ffa38c89b088357b89efe", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
//        toHMAC_Sha1_Base64("GetTagItuneAdvertBothProtobufReturn?platform=iPhone&fw=iPhone_5_1_0&edittagid=2556&adtagid=0&check=C7C11F871D75DACD3CFBCB000B442755&clientIp=172.17.148.52&projectid=6800&getInstall=0&cpu=&imsi=&isappstore=False&testymd=&cuid=fd467bf7587dd29995400c1bd727d99e37a98954&osv=5.1.1&requestId=5386708669352056758&clientVersion=2.5.2.1&imei=f6a6cb52a8f2e1ac7e3ffa38c89b088357b89efe", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
//        toHMAC_Sha1_Base64("GetTagItuneAdvertBothProtobufReturn?platform=iPhone&fw=iPhone_5_1_0&edittagid=846&adtagid=1075&check=6E57A3053D2863A040925699B9F2A49C&clientIp=172.17.148.52&projectid=6800&getInstall=0&cpu=&imsi=&isappstore=False&testymd=&cuid=fd467bf7587dd29995400c1bd727d99e37a98954&osv=5.1.1&requestId=5416497733626937499&clientVersion=2.5.2.1&imei=f6a6cb52a8f2e1ac7e3ffa38c89b088357b89efe", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
//        toHMAC_Sha1_Base64("ClearConfigCache?act=cleartagcache&tagid=1007794&platformid=4&seriesId=30258", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
//        toHMAC_Sha1_Base64("ClearConfigCache?act=cleartagcache&tagid=1007791&platformid=4&seriesId=30112", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
//        toHMAC_Sha1_Base64("ClearConfigCache?act=cleartagcache&tagid=1007793&platformid=4&seriesId=30257", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
//        toHMAC_Sha1_Base64("ClearConfigCache?act=cleartagcache&tagid=237453&platformid=4&seriesId=30077", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
//        toHMAC_Sha1_Base64("ClearConfigCache?act=cleartagcache&tagid=1008526&platformid=4&seriesId=30353", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
//        toHMAC_Sha1_Base64("ClearConfigCache?act=cleartagcache&tagid=138598&platformid=4&seriesId=30079", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
//        toHMAC_Sha1_Base64("ClearConfigCache?act=cleartagcache&tagid=884868&platformid=4&seriesId=30134", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
//        toHMAC_Sha1_Base64("ClearConfigCache?act=cleartagcache&tagid=1012764&platformid=4&seriesId=50038", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
//        toHMAC_Sha1_Base64("GetItuneTemplateAdvertProtobufReturn?platform=iPhone&fw=iPhone_5_1_0&placeIds=3001&clientVersion=1.3&IsHao123=False&chl=iMGAs5ltDHqfB7qvZMF0hw==&check=56571486AE166B4B135BC7CF6AD8F5D1&clientIp=172.17.148.52&projectId=6800&NoCmsTag=False&getInstall=0&cpu=&imsi=&isappstore=False&cuid=fd467bf7587dd29995400c1bd727d99e37a98954&osv=5.1.1&requestId=5247298479725846113&imei=515FA066-499B-424B-A436-3DB4CE1B7089", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
//        toHMAC_Sha1_Base64("GetTagItuneAdvertProtobufReturn?platform=iPhone&fw=iPhone_5_1_0&edittagid=2508&check=C1D756F1CDFF8073DFFE70731C844A73&clientIp=172.17.148.52&projectid=6800&oldMode=False&count=12&getInstall=0&cpu=&imsi=&isappstore=False&cuid=fd467bf7587dd29995400c1bd727d99e37a98954&osv=5.1.1&requestId=4697291617413599593&clientVersion=1.3&imei=515FA066-499B-424B-A436-3DB4CE1B7089", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
//        toHMAC_Sha1_Base64("GetTagItuneAdvertBothProtobufReturn?platform=iPhone&fw=iPhone_5_1_0&edittagid=2556&adtagid=0&check=EF22D6CB116D832A77A11423E9149B81&clientIp=172.17.148.52&projectid=6800&getInstall=0&cpu=&imsi=&isappstore=False&testymd=&cuid=fd467bf7587dd29995400c1bd727d99e37a98954&osv=5.1.1&requestId=5118529212087620206&clientVersion=1.3&imei=515FA066-499B-424B-A436-3DB4CE1B7089", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
//        toHMAC_Sha1_Base64("GetTagItuneAdvertBothProtobufReturn?platform=iPhone&fw=iPhone_5_1_0&edittagid=846&adtagid=1075&check=199B1185BB3B6A08EE963E4B2816F35E&clientIp=172.17.148.52&projectid=6800&getInstall=0&cpu=&imsi=&isappstore=False&testymd=&cuid=fd467bf7587dd29995400c1bd727d99e37a98954&osv=5.1.1&requestId=5302321256872746305&clientVersion=1.3&imei=515FA066-499B-424B-A436-3DB4CE1B7089", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
//        toHMAC_Sha1_Base64("GetTagItuneAdvertBothProtobufReturn?platform=iPhone&fw=iPhone_5_1_0&edittagid=846&adtagid=1075&check=199B1185BB3B6A08EE963E4B2816F35E&clientIp=172.17.148.52&projectid=6800&getInstall=0&cpu=&imsi=&isappstore=False&testymd=&cuid=fd467bf7587dd29995400c1bd727d99e37a98954&osv=5.1.1&requestId=5473568233950735844&clientVersion=1.3&imei=515FA066-499B-424B-A436-3DB4CE1B7089", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
//        toHMAC_Sha1_Base64("GetTagItuneAdvertBothProtobufReturn?platform=iPhone&fw=iPhone_9_0_0&edittagid=0&adtagid=2276&check=94CE309AC825B4B78DB88DDF59A6F258&clientIp=&projectid=6800&getInstall=0&cpu=0&imsi=&isappstore=False&testymd=&cuid=&osv=&requestId=0&imei=d9a027cf-00e2-40c5-8649-405a29861186", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
//        toHMAC_Sha1_Base64("GetTagItuneAdvertBothProtobufReturn?platform=iPhone&fw=iPhone_8_4_0&edittagid=0&adtagid=2276&check=E3612367311D81DA51E4A1161E9879B7&clientIp=&projectid=6800&getInstall=0&cpu=0&imsi=&isappstore=False&testymd=&cuid=&osv=&requestId=0&imei=5dedc7c2-7f34-47ae-bb93-80bdd4da4298", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
//        toHMAC_Sha1_Base64("GetTagItuneAdvertBothProtobufReturn?platform=iPhone&fw=iPhone_8_4_0&edittagid=0&adtagid=2276&check=E3612367311D81DA51E4A1161E9879B7&clientIp=&projectid=6800&getInstall=0&cpu=0&imsi=&isappstore=False&testymd=&cuid=&osv=&requestId=0&imei=5dedc7c2-7f34-47ae-bb93-80bdd4da4298", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
//        toHMAC_Sha1_Base64("GetTagItuneAdvertBothProtobufReturn?platform=iPhone&fw=iPhone_8_4_0&edittagid=0&adtagid=2276&check=E3612367311D81DA51E4A1161E9879B7&clientIp=&projectid=6800&getInstall=0&cpu=0&imsi=&isappstore=False&testymd=&cuid=&osv=&requestId=0&imei=5dedc7c2-7f34-47ae-bb93-80bdd4da4298", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
//        toHMAC_Sha1_Base64("GetItuneTemplateAdvertProtobufReturn?platform=iPhone&fw=iPhone_9_0_2&placeIds=2005&clientVersion=2.5.7&IsHao123=False&chl=jZg3GleBqu8bs14RkWZYOsGGj4HzUz6l&check=3589E62ED2DAC0BC1F325EB156E4F0D7&clientIp=172.17.150.144&projectId=6800&NoCmsTag=False&getInstall=0&cpu=&imsi=&isappstore=False&cuid=3045560f4e46a65c57ea764c727ad912cb2b1f3c&osv=9.0.2&requestId=4724937534489506116&imei=312320C9-3417-4258-9CF4-DF54AB902344", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
//        toHMAC_Sha1_Base64("GetItuneTemplateAdvertProtobufReturn?platform=iPhone&fw=iPhone&placeIds=2005&clientVersion=1.0.0&IsHao123=False&chl=&check=C5898CB8FB20E3E9F9CFE8340B8BCE33&clientIp=172.17.150.144&projectId=6800&NoCmsTag=False&getInstall=0&cpu=&imsi=&isappstore=False&cuid=&osv=&requestId=5359972002621310342&imei=", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
//        toHMAC_Sha1_Base64("GetActivityPageProtobufReturn?platform=iPhone&fw=iPhone_7_0_0&actype=0&showPlace=2&pageIndex=1&pageSize=2147483647&count=0&clientVersion=5.2.0&chl=91assistant_Iosphone27&check=E04A5E782F8E7DAC261D77FEED1232A5&clientIp=&projectId=1800&cpu=&imsi=&cuid=96219def20140511b940d862b33980b6edd32d37&clienttype=ipa&requestId=0&imei=69b4402022f4633f11fceeb5b3df42d83cbf1f47", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
//        toHMAC_Sha1_Base64("GetTagItuneAdvertBothProtobufReturn?platform=iPhone&fw=iPhone_MAX&edittagid=0&adtagid=2257&check=180573E44537FEA748CEB897B695ACD5&clientIp=&projectid=0&getInstall=0&cpu=&imsi=&isappstore=False&testymd=&cuid=&osv=&requestId=0&imei=", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
//        toHMAC_Sha1_Base64("GetTagItuneAdvertBothProtobufReturn?platform=iPhone&fw=iPhone_8_4_0&edittagid=0&adtagid=2276&check=E3612367311D81DA51E4A1161E9879B7&clientIp=&projectid=6800&getInstall=0&cpu=0&imsi=&isappstore=False&testymd=&cuid=&osv=&requestId=0&imei=5dedc7c2-7f34-47ae-bb93-80bdd4da4298", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
//        toHMAC_Sha1_Base64("GetTagItuneAdvertBothProtobufReturn?platform=iPhone&fw=iPhone_9_0_0&edittagid=0&adtagid=2276&check=EEEBE5BEDC91E7B34464B9DBD3F72D4C&clientIp=&projectid=6800&getInstall=0&cpu=0&imsi=&isappstore=False&testymd=&cuid=&osv=&requestId=0&imei=312320c9-3417-4258-9cf4-df54ab902344", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
//        toHMAC_Sha1_Base64("GetTagItuneAdvertBothProtobufReturn?platform=iPhone&fw=iPhone_8_4_0&edittagid=0&adtagid=2276&check=E3612367311D81DA51E4A1161E9879B7&clientIp=&projectid=6800&getInstall=0&cpu=0&imsi=&isappstore=False&testymd=&cuid=&osv=&requestId=0&imei=5dedc7c2-7f34-47ae-bb93-80bdd4da4298", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
//        toHMAC_Sha1_Base64("GetTagItuneAdvertBothProtobufReturn?platform=iPhone&fw=iPhone_8_4_0&edittagid=0&adtagid=2276&check=E3612367311D81DA51E4A1161E9879B7&clientIp=&projectid=6800&getInstall=0&cpu=0&imsi=&isappstore=False&testymd=&cuid=&osv=&requestId=0&imei=5dedc7c2-7f34-47ae-bb93-80bdd4da4298", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
//        toHMAC_Sha1_Base64("GetTagItuneAdvertBothProtobufReturn?platform=iPhone&fw=iPhone_8_4_0&edittagid=0&adtagid=2276&check=E3612367311D81DA51E4A1161E9879B7&clientIp=&projectid=6800&getInstall=0&cpu=0&imsi=&isappstore=False&testymd=&cuid=&osv=&requestId=0&imei=5dedc7c2-7f34-47ae-bb93-80bdd4da4298", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
//        toHMAC_Sha1_Base64("GetTagItuneAdvertBothProtobufReturn?platform=iPhone&fw=iPhone_9_0_0&edittagid=0&adtagid=2276&check=94CE309AC825B4B78DB88DDF59A6F258&clientIp=&projectid=6800&getInstall=0&cpu=0&imsi=&isappstore=False&testymd=&cuid=&osv=&requestId=0&imei=d9a027cf-00e2-40c5-8649-405a29861186", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
//        toHMAC_Sha1_Base64("GetTagItuneAdvertBothProtobufReturn?platform=iPhone&fw=iPhone_9_0_0&edittagid=0&adtagid=2276&check=94CE309AC825B4B78DB88DDF59A6F258&clientIp=&projectid=6800&getInstall=0&cpu=0&imsi=&isappstore=False&testymd=&cuid=&osv=&requestId=0&imei=d9a027cf-00e2-40c5-8649-405a29861186", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
//        toHMAC_Sha1_Base64("GetTagItuneAdvertBothProtobufReturn?platform=iPhone&fw=iPhone_9_0_0&edittagid=0&adtagid=2276&check=EEEBE5BEDC91E7B34464B9DBD3F72D4C&clientIp=&projectid=6800&getInstall=0&cpu=0&imsi=&isappstore=False&testymd=&cuid=&osv=&requestId=0&imei=312320c9-3417-4258-9cf4-df54ab902344", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
//        toHMAC_Sha1_Base64("ClearConfigCache?act=cleartagcache&tagid=1007794&platformid=4&seriesId=30258", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
//        toHMAC_Sha1_Base64("ClearConfigCache?act=cleartagcache&tagid=1007793&platformid=4&seriesId=30257", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
//        toHMAC_Sha1_Base64("ClearConfigCache?act=cleartagcache&tagid=1013310&platformid=4&seriesId=30340", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
//        toHMAC_Sha1_Base64("ClearConfigCache?act=cleartagcache&tagid=884868&platformid=4&seriesId=30134", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
//        toHMAC_Sha1_Base64("GetTagItuneAdvertBothProtobufReturn?platform=iPhone&fw=iPhone_8_1_0&edittagid=0&adtagid=2276&check=AC991DDC6D879AF2A613274CFAE70E55&clientIp=&projectid=6800&getInstall=0&cpu=0&imsi=&isappstore=False&testymd=&cuid=&osv=&requestId=0&imei=075944ae3c853cf23ab411adb84dbb9aa393f9db", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
//        toHMAC_Sha1_Base64("ClearConfigCache?act=cleartagcache&tagid=1012764&platformid=4&seriesId=50038", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
//        toHMAC_Sha1_Base64("GetActivityPageProtobufReturn?platform=iPhone&fw=iPhone_8_0_0&actype=0&showPlace=2&pageIndex=1&pageSize=2147483647&count=0&clientVersion=5.1.1&chl=91assistant_Iosphone26&check=99B69DA1D8311AF1A81F09244BB8B8C3&clientIp=&projectId=1800&cpu=&imsi=&cuid=3bd57f44dab033cb14b89a43e4a5d78dd71faf70&clienttype=pxl&requestId=0&imei=3fd7cdf5efacfeba47d693674d2ee24a35b8d2b4", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
//        toHMAC_Sha1_Base64("GetActivityPageProtobufReturn?platform=iPhone&fw=iPhone_8_0_0&actype=0&showPlace=2&pageIndex=1&pageSize=2147483647&count=0&clientVersion=5.1.1&chl=91assistant_Iosphone26&check=99B69DA1D8311AF1A81F09244BB8B8C3&clientIp=&projectId=1800&cpu=&imsi=&cuid=3bd57f44dab033cb14b89a43e4a5d78dd71faf70&clienttype=ipa&requestId=0&imei=3fd7cdf5efacfeba47d693674d2ee24a35b8d2b4", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
//        toHMAC_Sha1_Base64("GetActivityPageProtobufReturn?platform=iPhone&fw=iPhone_7_0_0&actype=0&showPlace=2&pageIndex=1&pageSize=2147483647&count=0&clientVersion=5.2.0&chl=91assistant_Iosphone27&check=E04A5E782F8E7DAC261D77FEED1232A5&clientIp=&projectId=1800&cpu=&imsi=&cuid=96219def20140511b940d862b33980b6edd32d37&clienttype=ipa&requestId=0&imei=69b4402022f4633f11fceeb5b3df42d83cbf1f47", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
//        toHMAC_Sha1_Base64("GetActivityPageProtobufReturn?platform=iPhone&fw=iPhone_7_0_0&actype=0&showPlace=2&pageIndex=1&pageSize=2147483647&count=0&clientVersion=5.2.0&chl=91assistant_Iosphone27&check=E04A5E782F8E7DAC261D77FEED1232A5&clientIp=&projectId=1800&cpu=&imsi=&cuid=96219def20140511b940d862b33980b6edd32d37&clienttype=pxl&requestId=0&imei=69b4402022f4633f11fceeb5b3df42d83cbf1f47", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
//        toHMAC_Sha1_Base64("GetActivityPageProtobufReturn?platform=iPhone&fw=iPhone_7_0_0&actype=0&showPlace=2&pageIndex=1&pageSize=2147483647&count=0&clientVersion=5.2.0&chl=91assistant_Iosphone27&check=E04A5E782F8E7DAC261D77FEED1232A5&clientIp=&projectId=1800&cpu=&imsi=&cuid=96219def20140511b940d862b33980b6edd32d37&clienttype=ipa&requestId=0&imei=69b4402022f4633f11fceeb5b3df42d83cbf1f47", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
//        toHMAC_Sha1_Base64("ClearConfigCache?act=cleartagcache&tagid=1013310&platformid=4&seriesId=30340", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
//        toHMAC_Sha1_Base64("ClearConfigCache?act=cleartagcache&tagid=1007794&platformid=4&seriesId=30258", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
//        toHMAC_Sha1_Base64("ClearConfigCache?act=cleartagcache&tagid=884868&platformid=4&seriesId=30134", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
//        toHMAC_Sha1_Base64("ClearConfigCache?act=cleartagcache&tagid=138598&platformid=4&seriesId=30079", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
//        toHMAC_Sha1_Base64("ClearConfigCache?act=cleartagcache&tagid=1012764&platformid=4&seriesId=50038", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
//        toHMAC_Sha1_Base64("GetActivityPageProtobufReturn?platform=iPhone&fw=iPhone_7_0_0&actype=0&showPlace=2&pageIndex=1&pageSize=2147483647&count=0&clientVersion=5.2.0&chl=91assistant_Iosphone27&check=E04A5E782F8E7DAC261D77FEED1232A5&clientIp=&projectId=1800&cpu=&imsi=&cuid=96219def20140511b940d862b33980b6edd32d37&clienttype=ipa&requestId=0&imei=69b4402022f4633f11fceeb5b3df42d83cbf1f47", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
//        toHMAC_Sha1_Base64("GetItuneTemplateAdvertProtobufReturn?platform=iPhone&fw=iPhone_7_1_0&placeIds=2001&clientVersion=2.5.6&IsHao123=False&chl=jZg3GleBqu8bs14RkWZYOsGGj4HzUz6l&check=34AD61DDC3409A55F372075691955BC6&clientIp=172.17.148.52&projectId=6800&NoCmsTag=False&getInstall=0&cpu=&imsi=&isappstore=False&cuid=5da745a427dd5a18e8565bfacb8c8c88fc5d6134&osv=7.1.2&requestId=4974717103965367433&imei=383DD6B0-BAFB-4F05-8A61-189B6808BBC4", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
//        toHMAC_Sha1_Base64("GetTagItuneAdvertProtobufReturn?platform=iPhone&fw=iPhone_7_1_0&edittagid=2508&check=9DFBD4C3069C946A6CC59A3BA608F73C&clientIp=172.17.148.52&projectid=6800&oldMode=False&count=12&getInstall=0&cpu=&imsi=&isappstore=False&cuid=5da745a427dd5a18e8565bfacb8c8c88fc5d6134&osv=7.1.2&requestId=5401613062820295179&clientVersion=2.5.6&imei=383DD6B0-BAFB-4F05-8A61-189B6808BBC4", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
//        toHMAC_Sha1_Base64("GetTagItuneAdvertBothProtobufReturn?platform=iPhone&fw=iPhone_7_1_0&edittagid=846&adtagid=1075&check=EAF0FB4A0DC3EE5120840022C4907EB8&clientIp=172.17.148.52&projectid=6800&getInstall=0&cpu=&imsi=&isappstore=False&testymd=&cuid=5da745a427dd5a18e8565bfacb8c8c88fc5d6134&osv=7.1.2&requestId=5475206327796554725&clientVersion=2.5.6&imei=383DD6B0-BAFB-4F05-8A61-189B6808BBC4", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
//        toHMAC_Sha1_Base64("GetTagItuneAdvertBothProtobufReturn?platform=iPhone&fw=iPhone_7_1_0&edittagid=2556&adtagid=0&check=A3BABDEE191E9366F13DFA61C8F573F2&clientIp=172.17.148.52&projectid=6800&getInstall=0&cpu=&imsi=&isappstore=False&testymd=&cuid=5da745a427dd5a18e8565bfacb8c8c88fc5d6134&osv=7.1.2&requestId=5078143301239542000&clientVersion=2.5.6&imei=383DD6B0-BAFB-4F05-8A61-189B6808BBC4", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
//        toHMAC_Sha1_Base64("GetTagItuneAdvertBothProtobufReturn?platform=iPhone&fw=iPhone_7_1_0&edittagid=846&adtagid=1075&check=EAF0FB4A0DC3EE5120840022C4907EB8&clientIp=172.17.148.52&projectid=6800&getInstall=0&cpu=&imsi=&isappstore=False&testymd=&cuid=5da745a427dd5a18e8565bfacb8c8c88fc5d6134&osv=7.1.2&requestId=5071016259077679437&clientVersion=2.5.6&imei=383DD6B0-BAFB-4F05-8A61-189B6808BBC4", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
//        toHMAC_Sha1_Base64("GetTagItuneAdvertBothProtobufReturn?platform=iPhone&fw=iPhone_8_4_0&edittagid=0&adtagid=2276&check=E3612367311D81DA51E4A1161E9879B7&clientIp=&projectid=6800&getInstall=0&cpu=0&imsi=&isappstore=False&testymd=&cuid=&osv=&requestId=0&imei=5dedc7c2-7f34-47ae-bb93-80bdd4da4298", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
//        toHMAC_Sha1_Base64("GetItuneTemplateAdvertProtobufReturn?platform=iPhone&fw=iPhone_5_1_0&placeIds=3001&clientVersion=1.3&IsHao123=False&chl=iMGAs5ltDHqfB7qvZMF0hw==&check=56571486AE166B4B135BC7CF6AD8F5D1&clientIp=172.17.148.52&projectId=6800&NoCmsTag=False&getInstall=0&cpu=&imsi=&isappstore=False&cuid=fd467bf7587dd29995400c1bd727d99e37a98954&osv=5.1.1&requestId=5115239870141487933&imei=515FA066-499B-424B-A436-3DB4CE1B7089", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
//        toHMAC_Sha1_Base64("GetTagItuneAdvertProtobufReturn?platform=iPhone&fw=iPhone_5_1_0&edittagid=2508&check=C1D756F1CDFF8073DFFE70731C844A73&clientIp=172.17.148.52&projectid=6800&oldMode=False&count=12&getInstall=0&cpu=&imsi=&isappstore=False&cuid=fd467bf7587dd29995400c1bd727d99e37a98954&osv=5.1.1&requestId=5327850472230011956&clientVersion=1.3&imei=515FA066-499B-424B-A436-3DB4CE1B7089", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
//        toHMAC_Sha1_Base64("GetTagItuneAdvertBothProtobufReturn?platform=iPhone&fw=iPhone_5_1_0&edittagid=2556&adtagid=0&check=EF22D6CB116D832A77A11423E9149B81&clientIp=172.17.148.52&projectid=6800&getInstall=0&cpu=&imsi=&isappstore=False&testymd=&cuid=fd467bf7587dd29995400c1bd727d99e37a98954&osv=5.1.1&requestId=5211820251332758549&clientVersion=1.3&imei=515FA066-499B-424B-A436-3DB4CE1B7089", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
//        toHMAC_Sha1_Base64("GetTagItuneAdvertBothProtobufReturn?platform=iPhone&fw=iPhone_5_1_0&edittagid=846&adtagid=1075&check=199B1185BB3B6A08EE963E4B2816F35E&clientIp=172.17.148.52&projectid=6800&getInstall=0&cpu=&imsi=&isappstore=False&testymd=&cuid=fd467bf7587dd29995400c1bd727d99e37a98954&osv=5.1.1&requestId=4854554445237497274&clientVersion=1.3&imei=515FA066-499B-424B-A436-3DB4CE1B7089", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
//        toHMAC_Sha1_Base64("GetTagItuneAdvertBothProtobufReturn?platform=iPhone&fw=iPhone_5_1_0&edittagid=846&adtagid=1075&check=199B1185BB3B6A08EE963E4B2816F35E&clientIp=172.17.148.52&projectid=6800&getInstall=0&cpu=&imsi=&isappstore=False&testymd=&cuid=fd467bf7587dd29995400c1bd727d99e37a98954&osv=5.1.1&requestId=5269065718736981945&clientVersion=1.3&imei=515FA066-499B-424B-A436-3DB4CE1B7089", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
//        toHMAC_Sha1_Base64("GetTagItuneAdvertBothProtobufReturn?platform=iPhone&fw=iPhone_8_4_0&edittagid=0&adtagid=2276&check=E3612367311D81DA51E4A1161E9879B7&clientIp=&projectid=6800&getInstall=0&cpu=0&imsi=&isappstore=False&testymd=&cuid=&osv=&requestId=0&imei=5dedc7c2-7f34-47ae-bb93-80bdd4da4298", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
//        toHMAC_Sha1_Base64("GetTagItuneAdvertBothProtobufReturn?platform=iPhone&fw=iPhone_8_2_0&edittagid=0&adtagid=2276&check=B936691B3A343DEA07DA83553729942D&clientIp=&projectid=6800&getInstall=0&cpu=0&imsi=&isappstore=False&testymd=&cuid=&osv=&requestId=0&imei=ae0bbaac-6c9c-42d4-a0fc-f3470a39f710", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
//        toHMAC_Sha1_Base64("GetTagItuneAdvertBothProtobufReturn?platform=iPhone&fw=iPhone_8_4_0&edittagid=0&adtagid=2276&check=E3612367311D81DA51E4A1161E9879B7&clientIp=&projectid=6800&getInstall=0&cpu=0&imsi=&isappstore=False&testymd=&cuid=&osv=&requestId=0&imei=5dedc7c2-7f34-47ae-bb93-80bdd4da4298", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
//        toHMAC_Sha1_Base64("ClearConfigCache?act=cleartagcache&tagid=1007791&platformid=4&seriesId=30112", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
//        toHMAC_Sha1_Base64("ClearConfigCache?act=cleartagcache&tagid=1007794&platformid=4&seriesId=30258", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
//        toHMAC_Sha1_Base64("ClearConfigCache?act=cleartagcache&tagid=1007793&platformid=4&seriesId=30257", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
//        toHMAC_Sha1_Base64("ClearConfigCache?act=cleartagcache&tagid=1013310&platformid=4&seriesId=30340", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
//        toHMAC_Sha1_Base64("ClearConfigCache?act=cleartagcache&tagid=251956&platformid=4&seriesId=30071", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
//        toHMAC_Sha1_Base64("ClearConfigCache?act=cleartagcache&tagid=1008526&platformid=4&seriesId=30353", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
//        toHMAC_Sha1_Base64("ClearConfigCache?act=cleartagcache&tagid=138598&platformid=4&seriesId=30079", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
//        toHMAC_Sha1_Base64("ClearConfigCache?act=cleartagcache&tagid=1012764&platformid=4&seriesId=50038", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
//        toHMAC_Sha1_Base64("GetTagItuneAdvertBothProtobufReturn?platform=iPhone&fw=iPhone_MAX&edittagid=0&adtagid=2257&check=180573E44537FEA748CEB897B695ACD5&clientIp=&projectid=0&getInstall=0&cpu=&imsi=&isappstore=False&testymd=&cuid=&osv=&requestId=0&imei=", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
//        toHMAC_Sha1_Base64("GetTagItuneAdvertBothProtobufReturn?platform=iPhone&fw=iPhone_9_0_0&edittagid=0&adtagid=2276&check=EEEBE5BEDC91E7B34464B9DBD3F72D4C&clientIp=&projectid=6800&getInstall=0&cpu=0&imsi=&isappstore=False&testymd=&cuid=&osv=&requestId=0&imei=312320c9-3417-4258-9cf4-df54ab902344", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
//        toHMAC_Sha1_Base64("GetActivityPageProtobufReturn?platform=iPhone&fw=iPhone_8_0_0&actype=0&showPlace=2&pageIndex=1&pageSize=2147483647&count=0&clientVersion=5.2.0.1&chl=91assistant_Iosphone22&check=99B69DA1D8311AF1A81F09244BB8B8C3&clientIp=&projectId=1800&cpu=&imsi=&cuid=3bd57f44dab033cb14b89a43e4a5d78dd71faf70&clienttype=pxl&requestId=0&imei=3fd7cdf5efacfeba47d693674d2ee24a35b8d2b4", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
//        toHMAC_Sha1_Base64("GetActivityPageProtobufReturn?platform=iPhone&fw=iPhone_8_0_0&actype=0&showPlace=2&pageIndex=1&pageSize=2147483647&count=0&clientVersion=5.2.0.1&chl=91assistant_Iosphone22&check=99B69DA1D8311AF1A81F09244BB8B8C3&clientIp=&projectId=1800&cpu=&imsi=&cuid=3bd57f44dab033cb14b89a43e4a5d78dd71faf70&clienttype=pxl&requestId=0&imei=3fd7cdf5efacfeba47d693674d2ee24a35b8d2b4", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
//        toHMAC_Sha1_Base64("GetActivityPageProtobufReturn?platform=iPhone&fw=iPhone_8_0_0&actype=0&showPlace=2&pageIndex=1&pageSize=2147483647&count=0&clientVersion=5.1.1&chl=91assistant_Iosphone26&check=99B69DA1D8311AF1A81F09244BB8B8C3&clientIp=&projectId=1800&cpu=&imsi=&cuid=3bd57f44dab033cb14b89a43e4a5d78dd71faf70&clienttype=ipa&requestId=0&imei=3fd7cdf5efacfeba47d693674d2ee24a35b8d2b4", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
//        toHMAC_Sha1_Base64("ClearConfigCache?act=cleartagcache&tagid=1013310&platformid=4&seriesId=30340", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
//        toHMAC_Sha1_Base64("ClearConfigCache?act=cleartagcache&tagid=1007793&platformid=4&seriesId=30257", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
//        toHMAC_Sha1_Base64("ClearConfigCache?act=cleartagcache&tagid=1007794&platformid=4&seriesId=30258", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
//        toHMAC_Sha1_Base64("ClearConfigCache?act=cleartagcache&tagid=1008526&platformid=4&seriesId=30353", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
//        toHMAC_Sha1_Base64("ClearConfigCache?act=cleartagcache&tagid=251956&platformid=4&seriesId=30071", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
//        toHMAC_Sha1_Base64("ClearConfigCache?act=cleartagcache&tagid=237453&platformid=4&seriesId=30077", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
//        toHMAC_Sha1_Base64("ClearConfigCache?act=cleartagcache&tagid=138598&platformid=4&seriesId=30079", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
//        toHMAC_Sha1_Base64("ClearConfigCache?act=cleartagcache&tagid=884868&platformid=4&seriesId=30134", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
//        toHMAC_Sha1_Base64("ClearConfigCache?act=cleartagcache&tagid=1012764&platformid=4&seriesId=50038", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
//        toHMAC_Sha1_Base64("GetActivityPageProtobufReturn?platform=iPhone&fw=iPhone_8_0_0&actype=0&showPlace=2&pageIndex=1&pageSize=2147483647&count=0&clientVersion=5.1.1&chl=91assistant_Iosphone26&check=99B69DA1D8311AF1A81F09244BB8B8C3&clientIp=&projectId=1800&cpu=&imsi=&cuid=3bd57f44dab033cb14b89a43e4a5d78dd71faf70&clienttype=ipa&requestId=0&imei=3fd7cdf5efacfeba47d693674d2ee24a35b8d2b4", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
//        toHMAC_Sha1_Base64("GetActivityPageProtobufReturn?platform=iPhone&fw=iPhone_7_0_0&actype=0&showPlace=2&pageIndex=1&pageSize=2147483647&count=0&clientVersion=5.2.0&chl=91assistant_Iosphone27&check=E04A5E782F8E7DAC261D77FEED1232A5&clientIp=&projectId=1800&cpu=&imsi=&cuid=96219def20140511b940d862b33980b6edd32d37&clienttype=ipa&requestId=0&imei=69b4402022f4633f11fceeb5b3df42d83cbf1f47", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
//        toHMAC_Sha1_Base64("GetItuneTemplateAdvertProtobufReturn?platform=iPhone&fw=iPhone_9_1_0&placeIds=1&clientVersion=2.5.6&IsHao123=False&chl=jZg3GleBqu8bs14RkWZYOsGGj4HzUz6l&check=161DEA8D3CDA0B7AB87BF6D8D41384E5&clientIp=172.17.148.52&projectId=6800&NoCmsTag=False&getInstall=0&cpu=&imsi=&isappstore=False&cuid=82a16618329edcb0313fb209f22823570eaceef4&osv=9.1&requestId=5236952727179215389&imei=018AA55A-F4CB-471A-B713-60764013C633", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
//        toHMAC_Sha1_Base64("GetTagItuneAdvertProtobufReturn?platform=iPhone&fw=iPhone_9_1_0&edittagid=2508&check=34C802A926196F6C35384DD2AEC09827&clientIp=172.17.148.52&projectid=6800&oldMode=False&count=12&getInstall=0&cpu=&imsi=&isappstore=False&cuid=82a16618329edcb0313fb209f22823570eaceef4&osv=9.1&requestId=4835101214890950931&clientVersion=2.5.6&imei=018AA55A-F4CB-471A-B713-60764013C633", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
//        toHMAC_Sha1_Base64("GetTagItuneAdvertBothProtobufReturn?platform=iPhone&fw=iPhone_9_1_0&edittagid=1376&adtagid=1075&check=06BF208DF66E01F23284DB9B7FD4E98A&clientIp=172.17.148.52&projectid=6800&getInstall=0&cpu=&imsi=&isappstore=False&testymd=&cuid=82a16618329edcb0313fb209f22823570eaceef4&osv=9.1&requestId=4687684435976106806&clientVersion=2.5.6&imei=018AA55A-F4CB-471A-B713-60764013C633", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
//        toHMAC_Sha1_Base64("GetTagItuneAdvertBothProtobufReturn?platform=iPhone&fw=iPhone_9_1_0&edittagid=2556&adtagid=0&check=C891A61728BFE9F39104313BB8C5B199&clientIp=172.17.148.52&projectid=6800&getInstall=0&cpu=&imsi=&isappstore=False&testymd=&cuid=82a16618329edcb0313fb209f22823570eaceef4&osv=9.1&requestId=5452934953438205940&clientVersion=2.5.6&imei=018AA55A-F4CB-471A-B713-60764013C633", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
//        toHMAC_Sha1_Base64("GetTagItuneAdvertBothProtobufReturn?platform=iPhone&fw=iPhone_9_1_0&edittagid=1376&adtagid=1075&check=06BF208DF66E01F23284DB9B7FD4E98A&clientIp=172.17.148.52&projectid=6800&getInstall=0&cpu=&imsi=&isappstore=False&testymd=&cuid=82a16618329edcb0313fb209f22823570eaceef4&osv=9.1&requestId=5690154915214525413&clientVersion=2.5.6&imei=018AA55A-F4CB-471A-B713-60764013C633", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
//        toHMAC_Sha1_Base64("GetActivityPageProtobufReturn?platform=iPhone&fw=iPhone_7_0_0&actype=0&showPlace=2&pageIndex=1&pageSize=2147483647&count=0&clientVersion=5.1.2.1&chl=91assistant_Iosphone22&check=E04A5E782F8E7DAC261D77FEED1232A5&clientIp=&projectId=1800&cpu=&imsi=&cuid=96219def20140511b940d862b33980b6edd32d37&clienttype=pxl&requestId=0&imei=69b4402022f4633f11fceeb5b3df42d83cbf1f47", "hCqq5mb4YUxYU7WTLMTs6qiU-RdvgXKVESWURj");
//

    }

    private static void aesEncrypt(String source, String key)
    {
        try
        {
//加密
            //byte[] encryptResult = encrypt(source, key);
//解密
            //byte[] decryptResult = decrypt(encryptResult, key);
            //System.out.println("解密后：" + new String(decryptResult));

            String iv = key.substring(0, 16);


            String encryptResult = CryptoHelper.aesEncrypt(source, key);
            String encryptResultiv = CryptoHelper.aesEncrypt(source, key, iv);

            System.out.printf("%s\t%s\t%s\t%s", "aesEncrypt", source, key, encryptResult).println();
            System.out.println(encryptResultiv);


            String decryptResult = CryptoHelper.aesDecrypt(encryptResult, key);
            System.out.printf("%s\t%s\t%s\t%s", "aesDecrypt", encryptResult, key, new String(decryptResult)).println();
            System.out.println();
            System.out.println();

        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    private static void aesEncryptAndDecryptIv(String source, String key, String iv)
    {
        try
        {
            System.out.println("==aesEncryptAndDecryptIv start==================================================================");
            String encryptResult = CryptoHelper.aesEncrypt(source, key, iv);
            System.out.printf("%s\t%s\t%s\t%s\t%s", "aesEncryptIv", source, key, iv, encryptResult).println();
            String decryptResult = CryptoHelper.aesDecrypt(encryptResult, key, iv);
            System.out.printf("%s\t%s\t%s\t%s\t%s", "aesDecryptIv", encryptResult, key, iv, decryptResult).println();
            System.out.println("==aesEncryptAndDecryptIv end=================================================================");
            System.out.println();
            System.out.println();


        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    private static void aesEncryptByte(String source, String key)
    {
        try
        {
            byte[] data = CryptoHelper.aesEncryptBytes(source.getBytes("utf-8"), key.getBytes("utf-8"), key.substring(0, 16).getBytes("utf-8"));
            System.out.printf("%s\t%s\t%s\t%s", "aesEncryptByte", source, key, new Base64().encode(data)).println();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    private static void aesEncryptByteIv(String source, String key, String iv)
    {
        try
        {
            byte[] data = CryptoHelper.aesEncryptBytes(source.getBytes("utf-8"), key.getBytes("utf-8"), iv.getBytes("utf-8"));
            System.out.printf("%s\t%s\t%s\t%s\t%s", "aesEncryptByteIv", source, key, iv, new Base64().encode(data)).println();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }


    private static void aesDecryptIv(String source, String key, String iv)
    {
        try
        {
            String result = CryptoHelper.aesDecrypt(source, key, iv);
            System.out.printf("%s\t%s\t%s\t%s\t%s", "aesDecryptIv", source, key, iv, result).println();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    private static void desEncrypt(String source, String key)
    {
        try
        {
            System.out.println("");
            System.out.println("");
            System.out.println("===des加密==");
            String encryptResult = CryptoHelper.desEncrypt(source, key);
            System.out.printf("%s\t%s\t%s", source, key, encryptResult).println();
            System.out.printf("%s\t%s\t%s\t%s", source, key, key.substring(0, 8), CryptoHelper.desEncrypt(source, key, key.substring(0, 8))).println();
            System.out.printf("%s\t%s\t%s\t%s\t%s", source, key, key.substring(0, 8), "utf-8", CryptoHelper.desEncrypt(source, key, key.substring(0, 8), "utf-8")).println();
            System.out.printf("%s\t%s\t%s\t%s\t%s", source, key, key.substring(0, 8), "gb2312", CryptoHelper.desEncrypt(source, key, key.substring(0, 8), "gb2312")).println();

            System.out.println();
            //encryptResult = "A3E8FE6E6D5E14726B11A5CF38CF27A87504E614BCE2118A";
            String decryptResult = CryptoHelper.desDecrypt(encryptResult, key);
            System.out.printf("%s\t%s\t%s", encryptResult, key, decryptResult).println();
            System.out.printf("%s\t%s\t%s\t%s", encryptResult, key, key.substring(0, 8), CryptoHelper.desDecrypt(encryptResult, key, key.substring(0, 8))).println();
            System.out.printf("%s\t%s\t%s\t%s\t%s", encryptResult, key, key.substring(0, 8), "utf-8", CryptoHelper.desDecrypt(encryptResult, key, key.substring(0, 8), "utf-8")).println();
        }
        catch (Exception e)
        {
            e.printStackTrace();

        }
    }


    private static void tripleDesEncrypt(String source, String key)
    {
        try
        {
            System.out.println("");
            System.out.println("");
            System.out.println("===3des加密==");
            String encryptResult = CryptoHelper.tripleDesEncrypt(source, key);
            System.out.printf("%s\t%s\t%s", source, key, encryptResult).println();
            System.out.printf("%s\t%s\t%s\t%s", source, key, key.substring(0, 8), CryptoHelper.tripleDesEncrypt(source, key, key.substring(0, 8))).println();
            System.out.printf("%s\t%s\t%s\t%s\t%s", source, key, key.substring(0, 8), "utf-8", CryptoHelper.tripleDesEncrypt(source, key, key.substring(0, 8), "utf-8")).println();
            System.out.printf("%s\t%s\t%s\t%s\t%s", source, key, key.substring(0, 8), "gb2312", CryptoHelper.tripleDesEncrypt(source, key, key.substring(0, 8), "gb2312")).println();

            System.out.println();
            //encryptResult = "A3E8FE6E6D5E14726B11A5CF38CF27A87504E614BCE2118A";
            String decryptResult = CryptoHelper.tripleDesDecrypt(encryptResult, key);
            System.out.printf("%s\t%s\t%s", encryptResult, key, decryptResult).println();
            System.out.printf("%s\t%s\t%s\t%s", encryptResult, key, key.substring(0, 8), CryptoHelper.tripleDesDecrypt(encryptResult, key, key.substring(0, 8))).println();
            System.out.printf("%s\t%s\t%s\t%s\t%s", encryptResult, key, key.substring(0, 8), "utf-8", CryptoHelper.tripleDesDecrypt(encryptResult, key, key.substring(0, 8), "utf-8")).println();

        }
        catch (Exception e)
        {
            e.printStackTrace();

        }
    }


    private static byte[] encrypt(String content, String password)
    {
        try
        {
            KeyGenerator kgen = KeyGenerator.getInstance("AES");
            kgen.init(128, new SecureRandom(password.getBytes()));
            SecretKey secretKey = kgen.generateKey();
            byte[] enCodeFormat = secretKey.getEncoded();
            SecretKeySpec key = new SecretKeySpec(enCodeFormat, "AES");
            Cipher cipher = Cipher.getInstance("AES");// 创建密码器
            byte[] byteContent = content.getBytes("utf-8");
            cipher.init(Cipher.ENCRYPT_MODE, key);// 初始化
            byte[] result = cipher.doFinal(byteContent);
            return result; // 加密
        }
        catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }
        catch (NoSuchPaddingException e)
        {
            e.printStackTrace();
        }
        catch (InvalidKeyException e)
        {
            e.printStackTrace();
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }
        catch (IllegalBlockSizeException e)
        {
            e.printStackTrace();
        }
        catch (BadPaddingException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 解密
     *
     * @param content  待解密内容
     * @param password 解密密钥
     * @return
     */
    public static byte[] decrypt(byte[] content, String password)
    {
        try
        {
            KeyGenerator kgen = KeyGenerator.getInstance("AES");
            kgen.init(128, new SecureRandom(password.getBytes()));
            SecretKey secretKey = kgen.generateKey();
            byte[] enCodeFormat = secretKey.getEncoded();
            SecretKeySpec key = new SecretKeySpec(enCodeFormat, "AES");
            Cipher cipher = Cipher.getInstance("AES");// 创建密码器
            cipher.init(Cipher.DECRYPT_MODE, key);// 初始化
            byte[] result = cipher.doFinal(content);
            return result; // 加密
        }
        catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }
        catch (NoSuchPaddingException e)
        {
            e.printStackTrace();
        }
        catch (InvalidKeyException e)
        {
            e.printStackTrace();
        }
        catch (IllegalBlockSizeException e)
        {
            e.printStackTrace();
        }
        catch (BadPaddingException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public static void toHMAC_Sha1_Base64(String source, String key)
    {
        try
        {
            String result = CryptoHelper.hash_HMAC_Sha1_Base64(source, key);
            String resultUrlSafe = CryptoHelper.hash_HMAC_Sha1_Base64UrlSafe(source, key);
            System.out.printf("%s", source).println();
            System.out.printf("%s, %s, %s", key, result, resultUrlSafe).println();
            System.out.println();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    @Test
    public void decryptChannel()
    {
        try
        {
            System.out.println(CryptoHelper.decryptChannel("LL0zK5Dt9l7Hp9mJma7e02cS87o2CV57"));
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

    }

}
