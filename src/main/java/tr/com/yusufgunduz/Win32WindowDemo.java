package tr.com.yusufgunduz;

/* Copyright (c) 2012 Tobias Wolf, All Rights Reserved
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 */

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;

import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.util.Set;

// TODO: Auto-generated Javadoc

/**
 * The Class Win32WindowTest.
 */
public class Win32WindowDemo {

    public static void main(String[] args) {
        Set<ObjectName> objectNames = ManagementFactory.getPlatformMBeanServer().queryNames(null, null);
        for (ObjectName objectName : objectNames) {
            String className = objectName.toString();
            if (className.contains("VideoInputDevice")) {
                System.out.println("Webcam: " + className);
            }
        }

        listCamerasUsingUser32EnumDisplayDevices();
    }

    private static void listCamerasUsingUser32EnumDisplayDevices() {
        final WinUser.WNDENUMPROC lpEnumFunc = (hwnd, lParam) -> {
            char[] className = new char[512];
            User32.INSTANCE.GetClassName(hwnd, className, className.length);
            String windowClass = Native.toString(className);
            System.out.println("windowClass:"+windowClass);
            if (windowClass.contains("#32770")) {
                char[] windowTitle = new char[512];
                User32.INSTANCE.GetWindowText(hwnd, windowTitle, windowTitle.length);
                String title = Native.toString(windowTitle).trim();
                if (title.contains("Camera")) {
                    System.out.println("Camera Window: " + title);
                }
            }
            return true;
        };

        User32.INSTANCE.EnumWindows(lpEnumFunc, Pointer.createConstant(0));
    }

    public interface User32 extends com.sun.jna.platform.win32.User32 {
        User32 INSTANCE = Native.load("user32", User32.class);

        int GetWindowText(HWND hWnd, char[] lpString, int nMaxCount);
    }
}
