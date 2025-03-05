package burp.sm4;

import burp.utils.CipherInfo;
import burp.utils.Utils;
import cn.hutool.crypto.Mode;
import cn.hutool.crypto.Padding;
import cn.hutool.crypto.symmetric.SM4;
import cn.hutool.crypto.symmetric.SymmetricCrypto;

import java.io.UnsupportedEncodingException;

/**
 * SM4加密解密工具类
 * 提供SM4算法的加密和解密功能
 */
public class SM4Util {
    // SM4配置信息
    private SM4Config config;
    // 密码信息对象，用于存储算法、模式和填充方式
    private CipherInfo cipherInfo;
    // 对称加密对象，用于执行加密和解密操作
    private SymmetricCrypto crypto;

    /**
     * 设置SM4配置信息
     * 初始化CipherInfo和SymmetricCrypto对象
     *
     * @param config SM4配置信息，包含算法模式、密钥和向量等
     * @throws IllegalStateException 如果配置信息无效或初始化失败
     */
    public void setConfig(SM4Config config) {
        this.config = config;
        try {
            // 根据配置信息初始化密码信息对象
            this.cipherInfo = new CipherInfo(config.Algorithms.name().replace("_", "/"));
            // 根据密码信息对象初始化对称加密对象
            this.crypto = new SM4(Mode.valueOf(this.cipherInfo.Mode), Padding.valueOf(this.cipherInfo.Padding), this.config.Key, this.config.IV);
        } catch (Exception e) {
            // 如果初始化失败，抛出非法状态异常
            throw fail(e);
        }
    }

    /**
     * 加密原始文本
     * 使用SM4算法和当前配置信息对原始文本进行加密
     *
     * @param plaintext 原始文本，即未加密的数据
     * @return 加密后的字符串，根据配置的输出格式进行编码
     */
    public String encrypt(byte[] plaintext) {
        byte[] encrypted = crypto.encrypt(plaintext);
        return Utils.encode(encrypted, config.OutFormat);
    }

    /**
     * 解密密文
     * 使用SM4算法和当前配置信息对密文进行解密
     *
     * @param cipherText 密文，即已加密的数据
     * @return 解密后的字符串
     * @throws IllegalStateException 如果解密过程中发生UnsupportedEncodingException
     */
    public String decrypt(String cipherText) {
        try {
            byte[] decrypted = crypto.decrypt(Utils.base64(cipherText));
            return new String(decrypted, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw fail(e);
        }
    }

    /**
     * 处理异常
     * 打印异常堆栈跟踪并抛出非法状态异常
     *
     * @param e 原始异常
     * @return 非法状态异常，包含原始异常信息
     */
    private IllegalStateException fail(Exception e) {
        e.printStackTrace(Utils.stderr);
        return new IllegalStateException(e);
    }
}
