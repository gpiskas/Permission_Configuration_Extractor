# Permission Configuration Extractor
An Android tool which is primarily used to extract the permission configuration of installed apps.

This repo contains the whole Android Studio project.  
Documentation is available under the doc directory.  
Example output is available in logcat.log.


The following example is the current permission configuration of the com.android.camera package.  
Information provided: status of permission, protection level, permission group and name.
```
PKG: com.android.camera
____PERM: GRNT	DNGR	              CAMERA	android.permission.CAMERA
____PERM: DENY	DNGR	            LOCATION	android.permission.ACCESS_FINE_LOCATION
____PERM: GRNT	DNGR	          MICROPHONE	android.permission.RECORD_AUDIO
____PERM: DENY	DNGR	                 SMS	android.permission.READ_SMS
____PERM: GRNT	DNGR	             STORAGE	android.permission.READ_EXTERNAL_STORAGE
____PERM: GRNT	DNGR	             STORAGE	android.permission.WRITE_EXTERNAL_STORAGE
____PERM: GRNT	NRML	           UNGROUPED	android.permission.SET_WALLPAPER
____PERM: GRNT	NRML	           UNGROUPED	android.permission.WAKE_LOCK
```