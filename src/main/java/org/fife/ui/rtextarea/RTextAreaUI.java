//
// BurpSuite patch
// Learn more: https://github.com/federicodotta/RSyntaxTextArea/commit/f20fc88fe47e320e32f5fea3b09518a67e26974d
//

package org.fife.ui.rtextarea;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.plaf.ActionMapUIResource;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.InputMapUIResource;
import javax.swing.plaf.InsetsUIResource;
import javax.swing.plaf.basic.BasicTextAreaUI;
import javax.swing.plaf.basic.BasicBorders.MarginBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.EditorKit;
import javax.swing.text.Element;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;
import javax.swing.text.Keymap;
import javax.swing.text.PlainView;
import javax.swing.text.View;
import javax.swing.text.WrappedPlainView;
import org.fife.ui.rtextarea.RTextAreaEditorKit.DefaultKeyTypedAction;

public class RTextAreaUI extends BasicTextAreaUI {
    private static final String SHARED_ACTION_MAP_NAME = "RTextAreaUI.actionMap";
    private static final String SHARED_INPUT_MAP_NAME = "RTextAreaUI.inputMap";
    protected RTextArea textArea;
    private static final EditorKit DEFAULT_KIT = new RTextAreaEditorKit();
    private static final TransferHandler DEFAULT_TRANSFER_HANDLER = new RTATextTransferHandler();
    private static final String RTEXTAREA_KEYMAP_NAME = "RTextAreaKeymap";

    public static ComponentUI createUI(JComponent textArea) {
        return new RTextAreaUI(textArea);
    }

    /**
     * RTextAreaUI类的构造函数
     * 该构造函数用于初始化RTextAreaUI实例，确保传入的参数是RTextArea的实例
     *
     * @param textArea 必须是RTextArea类的实例，否则将抛出IllegalArgumentException异常
     *
     * 为什么需要这个构造函数：
     * 这个构造函数的设计确保了RTextAreaUI只能被用于RTextArea类型的对象
     * 这是一种类型检查机制，避免了在UI设置过程中可能出现的类型不匹配问题
     *
     * 为什么这里要抛出IllegalArgumentException异常：
     * 抛出异常是为了确保代码的健壮性和可靠性如果传入的参数类型不正确，
     * 则立即反馈给调用者错误信息，避免了潜在的、难以追踪的错误
     */
    public RTextAreaUI(JComponent textArea) {
        // 检查传入的textArea是否为RTextArea的实例，如果不是，则抛出异常
        if (!(textArea instanceof RTextArea)) {
            throw new IllegalArgumentException("RTextAreaUI is for instances of RTextArea only!");
        } else {
            // 如果是RTextArea的实例，则将其转换为RTextArea类型并赋值给this.textArea
            this.textArea = (RTextArea)textArea;
        }
    }

    /**
     * 修正Nimbus默认样式下的一些问题
     * 此方法针对JTextComponent编辑器组件，修正或设置一些默认的样式属性，以确保组件在使用Nimbus样式时表现正常
     * 主要包括： caret颜色、选中背景色、选中文本颜色、禁用文本颜色、边框和内边距
     *
     * @param editor JTextComponent的实例，需要修正样式的编辑器组件
     */
    private void correctNimbusDefaultProblems(JTextComponent editor) {
        // 检查并设置 caret颜色
        Color c = editor.getCaretColor();
        if (c == null) {
            editor.setCaretColor(RTextArea.getDefaultCaretColor());
        }

        // 检查并设置 选中背景色
        c = editor.getSelectionColor();
        if (c == null) {
            c = UIManager.getColor("nimbusSelectionBackground");
            if (c == null) {
                c = UIManager.getColor("textHighlight");
                if (c == null) {
                    c = new ColorUIResource(Color.BLUE);
                }
            }

            editor.setSelectionColor((Color)c);
        }

        // 检查并设置 选中文本颜色
        c = editor.getSelectedTextColor();
        if (c == null) {
            c = UIManager.getColor("nimbusSelectedText");
            if (c == null) {
                c = UIManager.getColor("textHighlightText");
                if (c == null) {
                    c = new ColorUIResource(Color.WHITE);
                }
            }

            editor.setSelectedTextColor((Color)c);
        }

        // 检查并设置 禁用文本颜色
        c = editor.getDisabledTextColor();
        if (c == null) {
            c = UIManager.getColor("nimbusDisabledText");
            if (c == null) {
                c = UIManager.getColor("textInactiveText");
                if (c == null) {
                    c = new ColorUIResource(Color.DARK_GRAY);
                }
            }

            editor.setDisabledTextColor((Color)c);
        }

        // 检查并设置 边框
        Border border = editor.getBorder();
        if (border == null) {
            editor.setBorder(new MarginBorder());
        }

        // 检查并设置 内边距
        Insets margin = editor.getMargin();
        if (margin == null) {
            editor.setMargin(new InsetsUIResource(2, 2, 2, 2));
        }
    }

