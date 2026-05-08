/* global Stomp, SockJS */

// ============================================
// BACKEND CONFIGURATION - UPDATE THIS URL
// ============================================
const BACKEND_URL = "https://logo-bingo-complete-fgfh.onrender.com";
// ============================================

/**
 * Fetch game configuration from server
 */
async function fetchConfig() {
    try {
        const response = await fetch(`${BACKEND_URL}/game/config`);
        if (response.ok) {
            const config = await response.json();
            console.log("✅ Config received:", config);
            const numberOfCards = config.numberOfCards;
            localStorage.setItem("numberOfCards", numberOfCards);
        } else {
            console.warn("Config fetch failed, using defaults");
            localStorage.setItem("numberOfCards", "500");
        }
    } catch (error) {
        console.error("Config fetch error:", error);
        localStorage.setItem("numberOfCards", "500");
    }
}

/**
 * Connection State Manager
 */
const ConnectionState = {
    DISCONNECTED: "DISCONNECTED",
    CONNECTING: "CONNECTING",
    CONNECTED: "CONNECTED",
    RECONNECTING: "RECONNECTING"
};

let currentState = ConnectionState.DISCONNECTED;

function setState(newState) {
    if (currentState !== newState) {
        console.log(`[Lifecycle] ${currentState} → ${newState}`);
        currentState = newState;
    }
}

/**
 * Game Session Manager
 */
const GameSession = (() => {
    let myId = null;
    let myName = null;
    let stompClient = null;
    let retryDelay = 1000;
    let reconnectTimer = null;
    let playerStatus = "offline";

    /**
     * Login user - get Telegram user data or generate fallback ID
     */
    function login() {
        const tg = window.Telegram?.WebApp;
        tg?.ready();
        const user = tg?.initDataUnsafe?.user;

        if (user && user.id) {
            myId = String(user.id);
            myName = user.first_name?.trim() || user.username || "TG-" + myId;
            console.log("✅ Logged in as Telegram user:", myName, "ID:", myId);
        } else {
            console.log("⚠️ Telegram user not available, using fallback");
            myId = localStorage.getItem("myId");
            if (!myId) {
                myId = getRandomId();
            }
            myName = "Player@" + myId.toString().slice(-4);
            console.log("⚠️ Using fallback identity:", myName, "ID:", myId);
        }
        localStorage.setItem("myId", myId);
        localStorage.setItem("myName", myName);
    }

    /**
     * Join the game session
     */
    async function join() {
        console.log("🚀 Joining Logo Bing game as:", myName, myId);
        try {
            const response = await fetch(
                `${BACKEND_URL}/game/join?name=${encodeURIComponent(myName)}&id=${encodeURIComponent(myId)}`,
                { method: "POST" }
            );
            if (response.ok) {
                console.log("✅ Join successful");
                await fetchRoomState();
                connectWebSocket();
            } else {
                console.error("❌ Join failed:", response.status);
                showPopupMessage("Failed to join game. Please refresh.");
            }
        } catch (error) {
            console.error("Join error:", error);
            showPopupMessage("Network error. Please check your connection.");
        }
    }
    
    /**
     * Connect WebSocket for real-time updates
     */
    function connectWebSocket() {
        setState(ConnectionState.CONNECTING);
        console.log("🔌 Connecting WebSocket to:", BACKEND_URL);
        
        const socket = new SockJS(`${BACKEND_URL}/ws?playerId=` + myId);
        stompClient = Stomp.over(socket);
        stompClient.heartbeat.outgoing = 10000;
        stompClient.heartbeat.incoming = 10000;
        
        stompClient.connect({}, frame => {
            setState(ConnectionState.CONNECTED);
            console.log("✅ WebSocket connected!");
            onConnect(frame);
        }, error => {
            console.error("STOMP error:", error);
            setState(ConnectionState.RECONNECTING);
            onError(error);
        });
        
        socket.onclose = () => {
            console.warn("⚠️ WebSocket closed");
            setState(ConnectionState.RECONNECTING);
            handleDisconnect();
        };
    }

    /**
     * Handle successful WebSocket connection
     */
    async function onConnect(frame) {
        console.log("Connected to server:", frame);
        retryDelay = 1000;
        playerStatus = "active";
        updatePlayerStatus(playerStatus);
        subscribeAll();
        refresh();
    }

    /**
     * Handle WebSocket error
     */
    function onError(error) {
        console.error("STOMP error:", error);
        handleDisconnect();
    }

    /**
     * Handle disconnection and schedule reconnect
     */
    function handleDisconnect() {
        playerStatus = "away";
        updatePlayerStatus(playerStatus);
        scheduleReconnect();
    }

    /**
     * Schedule reconnection with exponential backoff
     */
    function scheduleReconnect() {
        if (reconnectTimer) clearTimeout(reconnectTimer);
        reconnectTimer = setTimeout(() => {
            console.log("🔄 Attempting reconnect...");
            connectWebSocket();
            retryDelay = Math.min(retryDelay * 2, 30000);
        }, retryDelay);
    }
    
    /**
     * Update player status
     */
    function updatePlayerStatus(status) {
        playerStatus = status;
        console.log(`Player status: ${status}`);
    }

    /**
     * Request full state refresh from server
     */
    function refresh() {
        const playerId = localStorage.getItem("myId");
        const playerName = localStorage.getItem("myName");
        if (stompClient && stompClient.connected) {
            console.log("🔄 Syncing state with server...");
            stompClient.send(
                "/app/syncState",
                {},
                JSON.stringify({ playerId, playerName })
            );
        } else {
            console.warn("Cannot sync - not connected");
        }
    }

    /**
     * Public API
     */
    return {
        login,
        join,
        refresh,
        getId: () => myId,
        getName: () => myName,
        getClient: () => stompClient,
        reconnect: connectWebSocket
    };
})();

