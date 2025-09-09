import { initializeApp } from "https://www.gstatic.com/firebasejs/12.0.0/firebase-app.js";
import { getAuth, GoogleAuthProvider, onAuthStateChanged, signInWithPopup, signOut, EmailAuthProvider, linkWithCredential, signInWithEmailAndPassword } from "https://www.gstatic.com/firebasejs/12.0.0/firebase-auth.js";
import { getStorage, ref, listAll, getBytes, getMetadata } from "https://www.gstatic.com/firebasejs/12.0.0/firebase-storage.js";

const firebaseConfig = {
    apiKey: "AIzaSyDZrghHsrdUM6WN0ArOcIchEqn5y4bBZGk",
    authDomain: "kanbanbro.firebaseapp.com",
    projectId: "kanbanbro",
    storageBucket: "kanbanbro.firebasestorage.app",
    messagingSenderId: "716803851987",
    appId: "1:716803851987:web:0bcd6f8b2e8d7d0151f626",
    measurementId: "G-N3C4HWN670"
};

const app = initializeApp(firebaseConfig);
const auth = getAuth(app);
const storage = getStorage(app);

window.KanbanBro.firebase = {
    app,
    auth,
    storage,
    GoogleAuthProvider,
    onAuthStateChanged,
    signInWithPopup,
    signOut,
    EmailAuthProvider,
    linkWithCredential,
    signInWithEmailAndPassword,
    ref,
    listAll,
    getBytes,
    getMetadata,
};

window.KanbanBro.userEvent = new EventTarget();

export function apply() {

    function openLoginDialog() {
        showDialog((container, dialogEvent) => {
            const registerUserListener = fn => {
                const handler = e => fn(e.detail);
                window.KanbanBro.userEvent.addEventListener("changed", handler);
                dialogEvent.addEventListener("close", () => {
                    window.KanbanBro.userEvent.removeEventListener("changed", handler);
                }, { once: true });
                fn(window.KanbanBro.firebase.auth.currentUser);
            };
            container.append(
                also(document.createElement("div"), titleDiv => {
                    titleDiv.textContent = "Account";
                    titleDiv.style.fontWeight = "700";
                }),
                also(document.createElement("div"), buttonsDiv => {
                    buttonsDiv.style.display = "grid";
                    buttonsDiv.style.gap = "8px";
                    buttonsDiv.append(
                        also(document.createElement("button"), googleButton => {
                            googleButton.type = "button";
                            googleButton.textContent = "Sign in with Google";
                            googleButton.classList.add("auth-button");
                            googleButton.addEventListener("click", async () => {
                                googleButton.disabled = true;
                                try {
                                    const provider = new window.KanbanBro.firebase.GoogleAuthProvider();
                                    await window.KanbanBro.firebase.signInWithPopup(window.KanbanBro.firebase.auth, provider);
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
                            registerUserListener(user => {
                                googleButton.style.display = user ? "none" : "inline-block";
                            });
                        }),
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
                                    await window.KanbanBro.firebase.signInWithEmailAndPassword(window.KanbanBro.firebase.auth, email, password);
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
                            registerUserListener(user => {
                                emailSignInButton.style.display = user ? "none" : "inline-block";
                            });
                        }),
                        also(document.createElement("button"), linkEmailButton => {
                            linkEmailButton.type = "button";
                            linkEmailButton.textContent = "Link Email / Password";
                            linkEmailButton.classList.add("auth-button");
                            linkEmailButton.addEventListener("click", async () => {
                                const currentUser = window.KanbanBro.firebase.auth.currentUser;
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
                                    const credential = window.KanbanBro.firebase.EmailAuthProvider.credential(email, password);
                                    await window.KanbanBro.firebase.linkWithCredential(currentUser, credential);
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
                            registerUserListener(user => {
                                const hasPassword = !!(user && user.providerData && user.providerData.some(p => p && p.providerId === 'password'));
                                linkEmailButton.style.display = user && !hasPassword ? "inline-block" : "none";
                            });
                        }),
                        also(document.createElement("button"), logoutButton => {
                            logoutButton.type = "button";
                            logoutButton.textContent = "Logout";
                            logoutButton.classList.add("auth-button");
                            logoutButton.addEventListener("click", async () => {
                                logoutButton.disabled = true;
                                try {
                                    await window.KanbanBro.firebase.signOut(window.KanbanBro.firebase.auth);
                                    dialogEvent.dispatchEvent(new Event("close"));
                                } catch (e) {
                                    console.error("Sign out failed", e);
                                    showToast(`Failed to sign out: ${e && e.message ? e.message : e}`);
                                } finally {
                                    logoutButton.disabled = false;
                                }
                            });
                            registerUserListener(user => {
                                logoutButton.style.display = user ? "inline-block" : "none";
                            });
                        }),
                    );
                }),
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

    document.getElementById("topbar-left-container").append(
        also(document.createElement("div"), container => {
            container.className = "topbar-property";
            container.append(
                also(document.createElement("button"), accountButton => {
                    accountButton.type = "button";
                    accountButton.textContent = "Account";
                    accountButton.style.display = "inline-block";

                    accountButton.addEventListener("click", () => {
                        openLoginDialog();
                    });
                }),
                also(document.createElement("div"), userBadgeDiv => {
                    userBadgeDiv.className = "user-badge";
                    function updateUserBadge(user) {
                        userBadgeDiv.style.display = user ? "inline-flex" : "none";
                    }
                    window.KanbanBro.userEvent.addEventListener("changed", e => {
                        updateUserBadge(e.detail);
                    });
                    updateUserBadge(window.KanbanBro.firebase.auth.currentUser);
                    userBadgeDiv.append(
                        also(document.createElement("div"), avatarDiv => {
                            avatarDiv.className = "user-avatar";
                            function updateAvatar(user) {
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
                            }
                            window.KanbanBro.userEvent.addEventListener("changed", e => {
                                updateAvatar(e.detail);
                            });
                            updateAvatar(window.KanbanBro.firebase.auth.currentUser);
                        }),
                        also(document.createElement("span"), nameSpan => {
                            nameSpan.className = "user-name";
                            function updateUserName(user) {
                                nameSpan.textContent = user == null ? "" : user.displayName || user.email || `UID:${user.uid}`;
                            }
                            window.KanbanBro.userEvent.addEventListener("changed", e => {
                                updateUserName(e.detail);
                            });
                            updateUserName(window.KanbanBro.firebase.auth.currentUser);
                        }),
                    );
                }),
            );
        }),
    );

    window.KanbanBro.firebase.onAuthStateChanged(window.KanbanBro.firebase.auth, user => {
        window.KanbanBro.userEvent.dispatchEvent(new CustomEvent("changed", { detail: user }));
    });
}
