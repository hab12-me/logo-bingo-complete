/**
 * Payment System for Logo Bing - Telebirr Integration (Manual Verification)
 */

// Backend URL - must match client-session.js
let BACKEND_URL = "https://logo-bingo-complete-fgfh.onrender.com";

const PaymentSystem = (() => {
    let currentPlayerId = null;
    let currentBalance = 0;
    
    /**
     * Initialize payment system
     */
    function init(playerId) {
        currentPlayerId = playerId;
        console.log("✅ Payment system initialized for player:", playerId);
        refreshWalletBalance();
        addWalletFloatingButton();
    }
    
    /**
     * Add floating wallet button
     */
    function addWalletFloatingButton() {
        if (document.getElementById("floatingWalletBtn")) return;
        
        const btn = document.createElement("div");
        btn.id = "floatingWalletBtn";
        btn.innerHTML = "💰 <span id='floatingBalance'>0</span>";
        btn.style.cssText = `
            position: fixed;
            bottom: 20px;
            right: 20px;
            background: linear-gradient(135deg, #f39c12, #e67e22);
            color: white;
            padding: 12px 18px;
            border-radius: 50px;
            font-weight: bold;
            cursor: pointer;
            z-index: 1000;
            box-shadow: 0 4px 15px rgba(0,0,0,0.3);
            display: flex;
            align-items: center;
            gap: 8px;
            font-size: 16px;
            font-family: monospace;
        `;
        btn.onclick = () => showWalletMenu();
        document.body.appendChild(btn);
    }
    
    /**
     * Update floating balance display
     */
    function updateFloatingBalance(balance) {
        const span = document.getElementById("floatingBalance");
        if (span) span.textContent = balance.toFixed(2);
    }
    
    /**
     * Refresh wallet balance from server
     */
    async function refreshWalletBalance() {
        try {
            const response = await fetch(`${BACKEND_URL}/payment/wallet`, {
                headers: { "User-Id": currentPlayerId }
            });
            const data = await response.json();
            if (data.status === "success" && data.data) {
                currentBalance = data.data.balance || 0;
                updateFloatingBalance(currentBalance);
                
                // Also update lobby wallet display
                const walletEl = document.getElementById("lobby-wallet-value");
                if (walletEl) walletEl.textContent = currentBalance.toFixed(2);
                
                return currentBalance;
            }
            return 0;
        } catch (error) {
            console.error("Failed to get balance:", error);
            return 0;
        }
    }
    
    /**
     * Show wallet menu
     */
    function showWalletMenu() {
        const menuHtml = `
            <div id="walletMenu" class="payment-modal">
                <div class="payment-modal-content" style="max-width: 380px;">
                    <div class="payment-modal-header">
                        <h2>💰 My Wallet</h2>
                        <button class="close-modal" onclick="closeWalletMenu()">&times;</button>
                    </div>
                    <div class="payment-modal-body">
                        <div class="balance-display">
                            <div class="balance-label">Current Balance</div>
                            <div class="balance-amount">${currentBalance.toFixed(2)} ETB</div>
                        </div>
                        <div class="wallet-buttons">
                            <button class="wallet-action-btn deposit-btn" onclick="PaymentSystem.showDepositModal()">
                                💰 Deposit
                            </button>
                            <button class="wallet-action-btn withdraw-btn" onclick="PaymentSystem.showWithdrawModal()">
                                💸 Withdraw
                            </button>
                            <button class="wallet-action-btn history-btn" onclick="PaymentSystem.showTransactionHistory()">
                                📜 History
                            </button>
                        </div>
                        <div class="wallet-info">
                            <small>🏦 Admin Telebirr: <strong>0931721793</strong></small><br>
                            <small>✅ Deposits verified manually (5-15 min)</small><br>
                            <small>⏰ Withdrawals processed manually (5-30 min)</small>
                        </div>
                    </div>
                </div>
            </div>
        `;
        
        removeExistingModal();
        document.body.insertAdjacentHTML("beforeend", menuHtml);
        addPaymentStyles();
        
        const modal = document.getElementById("walletMenu");
        setTimeout(() => modal.classList.add("show"), 10);
    }
    
    /**
     * Close wallet menu
     */
    function closeWalletMenu() {
        const modal = document.getElementById("walletMenu");
        if (modal) {
            modal.classList.remove("show");
            setTimeout(() => modal.remove(), 300);
        }
    }
    
    /**
     * Submit deposit request
     */
    async function submitDeposit(amount, transactionId, senderPhoneNumber) {
        try {
            const response = await fetch(`${BACKEND_URL}/payment/deposit/request`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    "User-Id": currentPlayerId
                },
                body: JSON.stringify({
                    amount: amount,
                    transactionId: transactionId,
                    senderPhoneNumber: senderPhoneNumber
                })
            });
            const data = await response.json();
            return data;
        } catch (error) {
            console.error("Deposit submission error:", error);
            return { status: "error", message: "Network error" };
        }
    }
    
    /**
     * Submit withdrawal request
     */
    async function submitWithdrawal(amount, recipientPhoneNumber, recipientName) {
        try {
            const response = await fetch(`${BACKEND_URL}/payment/withdraw/request`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    "User-Id": currentPlayerId
                },
                body: JSON.stringify({
                    amount: amount,
                    recipientPhoneNumber: recipientPhoneNumber,
                    recipientName: recipientName
                })
            });
            const data = await response.json();
            return data;
        } catch (error) {
            console.error("Withdrawal submission error:", error);
            return { status: "error", message: "Network error" };
        }
    }
    
    /**
     * Get transaction history
     */
    async function getTransactionHistory() {
        try {
            const response = await fetch(`${BACKEND_URL}/payment/transactions`, {
                headers: { "User-Id": currentPlayerId }
            });
            const data = await response.json();
            return data.data || [];
        } catch (error) {
            console.error("Failed to get transactions:", error);
            return [];
        }
    }
    
    /**
     * Show deposit modal
     */
    function showDepositModal() {
        closeWalletMenu();
        
        const modalHtml = `
            <div id="depositModal" class="payment-modal">
                <div class="payment-modal-content">
                    <div class="payment-modal-header">
                        <h2>💰 Deposit via Telebirr</h2>
                        <button class="close-modal" onclick="closeDepositModal()">&times;</button>
                    </div>
                    <div class="payment-modal-body">
                        <div class="telebirr-info-card">
                            <div class="info-row">
                                <span class="info-label">📱 Send to:</span>
                                <span class="info-value" id="telebirrNumber">0931721793</span>
                                <button class="copy-btn" onclick="copyToClipboard('0931721793')">📋 Copy</button>
                            </div>
                            <div class="info-row">
                                <span class="info-label">👤 Account Name:</span>
                                <span class="info-value">Logo Bing Bingo</span>
                            </div>
                            <div class="info-row">
                                <span class="info-label">💰 Min/Max:</span>
                                <span class="info-value">10 - 50,000 ETB</span>
                            </div>
                        </div>
                        
                        <div class="instruction-box">
                            <h4>📝 How to Deposit:</h4>
                            <ol>
                                <li>Open Telebirr app on your phone</li>
                                <li>Select "Send Money"</li>
                                <li>Enter number: <strong>0931721793</strong></li>
                                <li>Enter the amount you want to deposit</li>
                                <li>Complete the transaction</li>
                                <li><strong class="highlight">COPY the Transaction ID</strong> from the receipt</li>
                                <li>Fill the form below and submit</li>
                            </ol>
                        </div>
                        
                        <form id="depositForm" class="payment-form">
                            <div class="form-group">
                                <label>💰 Amount (ETB):</label>
                                <input type="number" id="depositAmount" min="10" max="50000" step="1" required placeholder="Enter amount">
                                <small>Minimum: 10 ETB | Maximum: 50,000 ETB</small>
                            </div>
                            <div class="form-group">
                                <label>🔢 Telebirr Transaction ID:</label>
                                <input type="text" id="transactionId" required placeholder="Paste the transaction ID from Telebirr">
                                <small>Example: TXN123456789 or REF20241234XXXX</small>
                            </div>
                            <div class="form-group">
                                <label>📱 Your Telebirr Phone Number:</label>
                                <input type="tel" id="senderPhone" pattern="09[0-9]{8}" required placeholder="09xxxxxxxx">
                                <small>Format: 09XXXXXXXX (10 digits)</small>
                            </div>
                            <div class="info-note">
                                ✅ <strong>Manual Verification:</strong> Admin will verify within 5-15 minutes
                            </div>
                            <button type="submit" class="submit-payment-btn">✅ Submit Deposit Request</button>
                        </form>
                    </div>
                </div>
            </div>
        `;
        
        removeExistingModal();
        document.body.insertAdjacentHTML("beforeend", modalHtml);
        addPaymentStyles();
        
        const modal = document.getElementById("depositModal");
        setTimeout(() => modal.classList.add("show"), 10);
        
        const form = document.getElementById("depositForm");
        form.addEventListener("submit", async (e) => {
            e.preventDefault();
            await handleDepositSubmit();
        });
    }
    
    /**
     * Handle deposit form submission
     */
    async function handleDepositSubmit() {
        const amount = parseFloat(document.getElementById("depositAmount").value);
        const transactionId = document.getElementById("transactionId").value.trim();
        const senderPhone = document.getElementById("senderPhone").value.trim();
        
        if (isNaN(amount) || amount < 10) {
            showPopupMessage("⚠️ Minimum deposit is 10 ETB");
            return;
        }
        if (amount > 50000) {
            showPopupMessage("⚠️ Maximum deposit is 50,000 ETB");
            return;
        }
        if (!transactionId) {
            showPopupMessage("⚠️ Please enter your Telebirr Transaction ID");
            return;
        }
        if (!senderPhone.match(/^09[0-9]{8}$/)) {
            showPopupMessage("⚠️ Please enter a valid Ethiopian phone number (format: 09XXXXXXXX)");
            return;
        }
        
        const submitBtn = document.querySelector("#depositForm .submit-payment-btn");
        const originalText = submitBtn.textContent;
        submitBtn.textContent = "⏳ Submitting...";
        submitBtn.disabled = true;
        
        const result = await submitDeposit(amount, transactionId, senderPhone);
        
        submitBtn.textContent = originalText;
        submitBtn.disabled = false;
        
        if (result.status === "success") {
            showPopupMessage("✅ Deposit request submitted! Admin will verify within 5-15 minutes.");
            closeDepositModal();
            refreshWalletBalance();
        } else {
            showPopupMessage("❌ " + (result.message || "Failed to submit deposit"));
        }
    }
    
    /**
     * Close deposit modal
     */
    function closeDepositModal() {
        const modal = document.getElementById("depositModal");
        if (modal) {
            modal.classList.remove("show");
            setTimeout(() => modal.remove(), 300);
        }
    }
    
    /**
     * Show withdrawal modal
     */
    function showWithdrawModal() {
        closeWalletMenu();
        
        const modalHtml = `
            <div id="withdrawModal" class="payment-modal">
                <div class="payment-modal-content">
                    <div class="payment-modal-header">
                        <h2>💸 Withdraw via Telebirr</h2>
                        <button class="close-modal" onclick="closeWithdrawModal()">&times;</button>
                    </div>
                    <div class="payment-modal-body">
                        <div class="warning-box">
                            ⚠️ <strong>Manual Processing:</strong> Withdrawals are processed manually by admin.
                            Funds will be sent within 5-30 minutes.
                        </div>
                        
                        <div class="balance-display-small">
                            💵 Available Balance: <strong>${currentBalance.toFixed(2)} ETB</strong>
                        </div>
                        
                        <form id="withdrawForm" class="payment-form">
                            <div class="form-group">
                                <label>💰 Amount to Withdraw (ETB):</label>
                                <input type="number" id="withdrawAmount" min="10" max="50000" step="1" required placeholder="Enter amount">
                                <small>Minimum: 10 ETB | Maximum: 50,000 ETB</small>
                            </div>
                            <div class="form-group">
                                <label>📱 Your Telebirr Phone Number:</label>
                                <input type="tel" id="recipientPhone" pattern="09[0-9]{8}" required placeholder="09xxxxxxxx">
                                <small>Funds will be sent to this number</small>
                            </div>
                            <div class="form-group">
                                <label>👤 Full Name on Telebirr Account:</label>
                                <input type="text" id="recipientName" required placeholder="Enter your full name">
                            </div>
                            <div class="info-note warning">
                                ⏰ Withdrawals are processed manually. You will receive confirmation once sent.
                            </div>
                            <button type="submit" class="submit-payment-btn withdraw-btn">📤 Submit Withdrawal Request</button>
                        </form>
                    </div>
                </div>
            </div>
        `;
        
        removeExistingModal();
        document.body.insertAdjacentHTML("beforeend", modalHtml);
        addPaymentStyles();
        
        const modal = document.getElementById("withdrawModal");
        setTimeout(() => modal.classList.add("show"), 10);
        
        const form = document.getElementById("withdrawForm");
        form.addEventListener("submit", async (e) => {
            e.preventDefault();
            await handleWithdrawSubmit();
        });
    }
    
    /**
     * Handle withdrawal form submission
     */
    async function handleWithdrawSubmit() {
        const amount = parseFloat(document.getElementById("withdrawAmount").value);
        const recipientPhone = document.getElementById("recipientPhone").value.trim();
        const recipientName = document.getElementById("recipientName").value.trim();
        
        if (isNaN(amount) || amount < 10) {
            showPopupMessage("⚠️ Minimum withdrawal is 10 ETB");
            return;
        }
        if (amount > 50000) {
            showPopupMessage("⚠️ Maximum withdrawal is 50,000 ETB");
            return;
        }
        if (amount > currentBalance) {
            showPopupMessage(`⚠️ Insufficient balance! Your current balance is ${currentBalance.toFixed(2)} ETB`);
            return;
        }
        if (!recipientPhone.match(/^09[0-9]{8}$/)) {
            showPopupMessage("⚠️ Please enter a valid Ethiopian phone number (format: 09XXXXXXXX)");
            return;
        }
        if (!recipientName || recipientName.length < 2) {
            showPopupMessage("⚠️ Please enter your full name");
            return;
        }
        
        const confirmed = confirm(
            `💸 WITHDRAWAL CONFIRMATION\n\n` +
            `Amount: ${amount} ETB\n` +
            `Send to: ${recipientPhone}\n` +
            `Recipient: ${recipientName}\n\n` +
            `⚠️ This request will be processed manually by admin.\n` +
            `Processing time: 5-30 minutes.\n\n` +
            `Click OK to submit withdrawal request.`
        );
        
        if (!confirmed) return;
        
        const submitBtn = document.querySelector("#withdrawForm .submit-payment-btn");
        const originalText = submitBtn.textContent;
        submitBtn.textContent = "⏳ Submitting...";
        submitBtn.disabled = true;
        
        const result = await submitWithdrawal(amount, recipientPhone, recipientName);
        
        submitBtn.textContent = originalText;
        submitBtn.disabled = false;
        
        if (result.status === "success") {
            showPopupMessage("✅ Withdrawal request submitted! Admin will process within 5-30 minutes.");
            closeWithdrawModal();
            refreshWalletBalance();
        } else {
            showPopupMessage("❌ " + (result.message || "Failed to submit withdrawal"));
        }
    }
    
    /**
     * Close withdrawal modal
     */
    function closeWithdrawModal() {
        const modal = document.getElementById("withdrawModal");
        if (modal) {
            modal.classList.remove("show");
            setTimeout(() => modal.remove(), 300);
        }
    }
    
    /**
     * Show transaction history
     */
    async function showTransactionHistory() {
        const transactions = await getTransactionHistory();
        
        const modalHtml = `
            <div id="historyModal" class="payment-modal">
                <div class="payment-modal-content history-modal">
                    <div class="payment-modal-header">
                        <h2>📜 Transaction History</h2>
                        <button class="close-modal" onclick="closeHistoryModal()">&times;</button>
                    </div>
                    <div class="payment-modal-body">
                        ${transactions.length === 0 ? 
                            '<div class="no-transactions">📭 No transactions yet. Make a deposit to start!</div>' :
                            `<div class="transactions-list">
                                ${transactions.map(t => `
                                    <div class="transaction-item ${t.type.toLowerCase()}">
                                        <div class="transaction-icon">
                                            ${t.type === 'DEPOSIT' ? '💰' : t.type === 'WITHDRAW' ? '💸' : t.type === 'WIN' ? '🏆' : t.type === 'BONUS' ? '🎁' : '🎲'}
                                        </div>
                                        <div class="transaction-details">
                                            <div class="transaction-type">${t.type}</div>
                                            <div class="transaction-date">${new Date(t.createdAt).toLocaleString()}</div>
                                            ${t.paymentReference ? `<div class="transaction-ref">Ref: ${t.paymentReference.substring(0, 20)}...</div>` : ''}
                                        </div>
                                        <div class="transaction-amount ${t.type === 'DEPOSIT' || t.type === 'WIN' || t.type === 'BONUS' ? 'positive' : 'negative'}">
                                            ${t.type === 'DEPOSIT' || t.type === 'WIN' || t.type === 'BONUS' ? '+' : '-'} ${t.amount.toFixed(2)} ETB
                                        </div>
                                    </div>
                                `).join('')}
                            </div>`
                        }
                        <div class="history-footer">
                            <small>📌 Deposits are manually verified | Withdrawals are manually processed</small>
                        </div>
                    </div>
                </div>
            </div>
        `;
        
        removeExistingModal();
        document.body.insertAdjacentHTML("beforeend", modalHtml);
        addPaymentStyles();
        
        const modal = document.getElementById("historyModal");
        setTimeout(() => modal.classList.add("show"), 10);
    }
    
    /**
     * Close history modal
     */
    function closeHistoryModal() {
        const modal = document.getElementById("historyModal");
        if (modal) {
            modal.classList.remove("show");
            setTimeout(() => modal.remove(), 300);
        }
    }
    
    /**
     * Remove existing modals
     */
    function removeExistingModal() {
        const modals = ["walletMenu", "depositModal", "withdrawModal", "historyModal"];
        modals.forEach(id => {
            const modal = document.getElementById(id);
            if (modal) modal.remove();
        });
    }
    
    /**
     * Add payment system styles
     */
    function addPaymentStyles() {
        if (document.getElementById("paymentStyles")) return;
        
        const styles = `
            <style id="paymentStyles">
                .payment-modal {
                    position: fixed;
                    top: 0;
                    left: 0;
                    width: 100%;
                    height: 100%;
                    background: rgba(0,0,0,0.85);
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    z-index: 10000;
                    opacity: 0;
                    visibility: hidden;
                    transition: all 0.3s ease;
                    backdrop-filter: blur(5px);
                }
                .payment-modal.show {
                    opacity: 1;
                    visibility: visible;
                }
                .payment-modal-content {
                    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                    border-radius: 24px;
                    width: 92%;
                    max-width: 520px;
                    max-height: 90vh;
                    overflow-y: auto;
                    box-shadow: 0 25px 50px rgba(0,0,0,0.5);
                    animation: modalSlideIn 0.3s ease;
                }
                @keyframes modalSlideIn {
                    from { transform: translateY(-50px); opacity: 0; }
                    to { transform: translateY(0); opacity: 1; }
                }
                .payment-modal-header {
                    display: flex;
                    justify-content: space-between;
                    align-items: center;
                    padding: 20px 24px;
                    border-bottom: 1px solid rgba(255,255,255,0.2);
                }
                .payment-modal-header h2 {
                    margin: 0;
                    color: white;
                    font-size: 1.5rem;
                }
                .close-modal {
                    background: none;
                    border: none;
                    color: white;
                    font-size: 32px;
                    cursor: pointer;
                    opacity: 0.7;
                    transition: opacity 0.2s;
                }
                .close-modal:hover { opacity: 1; }
                .payment-modal-body {
                    padding: 24px;
                }
                .balance-display {
                    text-align: center;
                    padding: 24px;
                    background: rgba(255,255,255,0.12);
                    border-radius: 20px;
                    margin-bottom: 24px;
                }
                .balance-label {
                    font-size: 14px;
                    color: rgba(255,255,255,0.8);
                    margin-bottom: 8px;
                }
                .balance-amount {
                    font-size: 42px;
                    font-weight: bold;
                    color: #ffeb3b;
                    font-family: monospace;
                }
                .balance-display-small {
                    background: rgba(255,255,255,0.12);
                    padding: 15px;
                    border-radius: 12px;
                    text-align: center;
                    margin-bottom: 24px;
                    color: white;
                    font-size: 18px;
                }
                .wallet-buttons {
                    display: flex;
                    gap: 12px;
                    margin-bottom: 24px;
                }
                .wallet-action-btn {
                    flex: 1;
                    padding: 14px;
                    border: none;
                    border-radius: 12px;
                    font-weight: bold;
                    font-size: 16px;
                    cursor: pointer;
                    transition: transform 0.2s;
                }
                .wallet-action-btn:active { transform: scale(0.97); }
                .deposit-btn { background: #2ecc71; color: white; }
                .withdraw-btn { background: #e74c3c; color: white; }
                .history-btn { background: #3498db; color: white; }
                .wallet-info {
                    text-align: center;
                    color: rgba(255,255,255,0.7);
                    font-size: 12px;
                    line-height: 1.6;
                }
                .telebirr-info-card {
                    background: rgba(255,255,255,0.12);
                    border-radius: 16px;
                    padding: 16px;
                    margin-bottom: 24px;
                }
                .info-row {
                    display: flex;
                    justify-content: space-between;
                    align-items: center;
                    padding: 10px 0;
                    color: white;
                    border-bottom: 1px solid rgba(255,255,255,0.1);
                }
                .info-row:last-child { border-bottom: none; }
                .info-label { font-weight: bold; }
                .info-value { font-family: monospace; background: rgba(0,0,0,0.3); padding: 4px 8px; border-radius: 6px; }
                .copy-btn {
                    background: #3498db;
                    border: none;
                    padding: 6px 12px;
                    border-radius: 8px;
                    color: white;
                    cursor: pointer;
                    font-size: 12px;
                }
                .instruction-box {
                    background: rgba(255,255,255,0.1);
                    border-radius: 16px;
                    padding: 16px;
                    margin-bottom: 24px;
                    color: white;
                }
                .instruction-box h4 { margin: 0 0 12px 0; }
                .instruction-box ol { margin: 0; padding-left: 20px; }
                .instruction-box li { margin: 8px 0; }
                .highlight { color: #ffeb3b; }
                .warning-box {
                    background: rgba(231,76,60,0.25);
                    border-left: 4px solid #e74c3c;
                    padding: 14px;
                    border-radius: 10px;
                    margin-bottom: 20px;
                    color: white;
                    font-size: 13px;
                }
                .payment-form .form-group {
                    margin-bottom: 18px;
                }
                .payment-form label {
                    display: block;
                    color: white;
                    margin-bottom: 8px;
                    font-weight: bold;
                }
                .payment-form input {
                    width: 100%;
                    padding: 14px;
                    border: none;
                    border-radius: 10px;
                    font-size: 15px;
                    box-sizing: border-box;
                    background: white;
                }
                .payment-form small {
                    display: block;
                    color: rgba(255,255,255,0.7);
                    font-size: 11px;
                    margin-top: 6px;
                }
                .info-note {
                    background: rgba(46,204,113,0.2);
                    padding: 12px;
                    border-radius: 10px;
                    margin: 20px 0;
                    color: white;
                    font-size: 13px;
                    text-align: center;
                }
                .info-note.warning {
                    background: rgba(231,76,60,0.2);
                }
                .submit-payment-btn {
                    width: 100%;
                    padding: 16px;
                    background: linear-gradient(135deg, #2ecc71, #27ae60);
                    border: none;
                    border-radius: 12px;
                    color: white;
                    font-weight: bold;
                    font-size: 18px;
                    cursor: pointer;
                    transition: transform 0.2s;
                }
                .submit-payment-btn.withdraw-btn {
                    background: linear-gradient(135deg, #e74c3c, #c0392b);
                }
                .submit-payment-btn:active { transform: scale(0.98); }
                .submit-payment-btn:disabled { opacity: 0.6; transform: none; }
                .transactions-list {
                    max-height: 450px;
                    overflow-y: auto;
                }
                .transaction-item {
                    display: flex;
                    align-items: center;
                    gap: 14px;
                    padding: 14px;
                    background: rgba(255,255,255,0.1);
                    border-radius: 12px;
                    margin-bottom: 10px;
                }
                .transaction-icon { font-size: 28px; }
                .transaction-details { flex: 1; }
                .transaction-type { font-weight: bold; color: white; text-transform: capitalize; }
                .transaction-date { font-size: 11px; color: rgba(255,255,255,0.6); }
                .transaction-ref { font-size: 10px; color: rgba(255,255,255,0.5); font-family: monospace; margin-top: 4px; }
                .transaction-amount { font-weight: bold; font-size: 16px; }
                .transaction-amount.positive { color: #2ecc71; }
                .transaction-amount.negative { color: #e74c3c; }
                .no-transactions {
                    text-align: center;
                    padding: 50px;
                    color: rgba(255,255,255,0.7);
                }
                .history-footer {
                    text-align: center;
                    padding: 16px;
                    color: rgba(255,255,255,0.6);
                    font-size: 11px;
                    border-top: 1px solid rgba(255,255,255,0.1);
                    margin-top: 16px;
                }
            </style>
        `;
        document.head.insertAdjacentHTML("beforeend", styles);
    }
    
    // Public API
    return {
        init,
        refreshWalletBalance,
        showDepositModal,
        showWithdrawModal,
        showTransactionHistory,
        getBalance: () => currentBalance
    };
})();

// Global helper functions
function closeWalletMenu() { 
    const modal = document.getElementById("walletMenu");
    if (modal) {
        modal.classList.remove("show");
        setTimeout(() => modal.remove(), 300);
    }
}

function closeDepositModal() { 
    const modal = document.getElementById("depositModal");
    if (modal) {
        modal.classList.remove("show");
        setTimeout(() => modal.remove(), 300);
    }
}

function closeWithdrawModal() {
    const modal = document.getElementById("withdrawModal");
    if (modal) {
        modal.classList.remove("show");
        setTimeout(() => modal.remove(), 300);
    }
}

function closeHistoryModal() {
    const modal = document.getElementById("historyModal");
    if (modal) {
        modal.classList.remove("show");
        setTimeout(() => modal.remove(), 300);
    }
}

function copyToClipboard(text) {
    navigator.clipboard.writeText(text);
    showPopupMessage("✅ Copied to clipboard!");
}

// Make PaymentSystem global
window.PaymentSystem = PaymentSystem;
