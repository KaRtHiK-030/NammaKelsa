# Add project specific ProGuard rules here.
# https://developer.android.com/build/shrink-code

# Keep our Firestore data classes so deserialization works in release builds.
-keep class com.karthik.nammakelsa.Worker { *; }
-keep class com.karthik.nammakelsa.Request { *; }
-keep class com.karthik.nammakelsa.Review { *; }
-keep class com.karthik.nammakelsa.Message { *; }
-keep class com.karthik.nammakelsa.Favorite { *; }
-keep class com.karthik.nammakelsa.ChatUser { *; }
-keep class com.karthik.nammakelsa.RequestStatus { *; }

# Firebase Firestore reflective access
-keepclassmembers class * {
    @com.google.firebase.firestore.PropertyName <fields>;
    @com.google.firebase.firestore.PropertyName <methods>;
}

# Keep no-arg constructors required by Firestore POJO mapping.
-keepclasseswithmembers class * {
    public <init>();
}

# Keep Coil + Compose (default rules suffice; safety net):
-dontwarn coil.**
