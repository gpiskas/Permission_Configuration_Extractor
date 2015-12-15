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

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionGroupInfo;
import android.content.pm.PermissionInfo;
import android.os.Build;
import android.util.Log;

import com.gpiskas.confextractor.exceptions.GroupNotFoundException;
import com.gpiskas.confextractor.exceptions.PermInfoNotFoundException;
import com.gpiskas.confextractor.exceptions.PkgInfoNotFoundException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Provides a collection of methods that are used to extract the permission configuration of any
 * installed package.
 */
public class ConfExtractor {

    private static ConfExtractor instance;
    private static PackageManager pm;

    /**
     * Only used by the instance method.
     */
    private ConfExtractor(PackageManager pm) {
        ConfExtractor.pm = pm;
    }

    /**
     * Gets the singleton instance of the class.
     */
    public static ConfExtractor instance(PackageManager pm) {
        if (instance == null) {
            instance = new ConfExtractor(pm);
        } else {
            ConfExtractor.pm = pm;
        }
        return instance;
    }

    //
    //
    // Helper methods:
    //
    //

    /**
     * Execute shell command.
     */
    public static String execCommand(String cmd) {
        try {
            String output = "";
            BufferedReader reader = new BufferedReader(new InputStreamReader(Runtime.getRuntime().exec(cmd).getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                output += line + "\n";
            }
            if (output.equals("")) {
                reader.close();
                reader = new BufferedReader(new InputStreamReader(Runtime.getRuntime().exec(cmd).getErrorStream()));
                while ((line = reader.readLine()) != null) {
                    output += line + "\n";
                }
            }
            reader.close();
            return output;
        } catch (IOException e) {
            e.printStackTrace();
            return cmd + " failed.";
        }
    }

    /**
     * Check whether a particular package has been granted a particular permission.
     */
    public boolean isGranted(String perm, String pkg) {
        return pm.checkPermission(perm, pkg) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Checks whether a particular permissions has been revoked for a package by policy.
     */
    public boolean isRevokedByPolicy(String perm, String pkg) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return pm.isPermissionRevokedByPolicy(perm, pkg);
        } else {
            return false;
        }
    }

