/*
*  Copyright (C) 2015 George Piskas
*
*  This program is free software; you can redistribute it and/or modify
*  it under the terms of the GNU General Public License as published by
*  the Free Software Foundation; either version 2 of the License, or
*  (at your option) any later version.
*
*  This program is distributed in the hope that it will be useful,
*  but WITHOUT ANY WARRANTY; without even the implied warranty of
*  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*  GNU General Public License for more details.
*
*  You should have received a copy of the GNU General Public License along
*  with this program; if not, write to the Free Software Foundation, Inc.,
*  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*
*  Contact: gpiskas@gmail.com
*/

package com.gpiskas.confextractor;

import android.content.pm.PermissionInfo;

/**
 * PermissionInfo wrapper that is used to encapsulate additional info along with the original object.
 */
public class PermissionInfoWrapper implements Comparable {

    public static final int REQ_FLAG_DENIED = 1;
    public static final int REQ_FLAG_GRANTED = 3;

    private PermissionInfo pi;
    private int requestFlag;
    private String groupName;

    /**
     * PermissionInfoWrapper constructor.
     * The request flag indicates if the permission was granted or not.
     */
    PermissionInfoWrapper(PermissionInfo pi, int requestFlag) {
        this.pi = pi;
        this.requestFlag = requestFlag;

        if (this.pi.group == null)
            this.pi.group = "android.permission-group.UNGROUPED";
        this.groupName = pi.group.substring(pi.group.lastIndexOf('.') + 1);
    }

    /**
     * Returns whether the permission was granted or not.
     */
    public boolean isGranted() {
        return requestFlag == REQ_FLAG_GRANTED;
    }

    /**
     * Returns the request flag.
     */
    public int getRequestFlag() {
        return requestFlag;
    }

    /**
     * Returns the wrapped PermissionInfo object.
     */
    public PermissionInfo getPermissionInfo() {
        return pi;
    }

    /**
     * Returns the protection level of the wrapped PermissionInfo object.
     */
    public String getProtectionLevel() {
        switch (pi.protectionLevel) {
            case PermissionInfo.PROTECTION_DANGEROUS:
                return "DNGR";
            case PermissionInfo.PROTECTION_NORMAL:
                return "NRML";
        }
        return String.valueOf(pi.protectionLevel);
    }

    /**
     * Used to informatively print the details of the object.
     */
    @Override
    public String toString() {
        String str = String.format("%4s\t%20s\t%s", getProtectionLevel(), groupName, pi.name);

        if (isGranted()) {
            return "GRNT\t" + str;
        } else {
            return "DENY\t" + str;
        }
    }

    /**
     * Used to sort a list of such items based on protection level, group, and name.
     */
    @Override
    public int compareTo(Object other) {
        PermissionInfoWrapper opiw = (PermissionInfoWrapper) other;

        int prot = getProtectionLevel().compareTo(opiw.getProtectionLevel());
        if (prot == 0) { // Same protection level.
            int gcmp = pi.group.compareTo(opiw.getPermissionInfo().group);
            if (gcmp == 0) // Same group.
                return pi.name.compareTo(opiw.getPermissionInfo().name);
            return gcmp;
        }
        return prot;
    }
}
