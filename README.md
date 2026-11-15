# 綿重商店 Web注文フロント

Kotlin Multiplatform、Compose Multiplatform、Ktor Client による顧客向けアプリ。初期設定はデモ店舗です。ブラウザー内で注文の流れを試せます。

## 起動と検証

JDK 21、Gradle 8.12.1、WasmGC 対応ブラウザーを使います。

```sh
gradle wasmJsBrowserDevelopmentRun
gradle jvmTest wasmJsBrowserDistribution
python3 tests/contracts.py
```

初回の Web ビルドで Noto Sans JP を指定リビジョンから取得し、SHA-256 を照合します。フォントと SIL Open Font License を配布物に同梱するため、実行時は外部フォントサービスへの接続は不要です。

配布物は `build/dist/wasmJs/productionExecutable/`。静的 HTTP サーバーから配信し、Wasm の MIME は `application/wasm` に設定します。

## 操作

ホーム → 商品一覧・検索 → SKU 詳細 → カート → 配送先 → 見積もり確認 → 注文完了。注文履歴から現在のカートへ再追加できます。販売終了した SKU は除外し、現在の商品と価格で改めて見積もります。マイページでは既定の配送先・言語・テーマを変更できます。狭い画面は下部5タブ、広い画面は上部ナビゲーション・商品グリッドを使います。

## 設定とデータ

配布物の `config.json` を差し替えます。既存のカタログ専用設定でも起動できます。

| キー | 役割 |
| --- | --- |
| appEntry | 配布する JS エントリ。対応する Wasm と付属リソースを同じビルドから配置 |
| storeId / storeName / notices | 店舗識別子、表示名、ホームのお知らせ。表示名・お知らせは言語別 |
| catalogUrl | 商品一覧の GET 先。価格はこの版では通貨の主単位の整数 |
| orderApi | 同一オリジンの注文 API プレフィックス。未指定ならタブ内デモ |
| loginUrl | サーバーが提供する同一オリジンのログイン開始 URL |
| defaultLocale | 未対応のブラウザー言語や商品翻訳に対する既定言語 |

`catalogUrl` と `orderApi` は独立しています。実店舗の例はそれぞれ `/api/catalog`、`/api/customer`。会員・取引先ごとの価格を返す場合、カタログも同一オリジンでセッションを参照してください。認証済みページや価格は共有キャッシュの対象外にします。公開設定には秘密鍵を置きません。

## 設計と保存範囲

[AgriOS との対応、Gradle モジュールの依存、別プロジェクトからの利用方法](docs/architecture.md)。

| 責務 | 実装 |
| --- | --- |
| Domain / commonMain | 配送先、SKU と数量の照合、見積もり・注文行のスナップショット。Compose・HTTP に依存しない |
| Application / commonMain | Checkout が見積もり要求・確認・再試行を担当。OrderGateway と CheckoutJournal が外部処理との境界 |
| Infrastructure / wasmJsMain | ServerOrders が REST を実行。PreviewOrders は同じ契約でタブ内の注文を扱う。TabJournal が未確定の見積もりと冪等性キーを復元 |
| UI / wasmJsMain | 商品・カート・配送先・履歴画面がユーザー操作を受け取り、アプリケーション処理を呼び出す。店舗の Material テーマを shared/ui に配置 |

見積もりの入力は SKU・数量・配送先です。サーバーが返した商品行・税・送料・合計を表示し、確定時は見積もり ID と同じ注文キーだけを送ります。応答を失った場合は、画面を再読み込みしても同じキーで再確認できます。サーバーが期限切れを明示した場合は再見積もりに戻ります。

言語・テーマ・SKU 数量は localStorage に保存します。配送先を含む未確定の注文は sessionStorage に置き、完了・確認取り消し時に消去します。デモの既定配送先と履歴もタブ内に限定します。本番の注文履歴は API から読みます。

画面翻訳は [ja.json](src/wasmJsMain/resources/locales/ja.json) と [en.json](src/wasmJsMain/resources/locales/en.json)。保存済み選択 → 端末言語 → 設定の既定値で決定。商品翻訳は選択言語 → 店舗既定言語 → 利用可能な翻訳にフォールバックします。

## サーバーとの契約

[OpenAPI](docs/customer-api.json) と [カタログ例](src/wasmJsMain/resources/demo/catalog.json) を参照。OpenAPI のパスは orderApi 基準です。ログイン開始 URL はサーバーで Supabase Auth と連携し、同一オリジンの HttpOnly セッションを確立する想定です。ログイン画面・JWT 検証・アクセス制御・冪等性の永続化はサーバーが担当します。

このリポジトリの対象は顧客向け Web フロントです。店舗管理画面、決済、本番サーバー、Android・iOS クライアントは別の実装範囲です。
