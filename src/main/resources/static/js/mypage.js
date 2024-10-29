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
document.getElementById('profile-edit-form').addEventListener('submit', async function(e) {
    e.preventDefault();

    const formData = new FormData(this);
    const profileData = Object.fromEntries(formData);

    try {
        const response = await fetch('/api/user/profile/update', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(profileData)
        });

        const result = await response.json();
        if (result.message === "Update Profile Success") {
            alert('프로필이 성공적으로 수정되었습니다.');
            location.reload();
        } else {
            alert('프로필 수정에 실패했습니다.');
        }
    } catch (error) {
        console.error('Error updating profile:', error);
        alert('프로필 수정 중 오류가 발생했습니다.');
    }
});

// 페이지 로드 시 프로필 정보 가져오기
window.onload = function() {
    fetchAndDisplayProfile();
};

// 탈퇴하기 버튼 클릭 시 확인 후 처리
document.getElementById('delete-account-button').addEventListener('click', function () {
    const confirmDelete = confirm("정말로 계정을 탈퇴하시겠습니까?");
    if (confirmDelete) {
        // 계정 삭제 로직 구현
        const accessToken = sessionStorage.getItem('accessToken');
        fetch('/mypage/delete-account', {
            method: 'DELETE',
            headers: {
                'Authorization': `Bearer ${accessToken}`,
                'Accept': 'application/json'
            }
        })
            .then(response => {
                if (response.ok) {
                    alert("계정이 성공적으로 삭제되었습니다.");
                    window.location.reload();
                } else {
                    alert("계정 삭제에 실패했습니다.");
                }
            })
            .catch(error => {
                console.error("계정 삭제 오류:", error);
                alert("계정 삭제에 실패했습니다. 에러: " + error.message);
            });
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
document.getElementById("birthdate").addEventListener("input", function(e) {
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
            if (response.status === 201) {
                return response.json();
            } else {
                throw new Error("자녀 등록에 실패하였습니다.");
            }
        })
        .then(data => {
            alert(data.message);
            window.location.href = `/childTestInfo.html?childName=${encodeURIComponent(childData.name)}`;
        })
        .catch(error => {
            console.error("Error:", error);
            alert("자녀 등록 중 오류가 발생했습니다.");
        });
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