    public View create(Element elem) {
        return (View)(this.textArea.getLineWrap() ? new WrappedPlainView(elem, this.textArea.getWrapStyleWord()) : new PlainView(elem));
    }

    protected Caret createCaret() {
        Caret caret = new ConfigurableCaret();
        caret.setBlinkRate(500);
        return caret;
    }

    protected Highlighter createHighlighter() {
        return new RTextAreaHighlighter();
    }

    protected Keymap createKeymap() {
        Keymap map = JTextComponent.getKeymap("RTextAreaKeymap");
        //if (map == null) {
            Keymap parent = JTextComponent.getKeymap("default");
            map = JTextComponent.addKeymap("RTextAreaKeymap", parent);
            map.setDefaultAction(new DefaultKeyTypedAction());
        //}

        return map;
    }

    protected ActionMap createRTextAreaActionMap() {
        ActionMap map = new ActionMapUIResource();
        Action[] actions = this.textArea.getActions();
        int n = actions.length;
        Action[] var4 = actions;
        int var5 = actions.length;

        for(int var6 = 0; var6 < var5; ++var6) {
            Action a = var4[var6];
            map.put(a.getValue("Name"), a);
        }

        map.put(TransferHandler.getCutAction().getValue("Name"), TransferHandler.getCutAction());
        map.put(TransferHandler.getCopyAction().getValue("Name"), TransferHandler.getCopyAction());
        map.put(TransferHandler.getPasteAction().getValue("Name"), TransferHandler.getPasteAction());
        return map;
    }

    protected String getActionMapName() {
        return "RTextAreaUI.actionMap";
    }

    public EditorKit getEditorKit(JTextComponent tc) {
        return DEFAULT_KIT;
    }

    public RTextArea getRTextArea() {
        return this.textArea;
    }

    private ActionMap getRTextAreaActionMap() {
        ActionMap map = (ActionMap)UIManager.get(this.getActionMapName());
        //if (map == null) {
            map = this.createRTextAreaActionMap();
            UIManager.put(this.getActionMapName(), map);
        //}

        ActionMap componentMap = new ActionMapUIResource();
        componentMap.put("requestFocus", new RTextAreaUI.FocusAction());
        if (map != null) {
            componentMap.setParent(map);
        }

        return componentMap;
    }

    protected InputMap getRTextAreaInputMap() {
        InputMap map = new InputMapUIResource();
        InputMap shared = (InputMap)UIManager.get("RTextAreaUI.inputMap");
        //if (shared == null) {
            shared = new RTADefaultInputMap();
            UIManager.put("RTextAreaUI.inputMap", shared);
        //}

        map.setParent((InputMap)shared);
        return map;
    }

    protected Rectangle getVisibleEditorRect() {
        Rectangle alloc = this.textArea.getBounds();
        if (alloc.width > 0 && alloc.height > 0) {
            alloc.x = alloc.y = 0;
            Insets insets = this.textArea.getInsets();
            alloc.x += insets.left;
            alloc.y += insets.top;
            alloc.width -= insets.left + insets.right;
            alloc.height -= insets.top + insets.bottom;
            return alloc;
        } else {
            return null;
        }
    }

