# Contributing

> [!NOTE]
>
> This is **not** a guide, it's merely a developer reference. This all assumes you have experience
> and knowledge with Android software development and does not spoon-feed you information.

> [!WARNING]
>
> Remember to follow [Codeberg's Terms of Use][CodebergTerms]
> and [IzzyOnDroid's policies][IzzyPolicy] when contributing.

## Rules & conventions

* Format your code properly before committing and include the copyright header. You can configure
  Android Studio to do this for you.
* In general, avoid adding dependencies, keeping the APK size low is a priority
* If you took code from somewhere else, please credit it if
  it's [legally significant][LegallySignificant]
* Avoid translating strings from PRs, do it through [Weblate][Weblate] instead
* Split big contributions into multiple PRs so I can merge them individually. There are some
  exceptions to this rule, like for big refactors
* Do not work on something if someone's already assigned to it, or ask permission before doing so
* *Please* include screenshots when changing UI, it saves me a lot of time

## Building from source

### Prerequisites

* The latest [Android Studio][AndroidStudio]
* The latest JDK, and Android SDK + NDK
	* These can all be managed from within Android Studio. Search JDK and SDK in the <kbd>
	  Ctrl</kbd> + <kbd>Shift</kbd> + <kbd>P</kbd> menu for related actions.
	* Alternatively, you can use your system's package manager to manage these tools or dedicated
	  tools like `sdkmanager`, `sdkman`, Nix, etc.

### Cloning the repository

You *need* to clone with submodules otherwise Navic will not compile:

```sh
git clone --recurse-submodules https://codeberg.org/paige/Navic.git
cd Navic
# to update submodules:
git submodule update --remote --merge --recursive
```

### Building

You can either build from the command line or from within Android Studio.

#### Gradle tasks overview

| Task                     | Description                                                         |
|--------------------------|---------------------------------------------------------------------|
| `:app:packageRelease`    | Package a release APK. For signing, see the env vars below          |
| `:app:assembleDebug`     | Package a debug APK. Equivalent to the run button in Android Studio |
| `:app:generateBuildInfo` | Generates a `BuildInfo` class, mainly used to read `FDROID` var     |

#### Environment variables overview

| Variable                 | Description                                              |
|--------------------------|----------------------------------------------------------|
| `FDROID`                 | Currently just dictates if app updates should be enabled |
| `SIGNING_STORE_FILE`     | Path to a keystore, used for signing the APK             |
| `SIGNING_STORE_PASSWORD` | Keystore's password                                      |
| `SIGNING_KEY_ALIAS`      | Which key in the keystore to sign with                   |
| `SIGNING_KEY_PASSWORD`   | Password for that key                                    |

## Adding new UI icons

Use the [rounded variant][Icons] of Material Symbols when adding new icons. Include the icon in
`paige.navic.ui.icons` and use the generated Kotlin code by selecting Android > Compose on the
Material Symbols site. You can also use the Valkyrie plugin in Android Studio to download icons from
within the IDE.

## Contributing help & discussion

Join the [Discord][Discord] and ask in `#🟢-android-development`.

[CodebergTerms]: https://codeberg.org/Codeberg/org/src/branch/main/TermsOfUse.md

[IzzyPolicy]: https://izzyondroid.org/docs/general/AppInclusionPolicy/

[LegallySignificant]: http://gnu.ist.utl.pt/prep/maintain/html_node/Legally-Significant.html

[Weblate]: https://hosted.weblate.org/projects/navic/navic/

[AndroidStudio]: https://developer.android.com/studio

[Icons]: https://fonts.google.com/icons?icon.style=Rounded&icon.set=Material+Symbols

[Discord]: https://discord.gg/TBcnNX66PH
