// @KanbanBroPlugin
// {
//   "name": "co.pplc.kanbanbro.plugins.test_plugin_1",
//   "title": "Test Plugin 1",
//   "version": "0.0.1",
//   "description": "以下の機能が含まれます◆example.comの背景色を変える◆NHK Pickup NEWSの動画を自動再生"
// }

if (window.location.href == "https://example.com/") {
  setInterval(() => {
    document.body.style.backgroundColor = "#" +
      (Math.floor(Math.random() * 20) + 80) +
      (Math.floor(Math.random() * 20) + 80) +
      (Math.floor(Math.random() * 20) + 80);
  }, 1000);
}

if (window.location.href == "https://www3.nhk.or.jp/news/pickup_news16/pickup_news_movie.html") {
  setTimeout(() => {
    nPlayerContainerAltContentVideoContentPosterFrame.click();
  }, 1000);
}
