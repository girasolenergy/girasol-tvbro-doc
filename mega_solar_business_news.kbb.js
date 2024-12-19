// @KanbanBroPlugin
// {
//     "name": "co.pplc.kanbanbro.plugins.mega_solar_business_news",
//     "title": "メガソーラービジネスニュースタイル表示",
//     "version": "1.0.0",
//     "description": "メガソーラービジネスニュースタイル表示"
// }

if (window.location.href.startsWith('https://project.nikkeibp.co.jp/ms/mega-solar/?bn=news')) {
  // ページ全体のスクロールバーを消す
  document.documentElement.style.overflow = 'hidden';

  // タイル表示にする
  const ulElement = document.querySelector('#mainContents .listContentsBox .mainCommonBox ul');
  ulElement.style.position = 'fixed';
  ulElement.style.top = '0';
  ulElement.style.left = '0';
  ulElement.style.width = '100vw';
  ulElement.style.height = '100vh';
  ulElement.style.boxSizing = 'border-box';
  ulElement.style.overflow = 'scroll';
  ulElement.style.backgroundColor = 'white';
  ulElement.style.zIndex = '99999';
  ulElement.style.display = 'grid';
  ulElement.style.gridTemplateColumns = 'repeat(3, 1fr)';
  ulElement.style.gap = '2em';
  ulElement.style.padding = '1em';

  // マージンを綺麗にする
  const listItems = ulElement.querySelectorAll('li');
  listItems.forEach(item => {
    item.style.margin = '0';
    item.style.padding = '0';
  });
}
