// 프로필 정보 가져오기 및 표시
async function fetchAndDisplayProfile() {
    try {
        const accessToken = sessionStorage.getItem('accessToken');
        const response = await fetch('/mypage/my-info', {
            headers: {
                'Authorization': `Bearer ${accessToken}`
            }
        });

        // 네트워크 연결 문제 처리
        if (!response.ok) {
            const errorMessage = await response.text();
            throw new Error(`Failed to fetch user profile data: ${errorMessage}`);
        }

        const data = await response.json();
        console.log('API Response:', data);
        const profileData = data.member;
        console.log('Profile Data:', profileData);
        console.log('Children Data:', profileData.children);

        if (profileData) {
            // 기본 프로필 정보 표시
            document.getElementById('user-name').textContent = `이름: ${profileData.name || ''}`;
            document.getElementById('user-email').textContent = profileData.email || '';
            document.getElementById('user-phone').textContent = profileData.phone || '';
            document.getElementById('user-provider').textContent = profileData.provider || '';

            // provider 확인하여 수정 버튼 표시/숨김 처리
            const editButton = document.querySelector('.button-container button:first-child');
            if (editButton) {
                if (profileData.provider === 'kakao') {
                    editButton.style.display = 'none';
                } else {
                    editButton.style.display = 'block';
                }
            }

            // 수정 폼 초기값 설정
            const editNameInput = document.getElementById('edit-name');
            const editEmailInput = document.getElementById('edit-email');
            const editPhoneInput = document.getElementById('edit-phone');
            const editPasswordInput = document.getElementById('edit-password');

            if (editNameInput) editNameInput.value = profileData.name || '';
            if (editEmailInput) editEmailInput.value = profileData.email || '';
            if (editPhoneInput) editPhoneInput.value = profileData.phone || '';
            if (editPasswordInput) editPasswordInput.value = '';

            // 자녀 프로필 표시
            const childProfiles = document.getElementById('child-profiles');
            if (childProfiles) {
                if (profileData.children && Array.isArray(profileData.children) && profileData.children.length > 0) {
                    childProfiles.innerHTML = profileData.children.map(child => `
                        <li class="list-group-item d-flex justify-content-between align-items-center" data-id="${child.childId}">
                            <div class="child-info d-flex align-items-center" 
                                 onclick="goToChildDetail('${child.childId}')" 
                                 data-id="${child.childId}">
                                <img src="/image/${child.imageUrl || 'profileDefault.png'}"
                                     alt="${child.name}의 프로필"
                                     onerror="this.src='/image/profileDefault.png'"
                                     style="width: 40px; height: 40px; border-radius: 50%; margin-right: 10px;">
                                <div>
                                    <span class="child-name fw-bold">${child.name}</span>
                                    <span class="child-age ms-2">${child.age}세</span>
                                </div>
                            </div>
                            <div class="child-actions">
                                <button class="btn btn-sm btn-outline-secondary edit-child me-2" data-id="${child.childId}" onclick="goToChildDetail('${child.childId}')">정보</button>
                                <button class="btn btn-sm btn-outline-danger delete-child" data-id="${child.childId}">삭제</button>
                            </div>
                        </li>
                    `).join('');
                } else {
                    childProfiles.innerHTML = '<li class="list-group-item text-center">등록된 자녀가 없습니다.</li>';
                }
            } else {
                console.error("자녀 프로필 목록 엘리먼트를 찾을 수 없습니다.");
            }
        }
    } catch (error) {
        console.error("사용자 정보 로드 오류:", error);
        console.error("Error details:", error.stack);

        // 네트워크 연결 오류 처리
        if (error.message.includes('Failed to fetch')) {
            const errorMessage = error.message.split(':')[1].trim();
            alert(`네트워크 연결에 실패했습니다. 에러 메시지: ${errorMessage}`);
        } else {
            alert('사용자 정보를 불러올 수 없습니다. 자세한 내용은 콘솔을 확인해주세요.');
        }
    }
}

// 수정 폼 토글
function toggleEditProfile() {
    const editProfile = document.getElementById('edit-profile');
    const profileInfo = document.querySelector('.profile-header'); // 이름 부분
    const contactInfo = document.querySelector('.profile-info');   // 이메일, 전화번호 부분

    // 수정 폼이 숨겨져 있는 상태라면
    if (editProfile.style.display === 'none') {
        // 수정 폼 표시
        editProfile.style.display = 'block';
        // 기존 정보 숨기기
        profileInfo.style.display = 'none';
        contactInfo.style.display = 'none';
    } else {
        // 수정 폼 숨기기
        editProfile.style.display = 'none';
        // 기존 정보 표시
        profileInfo.style.display = 'block';
        contactInfo.style.display = 'block';
    }
}

