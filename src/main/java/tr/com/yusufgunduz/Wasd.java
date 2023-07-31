//package tr.com.yusufgunduz;
//
//import com.sun.jna.Native;
//import com.sun.jna.WString;
//import com.sun.jna.platform.win32.Kernel32;
//import com.sun.jna.platform.win32.User32;
//import com.sun.jna.platform.win32.WinDef;
//import com.sun.jna.platform.win32.WinUser;
//
//import static com.sun.jna.platform.win32.WinUser.WS_CHILD;
//import static com.sun.jna.platform.win32.WinUser.WS_VISIBLE;
//
//interface Avicap32 extends com.sun.jna.Library {
//    Avicap32 INSTANCE = Native.load("avicap32", Avicap32.class);
//
//    WinDef.HWND capCreateCaptureWindowW(
//            WString windowName,
//            WinDef.DWORD dwStyle,
//            int x,
//            int y,
//            int nWidth,
//            int nHeight,
//            WinDef.HWND hwndParent,
//            int nID
//    );
//}
//
//public class Wasd {
//
//    public static void main(String[] args) {
//
//
//        // Register the window class before creating the capture window
//        WinUser.WNDCLASSEX wndClass = new WinUser.WNDCLASSEX();
//        wndClass.lpfnWndProc = null;
//        wndClass.hInstance = Kernel32.INSTANCE.GetModuleHandle(null);
//        wndClass.lpszClassName = "MyCaptureWindowClass";
//        User32.INSTANCE.RegisterClassEx(wndClass);
//
//
//        Avicap32 avicap32 = Avicap32.INSTANCE;
//        WinDef.HWND hWnd = avicap32.capCreateCaptureWindowW(
//                new WString("Capture Window"),
//                new WinDef.DWORD(WS_VISIBLE | WS_CHILD),
//                0,
//                0,
//                640,
//                480,
//                wndClass,
//                0
//        );
//
//        if (hWnd != null) {
//            // Handle successful creation of the capture window
//            System.out.println("Capture window created successfully.");
//        } else {
//            int errorCode = Kernel32.INSTANCE.GetLastError();
//            System.out.println("Error creating capture window. Error code: " + errorCode);
//        }
//    }
//}
//
//
