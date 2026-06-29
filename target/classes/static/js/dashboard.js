// Dashboard Interactivity

document.addEventListener('DOMContentLoaded', () => {
    // Initialize toast element
    const toast = document.createElement('div');
    toast.className = 'toast';
    toast.innerHTML = `
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <polyline points="20 6 9 17 4 12"></polyline>
        </svg>
        <span class="toast-message">Copied to clipboard!</span>
    `;
    document.body.appendChild(toast);

    function showToast(message) {
        toast.querySelector('.toast-message').textContent = message;
        toast.classList.add('show');
        setTimeout(() => {
            toast.classList.remove('show');
        }, 3000);
    }

    // Copy to Clipboard
    const copyBadges = document.querySelectorAll('.copy-badge');
    copyBadges.forEach(badge => {
        badge.addEventListener('click', async (e) => {
            e.preventDefault();
            const textToCopy = badge.getAttribute('data-copy');
            try {
                await navigator.clipboard.writeText(textToCopy);
                showToast("Short link copied!");
                
                // Temporary text feedback on the element
                const originalText = badge.textContent;
                badge.textContent = "Copied!";
                badge.style.borderColor = "var(--success)";
                badge.style.color = "var(--success)";
                badge.style.background = "rgba(46, 213, 115, 0.1)";
                
                setTimeout(() => {
                    badge.textContent = originalText;
                    badge.style.borderColor = "";
                    badge.style.color = "";
                    badge.style.background = "";
                }, 2000);
            } catch (err) {
                console.error('Failed to copy text: ', err);
                showToast("Failed to copy link.");
            }
        });
    });

    // QR Code Modal handling
    const qrButtons = document.querySelectorAll('.btn-qr-trigger');
    const modalOverlay = document.getElementById('qr-modal-overlay');
    const qrModalImage = document.getElementById('qr-modal-image');
    const qrModalTitle = document.getElementById('qr-modal-title');
    const qrDownloadLink = document.getElementById('qr-download-link');
    const modalClose = document.getElementById('qr-modal-close');

    if (modalOverlay) {
        qrButtons.forEach(btn => {
            btn.addEventListener('click', () => {
                const qrBase64 = btn.getAttribute('data-qr');
                const shortCode = btn.getAttribute('data-code');
                
                qrModalImage.src = `data:image/png;base64,${qrBase64}`;
                qrModalTitle.textContent = `QR Code for /${shortCode}`;
                qrDownloadLink.href = `data:image/png;base64,${qrBase64}`;
                qrDownloadLink.download = `qrcode-${shortCode}.png`;
                
                modalOverlay.classList.add('active');
            });
        });

        const closeModal = () => {
            modalOverlay.classList.remove('active');
        };

        modalClose.addEventListener('click', closeModal);
        modalOverlay.addEventListener('click', (e) => {
            if (e.target === modalOverlay) {
                closeModal();
            }
        });

        // Close on escape key
        document.addEventListener('keydown', (e) => {
            if (e.key === 'Escape' && modalOverlay.classList.contains('active')) {
                closeModal();
            }
        });
    }
});
