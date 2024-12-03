// @KanbanBroPlugin
// {
//   "name": "co.pplc.kanbanbro.plugins.nhk_pickup_autoplayer",
//   "title": "NHK Pickup Autoplayer",
//   "version": "0.0.1",
//   "description": "NHKのPickup NEWSに以下の機能を追加します。<ul><li>自動再生</li><li>自動ループ</li></ul>"
// }

if (window.location.href == "https://www3.nhk.or.jp/news/pickup_news16/pickup_news_movie.html") {
  setTimeout(() => {
    const video = document.querySelector('#nPlayerContainerAltContentVideoContent > video');
    video.setAttribute('loop', '');
    document.getElementById("nPlayerContainerAltContentVideoContentPosterFrame").click();
  }, 1000);
}
