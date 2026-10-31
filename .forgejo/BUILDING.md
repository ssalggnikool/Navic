## Building from source

Developer reference for how to build Navic from source

### Prerequisites

* The latest [Android Studio][AndroidStudio]
* The latest JDK, and Android SDK + NDK
	* You can manage all of these from within Android Studio.\
	  ![](https://files.catbox.moe/zhrvz9.png)
	* Android Studio has likely already prompted you to install the SDK and JDK. You likely still
	  have to install the NDK, though

### Cloning the repository

Clone Navic along with its submodules.

```sh
git clone --recurse-submodules https://codeberg.org/paige/Navic.git
cd Navic
# to update submodules later:
git submodule update --remote --merge --recursive
```

### Building

You can either build from the command line or from within Android
Studio (![run button](https://files.catbox.moe/gr5204.png)).

A more in depth example of building can be seen in the [CI build workflow][BuildWorkflow]

#### Gradle tasks overview

| Task                         | Description                                                                              |
|------------------------------|------------------------------------------------------------------------------------------|
| `:app:packageFossRelease`    | Package a release APK with foss flavour. For signing, see the env vars below             |
| `:app:packageNonfreeRelease` | Package a release APK with nonfree flavour. For signing, see the env vars below          |
| `:app:assembleFossDebug`     | Package a debug APK with foss flavour.                                                   |
| `:app:assembleNonfreeDebug`  | Package a debug APK with nonfree flavour. Equivalent to the run button in Android Studio |

#### Environment variables overview

| Variable                 | Description                                  |
|--------------------------|----------------------------------------------|
| `SIGNING_STORE_FILE`     | Path to a keystore, used for signing the APK |
| `SIGNING_STORE_PASSWORD` | Keystore's password                          |
| `SIGNING_KEY_ALIAS`      | Which key in the keystore to sign with       |
| `SIGNING_KEY_PASSWORD`   | Password for that key                        |

[AndroidStudio]: https://developer.android.com/studio

[BuildWorkflow]: https://codeberg.org/paige/Navic/src/branch/master/.forgejo/workflows/build.yml
