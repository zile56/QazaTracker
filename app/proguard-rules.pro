# Project-specific R8 rules. Room, Hilt, WorkManager and AndroidX ship their own
# consumer rules, so only add a rule here if a build warning or a runtime crash needs it.

# Keep line numbers so Play Console crash reports can be deobfuscated with mapping.txt.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