// 프로필 수정 폼
document.getElementById('profile-edit-form').addEventListener('submit', async function (e) {
    e.preventDefault();

    try {
        // accessToken 가져오기
        const accessToken = sessionStorage.getItem('accessToken');

        // 현재 사용자의 provider 정보 확인
        const profileResponse = await fetch('/mypage/my-info', {
            headers: {
                'Authorization': `Bearer ${accessToken}`
            }
        });
        const profileData = await profileResponse.json();

        if (profileData.member.provider === 'kakao') {
            alert('카카오 로그인 사용자는 정보를 수정할 수 없습니다.');
            return;
        }

        const formData = new FormData(this);
        const updateData = {
            name: formData.get('name'),
            email: formData.get('email'),
            phone: formData.get('phone'),
            password: formData.get('password')
        };

        // 수정 요청 - Authorization 헤더 추가
        const response = await fetch('/mypage/my-info', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${accessToken}`
            },
            body: JSON.stringify(updateData)
        });

        const result = await response.json();

        if (!response.ok) {
            if (result.message && result.message.includes("이메일이 이미 존재합니다")) {
                alert("이미 사용 중인 이메일입니다.");
            } else if (result.message && result.message.includes("Access token is invalid or expired")) {
                // 토큰이 유효하지 않거나 만료된 경우
                console.error('Access token is invalid or expired. Redirecting to login page.');
                sessionStorage.removeItem('accessToken');
                window.location.href = '/login.html';
            } else if (result.message && result.message.includes("카카오 로그인 사용자는 정보를 수정할 수 없습니다")) {
                // 카카오 사용자 수정 불가 처리
                alert(result.message);
            } else {
                alert(result.message || '프로필 수정에 실패했습니다.');
            }
            return;
        }

        if (result.message === "Update MyInfo Success") {
            alert('프로필이 성공적으로 수정되었습니다.');
            // 수정 폼 숨기기 및 프로필 정보 다시 표시
            toggleEditProfile();
            // 프로필 정보 새로고침
            fetchAndDisplayProfile();
            location.reload();
        } else {
            alert(result.message || '프로필 수정에 실패했습니다.');
        }
    } catch (error) {
        console.error('Error updating profile:', error);

        // 네트워크 연결 문제 처리
        if (error.message && error.message.includes('Failed to fetch')) {
            alert('네트워크 연결이 불안정하여 프로필 수정에 실패했습니다. 인터넷 연결을 확인하고 다시 시도해주세요.');
        } else if (error.message && error.message.includes('Access token is invalid or expired')) {
            // 토큰이 유효하지 않거나 만료된 경우
            console.error('Access token is invalid or expired. Redirecting to login page.');
            sessionStorage.removeItem('accessToken');
            window.location.href = '/login.html';
        } else {
            alert('프로필 수정 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.');
        }
    }
});

// 수정 폼 초기값 설정
const editPasswordInput = document.getElementById('edit-password');
if (editPasswordInput) {
    // 현재 비밀번호를 마스킹하여 초기값으로 표시
    editPasswordInput.value = '****';
}
// 페이지 로드 시 프로필 정보 가져오기
window.onload = function () {
    fetchAndDisplayProfile();
};

// 1. 이벤트 리스너 등록
document.getElementById('delete-account-button').addEventListener('click', async function () {
    try {
        // 2. 사용자 정보 조회
        const profileResponse = await fetch('/api/user/profile');
        const profileData = await profileResponse.json();
        // 카카오 사용자 여부 확인
        const isKakaoUser = profileData.data.provider === 'kakao';

        // 3. 사용자 유형에 따른 확인 메시지 설정
        const confirmMessage = isKakaoUser
            ? "카카오 계정 연동이 해제되며, 모든 데이터가 삭제됩니다. 정말 탈퇴하시겠습니까?"
            : "정말로 계정을 탈퇴하시겠습니까? 탈퇴 시 모든 데이터가 삭제됩니다.";

        // 4. 사용자 확인
        if (confirm(confirmMessage)) {
            // 5. 탈퇴 요청 전송
            const accessToken = sessionStorage.getItem('accessToken');
            const response = await fetch('/mypage/delete-account', {
                method: 'DELETE',
                headers: {
                    'Authorization': `Bearer ${accessToken}`,
                    'Accept': 'application/json'
                }
            });

            // 6. 응답 확인
            if (!response.ok) {
                throw new Error("계정 탈퇴 처리 중 오류가 발생했습니다.");
            }

            // 7. 탈퇴 성공 처리
            sessionStorage.removeItem('accessToken');  // 토큰 제거

            // 8. 사용자 유형에 따른 성공 메시지
            alert(isKakaoUser
                ? "카카오 계정 연동이 해제되었으며, 계정이 성공적으로 삭제되었습니다."
                : "계정이 성공적으로 삭제되었습니다.");

            // 9. 카카오 사용자 추가 처리
            if (isKakaoUser && window.Kakao && window.Kakao.isInitialized()) {
                await window.Kakao.Auth.logout();  // 카카오 로그아웃
            }

            // 10. 로그인 페이지로 이동
            window.location.href = "/login.html";
        }
    } catch (error) {
        // 11. 에러 처리
        console.error("계정 탈퇴 오류:", error);
        alert("계정 탈퇴 처리 중 오류가 발생했습니다.");
    }
});


// 자녀 프로필 삭제
document.getElementById('child-profiles').addEventListener('click', function (event) {
    if (event.target.classList.contains('delete-child')) {
        // 삭제 버튼 클릭 시 처리
        const childId = event.target.getAttribute('data-id');
        const accessToken = sessionStorage.getItem('accessToken');

        if (confirm("정말로 자녀 프로필을 삭제하시겠습니까?")) {
            fetch(`/mypage/child-child-info/${childId}`, {
                method: 'DELETE',
                headers: {
                    'Authorization': `Bearer ${accessToken}`,
                    'Accept': 'application/json'
                }
            })
                .then(response => {
                    if (response.ok) {
                        alert("자녀 프로필이 삭제되었습니다.");
                        location.reload(); // 페이지 새로고침으로 변경
                    } else {
                        throw new Error("자녀 프로필 삭제 실패");
                    }
                })
                .catch(error => {
                    console.error("자녀 프로필 삭제 오류:", error);
                    alert("자녀 프로필 삭제에 실패했습니다.");
                });
        }
    } else if (event.target.classList.contains('child-info')) { // 클래스 이름 변경
        // 자녀 정보 버튼 클릭 시 처리
        const childId = event.target.closest('li').getAttribute('data-id'); // li 요소의 data-id 가져오기
        window.location.href = `/childDetail.html?id=${childId}`; // 페이지 이동
    }
});

// 자녀 등록 버튼 클릭 이벤트
document.querySelector('.bottom-info button').addEventListener('click', function () {
    window.location.href = '/childRegister.html';
});

// 자녀 프로필 등록 폼 제출
const childRegistrationForm = document.getElementById('child-registration-form'); // 폼 요소 가져오기

childRegistrationForm.addEventListener('submit', function (event) {
    event.preventDefault(); // 기본 폼 제출 방지

    // 폼 데이터 가져오기
    const childName = document.getElementById('child-name').value;
    const birthDate = document.getElementById('birthdate').value;
    const gender = document.querySelector('input[name="gender"]:checked').value;
    const profileImage = document.getElementById('profile-img').src; // 이미지 URL 가져오기

    // API 요청
    fetch('/mypage/child-info', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${sessionStorage.getItem('accessToken')}`
        },
        body: JSON.stringify({
            name: childName,
            birthDate: birthDate,
            gender: gender,
            profileImage: profileImage // 이미지 URL을 API로 전송
        })
    })
        .then(response => {
            if (response.ok) {
                alert("자녀 프로필이 등록되었습니다.");
                window.location.href = '/mypage.html'; // 마이페이지로 리디렉션
            } else {
                throw new Error("자녀 프로필 등록 실패");
            }
        })
        .catch(error => {
            console.error("자녀 프로필 등록 오류:", error);
            alert("자녀 프로필 등록에 실패했습니다.");
        });
});

