/* global Stomp, SockJS */

const BACKEND_URL = "https://logo-bingo-complete-fgfh.onrender.com";

async function fetchConfig() {
    try {
        const response = await fetch(`${BACKEND_URL}/game/config`);
        if (response.ok) {
            const config = await response.json();
            localStorage.setItem("numberOfCards", config.numberOfCards);
        }
    } catch(e) { console.error(e); }
}

const GameSession = (() => {
    let myId = null, myName = null, stompClient = null;
    
    function login() {
        const tg = window.Telegram?.WebApp;
        tg?.ready();
        const user = tg?.initDataUnsafe?.user;
        if (user && user.id) {
            myId = String(user.id);
            myName = user.first_name || "User";
        } else {
            myId = localStorage.getItem("myId") || "test_" + Math.random();
            myName = "Player";
        }
        localStorage.setItem("myId", myId);
        localStorage.setItem("myName", myName);
    }
    
    async function join() {
        try {
            const response = await fetch(`${BACKEND_URL}/game/join?name=${encodeURIComponent(myName)}&id=${encodeURIComponent(myId)}`, {
                method: "POST"
            });
            if (response.ok) {
                await fetchRoomState();
                connectWebSocket();
            }
        } catch(e) { console.error(e); }
    }
    
    function connectWebSocket() {
        const socket = new SockJS(`${BACKEND_URL}/ws?playerId=` + myId);
        stompClient = Stomp.over(socket);
        stompClient.connect({}, frame => {
            console.log("Connected:", frame);
            subscribeAll();
            refresh();
        }, error => console.error(error));
    }
    
    function refresh() {
        if (stompClient && stompClient.connected) {
            stompClient.send("/app/syncState", {}, JSON.stringify({ playerId: myId, playerName: myName }));
        }
    }
    
    function subscribeAll() {
        stompClient.subscribe('/topic/playroom_transit', message => {
            const payload = JSON.parse(message.body);
            console.log("Playroom transit:", payload);
            if (payload.playerCards) {
                const myCard = payload.playerCards.find(pc => pc.userId == myId);
                if (myCard && window.showPlayroom) window.showPlayroom(myCard.card);
            }
        });
        
        stompClient.subscribe('/topic/lobbyTimer', message => {
            const el = document.getElementById("lobby-timer-value");
            if (el) el.textContent = message.body;
        });
        
        stompClient.subscribe('/topic/draws', message => {
            const draws = JSON.parse(message.body);
            if (window.highlightDraws) window.highlightDraws(draws);
            if (window.highlightUserCard) window.highlightUserCard(draws);
            const callEl = document.getElementById("playroom-call-count");
            if (callEl) callEl.textContent = draws.length;
        });
        
        stompClient.subscribe('/topic/game-over', message => {
            const result = JSON.parse(message.body);
            if (window.announceWinners) window.announceWinners(result);
        });
        
        stompClient.subscribe('/queue/user_wallet-' + myId, message => {
            const data = JSON.parse(message.body);
            const walletEl = document.getElementById("lobby-wallet-value");
            if (walletEl) walletEl.textContent = data;
            if (window.updateWalletDisplay) window.updateWalletDisplay(data);
        });
        
        stompClient.subscribe('/user/queue/state', message => {
            const payload = JSON.parse(message.body);
            if (payload.roomId === "LOBBY" && window.updateLobbyDisplay) {
                window.updateLobbyDisplay(payload);
            } else if (payload.roomId === "PLAYROOM" && window.updatePlayroomDisplay) {
                window.updatePlayroomDisplay(payload);
            }
        });
    }
    
    return { login, join, refresh, getClient: () => stompClient, getId: () => myId, getName: () => myName, reconnect: connectWebSocket };
})();

async function fetchRoomState() {
    try {
        const response = await fetch(`${BACKEND_URL}/game/state?playerId=${GameSession.getId()}&playerName=${GameSession.getName()}`, {
            method: "POST"
        });
        if (response.ok) {
            const payload = await response.json();
            if (payload.roomId === "LOBBY" && window.updateLobbyDisplay) {
                window.updateLobbyDisplay(payload);
            } else if (payload.roomId === "PLAYROOM" && window.updatePlayroomDisplay) {
                window.updatePlayroomDisplay(payload);
            }
        }
    } catch(e) { console.error(e); }
}

window.GameSession = GameSession;
