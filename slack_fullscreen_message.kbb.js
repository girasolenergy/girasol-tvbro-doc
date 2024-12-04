// @KanbanBroPlugin
// {
//     "name": "co.pplc.kanbanbro.plugins.slack_fullscreen_message",
//     "title": "Slack全画面メッセージ",
//     "version": "1.2.0",
//     "description": "Slackのメッセージ領域を全画面に表示します。"
// }

if (location.href.startsWith('https://app.slack.com/')) {
    async function delay(wait) {
        return new Promise(callback => setTimeout(() => callback(), wait));
    }
    function error(message) {
        const warningBanner = document.createElement('div');
        warningBanner.style.position = 'fixed';
        warningBanner.style.top = '0';
        warningBanner.style.left = '0';
        warningBanner.style.width = '100%';
        warningBanner.style.backgroundColor = 'rgba(255, 128, 0, 0.8)';
        warningBanner.style.color = 'white';
        warningBanner.style.padding = '10px';
        warningBanner.style.zIndex = '1000';
        warningBanner.style.fontSize = '1.5em';
        warningBanner.style.textAlign = 'center';
        warningBanner.textContent = message;
        document.body.appendChild(warningBanner);
        setTimeout(() => {
            warningBanner.parentNode.removeChild(warningBanner);
        }, 10000);

        throw new Error(message);
    }
    new Promise(async () => {

        // メッセージ領域を取得
        const messagePane = await new Promise(async callback => {
            for (let t = 0; t < 100; t++) {
                if (t > 0) await delay(100);

                const messagePanes = Array.from(document.querySelectorAll('.p-message_pane'));
                if (messagePanes.length == 1) {
                    callback(messagePanes[0]);
                    return;
                }

            }
            error('[SFM]メッセージ領域を取得できませんでした。');
        });

        await delay(10000);

        // 一旦body配下の全要素を非表示
        document.querySelectorAll('body > *').forEach(element => {
            element.style.display = 'none';
        });

        // メッセージ領域をbody直下に移動
        document.body.appendChild(messagePane);

    });
}
