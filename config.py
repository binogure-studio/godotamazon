
def can_build(plat):
    return (plat == "android")

def configure(env):
    if env["platform"] == "android":
        # Amazon dependencies
        env.android_add_dependency("compile fileTree(dir: '../../../modules/godotamazon/android/lib/', include: ['*.jar'])")
        env.android_add_java_dir("android/src")

        env.android_add_res_dir("res")
        env.android_add_to_manifest("android/AndroidManifestChunk.xml")
        env.android_add_to_permissions("android/AndroidPermissionsChunk.xml")

        env.disable_module()
