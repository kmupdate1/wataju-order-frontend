# B3 共通部品と綿重注文の境界

設計の参照元は AgriOS の [feature/cloud-ready-dashboard、a30e2c3](https://github.com/kmupdate1/agrios_ui_of_kmp/tree/a30e2c3addb1f426a45ee37425f4bc693e26cb1f)。ブランチ先端のコミット日時で比較して選択した。

| AgriOS で参考にした箇所 | 綿重での実装 |
| --- | --- |
| application の契約と ui-system の実装を分離 | :order-application にモデル・注文ポート・ユースケースを置く。HTTP、ブラウザー保存、Compose はアプリ側 |
| DashboardController / DashboardUiState | OrderController が確認前・受付結果待ち・完了を遷移する。UI は状態を描画し、再送・編集可否を反映 |
| ContainerView と ContentView の区別 | :b3-ui の AdaptiveNavigation・AdaptiveGrid が配置を担当。画面名・操作・商品カード・ブランド色は呼び出し側が指定 |
| PlatformLocale と言語タグの解決 | :b3-foundation の LocalePolicy が候補照合・優先順位を担当。ブラウザー言語取得と対応言語リストは製品側 |

## ビルド時の依存

Web アプリ → :order-application、:b3-foundation、:b3-ui。共通モジュールは綿重・AgriOS のモデルを参照しない。:order-application は Kotlin と serialization のみで JVM / Wasm にコンパイルする。Web の StoreServices が REST / デモの実装、カート保存先、注文コントローラーを組み立てる。

注文を送信する直前にキーを保存する。通信が途切れた場合は同じキーで受付を再確認し、この間は編集をロックする。サーバーが見積もり失効を明示した場合はキーと見積もりを破棄し、配送先画面へ戻る。既存のブラウザー保存キーと API 契約は継続利用する。

## 別の KMP プロジェクトから参照する

綿重のリポジトリをバージョン固定で隣にチェックアウトし、利用側の settings.gradle.kts に必要なモジュールを登録する。利用側は Kotlin Multiplatform、Compose Compiler、Compose Multiplatform プラグインのバージョンを供給する。

```kotlin
include(":b3-foundation", ":b3-ui")
project(":b3-foundation").projectDir = file("../wataju-order-frontend/b3-foundation")
project(":b3-ui").projectDir = file("../wataju-order-frontend/b3-ui")
```

これらのビルド定義が提供するターゲットは JVM / Wasm。利用側の同じターゲットのソースセットに `implementation(project(":b3-foundation"))` や `implementation(project(":b3-ui"))` を追加する。Web のアプリや注文モジュールへの依存は不要。

AgriOS のように Android / iOS も同時にコンパイルする場合は、既存の KMP ライブラリの commonMain から同じソースディレクトリを参照する方法もある。どちらか一方の方法を選ぶ。

```kotlin
// AgriOS の ui-system/build.gradle.kts 内。パスは利用側のルートから解決する。
kotlin.sourceSets.commonMain {
    kotlin.srcDir(rootProject.file("../wataju-order-frontend/b3-foundation/src/commonMain/kotlin"))
    kotlin.srcDir(rootProject.file("../wataju-order-frontend/b3-ui/src/commonMain/kotlin"))
}
```

AgriOS の地域・文字体系別の対応方針（例 zh-Hans → zh-CN）はその製品の言語マッピングで指定する。農業コマンド、注文金額、商品・ゾーンのモデルはそれぞれの製品が所有する。
