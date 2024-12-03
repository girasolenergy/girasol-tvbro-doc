# プラグイン

Kanban Broのプラグインは、Kanban Broで表示しているページの動作を拡張するための仕組みです。

プラグインはページのロード時にJavaScriptとして実行されます。

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

本文の内容は概ね自由ですが、多くの場合、特定のページ上でのみ動作するための判定が行われます。

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

## メタデータ

### `name` : required string

プラグインを一意に識別するための名前です。

プラグインの名前は、多くの場合Javaのパッケージ名のようにドット区切りで表現された識別子のような形式を取ります。

例: `com.example.example_plugin`

### `title` : required string

人間にとって読みやすいプラグインのタイトルです。

プラグインのタイトルは英語でなくてもかまいません。

例: `Example Plugin`

### `version` : required string

プラグインのバージョンです。

バージョンは[セマンティックバージョニング](https://semver.org/)に従うことが推奨されます。

例: `1.0.0`

### `description` : required string

プラグインの説明です。

説明は、以下のコードによってAndroidのGUI上で解釈可能な簡易的なHTMLをサポートします。

```
[android.widget.TextView.setText](https://developer.android.com/reference/android/widget/TextView#setText(int))([Html.fromHtml](https://developer.android.com/reference/android/text/Html#fromHtml(java.lang.String,%20int))(description))
```

例: `This is <b>an example</b> plugin.`
