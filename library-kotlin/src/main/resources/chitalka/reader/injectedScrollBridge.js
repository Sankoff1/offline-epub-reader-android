(function () {
  try {
    // Outbound: Kotlin -> WebView. Меняет CSS-переменные темы без перезагрузки страницы.
    // Контракт payload фиксирован в ReaderBridgeMessages.encodeReaderBridgeOutboundMessage.
    window.__chitalkaApplyTheme = function (payload) {
      try {
        var data = (typeof payload === 'string') ? JSON.parse(payload) : payload;
        if (!data) return;
        var root = document.documentElement;
        var body = document.body;
        if (data.background) {
          root.style.setProperty('--chitalka-bg', data.background);
          if (body) body.style.backgroundColor = data.background;
        }
        if (data.foreground) {
          root.style.setProperty('--chitalka-fg', data.foreground);
          if (body) body.style.color = data.foreground;
        }
        root.classList.toggle('chitalka-dark', !!data.isDark);
        root.style.colorScheme = data.isDark ? 'dark' : 'light';
      } catch (e) {
        if (window.console) console.warn('__chitalkaApplyTheme failed', e);
      }
    };

    function postY() {
      var y = window.pageYOffset || document.documentElement.scrollTop || document.body.scrollTop || 0;
      var h = document.documentElement.scrollHeight || document.body.scrollHeight || 0;
      var vh = window.innerHeight || document.documentElement.clientHeight || 0;
      var yMax = Math.max(0, h - vh);
      if (window.ReactNativeWebView) {
        window.ReactNativeWebView.postMessage(JSON.stringify({ t: 'scroll', y: y, yMax: yMax }));
      }
    }
    var timer;
    function onScroll() {
      clearTimeout(timer);
      timer = setTimeout(postY, 200);
    }
    window.addEventListener('scroll', onScroll, { passive: true });

    var startX = 0, startY = 0, startT = 0;
    var TAP_MAX_DELTA = 10;
    var TAP_MAX_TIME = 400;
    var SWIPE_MIN_DX = 30;
    var SWIPE_MAX_DY = 80;
    var SWIPE_MAX_TIME = 1000;
    var SWIPE_DX_DY_RATIO = 1.2;
    var TAP_PREV_ZONE_END = 1 / 3;
    var TAP_NEXT_ZONE_START = 2 / 3;
    var INTERACTIVE_SELECTOR = 'a,button,input,select,textarea,label,[role="link"],[role="button"]';

    function postPage(dir) {
      if (window.ReactNativeWebView) {
        window.ReactNativeWebView.postMessage(JSON.stringify({ t: 'page', dir: dir }));
      }
    }
    function onTouchStart(e) {
      if (!e.changedTouches || e.changedTouches.length !== 1) {
        startT = 0;
        return;
      }
      var p = e.changedTouches[0];
      startX = p.clientX; startY = p.clientY; startT = Date.now();
    }
    function onTouchEnd(e) {
      if (!startT || !e.changedTouches || e.changedTouches.length !== 1) {
        startT = 0;
        return;
      }
      var p = e.changedTouches[0];
      var dx = p.clientX - startX;
      var dy = p.clientY - startY;
      var dt = Date.now() - startT;
      startT = 0;
      if (window.getSelection && String(window.getSelection()).length > 0) return;
      var adx = Math.abs(dx), ady = Math.abs(dy);
      var horizontalDominant = adx > ady * SWIPE_DX_DY_RATIO;
      if (dt <= SWIPE_MAX_TIME && adx >= SWIPE_MIN_DX && (ady <= SWIPE_MAX_DY || horizontalDominant)) {
        postPage(dx < 0 ? 'next' : 'prev');
        return;
      }
      if (dt <= TAP_MAX_TIME && Math.abs(dx) <= TAP_MAX_DELTA && Math.abs(dy) <= TAP_MAX_DELTA) {
        if (e.target && e.target.closest && e.target.closest(INTERACTIVE_SELECTOR)) return;
        var w = window.innerWidth || document.documentElement.clientWidth || 1;
        var frac = p.clientX / w;
        if (frac <= TAP_PREV_ZONE_END) postPage('prev');
        else if (frac >= TAP_NEXT_ZONE_START) postPage('next');
      }
    }
    window.addEventListener('touchstart', onTouchStart, { passive: true });
    window.addEventListener('touchend', onTouchEnd, { passive: true });
    window.addEventListener('touchcancel', function () { startT = 0; }, { passive: true });
  } catch (e) {
    // Глушим всё: страница из EPUB должна оставаться рабочей, даже если мост поднять не удалось.
  }
  true;
})();
