# ProGuard rules for MyRecipeLab
# Keep Room entity classes so reflection-based access works in release builds
-keep class com.myrecipelab.data.db.entities.** { *; }
