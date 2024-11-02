const loggedInButtons = document.querySelector('.auth-buttons.logged-in');
const loggedOutButtons = document.querySelector('.auth-buttons.logged-out');
const logoutButton = document.getElementById('logout-button');

function showLoggedInState() {
    if (loggedInButtons) loggedInButtons.style.display = 'flex';
    if (loggedOutButtons) loggedOutButtons.style.display = 'none';
}

function showLoggedOutState() {
    if (loggedInButtons) loggedInButtons.style.display = 'none';
    if (loggedOutButtons) loggedOutButtons.style.display = 'flex';
}

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

// 자녀 성향 진단하기 버튼
const floatingButton = document.createElement("button");
floatingButton.className = "floating-button";
floatingButton.innerHTML = `
    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <!-- 귀여운 아기 얼굴 아이콘 -->
        <circle cx="12" cy="10" r="7" /> <!-- 얼굴 -->
        <circle cx="9" cy="8" r="1" fill="currentColor" /> <!-- 왼쪽 눈 -->
        <circle cx="15" cy="8" r="1" fill="currentColor" /> <!-- 오른쪽 눈 -->
        <path d="M9 12a3 3 0 0 0 6 0" /> <!-- 웃는 입 -->
        <path d="M6 20c1-2 3-3 6-3s5 1 6 3" /> <!-- 볼살 -->
        <path d="M8 4s1-1 4-1 4 1 4 1" /> <!-- 앞머리 -->
    </svg>
    자녀 성향 진단하기
`;

// 버튼 클릭 이벤트
floatingButton.onclick = () => {
    const accessToken = sessionStorage.getItem('accessToken');
    if (!accessToken) {
        // 커스텀 로그인 알림창 생성
        const loginAlertDiv = document.createElement('div');
        loginAlertDiv.className = 'custom-alert';
        loginAlertDiv.innerHTML = `
        <div class="alert-content">
            <p>로그인이 필요해요 🙂</p>
            <div class="alert-buttons">
                <button id="goToLogin" class="goLogin-btn">로그인하러 가기</button>
                <button id="closeLoginAlert" class="close-btn">닫기</button>
            </div>
        </div>
    `;

        document.body.appendChild(loginAlertDiv);

        // 로그인 페이지로 이동
        document.getElementById('goToLogin').onclick = () => {
            window.location.href = '/login.html';
        };

        // 알림창 닫기
        document.getElementById('closeLoginAlert').onclick = () => {
            loginAlertDiv.remove();
        };
        return;
    }

    const childId = sessionStorage.getItem('child_id');
    if (!childId) {
        // 커스텀 알림창 생성
        const alertDiv = document.createElement('div');
        alertDiv.className = 'custom-alert';
        alertDiv.innerHTML = `
            <div class="alert-content">
                <p>자녀 등록 후 이용 가능합니다 🥰</p>
                <button id="goToRegister" class="register-btn">자녀 등록하러 가기</button>
                <button id="closeAlert" class="close-btn">닫기</button>
            </div>
        `;

        document.body.appendChild(alertDiv);

        // 자녀 등록 페이지로 이동
        document.getElementById('goToRegister').onclick = () => {
            window.location.href = '/childRegister.html';
        };

        // 알림창 닫기
        document.getElementById('closeAlert').onclick = () => {
            alertDiv.remove();
        };
        return;
    }

    window.location.href = '/test.html';
}

document.body.appendChild(floatingButton);

// 스크롤 이벤트 리스너 추가
let lastScroll = 0;
window.addEventListener('scroll', () => {
    const currentScroll = window.pageYOffset;

    if (currentScroll <= 0) {
        floatingButton.style.opacity = "1";
    }

    if (currentScroll > lastScroll && currentScroll > 300) {
        floatingButton.style.opacity = "0.7";
    } else {
        floatingButton.style.opacity = "1";
    }

    lastScroll = currentScroll;
});

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

