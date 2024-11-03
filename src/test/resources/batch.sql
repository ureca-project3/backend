-- 멤버
INSERT INTO member (member_id, member_name, email, phone, password, created_at, modified_at, provider, provider_id)
VALUES (1, '최지훈', 'c62@gmail.com', '010-3225-8151', 'dxxj80zA', '2024-01-16 00:00:00', '2024-02-18 00:00:00', 'email',
        'kakao'),
       (2, '박지아', 'pb56@naver.com', '010-7060-8178', 'OI4micPY', '2024-03-17 00:00:00', '2024-04-21 00:00:00', 'email',
        'kakao'),
       (3, '이민준', 'le10@naver.com', '010-2863-4384', 'xgxAJOdy', '2024-03-12 00:00:00', '2024-04-13 00:00:00', 'email',
        'kakao'),
       (4, '하승희', 'hh21@naver.com', '010-1274-5750', 'SAMx6VPP', '2024-08-22 00:00:00', '2024-09-30 00:00:00', 'email',
        'kakao'),
       (5, '강민수', 'km94@naver.com', '010-3873-5924', '6RPHYey8', '2024-03-19 00:00:00', '2024-04-21 00:00:00', 'email',
        'kakao');

-- 자녀
INSERT INTO child (child_id, member_id, child_name, gender, birthdate, profile_image, age)
VALUES (1, 1, '김민준', '남자', '2015-05-10', 'profile1.png', 9),
       (2, 1, '김서연', '여자', '2016-11-11', 'profile2.png', 8),
       (3, 2, '이도윤', '남자', '2017-01-15', 'profile3.png', 7),
       (4, 2, '이하은', '여자', '2014-03-21', 'profile4.png', 10),
       (5, 3, '박지호', '남자', '2013-08-02', 'profile5.png', 11),
       (6, 3, '박수아', '여자', '2015-07-30', 'profile6.png', 9),
       (7, 4, '최우진', '남자', '2016-06-22', 'profile7.png', 8),
       (8, 5, '정서진', '여자', '2014-09-19', 'profile8.png', 10);

-- 책
INSERT INTO book (title, author, publisher, published_at, rec_age, summary, bookcover_image, genre_code, created_at, modified_at)
VALUES
    ('욕심꾸러기 삼각형', '마릴린 번스', '보물창고', '2022-10-25', '8-9', '주변 사물을 활용해 다양한 도형을 쉽게 알려 주는 스토리텔링 수학 그림책이다. 알록달록한 색감의 도형 캐릭터들이 우리 주변의 사물들과 어울려 와글와글 떠들며 신나게 노는 모습이 흥미진진하게 펼쳐진다.', 'https://image.aladin.co.kr/product/30138/4/cover500/8961708848_1.jpg', '수학', '2024-01-20 12:00:00', '2024-01-20 12:00:00'),
    ('머나먼 길', '정희린', '곰세마리', '2024-10-01', '7', '이른 새벽, 다섯 명의 아이들이 길을 나선다. 자신의 몸보다 더 큰 물동이를 머리 위에 얹은 채 아이들은 끝없이 이어지는 길을 걷는다. 아직 어둠이 걷히지 않은 새벽에서 시작해 해가 빨갛게 익기까지, 아이들은 저마다의 소망을 이야기한다.', 'https://image.aladin.co.kr/product/34766/9/coversum/k862933029_2.jpg', '모험탐험', '2024-01-21 10:30:00', '2024-01-21 10:30:00'),
    ('천둥 꼬마 선녀 번개 꼬마 선녀', '한강', '문학동네', '2007-02-28', '4-7', '장마철을 앞두고 비구름을 짜느라 여념이 없는 하늘나라 선녀들 가운데, 심심하고 지루해 못 견디는 꼬마 선녀 둘이 있다. 가만히 앉아 끝도 없이 비구름을 만드는 것도 힘들고, 날개옷의 긴 치마는 발목에 자꾸 감기는 데다 말아 올린 머리가 무거워 목도 가누기 힘들다. 도저히 못 참겠다 툴툴대던 두 꼬마 선녀는 치렁치렁한 날개옷을 던지고 알몸으로 구름 위를 달려 세상 구경에 나선다.', 'https://image.aladin.co.kr/product/88/95/coversum/8954602797_2.jpg', '동화', '2024-01-22 09:00:00', '2024-01-22 09:00:00'),
    ('완벽한 바나비 가족의 탄생', '테리 펜 외 2명', '북금곰', '2024-11-03', '4-7', '완벽한 반려동물 가게에서 판매하는 반려동물 중 하나인 ‘완벽한 바나비’가 겪은 도전과 모험 이야기다.', 'https://image.aladin.co.kr/product/34777/90/coversum/k292933123_2.jpg', '반려동물', '2024-01-23 14:20:00', '2024-01-23 14:20:00'),
    ('럭키 덕희', '매튜 브로드허스트', '페이퍼독', '2024-10-15', '4-7', '여기, 놀라울 정도로 되는 일이 하나도 없고, 작은 운조차 따르지 않는 주인공 오리 ‘덕희’가 있다. 친구들마저도 놀리며 ‘럭키 덕희’라고 별명을 붙여 주었다. 이럴 수가! 하얗고 고운 털들이 모두 빠져버렸다. 아, 우리의 덕희는 정말 어떡하면 좋을까? 좌충우돌 되는 일은 없고, 점점 더 안 좋은 일만 생기는 덕희. 힘든 하루 속 덕희에게도 작은 행운이 찾아오긴 할까?', 'https://image.aladin.co.kr/product/34850/55/coversum/k562933746_2.jpg', '동물', '2024-01-24 16:45:00', '2024-01-24 16:45:00');

