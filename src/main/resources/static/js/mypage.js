// 프로필 정보 가져오기 및 표시
async function fetchAndDisplayProfile() {
    try {
        const response = await fetch('/api/user/profile');
        const data = await response.json();
        const profileData = data.data;

        if (profileData) {
            // 기본 프로필 정보 표시
            document.getElementById('user-name').textContent = `이름: ${profileData.name || ''}`;
            document.getElementById('user-email').textContent = profileData.email || '';
            document.getElementById('user-phone').textContent = profileData.phone || '';

            // provider 확인하여 수정 버튼 표시/숨김 처리
            const editButton = document.querySelector('.button-container button:first-child');
            if (profileData.provider === 'kakao') {
                editButton.style.display = 'none';
            } else {
                editButton.style.display = 'block';
            }

            // 수정 폼 초기값 설정
            document.getElementById('edit-name').value = profileData.name || '';
            document.getElementById('edit-email').value = profileData.email || '';
            document.getElementById('edit-phone').value = profileData.phone || '';

            // 자녀 프로필 표시
            const childrenList = document.querySelector('.children-list');
            if (childrenList && profileData.children && Array.isArray(profileData.children)) {
                childrenList.innerHTML = profileData.children.map(child => `
                            <div class="child-item">
                                <img src="/image/${child.imageUrl || 'profileDefault.png'}"
                                     alt="${child.name}의 프로필"
                                     onerror="this.src='/image/profileDefault.png'">
                                <div class="child-info">
                                    <div class="child-name">${child.name}</div>
                                    <div class="child-age">${child.age}세</div>
                                </div>
                            </div>
                        `).join('');
            }
        }
    } catch (error) {
        console.error("사용자 정보 로드 오류:", error);
        alert("사용자 정보를 불러올 수 없습니다.");
    }
}


// 수정 폼 토글
function toggleEditProfile() {
    const editProfile = document.getElementById('edit-profile');
    editProfile.style.display = editProfile.style.display === 'none' ? 'block' : 'none';
}

// 프로필 수정 폼 제출 처리
document.getElementById('profile-edit-form').addEventListener('submit', async function (e) {
    e.preventDefault();

    try {
        // 현재 사용자의 provider 정보 확인
        const profileResponse = await fetch('/mypage/my-info');
        const profileData = await profileResponse.json();

        if (profileData.data.provider === 'kakao') {
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
        // 수정 요청
        const response = await fetch('/mypage/my-info', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(updateData)
        });

        const result = await response.json();
        if (result.message === "Update Profile Success") {
            alert('프로필이 성공적으로 수정되었습니다.');
            location.reload();
        } else {
            // 서버에서 받은 에러 메시지 표시
            if (result.message.includes("이메일이 이미 존재합니다")) {
                alert("이미 사용 중인 이메일입니다.");
            } else {
                alert(result.message || '프로필 수정에 실패했습니다.');
            }
        }
    } catch (error) {
        console.error('Error updating profile:', error);
        alert('프로필 수정 중 오류가 발생했습니다.');
    }
});

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


// 자녀 프로필 삭제 기능
document.getElementById('child-profiles').addEventListener('click', function (event) {
    if (event.target.classList.contains('delete-child')) {
        const childId = event.target.getAttribute('data-id');
        const accessToken = sessionStorage.getItem('accessToken');

        console.log("자녀 삭제 요청 ID:", childId);

        // 자녀 삭제 요청
        fetch(`/api/user/children/${childId}`, {
            method: 'DELETE',
            headers: {
                'Authorization': `Bearer ${accessToken}`
            }
        })
            .then(response => {
                if (!response.ok) {
                    throw new Error("자녀 삭제 실패");
                }
                alert("자녀 프로필이 삭제되었습니다.");
                window.location.reload(); // 페이지 새로 고침
            })
            .catch(error => {
                console.error("자녀 삭제 오류:", error);
                alert("자녀 삭제에 실패했습니다.");
            });
    }
});

