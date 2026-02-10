// @KanbanBroPlugin
// {
//     "name": "co.pplc.kanbanbro.plugins.jepx_market_data",
//     "title": "JEPX取引市場データ",
//     "version": "1.0.0",
//     "description": "JEPXの時間前市場の約定価格グラフを全画面表示します。"
// }

if (window.location.href.startsWith('https://www.jepx.jp/electricpower/market-data/intraday/')) {
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

    // ターゲット自身および、htmlタグを含むそれより先祖のすべてのエレメントにスタイルを適用
    let current = graphWrapper;
    while (current) { // htmlタグまで到達するまで先祖を辿る
      current.style.margin = '0';
      current.style.border = 'none';
      current.style.padding = '0';
      current.style.width = '100%';
      current.style.height = '100%';
      current.style.boxSizing = 'border-box';

      // bodyのみ例外的に余白を保証
      if (current.tagName && current.tagName.toUpperCase() === 'BODY') {
        current.style.padding = '1vw';
      }

      // 兄弟要素を非表示にする
      if (current.parentElement) {
        Array.from(current.parentElement.children).forEach(sibling => {
          if (sibling !== current) {
            sibling.style.display = 'none';
          }
        });
      }

      current = current.parentElement;
    }

    console.log("[jepx_market_data] fullscreen graph: post");

  });
}