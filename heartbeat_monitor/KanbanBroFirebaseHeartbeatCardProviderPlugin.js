export function apply() {

    const providers = [];

    async function rebuild(user) {
        providers.length = 0;

        if (!user) {
            scheduleUpdate();
            return;
        }

        let files;
        try {
            files = await window.KanbanBro.firebase.listAll(window.KanbanBro.firebase.ref(window.KanbanBro.firebase.storage, `users/${user.uid}`));
        } catch (e) {
            console.error('Failed to list heartbeat roots', e);
            scheduleUpdate();
            return;
        }

        for (const folderRef of files.prefixes) {
            providers.push(signal => {
                return window.KanbanBro.dispatcher(async () => {
                    signal.throwIfAborted();
                    const imageBytes = await window.KanbanBro.firebase.getBytes(window.KanbanBro.firebase.ref(folderRef, 'screenshot.png'));
                    signal.throwIfAborted();
                    const settingsBytes = await window.KanbanBro.firebase.getBytes(window.KanbanBro.firebase.ref(folderRef, 'settings.json'));
                    signal.throwIfAborted();
                    const metadata = await window.KanbanBro.firebase.getMetadata(window.KanbanBro.firebase.ref(folderRef, 'settings.json'));
                    signal.throwIfAborted();

                    const settings = JSON.parse(new TextDecoder().decode(settingsBytes));

                    return {
                        image: (() => {
                            return also(new Image(), img => {
                                img.loading = 'lazy';
                                img.decoding = 'async';
                                setImageBlob(img, new Blob([imageBytes], { type: 'image/png' }));
                                img.alt = settings.title ?? folderRef.name;
                            });
                        })(),
                        alerts: (() => {
                            return [];
                        })(),
                        texts: (() => {
                            const texts = [];
                            texts.push(also(document.createElement('div'), d => {
                                d.textContent = settings.title ?? folderRef.name;
                                d.style.fontWeight = '700';
                            }));
                            texts.push(also(document.createElement('div'), d => {
                                d.textContent = `${metadata.updated}`;
                                d.style.fontSize = '12px';
                                d.style.color = '#555';
                            }));
                            return texts;
                        })(),
                    };
                });
            });
        }
        scheduleUpdate();
    }

    window.KanbanBro.cardProviders.push(signal => {
        return providers.map(p => p(signal));
    });

    window.KanbanBro.onUserChangedListeners.push(user => rebuild(user));
    rebuild(window.KanbanBro.firebase.auth.currentUser);

}