// 자녀 프로필 수정 기능
document.getElementById('child-profiles').addEventListener('click', function (event) {
    if (event.target.classList.contains('edit-child')) {
        const childId = event.target.getAttribute('data-id');
        const accessToken = sessionStorage.getItem('accessToken');

        console.log("자녀 수정 요청 ID:", childId);

        // 자녀 정보 요청
        fetch(`/api/user/children/${childId}`, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${accessToken}`
            }
        })
            .then(response => {
                if (!response.ok) {
                    throw new Error("자녀 정보 요청 실패");
                }
                return response.json();
            })
            .then(child => {
                console.log("자녀 정보 로드 성공:", child);

                // 수정 폼에 자녀 정보 설정
                const childNameInput = document.getElementById('edit-child-name');
                const childAgeInput = document.getElementById('edit-child-age');
                const childGenderInput = document.getElementById('edit-child-gender');

                // 한 번에 DOM 업데이트
                childNameInput.value = child.name;
                childAgeInput.value = child.age;
                childGenderInput.value = child.gender;

                // 수정 버튼 클릭 시 자녀 ID 저장
                document.getElementById('child-id').value = childId;

                // 수정 폼 표시
                document.getElementById('edit-child-profile').style.display = 'block';
            })
            .catch(error => {
                console.error("자녀 정보 로드 오류:", error);
                alert("자녀 정보를 불러올 수 없습니다.");
            });
    }
});

// 자녀 프로필 수정 폼 제출
document.getElementById('child-edit-form').addEventListener('submit', function (event) {
    event.preventDefault();
    const accessToken = sessionStorage.getItem('accessToken');
    const childId = document.getElementById('child-id').value;

    // 수정된 자녀 정보
    const updatedChildData = {
        name: document.getElementById('edit-child-name').value,
        age: document.getElementById('edit-child-age').value,
        gender: document.getElementById('edit-child-gender').value
    };

    console.log("자녀 수정 요청 데이터:", updatedChildData);

    // 자녀 정보 수정 요청
    fetch(`/api/user/children/${childId}`, {
        method: 'PUT', // Assuming the API uses PUT for updates
        headers: {
            'Authorization': `Bearer ${accessToken}`,
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(updatedChildData)
    })
        .then(response => {
            if (!response.ok) {
                throw new Error("자녀 프로필 수정 실패");
            }
            alert("자녀 프로필이 수정되었습니다.");
            window.location.reload(); // 페이지 새로 고침
        })
        .catch(error => {
            console.error("자녀 프로필 수정 오류:", error);
            alert("자녀 프로필 수정에 실패했습니다.");
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


// 자녀 등록
const images = ['profile1.png', 'profile2.png', 'profile3.png', 'profile4.png', 'profile5.png', 'profile6.png', 'profile7.png'];
let selectedImage = 'profileDefault.png';
let selectedGender = null;

// 프로필 이미지 선택 모달 열기
function openModal() {
    const imageOptions = document.getElementById("image-options");
    imageOptions.innerHTML = ''; // 기존 이미지 삭제
    images.forEach(image => {
        const img = document.createElement("img");
        img.src = `/image/${image}`;
        img.alt = image;
        img.onclick = () => selectImage(img, image);
        imageOptions.appendChild(img);
    });
    document.getElementById("imageModal").style.display = "flex";
}

// 이미지 선택
function selectImage(imgElement, image) {
    document.querySelectorAll("#image-options img").forEach(img => img.classList.remove("selected"));
    imgElement.classList.add("selected");
    selectedImage = image;
}

// 선택된 이미지 확인 및 설정
function confirmImageSelection() {
    document.getElementById("profile-img").src = `/image/${selectedImage}`;
    document.getElementById("imageModal").style.display = "none";
}

// 성별 선택 기능
function selectGender(gender) {
    selectedGender = gender;
    document.getElementById("gender-male").classList.toggle("btn-dark", gender === "남");
    document.getElementById("gender-male").classList.toggle("btn-outline-dark", gender !== "남");
    document.getElementById("gender-female").classList.toggle("btn-dark", gender === "여");
    document.getElementById("gender-female").classList.toggle("btn-outline-dark", gender !== "여");
}

// 생년월일 입력 형식
document.getElementById("birthdate").addEventListener("input", function (e) {
    let input = e.target.value.replace(/[^0-9]/g, "");
    if (input.length >= 4) input = input.slice(0, 4) + "." + input.slice(4);
    if (input.length >= 7) input = input.slice(0, 7) + "." + input.slice(7, 9);
    e.target.value = input;
});

// 자녀 등록 API 호출
function registerChild() {
    const childName = document.getElementById('child-name').value;
    const birthDate = document.getElementById('birthdate').value;

    // 입력값 검증
    let isValid = true;
    if (!childName) {
        document.getElementById('child-name').classList.add('highlight');
        isValid = false;
    } else {
        document.getElementById('child-name').classList.remove('highlight');
    }

    if (!birthDate) {
        document.getElementById('birthdate').classList.add('highlight');
        isValid = false;
    } else {
        document.getElementById('birthdate').classList.remove('highlight');
    }

    if (!selectedGender) {
        document.querySelector('.gender-selection').classList.add('highlight');
        isValid = false;
    } else {
        document.querySelector('.gender-selection').classList.remove('highlight');
    }

    if (!isValid) {
        alert("모든 필드를 작성해 주세요.");
        return;
    }

    const accessToken = sessionStorage.getItem('accessToken');
    if (!accessToken) {
        alert("로그인이 필요합니다.");
        window.location.href = "/login.html";
        return;
    }

    // 입력된 자녀 정보 가져오기
    const childData = {
        name: childName,
        age: calculateAge(birthDate),
        birthDate: birthDate,
        gender: selectedGender === "남" ? "M" : "F",
        profileImage: selectedImage
    };

    // API 호출
    fetch('/mypage/child-info', {
        method: 'POST',
        headers: {
            'Accept': 'application/json',
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${accessToken}`
        },
        body: JSON.stringify(childData)
    })
        .then(response => {
            if (response.ok) {
                return response.json();
            }
            throw new Error("자녀 등록에 실패하였습니다.");
        })
        .then(data => {
            alert("자녀가 성공적으로 등록되었습니다.");
            window.location.href = `/childTestInfo.html?childName=${encodeURIComponent(childData.name)}`;
        })
        .catch(error => {
            console.error("Error:", error);
            alert("자녀 등록 중 오류가 발생했습니다.");
        });
}
// 자녀 프로필 모달 관련 코드
let childProfileModal = null;
let imageModal = null;

