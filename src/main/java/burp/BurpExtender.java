package burp;

import burp.aes.AesUIHandler;
import burp.des.DesUIHandler;
import burp.execjs.JsUIHandler;
import burp.pbkdf2.PBKDF2UIHandler;
import burp.rsa.RsaUIHandler;
import burp.sm3.SM3UIHandler;
import burp.sm4.SM4UIHandler;
import burp.utils.BurpCryptoMenuFactory;
import burp.utils.BurpStateListener;
import burp.utils.DictLogManager;
import burp.utils.Utils;
import burp.zuc.ZUCUIHandler;
import cn.hutool.crypto.SecureUtil;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.Options;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

import static org.iq80.leveldb.impl.Iq80DBFactory.factory;

/**
 * BurpExtender类实现了IBurpExtender和ITab接口，用于扩展Burp Suite的功能。
 * 它主要负责处理各种加密和解密操作的用户界面，并提供与Burp Suite交互的接口。
 */
public class BurpExtender implements IBurpExtender, ITab {

    // Burp Suite的辅助工具，用于各种操作
    public IExtensionHelpers helpers;
    // Burp Suite的回调接口，用于注册扩展的功能
    public IBurpExtenderCallbacks callbacks;
    // 标准输出，用于打印日志信息
    public PrintWriter stdout;
    // 错误输出，用于打印错误信息
    public PrintWriter stderr;
    // 数据存储，使用LevelDB数据库
    public DB store;
    // 字典日志管理器，用于管理字典日志
    public DictLogManager dict;
    // 版本号
    public String version = "0.1.9.1";
    // Intruder负载处理器映射，用于注册和管理自定义的负载处理器
    public HashMap<String, IIntruderPayloadProcessor> IPProcessors = new HashMap<>();

    // 主界面面板，使用JTabbedPane
    public JTabbedPane mainPanel;

    // AES加密的界面面板和处理器
    public JPanel aesPanel;
    public AesUIHandler AesUI;

    // RSA加密的界面面板和处理器
    public JPanel rsaPanel;
    public RsaUIHandler RsaUI;

    // DES加密的界面面板和处理器
    public JPanel desPanel;
    public DesUIHandler DesUI;

    // 执行JavaScript的界面面板和处理器
    public JPanel execJsPanel;
    public JsUIHandler JsUI;

    // SM3加密的界面面板和处理器
    public JPanel sm3Panel;
    public SM3UIHandler SM3UI;

    // SM4加密的界面面板和处理器
    public JPanel sm4Panel;
    public SM4UIHandler SM4UI;

    // ZUC加密的界面面板和处理器
    public JPanel zucPanel;
    public ZUCUIHandler ZUCUI;

    // PBKDF2加密的界面面板和处理器
    public JPanel pbkdf2Panel;
    public PBKDF2UIHandler PBKDF2UI;

    /**
     * 注册自定义的Intruder负载处理器。
     *
     * @param extName 处理器的名称，用于标识处理器。
     * @param processor 要注册的处理器实例。
     * @return 如果注册成功返回true，否则返回false。
     */
    public boolean RegIPProcessor(String extName, IIntruderPayloadProcessor processor) {
        if (IPProcessors.containsKey(extName)) {
            JOptionPane.showMessageDialog(mainPanel, "This name already exist!");
            return false;
        }
        callbacks.registerIntruderPayloadProcessor(processor);
        IPProcessors.put(extName, processor);
        return true;
    }

    /**
     * 移除已注册的Intruder负载处理器。
     *
     * @param extName 要移除的处理器的名称。
     */
    public void RemoveIPProcessor(String extName) {
        if (IPProcessors.containsKey(extName)) {
            IIntruderPayloadProcessor processor = IPProcessors.get(extName);
            callbacks.removeIntruderPayloadProcessor(processor);
            IPProcessors.remove(extName);
        }
    }

    /**
     * 注册扩展回调方法，这是Burp Suite扩展的入口点。
     *
     * @param callbacks Burp Suite提供的回调接口。
     */
    @Override
    public void registerExtenderCallbacks(IBurpExtenderCallbacks callbacks) {
        SecureUtil.disableBouncyCastle();
        this.callbacks = callbacks;
        this.helpers = callbacks.getHelpers();
        Utils.stdout = this.stdout = new PrintWriter(callbacks.getStdout(), true);
        Utils.stderr = this.stderr = new PrintWriter(callbacks.getStderr(), true);
        callbacks.setExtensionName("BurpCrypto v" + version);
        callbacks.registerExtensionStateListener(new BurpStateListener(this));
        callbacks.registerContextMenuFactory(new BurpCryptoMenuFactory(this));
        Options options = new Options();
        options.createIfMissing(true);
        try {
            this.store = factory.open(new File("BurpCrypto.ldb"), options);
            this.dict = new DictLogManager(this);
            callbacks.issueAlert("LevelDb init success!");
        } catch (IOException e) {
            callbacks.issueAlert("LevelDb init failed! error message: " + e.getMessage());
        }

        stdout.println("BurpCrypto loaded successfully!\r\n");
        stdout.println("Anthor: Whwlsfb");
        stdout.println("Email: whwlsfb@wanghw.cn");
        stdout.println("Github: https://github.com/whwlsfb/BurpCrypto");
        InitUi();
    }

    /**
     * 初始化用户界面。
     */
    private void InitUi() {
        this.AesUI = new AesUIHandler(this);
        this.RsaUI = new RsaUIHandler(this);
        this.JsUI = new JsUIHandler(this);
        this.DesUI = new DesUIHandler(this);
        this.SM3UI = new SM3UIHandler(this);
        this.SM4UI = new SM4UIHandler(this);
        this.ZUCUI = new ZUCUIHandler(this);
        this.PBKDF2UI = new PBKDF2UIHandler(this);
        SwingUtilities.invokeLater(() -> {
            BurpExtender bthis = BurpExtender.this;
            bthis.mainPanel = new JTabbedPane();
            bthis.aesPanel = AesUI.getPanel();
            bthis.mainPanel.addTab("AES", bthis.aesPanel);
            bthis.rsaPanel = RsaUI.getPanel();
            bthis.mainPanel.addTab("RSA", bthis.rsaPanel);
            bthis.desPanel = DesUI.getPanel();
            bthis.mainPanel.addTab("DES", bthis.desPanel);
            bthis.sm3Panel = SM3UI.getPanel();
            bthis.mainPanel.addTab("SM3", bthis.sm3Panel);
            bthis.sm4Panel = SM4UI.getPanel();
            bthis.mainPanel.addTab("SM4", bthis.sm4Panel);
            bthis.zucPanel = ZUCUI.getPanel();
            bthis.mainPanel.addTab("ZUC", bthis.zucPanel);
            bthis.pbkdf2Panel = PBKDF2UI.getPanel();
            bthis.mainPanel.addTab("PBKDF2", bthis.pbkdf2Panel);
            bthis.execJsPanel = JsUI.getPanel();
            bthis.mainPanel.addTab("Exec Js", bthis.execJsPanel);
            bthis.callbacks.addSuiteTab(bthis);
        });
    }

    /**
     * 获取标签标题。
     *
     * @return 标签的标题字符串。
     */
    @Override
    public String getTabCaption() {
        return "BurpCrypto";
    }

    /**
     * 获取用户界面组件。
     *
     * @return 主界面面板组件。
     */
    @Override
    public Component getUiComponent() {
        return this.mainPanel;
    }
}
