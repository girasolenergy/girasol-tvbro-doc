// @KanbanBroPlugin
// {
//     "name": "co.pplc.kanbanbro.plugins.jepx_fullscreen_graph",
//     "title": "JEPX全画面グラフ",
//     "version": "1.0.3",
//     "description": "JEPXのページでグラフを全画面にし、すべての項目を表示します。"
// }

if (window.location.href == 'https://www.jepx.info/spot_free') {
  function delay(wait) {
    return new Promise(callback => setTimeout(() => callback(), wait));
  }
  function retry(wait, limit, getter) {
    return new Promise(async callback => {
      for (let t = 0; t < limit; t++) {
        if (t > 0) await delay(wait);
        const result = getter();
        if (result !== undefined) {
          callback(result);
          return;
        }
      }
      callback(undefined);
    });
  }
  new Promise(async () => {
    // ページ全体のスクロールバーを消す
    console.log("[jepx_fullscreen_graph] hide scrollbar: pre");
    document.documentElement.style.overflow = 'hidden';
    console.log("[jepx_fullscreen_graph] hide scrollbar: post");

    // グラフを全画面にする
    console.log("[jepx_fullscreen_graph] fullscreen graph: pre");
    const chartCard = await retry(1000, 10, () => {
      return Array.from(document.querySelectorAll('.card')).find(card => {
        const header = card.querySelector('.card-header');
        return header && header.innerText.startsWith('スポット市場日間価格');
      });
    });
    chartCard.style.position = 'fixed';
    chartCard.style.top = '0';
    chartCard.style.left = '0';
    chartCard.style.width = '100vw';
    chartCard.style.height = '100vh';
    chartCard.style.boxSizing = 'border-box';
    chartCard.style.zIndex = '99999';
    console.log("[jepx_fullscreen_graph] fullscreen graph: post");

    // すべての項目にチェックを入れる
    console.log("[jepx_fullscreen_graph] check all: pre");
    const areaCard = await retry(1000, 10, () => {
      return Array.from(document.querySelectorAll('.card')).find(card => {
        const header = card.querySelector('.card-header');
        return header && header.innerText == '表示したいエリアを選択してください';
      });
    });
    console.log("[jepx_fullscreen_graph] check all: areaCard = " + areaCard);
    let count = 0; // DEBUG
    Array.from(areaCard.querySelectorAll('.card-body input[type="checkbox"]')).forEach(checkbox => {
      console.log("[jepx_fullscreen_graph] checkbox = " + checkbox);
      if (!checkbox.checked) {
        console.log("[jepx_fullscreen_graph] checking");
        checkbox.click();
        count++; // DEBUG
      }
    });
    console.log("[jepx_fullscreen_graph] check all: post");
    if (count == 0) { // DEBUG
      console.log("[jepx_fullscreen_graph] rollback");
      document.querySelectorAll('.card')[3].style.position = 'static';
      document.documentElement.style.overflow = 'scroll';
    }
  })
}
