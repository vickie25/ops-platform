const BASE_URL = 'http://localhost:8081/api';

export async function fetchInventory() {
  const res = await fetch(`${BASE_URL}/inventory`);
  if (!res.ok) throw new Error('Failed to fetch inventory');
  return res.json();
}

export async function fetchLowStock() {
  const res = await fetch(`${BASE_URL}/inventory/low-stock`);
  if (!res.ok) throw new Error('Failed to fetch low-stock inventory');
  return res.json();
}

export async function fetchOrders() {
  const res = await fetch(`${BASE_URL}/orders`);
  if (!res.ok) throw new Error('Failed to fetch orders');
  return res.json();
}

export async function fetchOrdersByStatus(status) {
  const res = await fetch(`${BASE_URL}/orders/filter/status?status=${encodeURIComponent(status)}`);
  if (!res.ok) throw new Error('Failed to fetch orders by status');
  return res.json();
}

export async function fetchOrdersByDateRange(from, to) {
  const res = await fetch(`${BASE_URL}/orders/filter/date-range?from=${encodeURIComponent(from)}&to=${encodeURIComponent(to)}`);
  if (!res.ok) throw new Error('Failed to fetch orders by date range');
  return res.json();
}

export async function cancelOrder(id) {
  const res = await fetch(`${BASE_URL}/orders/${id}`, { method: 'DELETE' });
  if (!res.ok) throw new Error('Failed to cancel order');
  return res.json();
}

export async function calculateFare(distanceKm, surgeMultiplier) {
  let url = `${BASE_URL}/fare/calculate?distanceKm=${encodeURIComponent(distanceKm)}`;
  if (surgeMultiplier) {
    url += `&surgeMultiplier=${encodeURIComponent(surgeMultiplier)}`;
  }
  const res = await fetch(url);
  if (!res.ok) throw new Error('Failed to calculate fare');
  return res.json();
}

export async function fetchFareConfig() {
  const res = await fetch(`${BASE_URL}/fare/config`);
  if (!res.ok) throw new Error('Failed to fetch fare configuration');
  return res.json();
}
