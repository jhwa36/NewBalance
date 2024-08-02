let pageNumber = 0;
$(document).ready(function () {

    let offset = 0;
    const limit = 10;

    // 메뉴 토글
    $(".adminMenu > a").click(function () {
        const submenu = $(this).next("ul");
        if (submenu.is(":visible")) {
            submenu.slideUp();
        } else {
            submenu.slideDown();
        }
    });

    // 모달 초기화
    $('#noticeModal').on('show.bs.modal', function () {
        $('#noticeModalCount').prop('disable', true);
        $('#noticeModalModifiedDate').prop('disable', true);
    });

    // 공지사항 목록 로드
    function noticeList(append = false) { // append가 true일 경우 기존 목록 유지하고 새로 가져온 목록내용추가 더보기 버튼 클릭시 true
        $.ajax({
            url: '/admin/notices',
            type: 'GET',
            data: {offset: offset, limit: limit},
            dataType: 'json',
            success: function (response) {
                const noticeTableBody = $('#noticesTable tbody');
                if (!append) noticeTableBody.empty(); // append가 false면 기존 목록을 지움
                $.each(response.notices, function (index, notice) {
                    const row = $('<tr>');
                    row.html(`
                            <td>${notice.id}</td>
                            <td><a href="#" class="notice-link" data-id="${notice.id}" data-noticetitle="${notice.noticeTitle}" data-noticecontent="${notice.noticeContent}" data-modifieddate="${notice.modifiedDate}" data-noticecount="${notice.noticeCount}">${notice.noticeTitle}</a></td>
                            <td>${formatDate(notice.modifiedDate)}</td>
                            <td>${notice.noticeCount}</td>
                        `);
                    noticeTableBody.append(row);
                });

                $('.notice-link').click(showNoticeModal);

                const remingCount = Math.max(0, response.totalNotices - (offset + limit));
                $('#loadNoticeMore').text('더보기(' + remingCount + ')');

                // 더보기 버튼 표시 여부 결정
                if (remingCount <= 0) {
                    $('#loadNoticeMore').hide();
                } else {
                    $('#loadNoticeMore').show();
                }
            },
            error: function (xhr, status, error) {
                console.error('Error fetching notices:', error);
            }
        });
    }// 공지사항 목록 로드 끝

    // 회원목록 로드
    function memberList(append = false) {
        $.ajax({
            url: "/admin/membersList",
            type: 'GET',
            data: {offset: offset, limit: limit},
            dataType: 'json',
            success: function (response) {
                const membersTableBody = $('#membersTable tbody');
                if (!append) membersTableBody.empty();
                $.each(response.members, function (index, member) {
                    const row = $('<tr>');
                    row.html(`
                            <td>${member.userId}</td>
                            <td>${member.email}</td>
                            <td>${member.name}</td>
                            <td>${member.sex}</td>
                            <td>${member.role}</td>
                        `);
                    membersTableBody.append(row);
                });
                const remingCount = Math.max(0, response.totalMembers - (offset + limit));
                $('#loadMemberMore').text('더보기(' + remingCount + ')');
                if (remingCount <= 0) {
                    $('#loadMemberMore').hide();
                } else {
                    $('#loadMemberMore').show();
                }
            }
        });
    }// 회원목록 로드 끝

    // 쿠폰목록 로드 시작
    function couponList(append = false) {
        $.ajax({
            url: '/admin/coupons',
            type: 'GET',
            dataType: 'json',
            success: function (response) {
                const couponTable = $('#couponTable tbody');
                if (!append) couponTable.empty();
                $.each(response.coupons, function (index, coupon) {
                    const row = $('<tr>');
                    row.html(`
                        <td>${coupon.benefit}</td>
                        <td><a href="#" class="updateCoupon-link" data-id="${coupon.id}" data-benefit="${coupon.benefit}"
                              data-period="${coupon.period}" data-quantity="${coupon.quantity}" data-code="${coupon.code}"
                               data-title="${coupon.title}" data-status="${coupon.status}">${coupon.title}</a></td>
                        <td>${coupon.period}</td>
                        <td>${coupon.quantity}</td>
                        <td>${coupon.status}</td>
                   `);
                    couponTable.append(row);
                });
                $('.updateCoupon-link').click(updateCoupon);

                const remingCount = Math.max(0, response.totalCoupon - (offset + limit));
                $('#loadCouponMore').text('더보기(' + remingCount + ')');
                if (remingCount <= 0) {
                    $('#loadCouponMore').hide();
                } else {
                    $('#loadCouponMore').show();
                }
            }
        });
    }

    function updateCoupon(event){
        event.preventDefault();
        const coupon = $(this).data();
        $('#couponUpdateId').val(coupon.id);
        $('#couponUpdateBenefit').val(coupon.benefit);
        $('.couponEditCode').val(coupon.code);
        $('#couponUpdatePeriod').val(coupon.period);
        $('#couponUpdateQuantity').val(coupon.quantity);
        $('#couponUpdateTitle').val(coupon.title);

        $('#couponUpdateModal').show();
    }

    $('#updateCouponBtn').click(function(event){
        const couponId = $('#couponUpdateId').val();
        const dataForm = {
            id: couponId,
            benefit: $('#couponUpdateBenefit').val(),
            code: $('.couponEditCode').val(),
            period: $('#couponUpdatePeriod').val(),
            quantity: $('#couponUpdateQuantity').val(),
            title: $('#couponUpdateTitle').val()
        }

        $.ajax({
            url: '/admin/updateCoupon/' + couponId,
            type: 'PUT',
            contentType: 'application/json',
            data: JSON.stringify(dataForm),
            success: function(response) {
                alert('저장완료');
                $('#couponUpdateModal').hide();
                couponList();
            },
            error:function(xhr, status, error) {
                console.error('저장실패', error);
            }
        });
    });

    $('#deleteCouponBtn').click(function(event){
        event.preventDefault();
        $.ajax({
            url: '/admin/deleteCoupon/' + $('#couponUpdateId').val(),
            type: 'DELETE',
            success: function(response) {
                alert('삭제완료');
                $('#couponUpdateModal').hide();
                couponList();
            },
            error: function(xhr, status, error) {
                console.error('삭제실패', error);
            }
        });
    });


    // 공지사항 링크 클릭 모달 핸들러
    function showNoticeModal(event) {
        event.preventDefault();
        const notice = $(this).data();

        $('#noticeModalId').val(notice.id);
        $('#noticeModalTitle').val(notice.noticetitle);
        $('#noticeModalContent').val(notice.noticecontent);
        $('#noticeModalCount').text(notice.noticecount);
        $('#noticeModalModifiedDate').text(formatDate(notice.modifieddate));
        $('#noticeModal').show();
    }

    // 초기 공지사항 목록 로드
    $('#noticeList').click(function (event) {
        event.preventDefault();
        $('#noticesTable, #loadNoticeMore, #noticeEdit').show();
        $('#membersTable, #loadMemberMore').hide();
        $('#faqsDivList').hide();
        $('#categoryDivList').hide();
        $('#couponDivList, #loadCouponMore').hide();
        $('#productDivList').hide();
        noticeList(offset, limit);
    });

    // 초기 상품정보 목록 로드
    $('#productList').click(function (event) {
        event.preventDefault();
        $('#noticesTable, #loadNoticeMore, #noticeEdit').hide();
        $('#membersTable, #loadMemberMore').hide();
        $('#faqsDivList').hide();
        $('#categoryDivList').hide();
        $('#couponDivList, #loadCouponMore').hide();
        $('#productDivList').show();
        productList();
    });


    // 초기 회원정보 목록 로드
    $('#memberList').click(function (event) {
        event.preventDefault();
        $('#membersTable, #loadMemberMore').show();
        $('#faqsDivList').hide();
        $('#noticesTable, #loadNoticeMore, #noticeEdit').hide();
        $('#categoryDivList').hide();
        $('#couponDivList, #loadCouponMore').hide();
        $('#productDivList').hide();
        memberList(offset, limit);
    });

    // category 목록 로드
    $('#categoryList').click(function (event) {
        event.preventDefault();
        $('#noticesTable, #loadNoticeMore, #noticeEdit').hide();
        $('#membersTable, #loadMemberMore').hide();
        $('#faqsDivList').hide();
        $('#couponDivList, #loadCouponMore').hide();
        $('#categoryDivList').show();
        $('#productDivList').hide();
        $('.tab-item[data-title="MEN"]').addClass('active');
        categoryList('MEN');
    });

    // coupon 목록 로드
    $('#couponList').click(function (event) {
        event.preventDefault();
        $('#noticesTable, #loadNoticeMore, #noticeEdit').hide();
        $('#membersTable, #loadMemberMore').hide();
        $('#faqsDivList').hide();
        $('#categoryDivList').hide();
        $('#couponDivList').show();
        $('#productDivList').hide();
        couponList(offset, limit);
    });

    // product 목록 로드
    $('#prductList').click(function(event){
        $('#noticesTable, #loadNoticeMore, #noticeEdit').hide();
        $('#membersTable, #loadMemberMore').hide();
        $('#faqsDivList').hide();
        $('#categoryDivList').hide();
        $('#couponDivList').hide();
        $('#productDivList').show();

    });

    // 공지사항 더보기 버튼 클릭 이벤트
    $('#loadNoticeMore').click(function () {
        offset += limit;
        noticeList(offset, limit);
    });

    // 회원정보 더보기 버튼 클릭 이벤트
    $('#loadMemberMore').click(function () {
        offset += limit;
        memberList(offset, limit);
    });

    $('#loadCouponMore').click(function () {
        offset += limit;
        couponList(offset, limit);
    });

    // 날짜 포맷
    function formatDate(dateString) {
        const date = new Date(dateString);
        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const day = String(date.getDate()).padStart(2, '0');
        return `${year}-${month}-${day}`;
    }

    // 공지사항 모달페이지 수정
    $('#editNoticeBtn').click(function () {

        const noticeId = $('#noticeModalId').val();
        // 수정할 데이터 객체 생성
        const updateNoticeData = {
            noticeTitle: $('#noticeModalTitle').val(),
            noticeContent: $('#noticeModalContent').val(),
            noticeCount: $('#noticeModalCount').text()
        };

        $.ajax({
            url: "/admin/noticeEdit/" + noticeId,
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify(updateNoticeData),
            success: function (response) {
                alert('수정 완료');
                // window.location.reload();
                noticeList(0, offset + limit);
                $('#noticeModal').hide();
            },
            error: function (xhr, status, error) {
                console.error(error);
            }
        });
    });

    // 공지사항 삭제
    $('#deleteNoticeBtn').click(function () {

        if (confirm("삭제하시겠습니까?")) {
            const noticeId = $('#noticeModalId').val();

            $.ajax({
                url: '/admin/noticeDelete/' + noticeId,
                type: 'DELETE',
                success: function (response) {
                    alert('삭제 완료');
                    noticeList(0, offset + limit);
                    $('#noticeModal').hide();
                },
                error: function (xhr, status, error) {
                    console.error(error);
                }
            });
        } else {

        }
    });

    // 모달 닫기
    $('.close').click(function () {
        $('#noticeModal').hide();
        $('#faqEditModal').hide();
        $('#faqUpdateModal').hide();
        $('#couponEditModal').hide();
        $('#productEditModal').hide();
        $('#productUpdateModal').hide();
        $('.colorContainer').remove();
    });

    $('#cancelEditFaqBtn').click(function () {
        $('#faqEditModal').hide();
    });

    $('#cancelEditCouponBtn').click(function () {
        $('#couponEditModal').hide();
    });

    $('#cancelUpdateCouponBtn').click(function () {
        $('#couponUpdateModal').hide();
    });

    $('#cancelEditProductBtn').click(function () {
        $('#productTitle').val('');
        $('#productPrice').val('');
        $('#productCode').val('');
        $('#productContry').val('');
        $('#productManufactureDate').val('');
        $('#productMaterial').val('');
        $('#productFeatures').val('');
        $('.colorContainer').empty();
        $('#productCategorySelect1').empty();
        $('#productCategorySelect2').empty();
        $('#productCategorySelect3').empty();
        $('#thumbnailInput').val('');
        $('#thumbnailPreview').empty();
        $('#productEditModal').hide();
        if (window.ckEditor) {
            window.ckEditor.setData('');
        }
    });

    $('#cancelUpdateProductBtn').click(function (){
        $('#productUpdateModal').hide();
        $('.colorContainer').empty();
        $('#updateProductCategorySelect1').empty();
        $('#updateProductCategorySelect2').empty();
        $('#updateProductCategorySelect3').empty();
        $('#thumbnailInput').val('');
        $('#thumbnailPreview').empty();
        if(window.updateCkEditor) {
            window.updateCkEditor.setData('');
        }
        newThumbnails = [];
    });

    $(window).click(function (event) {
        if (event.target.id === 'noticeModal') {
            $('#noticeModal').hide();
        } else if (event.target === $('#faqEditModal')[0]) {
            $('#faqEditModal').hide();
        } else if (event.target === $('#faqUpdateModal')[0]) {
            $('#faqUpdateModal').hide();
        } else if (event.target === $('#couponEditModal')[0]) {
            $('#couponEditModal').hide();
        } else if (event.target === $('#couponUpdateModal')[0]) {
            $('#couponUpdateModal').hide();
        } else if (event.target === $('#productEditModal')[0]) {
            $('#productTitle').val('');
            $('#productPrice').val('');
            $('#productCode').val('');
            $('#productContry').val('');
            $('#productManufactureDate').val('');
            $('#productMaterial').val('');
            $('#productFeatures').val('');
            $('.colorContainer').empty();
            $('#productCategorySelect1').empty();
            $('#productCategorySelect2').empty();
            $('#productCategorySelect3').empty();
            $('#thumbnailInput').val('');
            $('#thumbnailPreview').empty();
            $('#productEditModal').hide();
        } else if(event.target === $('#productUpdateModal')[0]) {
            $('.colorContainer').empty();
            $('#productCategorySelect1').empty();
            $('#productCategorySelect2').empty();
            $('#productCategorySelect3').empty();
            $('#updateThumbnailInput').val('');
            $('#updateThumbnailPreview').empty();
            newThumbnails = [];
            $('#productUpdateModal').hide();
            productList();
        }
    });

    // faq 시작
    // 초기 faq 목록 로드
    $('#faqList').click(function (event) {
        event.preventDefault();
        $('#faqsDivList').show();
        $('#membersTable, #loadMemberMore').hide();
        $('#noticesTable, #loadNoticeMore, #noticeEdit').hide();
        $('#categoryDivList').hide();
        let tag = document.getElementsByClassName("active")[0].getAttribute("id").toUpperCase();
        loadFaqContent(tag);
    });

    // faq 글쓰기 모달 selectbox로 데이터가져오기
    $('#faqEditBtn').click(function (event) {
        event.preventDefault();
        $('#faqEditModal').show();
        loadTagList();
    });

    // coupon 등록 modal
    $('#couponEditBtn').click(function (event) {
        event.preventDefault();

        $('#couponEditTitle').val('');
        $('.couponEditCode').val('');
        $('#couponEditPeriod').val('');
        $('#couponEditBenefit').val('5');
        $('#couponEditQuantity').val('');
        $('#couponEditModal').show();

    });


    // faq SelectBox Tag 목록 로드
    function loadTagList() {
        $.ajax({
            url: '/admin/faqTagList',
            type: 'GET',
            success: function (tagList) {
                const tagEditSelect = $('#tagEditSelect');
                tagEditSelect.empty();
                tagList.forEach(tag => {
                    tagEditSelect.append(new Option(tag.tagName, tag.value))
                });
            },
            error: function (xhr, status, error) {
                console.error('태그 로드 실패' + error);
            }
        });
    }


    // 쿠폰등록 Modal
    $('#editCouponBtn').click(function () {
        const dataForm = {
            title: $('#couponEditTitle').val(),
            code: $('.couponEditCode').val(),
            period: $('#couponEditPeriod').val(),
            benefit: $('#couponEditBenefit').val(),
            quantity: $('#couponEditQuantity').val(),
            status: 'NEW'
        }
        $.ajax({
            url: '/admin/addCoupon',
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify(dataForm),
            success: function (response) {
                alert('저장완료');
                $('#couponEditModal').hide();
                couponList();
            },
            error: function (xhr, status, error) {
                console.error('coupon 저장 실패' + error);
            }
        });
    });

    // faq 등록
    $('#editFaqBtn').click(function () {
        const dataForm = {
            tag: $('#tagEditSelect').val(),
            question: $('#faqEditQuestionModalContent').val(),
            answer: $('#faqEditAnswerModalContent').val()
        };

        $.ajax({
            url: '/admin/faqEdit',
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify(dataForm),
            success: function (response) {
                alert('저장완료');
                $('#faqEditModal').hide();
                let activeTag = $('.tabs li.active').attr('id').toUpperCase();
                loadFaqContent(activeTag, '', pageNumber);
            },
            error: function (xhr, status, error) {
                console.error('저장실패' + error);
            }
        });
    });

    // faq 수정
    $('#updateFaqBtn').click(function (event) {
        event.preventDefault();
        const faqId = $('#faqUpdateId').val();
        const updateFaqData = {
            tag: $('#tagUpdateSelect').val(),
            question: $('#faqUpdateQuestionModalContent').val(),
            answer: $('#faqUpdateAnswerModalContent').val()
        };
        $.ajax({
            url: '/admin/faqEdit/' + faqId,
            type: 'PUT', // PUT으로 변경
            contentType: 'application/json',
            data: JSON.stringify(updateFaqData),
            success: function (response) {
                alert('수정완료');
                $('#faqUpdateModal').hide();
                let activeTag = $('.tabs li.active').attr('id').toUpperCase();
                loadFaqContent(activeTag, '', pageNumber);
            },
            error: function (xhr, status, error) {
                console.error('fsq 수정 실패' + error);
            }
        });
    });
    // faq 삭제
    $('#deleteFaqBtn').click(function (event) {
        event.preventDefault();
        if (confirm("삭제하시겠습니까?")) {
            const faqId = $('#faqUpdateId').val();
            $.ajax({
                url: '/admin/faqDelete/' + faqId,
                type: 'DELETE',
                success: function (response) {
                    alert('삭제 완료');
                    $('#faqUpdateModal').hide();
                    let activeTag = $('.tabs li.active').attr('id').toUpperCase();
                    loadFaqContent(activeTag, '', 0);
                },
                error: function (xhr, status, error) {
                    console.error('fsq 삭제 실패' + error);
                }
            });
        } else {
        }
    });

    // faq 시작
    let resTag = $("ul.tabs input").val();
    let keyword = $("#condition-hidden").val();
    $("#condition").val(keyword);

    //탭 이동
    changeActive(resTag);

    //li 클릭이벤트로 폼 전송
    $('ul.tabs li').click(function () {
        pageNumber = 0;

        //클래스 수정
        $('li').removeClass('active');
        $(this).addClass('active');

        let tag = $(this).attr('id').toUpperCase();
        let condtion = $('#condition-hidden').val();
        $('#submitBtn').click();
    });

    //검색 버튼 클릭 이벤트
    $('#submitBtn').click(function () {
        pageNumber = 0;
        faqSearchBtn();
    });

    function loadFaqContent(tag = '', condition = '', page = 0) {
        $.ajax({
            url: '/admin/faqsList',
            method: 'GET',
            data: {tag: tag.toUpperCase(), condition: condition, page: page},
            success: function (response) {
                const faqDivContents = $('#faqsDivList .contents');
                faqDivContents.empty();
                $.each(response.contents, function (index, contents) {
                    const row = $('<div>');
                    row.html(`
                            <div class="content">
                            <div class="question"><p><a href="#" class="editModal-link" data-id="${contents.id}" data-question="${contents.question}" data-answer="${contents.answer}" data-tag="${contents.tag}">${contents.question}</a></p></div>
                            <div class="answer"><p>${contents.answer}</p></div>
                            </div>
                        `);
                    faqDivContents.append(row);
                });
                $('.editModal-link').click(editModal);

                $('#page').val(page);

                if (response.count <= 0) {
                    $('#pagingBtn').hide();
                } else {
                    $('#pagingBtn').show();
                    $('#pagingBtn').text('더보기(' + response.count + ')');
                }
            },
            error: function (error) {
                console.error('Error', error);
            }
        });
    }

    // faq 링크 클릭 모달 핸들러
    function editModal(event) {
        event.preventDefault();
        const faqs = $(this).data();

        $('#faqUpdateId').val(faqs.id);
        $('#faqUpdateQuestionModalContent').val(faqs.question);
        $('#faqUpdateAnswerModalContent').val(faqs.answer);

        // 카테고리 옵션 추가
        addOptionsToModal([
            {value: 'ALL', text: '전체'},
            {value: 'WEBSITE', text: '웹사이트'},
            {value: 'MILEAGE', text: '통합 마일리지'},
            {value: 'REPAIR', text: '수선'},
            {value: 'STORE', text: '매장 관련'},
            {value: 'PRODUCT', text: '제품'},
            {value: 'MEMBERS', text: '멤버스'},
            {value: 'RETURN', text: '교환/반품'}
        ]);
        $('#tagUpdateSelect').val(faqs.tag);

        $('#faqUpdateModal').show();
    }

    function addOptionsToModal(tags) {
        let selectElement = $('#tagUpdateSelect');
        selectElement.empty(); // 기존의 옵션들 제거

        // 옵션 추가
        tags.forEach(function (tag) {
            let option = $('<option></option>').attr('value', tag.value).text(tag.text);
            selectElement.append(option);
        });
    }

    function faqSearchBtn() {
        let tag = document.getElementsByClassName("active")[0].getAttribute("id").toUpperCase();
        let page = pageNumber;
        let condition = $('#condition').val();

        $.ajax({
            url: '/admin/faqsList',
            method: 'GET',
            data: {tag: tag, page: page, condition: condition},
            success: function (response) {
                const faqDivContents = $('#faqsDivList .contents');
                faqDivContents.empty();
                $.each(response.contents, function (index, contents) {

                    const row = $('<div>');
                    row.html(`
                            <div class="content">
                            <div class="question"><p><a href="#" class="editModal-link" data-id="${contents.id}" data-question="${contents.question}" data-answer="${contents.answer}" data-tag="${contents.tag}">${contents.question}</a></p></div>
                            <div class="answer"><p>${contents.answer}</p></div>
                            </div>
                        `);
                    faqDivContents.append(row);
                });
                $('.editModal-link').click(editModal);

                $('#page').val(page);

                if (response.count <= 0) {
                    $('#pagingBtn').hide();
                } else {
                    $('#pagingBtn').show();
                    $('#pagingBtn').text('더보기(' + response.count + ')');
                }
            },
            error: function (error) {
                console.error('Error:', error);
            }
        });
    }

    //더보기 버튼 클릭 이벤트
    $('#pagingBtn').click(function () {
        paging();
    });

    function paging() {
        pageNumber++; //페이지 번호 증가
        $.get('/admin/api/faqs', {
            page: pageNumber,
            condition: $("#condition").val(),
            tag: document.getElementsByClassName("active")[0].getAttribute("id").toUpperCase()
        })
            .done(data => {
                let contents = data.contents;
                let count = data.count;
                let divContents = $("div.contents");
                contents.forEach(contents => {
                    divContents.append(`
                           <div class="content"></div>
                           <div class="questrion"><p><a href="" class="editModal-link" data-id="${contents.id}" data-question="${contents.question}" data-answer="${contents.answer}" data-tag="${contents.tag}">${contents.question}</a></p></div>
                           <div class="answer"><p>${contents.answer}</p></div>
                        `)
                });
                $('.editModal-link').click(editModal);

                $("#pagingBtn").text('더보기(' + data.count + ')');

                if (count === 0) {
                    $("#pagingBtn").hide();
                }
            })
            .fail(error => {
                console.error('Error', error);
            });
    }

    function changeActive(resTag) {
        $('li').removeClass('active');

        switch (resTag) {
            case "ALL":
                $("#all").addClass("active");
                break;
            case "WEBSITE":
                $("#website").addClass("active");
                break;
            case "MILEAGE":
                $("#mileage").addClass("active");
                break;
            case "REPAIR":
                $("#repair").addClass("active");
                break;
            case "STORE":
                $("#store").addClass("active");
                break;
            case "PRODUCT":
                $("#product").addClass("active");
                break;
            case "MEMBERS":
                $("#members").addClass("active");
                break;
            case "RETURN":
                $("#return").addClass("active");
                break;
            default:
                $("#all").addClass("active");
                break;
        }
    }

    //Enter 검색
    $('#condition').on('keypress', function (event) {
        if (event.keyCode === 13) {
            event.preventDefault();
            pageNumber = 0; // Enter 검색 시 페이지 번호 초기화
            const tag = document.getElementsByClassName('active')[0].getAttribute('id').toUpperCase();
            loadFaqContent(tag);
        }
    });

    // 카테고리 목록 시작
    function categoryList(title) {
        $.ajax({
            url: '/admin/categoryList',
            type: 'GET',
            data: {title: title},
            success: function (data) {
                var categoryDtos = data.categoryDtos;

                if (Array.isArray(categoryDtos)) {
                    var ulFeatured = $('#ul_Featured');
                    var ulShoes = $('#ul_Shoes');
                    var ulSupplies = $('#ul_Supplies');
                    var ulUnderwear = $('#ul_Underwear');
                    var ulClothing = $('#ul_Clothing');
                    var ulSports = $('#ul_Sports');
                    var ulYearRound = $('#ul_YearRound');

                    ulFeatured.empty();
                    ulShoes.empty();
                    ulClothing.empty();
                    ulSupplies.empty();
                    ulUnderwear.empty();
                    ulSports.empty();
                    ulYearRound.empty();

                    categoryDtos.forEach(function (category) {
                        if (title !== "KIDS") {
                            if (category.ref === 1) {
                                ulFeatured.append('<li id="' + category.id + '" data-step="' + category.step + '">' + category.name + '</li>');
                            } else if (category.ref === 2) {
                                ulShoes.append('<li id="' + category.id + '" data-step="' + category.step + '">' + category.name + '</li>');
                            } else if (category.ref === 3) {
                                ulClothing.append('<li id="' + category.id + '" data-step="' + category.step + '">' + category.name + '</li>');
                            } else if (category.ref === 4) {
                                ulSupplies.append('<li id="' + category.id + '" data-step="' + category.step + '">' + category.name + '</li>');
                            } else if (category.ref === 5) {
                                ulUnderwear.append('<li id="' + category.id + '" data-step="' + category.step + '">' + category.name + '</li>');
                            } else if (category.ref === 6) {
                                ulSports.append('<li id="' + category.id + '" data-step="' + category.step + '">' + category.name + '</li>');
                            }
                            $('#cateKids').hide();
                            $('#cateUnderwear').show();
                            $('#cateSports').show();
                            ulListHeight();
                        } else {
                            if (category.ref === 1) {
                                ulFeatured.append('<li id="' + category.id + '" data-step="' + category.step + '">' + category.name + '</li>');
                            } else if (category.ref === 2) {
                                ulShoes.append('<li id="' + category.id + '" data-step="' + category.step + '">' + category.name + '</li>');
                            } else if (category.ref === 3) {
                                ulClothing.append('<li id="' + category.id + '" data-step="' + category.step + '">' + category.name + '</li>');
                            } else if (category.ref === 4) {
                                ulSupplies.append('<li id="' + category.id + '" data-step="' + category.step + '">' + category.name + '</li>');
                            } else if (category.ref === 7) {
                                ulYearRound.append('<li id="' + category.id + '" data-step="' + category.step + '">' + category.name + '</li>');
                            }
                            $('#cateKids').show();
                            $('#cateUnderwear').hide();
                            $('#cateSports').hide();
                            ulListHeight();
                        }
                    });
                } else {
                    console.log('not array');
                }
            },
            error: function (error) {
                console.error('Error:', error);
            }
        });
    }

    // 탭 클릭시 이동
    $('.tab-item').on('click', function () {
        // 모든 탭에서 active 클래스 제거하고 클릭된 탭에 active 추가
        $('.tab-item').removeClass('active');
        $(this).addClass('active');

        // 현재 설정된 탭의 타이틀 가져오기
        const title = $(this).data('title');

        categoryList(title);
        $('#depth_1, #depth_2, #depth_3, #depth_4, #depth_5, #depth_6, #depth_7').val('');
    });

    // 추가, 수정, 삭제 함수
    function multiCategory(ref, action, selectedText, title, categoryId) {
        const text = $('#depth_' + ref).val();
        let step;

        if (action === 'addItem') {
            step = $('#' + getSelectedListId(title)[ref - 1] + ' li').length + 1;
            categoryId = null; // 추가 시 id를 null로 설정
        } else {
            step = selectedText.data('step');
        }

        $.ajax({
            url: '/admin/' + action + (categoryId ? '/' + categoryId : ''),
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify({id: categoryId, name: text, ref: ref, step: step, title: title}),
            success: function (data) {
                if (action === 'addItem') {
                    const newId = data.id
                    $('#' + getSelectedListId(title)[ref - 1]).append('<li id="' + newId + '" data-step="' + step + '">' + text + '</li>');
                    $('#depth_' + ref).val('');
                    categoryList(title);
                } else if (action === 'editItem') {
                    selectedText.text(text);
                    $('#depth_' + ref).val('');
                    categoryList(title);
                } else if (action === 'deleteItem') {
                    selectedText.remove();
                    $('#depth_' + ref).val('');
                    categoryList(title);
                }
            },
            error: function (xhr, status, error) {
                console.error('Error:', error);
                alert('서버 오류가 발생했습니다.');
            }
        });
    }

    function getSelectedListId(title) {
        switch (title) {
            case 'MEN':
            case 'WOMEN':
                return ['ul_Featured', 'ul_Shoes', 'ul_Clothing', 'ul_Supplies', 'ul_Underwear', 'ul_Sports'];
            case 'KIDS':
                return ['ul_Featured', 'ul_Shoes', 'ul_Clothing', 'ul_Supplies', '', '', 'ul_YearRound']; // KIDS의 설정된 ref값 만큼의 배열의 길이가 필요, ul_Supplies, ul_Underwear는 사용하지 않는 태그로 공백처리로 배열의 값 설정
            default:
                return [];
        }
    }

    // 추가, 수정, 삭제 버튼 클릭 시 이벤트 처리
    for (let i = 1; i <= 10; i++) {
        $('#depth_' + i + '_BtnAdd').click(function () {
            const title = $('.category_tabs .tab-item.active').data('title');
            multiCategory(i, 'addItem', null, title, null);
        });

        $('#depth_' + i + '_BtnEdit').click(function () {
            const selectedText = $('#' + getSelectedListId($('.category_tabs .tab-item.active').data('title'))[i - 1] + ' li.selected');
            if (selectedText.length) {
                const title = $('.category_tabs .tab-item.active').data('title');
                const categoryId = selectedText.attr('id');
                multiCategory(i, 'editItem', selectedText, title, categoryId);
            } else {
                alert('수정할 항목을 선택하세요.');
            }
        });


        $('#depth_' + i + '_BtnDelete').click(function () {
            const selectedText = $('#' + getSelectedListId($('.category_tabs .tab-item.active').data('title'))[i - 1] + ' li.selected');
            if (selectedText.length) {
                const title = $('.category_tabs .tab-item.active').data('title');
                const categoryId = selectedText.attr('id'); // 선택된 아이템의 ID를 가져옴
                multiCategory(i, 'deleteItem', selectedText, title, categoryId);
            } else {
                alert('삭제할 항목을 선택하세요.');
            }
        });
    }

    $(document).on('click', '#ul_Featured li, #ul_Shoes li, #ul_Clothing li, #ul_Supplies li, #ul_Underwear li, #ul_Sports li, #ul_YearRound li', function () {
        $(this).siblings().removeClass('selected');
        $(this).addClass('selected');

        const parentId = $(this).parent().attr('id');
        const text = $(this).text();

        if (parentId === 'ul_Featured') {
            $('#depth_1').val(text);
        } else if (parentId === 'ul_Shoes') {
            $('#depth_2').val(text);
        } else if (parentId === 'ul_Clothing') {
            $('#depth_3').val(text);
        } else if (parentId === 'ul_Supplies') {
            $('#depth_4').val(text);
        } else if (parentId === 'ul_Underwear') {
            $('#depth_5').val(text);
        } else if (parentId === 'ul_Sports') {
            $('#depth_6').val(text);
        } else if (parentId === 'ul_YearRound') {
            $('#depth_7').val(text);
        }
    });

    // 동적 li태그 추가 시 높이 자동맞춤
    function ulListHeight() {
        const uls = document.querySelectorAll('.depth ul');
        let maxHeight = 0;

        uls.forEach(ul => {
            ul.style.height = 'auto';
            if (ul.clientHeight > maxHeight) {
                maxHeight = ul.clientHeight;
            }
        });

        uls.forEach(ul => {
            ul.style.height = maxHeight + 'px';
        });
    }

    // 쿠폰코드 생성 버튼 호출
    // $('#randomCodeBtn').click(function () {
    $('.randomCodeBtn').click(function () {
        randomCode();
    });

    // 쿠폰코드 생성 함수
    function randomCode() {
        let characters = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ';
        let code = '';
        for (let i = 0; i < 9; i++) {
            code += characters.charAt(Math.floor(Math.random() * characters.length));
        }
        $('.couponEditCode').val(code);
    }

    // 쿠폰수량 입력필드 숫자만 입력가능
    $(function () {
        var alerted = false;
        $('#couponEditQuantity').on('input', function () {

            if (alerted) {
                alerted = false;
                return;
            }

            const inputVal = $(this).val();

            if (!/^\d+$/.test(inputVal)) {
                alert('숫자만입력가능합니다.');
                alerted = true;
                $(this).val('');
            }
        });
    });

    // jQuery 달력위젯
    $(function () {
        $('#couponEditPeriod').datepicker({
            dateFormat: 'yy-mm-dd',
            showMonthAfterYear: true,
            changeYear: true,
            changeMonth: true,
            minDate: 0,
            monthNamesShort: ['1월', '2월', '3월', '4월', '5월', '6월', '7월', '8월', '9월', '10월', '11월', '12월'],
            dayNamesMin: ['일', '월', '화', '수', '목', '금', '토'],
            onSelect: function (dateText) {
                const dateTime = dateText + " 23:59:59";
                $(this).val(dateTime);
            }
        });
    });

    $(function () {
        $('#couponUpdatePeriod').datepicker({
            dateFormat: 'yy-mm-dd',
            showMonthAfterYear: true,
            changeYear: true,
            changeMonth: true,
            minDate: 0,
            monthNamesShort: ['1월', '2월', '3월', '4월', '5월', '6월', '7월', '8월', '9월', '10월', '11월', '12월'],
            dayNamesMin: ['일', '월', '화', '수', '목', '금', '토'],
            onSelect: function (dateText) {
                const dateTime = dateText + " 23:59:59";
                $(this).val(dateTime);
            }
        });
    });


    $(function() {
        $('#productManufactureDate').datepicker({
            dateFormat: 'yy-mm',
            showMonthAfterYear: true,
            changeYear: true,
            changeMonth: true,
            monthNamesShort: ['1월', '2월', '3월', '4월', '5월', '6월', '7월', '8월', '9월', '10월', '11월', '12월'],
            onClose: function() {
                // 선택한 연도와 월 가져오기
                const year = $('#ui-datepicker-div .ui-datepicker-year :selected').val();
                let month = $('#ui-datepicker-div .ui-datepicker-month :selected').val();

                // 월 2자리로 포맷팅
                month = (parseInt(month) + 1).toString().padStart(2, '0'); // padStart 메서드는 두자리로 포맷팅하는 것, 지정 길이보다 짧으면 앞에 0을 채워 문자열의 길이 맞춤
                $(this).val(year + '-' + month + '-' + '01'); // 월은 0부터 시작하므로 1을 더해줌
                // 년월만 넘겨주려했으나 서버에서 년월만 받으려면 convert 하는 작업을 진행해야하므로 01을 넣어줌
            }
        });
    });

    $(function() {
        $('#updateProductManufactureDate').datepicker({
            dateFormat: 'yy-mm-dd',
            showMonthAfterYear: true,
            changeYear: true,
            changeMonth: true,
            monthNamesShort: ['1월', '2월', '3월', '4월', '5월', '6월', '7월', '8월', '9월', '10월', '11월', '12월'],
            onClose: function() {
                // 선택한 연도와 월 가져오기
                const year = $('#ui-datepicker-div .ui-datepicker-year :selected').val();
                let month = $('#ui-datepicker-div .ui-datepicker-month :selected').val();

                // 월 2자리로 포맷팅
                month = (parseInt(month) + 1).toString().padStart(2, '0'); // padStart 메서드는 두자리로 포맷팅하는 것, 지정 길이보다 짧으면 앞에 0을 채워 문자열의 길이 맞춤
                $(this).val(year + '-' + month + '-' + '01'); // 월은 0부터 시작하므로 1을 더해줌
                // 년월만 넘겨주려했으나 서버에서 년월만 받으려면 convert 하는 작업을 진행해야하므로 01을 넣어줌
            }
        });
    });



    // ckEditor Setting
    ClassicEditor
        .create(document.querySelector('#ckEditor'), {
            ckfinder : {
                uploadUrl : '/image/upload' // 해당기능 처리할 controller url
            }
        }).then(editor => {
        window.ckEditor = editor
    })
        .catch(error => {
            console.error(error);
        });

    // update Settig
    ClassicEditor
        .create(document.querySelector('#updateCkEditor'), {
            ckfinder : {
                uploadUrl : '/image/upload' // 해당기능 처리할 controller url
            }
        }).then(editor => {
        window.updateCkEditor = editor
    })
        .catch(error => {
            console.error(error);
        });

    // product 등록 modal
    $('#productEditBtn').click(function (event) {
        event.preventDefault();

        // 달력 위젯 숨기기
        $('#productManufactureDate').focus(function(){
            $('.ui-datepicker-calendar').hide();
        });

        $('#productEditModal').show();

        loadCategoryList();

        $('#productCategorySelect2').prop('disabled', true);
        $('#productCategorySelect3').prop('disabled', true);
    });

    // 첫 번째 셀렉트박스 변경 이벤트 핸들러
    $(document).on('change', '#productCategorySelect1', function(){
        const selectedParentCategory = $(this).val();


        if(selectedParentCategory) {
            loadSubCategories(selectedParentCategory, function(){
                $('#productCategorySelect2').prop('disabled', false).trigger('change');
            });
        } else {
            //선택이 취소된 경우 두 번째, 세 번째 셀렉트박스 초기화
            $('#productCategorySelect2').empty().append('<option value=""></option>').prop('disabled', true);
            $('#productCategorySelect3').empty().append('<option value=""></option>').prop('disabled', true);
        }
    });

    // 두 번째 셀렉트 박스 변경 이벤트 핸들러
    $(document).on('change', '#productCategorySelect2', function() {
        const selectedSubCategory = $(this).val();

        if (selectedSubCategory) {
            const selectedParentCategory = $('#productCategorySelect1').val();

            if (selectedParentCategory) {
                loadDetailedCategories(selectedParentCategory, selectedSubCategory, function() {
                    $('#productCategorySelect3').prop('disabled', false).trigger('change');
                });
            } else {
                console.error('Selected Parent Category is not defined.');
            }
        } else {
            // 선택이 취소된 경우 세 번째 셀렉트 박스 초기화
            $('#productCategorySelect3').empty().append('<option value=""></option>').prop('disabled', true);
        }
    });

    // 첫 번째 셀렉트박스 로드
    function loadCategoryList(){
        $.ajax({
            url: '/products/categories',
            type: 'GET',
            success: function(response){
                const categories = response.categories;
                const productCategorySelect1 = $('#productCategorySelect1');
                productCategorySelect1.empty();
                productCategorySelect1.append('<option value="">카테고리 선택</option>');

                const categorySet = new Set();
                categories.forEach(category =>{
                    categorySet.add(category.title);
                });

                const uniqueCategories = Array.from(categorySet).sort();

                // 카테고리 목록을 첫 번째 셀렉트 박스에 추가
                uniqueCategories.forEach(title => {
                    productCategorySelect1.append(new Option(title, title));
                });
            },

            error: function(error){
                console.error('error:', error);
            }
        });
    }

    const categoryMapping = {
        1: 'Featured',
        2: '신발',
        3: '의류',
        4: '용품',
        5: '언더웨어',
        6: '스포츠',
        7: '연중판매'
    }

    function loadSubCategories(parentCategory, callback) {
        $.ajax({
            url: '/products/subCategories',
            type: 'GET',
            data: { title : parentCategory },
            success: function(response){
                const subCategories = response.categoryDtos;
                const productCategorySelect2 = $('#productCategorySelect2');
                const productCategorySelect3 = $('#productCategorySelect3');
                productCategorySelect2.empty();
                productCategorySelect3.empty();
                productCategorySelect2.append('<option value="">카테고리 선택</option>');
                productCategorySelect3.append('<option value="">카테고리 선택</option>');

                const uniqueSubCategories = new Set();
                subCategories.forEach(subCategory => {
                    uniqueSubCategories.add(subCategory.ref);
                });

                // Set을 배열로 변환하여 정렬
                const sortUniqueSubCategories = Array.from(uniqueSubCategories).sort();

                sortUniqueSubCategories.forEach(ref => {
                    const text = categoryMapping[ref] || ref;
                    productCategorySelect2.append(new Option(text, ref));
                });

                if(callback) callback();
            },
            error: function (xhr, status, error){
                console.error('로드 실패' + error);
            }
        });
    }

    // 세부 카테고리 로드
    function loadDetailedCategories(parentTitle, subCategoryRef, callback) {

        $.ajax({
            url: '/products/detailedCategories',
            type: 'GET',
            data: {
                parentTitle: parentTitle,
                subCategoryRef: subCategoryRef
            },
            success: function(response) {

                const detailedCategories = response.detailedCategories;
                const selectBox3 = $('#productCategorySelect3');
                selectBox3.empty();
                selectBox3.append('<option value="">카테고리 선택</option>');
                detailedCategories.forEach(detailedCategory => {
                    selectBox3.append(new Option(detailedCategory.name, detailedCategory.id));
                });

                if(callback) callback();
            },
            error: function(xhr, status, error) {
                console.error('로드 실패:', error);
            }
        });
    }


    // 상품 등록 버튼
    $('#editProductBtn').click(function (event) {
        event.preventDefault();
        const title = $('#productTitle').val();
        const price = $('#productPrice').val();
        const code = $('#productCode').val();
        const contry = $('#productContry').val();
        const manufacture = $('#productManufactureDate').val();
        const material = $('#productMaterial').val();
        const features = $('#productFeatures').val();
        const editorData = window.ckEditor.getData();

        const colors = getColorDataAdd();

        // ckEditor의 데이터 imgUrl 함수에 매개변수로 전달
        const imageUrls = imgUrl(editorData);

        function imgUrl(imgContent) {
            const div = document.createElement('div');
            div.innerHTML = imgContent;

            const imgTags = div.getElementsByTagName('img');
            const imgUrls = [];

            for(let img of imgTags) {
                imgUrls.push(img.src);
            }
            return imgUrls;
        }

        // formData 객체 생성
        const formData = new FormData();
        const productDto = {
            title: title,
            content: editorData,
            option: JSON.stringify(colors), // colors를 JSON 문자열로 직렬화하여 option 필드에 저장
            price: price,
            code: code,
            contry: contry, // 오타 유지
            manufactureDate: manufacture,
            material: material,
            features: features,
            imageUrls: imageUrls,
            category: $('#productCategorySelect3').val(),
        };
        formData.append('productDto', new Blob([JSON.stringify(productDto)], { type: "application/json" }));
        // 여러 개의 썸네일 파일 추가
        const thumbnailFiles = document.getElementById('thumbnailInput').files;
        for (let i = 0; i < thumbnailFiles.length; i++) {
            formData.append('thumbnails', thumbnailFiles[i]);
        }

        $.ajax({
            url: '/products/addProduct',
            type: 'POST',
            processData: false, // FormData 사용 시 설정
            contentType: false, // FormData 사용 시 설정
            data: formData, // JSON 직렬화
            success: function (data) {
                alert('저장완료');
                $('#productTitle').val('');
                $('#productCode').val('');
                $('#productContry').val(''); // 오타 유지
                $('#productFeatures').val('');
                $('#productManufactureDate').val('');
                $('#productMaterial').val('');
                $('#productPrice').val('');
                window.ckEditor.setData(''); // ckEditor 필드 초기화
                $('.color-group').remove(); // 생성된 색상 그룹 필드 삭제

                $('#productCategorySelect1').val('');
                $('#productCategorySelect2').val('');
                $('#productCategorySelect3').val('');
                $('#thumbnailInput').val('');
                $('#thumbnailPreview').html('');

                $('#productEditModal').hide();
                productList();
            },
            error: function (xhr, status, error) {
                console.error('Error:', error);
                alert('서버 오류가 발생했습니다.');
            }
        });
    });

    document.getElementById('thumbnailInput').addEventListener('change', function (event) {
        const files = event.target.files;
        const previewContainer = document.getElementById('thumbnailPreview');
        previewContainer.innerHTML = ''; // 기존 미리보기 초기화

        Array.from(files).forEach((file, index) => {
            if (file.type.startsWith('image/')) {
                const reader = new FileReader();
                reader.onload = function (e) {
                    const container = document.createElement('div');
                    container.style.display = 'inline-block';
                    container.style.position = 'relative';

                    // 이미지 생성
                    const img = document.createElement('img');
                    img.src = e.target.result;
                    img.style.width = '100px'; // 미리보기 이미지 크기 설정
                    img.style.height = '100px';
                    img.style.margin = '5px';
                    container.appendChild(img);

                    // 삭제 버튼 생성
                    const removeBtn = document.createElement('button');
                    removeBtn.innerHTML = 'X';
                    removeBtn.style.position = 'absolute';
                    removeBtn.style.top = '0';
                    removeBtn.style.right = '0';
                    removeBtn.style.backgroundColor = 'red';
                    removeBtn.style.color = 'white';
                    removeBtn.style.border = 'none';
                    removeBtn.style.cursor = 'pointer';

                    removeBtn.addEventListener('click', function() {
                        // 현재 이미지 컨테이너 제거
                        previewContainer.removeChild(container);

                        // 선택된 파일 리스트에서 파일 제거
                        // files는 event.target.files로 전달된 FileList 객체를 배열로 변환한 것
                        // _ 첫번째 매개변수는 사용하지 않을때 언더스코어 사용, i는 두번째 매개변수로 현재 요소의 인덱스
                        const newFileList = Array.from(files).filter((_, i) => i !== index);
                        // 새 FileList 객체를 만들기 위해 DataTransfer 객체 생성
                        const dataTransfer = new DataTransfer();
                        
                        // 새 파일 리스트의 각 파일을 DataTransfer 객체에 추가
                        newFileList.forEach(file => dataTransfer.items.add(file));

                        // thumbnailInput 요소의 files 속성을 DataTransfer 객체의 files로 업데이트
                        document.getElementById('thumbnailInput').files = dataTransfer.files;
                    });
                    container.appendChild(removeBtn);
                    previewContainer.appendChild(container);
                };
                reader.readAsDataURL(file);
            } else {
                alert('이미지 파일만 업로드할 수 있습니다.');
                event.target.value = ''; // 파일 입력 초기화
                previewContainer.innerHTML = ''; // 미리보기 초기화
            }
        });
    });



    // 상품 목록 로드
    function productList(){
        $.ajax({
            url: '/products/productList',
            type: 'GET',
            dataType: 'json',
            success: function(response){
                const productTable = $('#productTable tbody');
                productTable.empty();
                $.each(response, function(index, product){
                    const row = $('<tr>');
                    row.append(`<td>${product.price}</td>`);
                    if(product.id) {

                        row.append(`<td>
                                        <img src="${product.thumbnailUrl[0].thumbnailUrl}" alt="Thumbnail" class="product-thumbnail"
                                             data-content='${product.content}' data-id="${product.id}" data-price="${product.price}"
                                             data-code="${product.code}" data-contry="${product.contry}" data-features="${product.features}"
                                             data-material="${product.material}" data-title="${product.title}" data-categoryId="${product.category.id}" data-thumbnails='${JSON.stringify(product.thumbnailUrl)}'
                                             data-manufacture="${product.manufactureDate}" data-categoryTitle="${product.category.title}" data-categoryRef="${product.category.ref}"">
                                    </td>`);
                    } else {
                        row.append(`<td>No Image</td>`);
                    }
                    productTable.append(row);
                });
                $('.product-thumbnail').click(productUpdate);
            },
            error: function(xhr, status, error) {
                console.error('Error fetching product list:', error);
                // Handle error scenario
            }
        });
    }

        document.getElementById('updateThumbnailInput').addEventListener('change', function (event) {
            const files = event.target.files;
            const previewContainer = document.getElementById('updateThumbnailPreview');
            previewContainer.innerHTML = ''; // 기존 미리보기 초기화

            Array.from(files).forEach((file, index) => {
                if (file.type.startsWith('image/')) {
                    const reader = new FileReader();
                    reader.onload = function (e) {
                        const container = document.createElement('div');
                        container.style.display = 'inline-block';
                        container.style.position = 'relative';

                        // 이미지 생성
                        const img = document.createElement('img');
                        img.src = e.target.result;
                        img.style.width = '100px'; // 미리보기 이미지 크기 설정
                        img.style.height = '100px';
                        img.style.margin = '5px';
                        container.appendChild(img);

                        // 삭제 버튼 생성
                        const removeBtn = document.createElement('button');
                        removeBtn.innerHTML = 'X';
                        removeBtn.style.position = 'absolute';
                        removeBtn.style.top = '0';
                        removeBtn.style.right = '0';
                        removeBtn.style.backgroundColor = 'red';
                        removeBtn.style.color = 'white';
                        removeBtn.style.border = 'none';
                        removeBtn.style.cursor = 'pointer';

                        removeBtn.addEventListener('click', function() {
                            // 현재 이미지 컨테이너 제거
                            previewContainer.removeChild(container);

                            // 선택된 파일 리스트에서 파일 제거
                            const newFileList = Array.from(files).filter((_, i) => i !== index);

                            // 새 FileList 객체를 만들기 위해 DataTransfer 객체 생성
                            const dataTransfer = new DataTransfer();

                            // 새 파일 리스트의 각 파일을 DataTransfer 객체에 추가
                            newFileList.forEach(file => dataTransfer.items.add(file));

                            // thumbnailInput 요소의 files 속성을 DataTransfer 객체의 files로 업데이트
                            document.getElementById('updateThumbnailInput').files = dataTransfer.files;
                        });

                        container.appendChild(removeBtn);
                        previewContainer.appendChild(container);
                    };
                    reader.readAsDataURL(file);
                } else {
                    alert('이미지 파일만 업로드할 수 있습니다.');
                    event.target.value = ''; // 파일 입력 초기화
                    previewContainer.innerHTML = ''; // 미리보기 초기화
                }
            });
        });
    let existingThumbnails = [];
    let newThumbnails = [];
    // 상품 수정
    function productUpdate(event){
        event.preventDefault();
        const products = $(this).data();
        // 달력 위젯 숨기기
        $('#updateProductManufactureDate').focus(function(){
            $('.ui-datepicker-calendar').hide();
        });

        $('#updateProductId').val(products.id);
        $('#updateProductCode').val(products.code);
        $('#updateProductPrice').val(products.price);
        $('#updateProductTitle').val(products.title);
        $('#updateProductManufactureDate').val(products.manufacture);
        $('#updateProductFeatures').val(products.features);
        $('#updateProductContry').val(products.contry);
        $('#updateProductMaterial').val(products.material);

        updateCkEditor.setData(products.content);

        // 기존 썸네일 미리보기
        loadExistingThumbnails(products.thumbnails || []);

        const productId = products.id;

        // callback함수
        getProductOptions(productId, function(productDetails){

            // 옵션 바인딩
            bindProductOptions(productDetails);

            // 카테고리 바인딩
            // 첫 번째 셀렉트박스 변경 이벤트 핸들러
            updateLoadCategoryList(function() {
                // 첫 번째 셀렉트 박스의 값 설정
                $('#updateProductCategorySelect1').val(products.categorytitle).trigger('change');

                // 두 번째 셀렉트 박스 로드
                updateLoadSubCategories(products.categorytitle, function() {
                    $('#updateProductCategorySelect2').val(products.categoryref).prop('disabled', false).trigger('change');

                    // 세 번째 셀렉트 박스 로드
                    updateLoadDetailedCategories(products.categorytitle, products.categoryref, function() {
                        $('#updateProductCategorySelect3').val(products.categoryid).prop('disabled', false);
                    });
                });
            });
            $('#productUpdateModal').show();
        });
    }
    // 기존 썸네일 로드 함수
    function loadExistingThumbnails(thumbnails) {
        existingThumbnails = thumbnails.map(thumbnail => ({ id: thumbnail.id, thumbnailUrl: thumbnail.thumbnailUrl }));
        updateThumbnailPreview();
    }

    // 미리보기 업데이트
    function updateThumbnailPreview() {
        const previewContainer = $('#updateThumbnailPreview');
        previewContainer.empty();

        [...existingThumbnails, ...newThumbnails].forEach(thumbnail => {
            const container = $('<div>').css({ display: 'inline-block', position: 'relative' });

            // 이미지 생성
            const img = $('<img>').attr('src', thumbnail.thumbnailUrl).css({ width: '100px', height: '100px', margin: '5px' });
            container.append(img);

            // 삭제 버튼 생성
            const removeBtn = $('<button>')
                .text('X')
                .addClass('remove-thumbnail-btn')
                .css({
                    position: 'absolute',
                    top: '0',
                    right: '0',
                    backgroundColor: 'red',
                    color: 'white',
                    border: 'none',
                    cursor: 'pointer'
                })
                .on('click', function () {
                    container.remove();
                    if (thumbnail.id) {
                        existingThumbnails = existingThumbnails.filter(t => t.id !== thumbnail.id); // 현재 클릭한 썸네일의  ID와 일치하지 않는 썸네일만 배열에 넣음, 클릭한 썸네일을 배열에서 제거
                        deleteThumbnail(thumbnail.id); // 서버에서 썸네일 삭제
                    } else {
                        newThumbnails = newThumbnails.filter(t => t.thumbnailUrl !== thumbnail.thumbnailUrl); // 새로운 썸네일 url 값으로 비교 후 클릭한 썸네일 삭제
                    }
                    updateThumbnailPreview();
                });
            container.append(removeBtn);
            previewContainer.append(container);
        });
    }

    // 새 썸네일 파일 입력 처리
    $('#updateThumbnailInput').on('change', function (event) {
        const files = event.target.files;

        Array.from(files).forEach((file) => {
            if (file.type.startsWith('image/')) {
                const reader = new FileReader();
                reader.onload = function (e) {
                    const thumbnailUrl = e.target.result;
                    newThumbnails.push({ thumbnailUrl });
                    updateThumbnailPreview();
                };
                reader.readAsDataURL(file);
            } else {
                alert('이미지 파일만 업로드할 수 있습니다.');
                $(this).val(''); // 파일 입력 초기화
            }
        });
    });

    // 썸네일 삭제 버튼 클릭 이벤트
    function deleteThumbnail(thumbnailId) {
        $.ajax({
            url: `/products/thumbnails/${thumbnailId}`,
            type: 'DELETE',
            success: function (response) {
                alert('삭제완료');
            },
            error: function (xhr, status, error) {
                console.error('Error delete thumbnail', error);
                alert('썸네일 삭제 실패했습니다.');
            }
        });
    }

    // 상품 수정 카테고리
    function updateLoadSubCategories(parentCategory, callback) {
        $.ajax({
            url: '/products/subCategories',
            type: 'GET',
            data: { title : parentCategory },
            success: function(response){
                const subCategories = response.categoryDtos;
                const updateProductCategorySelect2 = $('#updateProductCategorySelect2');
                updateProductCategorySelect2.empty();
                updateProductCategorySelect2.append('<option value="">카테고리 선택</option>');

                const uniqueSubCategories = new Set();
                subCategories.forEach(subCategory => {
                    uniqueSubCategories.add(subCategory.ref);
                });

                // Set을 배열로 변환하여 정렬
                const sortUniqueSubCategories = Array.from(uniqueSubCategories).sort();

                sortUniqueSubCategories.forEach(ref => {
                    const text = categoryMapping[ref] || ref;
                    updateProductCategorySelect2.append(new Option(text, ref));
                });

                if(callback) callback();
            },
            error: function (xhr, status, error){
                console.error('로드 실패' + error);
            }
        });
    }

    // 첫 번째 상품 수정 셀렉트박스 로드
    function updateLoadCategoryList(callback){
        $.ajax({
            url: '/products/categories',
            type: 'GET',
            success: function(response){
                const categories = response.categories;
                const productCategorySelect1 = $('#updateProductCategorySelect1');
                productCategorySelect1.empty();
                productCategorySelect1.append('<option value="">카테고리 선택</option>');

                const categorySet = new Set();
                categories.forEach(category =>{
                    categorySet.add(category.title);
                });

                const uniqueCategories = Array.from(categorySet).sort();

                // 카테고리 목록을 첫 번째 셀렉트 박스에 추가
                uniqueCategories.forEach(title => {
                    updateProductCategorySelect1.append(new Option(title, title));
                });

                if (callback) callback();

            },

            error: function(error){
                console.error('error:', error);
            }
        });
    }

    // 상품 수정  세부 카테고리 로드
    function updateLoadDetailedCategories(parentTitle, subCategoryRef, callback) {
        $.ajax({
            url: '/products/detailedCategories',
            type: 'GET',
            data: {
                parentTitle: parentTitle,
                subCategoryRef: subCategoryRef
            },
            success: function(response) {
                const detailedCategories = response.detailedCategories;
                const selectBox3 = $('#updateProductCategorySelect3');
                selectBox3.empty();
                selectBox3.append('<option value="">카테고리 선택</option>');
                detailedCategories.forEach(detailedCategory => {
                    selectBox3.append(new Option(detailedCategory.name, detailedCategory.id));
                });
                if(callback) callback();
            },
            error: function(xhr, status, error) {
                console.error('로드 실패:', error);
            }
        });
    }

    // 상품 수정 첫 번째 셀렉트박스 변경 이벤트 핸들러
    $(document).on('change', '#updateProductCategorySelect1', function(){
        const selectedParentCategory = $(this).val();

        if(selectedParentCategory) {
            updateLoadSubCategories(selectedParentCategory, function(){
                $('#updateProductCategorySelect2').prop('disabled', false).trigger('change');
            });
        } else {
            //선택이 취소된 경우 두 번째, 세 번째 셀렉트박스 초기화
            $('#updateProductCategorySelect2').empty().append('<option value=""></option>').prop('disabled', true);
            $('#updateProductCategorySelect3').empty().append('<option value=""></option>').prop('disabled', true);
        }
    });

    // 상품 수정 두 번째 셀렉트 박스 변경 이벤트 핸들러
    $(document).on('change', '#updateProductCategorySelect2', function() {
        const selectedSubCategory = $(this).val();

        if (selectedSubCategory) {
            const selectedParentCategory = $('#updateProductCategorySelect1').val();

            if (selectedParentCategory) {
                updateLoadDetailedCategories(selectedParentCategory, selectedSubCategory, function() {
                    $('#updateProductCategorySelect3').prop('disabled', false).trigger('change');
                });
            } else {
                console.error('Selected Parent Category is not defined.');
            }
        } else {
            // 상품 수정 선택이 취소된 경우 세 번째 셀렉트 박스 초기화
            $('#updateProductCategorySelect3').empty().append('<option value=""></option>').prop('disabled', true);
        }
    });

    function getProductOptions(productId, callback){
        $.ajax({
            url: 'products/getProductDetails',
            type: 'GET',
            data: { productId: productId },
            dataType: 'json',
            success: function(data) {
                callback(data); // callback 함수
            },
            error:function(xhr, status, error){
                console.error('product options 실패 : ' + error);
            }
        });
    }

    // 색상 및 사이즈 입력 그룹 추가
    $('#productEditModal .addColorBtn').click(function () {
        addColorGroup('#productEditModal .colorContainer');
    });

    $('#productUpdateModal .addColorBtn').click(function () {
        addColorGroup('#productUpdateModal .colorContainer');
    });

    let colorCount = 0; // 전역으로 선언하여 초기화

    function addColorGroup(containerSelector) {

        const colorGroup = $('<div>').addClass('color-group').attr('data-color-group', colorCount);

        const colorLabel = $('<label>').addClass('color-label').text(`Color`);
        const colorInput = $('<input>').attr({
            type: 'color',
            name: `colors[${colorCount}].color`,
            placeholder: '색상을 입력하세요.',
            required: true
        });

        const sizes = ['220', '225', '230', '235', '240', '245', '250', '255', '260', '265', '270', '275', '280', '285', '290'];
        sizes.forEach(size => {
            const sizeRow = $('<div>').addClass('size-row');

            const sizeCheckbox = $('<input>').attr({
                type: 'checkbox',
                name: `colors[${colorCount}].sizes`,
                value: size
            });
            const sizeCheckboxLabel = $('<label>').text(size);

            const quantityInput = $('<input>').attr({
                type: 'number',
                name: `colors[${colorCount}].quantities[${size}]`,
                placeholder: '수량',
                min: 0
            });

            sizeRow.append(sizeCheckbox, sizeCheckboxLabel, quantityInput);
            colorGroup.append(sizeRow);
        });

        const removeColorBtn = $('<button>').attr('type', 'button').text('삭제').addClass('remove-color-btn').click(function(){
            colorGroup.remove();

        });

        colorGroup.append(colorLabel, colorInput, removeColorBtn);
        $(containerSelector).append(colorGroup);

        colorCount++;
    }

    function getColorDataAdd() {
        const colors = [];
        // 색상 및 사이즈 데이터
        $('.color-group').each(function () {

            const colorGroup = $(this);
            const color = colorGroup.find('input[type="color"]').val();

            const sizes = [];

            colorGroup.find('.size-row').each(function () {
                const sizeRow = $(this);
                const isChecked = sizeRow.find('input[type="checkbox"]').is(':checked');
                if (isChecked) {
                    const size = sizeRow.find('input[type="checkbox"]').val();
                    const quantity = sizeRow.find('input[type="number"][name*="quantities"]').val();

                    sizes.push({
                        sizeValue: size,
                        quantity: quantity
                    });
                }
            });

            if (sizes.length > 0) {
                colors.push({
                    color: color,
                    productOptionDtoDetailsList: sizes
                });
            }
        });
        return colors;
    }
    function bindProductOptions(data){

        // 색상별 그룹옵션 객체 생성
        const colorGroups = {};
        data.productDetails.forEach(function(product) {

            // productOptions 바인딩
            product.productOptions.forEach(function(option){

                if(colorGroups[option.color]) {
                    // push(option)은 배열에 option 객체를 추가하는 것 option 객체의 모든 정보를 배열에 유지하려는 목적
                    colorGroups[option.color].push(option);
                } else {
                    // option.color 존재하지 않으면 새로운 색상 그룹 생성
                    colorGroups[option.color] = [option];
                }

                // 색상 컨테이너 초기화
                $('.colorContainer').empty();

                // colorGroups에 키로 넣어둔 color의 배열을 반복
                Object.keys(colorGroups).forEach(function(color, index) {
                    const options = colorGroups[color];

                    const colorGroup = $('<div>').addClass('color-group').attr('data-color-group', index);
                    const colorLabel = $('<label>').addClass('color-label').text(`Color`);

                    const colorInput = $('<input>').attr({
                        type: 'color',
                        name: `colors[${index}].color`,
                        value: options[0].color, // 첫 번째 옵션의 color 값을 사용
                        required: true
                    });

                    const sizes = ['220', '225', '230', '235', '240', '245', '250', '255', '260', '265', '270', '275', '280', '285', '290'];
                    sizes.forEach(size =>{
                        const option = options.find(option => option.size === size);
                        const sizeRow = $('<div>').addClass('size-row');

                        const sizeCheckbox = $('<input>').attr({
                            type: 'checkbox',
                            name: `colors[${index}].sizes`,
                            value: size,
                            // checked: options.some(option => option.size === size) ? true : false // 해당 사이즈가 있는지 확인
                            checked: !!option
                        });
                        const sizeCheckboxLabel = $('<label>').text(size);

                        const quantityInput = $('<input>').attr({
                            type: 'number',
                            name: `colors[${index}].quantities[${size}]`,
                            // value: options.find(option => option.size === size)?.quantity || 0,
                            value: option ? option.quantity : null,
                            placeholder: '수량',
                            min: 0
                        });
                        sizeRow.append(sizeCheckbox, sizeCheckboxLabel, quantityInput);

                        // 옵션이 존재할 때만 ID 값을 추가
                        if (option) {
                            const colorOptionId = $('<input>').attr({
                                type: 'hidden',
                                name: `colors[${index}].ids[${size}]`,
                                value: option.id
                            });
                            sizeRow.append(colorOptionId);
                        }

                        colorGroup.append(sizeRow);
                    });

                    // 상품 옵션 삭제
                    const removeColorBtn = $('<button>').text('삭제').addClass('remove-color-btn').click(function(event){
                        event.preventDefault();
                        $.ajax({
                            url: '/products/deleteOption',
                            type: 'POST',
                            data: JSON.stringify({
                                color: options[0].color,
                                optionId: options.map(option => option.id)
                            }),
                            contentType: 'application/json',
                            success: function(response){
                                colorGroup.remove();
                                alert('삭제완료');
                            },
                            error:function(xhr, status, error){
                                console.error('삭제실패',error);
                            }
                        });
                    });
                    colorGroup.append(colorLabel, colorInput, removeColorBtn);
                    $('.colorContainer').append(colorGroup);
                    colorCount++;
                });
            });
        });
    }

    // 상품 삭제
    $('#deleteProductBtn').click(function(){
        const productId = $('#updateProductId').val();

        $.ajax({
            url: '/products/deleteProduct/'+ productId,
            type: 'DELETE',
            success: function(response) {
                alert('삭제완료');
                $('#productUpdateModal').hide();
                productList();
            },
            error: function(xhr, status, error) {
                console.error('삭제실패',error);
            }
        });
    });

    $('#updateProductBtn').click(function(event){
        event.preventDefault();
        const productId = $('#updateProductId').val();

        const title = $('#updateProductTitle').val();
        const price = $('#updateProductPrice').val();
        const code = $('#updateProductCode').val();
        const contry = $('#updateProductContry').val();
        const manufacture = $('#updateProductManufactureDate').val();
        const material = $('#updateProductMaterial').val();
        const features = $('#updateProductFeatures').val();

        const editorData = window.updateCkEditor.getData();

        const colors = getColorDataUpdate();

        // ckEditor의 데이터 imgUrl 함수에 매개변수로 전달
        const imageUrls = imgUrl(editorData);
        function imgUrl(imgContent) {
            const div = document.createElement('div');
            div.innerHTML = imgContent;

            const imgTags = div.getElementsByTagName('img');
            const imgUrls = [];

            for(let img of imgTags) {
                imgUrls.push(img.src);
            }
            return imgUrls;
        }

        // formData 객체 생성
        const formData = new FormData();
        const productDto = {
            title: title,
            content: editorData,
            option: JSON.stringify(colors), // colors를 JSON 문자열로 직렬화하여 option 필드에 저장
            price: price,
            code: code,
            contry: contry,
            manufactureDate: manufacture,
            material: material,
            features: features,
            imageUrls: imageUrls,
            category: $('#updateProductCategorySelect3').val(),
        };
        formData.append('productDto', new Blob([JSON.stringify(productDto)], { type: "application/json" }));

        // 기존 썸네일 추가
        formData.append('existingThumbnails', new Blob([JSON.stringify(existingThumbnails)], { type: "application/json" }));

        // 새 썸네일 파일 추가
        const newThumbnailFiles = document.getElementById('updateThumbnailInput').files;
        for (let i = 0; i < newThumbnailFiles.length; i++) {
            formData.append('thumbnails', newThumbnailFiles[i]);
        }

        $.ajax({
            url: '/products/updateProduct/'+ productId,
            type: 'PUT',
            processData: false,
            contentType: false,
            data: formData,
            success:function(response){
                alert('저장완료');
                $('#productUpdateModal').hide();
                newThumbnails = [];
                productList();
            },
            error:function(xhr, status, error){
                console.error('저장실패',error);
            }
        });
    });

    function getColorDataUpdate() {
        const colors = {};
        const existingColors = [];

        // 기존 데이터에서 사용된 색상 목록 수집
        $('.existing-color-group').each(function() {
            const color = $(this).find('input[type="color"]').val();
            existingColors.push(color);
        });

        // 색상 및 사이즈 데이터
        $('.color-group').each(function() {
            const colorGroup = $(this);
            const color = colorGroup.find('input[type="color"]').val();
            const sizes = [];

            colorGroup.find('.size-row').each(function() {
                const sizeRow = $(this);
                const isChecked = sizeRow.find('input[type="checkbox"]').is(':checked');
                if (isChecked) {
                    const size = sizeRow.find('input[type="checkbox"]').val();
                    const quantity = sizeRow.find('input[type="number"][name*="quantities"]').val();
                    const optionId = sizeRow.find('input[type="hidden"][name*="ids"]').val();

                    sizes.push({
                        id: optionId,
                        sizeValue: size,
                        quantity: quantity
                    });
                }
            });

            if (sizes.length > 0) {
                // 기존 색상 데이터가 존재하는 경우
                if (existingColors.includes(color)) {
                    if (!colors[color]) {
                        colors[color] = [];
                    }
                    colors[color].push(...sizes);
                } else {
                    // 새로운 색상 데이터 추가
                    if (!colors[color]) {
                        colors[color] = [];
                    }
                    colors[color] = sizes;
                }
            }
        });

        return Object.keys(colors).map(color => ({
            color: color,
            productOptionDtoDetailsList: colors[color]
        }));
    }
});