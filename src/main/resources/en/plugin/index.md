# Plugin

KanbanBro plugins are a mechanism for extending the behavior of pages displayed by KanbanBro.

## Plugin syntax

A plugin consists of the following three parts:

- Magic comment
- Metadata section
- Body

### Magic comment

Use the following magic comment to mark the script as a KanbanBro plugin.

```javascript
// @KanbanBroPlugin
```

### Metadata section

The metadata section contains the plugin's metadata.

It is JSON code commented out with `// `.

---

Example:

```javascript
// {
//     "name": "com.example.example_plugin",
//     "title": "Example Plugin",
//     "version": "1.0.0",
//     "description": "This is <b>an example</b> plugin."
// }
```

### Body

The body is JavaScript code that implements the plugin's behavior.

To avoid conflicts with the metadata section, the first line of the body must not be a line comment.

The body is generally free-form, but in many cases it includes a check so it runs only on specific pages.

---

Example:

```javascript
if (location.href === 'https://example.com/') {
    console.log('This is example.com');
}
```

### Example

Below is a complete plugin example.

```javascript
// @KanbanBroPlugin
// {
//     "name": "com.example.example_plugin",
//     "title": "Example Plugin",
//     "version": "1.0.0",
//     "description": "This is <b>an example</b> plugin."
// }

if (location.href === 'https://example.com/') {
    console.log('This is example.com');
}
```

## File name

The plugin file must have the `.kbb.js` extension.

More precisely, the URL from which the plugin is downloaded is expected to end with `.kbb.js`.

## Metadata

### `name` : required string

A name that uniquely identifies the plugin.

The name is expected to follow an identifier-like format separated by dots, similar to a Java package name.

Each identifier component is expected to consist of one or more lowercase ASCII letters, digits, or underscores (`/[a-z0-9_]+/`).

Example: `com.example.example_plugin`

### `title` : required string

A human-readable title for the plugin.

The title does not have to be in English.

We recommend up to 10 full-width characters or 20 half-width characters.

Example: `Example Plugin`

### `version` : required string

The plugin version.

The version is expected to be the `<version core>` of [Semantic Versioning](https://semver.org/) (`/([1-9][0-9]*)\.([1-9][0-9]*)\.([1-9][0-9]*)/`).

Example: `1.0.0`

### `description` : required string

A description of the plugin.

It does not have to be in English.

The description supports a subset of HTML, which is rendered on Android by the following code:

<code>textView.<a href="https://developer.android.com/reference/android/widget/TextView#setText(int)">setText</a>(Html.<a href="https://developer.android.com/reference/android/text/Html#fromHtml(java.lang.String,%20int)">fromHtml</a>(description, Html.FROM_HTML_MODE_COMPACT))</code>

Example: `This is <b>an example</b> plugin.`

## Plugin behavior

Plugins are executed as JavaScript after the page has fully loaded.

Therefore, you do not need to wait for the onload event.

---

You cannot use `await` directly in the plugin body.

If you want to use `await`, wrap the code in a `Promise` as follows:

```javascript
new Promise(async () => {
    // Code that uses await
});
```