    protected void installDefaults() {
        super.installDefaults();
        JTextComponent editor = this.getComponent();
        editor.setFont(RTextAreaBase.getDefaultFont());
        this.correctNimbusDefaultProblems(editor);
        editor.setTransferHandler(DEFAULT_TRANSFER_HANDLER);
    }

    protected void installKeyboardActions() {
        RTextArea textArea = this.getRTextArea();
        textArea.setKeymap(this.createKeymap());
        InputMap map = this.getRTextAreaInputMap();
        SwingUtilities.replaceUIInputMap(textArea, 0, map);
        ActionMap am = this.getRTextAreaActionMap();
        if (am != null) {
            SwingUtilities.replaceUIActionMap(textArea, am);
        }

    }

    public void installUI(JComponent c) {
        if (!(c instanceof RTextArea)) {
            throw new Error("RTextAreaUI needs an instance of RTextArea!");
        } else {
            super.installUI(c);
        }
    }

    protected void paintBackground(Graphics g) {
        Color bg = this.textArea.getBackground();
        if (bg != null) {
            g.setColor(bg);
            Rectangle r = g.getClipBounds();
            g.fillRect(r.x, r.y, r.width, r.height);
        }

        this.paintEditorAugmentations(g);
    }

    protected void paintCurrentLineHighlight(Graphics g, Rectangle visibleRect) {
        if (this.textArea.getHighlightCurrentLine()) {
            Caret caret = this.textArea.getCaret();
            if (caret.getDot() == caret.getMark()) {
                Color highlight = this.textArea.getCurrentLineHighlightColor();
                int height = this.textArea.getLineHeight();
                if (this.textArea.getFadeCurrentLineHighlight()) {
                    Graphics2D g2d = (Graphics2D)g;
                    Color bg = this.textArea.getBackground();
                    GradientPaint paint = new GradientPaint((float)visibleRect.x, 0.0F, highlight, (float)(visibleRect.x + visibleRect.width), 0.0F, bg == null ? Color.WHITE : bg);
                    g2d.setPaint(paint);
                    g2d.fillRect(visibleRect.x, this.textArea.currentCaretY, visibleRect.width, height);
                } else {
                    g.setColor(highlight);
                    g.fillRect(visibleRect.x, this.textArea.currentCaretY, visibleRect.width, height);
                }
            }
        }

    }

    protected void paintEditorAugmentations(Graphics g) {
        Rectangle visibleRect = this.textArea.getVisibleRect();
        this.paintLineHighlights(g);
        this.paintCurrentLineHighlight(g, visibleRect);
        this.paintMarginLine(g, visibleRect);
    }

    protected void paintLineHighlights(Graphics g) {
        LineHighlightManager lhm = this.textArea.getLineHighlightManager();
        if (lhm != null) {
            lhm.paintLineHighlights(g);
        }

    }

    protected void paintMarginLine(Graphics g, Rectangle visibleRect) {
        if (this.textArea.isMarginLineEnabled()) {
            g.setColor(this.textArea.getMarginLineColor());
            Insets insets = this.textArea.getInsets();
            int marginLineX = this.textArea.getMarginLinePixelLocation() + (insets == null ? 0 : insets.left);
            g.drawLine(marginLineX, visibleRect.y, marginLineX, visibleRect.y + visibleRect.height);
        }

    }

    protected void paintSafely(Graphics g) {
        if (!this.textArea.isOpaque()) {
            this.paintEditorAugmentations(g);
        }

        super.paintSafely(g);
    }

    public int yForLine(int line) throws BadLocationException {
        int startOffs = this.textArea.getLineStartOffset(line);
        return this.yForLineContaining(startOffs);
    }

    public int yForLineContaining(int offs) throws BadLocationException {
        Rectangle r = this.modelToView(this.textArea, offs);
        return r != null ? r.y : -1;
    }

    class FocusAction extends AbstractAction {
        FocusAction() {
        }

        public void actionPerformed(ActionEvent e) {
            RTextAreaUI.this.textArea.requestFocus();
        }

        public boolean isEnabled() {
            return RTextAreaUI.this.textArea.isEditable();
        }
    }
}
