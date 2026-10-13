// @KanbanBroPlugin
// {
//     "name": "co.pplc.kanbanbro.plugins.jepx_fullscreen_graph",
//     "title": "JEPX全画面グラフ",
//     "version": "2.0.0",
//     "description": "JEPXのページでグラフを全画面にし、すべての項目を表示します。"
// }

const enabledIds = ["shikoku", "kyushu", "tokyo", "hokuriku", "tohoku"];

if (window.location.origin + window.location.pathname == 'https://www.jepx.info/spot_free') {
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
    const colors = [
      "#3366CC", "#DC3912", "#FF9900", "#109618", "#990099",
      "#3B3EAC", "#0099C6", "#DD4477", "#66AA00", "#B82E2E",
      "#316395", "#994499", "#22AA99", "#AAAA11", "#6633CC",
      "#E67300", "#8B0707", "#329262", "#5574A6", "#651067"
    ];
    function blend(color, opacity) {
      const r = parseInt(color.substr(1, 2), 16);
      const g = parseInt(color.substr(3, 2), 16);
      const b = parseInt(color.substr(5, 2), 16);
      const r2 = Math.round(r * opacity + 255 * (1 - opacity));
      const g2 = Math.round(g * opacity + 255 * (1 - opacity));
      const b2 = Math.round(b * opacity + 255 * (1 - opacity));
      return `#${r2.toString(16).padStart(2, '0')}${g2.toString(16).padStart(2, '0')}${b2.toString(16).padStart(2, '0')}`;
    }

    // 日付を今日にする
    {
      console.log("[jepx_fullscreen_graph] date patch: pre");

      const params = new URLSearchParams(window.location.search);
      const dateParam = params.get("date");

      let date;
      if (dateParam == null) {
        date = null;
      } else if (dateParam == "today") {
        date = new Date();
      } else if (dateParam == "tomorrow") {
        const now = new Date();
        date = new Date(
          now.getFullYear(),
          now.getMonth(),
          now.getDate() + 1
        );
      } else if (/^\d{4}-\d{2}-\d{2}$/.test(dateParam)) {
        const [year, month, day] = dateParam.split("-").map(Number);
        date = new Date(year, month - 1, day);
      } else {
        throw new Error("Invalid date parameter: " + dateParam);
      }

      if (date != null) {
        date.setHours(0, 0, 0, 0);
        const dateString = `${date.getFullYear()}/${String(date.getMonth() + 1).padStart(2, "0")}/${String(date.getDate()).padStart(2, "0")}`;
        const input = await retry(1000, 10, () => {
          const elements = [...document.querySelectorAll(".card")]
            .filter(card => [...card.querySelectorAll(".card-header .card-title")].some(t => (t.textContent ?? "").includes("表示したい日付を選択してください")))
            .flatMap(card => [...card.querySelectorAll('.card-body input[type="text"]')]);
          if (elements.length == 0) return undefined;
          if (elements.length > 1) throw new Error("Unexpected multiple date input elements: " + elements.length);
          return elements[0];
        });
        // ページ側が読み込みの途中で日付を「今日」から「明日」へ書き換えるため、
        // 一度設定しただけでは上書きされてしまう。
        // そこで、設定した値が 5 秒間保たれることを確認できるまで、設定し直しを繰り返す。
        let stableCount = 0;
        for (let attempt = 0; attempt < 60; attempt++) {
          if (input.value == dateString) {
            stableCount++;
            if (stableCount >= 10) break;
          } else {
            stableCount = 0;
            const old = input.value;
            input.value = dateString;
            input._valueTracker.setValue(old);
            input.dispatchEvent(new Event("input", { bubbles: true, cancelable: true }));
            input.dispatchEvent(new Event("change", { bubbles: true, cancelable: true }));
          }
          await delay(500);
        }
        console.log("[jepx_fullscreen_graph] date patch: post");
      }
    }

    // 凡例パッチ
    {
      console.log("[jepx_fullscreen_graph] legend patch: pre");
      const old = google.visualization.ChartWrapper.prototype.setOptions;
      google.visualization.ChartWrapper.prototype.setOptions = function (options) {
        console.log("setOptions", this, options);

        // 凡例をページングせずに改行させる
        options.legend.maxLines = 2;

        // 凡例の色を薄くするために系列自体の色をブレンド済みの色にする
        if (options.series === undefined) options.series = [];
        range(0, 20).forEach(s => { // 20系列までを決め打ちする
          if (options.series[s] === undefined) options.series[s] = {};
          options.series[s].color = blend(colors[s % 20], 0.5);
        });

        return old.call(this, options);
      };
      console.log("[jepx_fullscreen_graph] legend patch: post");
    }

    // 系列パッチ
    {
      console.log("[jepx_fullscreen_graph] series transparency patch: pre");
      const old = google.visualization.LineChart.prototype.draw;
      google.visualization.LineChart.prototype.draw = function (data, b, c) {
        console.log("draw", this, data, b, c);

        // 既に改変済みもしくは想定外のデータが降ってきた場合は無視
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

            // 凡例のために系列自体の色を変更しているのでここでデフォルトに上書きする
            const color = colors[s % 20];

            setStyle(r, s, `
              line {
                color: ${color};
                opacity: 0.5;
              }
              point {
                color: ${color};
                opacity: 0.5;
                size: ${neighborPointCount <= 0 ? 0 : (2 + neighborPointCount) * 1};
              }
            `);
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
    // React がカードの要素を作り直すため、要素ではなく CSS ルールで指定する
    // グラフ本体には react-google-charts がインラインで height: 400px を付けるので、それも上書きする
    const fullscreenStyle = document.createElement('style');
    fullscreenStyle.textContent = `
      #react_container > div > .card.card-default {
        position: fixed !important;
        top: 0 !important;
        left: 0 !important;
        width: 100vw !important;
        height: 100vh !important;
        box-sizing: border-box !important;
        z-index: 99999 !important;
        display: flex !important;
        flex-direction: column !important;
      }
      #react_container > div > .card.card-default > .card-body {
        flex: 1 1 auto !important;
        min-height: 0 !important;
      }
      #react_container > div > .card.card-default .chart,
      #react_container > div > .card.card-default .chart > div,
      #react_container > div > .card.card-default [id^="reactgooglegraph"] {
        height: 100% !important;
      }
    `;
    document.head.appendChild(fullscreenStyle);
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
    for (const checkbox of Array.from(areaCard.querySelectorAll('.card-body input[type="checkbox"]'))) {
      console.log("[jepx_fullscreen_graph] checkbox = " + checkbox);
      if (checkbox.checked != enabledIds.includes(checkbox.id)) {
        console.log("[jepx_fullscreen_graph] checking");
        checkbox.click();
        count++; // DEBUG
        await delay(1000);
      }
    }
    console.log("[jepx_fullscreen_graph] check all: post");
    if (count == 0) { // DEBUG
      console.log("[jepx_fullscreen_graph] rollback");
      fullscreenStyle.remove();
      document.documentElement.style.overflow = 'scroll';
    }

  })
}
