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
        this.currentProfileBtn = document.getElementById('current-profile');
        this.profileDropdown = document.querySelector('.profile-dropdown');
        this.profileList = document.querySelector('.profile-list');
        this.logoutButton = document.getElementById('logout-button');
    }

    initializeEventListeners() {
        if (this.currentProfileBtn) {
            this.currentProfileBtn.addEventListener('click', () => {
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
            const response = await fetch('/api/member/children', {
                headers: {
                    'Authorization': `Bearer ${sessionStorage.getItem('accessToken')}`
                }
            });

            if (!response.ok) throw new Error('Failed to fetch children');

            const children = await response.json();
            this.renderChildrenProfiles(children);

            // 저장된 현재 선택된 자녀가 있다면 표시
            const currentChildId = localStorage.getItem('currentChildId');
            if (currentChildId && children.length > 0) {
                const currentChild = children.find(child => child.id === currentChildId);
                if (currentChild) {
                    this.updateCurrentProfile(currentChild);
                } else {
                    this.updateCurrentProfile(children[0]);
                }
            } else if (children.length > 0) {
                this.updateCurrentProfile(children[0]);
            }
        } catch (error) {
            console.error('Error fetching children:', error);
        }
    }

    renderChildrenProfiles(children) {
        if (!this.profileList) return;

        this.profileList.innerHTML = children.map(child => `
        <div class="profile-item" data-child-id="${child.childId}">
            <img class="profile-item-image" 
                 src="/image/${child.imageUrl || 'profileDefault.png'}" 
                 alt="${child.name}의 프로필">
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
                    if (this.profileDropdown) {
                        this.profileDropdown.style.display = 'none';
                    }

                    localStorage.setItem('currentChildId', childId);

                    const event = new CustomEvent('childProfileChanged', {
                        detail: { childId, childInfo: child }
                    });
                    document.dispatchEvent(event);
                }
            });
        });
    }

    updateCurrentProfile(child) {
        if (!child) return;

        const currentChildNameElement = document.getElementById('current-child-name');
        const currentProfileImage = document.getElementById('current-profile-image');

        if (currentChildNameElement) {
            currentChildNameElement.textContent = child.name;
        }
        if (currentProfileImage) {
            // 이미지 경로를 /image/ 디렉토리 하위로 수정
            currentProfileImage.src = `/image/${child.imageUrl || 'profileDefault.png'}`;
            currentProfileImage.alt = `${child.name}의 프로필`;
        }

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
            const response = await fetch('/api/member/provider', {
                method: 'GET',
                headers: {
                    'Authorization': `Bearer ${sessionStorage.getItem('accessToken')}`
                }
            });
            const { provider } = await response.json();

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
        sessionStorage.removeItem('accessToken');
        localStorage.removeItem('currentChildId');
        document.cookie = 'refreshToken=; Path=/; Expires=Thu, 01 Jan 1970 00:00:01 GMT;';
        this.showLoggedOutState();
        window.location.href = '/auth/api/member/kakao-logout';
    }

    async performGeneralLogout() {
        try {
            const response = await fetch('/logout', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' }
            });

            if (response.ok) {
                sessionStorage.removeItem('accessToken');
                localStorage.removeItem('currentChildId');
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
    fetch('/header.html')
        .then(response => response.text())
        .then(html => {
            document.getElementById('header-container').innerHTML = html;
            new Header();
        })
        .catch(error => {
            console.error('Error loading header:', error);
        });
});