// @KanbanBroPlugin
// {
//     "name": "co.pplc.kanbanbro.plugins.jpex_fullscreen_graph",
//     "title": "JPEX全画面グラフ",
//     "version": "1.0.0",
//     "description": "JPEXのページでグラフを全画面にし、すべての項目を表示します。"
// }

if (window.location.href == 'https://www.jepx.info/spot_free') {
  // ページ全体のスクロールバーを消す
  document.documentElement.style.overflow = 'hidden';

  // グラフを全画面にする
  const chartCard = Array.from(document.querySelectorAll('.card')).find(card => {
    const header = card.querySelector('.card-header');
    return header && header.innerText.startsWith('スポット市場日間価格');
  });
  chartCard.style.position = 'fixed';
  chartCard.style.top = '0';
  chartCard.style.left = '0';
  chartCard.style.width = '100vw';
  chartCard.style.height = '100vh';
  chartCard.style.boxSizing = 'border-box';
  chartCard.style.zIndex = '99999';

  // すべての項目にチェックを入れる
  const areaCard = Array.from(document.querySelectorAll('.card')).find(card => {
    const header = card.querySelector('.card-header');
    return header && header.innerText == '表示したいエリアを選択してください';
  });
  Array.from(areaCard.querySelectorAll('.card-body input[type="checkbox"]')).forEach(checkbox => {
    if (!checkbox.checked) checkbox.click();
  });
}
