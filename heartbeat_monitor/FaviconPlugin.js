export function apply(href) {
    document.head.append(
        also(document.createElement("link"), link => {
            link.rel = "icon";
            link.href = href;
        }),
    );
}