    /**
     * Retrieve all of the information we know about a particular permission.
     */
    public PermissionInfo getPermInfo(String perm) throws PermInfoNotFoundException {
        try {
            return pm.getPermissionInfo(perm, PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            throw new PermInfoNotFoundException(perm);
        }
    }

    /**
     * Retrieve overall information about an application package that is installed on the system.
     */
    public PackageInfo getPkgInfo(String pkg) throws PkgInfoNotFoundException {
        try {
            return pm.getPackageInfo(pkg, PackageManager.GET_PERMISSIONS);
        } catch (PackageManager.NameNotFoundException e) {
            throw new PkgInfoNotFoundException(pkg);
        }
    }

    /**
     * Retrieve all of the known permission groups in the system.
     */
    public List<PermissionGroupInfo> getPermGroups() {
        return pm.getAllPermissionGroups(PackageManager.GET_META_DATA);
    }

    /**
     * Query for all of the permissions associated with a particular group.
     */
    public List<PermissionInfo> getPermsInGroup(String group) throws GroupNotFoundException {
        try {
            return pm.queryPermissionsByGroup(group, PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            throw new GroupNotFoundException(group);
        }
    }

    /**
     * Return a List of all packages that are installed on the device.
     */
    public List<PackageInfo> getInstalledPkgs() {
        return pm.getInstalledPackages(PackageManager.GET_PERMISSIONS);
    }

    //
    //
    // Useful methods:
    //
    //

    /**
     * Return a List of all installed packages that are currently holding any of the given permissions.
     */
    public List<PackageInfo> getPkgsHoldingPerm(String perm) {
        return pm.getPackagesHoldingPermissions(new String[]{perm}, PackageManager.GET_PERMISSIONS);
    }

    /**
     * Output the given permissions in the required manifest format.
     * Returns a single line string. Use IDE's autoformat feature.
     */
    public void outputManifestFormat(List<PermissionInfo> perms) {
        StringBuilder sb = new StringBuilder();
        for (PermissionInfo pi : perms) {
            sb.append("<uses-permission android:name=\"");
            sb.append(pi.name);
            sb.append("\" />");
        }
        System.out.println(sb.toString());
    }

    /**
     * Output the given permissions as a string array.
     */
    public void outputAsStringArray(List<PermissionInfo> perms) {
        StringBuilder sb = new StringBuilder();
        sb.append("String[] perms = new String[]{");
        for (PermissionInfo pi : perms) {
            sb.append("\"");
            sb.append(pi.name);
            sb.append("\"");
            sb.append(","); // Delimiter.
        }
        sb.append("};");
        System.out.println(sb.toString());
    }

    /**
     * Output the given permissions to the logcat.
     */
    public void outputLog(List<PermissionInfo> perms) {
        for (PermissionInfo pi : perms) {
            Log.i("ConfExtractor", pi.name);
        }
    }

    /**
     * Converts List<PermissionInfoWrapper> to List<PermissionInfo>.
     */
    public List<PermissionInfo> unwrap(List<PermissionInfoWrapper> perms) {
        List<PermissionInfo> lst = new ArrayList<>();
        for (PermissionInfoWrapper pi : perms) {
            lst.add(pi.getPermissionInfo());
        }
        return lst;
    }

    /**
     * Retrieve the permissions that are defined by the specified package.
     */
    public List<PermissionInfo> getPermsDefined(String pkg) {
        try {
            PackageInfo pi = getPkgInfo(pkg);
            if (pi.permissions != null) {
                return Arrays.asList(pi.permissions);
            }
        } catch (PkgInfoNotFoundException e) {
            Log.v("PkgInfoNotFound", e.getLocalizedMessage());
        }
        return Collections.emptyList();
    }

    /**
     * Retrieve the permission definitions per package, for all packages.
     */
    public Map<PackageInfo, List<PermissionInfo>> getGlobalPermsDefined() {
        Map<PackageInfo, List<PermissionInfo>> map = new HashMap<>();
        for (PackageInfo pi : getInstalledPkgs()) {
            if (pi.permissions != null) {
                map.put(pi, Arrays.asList(pi.permissions));
            }
        }
        return map;
    }

    /**
     * Retrieve the permissions that are used by the specified package.
     */
    public List<PermissionInfoWrapper> getPermsUsed(String pkg) {
        try {
            PackageInfo pi = getPkgInfo(pkg);
            if (pi.requestedPermissions != null) {
                List<PermissionInfoWrapper> lst = new ArrayList<>();
                for (int i = 0; i < pi.requestedPermissions.length; i++) {
                    try {
                        lst.add(new PermissionInfoWrapper(
                                getPermInfo(pi.requestedPermissions[i]),
                                pi.requestedPermissionsFlags[i]));
                    } catch (PermInfoNotFoundException e) {
                        Log.v("PermInfoNotFound", e.getLocalizedMessage());
                    }
                }
                return lst;
            }
        } catch (PkgInfoNotFoundException e) {
            Log.v("PkgInfoNotFound", e.getLocalizedMessage());
        }
        return Collections.emptyList();
    }

    /**
     * Retrieve the permissions used per package, for all packages.
     */
    public Map<PackageInfo, List<PermissionInfoWrapper>> getGlobalPermsUsed() {
        Map<PackageInfo, List<PermissionInfoWrapper>> map = new HashMap<>();
        for (PackageInfo pi : getInstalledPkgs()) {
            if (pi.requestedPermissions != null) {
                List<PermissionInfoWrapper> lst = new ArrayList<>();
                for (int i = 0; i < pi.requestedPermissions.length; i++) {
                    try {
                        PermissionInfo info = getPermInfo(pi.requestedPermissions[i]);
                        lst.add(new PermissionInfoWrapper(info, pi.requestedPermissionsFlags[i]));
                    } catch (PermInfoNotFoundException e) {
                        Log.v("PermInfoNotFound", e.getLocalizedMessage());
                    }
                }
                map.put(pi, lst);
            }
        }
        return map;
    }

    /**
     * Retrieve the permissions held by all packages, grouped by permission groups.
     * Permissions that do not belong to a group will not be included.
     */
    public Map<PermissionGroupInfo, Map<PermissionInfo, List<PackageInfo>>> getPermHoldersPerGroup() {
        Map<PermissionGroupInfo, Map<PermissionInfo, List<PackageInfo>>> map = new HashMap<>();
        for (PermissionGroupInfo group : getPermGroups()) {
            Map<PermissionInfo, List<PackageInfo>> permMap = new HashMap<>();
            try {
                for (PermissionInfo perm : getPermsInGroup(group.name)) {
                    permMap.put(perm, getPkgsHoldingPerm(perm.name));
                }
            } catch (GroupNotFoundException e) {
                Log.v("GroupNotFound", e.getLocalizedMessage());
            }
            map.put(group, permMap);
        }
        return map;
    }

    /**
     * Retrieve all permissions.
     */
    public List<PermissionInfo> getAllPerms() {
        return getPermsDefined("android");
    }

    /**
     * Retrieve the permissions that have a normal protection level.
     */
    public List<PermissionInfo> getNormalPerms() {
        List<PermissionInfo> lst = new ArrayList<>();
        for (PermissionInfo pi : getAllPerms()) {
            if (pi.protectionLevel == PermissionInfo.PROTECTION_NORMAL) {
                lst.add(pi);
            }
        }
        return lst;
    }

}