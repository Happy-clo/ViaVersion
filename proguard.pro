# 指定压缩级别
-optimizationpasses 5
# 不跳过非公共的库的类成员
-dontskipnonpubliclibraryclassmembers
# 混淆时采用的算法
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*
# 保持行号
-keepattributes SourceFile,LineNumberTable
# 保持泛型
-keepattributes Signature
# 保持注解
-keepattributes *Annotation*,InnerClasses