-- 테스트
INSERT INTO test (test_description, test_name, created_at, modified_at) VALUES ('성향 진단 결과로 맞춤 도서를 추천 받을 수 있어요', '자녀성향진단', '2024-10-19','2024-10-19');

-- 성향
INSERT INTO trait (test_id, trait_description, trait_name, max_score, min_score) VALUES (1, '외향형(E) 외부 세계에 주의를 집중해 사교적이고 활동적이며, 외부 자극을 통해 배움을 추구합니다. 내향형(I)은 내면에 집중해 충분히 생각한 후 행동하는 경향이 있습니다.', '에너지방향', 100, 0);
INSERT INTO trait (test_id, trait_description, trait_name, max_score, min_score) VALUES (1, '감각형(S) 현재의 감각적 경험에 집중하며, 주어진 것을 있는 그대로 받아들입니다. 반면, 직관형(N)은 정보를 연결해 새로운 의미를 부여하고 재해석하는 경향이 있습니다.', '인식기능', 100, 0);
INSERT INTO trait (test_id, trait_description, trait_name, max_score, min_score) VALUES (1, '사고형(T) 객관적인 사실을 중시하며, 분석적으로 판단하고 원칙과 규범을 따릅니다. 감정형(F)은 인간관계나 상황의 특성을 고려해 판단하고 결정을 내리는 경향이 있습니다.', '판단기능', 100, 0);
INSERT INTO trait (test_id, trait_description, trait_name, max_score, min_score) VALUES (1, '판단형(J) 체계적이고 조직적이며, 목표에 집중해 합리적으로 행동하는 것을 중요하게 여깁니다. 반면, 인식형(P)은 변화를 즐기고 상황에 따라 유연하게 대처하며, 계획이 있어도 유동적으로 행동하는 경향이 있습니다.', '생활양식', 100, 0);

