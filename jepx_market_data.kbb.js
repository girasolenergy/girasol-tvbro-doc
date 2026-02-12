// @KanbanBroPlugin
// {
//     "name": "co.pplc.kanbanbro.plugins.jepx_market_data",
//     "title": "JEPX取引市場データ",
//     "version": "1.0.0",
//     "description": "JEPXの時間前市場の約定価格グラフを全画面表示します。"
// }

if (window.location.href == 'https://www.jepx.jp/electricpower/market-data/intraday/') {
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
    const $ = await retry(1000, 10, () => window.jQuery);

    // 日付を今日に設定
    {

      // 今日の日付を計算
      const now = new Date();
      const todayString = `${now.getFullYear()}/${String(now.getMonth() + 1).padStart(2, '0')}/${String(now.getDate()).padStart(2, '0')}`;
      console.log(`[jepx_market_data/set_date] date: ${todayString}`);

      // UIの初期化を待つ
      const datepicker = await retry(1000, 10, () => $("#datepicker").length > 0 ? $("#datepicker") : undefined);
      if (datepicker === undefined) throw new Error("Failed to find datepicker.");

      // 日付を設定
      console.log("[jepx_market_data/set_date] pre");
      datepicker.datepicker("setDate", todayString); // UI上の日付の選択状態を変更
      datepicker.datepicker("option", "onSelect").call(datepicker[0], todayString, datepicker.data("datepicker")); // 選択時のイベントを発火させる
      console.log("[jepx_market_data/set_date] post");

    }

    // グラフを全画面にする
    {

      // グラフの初期化を待つ
      const graphWrapper = await retry(1000, 10, () => document.querySelector(".graph-section__content-wrapper") ?? undefined);
      if (graphWrapper === undefined) throw new Error("Failed to find graph wrapper.");

      // ターゲット自身および、htmlタグを含むそれより先祖のすべてのエレメントにスタイルを適用
      console.log("[jepx_market_data/fullscreen_graph] pre");
      let element = graphWrapper;
      while (element) { // htmlタグまで到達するまで先祖を辿る
        console.log(`[jepx_market_data/fullscreen_graph] applying style to <${element.tagName.toLowerCase()}>`);

        // ターゲット要素が全画面に表示されるようにする
        element.style.margin = '0';
        element.style.border = 'none';
        element.style.padding = element.tagName?.toLowerCase() === 'body' ? '1vw' : '0'; // bodyのみ例外的に余白を保証
        element.style.width = '100%';
        element.style.height = '100%';
        element.style.boxSizing = 'border-box';

        // 兄弟要素を非表示にする
        if (element.parentElement) {
          Array.from(element.parentElement.children).forEach(sibling => {
            if (sibling !== element) sibling.style.display = 'none';
          });
        }

        element = element.parentElement;
      }
      console.log("[jepx_market_data/fullscreen_graph] post");

    }

  });
}
