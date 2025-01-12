document.addEventListener('DOMContentLoaded', function() {
    const loginForm = document.getElementById('loginForm');

    if (!loginForm) {
        console.error('Login form not found!');
        return;
    }

    loginForm.addEventListener('submit', async function(event) {
        event.preventDefault();

        const emailInput = document.getElementById('email');
        const passwordInput = document.getElementById('password');

        if (!emailInput || !passwordInput) {
            console.error('Form inputs not found!');
            return;
        }

        const email = emailInput.value.trim();
        const password = passwordInput.value;

        console.log('로그인 시도:', { email });

        try {
            const response = await fetch('/login', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                    'Accept': 'application/json'
                },
                body: `email=${encodeURIComponent(email)}&password=${encodeURIComponent(password)}`
            });

            console.log('서버 응답 상태:', response.status);
            const contentType = response.headers.get("content-type");
            console.log('응답 Content-Type:', contentType);

            const responseText = await response.text();
            console.log('서버 응답 텍스트:', responseText);

            if (!response.ok) {
                throw new Error(`로그인 실패: ${response.status}`);
            }

            const data = JSON.parse(responseText);
            console.log('로그인 성공 응답:', data);

            if (data.data?.accessToken) {
                sessionStorage.setItem('accessToken', data.data.accessToken);

                showLoginModal('로그인이 되었습니다.', true);
            } else {
                throw new Error('토큰을 받지 못했습니다.');
            }
        } catch (error) {
            console.error('Login error:', error);
            showLoginModal(error.message, false);
        }
    });
});

function showLoginModal(message, isSuccess) {
    const modal = document.getElementById('loginModal');
    const modalMessage = document.getElementById('loginModalMessage');
    modalMessage.textContent = message;
    modal.style.display = 'flex';

    const confirmButton = modal.querySelector('.modal-button');
    confirmButton.onclick = function() {
        if (isSuccess) {
            window.location.href = '/index.html';
        } else {
            modal.style.display = 'none';
        }
    };
}

function closeModal() {
    const modal = document.getElementById('loginModal');
    modal.style.display = 'none';
}

// Close the modal when clicking outside of it
window.onclick = function(event) {
    const modal = document.getElementById('loginModal');
    if (event.target === modal) {
        modal.style.display = 'none';
    }
}
