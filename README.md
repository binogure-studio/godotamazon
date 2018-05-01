# godot-android

[![Platform](https://img.shields.io/badge/Platform-Android-green.svg?longCache=true&style=flat-square)](https://github.com/xsellier/godotamazon)
[![Godot Engine](https://img.shields.io/badge/GodotEngine-2.1-orange.svg?longCache=true&style=flat-square)](https://github.com/godotengine/godot)
[![LICENCE](https://img.shields.io/badge/License-MIT-green.svg?longCache=true&style=flat-square)](https://github.com/xsellier/godotamazon/blob/master/LICENSE)

Android amazon services for Godot Engine 2.1, including:

* Amazon In App purchases
   * Query SKU details
   * Purchase
* Amazon Game Circle
   * Authentication
   * Whispersync (Cloud save)
   * Achievements
   * Leaderboard

# Usage

## Setup

### Amazon In App Purchase

Open `/res/values/ids.xml` and update the following:

```xml
<?xml version="1.0" encoding="utf-8"?>

<resources>
	<string name="amazon_application_id">org.binogurestudio.sneakin</string>
</resources>
```

### Amazon Game Circle

Download the `api_key.txt` from the amazon's dashboard and put it in your game's directory (same level as `engine.cfg`)

## Compilation

[Prerequisites documentation](http://docs.godotengine.org/en/2.1/development/compiling/compiling_for_android.html)

```sh
export CXX=g++
export CC=gcc

# This one is optional
export SCRIPT_AES256_ENCRYPTION_KEY=YOUR_ENCRYPTION_KEY

# Place where the NDK/SDK are
export ANDROID_HOME=/usr/lib/android-sdk
export ANDROID_NDK_ROOT=/usr/lib/android-sdk/ndk-bundle

# Godot engine source directory
cd ./godot

scons -j2 CXX=$CXX CC=$CC platform=android tools=no target=debug
scons -j2 CXX=$CXX CC=$CC platform=android tools=no target=debug android_arch=x86
scons -j2 CXX=$CXX CC=$CC platform=android tools=no target=release
scons -j2 CXX=$CXX CC=$CC platform=android tools=no target=release android_arch=x86

cd platform/android/java
./gradlew clean
./gradlew build
cd -
```

## Edit Godot engine settings

### Using compiled templates

`Export` > `Android` > `Custom Package`, change fields `Debug` and `Release` to use the compiled android's templates (`bin` directory).

#### Loading the module

Edit `engine.cfg` and add an `android` part as following:

```ini
[android]
modules="org/godotengine/godot/GodotAmazon"
```

#### Initializing the module using GDScript

Here is an example

```python
extends Node

onready var godot_amazon = Globals.get_singleton('GodotAamazon')

func _ready():
  if OS.get_name() == 'Android' and godot_amazon != null:
    godot_amazon.amazon_initialize(get_instance_ID())
  else:
    godot_amazon = null

func amazon_auth_connected(user):
  print('User %s has logged in' % [user])

func amazon_auth_connect_failed(message):
  print('Login failed %s' % [ message ])

func amazon_connect():
  if godot_amazon != null:
    godot_amazon.amazon_connect()
```

# API

## Functions

TODO

## Callbacks

TODO

# License

[See LICENSE file](./LICENSE)
