/**
 * Grid Master - Manages Bingo Cards and Game Boards
 */

let lockCount = 0;

/**
 * Show lobby screen
 */
function showLobby() {
    hidePlayroom();
    const lobby = document.getElementById("lobby");
    if (lobby) {
        lobby.classList.remove("hidden");
        lobby.style.display = "block";
    }
    const lobbyHeader = document.getElementById("lobby-header");
    if (lobbyHeader) lobbyHeader.style.display = "flex";
    buildCardButtonsGrid();
}
    
/**
 * Show playroom screen
 */
function showPlayroom(card) {
    hideLobby();
    buildMasterGrid();
    showPlaycard(card);
    const playroom = document.getElementById("playroom");
    if (playroom) {
        playroom.classList.remove("hidden");
        playroom.style.display = "block";
    }
}

/**
 * Hide playroom screen
 */
function hidePlayroom() {
    const playroom = document.getElementById("playroom");
    if (playroom) playroom.classList.add("hidden");
}
    
/**
 * Hide lobby screen
 */
function hideLobby() {
    const lobby = document.getElementById("lobby");
    if (lobby) lobby.classList.add("hidden");
}
    
let myCardGrid = null;

/**
 * Display player's bingo card
 */
function showPlaycard(card) {
    const bingoCardContainer = document.getElementById("playroom-card-container");
    if (!bingoCardContainer) return;
    bingoCardContainer.innerHTML = "";
    const bingoCard = document.createElement("div");
    const footer = document.getElementById("playroom-footer");
    
    if(card) {
        let grid;
        if (typeof card.grid === 'string') {
            try {
                grid = JSON.parse(card.grid);
            } catch(e) {
                grid = card.grid;
            }
        } else {
            grid = card.grid;
        }
        
        myCardGrid = grid;
        const id = card.id;
        lockedCardId = card.id;
        
        bingoCard.className = 'bingo-card-grid';
        
        const colors = {
            B: '#2ecc71',
            I: '#f39c12',
            N: '#3498db',
            G: '#e74c3c',
            O: '#9b59b6'
        };
        
        ["B","I","N","G","O"].forEach(letter => {
            const headerCell = document.createElement('div');
            headerCell.className = 'header-cell';
            headerCell.textContent = letter;
            headerCell.style.backgroundColor = colors[letter];
            bingoCard.appendChild(headerCell);
        });
        
        for(let row = 0; row < 5; row++) {
            for(let col = 0; col < 5; col++) {
                let num = grid[row][col];
                const cell = document.createElement("div");
                if (num !== 0) {
                    cell.className = "num-cell";
                    cell.textContent = num;
                    cell.id = `num-cell-${num}`;
                    cell.onclick = () => {
                        cell.classList.toggle("marked");
                    };
                } else {
                    cell.className = "free-cell";
                    cell.textContent = '⭐';
                }
                bingoCard.appendChild(cell);
            }
        }
        
        bingoCardContainer.appendChild(bingoCard);
        
        const cardIdDiv = document.createElement('div');
        cardIdDiv.className = "bingo-card-id";
        cardIdDiv.textContent = '🎴 Board #: ' + id;
        bingoCardContainer.appendChild(cardIdDiv);
        
        const bingoButton = document.getElementById("bingo-button");
        if (bingoButton) {
            bingoButton.removeEventListener("click", claimBingo);
            bingoButton.addEventListener("click", claimBingo);
            bingoButton.style.userSelect = "auto";
            bingoButton.style.webkitUserSelect = "auto";
            bingoButton.style.pointerEvents = "auto";
            bingoButton.disabled = false;
            bingoButton.style.backgroundColor = "var(--orange-bg)";
            bingoButton.innerText = "🎯 BINGO! 🎯";
        }
        
        if (footer) footer.style.display = "block";
    } else {
        bingoCard.classList.add("empty-card");
        const playerName = localStorage.getItem("myName") || "Player";
        
        const greeting = document.createElement("div");
        greeting.textContent = `👋 Hello, ${playerName}!`;
        greeting.classList.add("greeting-line");
        bingoCard.appendChild(greeting);
        
        const message = document.createElement("div");
        message.textContent = "⏳ Waiting for next game to start...";
        bingoCard.appendChild(message);
        
        const subMessage = document.createElement("div");
        subMessage.textContent = "Select a card in the lobby to play!";
        subMessage.style.fontSize = "0.8em";
        subMessage.style.marginTop = "10px";
        subMessage.style.opacity = "0.8";
        bingoCard.appendChild(subMessage);
        
        bingoCardContainer.appendChild(bingoCard);
        if (footer) footer.style.display = "none";
        
        const bingoButton = document.getElementById("bingo-button");
        if (bingoButton) {
            bingoButton.disabled = true;
            bingoButton.style.backgroundColor = "#95a5a6";
            bingoButton.innerText = "⏳ WAITING...";
        }
    }
}
    
