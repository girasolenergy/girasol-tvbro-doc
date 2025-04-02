// @KanbanBroPlugin
// {
//     "name": "co.pplc.kanbanbro.plugins.fullscreen_amesh",
//     "title": "全画面アメッシュ",
//     "version": "1.0.0",
//     "description": "東京アメッシュを全画面表示にするプラグインです。"
// }

if (location.href.startsWith("https://tokyo-ame.jwa.or.jp/")) {
    new Promise(async () => {
        async function select(selector) {
            for (let i = 0; i < 50; i++) {
                const elements = Array.from(document.querySelectorAll(selector));
                if (elements.length == 1) {
                    return elements[0];
                } else if (elements.length > 1) {
                    throw new Error(`Found ${elements.length} elements matching selector "${selector}"`);
                }
                await new Promise(resolve => setTimeout(resolve, 200));
            }
            throw new Error(`Timed out waiting for selector "${selector}"`);
        }

        // ページのスクロールバーを消す
        document.documentElement.style.overflowY = "hidden";

        // 地図を全画面にする
        const map = await select("#left");
        map.style.width = "100vw";
        map.style.height = "100vh";
        map.style.position = "fixed";
        map.style.top = "0";
        map.style.right = "0";
        map.style.bottom = "0";
        map.style.left = "0";
        map.style.backgroundColor = "#FFFFFF";

        // 周囲の要素を非表示
        (await select("#map .background-cover")).style.display = "none";
        (await select("#header")).style.display = "none";
        (await select("#explanation")).style.display = "none";
        (await select("#right")).style.display = "none";
        (await select("#outer-link")).style.display = "none";
        (await select("#record_controll")).style.display = "none";
        (await select("#region_controll")).style.display = "none";
    });
}
