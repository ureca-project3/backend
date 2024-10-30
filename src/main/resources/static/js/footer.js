async function loadFooter() {
    try {
        const response = await fetch('/html/footer.html');
        if (!response.ok) throw new Error(`Error loading footer: ${response.status}`);

        const footerContent = await response.text();
        document.getElementById('footer-container').innerHTML = footerContent;
    } catch (error) {
        console.error('Footer loading error:', error);
        document.getElementById('footer-container').innerHTML = '<footer class="footer">Footer 로드 실패</footer>'; // 오류 메시지
    }
}

document.addEventListener('DOMContentLoaded', loadFooter);
