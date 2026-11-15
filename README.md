# 綿重商店 注文フロントエンド

Kotlin Multiplatform / Compose Multiplatform の顧客向け Wasm Web アプリ。商品検索、カートの追加・数量変更・削除、概算小計、注文内容の確認を実装しています。初期設定では静的サーバーから架空の商品と価格を読み込みます。注文内容の確認で操作は完了し、受注は発生しません。

## 起動・ビルド

Gradle 8.12.1、JDK 17 以上と WasmGC 対応ブラウザーを使用します。

```sh
gradle wasmJsBrowserDevelopmentRun
gradle wasmJsBrowserDistribution
python3 tests/contracts.py
```

配布ディレクトリは `build/dist/wasmJs/productionExecutable/`。その内容を静的 HTTP サーバーに配置します。Wasm の MIME は `application/wasm` です。

## サーバー接続

配布ディレクトリの `config.json` を環境ごとに差し替えます。アプリの再ビルドは不要です。

```json
{
  "appEntry": "./store.js",
  "catalogUrl": "/api/v1/catalog",
  "storeName": { "ja": "綿重商店", "en": "Wataju Shoten" },
  "defaultLocale": "ja"
}
```

`appEntry` は配布する JS エントリです。JS と Wasm の関連ファイルを同じ配布構成で置いてください。

Ktor Client が `catalogUrl` に GET し、選択言語を `Accept-Language` で渡します。相対 URL はページ基準です。別オリジンの API は CORS を設定してください。レスポンス形式は [サンプル](src/wasmJsMain/resources/demo/catalog.json) を参照。SKU の ID は言語間で共通とし、単価は v0.1 では通貨の主単位の整数で返します。通信失敗時は画面から再試行できます。公開設定に秘密鍵は含めません。

## 表示言語とテーマ

画面文言は [日本語](src/wasmJsMain/resources/locales/ja.json) と [英語](src/wasmJsMain/resources/locales/en.json) に分離しました。言語は保存済み選択 → ブラウザーの言語 → 設定の既定言語の順で決定します。商品名・説明はサーバーが言語別に返し、未翻訳なら設定の既定言語などにフォールバックします。金額は通貨コードと表示言語を使い Intl.NumberFormat で整形します。

テーマは端末設定に追従し、ライト・ダークを明示選択できます。言語とテーマはブラウザーに保存します。カートはページを開いている間保持します。

## 初版の責務

共有の型・検証は commonMain、HTTP・ブラウザー設定・画面は wasmJsMain に置いています。今後の注文 API は認証・取引先権限・最終価格・税・送料・注文確定をサーバー側で扱う想定です。ログイン、注文送信・履歴、店舗管理、Android/iOS 配布は後続の実装範囲です。
