export function apply() {
    document.getElementById("topbar-right-container").prepend(
        also(document.createElement("div"), container => {
            container.className = "topbar-property";
            container.append(
                also(document.createElement("span"), span => {
                    span.textContent = "Auto Update:";
                }),
                also(document.createElement("select"), select => {
                    let timerId = null;

                    const defaultIntervals = ["10s", "30s", "1m", "2m", "5m", "10m", "30m", "1h", "2h", "6h", "12h"];

                    function optionOf(value) {
                        if (value === "") return null;
                        const unit = value.slice(-1);
                        const amount = Number(value.slice(0, -1));
                        if (!Number.isFinite(amount) || amount <= 0) {
                            throw new Error(`Invalid interval value: ${value}`);
                        } else if (unit === "s") {
                            return { value, interval: amount * 1000, title: `${amount} ${amount === 1 ? "Second" : "Seconds"}` };
                        } else if (unit === "m") {
                            return { value, interval: amount * 60 * 1000, title: `${amount} ${amount === 1 ? "Minute" : "Minutes"}` };
                        } else if (unit === "h") {
                            return { value, interval: amount * 60 * 60 * 1000, title: `${amount} ${amount === 1 ? "Hour" : "Hours"}` };
                        } else {
                            throw new Error(`Invalid interval value: ${value}`);
                        }
                    }

                    function apply(option) {
                        if (timerId !== null) {
                            clearInterval(timerId);
                            timerId = null;
                        }
                        if (option) timerId = setInterval(() => scheduleUpdate(), option.interval);
                    }

                    function registerOption(value, title) {
                        select.append(
                            also(document.createElement("option"), optionElement => {
                                optionElement.value = value;
                                optionElement.textContent = title;
                            }),
                        );
                    }

                    registerOption("", "None");
                    for (const option of defaultIntervals.map(optionOf)) {
                        registerOption(option.value, option.title);
                    }

                    select.addEventListener("change", () => {
                        const option = optionOf(select.value);
                        setPageParameter("r", option?.value);
                        apply(option);
                    });

                    window.addEventListener("popstate", () => {
                        const option = optionOf(getPageParameter("r") ?? "");
                        if (option != null && !defaultIntervals.includes(option.value)) {
                            registerOption(option.value, option.title);
                        }
                        select.value = option?.value ?? "";
                        apply(option);
                    });

                    {
                        const option = optionOf(getPageParameter("r") ?? "");
                        if (option != null && !defaultIntervals.includes(option.value)) {
                            registerOption(option.value, option.title);
                        }
                        select.value = option?.value ?? "";
                        apply(option);
                    }

                }),
            );
        })
    );
}
