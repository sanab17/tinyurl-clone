// Analytics Charts Rendering using Chart.js

document.addEventListener('DOMContentLoaded', () => {
    // Get data injected from HTML template attributes (or window globals)
    const clicksByDate = window.analyticsData?.clicksByDate || {};
    const clicksByBrowser = window.analyticsData?.clicksByBrowser || {};
    const clicksByOs = window.analyticsData?.clicksByOs || {};
    const clicksByReferrer = window.analyticsData?.clicksByReferrer || {};

    Chart.defaults.color = '#8f9cae';
    Chart.defaults.font.family = "'Outfit', 'Inter', sans-serif";

    // Helper to check if any data exists
    const hasData = Object.keys(clicksByDate).length > 0;
    if (!hasData) {
        return; // Let the HTML template display empty state
    }

    // 1. Clicks Over Time (Line Chart)
    const dateCtx = document.getElementById('dateChart');
    if (dateCtx) {
        const dates = Object.keys(clicksByDate);
        const counts = Object.values(clicksByDate);

        new Chart(dateCtx, {
            type: 'line',
            data: {
                labels: dates,
                datasets: [{
                    label: 'Clicks',
                    data: counts,
                    borderColor: '#00f2fe',
                    backgroundColor: 'rgba(0, 242, 254, 0.1)',
                    fill: true,
                    tension: 0.3,
                    borderWidth: 2,
                    pointBackgroundColor: '#00f2fe',
                    pointHoverBackgroundColor: '#ffffff'
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: { display: false }
                },
                scales: {
                    x: {
                        grid: { color: 'rgba(255, 255, 255, 0.05)' }
                    },
                    y: {
                        grid: { color: 'rgba(255, 255, 255, 0.05)' },
                        beginAtZero: true,
                        ticks: { stepSize: 1 }
                    }
                }
            }
        });
    }

    // 2. Browsers (Doughnut Chart)
    const browserCtx = document.getElementById('browserChart');
    if (browserCtx) {
        const browsers = Object.keys(clicksByBrowser);
        const counts = Object.values(clicksByBrowser);

        new Chart(browserCtx, {
            type: 'doughnut',
            data: {
                labels: browsers,
                datasets: [{
                    data: counts,
                    backgroundColor: [
                        '#00f2fe', // Cyan
                        '#7f00ff', // Violet
                        '#ff4757', // Coral
                        '#2ed573', // Green
                        '#ffa502', // Orange
                        '#747d8c'  // Grey
                    ],
                    borderWidth: 1,
                    borderColor: '#0e1626'
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        position: 'bottom',
                        labels: { boxWidth: 12, padding: 15 }
                    }
                }
            }
        });
    }

    // 3. Operating Systems (Doughnut Chart)
    const osCtx = document.getElementById('osChart');
    if (osCtx) {
        const oss = Object.keys(clicksByOs);
        const counts = Object.values(clicksByOs);

        new Chart(osCtx, {
            type: 'doughnut',
            data: {
                labels: oss,
                datasets: [{
                    data: counts,
                    backgroundColor: [
                        '#e100ff', // Magenta
                        '#4facfe', // Ice Blue
                        '#2ed573', // Green
                        '#ffa502', // Orange
                        '#747d8c'  // Grey
                    ],
                    borderWidth: 1,
                    borderColor: '#0e1626'
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        position: 'bottom',
                        labels: { boxWidth: 12, padding: 15 }
                    }
                }
            }
        });
    }

    // 4. Referrers (Bar Chart)
    const referrerCtx = document.getElementById('referrerChart');
    if (referrerCtx) {
        const referrers = Object.keys(clicksByReferrer);
        const counts = Object.values(clicksByReferrer);

        new Chart(referrerCtx, {
            type: 'bar',
            data: {
                labels: referrers,
                datasets: [{
                    label: 'Clicks',
                    data: counts,
                    backgroundColor: 'rgba(127, 0, 255, 0.6)',
                    borderColor: '#7f00ff',
                    borderWidth: 1,
                    hoverBackgroundColor: '#e100ff'
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: { display: false }
                },
                scales: {
                    x: {
                        grid: { color: 'rgba(255, 255, 255, 0.05)' }
                    },
                    y: {
                        grid: { color: 'rgba(255, 255, 255, 0.05)' },
                        beginAtZero: true,
                        ticks: { stepSize: 1 }
                    }
                }
            }
        });
    }
});