// 탈퇴하기 버튼 기능
document.getElementById('delete-account-button').addEventListener('click', function () {
    const accessToken = sessionStorage.getItem('accessToken');

    if (confirm("정말로 계정을 탈퇴하시겠습니까? 탈퇴 시 모든 데이터가 삭제됩니다.")) {
        fetch('/mypage/my-info', {
            method: 'DELETE', // DELETE 메서드 사용
            headers: {
                'Authorization': `Bearer ${accessToken}`
            }
        })
            .then(response => {
                if (!response.ok) {
                    throw new Error("계정 탈퇴 실패");
                }
                // 탈퇴 성공 시 로그아웃 처리
                sessionStorage.removeItem('accessToken');
                alert("계정이 성공적으로 삭제되었습니다.");
                console.log("계정 탈퇴 성공.");
                window.location.href = "/login.html";
            })
            .catch(error => {
                console.error("계정 탈퇴 오류:", error);
                alert("계정 탈퇴에 실패했습니다.");
            });
    }
});

// 자녀 등록 버튼 클릭 이벤트
document.getElementById('add-child-profile-button').addEventListener('click', function () {
    window.location.href = '/childRegister.html'; // 자녀 등록 페이지로 이동
});
// 자녀 상세 페이지로 이동하는 함수
function goToChildDetail(childId) {
    window.location.href = `/testHistory.html?id=${childId}`;
}