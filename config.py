
def can_build(plat):
    return (plat == "android")

def configure(env):
    if env["platform"] == "android":
        relative_path = "../../../modules/godotamazon/android/lib/"

        # Amazon dependencies
        env.android_add_dependency("compile files('" + relative_path + "amazonclouddrive-1.0.0.jar')")
        env.android_add_dependency("compile files('" + relative_path + "AmazonInsights-android-sdk-2.1.26.jar')")
        env.android_add_dependency("compile files('" + relative_path + "gamecirclesdk.jar')")
        env.android_add_dependency("compile files('" + relative_path + "in-app-purchasing-2.0.76.jar')")
        env.android_add_dependency("compile files('" + relative_path + "login-with-amazon-sdk.jar')")
        
        env.android_add_java_dir("android/src")

        env.android_add_res_dir("res")
        env.android_add_to_manifest("android/AndroidManifestChunk.xml")
        env.android_add_to_permissions("android/AndroidPermissionsChunk.xml")

        env.disable_module()
