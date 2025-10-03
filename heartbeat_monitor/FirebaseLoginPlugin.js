import { getApp, initializeApp, deleteApp } from "https://www.gstatic.com/firebasejs/12.0.0/firebase-app.js";
import { getAuth, GoogleAuthProvider, onAuthStateChanged, signInWithPopup, signOut, EmailAuthProvider, linkWithCredential, signInWithEmailAndPassword } from "https://www.gstatic.com/firebasejs/12.0.0/firebase-auth.js";

const firebaseConfig = {
    apiKey: "AIzaSyDZrghHsrdUM6WN0ArOcIchEqn5y4bBZGk",
    authDomain: "kanbanbro.firebaseapp.com",
    projectId: "kanbanbro",
    storageBucket: "kanbanbro.firebasestorage.app",
    messagingSenderId: "716803851987",
    appId: "1:716803851987:web:0bcd6f8b2e8d7d0151f626",
    measurementId: "G-N3C4HWN670"
};

window.KanbanBro.firebase = {
    getApp,
    getAuth,
    onAuthStateChanged,
};

window.KanbanBro.appsEvent = new EventTarget();
window.KanbanBro.appNames = [];

function setAppNames(appNames) {
    window.KanbanBro.appNames = appNames;
    localStorage.setItem("kanbanbro.appNames", JSON.stringify(appNames));
    window.KanbanBro.appsEvent.dispatchEvent(new Event("appNamesChanged"));
}

