import React, { useState, useEffect } from 'react';
import { useApi } from './useApi';
import { fetchInventory, fetchOrders, fetchOrdersByDateRange, fetchFareConfig, calculateFare } from './api';
import { 
  LayoutDashboard, 
  Package, 
  ShoppingCart, 
  Calculator, 
  AlertTriangle,
  TrendingUp,
  PackageX,
  DollarSign,
  Search,
  Filter,
  Activity,
  Calendar,
  Box,
  Menu,
  X
} from 'lucide-react';
import { format } from 'date-fns';

function App() {
  const [activeTab, setActiveTab] = useState('overview');
  const [isSidebarOpen, setIsSidebarOpen] = useState(false);

  const { data: inventory, loading: invLoading } = useApi(fetchInventory, []);
  const { data: orders, loading: ordLoading } = useApi(fetchOrders, []);
  const { data: fareConfig, loading: fareLoading } = useApi(fetchFareConfig, null);

  // Computed Summary Metrics
  const totalItems = inventory?.length || 0;
  const lowStockItems = inventory?.filter(i => i.isLowStock) || [];
  const outOfStockItems = inventory?.filter(i => i.stockQuantity === 0) || [];
  const totalStockUnits = inventory?.reduce((sum, item) => sum + item.stockQuantity, 0) || 0;
  
  const totalOrders = orders?.length || 0;
  const completedOrders = orders?.filter(o => o.status === 'COMPLETED' || o.status === 'CONFIRMED') || [];
  const cancelledOrders = orders?.filter(o => o.status === 'CANCELLED') || [];
  const grossRevenue = completedOrders.reduce((sum, o) => sum + o.totalAmount, 0) || 0;

  return (
    <div className="flex h-screen bg-slate-950 text-slate-200 overflow-hidden relative">
      
      {/* Mobile Sidebar Overlay */}
      {isSidebarOpen && (
        <div 
          className="fixed inset-0 bg-slate-950/80 backdrop-blur-sm z-20 lg:hidden"
          onClick={() => setIsSidebarOpen(false)}
        />
      )}

      {/* Sidebar */}
      <aside className={`fixed inset-y-0 left-0 z-30 w-64 bg-slate-900 border-r border-slate-800 flex flex-col shadow-xl transform transition-transform duration-300 ease-in-out lg:translate-x-0 lg:static lg:inset-auto ${isSidebarOpen ? 'translate-x-0' : '-translate-x-full'}`}>
        <div className="p-6 border-b border-slate-800 flex items-center justify-between">
          <div className="flex items-center space-x-3">
            <div className="w-8 h-8 bg-cyan-600 rounded flex items-center justify-center shadow-lg shadow-cyan-900/50">
              <Activity className="w-5 h-5 text-white" />
            </div>
            <span className="font-bold text-lg tracking-wide text-white">Cymelle Ops</span>
          </div>
          <button 
            className="lg:hidden text-slate-400 hover:text-white"
            onClick={() => setIsSidebarOpen(false)}
          >
            <X size={20} />
          </button>
        </div>
        <nav className="flex-1 p-4 space-y-1 overflow-y-auto">
          <NavItem 
            icon={<LayoutDashboard size={20} />} 
            label="Overview" 
            isActive={activeTab === 'overview'} 
            onClick={() => { setActiveTab('overview'); setIsSidebarOpen(false); }} 
          />
          <NavItem 
            icon={<Package size={20} />} 
            label="Inventory" 
            isActive={activeTab === 'inventory'} 
            onClick={() => { setActiveTab('inventory'); setIsSidebarOpen(false); }} 
            badge={lowStockItems.length > 0 ? lowStockItems.length : null}
            badgeColor="bg-amber-500 text-amber-950"
          />
          <NavItem 
            icon={<ShoppingCart size={20} />} 
            label="Orders" 
            isActive={activeTab === 'orders'} 
            onClick={() => { setActiveTab('orders'); setIsSidebarOpen(false); }} 
          />
          <NavItem 
            icon={<Calculator size={20} />} 
            label="Fare Engine" 
            isActive={activeTab === 'fare'} 
            onClick={() => { setActiveTab('fare'); setIsSidebarOpen(false); }} 
          />
        </nav>
        <div className="p-4 border-t border-slate-800 text-xs text-slate-500 font-mono text-center">
          SYSTEM ONLINE • v1.0.0
        </div>
      </aside>

      {/* Main Content */}
      <main className="flex-1 overflow-y-auto overflow-x-hidden bg-slate-950 relative w-full lg:w-auto">
        <div className="absolute inset-0 bg-[radial-gradient(ellipse_at_top,_var(--tw-gradient-stops))] from-slate-900/50 via-slate-950 to-slate-950 pointer-events-none"></div>
        
        {/* Mobile Header */}
        <div className="lg:hidden sticky top-0 z-10 bg-slate-950/80 backdrop-blur-md border-b border-slate-800 p-4 flex items-center">
          <button 
            onClick={() => setIsSidebarOpen(true)}
            className="text-slate-400 hover:text-white p-1"
          >
            <Menu size={24} />
          </button>
          <span className="font-bold tracking-wide text-white ml-4">Cymelle Ops</span>
        </div>

        <div className="p-4 sm:p-8 max-w-7xl mx-auto relative z-10">
          <SectionErrorBoundary activeTab={activeTab}>
            {activeTab === 'overview' && (
              <OverviewSection 
                totalItems={totalItems}
                lowStockCount={lowStockItems.length}
                outOfStockCount={outOfStockItems.length}
                totalStockUnits={totalStockUnits}
                totalOrders={totalOrders}
                completedCount={completedOrders.length}
                cancelledCount={cancelledOrders.length}
                grossRevenue={grossRevenue}
                loading={invLoading || ordLoading}
              />
            )}

            {activeTab === 'inventory' && (
              <InventorySection inventory={inventory || []} loading={invLoading} />
            )}

            {activeTab === 'orders' && (
              <OrdersSection orders={orders || []} loading={ordLoading} />
            )}

            {activeTab === 'fare' && (
              <FareSection config={fareConfig} loading={fareLoading} />
            )}
          </SectionErrorBoundary>
        </div>
      </main>
    </div>
  );
}

