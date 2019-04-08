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

import com.sun.jna.Native;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.win32.StdCallLibrary;

public class Tools implements ClipboardOwner, NativeKeyListener, NativeMouseInputListener, NativeMouseWheelListener {

	private Boolean esc = false;
	private Boolean shift = false;

	public interface CustomUser32 extends StdCallLibrary {
		CustomUser32 INSTANCE = (CustomUser32) Native.load("user32", CustomUser32.class);

		HWND GetForegroundWindow();

		void keybd_event(byte bVk, byte bScan, int dwFlags, int dwExtraInfo);
	}

	void controlC(CustomUser32 customUser32) {
		customUser32.keybd_event((byte) 0x11, (byte) 0, 0, 0);
		customUser32.keybd_event((byte) 0x43, (byte) 0, 0, 0);
		customUser32.keybd_event((byte) 0x43, (byte) 0, 2, 0);
		customUser32.keybd_event((byte) 0x11, (byte) 0, 2, 0);
	}

	String getClipboardText() throws Exception {
		return (String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
	}

	void setClipboardText(String data) throws Exception {
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(data), this);
	}

	String getSelectedText(User32 user32, CustomUser32 customUser32) throws Exception {
		String before = getClipboardText();
		controlC(customUser32);
		Thread.sleep(100);
		String text = getClipboardText();
		setClipboardText(before);
		return text;
	}

	@Override
	public void lostOwnership(Clipboard clipboard, Transferable contents) {
	}

	@Override
	public void nativeKeyPressed(NativeKeyEvent e) {
		if (e.getKeyCode() == 42) {
			this.shift = true;
		}
		if (e.getKeyCode() == 1) {
			this.esc = true;
		}
		System.out.println(e.getKeyCode());
		this.macro(esc&&shift);
	}

	@Override
	public void nativeKeyReleased(NativeKeyEvent e) {
		if (e.getKeyCode() == 42) {
			this.shift = false;
		}
		if (e.getKeyCode() == 1) {
			this.esc = false;
		}
	}

	public void macro(Boolean ativar) {
		if (ativar) {
			Tools.unRegisterListner();
			try {
				Robot robot = new Robot();
				robot.keyPress (KeyEvent.VK_CONTROL);
				robot.keyPress (KeyEvent.VK_V);
				robot.keyRelease (KeyEvent.VK_V);
				robot.keyRelease (KeyEvent.VK_CONTROL);
			} catch ( AWTException e) {
				System.out.println(e);
			}
			
			Tools.registerListner();
		}
	}

	@Override
	public void nativeKeyTyped(NativeKeyEvent e) {
	}

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
