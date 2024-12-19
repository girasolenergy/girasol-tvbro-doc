// @KanbanBroPlugin
// {
//     "name": "co.pplc.kanbanbro.plugins.investing_fullscreen_chart",
//     "title": "Investing全画面グラフ",
//     "version": "1.0.0",
//     "description": "jp.investing.comでグラフをろうそく足チャートにして全画面表示します。"
// }

if (window.location.host == 'jp.investing.com') {
  // ページ全体のスクロールバーを消す
  document.documentElement.style.overflow = 'hidden';

  // グラフを全画面にする
  const container = document.querySelector('.overview-chart .overview-chart-body');
  container.style.position = 'fixed';
  container.style.top = '0';
  container.style.left = '0';
  container.style.width = '100vw';
  container.style.height = '100vh';
  container.style.boxSizing = 'border-box';
  container.style.zIndex = '99999';
  container.style.backgroundColor = 'white';
  container.style.padding = '3vw';

  // UIを消す
  //container.querySelector('.overview-chart-footer').style.display = 'none';

  // 表示モードをろうそく足チャートにする
  document.querySelector('button[title="ろうそく足チャート"]').click()
}
