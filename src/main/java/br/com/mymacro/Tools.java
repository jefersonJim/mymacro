package br.com.mymacro;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.KeyEvent;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;
import org.jnativehook.mouse.NativeMouseEvent;
import org.jnativehook.mouse.NativeMouseInputListener;
import org.jnativehook.mouse.NativeMouseWheelEvent;
import org.jnativehook.mouse.NativeMouseWheelListener;
import org.slf4j.helpers.MessageFormatter;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.win32.StdCallLibrary;

public class Tools implements ClipboardOwner, NativeKeyListener, NativeMouseInputListener, NativeMouseWheelListener {

	private SQLiteJDBCDriverConnection conn = new SQLiteJDBCDriverConnection();
	private Boolean isEnableMacro = true;
	private Boolean pad1 = false;
	private Boolean ctrl = false;
	private Boolean anotherKey = false;

	public interface CustomUser32 extends StdCallLibrary {
		CustomUser32 INSTANCE = (CustomUser32) Native.load("user32", CustomUser32.class);

		HWND GetForegroundWindow();

		void keybd_event(byte bVk, byte bScan, int dwFlags, int dwExtraInfo);
	}

	void controlC() {
		CustomUser32 customUser32 = CustomUser32.INSTANCE;
		customUser32.keybd_event((byte) 0x11, (byte) 0, 0, 0);
		customUser32.keybd_event((byte) 0x43, (byte) 0, 0, 0);
		customUser32.keybd_event((byte) 0x43, (byte) 0, 2, 0);
		customUser32.keybd_event((byte) 0x11, (byte) 0, 2, 0);
	}

	void controlV() throws AWTException {
		Robot robot = new Robot();
		robot.keyPress(KeyEvent.VK_CONTROL);
		robot.keyPress(KeyEvent.VK_V);
		robot.keyRelease(KeyEvent.VK_V);
		robot.keyRelease(KeyEvent.VK_CONTROL);
	}

	String getClipboardText() throws Exception {
		return (String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
	}

	void setClipboardText(String data) throws Exception {
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(data), this);
	}

	@Override
	public void lostOwnership(Clipboard clipboard, Transferable contents) {
	}

	@Override
	public void nativeKeyPressed(NativeKeyEvent e) {
		if (e.getKeyCode() == 2) {
			this.pad1 = true;
		}
		if (e.getKeyCode() == 29) {
			this.ctrl = true;
		}
		if (e.getKeyCode() != 2 && e.getKeyCode() != 29) {
			this.anotherKey = true;
		}

		this.macro(pad1 && ctrl && !anotherKey);
	}

	@Override
	public void nativeKeyReleased(NativeKeyEvent e) {
		if (e.getKeyCode() != 2 && e.getKeyCode() != 29) {
			this.anotherKey = false;
		}
	}

	static void sleep(long millissegundos) {
		try {
			Thread.sleep(millissegundos);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void macro(Boolean ativar) {
		if (ativar && isEnableMacro) {
			isEnableMacro = false;
			Tools.unRegisterListner();
			try {
				controlC();
				sleep(500);
				String textSelected = getClipboardText();
				setClipboardText(getMacro(1, textSelected));
				sleep(500);
				controlV();
			} catch (Exception e) {
				System.out.println(e);
			}
			isEnableMacro = true;
			pad1 = false;
			ctrl = false;
			Tools.registerListner();
		}
	}

	public String getMacro(Integer ordem, String selectedText) {
		ResultSet set;
		String macro = null;
		try {
			set = conn.select("SELECT * FROM MACRO WHERE ORDEM = " + ordem);
			if(set.next()) {
				String[] param = selectedText.split(" ");
				macro = set.getString("MACRO");
				macro = format(macro, param);
				
			}
			set.close();
		} catch (SQLException e) {
			System.out.println(e);
		}
		return macro;
	}

	public static String format(String msg, Object... objs) {
		return MessageFormatter.arrayFormat(msg, objs).getMessage();
	}

	@Override
	public void nativeKeyTyped(NativeKeyEvent e) {}

	public static void registraListener() {
		Tools tools = new Tools();

		LogManager.getLogManager().reset();
		Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
		logger.setLevel(Level.OFF);

		GlobalScreen.addNativeKeyListener(tools);
		GlobalScreen.addNativeMouseListener(tools);
		GlobalScreen.addNativeMouseWheelListener(tools);
		try {
			GlobalScreen.registerNativeHook();

		} catch (NativeHookException e) {
			System.out.println(e);
		}
	}
	
	public static void unRegisterListner() {
		try {
			GlobalScreen.unregisterNativeHook();
		} catch (NativeHookException e) {
			System.out.println(e);
		}
	}
	
	public static void registerListner() {
		try {
			GlobalScreen.registerNativeHook();;
		} catch (NativeHookException e) {
			System.out.println(e);
		}
	}

	@Override
	public void nativeMouseClicked(NativeMouseEvent e) {
	}

	@Override
	public void nativeMousePressed(NativeMouseEvent e) {
	}

	@Override
	public void nativeMouseReleased(NativeMouseEvent e) {
	}

	@Override
	public void nativeMouseMoved(NativeMouseEvent e) {
	}

	@Override
	public void nativeMouseDragged(NativeMouseEvent e) {
	}

	@Override
	public void nativeMouseWheelMoved(NativeMouseWheelEvent e) {
	}
}
