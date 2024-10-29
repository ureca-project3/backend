class EventBanner {
    constructor() {
        this.bannerContainer = document.querySelector('.banner-container');
        this.loadEvents();
    }

    async loadEvents() {
        try {
            const response = await Api.get('/event/list');
            const result = await response.json();
            this.renderEventBanners(result.data);
        } catch (error) {
            console.error('Error loading events:', error);
        }
    }

    renderEventBanners(events) {
        if (!this.bannerContainer) return;

        this.bannerContainer.innerHTML = events.map(event => {
            const now = new Date();
            const startTime = new Date(event.startTime);

            // 이벤트 시작 전이면 클릭 불가능한 배너로 표시
            const isNotStarted = now < startTime;
            const bannerClass = isNotStarted ? 'event-banner disabled' : 'event-banner';
            const clickHandler = isNotStarted ?
                `onclick="alert('아직 이벤트 시작 시간이 아닙니다.'); return false;"` :
                '';

            return `
                <a href="/event.html?eventId=${event.eventId}" 
                   class="${bannerClass}" 
                   ${clickHandler}>
                    <img src="/image/event-default.jpg" alt="${event.eventName}" class="banner-image">
                    <div class="banner-info">
                        <h3>${event.eventName}</h3>
                        <p>시작: ${new Date(event.startTime).toLocaleString()}</p>
                        <p>종료: ${new Date(event.endTime).toLocaleString()}</p>
                        <p class="status">${this.getEventStatus(event)}</p>
                    </div>
                </a>
            `;
        }).join('');
    }

    getEventStatus(event) {
        const now = new Date();
        const startTime = new Date(event.startTime);
        const endTime = new Date(event.endTime);

        if (now < startTime) {
            return '시작 전';
        } else if (now > endTime) {
            return '종료됨';
        } else {
            return '진행중';
        }
    }
}

document.addEventListener('DOMContentLoaded', () => {
    new EventBanner();
});

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