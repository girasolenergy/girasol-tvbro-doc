// @KanbanBroPlugin
// {
//     "name": "co.pplc.kanbanbro.plugins.weathernews_fullscreen",
//     "title": "ウェザーニュース県別天気全画面表示",
//     "version": "1.0.0",
//     "description": "ウェザーニュースの県別の天気を全画面で表示します。"
// }

if (new RegExp('^https://weathernews\\.jp/onebox/tenki/[^/]+/$').test(window.location.href)) {
  // ページ全体のスクロールバーを消す
  document.documentElement.style.overflow = 'hidden';

  // グラフを全画面にする
  const main = document.querySelector('#main');
  main.style.position = 'fixed';
  main.style.top = '0';
  main.style.left = '0';
  main.style.width = '100vw';
  main.style.maxWidth = '100vw';
  main.style.height = '100vh';
  main.style.boxSizing = 'border-box';
  main.style.margin = '0';
  main.style.padding = '1vw';
  main.style.gap = '2vw';
  main.style.zIndex = '99999';
  main.style.backgroundColor = 'white';
  main.style.flexWrap = 'wrap';
  main.style.overflow = 'scroll';
  main.style.scrollbarWidth = 'none';

  main.children[0].style.width = 'calc(96vw - 35em)';
  main.children[0].style.margin = '0';
  main.children[0].style.padding = '0';

  main.children[1].style.width = '35em';
  main.children[1].style.margin = '0';
  main.children[1].style.padding = '0';
}
