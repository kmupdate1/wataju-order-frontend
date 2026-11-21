# Foundation integration

## Upstream and compatibility

Uses BlueBikeBase-Inc/kmp-application-foundation commit `36bd2c7f0b87528fb867db94dd70ca18f96ff717` from `feature/core-lifecycle`.
The 41 upstream Kotlin files in `vendor/kmp-application-foundation` are **unmodified**. `source-lock.json` records their origin and SHA-256. Build adapters live in `foundation-core`, `foundation-localization`, and `foundation-ui`; no moving branch is downloaded during a build.

Upstream currently declares Kotlin 2.4.21 / Compose 1.12.0 / Android SDK 37. The Web app retains its tested Kotlin 2.1.21 / Compose 1.8.2 toolchain while compiling the compatible common sources. This is a source snapshot integration, not a published Maven dependency or a full upstream composite build. A future released artifact can replace the three adapters.

| API | Integration |
| --- | --- |
| Application, Feature, FeatureKey, registry | Upstream UiApplication installs UI / Localization and owns HTTP cleanup |
| LifecycleController | Browser create/start/stop/destroy transitions; visibility and bfcache handlers |
| Renderable, View, ViewLayer | Common contracts used by all store screens and adaptive containers |
| MaterialThemeLayer, SurfaceLayer | Wataju palettes and Japanese typography injected through configuration |
| Localization, LocalizationLayer | Initial language service and per-session layer for en/ja switching |
| ViewId | Excluded: unused UUID v7 API requires a newer Kotlin standard library |
| BindLifecycle, platform ApplicationMain | Browser-specific adapter handles visibility, bfcache, listeners and disposal |
| HTTP feature | Deferred: upstream common configuration imports JVM Ktor server types; existing Ktor Client remains behind store ports |
| web Gradle plugin | Deferred: existing configurable bootstrap and appEntry contract retained |

The foundation repository itself is not modified. Its singleton UiApplication supports one install per browser realm. `WasmJsMain.boot()` rejects a second boot. Visibility changes stop/start participants without destroying the HTTP client. Composition disposal removes listeners and destroys the controller, closing the client. Page termination also releases browser resources; bfcache remains resumable.

## UI ownership

`main()` delegates to `WasmJsMain : Bootable`. The foundation UI feature retains `StoreApplicationView`. This root loads configuration, font, messages, and catalog in a cancellable effect. It retains one `StorefrontShell` per service session; changing appearance does not reinstall features or recreate the checkout controller. Language selection creates a new localized catalog/service session and restores the stored basket/pending checkout.

The shell owns navigation and asynchronous commands. `HomeScreenView`, `CatalogueView`, `ProductDetailScreenView`, `BasketView`, `DeliveryScreenView`, `ReviewScreenView`, `CompleteScreenView`, and `OrderHistoryView` describe their UI with `onRender()`. Leaf views are immutable descriptions of the current inputs; they do not start work in constructors. Compose effects own image loading. State-bearing roots are retained with `remember`; callbacks receive current snapshots. Material components retain focus, keyboard and disabled semantics.

`ExecuteButtonView` supplies common execution/disabled behavior and Material appearance. `AddToCartButtonView` / `ConfirmOrderButtonView` supply business intent. Prices, validation, order snapshots and retry identity stay in `order-application`; foundation never imports Wataju types.

## Localization and deployment

The single editable source of 48 messages is `src/commonMain/composeResources/values/strings.xml` (English) and `values-ja/strings.xml` (Japanese). Compose Resources compiles these resources. `prepareStoreLocales` additionally derives the Web JSON dictionaries from the same XML: Compose 1.8.2 does not expose a public explicit-locale ResourceEnvironment constructor, while this app must support saved language preferences independent of navigator.language. No translated string is maintained twice. When upgrading Compose, replace this compatibility adapter with the explicit resource environment API.

Product names/descriptions and store notices remain external data. `config.json` continues to select appEntry, catalogUrl and orderApi. No server business data is baked into foundation.

## Upgrade and validation

1. Review an upstream commit in a clean checkout.
2. Run `python3 tools/update-foundation.py /path/to/foundation <full-sha>`.
3. Review the source diff and excluded APIs/build adapters. Do not silently skip new incompatible files.
4. Run `python3 tests/foundation.py`, `python3 tests/contracts.py`, and `./gradlew jvmTest wasmJsBrowserDistribution`.
5. Run browser checks against that distribution, then commit snapshot, adapters and lock together.

Core integration tests exercise feature identity, duplicate installation, lifecycle event ordering, idempotent stop/destroy, resume and failed teardown. Existing order tests cover persisted retry keys and immutable snapshots. Browser tests cover en/ja startup, Japanese font delivery and mobile navigation. CI records distributable assets and screenshots.