/**
 * Update wallet display
 */
function updateWalletDisplay(balance) {
    const walletElement = document.getElementById("lobby-wallet-value");
    if (walletElement) {
        let displayBalance = typeof balance === 'object' ? (balance.balance || balance) : balance;
        if (typeof displayBalance === 'number') {
            walletElement.textContent = displayBalance.toFixed(2);
        } else {
            walletElement.textContent = displayBalance;
        }
    }
}
    
/**
 * Update lobby display with payload data
 */
function updateLobbyDisplay(payload) {
    showLobby();
    
    const timerElement = document.getElementById("lobby-timer-value");
    if (payload.lobbyTimer && timerElement) {
        timerElement.textContent = payload.lobbyTimer;
    }
    
    if (payload.wallet) {
        updateWalletDisplay(payload.wallet);
    } else {
        updateWalletDisplay("0");
    }
    
    if (payload.cardOwners) {
        highlightCardsOwned(payload.cardOwners);
        const count = Object.keys(payload.cardOwners).length;
        const countEl = document.getElementById("lobby-player-count");
        if (countEl) countEl.textContent = count;
    }
    
    if (payload.user && payload.user.card) {
        lockedCardId = payload.user.card.id;
        isLocked = true;
        renderLobbyCard(payload.user.card);
    }
    
    if(isAdmin()) {
        const betEl = document.getElementById("lobby-bet-value");
        if (betEl) betEl.textContent = payload.gameRound;
    }
}

/**
 * Update playroom display with payload data
 */
function updatePlayroomDisplay(payload) {
    const countElement = document.getElementById("playroom-players");
    if (countElement) countElement.textContent = payload.playerCount;
    
    const rewardElement = document.getElementById("playroom-reward");
    if (rewardElement) rewardElement.textContent = payload.reward;
    
    const statusBox = document.getElementById("playroom-status-box");
    if (statusBox && payload.status === "PLAYING") {
        statusBox.className = "started";
        statusBox.innerHTML = '🎲 GAME STARTED 🎲';
    }
    
    if (isAdmin()) {
        const bet = document.getElementById("playroom-bet-value");
        if (bet) bet.textContent = payload.status;
    }
    
    let card = null;
    if (payload.user && payload.user.card) {
        card = payload.user.card;
    }
    
    showPlayroom(card);
    
    if (card && payload.user && payload.user.dismissed) {
        console.log("Player is dismissed in this game");
        applyDismissalVisuals();
    }
    
    const draws = payload.draws || [];
    highlightDraws(draws);
    highlightUserCard(draws);
    
    const callCountEl = document.getElementById("playroom-call-count");
    if (callCountEl) callCountEl.textContent = draws.length;
    
    const winResult = payload.winResult;
    if (winResult && winResult.winners && winResult.winners.length > 0) {
        announceWinners(winResult);
        const remaining = payload.overTimer;
        if(remaining === 0) {
            closeModal();
            reset();
            showLobby();
        }
    }
}

/**
 * Build card buttons grid in lobby
 */
function buildCardButtonsGrid() {
    const numberOfCards = parseInt(localStorage.getItem("numberOfCards"), 10) || 500;
    const cardContainer = document.getElementById("lobby-card-ids-container");
    if (!cardContainer) return;
    
    cardContainer.innerHTML = "";
    cardContainer.scrollTop = 0;
    
    for (let id = 1; id <= numberOfCards; id++) {
        const btn = document.createElement("button");
        btn.className = "card-button available";
        btn.id = "card-" + id;
        btn.textContent = id;
        btn.setAttribute("data-card-id", String(id));
        let isProcessing = false;
        
        btn.addEventListener("click", async () => {
            if (isProcessing) return;
            isProcessing = true;
            
            const cardId = btn.dataset.cardId;
            const myId = localStorage.getItem("myId");
            
            try {
                const response = await fetch(`${BACKEND_URL}/game/lockCard?cardId=${encodeURIComponent(cardId)}`, {
                    method: "POST",
                    headers: { "User-Id": myId }
                });
                const data = await response.json();
                
                if (data.status === "success") {
                    if(data.data) {
                        const card = data.data;
                        const message = data.message;
                        if(message === "Card locked") {
                            lockedCardId = card.id;
                            highlightCardButton(String(card.id), "locked-by-me");
                            renderLobbyCard(card);
                            showPopupMessage(`✅ Card ${card.id} locked successfully!`);
                        } else if(message === "Card changed") {
                            if (lockedCardId) highlightCardButton(lockedCardId, "available");
                            lockedCardId = card.id;
                            highlightCardButton(String(card.id), "locked-by-me");
                            renderLobbyCard(card);
                            showPopupMessage(`🔄 Switched to card ${card.id}`);
                        } 
                    } else {
                        if (lockedCardId) highlightCardButton(lockedCardId, "available");
                        lockedCardId = null;
                        renderLobbyCard(null);
                        showPopupMessage(`🔓 Card unlocked`);
                    }
                } else {
                    showPopupMessage(data.message || "Failed to lock card");
                }
                
                if (data.wallet) updateWalletDisplay(data.wallet);
                
            } catch (err) {
                console.error("Lock card error:", err);
                showPopupMessage("Network error, please try again.");
            } finally {
                isProcessing = false;
            }
        });
        
        cardContainer.appendChild(btn);
    }
}
    