document.addEventListener('DOMContentLoaded', function() {
    // 모달 초기화
    childProfileModal = new bootstrap.Modal(document.getElementById('childProfileModal'));
    imageModal = new bootstrap.Modal(document.getElementById('imageModal'));
});

// 모달 관련 함수들
function openChildModal() {
    document.getElementById('childProfileModal').style.display = 'flex';
}

function closeChildModal() {
    document.getElementById('childProfileModal').style.display = 'none';
}
// 이미지 선택 모달 열기
function openModal() {
    // 이미지 옵션 초기화
    const imageOptions = document.getElementById("image-options");
    imageOptions.innerHTML = ''; // 기존 이미지 삭제
    images.forEach(image => {
        const img = document.createElement("img");
        img.src = `/image/${image}`;
        img.alt = image;
        img.onclick = () => selectImage(img, image);
        imageOptions.appendChild(img);
    });
    imageModal.show();
}

// 이미지 선택 모달 닫기
function closeImageModal() {
    imageModal.hide();
}

// 이미지 선택 확인
function confirmImageSelection() {
    document.getElementById("profile-img").src = `/image/${selectedImage}`;
    closeImageModal();
}
// 생년월일 입력 형식화
function formatBirthDate(input) {
    let value = input.value.replace(/\D/g, '');
    if (value.length >= 4) {
        value = value.slice(0, 4) + '.' + value.slice(4);
    }
    if (value.length >= 7) {
        value = value.slice(0, 7) + '.' + value.slice(7, 9);
    }
    input.value = value;
}

// 등록하기 버튼 활성화/비활성화
function updateRegisterButton() {
    const childName = document.getElementById('child-name').value;
    const birthDate = document.getElementById('birthdate').value;
    const registerBtn = document.querySelector('.register-btn');

    if (childName && birthDate && selectedGender) {
        registerBtn.disabled = false;
        registerBtn.style.opacity = '1';
    } else {
        registerBtn.disabled = true;
        registerBtn.style.opacity = '0.5';
    }
}
function calculateAge(birthDate) {
    const birth = new Date(birthDate);
    const today = new Date();
    let age = today.getFullYear() - birth.getFullYear();
    const m = today.getMonth() - birth.getMonth();
    if (m < 0 || (m === 0 && today.getDate() < birth.getDate())) {
        age--;
    }
    return age;
}