class SectionErrorBoundary extends React.Component {
  constructor(props) {
    super(props);
    this.state = { hasError: false, errorMessage: '' };
  }

  static getDerivedStateFromError(error) {
    return {
      hasError: true,
      errorMessage: error?.message || 'Something went wrong in this section.',
    };
  }

  render() {
    if (this.state.hasError) {
      return (
        <div className="glass-panel p-8 border border-red-500/30">
          <h2 className="text-xl font-semibold text-white mb-2">Section failed to render</h2>
          <p className="text-slate-400 mb-4">{this.state.errorMessage}</p>
          <p className="text-sm text-slate-500">
            Try refreshing the page or switching tabs after the data source recovers.
          </p>
        </div>
      );
    }

    return this.props.children;
  }
}

// Sidebar Nav Item Component
function NavItem({ icon, label, isActive, onClick, badge, badgeColor = "bg-cyan-500 text-cyan-950" }) {
  return (
    <button
      onClick={onClick}
      className={`w-full flex items-center justify-between px-4 py-3 rounded-lg transition-all duration-200 ${
        isActive 
          ? 'bg-slate-800 text-cyan-400 shadow-sm' 
          : 'text-slate-400 hover:bg-slate-800/50 hover:text-slate-200'
      }`}
    >
      <div className="flex items-center space-x-3">
        {icon}
        <span className="font-medium text-sm">{label}</span>
      </div>
      {badge !== null && badge !== undefined && (
        <span className={`px-2 py-0.5 rounded-full text-xs font-bold ${badgeColor}`}>
          {badge}
        </span>
      )}
    </button>
  );
}

