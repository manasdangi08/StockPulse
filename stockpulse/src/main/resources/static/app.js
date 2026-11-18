let cachedProducts = [];
let cachedOrders = [];
let toastTimer = null;

window.onload = () => {
    loadAllData();
    setInterval(updateCountdowns, 1000);
    setInterval(loadAllData, 5000);
};

async function loadAllData() {
    await Promise.all([fetchProducts(), fetchOrders()]);
    updateStats();
}

// Live KPI Cards Update
function updateStats() {
    const totalStock = cachedProducts.reduce((sum, p) => sum + p.availableStock, 0);
    const activeHolds = cachedOrders.filter(o => o.status === 'RESERVED').length;
    const revenue = cachedOrders
        .filter(o => o.status === 'CONFIRMED')
        .reduce((sum, o) => sum + o.totalAmount, 0);

    document.getElementById('statTotalStock').textContent = totalStock;
    document.getElementById('statActiveHolds').textContent = activeHolds;
    document.getElementById('statRevenue').textContent = `₹${revenue.toLocaleString('en-IN')}`;
}

// 1. Products Fetch Karna
async function fetchProducts() {
    const res = await fetch('/api/products');
    cachedProducts = await res.json();
    const grid = document.getElementById('productGrid');

    grid.innerHTML = cachedProducts.map(p => `
        <div class="card">
            <div>
                <div class="card-top">
                    <h3>${p.name}</h3>
                    <span class="stock-pill ${p.availableStock <= 1 ? 'low' : 'ok'}">
                        ${p.availableStock === 0 ? 'Sold Out' : `${p.availableStock} left`}
                    </span>
                </div>
                <div class="price">₹${p.price.toLocaleString('en-IN')}</div>
            </div>
            <button class="btn-reserve"
                    onclick="reserveProduct(${p.id})"
                    ${p.availableStock === 0 ? 'disabled' : ''}>
                ${p.availableStock === 0 ? 'Out of Stock' : 'Lock 1 Unit (2 Min Hold)'}
            </button>
        </div>
    `).join('');
}

// 2. Stock Reserve Karna
async function reserveProduct(productId, customBuyer = null) {
    const customerName = customBuyer || document.getElementById('customerName').value.trim();

    const response = await fetch('/api/orders/reserve', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
            customerName: customerName,
            productId: productId,
            quantity: 1
        })
    });

    const data = await response.json();

    if (response.ok) {
        showAlert(`✅ HTTP 201: Order #${data.orderId} reserved for ${data.customerName}!`, 'success');
    } else {
        showAlert(`⚠️ HTTP ${response.status} (${data.error}): ${data.message}`, 'error');
    }

    await loadAllData();
}

// 3. Payment Confirm Karna
async function confirmOrder(orderId) {
    const response = await fetch(`/api/orders/${orderId}/confirm`, {
        method: 'POST'
    });

    const data = await response.json();

    if (response.ok) {
        showAlert(`🎉 HTTP 200: Order #${data.orderId} CONFIRMED!`, 'success');
    } else {
        showAlert(`❌ HTTP ${response.status} (${data.error}): ${data.message}`, 'error');
    }

    await loadAllData();
}

// 4. Orders List Fetch Karna
async function fetchOrders() {
    const res = await fetch('/api/orders');
    cachedOrders = await res.json();
    renderOrders();
}

function renderOrders() {
    const tbody = document.getElementById('orderTableBody');

    if (cachedOrders.length === 0) {
        tbody.innerHTML = `<tr><td colspan="7" class="empty-state">No reservations yet. Lock a product or run the race simulation above!</td></tr>`;
        return;
    }

    tbody.innerHTML = cachedOrders.map(o => `
        <tr>
            <td><strong>#${o.orderId}</strong></td>
            <td>${o.customerName}</td>
            <td>${o.productName}</td>
            <td>${o.quantity}</td>
            <td>₹${o.totalAmount.toLocaleString('en-IN')}</td>
            <td><span class="badge ${o.status}">${o.status}</span></td>
            <td id="timer-${o.orderId}">
                ${o.status === 'RESERVED'
                    ? `<span class="countdown" data-expires="${o.expiresAt}">--:--</span>
                       <button class="btn-confirm" onclick="confirmOrder(${o.orderId})">Confirm Pay</button>`
                    : '<span style="color:#64748b">—</span>'}
            </td>
        </tr>
    `).join('');
    updateCountdowns();
}

function updateCountdowns() {
    document.querySelectorAll('.countdown').forEach(el => {
        const expiresAt = new Date(el.getAttribute('data-expires')).getTime();
        const diff = Math.floor((expiresAt - Date.now()) / 1000);

        if (diff > 0) {
            const mins = Math.floor(diff / 60);
            const secs = diff % 60;
            el.textContent = `${mins}m ${secs < 10 ? '0' : ''}${secs}s`;
        } else {
            el.textContent = 'Releasing...';
        }
    });
}

// Race Condition Simulator
async function simulateRaceCondition() {
    showAlert('🔥 Firing 2 parallel requests for Product #1...', 'success');
    await Promise.all([
        reserveProduct(1, 'Buyer_A (Fast)'),
        reserveProduct(1, 'Buyer_B (Concurrent)')
    ]);
}

function showAlert(message, type) {
    const box = document.getElementById('alertBox');
    box.textContent = message;
    box.className = `toast ${type}`;

    clearTimeout(toastTimer);
    toastTimer = setTimeout(() => {
        box.className = 'toast hidden';
    }, 6000);
}