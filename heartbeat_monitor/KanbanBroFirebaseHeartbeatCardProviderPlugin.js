import { getStorage, ref, listAll, getBytes, getMetadata } from "https://www.gstatic.com/firebasejs/12.0.0/firebase-storage.js";

export function apply() {

    let providers = [];

    async function rebuild() {
        const providers2 = [];
        window.KanbanBro.appNames.forEach(appName => {
            rebuildForApp(window.KanbanBro.firebase.getApp(appName), providers2);
        });
        providers = providers2;
    }

    async function rebuildForApp(app, providers) {
        const user = window.KanbanBro.firebase.getAuth(app).currentUser;

        if (!user) {
            console.warn('No user logged in for app', app.name);
            scheduleUpdate();
            return;
        }

        let files;
        try {
            files = await listAll(ref(getStorage(app), `users/${user.uid}`));
        } catch (e) {
            console.error('Failed to list heartbeat roots', e);
            scheduleUpdate();
            return;
        }

        for (const folderRef of files.prefixes) {
            providers.push(signal => {
                return window.KanbanBro.dispatcher(async () => {
                    signal.throwIfAborted();
                    const imageBytes = await getBytes(ref(folderRef, 'screenshot.png'));
                    signal.throwIfAborted();
                    const settingsBytes = await getBytes(ref(folderRef, 'settings.json'));
                    signal.throwIfAborted();
                    const metadata = await getMetadata(ref(folderRef, 'settings.json'));
                    signal.throwIfAborted();

                    const settings = JSON.parse(new TextDecoder().decode(settingsBytes));
                    const name = settings.settings_code.settings.heartbeat_title || folderRef.name;
                    console.log(`${name}`, settings, metadata);

                    return {
                        keys: {
                            name,
                            updated: Date.parse(metadata.updated),
                        },
                        image: (() => {
                            return also(new Image(), img => {
                                img.loading = 'lazy';
                                img.decoding = 'async';
                                setImageBlob(img, new Blob([imageBytes], { type: 'image/png' }));
                                img.alt = name;
                            });
                        })(),
                        alerts: (() => {
                            function createSpan(message) {
                                return also(document.createElement("span"), span => {
                                    span.textContent = message;
                                });
                            }
                            const alerts = [];
                            if (settings.main_activity_has_focus === false) alerts.push({ message: createSpan("Lost Focus"), level: 2 });
                            if (settings.main_activity_resumed === false) alerts.push({ message: createSpan("Not Resumed"), level: 2 });
                            if (settings.main_activity_ui_active === true) alerts.push({ message: createSpan("UI Opened"), level: 1 });
                            if (Date.now() - Date.parse(metadata.updated) >= 1000 * 60 * 60 * 2) {
                                alerts.push({ message: createSpan("No updates (2 Hours+)"), level: 2 });
                            }
                            return alerts;
                        })(),
                        texts: (() => {
                            const texts = [];
                            texts.push(also(document.createElement('div'), div => {
                                div.className = 'name';
                                div.textContent = name;
                            }));
                            texts.push(also(document.createElement('div'), div => {
                                div.className = 'datetime';
                                div.textContent = new Date(metadata.updated).toLocaleString(undefined, { dateStyle: 'medium', timeStyle: 'medium' });
                                div.title = `${metadata.updated}`;
                            }));
                            return texts;
                        })(),
                        _debug: {
                            appName: app.name,
                            app,
                            user,
                            settings,
                            metadata,
                        },
                    };
                });
            });
        }
        scheduleUpdate();
    }

    window.KanbanBro.cardProviders.push(signal => providers.map(p => p(signal)));

    window.KanbanBro.appsEvent.addEventListener("appNamesChanged", () => rebuild());
    const unsubscribers = {};
    window.KanbanBro.appsEvent.addEventListener("added", e => {
        unsubscribers[e.detail.name] = window.KanbanBro.firebase.onAuthStateChanged(window.KanbanBro.firebase.getAuth(e.detail), () => rebuild());
    });
    window.KanbanBro.appsEvent.addEventListener("removed", e => {
        unsubscribers[e.detail.name]();
    });
    rebuild();

}
