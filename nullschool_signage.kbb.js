// @KanbanBroPlugin
// {
//     "name": "co.pplc.kanbanbro.plugins.nullschool_signage",
//     "title": "nullschool Signage",
//     "version": "1.0.2",
//     "description": "nullschool signage"
// }

if (location.hostname == "earth.nullschool.net") {
    conolg.log("[nullschool_signage] Start");
    new Promise(async () => {
        function make(e, block) {
            block(e);
            return e;
        }
        function delay(wait) {
            return new Promise(resolve => setTimeout(resolve, wait));
        }
        async function retry(getter) {
            let result;
            for (let i = 0; i < 60; i++) {
                if (i > 0) await delay(1000);
                result = getter();
                if (result !== undefined) return result;
            }
            throw new Error("Timeout waiting for element");
        }
        function element(query) {
            return retry(() => {
                const e = document.querySelectorAll(query);
                if (e.length == 0) return undefined;
                if (e.length > 1) throw new Error("Multiple elements found for query: " + query);
                return e[0];
            });
        }

        async function getModeUi() {
            const buttons = [...(await element('div[data-name="mode"]')).querySelectorAll("button")];
            const selectedIndex = buttons.findIndex(b => b.getAttribute("aria-selected") == "true");
            return {
                buttons: buttons,
                selectedIndex: selectedIndex,
                firstButton: buttons[0],
                currentButton: buttons[selectedIndex],
                nextButton: buttons[(selectedIndex + 1) % buttons.length]
            };
        }
        async function getOverlayUi() {
            const buttons = [...(await element('div:not([hidden]) > div[data-name="overlay_type"]')).querySelectorAll("button")];
            const selectedIndex = buttons.findIndex(b => b.getAttribute("aria-checked") == "true");
            return {
                buttons: buttons,
                selectedIndex: selectedIndex,
                firstButton: buttons[0],
                currentButton: buttons[selectedIndex],
                nextButton: buttons[(selectedIndex + 1) % buttons.length]
            };
        }
        async function updateLabel() {
            const modeUi = await getModeUi();
            const overlayUi = await getOverlayUi();
            label.innerText = modeUi.currentButton.querySelectorAll("span")[0].innerText.trim() + " / " + overlayUi.currentButton.querySelectorAll("span")[0].innerText.trim();
        }

        conolg.log("[nullschool_signage] Starting nullschool signage plugin");

        // Create label
        let label = null;
        document.querySelector("main").appendChild(make(document.createElement("div"), div => {
            div.classList.add("card");
            div.innerText = "Loading...";
            div.style.fontSize = "large";
            div.style.position = "absolute";
            div.style.top = "0";
            div.style.left = "0";
            div.style.whiteSpace = "nowrap";
            div.style.margin = "1rem 1rem 0 1rem";
            label = div;
        }));
        conolg.log("[nullschool_signage] Label created");

        // Hide UI
        document.getElementById("location-mark").style.display = "none";
        conolg.log("[nullschool_signage] location-mark hidden");
        document.querySelectorAll(".stack").forEach(e => {
            e.style.display = "none";
        });
        conolg.log("[nullschool_signage] .stack hidden");
        document.querySelectorAll(".cta-bar").forEach(e => {
            e.style.display = "none";
        });
        conolg.log("[nullschool_signage] .cta-bar hidden");
        document.querySelectorAll(".earth-bar").forEach(e => {
            e.style.display = "none";
        });
        conolg.log("[nullschool_signage] .earth-bar hidden");

        // Update label
        updateLabel();

        while (true) {

            await delay(30000);

            // Go to the next mode
            {
                const overlayUi = await getOverlayUi();
                if (overlayUi.selectedIndex == overlayUi.buttons.length - 1) {
                    const modeUi = await getModeUi()
                    modeUi.nextButton.click();
                    const overlayUi2 = await getOverlayUi();
                    overlayUi2.firstButton.click();
                } else {
                    overlayUi.nextButton.click();
                }
                updateLabel();
                conolg.log("[nullschool_signage] Overlay changed");
            }

        }
    });
}
