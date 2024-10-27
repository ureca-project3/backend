document.querySelector("form").addEventListener("submit", function(event) {
    event.preventDefault(); // 기본 폼 제출 방지

    const email = document.getElementById("email").value;
    const password = document.getElementById("password").value;

    // 로그인 요청 보내기
    fetch("/login", {
        method: "POST",
        headers: {
            "Content-Type": "application/x-www-form-urlencoded",
        },
        body: new URLSearchParams({
            email: email,
            password: password,
        }),
    })
        .then(response => {
            if (!response.ok) {
                throw new Error("로그인 실패"); // 실패 시 에러 처리
            }
            return response.json(); // JSON 응답으로 변환
        })
        .then(data => {
            // 로그인 성공 시
            alert(data.message); // 성공 메시지 표시

            // Access Token과 Refresh Token을 localStorage에 저장
            sessionStorage.setItem('accessToken', data.data.accessToken);
            //sessionStorage.setItem('refreshToken', data.data.refreshToken);
            document.cookie = `refreshToken=${data.data.refreshToken}; path=/; secure; HttpOnly;`;
            // 리다이렉션 처리
            window.location.href = "/index.html"; // index.html로 이동
        })
        .catch(error => {
            console.error("Error:", error);
            alert("로그인에 실패했습니다."); // 실패 메시지 표시
        });
});