// --- Overview Section ---
function OverviewSection({ totalItems, lowStockCount, outOfStockCount, totalStockUnits, totalOrders, completedCount, cancelledCount, grossRevenue, loading }) {
  return (
    <div className="space-y-6 animate-in fade-in duration-300">
      <header className="mb-8">
        <h1 className="text-3xl font-bold text-white tracking-tight">System Overview</h1>
        <p className="text-slate-400 mt-1">Real-time metrics and operational status.</p>
      </header>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        <MetricCard title="Gross Revenue" value={`$${grossRevenue.toFixed(2)}`} icon={<DollarSign size={24} className="text-emerald-400" />} loading={loading} />
        <MetricCard title="Total Orders" value={totalOrders} icon={<ShoppingCart size={24} className="text-cyan-400" />} loading={loading} />
        <MetricCard title="Completed" value={completedCount} icon={<TrendingUp size={24} className="text-emerald-400" />} loading={loading} />
        <MetricCard title="Cancelled" value={cancelledCount} icon={<PackageX size={24} className="text-red-400" />} loading={loading} />
        
        <MetricCard title="Inventory Items" value={totalItems} icon={<Box size={24} className="text-cyan-400" />} loading={loading} />
        <MetricCard title="Total Stock Units" value={totalStockUnits} icon={<Package size={24} className="text-slate-400" />} loading={loading} />
        <MetricCard title="Low Stock Alerts" value={lowStockCount} icon={<AlertTriangle size={24} className={lowStockCount > 0 ? "text-amber-400" : "text-slate-500"} />} loading={loading} highlight={lowStockCount > 0 ? 'amber' : null} />
        <MetricCard title="Out of Stock" value={outOfStockCount} icon={<PackageX size={24} className={outOfStockCount > 0 ? "text-red-400" : "text-slate-500"} />} loading={loading} highlight={outOfStockCount > 0 ? 'red' : null} />
      </div>
    </div>
  );
}

function MetricCard({ title, value, icon, loading, highlight }) {
  const borderHighlight = highlight === 'amber' ? 'border-amber-500/50 shadow-[0_0_15px_rgba(245,158,11,0.1)]' :
                          highlight === 'red' ? 'border-red-500/50 shadow-[0_0_15px_rgba(239,68,68,0.1)]' : '';

  return (
    <div className={`metric-card ${borderHighlight}`}>
      <div className="flex justify-between items-start">
        <h3 className="metric-label">{title}</h3>
        <div className="p-2 bg-slate-900/50 rounded-lg">{icon}</div>
      </div>
      {loading ? (
        <div className="h-9 w-24 skeleton mt-2"></div>
      ) : (
        <p className={`metric-value ${highlight === 'amber' ? 'text-amber-400' : highlight === 'red' ? 'text-red-400' : ''}`}>
          {value}
        </p>
      )}
    </div>
  );
}