document.addEventListener('DOMContentLoaded', async function() {

    // 검색 입력 필드에 이벤트 리스너 추가
    const searchInput = document.getElementById('searchInput'); // 검색 입력 필드 ID에 맞게 변경

    if (searchInput) {
        searchInput.addEventListener('keypress', (event) => {
            if (event.key === 'Enter') {
                const keyword = searchInput.value.trim(); // 입력된 값에서 공백 제거
                if (keyword) {
                    console.log(keyword)
                    window.location.href = `/searchResult.html?query=${encodeURIComponent(keyword)}`;
                } else {
                    alert("검색어를 입력해주세요.");
                }
            }
        });
    }

    // 이벤트 응모하기로 이동
    const eventButton = document.querySelector('.event-button');
    if (eventButton) {
        eventButton.addEventListener('click', () => {
            window.location.href = '/event.html'; // 이동할 URL
        });
    } else {
        console.error('이벤트 페이지 이동 중 에러가 발생했습니다.');
    }

    // 최신 책 목록 가져오기
    await fetchLatestBooks();

    const moreRecentBooksButton = document.getElementById('moreRecentBooks');
    if (moreRecentBooksButton) {
        moreRecentBooksButton.addEventListener('click', () => {
            window.location.href = '/bookList.html'; // 이동할 URL
        });
    } else {
        console.error('더보기 버튼을 찾을 수 없습니다.');
    }

    // 인기 책 목록 가져오기
    await fetchTopLikedBooks();

    const morePopularBooksButton = document.getElementById('morePopularBooks');
    if (morePopularBooksButton) {
        morePopularBooksButton.addEventListener('click', () => {
            window.location.href = '/recommend.html'; // 이동할 URL
        });
    } else {
        console.error('더보기 버튼을 찾을 수 없습니다.');
    }

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
});

// 로그아웃 기능
if (logoutButton) {
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
}

// 최신 책 조회
async function fetchLatestBooks(page = 0, size = 10) {
    try {
        const response = await fetch(`/books?size=${size}&page=${page}`);
        const data = await response.json();

        if (data.message === "Get BookList Success") {
            displayLatestBooks(data.data); // 데이터 표시 함수 호출
        } else {
            console.error("Failed to fetch book list:", data.message);
        }
    } catch (error) {
        console.error("Error fetching book list:", error);
    }
}

// 도서 목록을 화면에 표시하는 함수
function displayLatestBooks(books) {
    const container = document.getElementById('recentBookCards');
    container.innerHTML = ''; // 기존 내용 지우기

    if (books.length === 0) {
        console.log("No books available.");
        return;
    }

    books.forEach(book => {
        const card = document.createElement('div');
        card.className = 'book-card';

        // 카드 클릭 시 상세 페이지로 이동
        card.addEventListener('click', () => {
            window.location.href = `/bookDetail.html?bookId=${book.bookId}`;
        });

        card.innerHTML = `
            <img src="${book.imageUrl}" alt="${book.title}" class="book-image" />
            <h2 class="book-title">${book.title}</h2>
            <p class="book-info">추천 연령: ${book.recAge}세</p>
            <p class="book-info">출판사: ${book.publisher}</p>
        `;

        container.appendChild(card);
    });
}

// 인기 책 조회
async function fetchTopLikedBooks() {
    try {
        const response = await fetch('/books/ranking');
        const data = await response.json();

        if (data.message === "Get Top Liked Books Success") {
            displayTopLikedBooks(data.data.slice(0, 10));
        } else {
            console.error("Failed to fetch top liked books:", data.message);
        }
    } catch (error) {
        console.error("Error fetching top liked books:", error);
    }
}

function displayTopLikedBooks(books) {
    const container = document.getElementById('popularBooksCards');
    container.innerHTML = ''; // 기존 내용 지우기

    if (books.length === 0) {
        console.log("인기 책 데이터가 없습니다.");  // 데이터 없음 로그
    }

    books.forEach(book => {
        console.log("Rendering book:", book);

        const card = document.createElement('div');
        card.className = 'book-card';

        // 카드 클릭 시 상세 페이지로 이동하도록 이벤트 추가
        card.addEventListener('click', () => {
            const bookId = book.bookId;
            window.location.href = `/bookDetail.html?bookId=${bookId}`;
        });

        card.innerHTML = `
            <img src="${book.imageUrl}" alt="${book.imageUrl}" class="book-image" />
            <h2 class="book-title">${book.title}</h2>
            <p class="book-info">추천 연령: ${book.recAge}세</p>
            <p class="book-info">출판사: ${book.publisher}</p>
        `;

        container.appendChild(card);
    });
}

// '더 보기' 버튼 이벤트
document.getElementById('morePopularBooks').addEventListener('click', () => {
    console.log("더 보기 버튼 클릭됨");
    window.location.href = '/bookList.html';
});