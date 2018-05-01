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

|name|parameters|return|description|
|---|---|---|---|
|`amazon_initialize`|`int instance_id`|`void`|Initialize. Amazon callbacks will be done using the instance_id. |
|`amazon_connect`||`void`|Connect to amazon game circle|
|`amazon_disconnect`||`void`|Disconnect from amazon game circle|
|`amazon_is_connected`||`boolean`|Return `true` if connected, `false` otherwise|
|`amazon_leaderboard_submit`|`String id, int score`|`void`|Submit a score to the given leaderboard|
|`get_amazon_leaderboard_timespan`||`Dictionary`|Return the values of `LeaderboardFilter`|
|`amazon_leaderboard_load_player_score`|`String id, [int timespan = TIME_SPAN_ALL_TIME]`|`void`|Load the score of the leaderboard id for the current user|
|`amazon_leaderboard_load_top_scores`|`String id, [int timespan = TIME_SPAN_ALL_TIME, int max_results = 10, boolean force_reload = false`|`void`|Load top scores of the leaderboard id|
|`amazon_leaderboard_load_player_centered_scores`|`String id, [int timespan = TIME_SPAN_ALL_TIME, int max_results = 10, boolean force_reload = false`|`void`|Load scores centered of the current user of the leaderboard id|
|`amazon_leaderboard_show`|`String id`|`void`|Show the given leaderboard|
|`amazon_leaderboard_showlist`||`void`|Show the leaderboards' list|
|`amazon_snapshot_load`|`String name, int conflictResolutionPolicy`|`void`|Load the given snapshot. `conflictResolutionPolicy` is not used.|
|`amazon_snapshot_save`|`String name, String data, String description, boolean force`|`void`|Save a given snapshot. Use the `force` to overwrite a conflicting savegame|
|`amazon_achievement_unlock`|`String id`|`void`|Unlock the given achievement|
|`amazon_achievement_increment`|`String id, float percent`|`void`|Increment by `percent` the given achievement [0, 100]|
|`amazon_achievement_show_list`||`void`|Show the achievement list|
|`amazon_get_user_details`||`String`|Return the current firebase user. Need to `parse_json` in order to exploit it.|

## Callbacks

|name|parameters|description|
|---|---|---|
|`amazon_auth_connected`|`String username`|Called once connected to amazon game circle. username might be empty (not null)|
|`amazon_auth_disconnected`||Called once disconnected|
|`amazon_auth_connect_failed`|`String message`|Called when connection has failed. `message` is the reason of the failure|
|`amazon_achievement_unlocked`|`String id`|Called once the achievement has been unlocked|
|`amazon_achievement_unlock_failed`|`String message`|Called if the achievement unlocking has failed|
|`amazon_achievement_increased`|`String id, int amount`|Called once the achivement has been increased|
|`amazon_achievement_increment_failed`|`String message`|Called if the achievement increment has failed|
|`amazon_leaderboard_submitted`|`String id, int score`|Called once the leaderboard hs been updated|
|`amazon_leaderboard_submit_failed`|`String message`|Called if the leaderboard has not been updated|
|`amazon_leaderboard_load_top_scores_failed`|`String message`|Called if the top leaderboard has not been loaded|
|`amazon_leaderboard_loaded_top_score`|`String scores`|Return the users' scores (Dictionary: rank:score, rank:name, rank:photo_path)|
|`amazon_leaderboard_load_centered_scores_failed`|`String message`|Called if the centered leaderboard has not been loaded|
|`amazon_leaderboard_loaded_centered_scores`|`String scores`|Return the users' scores (Dictionary: rank:score, rank:name, rank:photo_path)|
|`amazon_leaderboard_load_score_failed`|`String message`|Called if the user's score has not been loaded|
|`amazon_leaderboard_loaded_score`|`int score, int rank`|Return the user's score and rank|
|`amazon_leaderboard_showd`|`String id`|Called once the leaderboard has been showd|
|`amazon_leaderboard_show_failed`|`String message`|Called if there is an issue when trying to show the leaderboard|
|`amazon_leaderboard_showlisted`||Called once the leaderboards have been listed|
|`amazon_leaderboard_showlist_failed`|`String message`|Call if it failed to show the leaderboards' list|
|`amazon_snapshot_loaded`|`String data`|Called once the snapshot has been loaded|
|`amazon_snapshot_load_failed`|`String message`|Called if it failed to load the snapshot|
|`amazon_snapshot_saved`||Called once the snapshot has been saved|
|`amazon_snapshot_save_failed`|`String message`|Called if it failed to save the snapshot|

# License

[See LICENSE file](./LICENSE)
