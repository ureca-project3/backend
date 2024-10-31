async function fetchSearchResults() {
    // URL에서 query 파라미터 추출
    const urlParams = new URLSearchParams(window.location.search);
    const keyword = urlParams.get('query');

    // 검색어를 제목에 추가
    const searchKeywordElement = document.getElementById('search-keyword');
    searchKeywordElement.textContent = `'${keyword}'`;
    searchKeywordElement.classList.add('highlight'); // 검색어 강조

    try {
        // API 호출 (예: /books/search?keyword=keyword)
        const response = await fetch(`/books/search?keyword=${keyword}`);
        const data = await response.json();

        if (data.message === "Get BookSearch Success") {
            displayResults(data.data.content, keyword);
        } else {
            console.error("Failed to fetch search results:", data.message);
        }
    } catch (error) {
        console.error("Error fetching search results:", error);
    }
}

function displayResults(books, keyword) {
    const container = document.getElementById('results-container');
    const noResultsMessage = document.getElementById('no-results-message');

    container.innerHTML = ''; // 기존 내용 지우기

    if (books.length === 0) {
        // 검색 결과가 없을 때 메시지 표시
        noResultsMessage.textContent = `'${keyword}' 관련 게시글이 없습니다.`;
        noResultsMessage.style.display = 'block'; // 메시지 보이기
    } else {
        noResultsMessage.style.display = 'none'; // 메시지 숨기기

        books.forEach(book => {
            const card = document.createElement('div');
            card.className = 'book-card';

            // 카드 클릭 시 상세 페이지로 이동하도록 이벤트 추가
            card.addEventListener('click', () => {
                window.location.href = `/bookDetail.html?bookId=${book.bookId}`;
            });

            card.innerHTML = `
                <img src="${book.imageUrl}" alt="${book.title}" class="book-image" />
                <h2 class="book-title">${book.title}</h2>
                <p class="book-age">추천 연령: ${book.recAge}세</p>
            `;

            container.appendChild(card);
        });
    }
}

// 페이지 로드 시 검색 결과 가져오기
window.onload = fetchSearchResults;
