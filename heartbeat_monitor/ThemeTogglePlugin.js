export function apply() {
    document.getElementById("topbar-right-container").prepend(
        also(document.createElement("button"), button => {
            button.type = "button";
            window.KanbanBro.event.addEventListener('themeChanged', () => {
                if (window.KanbanBro.theme === null) {
                    button.textContent = 'Theme: Auto';
                } else if (window.KanbanBro.theme === 'light') {
                    button.textContent = 'Theme: Light';
                } else if (window.KanbanBro.theme === 'dark') {
                    button.textContent = 'Theme: Dark';
                } else {
                    button.textContent = `Theme: ${window.KanbanBro.theme}`;
                }
            });
            button.addEventListener("click", () => {
                const currentTheme = window.KanbanBro.theme;
                setTheme(currentTheme === null ? "light" : currentTheme === "light" ? "dark" : null);
            });
        }),
    );
}
