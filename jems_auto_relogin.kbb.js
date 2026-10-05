// @KanbanBroPlugin
// {
//     "name": "co.pplc.kanbanbro.plugins.jems_auto_relogin",
//     "title": "J-EMS自動再ログイン",
//     "version": "1.0.0",
//     "description": "J-EMSのCloudflare Accessログインページで、1分おきにGoogle Workspaceのボタンをクリックし、認証切れから自動で復帰します。"
// }

if (window.location.origin + window.location.pathname == 'https://girasolenergy.cloudflareaccess.com/cdn-cgi/access/login/app.j-ems.co.jp') {
  function delay(wait) {
    return new Promise(callback => setTimeout(() => callback(), wait));
  }
  (async () => {
    while (true) {
      await delay(60000);

      // Google Workspaceのボタンが存在すればクリックする
      const button = document.getElementById("idp1");
      if (button !== null) {
        button.click();
        console.log("[jems_auto_relogin] clicked");
      }
    }
  })();
}
