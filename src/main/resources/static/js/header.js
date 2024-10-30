class Header {
    constructor() {
        this.initializeElements();
        this.initializeEventListeners();
        this.checkAuthStatus();
    }

    initializeElements() {
        this.loggedInButtons = document.querySelector('.auth-buttons.logged-in');
        this.loggedOutButtons = document.querySelector('.auth-buttons.logged-out');
        this.profileSelector = document.querySelector('.profile-selector');
        this.profileButton = document.querySelector('.profile-button');
        this.profileDropdown = document.querySelector('.profile-dropdown');
        this.profileList = document.querySelector('.profile-list');
        this.logoutButton = document.getElementById('logout-button');
        this.currentChildName = document.getElementById('current-child-name');
        this.currentProfileImage = document.getElementById('current-profile-image');
    }

    initializeEventListeners() {
        if (this.profileButton) {
            this.profileButton.addEventListener('click', () => {
                if (this.profileDropdown) {
                    this.profileDropdown.style.display =
                        this.profileDropdown.style.display === 'none' ? 'block' : 'none';
                }
            });
        }

        document.addEventListener('click', (e) => {
            if (this.profileSelector && !this.profileSelector.contains(e.target)) {
                if (this.profileDropdown) {
                    this.profileDropdown.style.display = 'none';
                }
            }
        });

        if (this.logoutButton) {
            this.logoutButton.addEventListener('click', () => this.handleLogout());
        }
    }

    async checkAuthStatus() {
        const accessToken = sessionStorage.getItem('accessToken');
        if (accessToken) {
            this.showLoggedInState();
            await this.fetchAndDisplayChildren();
        } else {
            this.showLoggedOutState();
        }
    }

    async fetchAndDisplayChildren() {
        try {
            const response = await Api.get('/api/member/children');
            const result = await response.json();

            // CommonResponse 형식에서 data 배열 추출
            const children = result.data;  // data: [] 형식으로 오는 것을 처리

            if (children && Array.isArray(children)) {  // 배열인지 확인
                if (children.length > 0) {  // 자녀가 있는 경우
                    this.renderChildrenProfiles(children);

                    const currentChildId = sessionStorage.getItem('currentChildId');
                    if (currentChildId) {
                        const currentChild = children.find(child => child.childId.toString() === currentChildId);
                        if (currentChild) {
                            this.updateCurrentProfile(currentChild);
                        } else {
                            this.updateCurrentProfile(children[0]);
                            sessionStorage.setItem('currentChildId', children[0].childId.toString());
                        }
                    } else {
                        this.updateCurrentProfile(children[0]);
                        sessionStorage.setItem('currentChildId', children[0].childId.toString());
                    }
                } else {  // 자녀가 없는 경우
                    if (this.currentChildName) {
                        this.currentChildName.textContent = "등록된 자녀 없음";
                    }
                    if (this.profileList) {
                        this.profileList.innerHTML = '<div class="no-children-message">등록된 자녀가 없습니다.</div>';
                    }
                }
            }
        } catch (error) {
            console.error('Error fetching children:', error);
            if (this.currentChildName) {
                this.currentChildName.textContent = "자녀 정보 로드 실패";
            }
        }
    }

    renderChildrenProfiles(children) {
        if (!this.profileList) return;

        this.profileList.innerHTML = children.map(child => `
            <div class="profile-item" data-child-id="${child.childId}">
                <img class="profile-item-image" 
                     src="/image/${child.imageUrl || 'profileDefault.png'}" 
                     alt="${child.name}의 프로필"
                     onerror="this.src='/image/profileDefault.png'">
                <div class="profile-item-info">
                    <div class="profile-item-name">${child.name}</div>
                    <div class="profile-item-age">${child.age}세</div>
                </div>
            </div>
        `).join('');

        this.profileList.querySelectorAll('.profile-item').forEach(item => {
            item.addEventListener('click', () => {
                const childId = item.dataset.childId;
                const child = children.find(c => c.childId.toString() === childId);

                if (child) {
                    this.updateCurrentProfile(child);
                    this.profileDropdown.style.display = 'none';
                    sessionStorage.setItem('currentChildId', childId);

                    const event = new CustomEvent('childProfileChanged', {
                        detail: { childId, childInfo: child }
                    });
                    document.dispatchEvent(event);
                }
            });
        });
    }

    updateCurrentProfile(child) {
        if (!child || !this.currentChildName || !this.currentProfileImage) return;

        this.currentChildName.textContent = child.name;
        this.currentProfileImage.src = `/image/${child.imageUrl || 'profileDefault.png'}`;
        this.currentProfileImage.alt = `${child.name}의 프로필`;
        this.currentProfileImage.onerror = () => {
            this.currentProfileImage.src = '/image/profileDefault.png';
        };

        if (this.profileList) {
            this.profileList.querySelectorAll('.profile-item').forEach(item => {
                item.classList.toggle('active', item.dataset.childId === child.childId.toString());
            });
        }
    }

    showLoggedInState() {
        if (this.loggedInButtons) {
            this.loggedInButtons.style.display = 'flex';
        }
        if (this.loggedOutButtons) {
            this.loggedOutButtons.style.display = 'none';
        }
        if (this.profileSelector) {
            this.profileSelector.style.display = 'block';
        }
    }

    showLoggedOutState() {
        if (this.loggedInButtons) {
            this.loggedInButtons.style.display = 'none';
        }
        if (this.loggedOutButtons) {
            this.loggedOutButtons.style.display = 'flex';
        }
        if (this.profileSelector) {
            this.profileSelector.style.display = 'none';
        }
    }

    async handleLogout() {
        try {
            const response = await Api.get('/api/member/provider');
            const data = await response.json();
            const {provider} = data.data; // Extract data from CommonResponse

            if (provider === 'kakao') {
                await this.performKakaoLogout();
            } else {
                await this.performGeneralLogout();
            }
        } catch (error) {
            console.error('로그아웃 중 오류 발생:', error);
            alert('로그아웃 중 오류가 발생했습니다.');
        }
    }

    async performKakaoLogout() {
        sessionStorage.clear(); // 모든 sessionStorage 항목 제거
        document.cookie = 'refreshToken=; Path=/; Expires=Thu, 01 Jan 1970 00:00:01 GMT;';
        this.showLoggedOutState();
        window.location.href = '/auth/api/member/kakao-logout';
    }

    async performGeneralLogout() {
        try {
            const response = await Api.post('/logout');

            if (response.ok) {
                sessionStorage.clear(); // 모든 sessionStorage 항목 제거
                document.cookie = 'refreshToken=; Path=/; Expires=Thu, 01 Jan 1970 00:00:01 GMT;';
                this.showLoggedOutState();
                window.location.href = '/index.html';
            } else {
                alert('로그아웃 실패');
            }
        } catch (error) {
            console.error('로그아웃 처리 중 오류:', error);
            alert('로그아웃 처리 중 오류가 발생했습니다.');
        }
    }
}

// DOM이 완전히 로드된 후에 헤더를 초기화
document.addEventListener('DOMContentLoaded', () => {
    fetch('/html/header.html')
        .then(response => response.text())
        .then(html => {
            document.getElementById('header-container').innerHTML = html;
            new Header();
        })
        .catch(error => {
            console.error('Error loading header:', error);
        });
});