// @KanbanBroPlugin
// {
//     "name": "co.pplc.kanbanbro.plugins.grafana_public_dashboards_hide_ui",
//     "title": "Grafana Public Dashboards Hide UI",
//     "version": "1.0.0",
//     "description": "Grafana Public Dashboards Hide UI"
// }

if (location.href.includes("grafana/public-dashboards")) {
    new Promise(async () => {
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
        (await element('div[data-testid="public-dashboard-scene-page"] > div:nth-child(1)')).style.visibility = "hidden";
        (await element('div[data-testid="public-dashboard-footer"]')).style.backgroundColor = "transparent";
        document.body.style.backgroundPositionY = "0";
    });
}
