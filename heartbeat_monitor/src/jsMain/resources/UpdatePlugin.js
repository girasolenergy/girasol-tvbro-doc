export function apply() {
    document.getElementById("topbar-right-container").prepend(
        also(document.createElement("button"), button => {
            button.type = "button";
            button.textContent = "Update";
            button.addEventListener("click", () => scheduleUpdate());
        }),
    );
}
