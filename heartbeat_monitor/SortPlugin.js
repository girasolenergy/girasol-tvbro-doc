export function apply() {
    window.KanbanBro.cardComparators["empty"] = {
        compare: (specifier, a, b) => 0,
        getTitle: specifier => "Unsorted",
    };
    window.KanbanBro.cardComparators["name"] = {
        compare: (specifier, a, b) => {
            const cmp = (a.keys?.name ?? "").localeCompare(b.keys?.name ?? "");
            return specifier.isDescending ? -cmp : cmp;
        },
        getTitle: specifier => specifier.isDescending ? "Name (desc)" : "Name",
    };
    window.KanbanBro.cardComparators["updated"] = {
        compare: (specifier, a, b) => {
            const cmp = (a.keys?.updated ?? 0) - (b.keys?.updated ?? 0);
            return specifier.isDescending ? -cmp : cmp;
        },
        getTitle: specifier => specifier.isDescending ? "Newest Update" : "Oldest Update",
    };

    function getTitle(cardComparatorSpecifier) {
        const cardComparator = window.KanbanBro.cardComparators[cardComparatorSpecifier.type];
        if (cardComparator == null) return "Invalid Comparator";
        return cardComparator.getTitle(cardComparatorSpecifier);
    }

    const cyclerCardComparatorSpecifiers = [
        { type: "name", isDescending: false },
        { type: "name", isDescending: true },
        { type: "updated", isDescending: true },
        { type: "updated", isDescending: false },
        { type: "empty" },
    ];

    function getNextCardComparatorSpecifier(cardComparatorSpecifier) {
        const oldIndex = cyclerCardComparatorSpecifiers.findIndex(cyclerCardComparatorSpecifier => {
            if (cyclerCardComparatorSpecifier.type != cardComparatorSpecifier.type) return false;
            if (cyclerCardComparatorSpecifier.isDescending !== undefined) {
                if (cyclerCardComparatorSpecifier.isDescending != cardComparatorSpecifier.isDescending) return false;
            }
            return true;
        });
        if (oldIndex == -1) return cyclerCardComparatorSpecifiers[0];
        return cyclerCardComparatorSpecifiers[(oldIndex + 1) % cyclerCardComparatorSpecifiers.length];
    }

    function openSortDialog() {
        showDialog((container, dialogEvent) => {
            container.append(
                also(document.createElement("div"), titleDiv => {
                    titleDiv.textContent = "Sort";
                    titleDiv.style.fontWeight = "700";
                }),
                also(document.createElement("div"), buttonsDiv => {
                    buttonsDiv.className = "dialog-container";
                    function updateButtons() {
                        buttonsDiv.innerHTML = "";
                        window.KanbanBro.cardComparatorSpecifiers.forEach((cardComparatorSpecifier, index) => {
                            buttonsDiv.append(
                                also(document.createElement("div"), buttonDiv => {
                                    buttonDiv.style.display = "flex";
                                    buttonDiv.style.gap = "12px";
                                    buttonDiv.style.alignItems = "center";
                                    buttonDiv.append(
                                        also(document.createElement("div"), leftDiv => {
                                            leftDiv.append(
                                                also(document.createElement("button"), toggleButton => {
                                                    toggleButton.type = "button";
                                                    toggleButton.classList.add("dialog-button");
                                                    toggleButton.textContent = getTitle(cardComparatorSpecifier);
                                                    toggleButton.addEventListener("click", () => {
                                                        const newCardComparatorSpecifiers = [...window.KanbanBro.cardComparatorSpecifiers];
                                                        newCardComparatorSpecifiers[index] = getNextCardComparatorSpecifier(newCardComparatorSpecifiers[index]);
                                                        setCardComparatorSpecifiers(newCardComparatorSpecifiers);
                                                    });
                                                }),
                                            );
                                        }),
                                        also(document.createElement("div"), rightDiv => {
                                            rightDiv.style.marginLeft = "auto";
                                            rightDiv.append(
                                                also(document.createElement("button"), removeButton => {
                                                    removeButton.type = "button";
                                                    removeButton.classList.add("dialog-button");
                                                    removeButton.textContent = "🗑️";
                                                    removeButton.addEventListener("click", () => {
                                                        const newCardComparatorSpecifiers = [...window.KanbanBro.cardComparatorSpecifiers];
                                                        newCardComparatorSpecifiers.splice(index, 1);
                                                        setCardComparatorSpecifiers(newCardComparatorSpecifiers);
                                                    });
                                                }),
                                            );
                                        }),
                                    );
                                }),
                            );
                        });
                    }
                    window.KanbanBro.event.addEventListener('cardComparatorSpecifiersChanged', () => updateButtons());
                    updateButtons();
                }),
                also(document.createElement("button"), addButton => {
                    addButton.type = "button";
                    addButton.classList.add("dialog-transparent-button");
                    addButton.textContent = "＋";
                    addButton.addEventListener("click", () => {
                        setCardComparatorSpecifiers([...window.KanbanBro.cardComparatorSpecifiers, { type: "empty" }]);
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
                            sortButton.textContent = "Unsorted";
                        } else if (window.KanbanBro.cardComparatorSpecifiers.length == 1) {
                            sortButton.textContent = getTitle(window.KanbanBro.cardComparatorSpecifiers[0]);
                        } else {
                            sortButton.textContent = getTitle(window.KanbanBro.cardComparatorSpecifiers[0]) + "+" + (window.KanbanBro.cardComparatorSpecifiers.length - 1);
                        }
                    }
                    window.KanbanBro.event.addEventListener('cardComparatorSpecifiersChanged', () => updateButton());
                    updateButton();

                    sortButton.addEventListener("click", () => openSortDialog());
                }),
            );
        }),
    );
}
