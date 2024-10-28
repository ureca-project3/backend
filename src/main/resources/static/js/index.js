const loggedInButtons = document.querySelector('.auth-buttons.logged-in');
const loggedOutButtons = document.querySelector('.auth-buttons.logged-out');
const logoutButton = document.getElementById('logout-button');

async function fetchAccessToken() {
    try {
        const response = await fetch('/auth/token/access', {
            method: 'GET',
            credentials: 'include'
        });
        if (!response.ok) throw new Error(`Server error: ${response.status}`);

        const data = await response.json();
        const accessToken = data.accessToken;
        if (accessToken) {
            sessionStorage.setItem('accessToken', accessToken);
            updateHeaderWithUserInfo(accessToken);
        } else {
            console.error('No access token found in response');
            showLoggedOutState();
        }
    } catch (error) {
        console.error('Error fetching access token:', error);
        showLoggedOutState();
    }
}

function updateHeaderWithUserInfo(accessToken) {
    try {
        const base64Url = accessToken.split('.')[1];
        const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
        const jsonPayload = decodeURIComponent(atob(base64).split('').map(c =>
            '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2)
        ).join(''));

        showLoggedInState();
    } catch (error) {
        console.error('Error updating header:', error);
        showLoggedOutState();
    }
}

function showLoggedInState() {
    loggedInButtons.style.display = 'flex';
    loggedOutButtons.style.display = 'none';
}

function showLoggedOutState() {
    loggedInButtons.style.display = 'none';
    loggedOutButtons.style.display = 'flex';
}

window.onload = async function() {
    const tempAccessTokenCookie = document.cookie
        .split('; ')
        .find(row => row.startsWith('tempAccessToken='));

    if (tempAccessTokenCookie) {
        const accessToken = tempAccessTokenCookie.split('=')[1];
        sessionStorage.setItem('accessToken', accessToken);
        document.cookie = 'tempAccessToken=; Path=/; Expires=Thu, 01 Jan 1970 00:00:01 GMT;';
        updateHeaderWithUserInfo(accessToken);
    } else {
        let accessToken = sessionStorage.getItem('accessToken');
        const refreshToken = document.cookie.split('; ').find(row => row.startsWith('refreshToken='));

        if (accessToken) {
            updateHeaderWithUserInfo(accessToken);
        } else if (refreshToken && location.pathname !== '/index.html') {
            await fetchAccessToken();
        } else {
            showLoggedOutState();
        }
    }
};

logoutButton.addEventListener('click', async function() {
    try {
        const response = await fetch('/api/member/provider', {
            method: 'GET',
            headers: { 'Authorization': `Bearer ${sessionStorage.getItem('accessToken')}` }
        });
        const { provider } = await response.json();

        if (provider === 'kakao') {
            sessionStorage.removeItem('accessToken');
            document.cookie = 'refreshToken=; Path=/; Expires=Thu, 01 Jan 1970 00:00:01 GMT;';
            showLoggedOutState();
            window.location.href = '/auth/api/member/kakao-logout';
        } else {
            const logoutResponse = await fetch('/logout', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' }
            });

            if (logoutResponse.ok) {
                sessionStorage.removeItem('accessToken');
                document.cookie = 'refreshToken=; Path=/; Expires=Thu, 01 Jan 1970 00:00:01 GMT;';
                showLoggedOutState();
                window.location.href = '/index.html';
            } else {
                alert('로그아웃 실패');
            }
        }
    } catch (error) {
        console.error('로그아웃 중 오류 발생:', error);
        alert('로그아웃 중 오류가 발생했습니다.');
    }
});