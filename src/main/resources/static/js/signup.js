document.getElementById('signupForm').addEventListener('submit', async function(e) {
    e.preventDefault();

    const formData = {
        memberName: document.getElementById('memberName').value,
        email: document.getElementById('email').value,
        password: document.getElementById('password').value,
        phone: document.getElementById('phone').value
    };

    try {
        const response = await fetch('/auth/signup', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(formData)
        });

        if (response.ok) {
            // 회원가입 성공 시 모달 창에 성공 메시지 표시
            showModal('회원가입에 성공했습니다!');
            // 일정 시간 후에 리다이렉트
            setTimeout(() => {
                window.location.href = '/index.html';
            }, 5000); // 5초 후에 리다이렉트
        } else {
            const errorData = await response.json();
            showModal(errorData.message || '회원가입에 실패했습니다.');
        }
    } catch (error) {
        showModal('서버와의 통신 중 오류가 발생했습니다.');
    }
});

function showModal(message) {
    const modal = document.getElementById('errorModal');
    const modalMessage = document.getElementById('modalMessage');
    modalMessage.textContent = message;
    modal.style.display = 'flex';
}

function closeModal() {
    const modal = document.getElementById('errorModal');
    modal.style.display = 'none';
}

// 모달 외부 클릭 시 닫기
window.onclick = function(event) {
    const modal = document.getElementById('errorModal');
    if (event.target === modal) {
        modal.style.display = 'none';
    }
}