/**
 * Fetch current room state from server
 */
async function fetchRoomState() {
    const playerId = localStorage.getItem("myId");
    const playerName = localStorage.getItem("myName");
    try {
        const response = await fetch(`${BACKEND_URL}/game/state?playerId=${playerId}&playerName=${encodeURIComponent(playerName)}`, {
            method: "POST"
        });
        if (response.ok) {
            const userPayload = await response.json();
            handleUserPayload(userPayload);
        } else {
            console.error("State fetch failed:", response.status);
        }
    } catch (error) {
        console.error("State fetch error:", error);
    }
}

/**
 * Subscribe to all WebSocket topics
 */
function subscribeAll() {
    const id = GameSession.getId();
    let client = GameSession.getClient();

    // Playroom transition topic
    client.subscribe('/topic/playroom_transit', (message) => {
        const payload = JSON.parse(message.body);
        console.log("📢 Playroom transition:", payload);
        
        const count = payload.activePlayersCount;
        const playersCount = document.getElementById("playroom-players");
        if (playersCount) playersCount.textContent = count;
        
        const reward = payload.rewardAmount;
        const rewardElement = document.getElementById("playroom-reward");
        if (rewardElement) rewardElement.textContent = reward;
        
        const statusBox = document.getElementById("playroom-status-box");
        if (statusBox) {
            if (payload.status === "SHUFFLING") {
                statusBox.className = "shuffling";
                statusBox.innerHTML = '🃏 SHUFFLING <span class="loader"></span>';
            } else if (payload.status === "PLAYING") {
                statusBox.className = "started";
                statusBox.innerHTML = '🎲 GAME STARTED 🎲';
            }
        }
        
        const me = Number(localStorage.getItem("myId"));
        const playerCards = payload.playerCards;
        const myCardPayload = playerCards?.find(pc => pc.userId === me);
        
        if (isAdmin()) {
            const bet = document.getElementById("playroom-bet-value");
            if (bet) bet.textContent = payload.status;
        }

        let card = null;
        if (myCardPayload) {
            card = myCardPayload.card;
            console.log("🎴 My card received:", card.id);
        } else {
            console.log("No card for me in this game");
        }
        showPlayroom(card);
    });
    
    // Admin topic
    client.subscribe('/topic/admin', message => {
        if (isAdmin()) {
            const bet = document.getElementById("lobby-bet-value");
            if (bet) bet.textContent = parseInt(message.body, 10);
        }
    });

    // Lobby timer topic
    client.subscribe('/topic/lobbyTimer', message => {
        const timerVal = parseInt(message.body, 10);
        const timerEl = document.getElementById("lobby-timer-value");
        if (timerEl) timerEl.textContent = timerVal;
    });
    
    // Game paused notification
    client.subscribe('/topic/game-paused', message => {
        const payload = JSON.parse(message.body);
        console.log("⏸️ Game paused:", payload);
        showPopupMessage("⏸️ Bingo claim being verified...");
    });
    
    // Game resumed notification
    client.subscribe('/topic/game-resumed', message => {
        console.log("▶️ Game resumed");
        showPopupMessage("▶️ Game resumed! No winner this time.");
    });
    
    // Invalid claim notification
    client.subscribe('/topic/invalid-claim', message => {
        console.log("⚠️ Invalid claim detected");
    });

    // Card owners topic - for lobby card highlighting
    let lastHighlighUpdate = 0;
    let lastCountUpdate = 0;
    let latestCount = 0;
    let latestLocks = null;
    
    client.subscribe('/topic/cardOwners', message => {
        latestLocks = JSON.parse(message.body);
        latestCount = Object.keys(latestLocks).length;
        const now = Date.now();
        if (now - lastCountUpdate > 900) {
            const countEl = document.getElementById("lobby-player-count");
            if (countEl) countEl.textContent = latestCount;
            lastCountUpdate = now;
        }
        if (now - lastHighlighUpdate > 950) {
            highlightCardsOwned(latestLocks);
            lastHighlighUpdate = now;
        }
        alwaysHighlightMyCardButton(latestLocks);
    });

    // Draws topic - called numbers
    client.subscribe('/topic/draws', message => {
        const draws = JSON.parse(message.body);
        const count = draws.length;
        
        if (count === 1) {
            const statusBox = document.getElementById("playroom-status-box");
            if (statusBox && statusBox.classList.contains("shuffling")) {
                statusBox.className = "started";
                statusBox.innerHTML = '🎲 GAME STARTED 🎲';
            }
        }
        
        const callCountEl = document.getElementById('playroom-call-count');
        if (callCountEl) callCountEl.textContent = count;
        highlightDraws(draws);
        highlightUserCard(draws);
        
        console.log(`🎯 Number called! Total draws: ${count}`);
    });

    // Game over topic
    client.subscribe('/topic/game-over', message => {
        const statusBox = document.getElementById("playroom-status-box");
        if (statusBox) {
            statusBox.className = "completed";
            statusBox.innerHTML = '🏆 GAME OVER 🏆';
        }
        hideProcessingPopup();
        const winResult = JSON.parse(message.body);
        announceWinners(winResult);
        
        bingoClaimed = false;
        isClaiming = false;
        
        const bingoBtn = document.getElementById("bingo-button");
        if (bingoBtn) {
            bingoBtn.disabled = false;
            bingoBtn.style.opacity = "1";
            bingoBtn.innerText = "🎯 BINGO! 🎯";
            bingoBtn.style.pointerEvents = "auto";
        }
    });

    // New game topic
    client.subscribe('/topic/newGame', message => {
        console.log("🔄 New game starting:", message.body);
        reset();
        bingoClaimed = false;
        isClaiming = false;
    });

    // Over timer topic (winner modal countdown)
    client.subscribe('/topic/overTimer', message => {
        const timer = parseInt(message.body, 10);
        if(timer >= 0) {
            const closeBtn = document.getElementById('close-modal-button');
            if (closeBtn) {
                if (timer > 0) {
                    closeBtn.textContent = `Close (${timer})`;
                } else {
                    closeBtn.textContent = "Close";
                }
            }
        }
    });

    // Popup message queue
    client.subscribe('/queue/popup_message-' + id, message => {
        showPopup(message.body);
    });

    // Dismissed queue
    client.subscribe('/queue/dismissed-' + id, () => {
        applyDismissalVisuals();
        showPopupMessage("❌ INVALID BINGO! You have been DISMISSED from this game!");
        isClaiming = false;
    });

    // Wallet update queue
    client.subscribe('/queue/user_wallet-' + id, message => {
        try {
            const walletData = JSON.parse(message.body);
            updateWalletDisplay(walletData);
            if (window.PaymentSystem) {
                window.PaymentSystem.refreshWalletBalance();
            }
        } catch (e) {
            console.error("Failed to parse wallet:", message.body, e);
        }
    });

    // Victory queue
    client.subscribe('/queue/victory-' + id, message => {
        try {
            const data = JSON.parse(message.body);
            console.log("🏆 VICTORY!", data);
            showPopupMessage(`🎉 ${data.message || "Congratulations! You won!"} 🎉`);
            hideProcessingPopup();
            
            if (window.confetti) {
                window.confetti({ particleCount: 200, spread: 100, origin: { y: 0.6 } });
                setTimeout(() => {
                    window.confetti({ particleCount: 150, spread: 70, origin: { y: 0.6, x: 0.2 } });
                    window.confetti({ particleCount: 150, spread: 70, origin: { y: 0.6, x: 0.8 } });
                }, 200);
            }
            
            if (data.newBalance) {
                updateWalletDisplay(data.newBalance);
                if (window.PaymentSystem) {
                    window.PaymentSystem.refreshWalletBalance();
                }
            }
            
            bingoClaimed = true;
            isClaiming = false;
        } catch (e) {
            console.error("Victory parse error:", e);
            hideProcessingPopup();
        }
    });

    // User state queue
    client.subscribe('/user/queue/state', message => {
        const payload = JSON.parse(message.body);
        if (payload.error) {
            console.error("Backend error:", payload.error);
            return;
        }
        handleUserPayload(payload);
    });
}

let currentRoomState = null;

/**
 * Handle user payload from server
 */
function handleUserPayload(room) {
    currentRoomState = room;
    if (!room || !room.roomId) {
        console.warn("Invalid room payload:", room);
        return;
    }
    reset();
    switch (room.roomId) {
        case "LOBBY":
            updateLobbyDisplay(room);
            break;
        case "PLAYROOM":
            updatePlayroomDisplay(room);
            break;
        default:
            console.warn("Unknown room:", room.roomId);
            break;
    }
}

/**
 * Check if current user is admin
 */
function isAdmin() {
    const id = localStorage.getItem("myId");
    const adminIds = ["1765057062", "1044688332", "6499874707"];
    return adminIds.includes(id);
}

/**
 * Show popup message helper
 */
function showPopupMessage(message) {
    showPopup(message);
}
