(function ($) {
  "user strict";
  // Preloader Js
  $(window).on('load', function () {
      $('.preloader').fadeOut(1000);
      $('.bg_img').each(function () {
          var bg = $(this).data('background');
          if (bg) {
              this.style.backgroundImage = 'url(' + bg + ')';
          }
      });
  });
    var scrollTop = $(".scrollToTop");
    $(window).on('scroll', function () {
        if ($(this).scrollTop() < 500) {
            scrollTop.removeClass("active");
        } else {
            scrollTop.addClass("active");
        }
    });
    //Click event to scroll to top
    $('.scrollToTop').on('click', function () {
        $('html, body').animate({
            scrollTop: 0
        }, 500);
        return false;
    });
    // Header Sticky Here
    var headerOne = $(".header-section");
    $(window).on('scroll', function () {
        if ($(this).scrollTop() < 1) {
            headerOne.removeClass("header-active");
        } else {
            headerOne.addClass("header-active");
        }
    });
    $('.window-warning .lay').on('click', function () {
        $('.window-warning').addClass('inActive');
    })
    $('.seat-plan-wrapper li .movie-schedule .item').on('click', function () {
        $('.window-warning').removeClass('inActive');
    })
    //Tab Section
    $('.tab ul.tab-menu li').on('click', function (g) {
        var tab = $(this).closest('.tab'),
            index = $(this).closest('li').index();
        tab.find('li').siblings('li').removeClass('active');
        $(this).closest('li').addClass('active');
        tab.find('.tab-area').find('div.tab-item').not('div.tab-item:eq(' + index + ')').fadeOut(500);
        tab.find('.tab-area').find('div.tab-item:eq(' + index + ')').fadeIn(500);
        g.preventDefault();
    });
    $('.search-tab ul.tab-menu li').on('click', function (k) {
        var search_tab = $(this).closest('.search-tab'),
            searchIndex = $(this).closest('li').index();
        search_tab.find('li').siblings('li').removeClass('active');
        $(this).closest('li').addClass('active');
        search_tab.find('.tab-area').find('div.tab-item').not('div.tab-item:eq(' + searchIndex + ')').hide(10);
        search_tab.find('.tab-area').find('div.tab-item:eq(' + searchIndex + ')').show(10);
        k.preventDefault();
    });
    $('.tabTwo ul.tab-menu li').on('click', function (g) {
        var tabTwo = $(this).closest('.tabTwo'),
            index = $(this).closest('li').index();
        tabTwo.find('li').siblings('li').removeClass('active');
        $(this).closest('li').addClass('active');
        tabTwo.find('.tab-area').find('div.tab-item').not('div.tab-item:eq(' + index + ')').fadeOut(10);
        tabTwo.find('.tab-area').find('div.tab-item:eq(' + index + ')').fadeIn(10);
        g.preventDefault();
    });
})(jQuery);

