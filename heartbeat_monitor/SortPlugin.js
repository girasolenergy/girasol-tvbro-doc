export function apply() {
    const cardComparatorSpecifierEntries = [
        {
            cardComparatorSpecifier: "name",
            title: "Name",
        },
        {
            cardComparatorSpecifier: "-name",
            title: "Name (desc)",
        },
        {
            cardComparatorSpecifier: "updated",
            title: "Newest Update",
        },
        {
            cardComparatorSpecifier: "-updated",
            title: "Oldest Update",
        },
    ];

    function cycleSort() {
        const oldIndex = cardComparatorSpecifierEntries.findIndex(cardComparatorSpecifierEntry => {
            return cardComparatorSpecifierEntry.cardComparatorSpecifier === window.KanbanBro.cardComparatorSpecifiers[0];
        });
        const newIndex = (oldIndex + 1) % cardComparatorSpecifierEntries.length;
        setCardComparatorSpecifiers([cardComparatorSpecifierEntries[newIndex].cardComparatorSpecifier]);
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
                        const index = cardComparatorSpecifierEntries.findIndex(cardComparatorSpecifierEntry => {
                            return cardComparatorSpecifierEntry.cardComparatorSpecifier === window.KanbanBro.cardComparatorSpecifiers[0];
                        });
                        toggleButton.textContent = cardComparatorSpecifierEntries[index].title;
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
                    sortButton.addEventListener("click", () => openSortDialog());
                }),
            );
        }),
    );
}
