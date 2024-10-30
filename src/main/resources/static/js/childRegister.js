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
// 나중에 등록하기
function handleLaterButton() {
    if (confirm('자녀 등록을 건너뛰시겠습니까?')) {
        window.location.href = '/mypage.html';
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