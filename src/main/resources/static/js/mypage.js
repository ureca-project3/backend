// 로그아웃 버튼 기능
document.getElementById('logout-button').addEventListener('click', function() {
    sessionStorage.removeItem('accessToken');
    alert("로그아웃 되었습니다.");
    window.location.href = "/login.html";
});

// 페이지 로드 시 사용자 정보 로드
window.onload = function() {
    const accessToken = sessionStorage.getItem('accessToken');
    if (!accessToken) {
        alert("로그인 상태가 아닙니다.");
        window.location.href = "/login.html";
        return;
    }

    // 사용자 정보 요청
    fetch('/api/user/profile', {
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${accessToken}`
        }
    })
        .then(response => response.json())
        .then(data => {
            // 사용자 정보 표시
            document.getElementById('user-name').textContent += data.name;
            document.getElementById('user-email').textContent = data.email;
            document.getElementById('user-phone').textContent = data.phone;

            // 수정 폼 초기 값 설정
            document.getElementById('edit-name').value = data.name;
            document.getElementById('edit-email').value = data.email;
            document.getElementById('edit-phone').value = data.phone;

            // 자녀 프로필 추가
            const childProfiles = document.getElementById('child-profiles');
            data.children.forEach(child => {
                const listItem = document.createElement('li');
                listItem.classList.add('list-group-item');
                listItem.innerHTML = `
                        이름: ${child.name}, 나이: ${child.age}, 성별: ${child.gender}
                        <button class="btn btn-warning btn-sm float-end edit-child" data-id="${child.id}">수정</button>
                        <button class="btn btn-danger btn-sm float-end me-2 delete-child" data-id="${child.id}">삭제</button>
                    `;
                childProfiles.appendChild(listItem);
            });
        })
        .catch(error => {
            console.error("Error fetching profile:", error);
            alert("사용자 정보를 불러올 수 없습니다.");
        });
}

// 프로필 수정 버튼 클릭 시 폼 토글
document.getElementById('edit-profile-button').addEventListener('click', function() {
    const editProfileDiv = document.getElementById('edit-profile');
    editProfileDiv.style.display = editProfileDiv.style.display === 'none' || editProfileDiv.style.display === '' ? 'block' : 'none';
});

// 프로필 수정 폼 제출
document.getElementById('profile-edit-form').addEventListener('submit', function(event) {
    event.preventDefault();
    const accessToken = sessionStorage.getItem('accessToken');

    // 수정된 사용자 정보
    const updatedData = {
        name: document.getElementById('edit-name').value,
        password: document.getElementById('edit-password').value,
        phoneNumber: document.getElementById('edit-phone').value
    };

    // 사용자 정보 수정 요청
    fetch('/mypage/my-info', {
        method: 'PATCH', // PATCH 메서드 사용
        headers: {
            'Authorization': `Bearer ${accessToken}`,
            'Content-Type': 'application/json',
            'Accept': 'application/json'
        },
        body: JSON.stringify(updatedData)
    })
        .then(response => {
            if (!response.ok) {
                throw new Error("프로필 수정 실패");
            }
            return response.json();
        })
        .then(data => {
            if (data.message === "Update MyInfo Success") {
                alert("프로필이 수정되었습니다.");
                window.location.reload(); // 페이지 새로 고침
            } else {
                alert("프로필 수정에 실패했습니다.");
            }
        })
        .catch(error => {
            console.error("Error updating profile:", error);
            alert("프로필 수정에 실패했습니다.");
        });
});


// 자녀 프로필 삭제 기능
document.getElementById('child-profiles').addEventListener('click', function(event) {
    if (event.target.classList.contains('delete-child')) {
        const childId = event.target.getAttribute('data-id');
        const accessToken = sessionStorage.getItem('accessToken');

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
                console.error("Error deleting child profile:", error);
                alert("자녀 삭제에 실패했습니다.");
            });
    }
});

// 자녀 프로필 수정 기능
document.getElementById('child-profiles').addEventListener('click', function(event) {
    if (event.target.classList.contains('edit-child')) {
        const childId = event.target.getAttribute('data-id');
        const accessToken = sessionStorage.getItem('accessToken');

        // 자녀 정보 요청
        fetch(`/api/user/children/${childId}`, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${accessToken}`
            }
        })
            .then(response => response.json())
            .then(child => {
                // 수정 폼에 자녀 정보 설정
                document.getElementById('edit-child-name').value = child.name;
                document.getElementById('edit-child-age').value = child.age;
                document.getElementById('edit-child-gender').value = child.gender;
                // 수정 버튼 클릭 시 자녀 ID 저장
                document.getElementById('child-id').value = childId;
                // 수정 폼 표시
                document.getElementById('edit-child-profile').style.display = 'block';
            })
            .catch(error => {
                console.error("Error fetching child profile:", error);
                alert("자녀 정보를 불러올 수 없습니다.");
            });
    }
});

// 자녀 프로필 수정 폼 제출
document.getElementById('child-edit-form').addEventListener('submit', function(event) {
    event.preventDefault();
    const accessToken = sessionStorage.getItem('accessToken');
    const childId = document.getElementById('child-id').value;

    // 수정된 자녀 정보
    const updatedChildData = {
        name: document.getElementById('edit-child-name').value,
        age: document.getElementById('edit-child-age').value,
        gender: document.getElementById('edit-child-gender').value
    };

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
            console.error("Error updating child profile:", error);
            alert("자녀 프로필 수정에 실패했습니다.");
        });
});

// 탈퇴하기 버튼 기능
document.getElementById('delete-account-button').addEventListener('click', function() {
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
                window.location.href = "/login.html"; // 로그인 페이지로 이동
            })
            .catch(error => {
                console.error("Error deleting account:", error);
                alert("계정 탈퇴에 실패했습니다.");
            });
    }
});
