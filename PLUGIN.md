# プラグイン

Kanban Broのプラグインは、Kanban Broで表示しているページの動作を拡張するための仕組みです。

## プラグインの記法

プラグインは以下の3パートに分かれます。

- マジックコメント
- メタデータ部
- 本文

### マジックコメント

マジックコメントは、このスクリプトがKanban Broのプラグインであることを示すための以下の文字列です。

```javascript
// @KanbanBroPlugin
```

### メタデータ部

メタデータ部は、プラグインの情報であるメタデータを記述した文字列です。

メタデータ部はJsonコードが `// ` によってコメントアウトされたものです。

---

例:

```javascript
// {
//   "name": "com.example.example_plugin",
//   "title": "Example Plugin",
//   "version": "1.0.0",
//   "description": "This is <b>an example</b> plugin."
// }
```

### 本文

本文は、プラグインの処理を記述するJavaScriptコードです。

メタデータ部との競合を回避するために、本文の先頭は行コメントであってはなりません。

本文の内容は概ね自由ですが、多くの場合、特定のページ上でのみ動作するための判定が含まれます。

---

例:

```javascript
if (location.href === 'https://example.com/') {
  console.log('This is example.com');
}
```

### 例

以下は完全なプラグインの例です。

```javascript
// @KanbanBroPlugin
// {
//   "name": "com.example.example_plugin",
//   "title": "Example Plugin",
//   "version": "1.0.0",
//   "description": "This is <b>an example</b> plugin."
// }

if (location.href === 'https://example.com/') {
  console.log('This is example.com');
}
```

## ファイル名

プラグインのファイル名は `.kbb.js` という拡張子である必要があります。

より正確にいうと、プラグインがダウンロードできるURLは `.kbb.js` で終わることが想定されます。

## メタデータ

### `name` : required string

プラグインを一意に識別するための名前です。

プラグインの名前は、Javaのパッケージ名のようにドット区切りで表現された識別子のような形式を取ることが想定されています。

識別子は、1文字以上の半角英小文字・半角数字・アンダースコア（ `/[a-z0-9_]+/` ）で構成されることが想定されます。

例: `com.example.example_plugin`

### `title` : required string

人間にとって読みやすいプラグインのタイトルです。

タイトルは英語でなくてもかまいません。

例: `Example Plugin`

### `version` : required string

プラグインのバージョンです。

バージョンは[セマンティックバージョニング](https://semver.org/)の `<version core>` である（ `/([1-9][0-9]*)\.([1-9][0-9]*)\.([1-9][0-9]*)/` ）ことが想定されます。

例: `1.0.0`

### `description` : required string

プラグインの説明です。

説明は英語でなくてもかまいません。

説明は、以下のコードによってAndroidのGUI上で解釈可能な簡易的なHTMLをサポートします。

<code>textView.<a href="https://developer.android.com/reference/android/widget/TextView#setText(int)">setText</a>(Html.<a href="https://developer.android.com/reference/android/text/Html#fromHtml(java.lang.String,%20int)">fromHtml</a>(description, Html.FROM_HTML_MODE_COMPACT))</code>

例: `This is <b>an example</b> plugin.`

## プラグインの動作

プラグインはページが完全にロードされた後にJavaScriptとして実行されます。

したがって、onloadイベントを待つ必要はありません。

---

プラグインの本文ではawaitを使うことができません。

awaitを使う場合は、以下のようにPromiseで囲う必要があります。

```
new Promise(async () => {
    // awaitを使う処理
});
```