/**
 * Render selected card in lobby preview
 */
function renderLobbyCard(card) {
    const container = document.getElementById("lobby-card-container");
    if (!container) return;
    container.innerHTML = "";
    
    if(card) {
        let grid;
        if (typeof card.grid === 'string') {
            try {
                grid = JSON.parse(card.grid);
            } catch(e) {
                grid = card.grid;
            }
        } else {
            grid = card.grid;
        }
        
        const table = document.createElement("div");
        table.className = "lobby-table";
        
        for (let row = 0; row < 5; row++) {
            for (let col = 0; col < 5; col++) {
                const td = document.createElement("div");
                const value = grid[row][col];
                td.textContent = value === 0 ? "⭐" : value;
                td.className = "lobby-table-cell";
                table.appendChild(td);
            }
        }
        container.appendChild(table);
        
        const cardIdDiv = document.createElement('div');
        cardIdDiv.className = "lobby-card-id";
        cardIdDiv.textContent = '🎴 Board #: ' + card.id;
        container.appendChild(cardIdDiv);
    }
}
    
/**
 * Clear lobby card preview
 */
function clearLobbyCard() {
    const preview = document.getElementById("lobby-card-container");
    if (preview) preview.innerHTML = "";
}

/**
 * Build master grid (1-75 numbers board)
 */
function buildMasterGrid() {
    const masterGrid = document.getElementById("master-grid");
    if (!masterGrid) {
        console.warn("master-grid element not found!");
        return;
    }
    
    masterGrid.innerHTML = "";
    for (let row = 1; row <= 15; row++) {
        const masterRow = document.createElement("div");
        masterRow.className = "master-row";
        let number = row;
        for (let col = 0; col < 5; col++) {
            const cell = document.createElement("div");
            cell.className = "master-cell";
            cell.id = `grid-cell-${number}`;
            cell.textContent = number;
            masterRow.appendChild(cell);
            number += 15;
        }
        masterGrid.appendChild(masterRow);
    }
}

/**
 * Show popup notification
 */
function showPopup(message) {
    const duration = 3500;
    const popup = document.getElementById("game-popup");
    const msg = document.getElementById("popup-message");
    const bar = document.getElementById("popup-bar");
    
    if (!popup || !msg || !bar) return;
    
    msg.textContent = message;
    popup.style.display = "block";
    
    bar.style.animation = "none";
    bar.offsetHeight;
    bar.style.animation = `slideBar ${duration/1000}s linear forwards`;
    
    setTimeout(() => {
        popup.style.display = "none";
    }, duration);
}

/**
 * Announce winners with modal
 */
function announceWinners(winResult) {
    const drawnNumbers = winResult.drawnNumbers || [];
    const winners = (winResult.winners || []).map(winner => ({
        playerName: winner.playerName,
        cardId: winner.card.id,
        card: winner.card.grid || winner.card,
        winningNumbers: winner.winningNumbers || [],
        drawnNumbers: drawnNumbers,
        winningAmount: winner.winningAmount || 0
    }));
    
    const modal = document.getElementById('winnerModal');
    const winnerContainer = document.getElementById('winners-container');
    const closeBtn = document.getElementById('close-modal-button');
    
    if (!modal || !winnerContainer) return;
    
    winnerContainer.innerHTML = "";
    winnerContainer.scrollTop = 0;
    
    if (winners.length === 0) {
        const noWinnerDiv = document.createElement('div');
        noWinnerDiv.className = 'winner-section';
        noWinnerDiv.innerHTML = '<div style="text-align:center; padding:20px;">😔 No winner this round! Better luck next time!</div>';
        winnerContainer.appendChild(noWinnerDiv);
    } else {
        winners.forEach(w => {
            const section = document.createElement('div');
            section.className = 'winner-section';
            
            const nameEl = document.createElement('div');
            nameEl.className = 'winner-name';
            nameEl.innerHTML = `🎉 ${w.playerName} won ${w.winningAmount.toFixed(2)} ETB! 🎉`;
            section.appendChild(nameEl);
            
            const cardGrid = generateCard(w.card, w.winningNumbers || [], w.drawnNumbers || []);
            section.appendChild(cardGrid);
            
            const cardIdEl = document.createElement("div");
            cardIdEl.className = "winner-card-id";
            cardIdEl.textContent = `🎴 Board #: ${w.cardId}`;
            section.appendChild(cardIdEl);
            
            winnerContainer.appendChild(section);
        });
    }
    
    updateWinnerSummary(winners);
    modal.classList.add('show');
    modal.setAttribute('aria-hidden','false');
    
    if (window.confetti && winners.length > 0) {
        window.confetti({ particleCount: 200, spread: 100, origin: { y: 0.6 } });
        setTimeout(() => {
            window.confetti({ particleCount: 150, spread: 70, origin: { y: 0.6, x: 0.2 } });
            window.confetti({ particleCount: 150, spread: 70, origin: { y: 0.6, x: 0.8 } });
        }, 200);
    }
}

