document.querySelector("form").addEventListener("submit", function(event) {
    event.preventDefault();

    const email = document.getElementById("email").value;
    const password = document.getElementById("password").value;

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
                throw new Error("로그인 실패");
            }
            return response.json();
        })
        .then(data => {
            alert(data.message);

            sessionStorage.setItem('accessToken', data.data.accessToken);
            document.cookie = `refreshToken=${data.data.refreshToken}; path=/; secure; HttpOnly;`;

            // Access Token 디코딩 및 role 확인
            const decodedToken = jwt_decode(data.data.accessToken);
            console.log("Decoded Token:", decodedToken);

            // role 값에서 대괄호 제거
            const userRole = decodedToken.role.replace(/[\[\]]/g, "");
            console.log("User Role:", userRole);

            if (userRole === "관리자") {
                window.location.href = "/admin.html";
            } else if (userRole === "회원") {
                window.location.href = "/index.html";
            } else {
                alert("알 수 없는 역할입니다. 역할: " + userRole);
            }
        })
        .catch(error => {
            console.error("Error:", error);
            alert("로그인에 실패했습니다.");
        });
});