// --- Inventory Section ---
function InventorySection({ inventory, loading }) {
  const [searchTerm, setSearchTerm] = useState('');

  const safeInventory = Array.isArray(inventory) ? inventory : [];
  const normalizedSearch = searchTerm.trim().toLowerCase();
  const filtered = safeInventory.filter(item => {
    const name = (item?.name ?? '').toLowerCase();
    const id = item?.id != null ? String(item.id) : '';
    return name.includes(normalizedSearch) || id.includes(normalizedSearch);
  });

  return (
    <div className="space-y-6 animate-in fade-in duration-300">
      <header className="flex flex-col sm:flex-row justify-between items-start sm:items-end mb-8 space-y-4 sm:space-y-0">
        <div>
          <h1 className="text-2xl sm:text-3xl font-bold text-white tracking-tight">Inventory Management</h1>
          <p className="text-slate-400 mt-1">Monitor stock levels and thresholds.</p>
        </div>
        <div className="relative w-full sm:w-64">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-500 w-4 h-4" />
          <input 
            type="text" 
            placeholder="Search by ID or name..." 
            className="input-field pl-9 w-full"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />
        </div>
      </header>

      <div className="glass-panel overflow-x-auto">
        <table className="data-table min-w-[800px]">
          <thead>
            <tr>
              <th>SKU / ID</th>
              <th>Item Name</th>
              <th>Price</th>
              <th>Stock</th>
              <th>Threshold</th>
              <th>Status</th>
            </tr>
          </thead>
          <tbody>
            {loading ? (
              [...Array(5)].map((_, i) => (
                <tr key={i}>
                  <td colSpan="6" className="py-3 px-4"><div className="h-4 w-full skeleton"></div></td>
                </tr>
              ))
            ) : filtered.length === 0 ? (
              <tr>
                <td colSpan="6" className="py-12 text-center text-slate-500">
                  <PackageX className="mx-auto h-8 w-8 mb-2 opacity-50" />
                  No items found.
                </td>
              </tr>
            ) : (
              filtered.map(item => (
                <tr key={item.id}>
                  <td className="font-mono text-slate-400">#{item.id ?? '-'}</td>
                  <td className="font-medium text-slate-200">{item.name ?? 'Unnamed item'}</td>
                  <td className="font-mono text-slate-300">${Number(item.price ?? 0).toFixed(2)}</td>
                  <td className="font-mono">
                    <span className={item.stockQuantity === 0 ? 'text-red-400 font-bold' : item.isLowStock ? 'text-amber-400 font-bold' : 'text-slate-300'}>
                      {item.stockQuantity ?? 0}
                    </span>
                  </td>
                  <td className="font-mono text-slate-500">{item.lowStockThreshold ?? '-'}</td>
                  <td>
                    {item.stockQuantity === 0 ? (
                      <span className="status-chip error">Out of Stock</span>
                    ) : item.isLowStock ? (
                      <span className="status-chip warning">Low Stock</span>
                    ) : (
                      <span className="status-chip success">In Stock</span>
                    )}
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}

// --- Orders Section ---
function OrdersSection({ orders, loading }) {
  const [statusFilter, setStatusFilter] = useState('ALL');
  const [fromDate, setFromDate] = useState('');
  const [toDate, setToDate] = useState('');
  const [displayOrders, setDisplayOrders] = useState(orders);
  const [filterLoading, setFilterLoading] = useState(false);
  const [filterError, setFilterError] = useState('');

  useEffect(() => {
    let cancelled = false;

    async function loadFilteredOrders() {
      setFilterLoading(true);
      setFilterError('');
      try {
        const hasRange = fromDate || toDate;
        const results = hasRange
          ? await fetchOrdersByDateRange(
              fromDate || '1970-01-01T00:00:00',
              toDate || new Date().toISOString().slice(0, 10) + 'T23:59:59'
            )
          : orders;
        if (!cancelled) {
          setDisplayOrders(Array.isArray(results) ? results : []);
        }
      } catch (error) {
        if (!cancelled) {
          setFilterError(error?.message || 'Failed to load filtered orders.');
          setDisplayOrders([]);
        }
      } finally {
        if (!cancelled) setFilterLoading(false);
      }
    }

    loadFilteredOrders();
    return () => {
      cancelled = true;
    };
  }, [orders, fromDate, toDate]);

  const filtered = displayOrders.filter(order => {
    const statusMatches = statusFilter === 'ALL' || order.status === statusFilter;
    const orderDate = order.createdAt ? new Date(order.createdAt) : null;
    const fromMatches = !fromDate || (orderDate && orderDate >= new Date(`${fromDate}T00:00:00`));
    const toMatches = !toDate || (orderDate && orderDate <= new Date(`${toDate}T23:59:59.999`));
    return statusMatches && fromMatches && toMatches;
  });

  return (
    <div className="space-y-6 animate-in fade-in duration-300">
      <header className="flex flex-col sm:flex-row justify-between items-start sm:items-end mb-8 space-y-4 sm:space-y-0">
        <div>
          <h1 className="text-2xl sm:text-3xl font-bold text-white tracking-tight">Order History</h1>
          <p className="text-slate-400 mt-1">Review and manage recent transactions.</p>
        </div>
        <div className="w-full lg:w-auto space-y-3">
          <div className="flex flex-wrap gap-2 bg-slate-900 p-1 rounded-lg border border-slate-700">
            {['ALL', 'PENDING', 'CONFIRMED', 'COMPLETED', 'CANCELLED'].map(status => (
              <button
                key={status}
                onClick={() => setStatusFilter(status)}
                className={`px-3 py-1.5 rounded-md text-xs font-medium transition-colors ${
                  statusFilter === status 
                    ? 'bg-slate-700 text-white shadow-sm' 
                    : 'text-slate-400 hover:text-slate-200'
                }`}
              >
                {status}
              </button>
            ))}
          </div>
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-2">
            <input
              type="date"
              className="input-field text-sm"
              value={fromDate}
              onChange={(e) => setFromDate(e.target.value)}
            />
            <input
              type="date"
              className="input-field text-sm"
              value={toDate}
              onChange={(e) => setToDate(e.target.value)}
            />
          </div>
        </div>
      </header>

      {filterError && (
        <div className="p-4 rounded-lg border border-red-500/20 bg-red-500/10 text-red-300 text-sm">
          {filterError}
        </div>
      )}

      <div className="glass-panel overflow-x-auto">
        <table className="data-table min-w-[800px]">
          <thead>
            <tr>
              <th>Order ID</th>
              <th>Date</th>
              <th>Items</th>
              <th>Total Amount</th>
              <th>Status</th>
            </tr>
          </thead>
          <tbody>
            {loading || filterLoading ? (
              [...Array(5)].map((_, i) => (
                <tr key={i}>
                  <td colSpan="5" className="py-3 px-4"><div className="h-4 w-full skeleton"></div></td>
                </tr>
              ))
            ) : filtered.length === 0 ? (
              <tr>
                <td colSpan="5" className="py-12 text-center text-slate-500">
                  <Filter className="mx-auto h-8 w-8 mb-2 opacity-50" />
                  No orders match the selected filter.
                </td>
              </tr>
            ) : (
              filtered.map(order => {
                const date = new Date(order.createdAt);
                return (
                  <tr key={order.id}>
                    <td className="font-mono text-cyan-400">#{order.id}</td>
                    <td className="text-slate-300">
                      <div className="flex items-center space-x-2">
                        <Calendar className="w-3 h-3 text-slate-500" />
                        <span>{format(date, 'MMM d, yyyy HH:mm')}</span>
                      </div>
                    </td>
                    <td className="text-slate-300">
                      {order.items?.length || 0} items
                    </td>
                    <td className="font-mono font-bold text-slate-200">${order.totalAmount.toFixed(2)}</td>
                    <td>
                      <StatusBadge status={order.status} />
                    </td>
                  </tr>
                );
              })
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}

function StatusBadge({ status }) {
  switch (status) {
    case 'COMPLETED':
    case 'CONFIRMED':
      return <span className="status-chip success">{status}</span>;
    case 'PENDING':
      return <span className="status-chip warning">{status}</span>;
    case 'CANCELLED':
      return <span className="status-chip error">{status}</span>;
    default:
      return <span className="status-chip neutral">{status}</span>;
  }
}

// --- Fare Section ---
function FareSection({ config, loading }) {
  const [distance, setDistance] = useState('10');
  const [surge, setSurge] = useState('1.0');
  const [calcLoading, setCalcLoading] = useState(false);
  const [result, setResult] = useState(null);
  const [error, setError] = useState(null);

  const handleCalculate = async (e) => {
    e.preventDefault();
    setCalcLoading(true);
    setError(null);
    try {
      const res = await calculateFare(parseFloat(distance), surge);
      setResult(res);
    } catch (err) {
      setError('Failed to calculate fare. Ensure backend is running.');
    } finally {
      setCalcLoading(false);
    }
  };

  const applyPreset = (dist, surg) => {
    setDistance(dist.toString());
    setSurge(surg.toString());
  };

  return (
    <div className="space-y-6 animate-in fade-in duration-300">
      <header className="mb-8">
        <h1 className="text-2xl sm:text-3xl font-bold text-white tracking-tight">Fare Engine</h1>
        <p className="text-slate-400 mt-1">Calculate trip costs and review pricing rules.</p>
      </header>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        
        {/* Calculator Form */}
        <div className="lg:col-span-2 space-y-6">
          <div className="glass-panel p-4 sm:p-6">
            <h2 className="text-lg font-medium text-white mb-4 flex items-center">
              <Calculator className="w-5 h-5 mr-2 text-cyan-400" />
              Fare Calculator
            </h2>
            <form onSubmit={handleCalculate} className="space-y-4">
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-medium text-slate-400 uppercase tracking-wide mb-1">Distance (km)</label>
                  <input 
                    type="number" 
                    step="0.1"
                    min="0.1"
                    className="input-field font-mono text-lg"
                    value={distance}
                    onChange={e => setDistance(e.target.value)}
                    required
                  />
                </div>
                <div>
                  <label className="block text-xs font-medium text-slate-400 uppercase tracking-wide mb-1">Surge Multiplier</label>
                  <input 
                    type="number" 
                    step="0.1"
                    min="1.0"
                    className="input-field font-mono text-lg"
                    value={surge}
                    onChange={e => setSurge(e.target.value)}
                  />
                </div>
              </div>
              <button 
                type="submit" 
                disabled={calcLoading}
                className="w-full btn-primary py-3 text-base flex justify-center items-center"
              >
                {calcLoading ? (
                  <span className="w-5 h-5 border-2 border-white/20 border-t-white rounded-full animate-spin"></span>
                ) : (
                  'Calculate Final Fare'
                )}
              </button>
            </form>

            {error && (
              <div className="mt-4 p-4 bg-red-500/10 border border-red-500/20 rounded-lg text-red-400 text-sm">
                {error}
              </div>
            )}

            {result && !error && (
              <div className="mt-6 p-6 bg-slate-900 border border-cyan-500/30 rounded-xl">
                <div className="text-sm text-slate-400 uppercase tracking-wide mb-2 text-center">Calculated Result</div>
                <div className="text-5xl font-mono font-bold text-white text-center mb-6">
                  ${result.finalFare.toFixed(2)}
                </div>
                
                <div className="grid grid-cols-3 gap-4 border-t border-slate-800 pt-4">
                  <div className="text-center">
                    <div className="text-xs text-slate-500 uppercase tracking-wide mb-1">Base</div>
                    <div className="font-mono text-slate-300">${result.baseFare.toFixed(2)}</div>
                  </div>
                  <div className="text-center border-l border-r border-slate-800">
                    <div className="text-xs text-slate-500 uppercase tracking-wide mb-1">Dist Charge</div>
                    <div className="font-mono text-slate-300">${result.distanceCharge.toFixed(2)}</div>
                  </div>
                  <div className="text-center">
                    <div className="text-xs text-slate-500 uppercase tracking-wide mb-1">Surge</div>
                    <div className="font-mono text-slate-300">x{result.surgeMultiplier}</div>
                  </div>
                </div>
                {result.finalFare === result.minimumFare && (
                  <div className="mt-4 text-center text-xs text-amber-400 bg-amber-500/10 py-1.5 rounded-full border border-amber-500/20">
                    Minimum fare applied
                  </div>
                )}
              </div>
            )}
          </div>
          
          {/* Quick Presets */}
          <div className="glass-panel p-4 sm:p-6">
            <h2 className="text-sm font-medium text-slate-400 uppercase tracking-wide mb-4">Quick Demo Presets</h2>
            <div className="flex flex-col sm:flex-row gap-3">
              <button onClick={() => applyPreset(2, 1.0)} className="btn-secondary flex-1 text-xs">Short (2km)</button>
              <button onClick={() => applyPreset(10, 1.5)} className="btn-secondary flex-1 text-xs">Medium (10km, 1.5x)</button>
              <button onClick={() => applyPreset(25, 2.0)} className="btn-secondary flex-1 text-xs">Long (25km, 2.0x)</button>
            </div>
          </div>
        </div>

        {/* Configuration Sidebar */}
        <div className="glass-panel p-0 h-fit">
          <div className="p-4 bg-slate-800/80 border-b border-slate-700">
            <h2 className="font-medium text-white flex items-center">
              <Activity className="w-4 h-4 mr-2 text-slate-400" />
              Active Rules Engine
            </h2>
          </div>
          <div className="p-6 space-y-4">
            {loading ? (
              <div className="space-y-4">
                <div className="h-8 w-full skeleton"></div>
                <div className="h-8 w-full skeleton"></div>
                <div className="h-8 w-full skeleton"></div>
              </div>
            ) : config ? (
              <>
                <div className="flex justify-between items-center py-2 border-b border-slate-800">
                  <span className="text-sm text-slate-400">Base Fare</span>
                  <span className="font-mono text-white font-medium">${config.baseFare.toFixed(2)}</span>
                </div>
                <div className="flex justify-between items-center py-2 border-b border-slate-800">
                  <span className="text-sm text-slate-400">Rate per km</span>
                  <span className="font-mono text-white font-medium">${config.ratePerKm.toFixed(2)}</span>
                </div>
                <div className="flex justify-between items-center py-2 border-b border-slate-800">
                  <span className="text-sm text-slate-400">Minimum Fare</span>
                  <span className="font-mono text-amber-400 font-medium">${config.minimumFare.toFixed(2)}</span>
                </div>
                <div className="flex justify-between items-center py-2">
                  <span className="text-sm text-slate-400">Default Surge</span>
                  <span className="font-mono text-white font-medium">x{config.defaultSurgeMultiplier}</span>
                </div>
              </>
            ) : (
              <div className="text-sm text-slate-500 text-center py-4">Failed to load configuration</div>
            )}
          </div>
        </div>

      </div>
    </div>
  );
}

export default App;
