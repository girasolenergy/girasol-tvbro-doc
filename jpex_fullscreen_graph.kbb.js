// @KanbanBroPlugin
// {
//     "name": "co.pplc.kanbanbro.plugins.jpex_fullscreen_graph",
//     "title": "JPEX全画面グラフ",
//     "version": "1.0.1",
//     "description": "JPEXのページでグラフを全画面にし、すべての項目を表示します。"
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
    document.documentElement.style.overflow = 'hidden';

    // グラフを全画面にする
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

    // すべての項目にチェックを入れる
    const areaCard = await retry(1000, 10, () => {
      return Array.from(document.querySelectorAll('.card')).find(card => {
        const header = card.querySelector('.card-header');
        return header && header.innerText == '表示したいエリアを選択してください';
      });
    });
    Array.from(areaCard.querySelectorAll('.card-body input[type="checkbox"]')).forEach(checkbox => {
      if (!checkbox.checked) checkbox.click();
    });
  })
}
