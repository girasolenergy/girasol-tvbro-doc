export function apply() {
    function getTitle(cardComparatorSpecifier) {
        if (cardComparatorSpecifier.type == "name") {
            return cardComparatorSpecifier.isDescending ? "Name (desc)" : "Name";
        } else if (cardComparatorSpecifier.type == "updated") {
            return cardComparatorSpecifier.isDescending ? "Newest Update" : "Oldest Update";
        } else {
            return "Unknown";
        }
    }

    const cyclerCardComparatorSpecifiers = [
        { type: "name", isDescending: false },
        { type: "name", isDescending: true },
        { type: "updated", isDescending: true },
        { type: "updated", isDescending: false },
    ];

    function cycleSort() {
        const oldIndex = cyclerCardComparatorSpecifiers.findIndex(cardComparatorSpecifierEntry => {
            if (cardComparatorSpecifierEntry.type != window.KanbanBro.cardComparatorSpecifiers[0].type) return false;
            if (cardComparatorSpecifierEntry.isDescending != window.KanbanBro.cardComparatorSpecifiers[0].isDescending) return false;
            return true;
        });
        const newIndex = oldIndex == -1 ? 0 : (oldIndex + 1) % cyclerCardComparatorSpecifiers.length;
        setCardComparatorSpecifiers([cyclerCardComparatorSpecifiers[newIndex]]);
    }

    function openSortDialog() {
        showDialog((container, dialogEvent) => {
            container.append(
                also(document.createElement("div"), titleDiv => {
                    titleDiv.textContent = "Sort";
                    titleDiv.style.fontWeight = "700";
                }),
                also(document.createElement("button"), toggleButton => {
                    toggleButton.type = "button";
                    toggleButton.classList.add("dialog-button");

                    function updateButton() {
                        if (window.KanbanBro.cardComparatorSpecifiers.length == 0) {
                            toggleButton.textContent = "(Default)";
                        } else if (window.KanbanBro.cardComparatorSpecifiers.length == 1) {
                            toggleButton.textContent = getTitle(window.KanbanBro.cardComparatorSpecifiers[0]);
                        } else {
                            toggleButton.textContent = getTitle(window.KanbanBro.cardComparatorSpecifiers[0]) + "+" + (window.KanbanBro.cardComparatorSpecifiers.length - 1);
                        }
                    }
                    window.KanbanBro.event.addEventListener('cardComparatorSpecifiersChanged', () => {
                        updateButton();
                    });
                    updateButton();

                    toggleButton.addEventListener("click", () => {
                        cycleSort();
                    });
                }),
                also(document.createElement("div"), actionsDiv => {
                    actionsDiv.style.display = "flex";
                    actionsDiv.style.justifyContent = "end";
                    actionsDiv.style.gap = "8px";
                    actionsDiv.append(
                        also(document.createElement("button"), closeButton => {
                            closeButton.type = "button";
                            closeButton.textContent = "Close";
                            closeButton.classList.add("dialog-button");
                            closeButton.addEventListener("click", () => dialogEvent.dispatchEvent(new Event("close")));
                        }),
                    );
                }),
            );
        });
    }

    document.getElementById("topbar-left-container").append(
        also(document.createElement("div"), container => {
            container.className = "topbar-property";
            container.append(
                also(document.createElement("button"), sortButton => {
                    sortButton.type = "button";
                    sortButton.textContent = "Sort";

                    function updateButton() {
                        if (window.KanbanBro.cardComparatorSpecifiers.length == 0) {
                            sortButton.textContent = "(Default)";
                        } else if (window.KanbanBro.cardComparatorSpecifiers.length == 1) {
                            sortButton.textContent = getTitle(window.KanbanBro.cardComparatorSpecifiers[0]);
                        } else {
                            sortButton.textContent = getTitle(window.KanbanBro.cardComparatorSpecifiers[0]) + "+" + (window.KanbanBro.cardComparatorSpecifiers.length - 1);
                        }
                    }
                    window.KanbanBro.event.addEventListener('cardComparatorSpecifiersChanged', () => {
                        updateButton();
                    });
                    updateButton();

                    sortButton.addEventListener("click", () => openSortDialog());
                }),
            );
        }),
    );
}
