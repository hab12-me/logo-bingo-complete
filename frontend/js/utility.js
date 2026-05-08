/**
 * Utility Functions for Logo Bing Game
 */

/**
 * Generate random ID for fallback users
 */
function getRandomId() {
    const num = Math.floor(Math.random() * 9999999) + 1;
    return num.toString().padStart(7, '0');
}

/**
 * Always highlight the button for the card owned by current player
 */
function alwaysHighlightMyCardButton(locks) {
    const me = Number(localStorage.getItem("myId"));
    const cardId = Object.keys(locks).find(cardId => locks[cardId] === me);
    if (cardId) {
        const button = document.querySelector(`.card-button[data-card-id="${cardId}"]`);
        if (button && !button.classList.contains("locked-by-me")) {
            button.classList.remove("locked-by-other", "available");
            button.classList.add("locked-by-me");
        }
    }
}

/**
 * Highlight cards based on ownership
 */
function highlightCardsOwned(owners) {
    const me = Number(localStorage.getItem("myId"));
    document.querySelectorAll('.card-button').forEach(button => {
        const cardId = button.dataset.cardId;
        const ownedBy = owners[cardId];
        button.classList.remove("locked-by-me", "locked-by-other", "available");
        if (ownedBy === me) {
            button.classList.add("locked-by-me");
            lockedCardId = cardId;
        } else if (ownedBy) {
            button.classList.add("locked-by-other");
        } else {
            button.classList.add("available");
        }
    });
}

/**
 * Highlight a specific card button
 */
function highlightCardButton(cardId, highlightClass) {
    const button = document.querySelector(`.card-button[data-card-id="${cardId}"]`);
    if (!button) return;
    button.classList.remove("locked-by-me", "locked-by-other", "available");
    button.classList.add(highlightClass);
}

/**
 * Update the last 3 called numbers display
 */
function updateLastThreeCalls(calls) {
    const container = document.getElementById("last-3-calls");
    if (!container) return;
    container.innerHTML = "";
    
    const lastThree = calls.slice(-3);
    lastThree.forEach(call => {
        const ball = document.createElement("div");
        ball.classList.add("history-ball");
        let letter = '';
        if(call < 16) {
            letter = 'B';
            ball.classList.add("ball-b");
        } else if(call < 31) {
            letter = 'I';
            ball.classList.add("ball-i");
        } else if(call < 46) {
            letter = 'N';
            ball.classList.add("ball-n");
        } else if(call < 61) {
            letter = 'G';
            ball.classList.add("ball-g");
        } else {
            letter = 'O';
            ball.classList.add("ball-o");
        }
        ball.textContent = `${letter}-${call}`;
        ball.style.animation = "pop 0.3s ease";
        container.appendChild(ball);
    });
    
    setTimeout(() => {
        const balls = container.querySelectorAll('.history-ball');
        balls.forEach(ball => ball.style.animation = "");
    }, 300);
}

/**
 * Highlight drawn numbers on master grid
 */
function highlightDraws(draws) {
    if (!draws || draws.length === 0) return;
    showLastCall(draws[draws.length - 1]);
    updateLastThreeCalls(draws);
    
    for (let i = 1; i <= 75; i++) {
        const cell = document.getElementById(`grid-cell-${i}`);
        if (!cell) continue;
        cell.classList.remove("called-new", "called-old");
        if (draws.includes(i)) {
            if (i === draws[draws.length - 1]) {
                cell.classList.add("called-new");
                cell.style.animation = "pulse 0.5s ease";
                setTimeout(() => { if(cell) cell.style.animation = ""; }, 500);
            } else {
                cell.classList.add("called-old");
            }
        }
    }
}

/**
 * Show the last called number with animation
 */
function showLastCall(number) {
    let callDisplay = '';
    if (number !== null && number !== undefined) {
        let letter = '';
        if(number < 16) letter = 'B';
        else if(number < 31) letter = 'I';
        else if(number < 46) letter = 'N';
        else if(number < 61) letter = 'G';
        else letter = 'O';
        callDisplay = `${letter}-${number}`;
    } else {
        callDisplay = "---";
    }
    
    const lastCall = document.getElementById("last-call");
    if (lastCall) {
        lastCall.textContent = callDisplay;
        lastCall.style.animation = "none";
        lastCall.offsetHeight;
        lastCall.style.animation = "pop 0.3s ease";
        setTimeout(() => { if(lastCall) lastCall.style.animation = ""; }, 300);
    }
}

/**
 * Highlight drawn numbers on player's card
 */
function highlightUserCard(draws) {
    if (!draws) return;
    draws.forEach(num => {
        const cell = document.getElementById(`num-cell-${num}`);
        if (cell && !cell.classList.contains("marked")) {
            cell.classList.add("marked");
            cell.style.animation = "pulse 0.3s ease";
            setTimeout(() => { if(cell) cell.style.animation = ""; }, 300);
        }
    });
}

/**
 * Claim Bingo - Manual claim with verification
 */
let bingoClaimed = false;
let isClaiming = false;

