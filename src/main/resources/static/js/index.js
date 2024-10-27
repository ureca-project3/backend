const userInfoDiv = document.getElementById('user-info');
const logoutButton = document.getElementById('logout-button');
const loginLink = document.getElementById('login-link');
const signupLink = document.getElementById('signup-link');

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
        }
    } catch (error) {
        console.error('Error fetching access token:', error);
        alert('Error occurred while fetching access token. Check console for more details.');
    }
}

function updateHeaderWithUserInfo(accessToken) {
    const base64Url = accessToken.split('.')[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const jsonPayload = decodeURIComponent(atob(base64).split('').map(c =>
        '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2)
    ).join(''));
    const userPayload = JSON.parse(jsonPayload);
    const username = userPayload.name || "User";

    userInfoDiv.innerText = `${username} 님 안녕하세요`;
    logoutButton.style.display = 'inline-block';
    loginLink.style.display = 'none';
    signupLink.style.display = 'none';
}

function resetHeader() {
    userInfoDiv.innerText = '';
    logoutButton.style.display = 'none';
    loginLink.style.display = 'inline-block';
    signupLink.style.display = 'inline-block';
}

window.onload = async function() {
    let accessToken = sessionStorage.getItem('accessToken');
    const refreshToken = document.cookie.split('; ').find(row => row.startsWith('refreshToken='));

    if (accessToken == null) {
        const urlParams = new URLSearchParams(window.location.search);
        accessToken = urlParams.get('accessToken');
        if (accessToken) {
            sessionStorage.setItem('accessToken', accessToken);

            urlParams.delete('accessToken');
            const newUrl = `${window.location.pathname}?${urlParams.toString()}`;
            window.history.replaceState({}, document.title, newUrl);
        }
    }

    if (accessToken) {
        updateHeaderWithUserInfo(accessToken);
    } else if (refreshToken && location.pathname !== '/index.html') {
        await fetchAccessToken();
    } else {
        resetHeader();
    }
};

logoutButton.addEventListener('click', async function() {
    const response = await fetch('/api/member/provider', {
        method: 'GET',
        headers: { 'Authorization': `Bearer ${sessionStorage.getItem('accessToken')}` }
    });
    const { provider } = await response.json();

    if (provider === 'kakao') {
        sessionStorage.removeItem('accessToken');
        document.cookie = 'refreshToken=; Path=/; Expires=Thu, 01 Jan 1970 00:00:01 GMT;';
        resetHeader();

        const clientId = 'ecbe8197ad00125d1d59da0fb88f4b3c';
        const redirectUri = encodeURIComponent('http://localhost:8080/auth/api/member/kakao-logout');
        const kakaoLogoutUrl = `https://kauth.kakao.com/oauth/logout?client_id=${clientId}&logout_redirect_uri=${redirectUri}`;

        window.location.href = kakaoLogoutUrl;
    } else {
        fetch('/logout', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' }
        })
            .then(response => {
                if (response.ok) {
                    sessionStorage.removeItem('accessToken');
                    document.cookie = 'refreshToken=; Path=/; Expires=Thu, 01 Jan 1970 00:00:01 GMT;';
                    resetHeader();
                    alert('로그아웃 성공');
                } else {
                    alert('로그아웃 실패');
                }
            })
            .catch(error => {
                console.error('로그아웃 중 오류 발생:', error);
            });
    }
});