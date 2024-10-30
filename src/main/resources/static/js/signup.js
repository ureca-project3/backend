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
            showModal('회원가입에 성공했습니다!', true);
        } else {
            const errorData = await response.json();
            showModal(errorData.message || '회원가입에 실패했습니다.', false);
        }
    } catch (error) {
        showModal('서버와의 통신 중 오류가 발생했습니다.', false);
    }
});

function showModal(message, isSuccess) {
    const modal = document.getElementById('errorModal');
    const modalMessage = document.getElementById('modalMessage');
    modalMessage.textContent = message;
    modal.style.display = 'flex';

    // 성공 메시지인 경우 확인 버튼 클릭 시 리다이렉트
    const confirmButton = modal.querySelector('.modal-button');
    confirmButton.onclick = function() {
        if (isSuccess) {
            window.location.href = '/index.html'; // 리다이렉트할 URL
        } else {
            modal.style.display = 'none'; // 모달 닫기
        }
    };
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
