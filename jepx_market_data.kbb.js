// @KanbanBroPlugin
// {
//   "name": "co.pplc.kanbanbro.plugins.jepx_market_data",
//   "title": "JEPX取引市場データ",
//   "version": "1.0.0",
//   "description": "JEPXの時間前市場の約定価格グラフを全画面表示します。"
// }

if (window.location.href.startsWith('https://www.jepx.jp/electricpower/market-data/intraday/')) {
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

    // 日付を今日に設定する関数
    function setDateAndFire(dateText) {
      $("#datepicker").datepicker("setDate", dateText);
      ($("#datepicker").datepicker("option", "onSelect") || function () { }).call(
        $("#datepicker")[0],
        dateText,
        $("#datepicker").data("datepicker")
      );
    }

    // 今日の日付を取得してフォーマット
    const today = new Date();
    const dateText = `${today.getFullYear()}/${String(today.getMonth() + 1).padStart(2, '0')}/${String(today.getDate()).padStart(2, '0')}`;

    // 日付を今日に設定
    console.log("[jepx_market_data] setting date to today: " + dateText);
    await retry(1000, 10, () => {
      if (typeof $ !== 'undefined' && $("#datepicker").length > 0) {
        setDateAndFire(dateText);
        return true;
      }
      return undefined;
    });
    console.log("[jepx_market_data] date set");

    // グラフの読み込みを待つ
    await delay(2000);

    // ページ全体のスクロールバーを消す
    console.log("[jepx_market_data] hiding scrollbar");
    document.documentElement.style.overflow = 'hidden';

    // グラフを全画面にする
    console.log("[jepx_market_data] fullscreen graph: pre");
    const graphWrapper = await retry(1000, 10, () => {
      const wrapper = document.querySelector(".graph-section__content-wrapper");
      return wrapper === null ? undefined : wrapper;
    });
    if (graphWrapper === undefined) {
      console.error("[jepx_market_data] Failed to get graph wrapper element");
      return;
    }
    graphWrapper.style.position = 'fixed';
    graphWrapper.style.top = '0';
    graphWrapper.style.left = '0';
    graphWrapper.style.width = '100vw';
    graphWrapper.style.height = '100vh';
    graphWrapper.style.boxSizing = 'border-box';
    graphWrapper.style.zIndex = '99999';
    graphWrapper.style.backgroundColor = 'white';
    console.log("[jepx_market_data] fullscreen graph: post");

  });
}
