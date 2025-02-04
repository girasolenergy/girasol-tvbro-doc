// @KanbanBroPlugin
// {
//     "name": "co.pplc.kanbanbro.plugins.slack_fullscreen_message",
//     "title": "Slack全画面メッセージ",
//     "version": "1.4.2",
//     "description": "Slackのメッセージ領域を全画面に表示します。"
// }

if (location.href.startsWith('https://app.slack.com/')) {
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
        function hideSiblingsAndAncestorsSiblings(element) {
            let current = element;
            while (current) {
                const siblings = Array.from(current.parentNode.children).filter(it => it !== current);
                siblings.forEach(it => {
                    it.style.display = 'none';
                });
                current = current.parentNode;
            }
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

        // メッセージ領域を取得
        const messagePane = await retry(1000, 10, () => {
            const messagePanes = Array.from(document.querySelectorAll('.p-message_pane'));
            if (messagePanes.length != 1) return undefined;
            return messagePanes[0];
        });
        if (messagePane === undefined) {
            error('[SFM]メッセージ領域を取得できませんでした。');
            return;
        }

        // 全画面に表示する
        messagePane.style.position = 'fixed';
        messagePane.style.top = '0';
        messagePane.style.left = '0';
        messagePane.style.width = '100vw';
        messagePane.style.height = '100vh';

        // 余計なUI要素を非表示にする
        hideSiblingsAndAncestorsSiblings(messagePane);

    });
}
