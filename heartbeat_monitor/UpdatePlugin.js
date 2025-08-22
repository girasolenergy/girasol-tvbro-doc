export function apply() {
    document.getElementById("topbar-left-container").append(
        also(document.createElement("button"), button => {
            button.type = "button";
            button.textContent = "Update";
            button.addEventListener("click", () => scheduleUpdate());
        }),
    );
}
