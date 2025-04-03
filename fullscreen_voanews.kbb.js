// @KanbanBroPlugin
// {
//     "name": "co.pplc.kanbanbro.plugins.fullscreen_voanews",
//     "title": "全画面VOA News",
//     "version": "1.0.0",
//     "description": "VOA Newsを全画面表示にするプラグインです。"
// }

if (location.href.startsWith("https://www.voanews.com/")) {
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
    
    async function selectAll(selector) {
        for (let i = 0; i < 50; i++) {
            const elements = Array.from(document.querySelectorAll(selector));
            if (elements.length >= 1) return elements;
            await new Promise(resolve => setTimeout(resolve, 200));
        }
        throw new Error(`Timed out waiting for selector "${selector}"`);
    }
    
    // コンテンツの幅を広げる
    (await selectAll(".container")).forEach(element => {
        element.style.maxWidth = "none";
    });
    
    // コンテンツ以外を消す
    let element = await select("#content");
    while (true) {
        const parent = element.parentElement;
        if (parent == null) break;
        for (let child of parent.children) {
            if (child !== element) {
                child.style.display = "none";
            }
        }
        element = parent;
    }
    
    // 見出しを消す
    (await selectAll(".section-head")).forEach(element => {
        element.style.display = "none";
    });
    
    // 先頭の画像の高さを小さくする
    (await selectAll(".img-wrap--size-1")).forEach(element => {
        element.style.height = "200px";
    });
    
    // 記事タイトルを大きくする
    (await selectAll("h4")).forEach(element => {
        element.style.fontSize = "20px";
    });
    
    // 先頭にスクロールし、画像を読み込むイベントを発火する
    document.documentElement.scrollTop = 0;
  });
}
