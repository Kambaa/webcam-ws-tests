package tr.com.yusufgunduz;

import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinUser;

public class Aa {


    public static void main(String[] args) {
        // Define the window class
        WinUser.WNDCLASSEX windowClass = new WinUser.WNDCLASSEX();
//        windowClass.hInstance = User32.INSTANCE.GetModuleHandle(null);
        windowClass.lpfnWndProc = new WindowProc();
        windowClass.lpszClassName = "MyWindowClass";
        User32.INSTANCE.RegisterClassEx(windowClass);

        // Create the window
        HWND hWnd = User32.INSTANCE.CreateWindowEx(
                0,
                "MyWindowClass", // Use the class name specified earlier
                "My Window",
                WinUser.WS_OVERLAPPEDWINDOW,
                100,
                100,
                640,
                480,
                null,
                null,
                null,// windowClass.hInstance,
                null
        );

        if (hWnd != null) {
            // Show and update the window
            User32.INSTANCE.ShowWindow(hWnd, WinUser.SW_SHOWNORMAL);
            User32.INSTANCE.UpdateWindow(hWnd);

            // Message loop
            WinUser.MSG msg = new WinUser.MSG();
            while (User32.INSTANCE.GetMessage(msg, null, 0, 0) > 0) {
                User32.INSTANCE.TranslateMessage(msg);
                User32.INSTANCE.DispatchMessage(msg);
            }
        }
    }

    // Custom Window Procedure to handle window messages
    public static class WindowProc implements WinUser.WindowProc {

        @Override
        public com.sun.jna.platform.win32.WinDef.LRESULT callback(
                HWND hwnd,
                int uMsg,
                com.sun.jna.platform.win32.WinDef.WPARAM wParam,
                com.sun.jna.platform.win32.WinDef.LPARAM lParam
        ) {
            switch (uMsg) {
                case WinUser.WM_DESTROY:
                    User32.INSTANCE.PostQuitMessage(0);
                    return new WinDef.LRESULT(0);
                default:
                    return User32.INSTANCE.DefWindowProc(hwnd, uMsg, wParam, lParam);
            }
        }
    }
}