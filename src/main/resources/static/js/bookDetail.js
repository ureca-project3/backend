window.onload = function () {
    checkLikeHateState();
};

// 도서 상세 정보 로드
document.addEventListener("DOMContentLoaded", async () => {
    const urlParams = new URLSearchParams(window.location.search);
    const bookId = urlParams.get('bookId');

    if (!bookId) {
        console.error("bookId가 없습니다");
        return;
    }

    try {
        const response = await fetch(`/books/${bookId}`);
        const data = await response.json();

        if (data.message === "Get BookDetail Success") {
            const book = data.data;
            document.getElementById("book-title").textContent = book.title;
            document.getElementById("book-author").textContent = `저자: ${book.author}`;
            document.getElementById("book-publisher").textContent = `출판사: ${book.publisher}`;
            document.getElementById("book-age").textContent = `추천 연령: ${book.recAge}세`;
            document.getElementById("book-published-at").textContent = `출판일: ${book.publishedAt}`;
            document.getElementById("book-summary").textContent = `${book.summary}`;
            document.getElementById("book-image").src = book.imageUrl;
        } else {
            console.error("Failed to fetch book details:", data.message);
        }
    } catch (error) {
        console.error("Error fetching book details:", error);
    }
});

// 좋아요 및 싫어요 상태를 확인하는 함수
async function checkLikeHateState() {
    const urlParams = new URLSearchParams(window.location.search);
    const bookId = urlParams.get('bookId');

    try {
        // 좋아요 상태 확인
        const likeResponse = await fetch(`/likes/${bookId}`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${sessionStorage.getItem('accessToken')}`
            }
        });

        const likeBtn = document.getElementById("like-btn");
        if (likeResponse.ok) {
            const likeData = await likeResponse.json();
            if (likeData.message === "좋아요한 책입니다.") {
                likeBtn.classList.add("liked");
            } else {
                likeBtn.classList.remove("liked");
            }
        }

        // 싫어요 상태 확인
        const hateResponse = await fetch(`/hates/${bookId}`, {
            method: 'GET',
            headers: {
                'Child-Id': childId
            }
        });

        const dislikeBtn = document.getElementById("dislike-btn");
        if (hateResponse.ok) {
            const hateData = await hateResponse.json();
            if (hateData.message === "싫어요한 책입니다.") {
                dislikeBtn.classList.add("disliked");
            } else {
                dislikeBtn.classList.remove("disliked");
            }
        }
    } catch (error) {
        console.error("Error checking like/hate state:", error);
    }
}

// 좋아요 기능
async function toggleLike() {
    const likeBtn = document.getElementById("like-btn");
    const dislikeBtn = document.getElementById("dislike-btn");
    const urlParams = new URLSearchParams(window.location.search);
    const bookId = urlParams.get('bookId');

    try {
        if (likeBtn.classList.contains("liked")) {
            const response = await fetch(`/likes/${bookId}`, {
                method: 'DELETE',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${sessionStorage.getItem('accessToken')}`
                }
            });
            if (response.ok) {
                likeBtn.classList.remove("liked");
            }
        } else {
            const response = await fetch(`/likes/${bookId}`, {
                method: 'POST',
                headers: {
                    'Child-Id': childId
                }
            });
            if (response.ok) {
                likeBtn.classList.add("liked");
                dislikeBtn.classList.remove("disliked"); // 싫어요가 활성화된 경우 비활성화
            }
        }
    } catch (error) {
        console.error("Error in toggleLike:", error);
    }
}

// 싫어요 기능
async function toggleDislike() {
    const likeBtn = document.getElementById("like-btn");
    const dislikeBtn = document.getElementById("dislike-btn");
    const urlParams = new URLSearchParams(window.location.search);
    const bookId = urlParams.get('bookId');

    try {
        if (dislikeBtn.classList.contains("disliked")) {
            const response = await fetch(`/hates/${bookId}`, {
                method: 'DELETE',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${sessionStorage.getItem('accessToken')}`
                }
            });
            if (response.ok) {
                dislikeBtn.classList.remove("disliked");
            }
        } else {
            const response = await fetch(`/hates/${bookId}`, {
                method: 'POST',
                headers: {
                    'Child-Id': childId
                }
            });
            if (response.ok) {
                dislikeBtn.classList.add("disliked");
                likeBtn.classList.remove("liked"); // 좋아요가 활성화된 경우 비활성화
            }
        }
    } catch (error) {
        console.error("Error in toggleDislike:", error);
    }
}

// 좋아요 및 싫어요 버튼 클릭 이벤트
document.addEventListener("DOMContentLoaded", function() {
    const likeBtn = document.getElementById("like-btn");
    const dislikeBtn = document.getElementById("dislike-btn");

    likeBtn.addEventListener("click", toggleLike);
    dislikeBtn.addEventListener("click", toggleDislike);
});
