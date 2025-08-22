import { initializeApp } from "https://www.gstatic.com/firebasejs/12.0.0/firebase-app.js";
import { getAuth, GoogleAuthProvider, onAuthStateChanged, signInWithPopup, signOut } from "https://www.gstatic.com/firebasejs/12.0.0/firebase-auth.js";
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
    ref,
    listAll,
    getBytes,
    getMetadata,
};

window.KanbanBro.onUserChangedListeners = [];

export function apply() {
    document.getElementById("topbar-left-container").append(
        also(document.createElement("div"), container => {
            container.className = "topbar-property";
            container.append(
                also(document.createElement("button"), signInButton => {
                    signInButton.type = "button";
                    signInButton.textContent = "Sign in";
                    signInButton.style.display = "inline-block";

                    const provider = new window.KanbanBro.firebase.GoogleAuthProvider();
                    signInButton.addEventListener("click", async () => {
                        try {
                            await window.KanbanBro.firebase.signInWithPopup(window.KanbanBro.firebase.auth, provider);
                        } catch (e) {
                            console.error("Sign in failed", e);
                        }
                    });
                    window.KanbanBro.onUserChangedListeners.push(user => {
                        signInButton.style.display = user ? "none" : "inline-block";
                    });
                }),
                also(document.createElement("button"), signOutButton => {
                    signOutButton.type = "button";
                    signOutButton.textContent = "Sign out";
                    signOutButton.style.display = "none";

                    signOutButton.addEventListener("click", async () => {
                        try {
                            await window.KanbanBro.firebase.signOut(window.KanbanBro.firebase.auth);
                        } catch (e) {
                            console.error("Sign out failed", e);
                        }
                    });
                    window.KanbanBro.onUserChangedListeners.push(user => {
                        signOutButton.style.display = user ? "inline-block" : "none";
                    });
                }),
                also(document.createElement("div"), userBadgeDiv => {
                    userBadgeDiv.className = "user-badge";
                    userBadgeDiv.style.display = "none";
                    window.KanbanBro.onUserChangedListeners.push(user => {
                        userBadgeDiv.style.display = user ? "inline-flex" : "none";
                    });
                    userBadgeDiv.append(
                        also(document.createElement("div"), avatarDiv => {
                            avatarDiv.className = "user-avatar";
                            window.KanbanBro.onUserChangedListeners.push(user => {
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
                            window.KanbanBro.onUserChangedListeners.push(user => {
                                nameSpan.textContent = user == null ? "" : user.displayName || user.email || `UID:${user.uid}`;
                            });
                        }),
                    );
                }),
            );
        }),
    );

    window.KanbanBro.firebase.onAuthStateChanged(window.KanbanBro.firebase.auth, user => {
        for (const listener of window.KanbanBro.onUserChangedListeners) {
            try {
                listener(user);
            } catch (e) {
                console.error(e);
            }
        }
    });
}
