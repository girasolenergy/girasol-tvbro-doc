export function apply(title) {
    document.title = title;
    document.getElementById("topbar-right-container").prepend(
        also(document.createElement("div"), titleDiv => {
            titleDiv.className = "topbar-title";
            titleDiv.textContent = document.title;
        }),
    );
}
