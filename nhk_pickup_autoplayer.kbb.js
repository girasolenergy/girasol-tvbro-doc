// @KanbanBroPlugin
// {
//   "name": "co.pplc.kanbanbro.plugins.nhk_pickup_autoplayer",
//   "title": "NHK Pickup Autoplayer",
//   "version": "0.0.2",
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
    const video = await retry(1000, 10, () => {
      const video = document.querySelector('#nPlayerContainerAltContentVideoContent > video');
      return video === null ? undefined : video;
    });
    if (video === undefined) {
      console.error('Failed to get video element: `#nPlayerContainerAltContentVideoContent > video`');
      return;
    }
    video.setAttribute('loop', '');
    document.getElementById("nPlayerContainerAltContentVideoContentPosterFrame").click();
  });
}
