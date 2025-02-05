// @KanbanBroPlugin
// {
//     "name": "co.pplc.kanbanbro.plugins.investing_fullscreen_chart",
//     "title": "Investing全画面グラフ",
//     "version": "1.0.1",
//     "description": "jp.investing.comでグラフをろうそく足チャートにして全画面表示します。"
// }

if (window.location.host == 'jp.investing.com') {
  function delay(wait) {
    return new Promise(callback => setTimeout(() => callback(), wait));
  }
  function retry(wait, limit, getter) {
    return new Promise(async callback => {
      for (let t = 0; t < limit; t++) {
        if (t > 0) await delay(wait);
        const result = await getter();
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
    console.log("[investing_fullscreen_chart] removing scroll bar");
    document.documentElement.style.overflow = 'hidden';

    // グラフを全画面にする
    console.log("[investing_fullscreen_chart] fullscreen");
    const container = await retry(1000, 10, () => {
      const container = document.querySelector('.overview-chart .overview-chart-body');
      return container === null ? undefined : container;
    });
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
    //console.log("[investing_fullscreen_chart] removing ui");
    //container.querySelector('.overview-chart-footer').style.display = 'none';

    // 表示モードをろうそく足チャートにする
    console.log("[investing_fullscreen_chart] candle");
    await delay(5000);
    const button = await retry(1000, 10, () => {
      const button = document.querySelector('button[title="ろうそく足チャート"]');
      return button === null ? undefined : button;
    });
    button.click()

  });
}
