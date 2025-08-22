export function apply() {
    window.KanbanBro.cardProviders.push(signal => {
        return Array.from({ length: 100 }, (_, index) => (async () => {
            signal.throwIfAborted();
            const number = await window.KanbanBro.dispatcher(async () => {
                const delay = ms => new Promise(r => setTimeout(r, ms));
                signal.throwIfAborted();
                await delay(20 + Math.floor(Math.random() * 120));
                signal.throwIfAborted();
                return index + 1;
            });
            signal.throwIfAborted();
            return {
                image: (() => {
                    return also(new Image(), img => {
                        img.loading = "lazy";
                        img.decoding = "async";
                        function isPrime(n) {
                            if (n < 2) return false;
                            for (let d = 2; d * d <= n; d++) {
                                if (n % d === 0) return false;
                            }
                            return true;
                        }
                        img.src = isPrime(number)
                            ? "https://upload.wikimedia.org/wikipedia/commons/thumb/0/08/Leonardo_da_Vinci_%281452-1519%29_-_The_Last_Supper_%281495-1498%29.jpg/1920px-Leonardo_da_Vinci_%281452-1519%29_-_The_Last_Supper_%281495-1498%29.jpg"
                            : "https://upload.wikimedia.org/wikipedia/commons/2/20/Zeus_Otricoli_Pio-Clementino_Inv257.jpg";
                        img.alt = `${number}番目の画像`;
                    });
                })(),
                alerts: (() => {
                    function createSpan(message) {
                        return also(document.createElement("span"), span => {
                            span.textContent = message;
                        });
                    }
                    const alerts = [];
                    if (number % 3 === 0) alerts.push({ message: createSpan("3の倍数"), level: 2 });
                    if (number % 5 === 0) alerts.push({ message: createSpan("5の倍数"), level: 1 });
                    return alerts;
                })(),
                texts: (() => {
                    function primeFactors(n) {
                        const a = [];
                        let d = 2;
                        while (n > 1) {
                            while (n % d === 0) {
                                a.push(d);
                                n /= d;
                            }
                            d++;
                            if (d * d > n && n > 1) {
                                a.push(n);
                                break;
                            }
                        }
                        return a;
                    }
                    const texts = [];
                    const factors = number === 1 ? [] : primeFactors(number);
                    for (let idx = 0; idx < factors.length; ) {
                        const n = factors[idx];
                        let cnt = 1;
                        while (idx + cnt < factors.length && factors[idx + cnt] === n) {
                            cnt++;
                        }
                        texts.push(also(document.createElement("div"), textDiv => {
                            textDiv.textContent = Array(cnt).fill(String(n)).join("-");
                        }));
                        idx += cnt;
                    }
                    return texts;
                })(),
            };
        })());
    });
}
