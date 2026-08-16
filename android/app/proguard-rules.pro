# Rules for a hypothetical future minified release build (isMinifyEnabled = false today - see
# build.gradle.kts). Checked empirically, not assumed: a plain, unminified `assembleDebug` on AGP
# 9.3.1 runs D8 directly and emits *zero* "missing class" diagnostics for this module - the
# whole-program reachability analysis that produces those warnings is an R8 (shrinker) behavior,
# not something D8 alone performs without minification enabled. So there is nothing to react to
# today; these lines are the desktop-only APIs the shared engine references but never calls on
# Android (see RipMeApplication's bootstrap comments for why each is unreachable), pre-declared so
# they're ready the day isMinifyEnabled flips to true rather than re-derived from scratch then.
-dontwarn java.awt.**
-dontwarn javax.swing.**
-dontwarn javax.sound.**

# Only relevant if :core's log4j-core exclusion (see :app's dependencies block) is still in place
# once minification is turned on; a harmless no-op otherwise.
-dontwarn org.apache.logging.log4j.core.**