function claimBingo() {
    // Prevent multiple claims
    if (bingoClaimed) {
        showPopupMessage("⚠️ You have already claimed Bingo in this game!");
        return;
    }
    
    if (isClaiming) {
        showPopupMessage("⏳ Please wait, processing your claim...");
        return;
    }
    
    const callCountElement = document.getElementById("playroom-call-count");
    const callCount = Number(callCountElement?.textContent?.trim() || 0);
    
    // Minimum 4 numbers required
    if (callCount < 4) {
        showPopupMessage("⚠️ Wait for at least 4 numbers to be called before claiming Bingo!");
        return;
    }
    
    // Check if game is active
    const statusBox = document.getElementById("playroom-status-box");
    if (statusBox && statusBox.classList.contains("completed")) {
        showPopupMessage("🏆 Game already ended!");
        return;
    }
    
    if (statusBox && statusBox.classList.contains("shuffling")) {
        showPopupMessage("⏳ Game hasn't started yet! Wait for numbers to be called.");
        return;
    }
    
    // Confirm with player
    const confirmClaim = confirm(
        "🎯 CLAIM BINGO?\n\n" +
        "⚠️ WARNING: If your card does NOT have a winning pattern:\n" +
        "• You will be DISMISSED from this game\n" +
        "• You will LOSE your bet\n" +
        "• You cannot play again until next round\n\n" +
        "✅ Make sure you have a complete:\n" +
        "• Row (5 numbers in a horizontal line)\n" +
        "• Column (5 numbers in a vertical line)\n" +
        "• OR Diagonal (5 numbers corner to corner)\n\n" +
        "Are you absolutely sure you have BINGO?"
    );
    
    if (!confirmClaim) {
        return;
    }
    
    isClaiming = true;
    bingoClaimed = true;
    
    const playerId = localStorage.getItem("myId");
    const client = GameSession.getClient();
    
    if (client && client.connected) {
        // Disable button
        const bingoBtn = document.getElementById("bingo-button");
        if (bingoBtn) {
            bingoBtn.disabled = true;
            bingoBtn.style.opacity = "0.6";
            bingoBtn.innerText = "⏳ VERIFYING...";
        }
        
        showProcessingPopup();
        
        // Send claim to server
        client.send("/app/claimBingo", {}, JSON.stringify(playerId));
        console.log("📢 BINGO claim sent for player:", playerId);
        
        // Timeout fallback
        setTimeout(() => {
            if (isClaiming) {
                isClaiming = false;
                hideProcessingPopup();
                if (bingoBtn) {
                    bingoBtn.disabled = false;
                    bingoBtn.style.opacity = "1";
                    bingoBtn.innerText = "🎯 BINGO! 🎯";
                }
                showPopupMessage("⏰ Claim timeout. Please try again or refresh.");
            }
        }, 15000);
        
    } else {
        console.error("Cannot claim - not connected");
        showPopupMessage("Connection error. Please refresh and try again.");
        bingoClaimed = false;
        isClaiming = false;
    }
}

/**
 * Apply dismissal visuals when player makes invalid claim
 */
function applyDismissalVisuals() {
    const card = document.getElementById("playroom-card-container");
    if (card) card.classList.add("dismissed");
    
    const bingoBtn = document.getElementById("bingo-button");
    if (bingoBtn) {
        bingoBtn.disabled = true;
        bingoBtn.style.backgroundColor = "#e74c3c";
        bingoBtn.innerText = "❌ DISMISSED ❌";
        bingoBtn.style.userSelect = "none";
        bingoBtn.style.webkitUserSelect = "none";
        bingoBtn.style.pointerEvents = "none";
    }
    
    bingoClaimed = true;
    isClaiming = false;
    hideProcessingPopup();
}

/**
 * Reset game state
 */
function reset() {
    lockedCardId = null;
    myCardGrid = null;
    isLocked = false;
    lateJoined = false;
    bingoClaimed = false;
    isClaiming = false;
    calledNumbers = [];
    lastLobbyUpdate = 0;
    latestPlayerCount = 0;
    
    // Reset card buttons
    document.querySelectorAll('.card-button').forEach(button => {
        button.classList.remove("locked-by-me", "locked-by-other");
        button.classList.add("available");
    });
    
    // Clear preview
    const cardPreview = document.getElementById("lobby-card-container");
    if (cardPreview) cardPreview.innerHTML = "";
    
    // Reset lobby timer
    const timerEl = document.getElementById('lobby-timer-value');
    if (timerEl) timerEl.textContent = "-";
    
    // Reset player count
    const lobbyPlayerCount = document.getElementById("lobby-player-count");
    if(lobbyPlayerCount) lobbyPlayerCount.textContent = "-";
    
    closeModal();
    showLobby();
    
    // Reset playroom elements
    const countElement = document.getElementById("playroom-players");
    if (countElement) countElement.textContent = "-";
    
    const rewardElement = document.getElementById("playroom-reward");
    if (rewardElement) rewardElement.textContent = "-";
    
    const lastCallEl = document.getElementById("last-call");
    if (lastCallEl) lastCallEl.textContent = "-";
    
    const callCountEl = document.getElementById('playroom-call-count');
    if (callCountEl) callCountEl.textContent = "-";
    
    const lastCallsEl = document.getElementById('last-3-calls');
    if (lastCallsEl) lastCallsEl.innerHTML = "";
    
    const playcard = document.getElementById("playroom-card-container");
    if (playcard) {
        playcard.classList.remove("dismissed");
        playcard.innerHTML = "";
    }
    
    const bingoBtn = document.getElementById("bingo-button");
    if (bingoBtn) {
        bingoBtn.disabled = false;
        bingoBtn.style.backgroundColor = "var(--orange-bg)";
        bingoBtn.style.opacity = "1";
        bingoBtn.innerText = "🎯 BINGO! 🎯";
        bingoBtn.style.pointerEvents = "auto";
    }
    
    const container = document.getElementById("last-3-calls");
    if (container) {
        container.innerHTML = "";
    }
    
    // Clear master grid highlights
    for (let i = 1; i <= 75; i++) {
        const cell = document.getElementById(`grid-cell-${i}`);
        if (cell) {
            cell.classList.remove("called-new", "called-old");
        }
    }
    
    // Refresh wallet display
    if (window.PaymentSystem) {
        window.PaymentSystem.refreshWalletBalance();
    }
}
