// @KanbanBroPlugin
// {
//   "name": "co.pplc.kanbanbro.plugins.nhk_pickup_autoplayer",
//   "title": "NHK Pickup Autoplayer",
//   "version": "0.0.3",
//   "description": "NHKのPickup NEWSに以下の機能を追加します。<ul><li>自動再生</li><li>自動ループ</li></ul>"
// }

if (window.location.href == "https://www3.nhk.or.jp/news/pickup_news16/pickup_news_movie.html") {
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
    const video = await retry(1000, 10, () => {
      const video = document.querySelector('#nPlayerContainerAltContentVideoContent > video');
      return video === null ? undefined : video;
    });
    if (video === undefined) {
      console.error('[nhk_pickup_autoplayer] Failed to get video element: `#nPlayerContainerAltContentVideoContent > video`');
      return;
    }
    console.log('[nhk_pickup_autoplayer] video: ' + video);
    video.setAttribute('loop', '');
    await retry(1000, 10, async () => {
      const poster = document.getElementById("nPlayerContainerAltContentVideoContentPosterFrame");
      if (poster === null) {
        console.log('[nhk_pickup_autoplayer] poster = null');
        return null;
      }
      poster.click();
      console.log('[nhk_pickup_autoplayer] clicked');
      await delay(1000);
      if (document.getElementById("nPlayerContainerAltContentVideoContentPosterFrame") !== null) {
        console.log('[nhk_pickup_autoplayer] retry');
        return undefined;
      } else {
        return null;
      }
    });
  });
}
