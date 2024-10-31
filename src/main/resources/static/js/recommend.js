function getChildIdFromSession() {
    return sessionStorage.getItem("Child-Id");
}

function getAccessTokenFromSession() {
    return sessionStorage.getItem("accessToken");
}

// 페이지 로드 시 좋아요 상태를 확인하여 버튼 스타일 설정
async function fetchRecommendedBooks() {
    const childId = getChildIdFromSession();
    const accessToken = getAccessTokenFromSession();
    console.log("childId : " + childId);
    if (!childId) {
        console.error("Child-Id를 찾을 수 없습니다.");
        return;
    }

    try {
        const response = await fetch("/recommends", {
            method: 'GET',
            headers: {
                'Child-Id': childId,
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${accessToken}`
            }
        });
        const data = await response.json();

        if (data.message === "Get Recommended Books Success") {
            displayRecommendedBooks(data.data, childId);
        } else {
            console.error("Failed to fetch recommended books:", data.message);
        }
    } catch (error) {
        console.error("Error fetching recommended books:", error);
    }
}

function displayRecommendedBooks(books, childId) {
    const container = document.getElementById('recommend-container');
    container.innerHTML = '';

    books.forEach(book => {
        const card = document.createElement('div');
        card.className = 'book-card';

        // 카드 클릭 시 책 상세 페이지로 이동
        card.addEventListener('click', () => {
            window.location.href = `/bookDetail.html?bookId=${book.bookId}`;
        });

        card.innerHTML = `
            <img src="${book.imageUrl}" alt="${book.title}" class="book-image" />
            <h2 class="book-title">${book.title}</h2>
            <p class="book-age">추천 연령: ${book.recAge}세</p>
            <button class="like-btn" id="like-btn-${book.bookId}" onclick="toggleRecBookLike(${book.bookId}, ${childId}); event.stopPropagation();">
                ❤️ 마음에 들어요
            </button>
        `;

        container.appendChild(card);
        checkRecBookLikeStatus(book.bookId, childId); // 추천책 좋아요 상태 확인
    });
}

// 좋아요 상태 확인
async function checkRecBookLikeStatus(bookId, childId) {
    try {
        const response = await fetch(`/recommends/likes/${bookId}`, {
            method: 'GET',
            headers: {
                'Child-Id': childId,
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${sessionStorage.getItem('accessToken')}`
            }
        });

        if (response.ok) {
            const result = await response.json();
            const likeBtn = document.getElementById(`like-btn-${bookId}`);

            if (result.message === "좋아요한 추천책입니다") {
                likeBtn.classList.add("liked");
            } else {
                likeBtn.classList.remove("liked");
            }
        }
    } catch (error) {
        console.error("Error checking like status:", error);
    }
}

// 좋아요 토글 기능
async function toggleRecBookLike(bookId, childId) {
    if (!childId) {
        console.error("Child-Id를 찾을 수 없습니다.");
        return;
    }

    const likeBtn = document.getElementById(`like-btn-${bookId}`);
    const isLiked = likeBtn.classList.contains("liked");

    try {
        if (isLiked) {
            await fetch(`/recommends/likes/${bookId}`, {
                method: 'DELETE',
                headers: {
                    'Child-Id': childId,
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${sessionStorage.getItem('accessToken')}`
                }
            });
            likeBtn.classList.remove("liked");
        } else {
            await fetch(`/recommends/likes/${bookId}`, {
                method: 'POST',
                headers: {
                    'Child-Id': childId,
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${sessionStorage.getItem('accessToken')}`
                }
            });
            likeBtn.classList.add("liked");
        }
    } catch (error) {
        console.error("Error toggling like:", error);
    }
}

// 페이지 로드 시 추천 책 목록 가져오기
window.onload = fetchRecommendedBooks;
