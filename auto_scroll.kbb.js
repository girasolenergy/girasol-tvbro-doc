// @KanbanBroPlugin
// {
//   "name": "co.pplc.kanbanbro.plugins.auto_scroll",
//   "title": "自動スクロール",
//   "version": "0.0.2",
//   "description": "ページ最下部にゆっくりスクロールし、最上部に戻ることを繰り返します。"
// }

if (window.location.href.match(/()/)) {
    function smoothScrollWithinElement(element, targetRatio, pixelsPerSecond) {
        return new Promise(resolve => {
            const maxPixels = element.scrollHeight - element.clientHeight;
            const targetPixels = maxPixels * targetRatio;
            let lastMilliseconds = performance.now();
            let totalDeltaPixels = 0;

            const animationStep = currentMilliseconds => {

                const deltaMilliseconds = Math.max(currentMilliseconds - lastMilliseconds, 0);
                lastMilliseconds = currentMilliseconds;

                const lastTotalDeltaPixels = totalDeltaPixels;
                totalDeltaPixels += pixelsPerSecond * (deltaMilliseconds / 1000);
                const deltaPixels = Math.round(totalDeltaPixels) - Math.round(lastTotalDeltaPixels);

                const remainingPixels = targetPixels - element.scrollTop;

                if (Math.abs(remainingPixels) <= deltaPixels) {
                    element.scrollTop = targetPixels;
                    resolve(null);
                } else {
                    element.scrollTop += remainingPixels > 0 ? deltaPixels : -deltaPixels;
                    requestAnimationFrame(animationStep);
                }
            };

            requestAnimationFrame(animationStep);
        });
    }

    new Promise(async () => {
        while (true) {
            await new Promise(resolve => setTimeout(resolve, 5000));
            await smoothScrollWithinElement(document.documentElement, 1.0, 20);
            await new Promise(resolve => setTimeout(resolve, 5000));
            document.documentElement.scrollTop = 0;
        }
    });
}