-- MBTI
INSERT INTO mbti (test_id, mbti_name, mbti_phrase, mbti_description, mbti_image) VALUES (1, 'INTP', '논리술사','논리술사(INTP)는 자신의 독특한 관점과 활기 넘치는 지성에 자부심을 느끼며, 우주의 미스터리에 대해 깊이 생각하곤 합니다. 유명한 철학자와 과학자 중 논리술사 성격이 많은 것도 이 때문일 것입니다. 논리술사는 상당히 희귀한 성격이지만 뛰어난 창의성과 독창성으로 많은 사람 사이에서 존재감을 드러내곤 합니다. 이렇게 논리적이면서도 마술사와 같은 창의력을 발휘하는 성격이기에 ‘논리술사’라고 부르게 되었습니다.', 'INTP.png');
INSERT INTO mbti (test_id, mbti_name, mbti_phrase, mbti_description, mbti_image) VALUES (1, 'ENTJ', '통솔자','통솔자(ENTJ)는 타고난 리더라고 할 수 있습니다. 이들은 카리스마와 자신감을 지니고 있으며 자신의 권한을 이용해 사람들이 공통된 목표를 위해 함께 노력하도록 이끕니다. 또한 이들은 냉철한 이성을 지닌 것으로 유명하며 자신이 원하는 것을 성취하기 위해 열정과 결단력과 날카로운 지적 능력을 활용합니다. 이들은 전체 인구의 3%에 불과하지만, 다른 많은 성격을 압도하는 존재감을 뽐내며 다양한 비즈니스와 단체를 이끄는 역할을 할 때가 많습니다.', 'ENTJ.png');
INSERT INTO mbti (test_id, mbti_name, mbti_phrase, mbti_description, mbti_image) VALUES (1, 'ENTP', '변론가','변론가(ENTP)는 두뇌 회전이 빠르고 대담한 성격으로 현재 상황에 이의를 제기하는 데 거리낌이 없습니다. 변론가는 어떤 의견이나 사람에 반대하는 일을 두려워하지 않으며, 논란이 될 만한 주제에 대해 격렬하게 논쟁하는 일을 즐깁니다.
그렇다고 변론가가 반론을 제기하는 데만 관심이 있거나 악의를 지닌 것은 아닙니다. 사실 변론가는 지식이 풍부하고 호기심이 넘치며 활기찬 유머 감각으로 다른 사람을 즐겁게 할 수 있는 성격입니다. 다만 대부분의 성격과 달리 논쟁에서 즐거움을 찾는 성향이 있을 뿐입니다.', 'ENTP.png');
INSERT INTO mbti (test_id, mbti_name, mbti_phrase, mbti_description, mbti_image) VALUES (1, 'INFJ', '옹호자','옹호자(INFJ)는 매우 희귀한 성격임에도 불구하고 세상에 큰 영향력을 발휘하곤 합니다. 이들은 이상주의적이고 원칙주의적인 성격으로, 삶에 순응하는 대신 삶에 맞서 변화를 만들어 내고자 합니다. 이들에게 성공이란 돈이나 지위가 아니라 자아를 실현하고 다른 사람을 도우며 세상에서 선을 실천하는 일입니다.
원대한 목표와 야망을 품고 있다고 해서 옹호자가 몽상가와 같은 성격이라는 뜻은 아닙니다. 이들은 원칙과 완벽함을 중시하며 자신이 옳다고 믿는 일을 끝내기 전에는 만족하지 않습니다. 또한 매우 양심적인 성격으로 자신의 확실한 가치관에 따라 인생을 살아가며, 다른 사람이나 사회의 가치를 따르는 대신 자신의 지혜와 직관을 통해 정말로 중요한 가치를 찾기 위해 노력합니다.', 'INFJ.png');
INSERT INTO mbti (test_id, mbti_name, mbti_phrase, mbti_description, mbti_image) VALUES (1, 'INFP', '중재자','중재자(INFP)는 언뜻 보기에 조용하고 자신을 내세우지 않는 것처럼 보이지만, 사실은 에너지와 열정이 넘치는 마음을 지닌 성격입니다. 이들은 창의적이고 상상력이 뛰어나며 몽상을 즐기는 성격으로 머릿속에서 수많은 이야기를 만들어 내곤 합니다. 또한 음악과 예술과 자연에 대한 감수성이 뛰어나며 다른 사람의 감정을 빠르게 알아차립니다.
중재자는 이상주의적이고 공감 능력이 높으며 깊고 의미 있는 관계를 원하고 다른 사람을 도와야 한다는 사명감을 느끼곤 합니다. 그러나 전체 인구에서 큰 비중을 차지하지 않는 성격이기 때문에 외로움을 느끼거나 존재감을 드러내지 못하는 때가 있으며, 자신의 독특한 강점을 인정하지 않는 세상에서 방황하는 느낌을 받을 수도 있습니다.','INFP.png');
INSERT INTO mbti (test_id, mbti_name, mbti_phrase, mbti_description, mbti_image) VALUES (1, 'ENFJ', '선도자','선도자(ENFJ)는 삶에서 위대한 사명을 위해 힘써야 한다는 느낌을 받곤 합니다. 사려 깊고 이상주의적 성향을 지닌 선도자는 다른 사람과 주변 세상에 긍정적인 영향력을 발휘하기 위해 최선을 다하며, 어려운 상황에서도 올바른 일을 할 기회를 마다하지 않습니다.
선도자는 타고난 지도자라고 할 수 있으며 많은 선도자가 정치인, 코치, 교사로 활동하고 있습니다. 이들의 열정과 카리스마는 직업뿐만 아니라 인간관계 등 삶의 다양한 측면에서 다른 사람에게 영향을 주곤 합니다. 또한 이들은 친구와 사랑하는 사람이 발전할 수 있도록 돕는 일에서 즐거움과 깊은 만족감을 느낍니다.','ENFJ.png');
INSERT INTO mbti (test_id, mbti_name, mbti_phrase, mbti_description, mbti_image) VALUES (1, 'ENFP', '활동가','활동가(ENFP)는 진정으로 자유로운 영혼이라고 할 수 있으며 외향적이고 솔직하며 개방적인 성격입니다. 이들은 활기차고 낙관적인 태도로 삶을 대하며 다른 사람들 사이에서 돋보이곤 합니다. 그러나 신나는 인생을 보내는 것처럼 보인다고 해서 즐거움만을 좇는 성격은 아니며, 다른 사람과 감정적으로 깊고 의미 있는 관계를 맺는 일을 추구합니다.','ENFP.png');
INSERT INTO mbti (test_id, mbti_name, mbti_phrase, mbti_description, mbti_image) VALUES (1, 'INTJ', '전략가','최고가 되는 것은 외로운 일입니다. 매우 희귀한 성격이면서도 뛰어난 능력을 지닌 전략가(INTJ)는 이러한 말의 의미를 잘 알고 있습니다. 전략가는 이성적이면서도 두뇌 회전이 빠른 성격으로, 자신의 뛰어난 사고 능력을 자랑스러워하며 거짓말과 위선을 꿰뚫어 보는 능력이 있습니다. 하지만 이로 인해 끊임없이 생각하고 주변의 모든 것을 분석하려는 자신의 성향을 이해할 수 있는 사람을 찾는 데 어려움을 겪기도 합니다.','INTJ.png');
INSERT INTO mbti (test_id, mbti_name, mbti_phrase, mbti_description, mbti_image) VALUES (1, 'ESFP', '연예인', '연예인(ESFP)은 즉흥적으로 노래하고 춤을 추는 일을 즐기는 성격입니다. 이들은 지금 이 순간을 즐기며 남들도 자신과 같은 즐거움을 느낄 수 있기를 바랍니다. 또한 남을 응원하는 데 기꺼이 시간과 에너지를 투자하며, 매우 매력적인 방식으로 다른 사람의 기운을 북돋곤 합니다.', 'ESFP.png');
INSERT INTO mbti (test_id, mbti_name, mbti_phrase, mbti_description, mbti_image) VALUES (1, 'ESTP', '사업가', '사업가(ESTP)는 항상 주변 사람에게 영향력을 행사하곤 합니다. 파티에서 가는 곳마다 사람들에게 둘러싸여 있는 사람을 발견한다면 바로 사업가일 것입니다. 이들은 직설적인 유머 감각을 지니고 있으며 수많은 사람의 관심을 받는 일을 즐깁니다. 한 마디로 사회자가 무대로 올라올 사람을 찾을 때 직접 무대로 올라가는 성격이라고 할 수 있습니다.
사업가는 이론적이고 추상적인 개념과 지루한 토론에는 크게 관심이 없습니다. 지능이 높고 활기찬 대화를 유지할 수 있는 성격이기는 하지만, 현실적인 주제에 대해 이야기하고 직접 행동하기를 원하는 성격이기 때문입니다. 이들은 계획을 심사숙고하기보다는 먼저 행동하고 시행착오를 겪으며 실수를 바로잡는 방식을 선호합니다.', 'ESFP.png');
INSERT INTO mbti (test_id, mbti_name, mbti_phrase, mbti_description, mbti_image) VALUES (1, 'ISFP', '모험가', '모험가(ISFP)는 진정한 의미의 예술가라고 할 수 있습니다. 하지만 모험가라고 반드시 예술 업계에만 종사하는 것은 아닙니다. 이들에게는 삶 자체가 자신을 표현하기 위한 캔버스이기 때문입니다. 이들은 입는 옷부터 여가 시간을 보내는 방식까지 다양한 측면에서 자신의 독특한 개성을 생생히 드러냅니다.
모든 모험가는 각자 독특한 성격을 지니고 있습니다. 이들은 호기심이 많고 새로운 것을 추구하는 성격으로 다양한 분야에 관심과 열정을 보일 때가 많습니다. 모험가는 탐험가적 성향과 일상 생활에서 즐거움을 찾을 수 있는 능력을 지니고 있는 매우 흥미로운 성격이라고 할 수 있습니다. 그러나 보통 자신의 놀라운 개성에 자만하는 대신 자신이 하고 싶은 일을 할 뿐이라고 겸손하게 생각하곤 합니다.
', 'ISFP.png');
INSERT INTO mbti (test_id, mbti_name, mbti_phrase, mbti_description, mbti_image) VALUES (1, 'ISTP','장인', '장인(ISTP)은 이성과 호기심을 통해 세상을 바라보며 눈과 손으로 직접 탐구하는 일을 즐깁니다. 이들은 타고난 손기술을 지니고 있으며, 다양한 프로젝트에서 유용하고 재미있는 물건을 만들어 내고 주변 환경에서 배울 점을 찾습니다. 장인은 보통 기술자나 엔지니어로 일하는 경우가 많으며 물건을 직접 분해하고 조립해 개선하는 일을 즐깁니다.
장인은 물건을 제작하고 문제를 해결하고 시행착오를 거치고 직접 경험함으로써 아이디어에 대해 탐구합니다. 또한 다른 사람이 자신의 프로젝트에 관심을 보이는 것에 즐거워하며 개방적인 태도를 보입니다. 다만 다른 사람이 자신의 원칙이나 자유를 침해하는 일은 원치 않으며, 상대방도 자신에게 개방적인 태도를 보일 것을 기대합니다.
장인은 남을 돕고 경험을 공유하는 일을 좋아합니다. 이렇게 매력적인 장인이 전체 인구의 약 5%밖에 되지 않는 점은 아쉽다고 할 수 있습니다. 특히 여성 장인은 더욱 희귀하며 전통적인 성 역할을 기대하는 사회에서는 말괄량이 취급을 받는 어려움을 겪기도 합니다.', 'ISTP.png');
INSERT INTO mbti (test_id, mbti_name, mbti_phrase, mbti_description, mbti_image) VALUES (1, 'ESFJ', '집정관', '집정관(ESFJ)에게 인생이란 다른 사람과 함께할 때 가장 달콤한 것입니다. 이들은 많은 공동체의 기반이 되며 친구와 연인과 이웃을 열린 마음으로 대합니다.
집정관이 모든 사람을 좋아하거나 무한한 관용을 지닌 것은 아닙니다. 하지만 이들은 친절하고 예의 바른 태도가 도움이 된다고 믿고 있으며 주변 사람들에게 강한 책임감을 느끼곤 합니다. 또한 관대하고 신뢰할 수 있는 성격으로 작은 일이든 큰 일이든 가족과 공동체를 하나로 모으기 위해 스스로 책임을 짊어질 때가 많습니다.', 'ESFJ.png');
INSERT INTO mbti (test_id, mbti_name, mbti_phrase, mbti_description, mbti_image) VALUES (1, 'ESTJ','경영자', '경영자(ESTJ)는 전통과 질서를 중시하는 성격으로, 자신이 생각하는 옳고 그름과 사회적 기준에 따라 가족과 공동체가 화합할 수 있도록 노력합니다. 이들은 정직과 헌신과 존엄성을 중시하며, 어려운 길을 기꺼이 앞장서고 다른 사람들에게 명확한 조언과 지도를 제공합니다. 이들은 사람들이 화합하도록 하는 일에서 자부심을 느끼며, 모든 사람이 지역 축제를 즐길 수 있도록 노력하거나 가족과 공동체의 전통적인 가치관을 지키는 역할 등을 맡곤 합니다.', 'ESTJ.png');
INSERT INTO mbti (test_id, mbti_name, mbti_phrase, mbti_description, mbti_image) VALUES (1, 'ISFJ', '수호자', '수호자(ISFJ)는 겸손한 자세로 세상을 지탱하는 역할을 합니다. 이들은 근면하고 헌신적인 성격으로 주변 사람들에 대한 깊은 책임감을 느낍니다. 이들은 마감 기한을 철저히 지키고 동료와 친구의 생일과 기념일을 챙기며, 기존 질서를 유지하고 주변 사람을 배려하는 동시에 기꺼이 도움의 손길을 건넵니다. 또한 감사를 요구하기보다는 뒤에서 묵묵히 헌신하는 성격이라고 할 수 있습니다.
수호자는 유능하고 긍정적인 성격으로 다양한 방면에서 역량을 발휘합니다. 또한 세심하고 배려심이 넘치며 분석 능력과 세부 사항 파악 능력도 뛰어납니다. 그리고 차분한 성격인 동시에 대인 관계 능력도 뛰어나며, 이 덕분에 여러 사람과 깊은 관계를 맺을 가능성도 큽니다. 이들의 다양한 장점은 시너지 효과를 내며 일상생활에서도 빛을 발하곤 합니다.', 'ISFJ.png');
INSERT INTO mbti (test_id, mbti_name, mbti_phrase, mbti_description, mbti_image) VALUES (1, 'ISTJ','현실주의자', '현실주의자(ISTJ)는 진솔하게 행동하는 자신의 모습에서 자부심을 느끼며, 자기 생각을 솔직하게 이야기하고 어떤 것에 헌신하기로 한 경우 최선을 다합니다.
현실주의자는 인구의 상당 부분을 차지합니다. 화려한 삶이나 다른 사람의 주의를 끄는 일에는 관심이 없으며 안정된 사회를 위해 자신의 몫보다 많은 기여를 하곤 합니다. 이들은 가족이나 주변 사람들로부터 믿음직한 사람이라는 평판을 받을 때가 많으며, 현실 감각이 뛰어나 스트레스가 극심한 상황에서도 현실적이고 논리적인 태도를 유지하는 사람으로 인정받곤 합니다.', 'ISTJ.png');

-- 테스트 질문
INSERT INTO test_question (test_id, trait_id, question_text) VALUES (1, 1, '좋아하는 친구에게 먼저 가서 이야기하는 게 어려워요?');
INSERT INTO test_question (test_id, trait_id, question_text) VALUES (1, 2, '마지막에 어떻게 될지 궁금한 책이나 영화를 좋아해요?');
INSERT INTO test_question (test_id, trait_id, question_text) VALUES (1, 3, '쉽게 울거나 쉽게 기뻐해요?');
INSERT INTO test_question (test_id, trait_id, question_text) VALUES (1, 3, '감정을 조절하기보다 감정에 따라 행동해요?');
INSERT INTO test_question (test_id, trait_id, question_text) VALUES (1, 4, '숙제 마감일을 지키기 어려워요?');
INSERT INTO test_question (test_id, trait_id, question_text) VALUES (1, 1, '새로운 친구와 놀기보다 예전 친구와 노는 게 더 좋아요?');
INSERT INTO test_question (test_id, trait_id, question_text) VALUES (1, 2, '모르는 것에 대해 질문하는 게 재밌어요?');
INSERT INTO test_question (test_id, trait_id, question_text) VALUES (1, 2, '그림이나 음악 같은 예술을 좋아해요?');
INSERT INTO test_question (test_id, trait_id, question_text) VALUES (1, 1, '혼자 하는 활동이 단체 활동보다 더 좋아요?');
INSERT INTO test_question (test_id, trait_id, question_text) VALUES (1, 4, '단계별로 해야 할 일을 순서대로 하는 편이에요?');
INSERT INTO test_question (test_id, trait_id, question_text) VALUES (1, 1, '단체 활동에서 팀원이 되는 게 리더가 되는 것보다 더 좋아요?');
INSERT INTO test_question (test_id, trait_id, question_text) VALUES (1, 4, '매일 할 일을 계획하는 게 귀찮아요?');
INSERT INTO test_question (test_id, trait_id, question_text) VALUES (1, 3, '친구가 슬픈 걸 빨리 알아챌 수 있어요?');
INSERT INTO test_question (test_id, trait_id, question_text) VALUES (1, 2, '자주 불안해요?');
INSERT INTO test_question (test_id, trait_id, question_text) VALUES (1, 1, '혼자 있는 시간을 더 원해요?');
INSERT INTO test_question (test_id, trait_id, question_text) VALUES (1, 3, '자신과 다른 사람의 기분을 쉽게 이해해요?');
INSERT INTO test_question (test_id, trait_id, question_text) VALUES (1, 3, '누군가 울고 있으면 나도 같이 울 때가 있어요?');
INSERT INTO test_question (test_id, trait_id, question_text) VALUES (1, 4, '해야 할 일을 자주 미루는 편이에요?');
INSERT INTO test_question (test_id, trait_id, question_text) VALUES (1, 4, '계획하기보다 즉흥적으로 하고 싶은 걸 하는 게 좋아요?');
INSERT INTO test_question (test_id, trait_id, question_text) VALUES (1, 2, '관심 있는 게 여러 가지예요?');

-- MbtiHistory
INSERT INTO mbti_history (is_deleted, child_id, created_at, modified_at, reason_id, current_mbti, reason)
VALUES
    (0, 1, NOW(), NOW(), 1, 'INTJ', '기본'),
    (0, 2, NOW(), NOW(), 1, 'ENTP', '기본'),
    (0, 3, NOW(), NOW(), 1, 'ISFJ', '기본'),
    (0, 4, NOW(), NOW(), 1, 'ESFP', '기본'),
    (0, 5, NOW(), NOW(), 1, 'ISTJ', '기본');


-- 누적 변화량
INSERT INTO traits_change (change_amount, child_id, created_at, modified_at, trait_id)
VALUES
    (0, 1, NOW(), NOW(), 1),
    (0, 1, NOW(), NOW(), 2),
    (0, 1, NOW(), NOW(), 3),
    (0, 1, NOW(), NOW(), 4),
    (0, 2, NOW(), NOW(), 1),
    (0, 2, NOW(), NOW(), 2),
    (0, 2, NOW(), NOW(), 3),
    (0, 2, NOW(), NOW(), 4),
    (0, 3, NOW(), NOW(), 1),
    (0, 3, NOW(), NOW(), 2),
    (0, 3, NOW(), NOW(), 3),
    (0, 3, NOW(), NOW(), 4),
    (0, 4, NOW(), NOW(), 1),
    (0, 4, NOW(), NOW(), 2),
    (0, 4, NOW(), NOW(), 3),
    (0, 4, NOW(), NOW(), 4),
    (0, 5, NOW(), NOW(), 1),
    (0, 5, NOW(), NOW(), 2),
    (0, 5, NOW(), NOW(), 3),
    (0, 5, NOW(), NOW(), 4);