export function apply() {

    function registerUserListener(app, closeEvent, fn) {
        const unsubscriber = onAuthStateChanged(getAuth(app), fn);
        closeEvent.addEventListener("close", () => {
            unsubscriber();
        }, { once: true });
        fn();
    }

    function createUserBadge(app, closeEvent) {
        return also(document.createElement("div"), userBadgeDiv => {
            userBadgeDiv.className = "user-badge";
            registerUserListener(app, closeEvent, () => {
                userBadgeDiv.style.display = getAuth(app).currentUser ? null : "none";
            });
            userBadgeDiv.append(
                also(document.createElement("div"), avatarDiv => {
                    avatarDiv.className = "user-avatar";
                    registerUserListener(app, closeEvent, () => {
                        const user = getAuth(app).currentUser;
                        avatarDiv.innerHTML = "";
                        if (user) {
                            avatarDiv.append(
                                also(new Image(), img => {
                                    img.src = user.photoURL;
                                    img.alt = "";
                                    img.referrerPolicy = "no-referrer";
                                }),
                            );
                        }
                    });
                }),
                also(document.createElement("span"), nameSpan => {
                    nameSpan.className = "user-name";
                    registerUserListener(app, closeEvent, () => {
                        const user = getAuth(app).currentUser;
                        nameSpan.textContent = user == null ? "" : user.displayName || user.email || `UID:${user.uid}`;
                    });
                }),
            );
        });
    }

    function openLoginDialog(app) {
        showDialog((container, dialogEvent) => {
            container.append(

                // タイトル
                also(document.createElement("div"), titleDiv => {
                    titleDiv.textContent = "Account";
                    titleDiv.style.fontWeight = "700";
                }),

                // ボタングループ
                also(document.createElement("div"), buttonsDiv => {
                    buttonsDiv.className = "dialog-container";
                    buttonsDiv.append(

                        // Googleログインボタン
                        also(document.createElement("button"), googleButton => {
                            googleButton.type = "button";
                            googleButton.textContent = "Sign in with Google";
                            googleButton.classList.add("auth-button");
                            googleButton.addEventListener("click", async () => {
                                googleButton.disabled = true;
                                try {
                                    const provider = new GoogleAuthProvider();
                                    await signInWithPopup(getAuth(app), provider);
                                    dialogEvent.dispatchEvent(new Event("close"));
                                } catch (e) {
                                    console.error("Sign in failed", e);
                                    let msg = e && e.message ? e.message : String(e);
                                    if (e && e.code === 'auth/popup-blocked') msg = 'Popup was blocked by the browser.';
                                    if (e && e.code === 'auth/popup-closed-by-user') msg = 'Popup closed before completing sign in.';
                                    showToast(`Failed to sign in: ${msg}`);
                                } finally {
                                    googleButton.disabled = false;
                                }
                            });
                            registerUserListener(app, dialogEvent, () => {
                                googleButton.style.display = getAuth(app).currentUser ? "none" : null;
                            });
                        }),

                        // メールアドレスでログインボタン
                        also(document.createElement("button"), emailSignInButton => {
                            emailSignInButton.type = "button";
                            emailSignInButton.textContent = "Sign in with Email / Password";
                            emailSignInButton.classList.add("auth-button");
                            emailSignInButton.addEventListener("click", async () => {
                                emailSignInButton.disabled = true;
                                try {
                                    const email = prompt("Enter email address");
                                    if (!email) return;
                                    const password = prompt("Enter password");
                                    if (!password) return;
                                    await signInWithEmailAndPassword(getAuth(app), email, password);
                                    showToast("Signed in successfully.");
                                    dialogEvent.dispatchEvent(new Event("close"));
                                } catch (e) {
                                    console.error("Email sign in failed", e);
                                    let msg = e && e.message ? e.message : String(e);
                                    if (e && e.code === 'auth/invalid-email') {
                                        msg = 'Invalid email address.';
                                    } else if (e && e.code === 'auth/user-disabled') {
                                        msg = 'This user account has been disabled.';
                                    } else if (e && e.code === 'auth/user-not-found') {
                                        msg = 'No user found with this email.';
                                    } else if (e && e.code === 'auth/wrong-password') {
                                        msg = 'Incorrect password.';
                                    } else if (e && e.code === 'auth/too-many-requests') {
                                        msg = 'Too many attempts. Try again later.';
                                    } else if (e && e.code === 'auth/network-request-failed') {
                                        msg = 'Network error. Please check your connection.';
                                    }
                                    showToast(`Failed to sign in: ${msg}`);
                                } finally {
                                    emailSignInButton.disabled = false;
                                }
                            });
                            registerUserListener(app, dialogEvent, () => {
                                emailSignInButton.style.display = getAuth(app).currentUser ? "none" : null;
                            });
                        }),

                        // メールアドレスをリンクボタン
                        also(document.createElement("button"), linkEmailButton => {
                            linkEmailButton.type = "button";
                            linkEmailButton.textContent = "Link Email / Password";
                            linkEmailButton.classList.add("auth-button");
                            linkEmailButton.addEventListener("click", async () => {
                                const currentUser = getAuth(app).currentUser;
                                if (!currentUser) {
                                    showToast("Please sign in first.");
                                    return;
                                }
                                linkEmailButton.disabled = true;
                                try {
                                    const email = prompt("Enter email address");
                                    if (!email) return;
                                    const password = prompt("Enter password (min 6 chars)");
                                    if (!password) return;
                                    if (password.length < 6) {
                                        showToast("Password must be at least 6 characters.");
                                        return;
                                    }
                                    const credential = EmailAuthProvider.credential(email, password);
                                    await linkWithCredential(currentUser, credential);
                                    showToast("Linked email/password to your account.");
                                    dialogEvent.dispatchEvent(new Event("close"));
                                } catch (e) {
                                    console.error("Link email failed", e);
                                    let msg = e && e.message ? e.message : String(e);
                                    if (e && e.code === 'auth/credential-already-in-use') {
                                        msg = 'This email is already in use by another account.';
                                    } else if (e && e.code === 'auth/provider-already-linked') {
                                        msg = 'Email/password is already linked to this account.';
                                    } else if (e && e.code === 'auth/requires-recent-login') {
                                        msg = 'Please reauthenticate and try again.';
                                    }
                                    showToast(`Failed to link: ${msg}`);
                                } finally {
                                    linkEmailButton.disabled = false;
                                }
                            });
                            registerUserListener(app, dialogEvent, () => {
                                const user = getAuth(app).currentUser;
                                const hasPassword = !!(user && user.providerData && user.providerData.some(p => p && p.providerId === 'password'));
                                linkEmailButton.style.display = user && !hasPassword ? null : "none";
                            });
                        }),

                        // ログアウトボタン
                        also(document.createElement("button"), logoutButton => {
                            logoutButton.type = "button";
                            logoutButton.textContent = "Logout";
                            logoutButton.classList.add("auth-button");
                            logoutButton.addEventListener("click", async () => {
                                logoutButton.disabled = true;
                                try {
                                    await signOut(getAuth(app));
                                    dialogEvent.dispatchEvent(new Event("close"));
                                } catch (e) {
                                    console.error("Sign out failed", e);
                                    showToast(`Failed to sign out: ${e && e.message ? e.message : e}`);
                                } finally {
                                    logoutButton.disabled = false;
                                }
                            });
                            registerUserListener(app, dialogEvent, () => {
                                logoutButton.style.display = getAuth(app).currentUser ? null : "none";
                            });
                        }),

                    );
                }),

                // Closeボタン
                also(document.createElement("div"), actionsDiv => {
                    actionsDiv.style.display = "flex";
                    actionsDiv.style.justifyContent = "end";
                    actionsDiv.style.gap = "8px";
                    actionsDiv.append(
                        also(document.createElement("button"), cancelButton => {
                            cancelButton.type = "button";
                            cancelButton.textContent = "Cancel";
                            cancelButton.classList.add("dialog-button");
                            cancelButton.addEventListener("click", () => dialogEvent.dispatchEvent(new Event("close")));
                        }),
                    );
                }),

            );
        });
    }

    function openAccountsDialog() {
        showDialog((container, dialogEvent) => {
            container.append(

                // タイトル
                also(document.createElement("div"), titleDiv => {
                    titleDiv.textContent = "Accounts";
                    titleDiv.style.fontWeight = "700";
                }),

                // ユーザーリスト
                also(document.createElement("div"), buttonsDiv => {
                    buttonsDiv.className = "dialog-container";
                    function updateButtons() {
                        buttonsDiv.innerHTML = "";
                        window.KanbanBro.appNames.forEach(appName => {
                            const app = getApp(appName);
                            buttonsDiv.append(
                                also(document.createElement("div"), buttonDiv => {
                                    buttonDiv.style.display = "flex";
                                    buttonDiv.style.gap = "12px";
                                    buttonDiv.style.alignItems = "center";
                                    buttonDiv.append(
                                        also(document.createElement("div"), leftDiv => {
                                            leftDiv.style.display = "flex";
                                            leftDiv.style.gap = "12px";
                                            leftDiv.style.alignItems = "center";
                                            leftDiv.append(

                                                // ログインボタン
                                                also(document.createElement("button"), loginButton => {
                                                    loginButton.type = "button";
                                                    loginButton.classList.add("dialog-button");
                                                    loginButton.textContent = "Login";
                                                    loginButton.addEventListener("click", () => openLoginDialog(app));
                                                }),

                                                // ユーザバッジ
                                                createUserBadge(app, dialogEvent),

                                            );
                                        }),
                                        also(document.createElement("div"), rightDiv => {
                                            rightDiv.style.marginLeft = "auto";
                                            rightDiv.append(

                                                // ログアウトボタン
                                                also(document.createElement("button"), removeButton => {
                                                    removeButton.type = "button";
                                                    removeButton.classList.add("dialog-button");
                                                    removeButton.textContent = "🗑️";
                                                    removeButton.addEventListener("click", () => {
                                                        (async () => {
                                                            await signOut(getAuth(app));
                                                            deleteApp(app);
                                                            setAppNames(window.KanbanBro.appNames.filter(appName => appName != app.name));
                                                            window.KanbanBro.appsEvent.dispatchEvent(new CustomEvent("removed", { detail: app }));
                                                        })();
                                                    });
                                                }),

                                            );
                                        }),
                                    );
                                }),
                            );
                        });
                    }
                    window.KanbanBro.appsEvent.addEventListener("appNamesChanged", () => updateButtons());
                    updateButtons();
                }),

                // 追加ボタン
                also(document.createElement("button"), addButton => {
                    addButton.type = "button";
                    addButton.classList.add("dialog-transparent-button");
                    addButton.textContent = "＋";
                    addButton.addEventListener("click", () => {
                        const appName = window.KanbanBro.appNames.includes("[DEFAULT]") ? crypto.randomUUID() : "[DEFAULT]";
                        const app = initializeApp(firebaseConfig, appName);
                        setAppNames([...window.KanbanBro.appNames, appName]);
                        window.KanbanBro.appsEvent.dispatchEvent(new CustomEvent("added", { detail: app }));
                    });
                }),

                // Closeボタン
                also(document.createElement("div"), actionsDiv => {
                    actionsDiv.style.display = "flex";
                    actionsDiv.style.justifyContent = "end";
                    actionsDiv.style.gap = "8px";
                    actionsDiv.append(
                        also(document.createElement("button"), closeButton => {
                            closeButton.type = "button";
                            closeButton.textContent = "Close";
                            closeButton.classList.add("dialog-button");
                            closeButton.addEventListener("click", () => dialogEvent.dispatchEvent(new Event("close")));
                        }),
                    );
                }),

            );
        });
    }

    // トップバー左のAccountsボタン
    document.getElementById("topbar-left-container").append(
        also(document.createElement("div"), container => {
            container.className = "topbar-property";
            container.append(
                also(document.createElement("button"), accountsButton => {
                    accountsButton.type = "button";
                    accountsButton.textContent = "Accounts";
                    accountsButton.addEventListener("click", () => openAccountsDialog());
                }),
            );
        }),
    );

    // トップバー左のアバター
    document.getElementById("topbar-left-container").append(
        also(document.createElement("div"), container => {
            container.className = "topbar-property";

            let closeEvent = null;

            function updateAvatar() {
                if (closeEvent != null) closeEvent.dispatchEvent(new Event("close"));
                closeEvent = new EventTarget();

                container.innerHTML = "";
                window.KanbanBro.appNames.forEach(appName => {
                    const app = getApp(appName);
                    container.append(
                        also(document.createElement("div"), avatarDiv => {
                            avatarDiv.className = "user-avatar";
                            registerUserListener(app, closeEvent, () => {
                                const user = getAuth(app).currentUser;
                                avatarDiv.innerHTML = "";
                                avatarDiv.style.display = user ? null : "none";
                                if (user) {
                                    avatarDiv.append(
                                        also(new Image(), img => {
                                            img.src = user.photoURL;
                                            img.alt = "";
                                            img.referrerPolicy = "no-referrer";
                                        }),
                                    );
                                }
                            });
                        }),
                    );
                });
            }
            window.KanbanBro.appsEvent.addEventListener("appNamesChanged", () => updateAvatar());
            updateAvatar();
        }),
    );

    window.KanbanBro.event.addEventListener("pluginLoaded", () => {
        window.KanbanBro.appNames = JSON.parse(localStorage.getItem("kanbanbro.appNames")) ?? [];
        window.KanbanBro.appNames.forEach(appName => {
            initializeApp(firebaseConfig, appName);
        });
        window.KanbanBro.appNames.forEach(appName => {
            window.KanbanBro.appsEvent.dispatchEvent(new CustomEvent("added", { detail: getApp(appName) }));
        });
        window.KanbanBro.appsEvent.dispatchEvent(new Event("appNamesChanged"));
    });


}
