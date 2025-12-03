// @KanbanBroPlugin
// {
//     "name": "co.pplc.kanbanbro.plugins.jepx_fullscreen_graph",
//     "title": "JEPX全画面グラフ",
//     "version": "1.1.0",
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
    const range = (start, until) => Array.from({ length: until - start }, (_, i) => start + i);

    // レジェンド改行パッチ
    {
      console.log("[jepx_fullscreen_graph] legend patch: pre");
      const old = google.visualization.ChartWrapper.prototype.setOptions;
      google.visualization.ChartWrapper.prototype.setOptions = function (options) {
        console.log("setOptions", this, options);
        options.legend.maxLines = 2;
        return old.call(this, options);
      };
      console.log("[jepx_fullscreen_graph] legend patch: post");
    }

    // 系列半透明パッチ
    {
      console.log("[jepx_fullscreen_graph] series transparency patch: pre");
      const old = google.visualization.LineChart.prototype.draw;
      google.visualization.LineChart.prototype.draw = function (data, b, c) {
        console.log("draw", this, data, b, c);

        // 既に改変済みもしくは想定外のデータが降ってきた
        let skip = false;
        range(0, data.getNumberOfColumns()).forEach(c => {
          if (data.getColumnRole(c) !== "") {
            console.log(`Unexpected column role at index ${c}: ${data.getColumnRole(c)}`);
            skip = true;
          }
        });
        if (skip) {
          console.log("skip: already modified or unexpected data");
          return old.call(this, data, b, c);
        }

        // スタイル列を追加
        const seriesCount = data.getNumberOfColumns() - 1;
        range(0, seriesCount).forEach(s => {
          // D V V      初期状態
          // D V S V    index=2
          // D V S V S  index=4
          data.insertColumn(2 + s * 2, { type: "string", role: "style" });
        });
        range(0, seriesCount).forEach(s => {
          range(0, data.getNumberOfRows()).forEach(r => {
            const MAX_DISTANCE = 0.2;
            const getDataC = s => 1 + s * 2;
            const getStyleC = s => 2 + s * 2;
            const getData = (r, s) => data.getValue(r, getDataC(s));
            const setStyle = (r, s, v) => data.setValue(r, getStyleC(s), v);

            // マーカーにかぶさってるマーカーの個数に応じてマーカーサイズを調整
            let neighborPointCount = 0;
            range(0, seriesCount).forEach(s2 => {
              if (s2 != s) {
                if (Math.abs(getData(r, s) - getData(r, s2)) <= MAX_DISTANCE) {
                  neighborPointCount++;
                }
              }
            });

            setStyle(r, s, `line { opacity: 0.5; } point { opacity: 0.5; size: ${neighborPointCount <= 0 ? 0 : (2 + neighborPointCount) * 1}; }`);
          });
        });

        return old.call(this, data, b, c);
      };
      console.log("[jepx_fullscreen_graph] series transparency patch: post");
    }

    // すべての項目にチェックが入っている場合、先頭だけをチェックした状態にする
    {
      console.log("[jepx_fullscreen_graph] uncheck all: pre");
      const areaCard = await retry(1000, 10, () => {
        return Array.from(document.querySelectorAll('.card')).find(card => {
          const header = card.querySelector('.card-header');
          return header && header.innerText == '表示したいエリアを選択してください';
        });
      });
      const checkboxes = Array.from(areaCard.querySelectorAll('.card-body input[type="checkbox"]'));
      const unchecked = checkboxes.map(c => c.checked ? 0 : 1).reduce((a, b) => a + b, 0);
      console.log("[jepx_fullscreen_graph] uncheck all: unchecked = " + unchecked);
      if (unchecked == 0) {
        console.log("[jepx_fullscreen_graph] uncheck all: unckecking");
        for (const [index, checkbox] of checkboxes.entries()) {
          const expected = index == 0 ? true : false;
          const actual = checkbox.checked;
          if (actual != expected) {
            console.log("[jepx_fullscreen_graph] uncheck all: clicking");
            checkbox.click();
            await delay(1000);
          }
        }
      }
      console.log("[jepx_fullscreen_graph] uncheck all: post");
    }

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
