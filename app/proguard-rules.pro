# TODO (davidburstrom): At 2017-03-08, it took 70 seconds for pass 5 to get rid of 5.5k APK.
# The relevant tuning needs to be investigated with Marvin and Perf
-optimizationpasses 4
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontpreverify
-repackageclasses
#-dontobfuscate

# This is specified by the AndroidProGuardTask
# -printusage

# Ensure we don't dump 130+ MB of data. The -dump is already specified by AndroidProGuardTask so we have to override the output path instead.
-dump /dev/null

-optimizations !class/merging/*
-allowaccessmodification

-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable

-include proguard-generic-dontwarn.cfg
-include proguard-generic-keep.cfg
