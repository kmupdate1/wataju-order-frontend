# B3共通基盤と綿重注文の境界

[Notionの設計決定](https://app.notion.com/p/4190d554f32d81eb9c15e8481c73fa9e)、AgriOSのクラス階層設計、および [kmp-application-foundation](https://github.com/BlueBikeBase-Inc/kmp-application-foundation) を基に構成する。foundationの固定版、互換性、更新手順は [foundation-integration.md](foundation-integration.md) に記載。

| モジュール | 責務 | 依存先 |
| --- | --- | --- |
| foundation-core | 上流のApplication、Feature、ライフサイクル | Kotlin |
| foundation-localization | 上流の言語型・Localization機能 | foundation-core |
| foundation-ui | 上流のRenderable / View / ViewLayer、UI登録、テーマ・Surface・Localization階層 | foundation-localization、Compose |
| b3-foundation | 言語タグ照合と翻訳フォールバック。上流がまだ提供しない補助ポリシー | Kotlin |
| b3-ui | 適応レイアウト、実行ボタン。綿重・AgriOSの型は参照しない | foundation-ui、Compose |
| order-application | 注文モデル、ポート、Checkout、OrderController | Kotlin、serialization |
| root Webアプリ | Web起動、画面、綿重ブランド、REST、ブラウザー保存、設定読込 | 上記の共通モジュール |

## 起動と描画

`main()`は`WasmJsMain : Bootable`へ委譲する。起動オブジェクトがfoundationの`UiApplication`に機能を登録し、`LifecycleController`を介して作成・開始する。ブラウザーの可視性とページ復帰で停止・再開し、Compositionの破棄でリスナーとHTTPクライアントを片付ける。

主要UIは`View.onRender()`を実装する型。`StoreApplicationView`は設定・翻訳・フォント・カタログを読み込み、セッションごとに`StorefrontShell`を保持する。Shellは画面遷移・通信中状態・イベントの接続を担当し、個別Screenへ現在の値とコールバックを渡す。画面・ボタンのコンストラクターでは通信しない。テーマ再描画で注文処理や機能登録を再実行しない。

`Renderable → View → ExecuteButtonView → AddToCartButtonView / ConfirmOrderButtonView`が契約・共通動作・業務上の意図の境界。Material標準部品がフォーカスや無効状態を処理し、ブランド配色とTypographyはfoundationの`MaterialThemeLayer`へ注入する。

## 注文処理と保存

`StoreServices`がREST / デモの実装、カート保存先、注文コントローラーを組み立てる。UIは価格を確定せず、サーバーから返る見積もりスナップショットを表示する。

送信前にキーを保存し、通信断では同じキーで再送する。受付結果が不明な間は編集・言語切替をロックする。見積もり失効が明示された場合は再見積もりへ戻す。既存のブラウザー保存キー・API契約を継続利用する。

設定・商品データは外部JSON/APIから注入する。翻訳XMLの所有者は注文アプリ。商品名や説明の翻訳はカタログデータの責務。UIの言語選択とサーバーへのAccept-Languageを一致させる。

## 別のKMPプロジェクトから利用する

再利用元をコミット固定で隣にチェックアウトする。既存foundationが同じパッケージを提供している場合は、その依存を使い、二重登録しない。まだ利用していない場合は次のモジュールを登録する。

```kotlin
val sharedRoot = file("../wataju-order-frontend")
listOf("foundation-core", "foundation-localization", "foundation-ui", "b3-foundation", "b3-ui").forEach { module ->
    include(":$module")
    project(":$module").projectDir = sharedRoot.resolve(module)
}
```

利用側で互換性のあるKotlin / Composeプラグインを供給し、必要なソースセットに`implementation(project(":b3-ui"))`などを追加する。現在のビルドアダプターの対象はJVM / Wasm。Android / iOSは上流のターゲット設定とツールチェーンを統合してから検証する。注文・農業それぞれのモデルや業務命令をB3共通UIへ持ち込まない。
