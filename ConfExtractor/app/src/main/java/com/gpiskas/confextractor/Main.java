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


import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Demonstration and Testing activity.
 */
public class Main extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    /**
     * Settings button handler.
     */
    public void settings(View view) {
        startActivity(new Intent(Settings.ACTION_APPLICATION_SETTINGS));
    }

    /**
     * Example test that outputs the permission configuration of all installed apps.
     */
    public void test(View view) {
        ConfExtractor ce = ConfExtractor.instance(getPackageManager());

        for (Map.Entry<PackageInfo, List<PermissionInfoWrapper>> entry : ce.getGlobalPermsUsed().entrySet()) {
            Log.i("PKG", entry.getKey().packageName);
            Collections.sort(entry.getValue());
            for (PermissionInfoWrapper piw : entry.getValue()) {
                Log.i("____PERM", piw.toString());
            }
            Log.i("---", "---------------------------------------------------");
        }
    }
}