/**
 * Update winner summary text
 */
function updateWinnerSummary(winners) {
    const summaryEl = document.getElementById("winner-summary");
    if (!summaryEl) return;
    
    if (winners.length === 0) {
        summaryEl.innerHTML = "😔 No winners this round!";
        return;
    }
    
    const styledNames = winners.map(w => {
        if (w.cardId === lockedCardId) {
            return `<span class="winner-name-highlight">You (${w.winningAmount.toFixed(2)} ETB)</span>`;
        }
        return `<span class="winner-name-highlight">${w.playerName} (${w.winningAmount.toFixed(2)} ETB)</span>`;
    });
    
    let sentence = "";
    if (styledNames.length === 1) {
        if (winners[0].cardId === lockedCardId) {
            sentence = `🎉 YOU WON ${winners[0].winningAmount.toFixed(2)} ETB! 🎉`;
        } else {
            sentence = `${styledNames[0]} won the game!`;
        }
    } else if (styledNames.length === 2) {
        sentence = `${styledNames[0]} and ${styledNames[1]} won the game!`;
    } else {
        sentence = `${styledNames[0]} and ${styledNames.length - 1} others won!`;
    }
    
    summaryEl.innerHTML = sentence;
}

/**
 * Close winner modal
 */
function closeModal() {
    const modal = document.getElementById('winnerModal');
    if (modal) {
        modal.classList.remove('show');
        modal.setAttribute('aria-hidden','true');
    }
}

/**
 * Generate card HTML for winner modal
 */
function generateCard(cardMatrix, winningNumbers, drawnNumbers) {
    const grid = document.createElement('div');
    grid.className = 'card-grid';
    
    const colors = {
        B: '#2ecc71',
        I: '#f39c12',
        N: '#3498db',
        G: '#e74c3c',
        O: '#9b59b6'
    };
    
    ["B","I","N","G","O"].forEach(letter => {
        const headerCell = document.createElement('div');
        headerCell.className = 'cell header';
        headerCell.textContent = letter;
        headerCell.style.backgroundColor = colors[letter];
        grid.appendChild(headerCell);
    });
    
    let matrix = cardMatrix;
    if (typeof cardMatrix === 'string') {
        try {
            matrix = JSON.parse(cardMatrix);
        } catch(e) {
            matrix = cardMatrix;
        }
    }
    
    const winningSet = new Set(winningNumbers);
    const drawnSet = new Set(drawnNumbers);
    const lastDrawn = drawnNumbers[drawnNumbers.length - 1];
    
    for (let r = 0; r < 5; r++) {
        for (let c = 0; c < 5; c++) {
            const num = matrix[r][c];
            const cell = document.createElement('div');
            cell.className = 'cell';
            cell.textContent = num === 0 ? '⭐' : num;
            
            if (r === 2 && c === 2 && num === 0) {
                cell.classList.add('free');
            }
            
            if (winningSet.has(num) && num !== 0) {
                cell.classList.add('winning');
            }
            
            if (num === lastDrawn && winningSet.has(num)) {
                cell.classList.add('last-winning');
            }
            
            if (drawnSet.has(num) && !winningSet.has(num) && num !== 0) {
                cell.classList.add('drawn');
            }
            
            grid.appendChild(cell);
        }
    }
    
    return grid;
}

/**
 * Show processing popup
 */
function showProcessingPopup() {
    const popup = document.getElementById("processing-popup");
    if (!popup) return;
    popup.classList.remove("hide");
    popup.style.display = "flex";
    requestAnimationFrame(() => popup.classList.add("show"));
}

/**
 * Hide processing popup
 */
function hideProcessingPopup() {
    const popup = document.getElementById("processing-popup");
    if (!popup) return;
    popup.classList.remove("show");
    popup.classList.add("hide");
    setTimeout(() => {
        popup.style.display = "none";
    }, 400);
}
