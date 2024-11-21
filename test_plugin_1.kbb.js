// @KanbanBroPlugin
// {
//   "name": "co.pplc.kanbanbro.plugins.test_plugin_1",
//   "title": "Test Plugin 1",
//   "version": "0.0.1",
//   "description": "example.comの背景色を変えます"
// }

if (window.location.href == "https://example.com/") {
  setInterval(() => {
    document.body.style.backgroundColor = "#" +
      (Math.floor(Math.random() * 20) + 80) +
      (Math.floor(Math.random() * 20) + 80) +
      (Math.floor(Math.random() * 20) + 80);
  }, 1000);
}
