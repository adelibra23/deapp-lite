(function () {
  if (window.__DEAPP_NATIVE_V14__) {
    if (window.__DEAPP_NATIVE_V14__.refresh) window.__DEAPP_NATIVE_V14__.refresh();
    return true;
  }

  const API = window.DeappNative || null;
  const root = document.documentElement;
  const path = (location.pathname || '').toLowerCase();
  const isProfilePage = /\/profile\.php$/.test(path);

  root.classList.add('deapp-native-shell', 'deapp-native-v14');
  if (isProfilePage) root.classList.add('deapp-native-profile');

  const style = document.createElement('style');
  style.id = 'deapp-native-v14-style';
  style.textContent = `
    :root{--topbar-h:0px!important;--bnav-h:0px!important}
    .topbar,.bottom-nav{display:none!important}
    html,body{scrollbar-width:none!important;background:var(--surface)!important;overscroll-behavior-y:none!important}
    html::-webkit-scrollbar,body::-webkit-scrollbar,*::-webkit-scrollbar{display:none!important;width:0!important;height:0!important;background:transparent!important}
    body{padding-top:0!important;padding-bottom:0!important;-webkit-tap-highlight-color:transparent!important}
    .layout,.layout-guest{padding-top:0!important;padding-bottom:14px!important}
    .page-home .composer-trigger{display:none!important}

    a,button,[role="button"],.btn,.icon-btn,.tab,.dropdown-item,.bnav,.nav-link,.post-card[data-url],.post-card[onclick],.qa-card[data-url],.qa-card[onclick]{
      -webkit-tap-highlight-color:transparent!important;
      touch-action:manipulation;
    }
    a,button,[role="button"],.btn,.icon-btn,.tab,.dropdown-item,.nav-link{
      -webkit-touch-callout:none;
      user-select:none;
    }
    .deapp-native-pressed{
      opacity:.68!important;
      transform:scale(.985)!important;
      transition:transform .065s ease,opacity .065s ease!important;
    }

    .tabs,.story-tabs,.nx-studio-nav,[role="tablist"],.feed-tabs,.reels-tabs,.community-tabs{
      overflow-x:auto!important;
      overflow-y:hidden!important;
      flex-wrap:nowrap!important;
      scrollbar-width:none!important;
      -webkit-overflow-scrolling:touch!important;
      overscroll-behavior-x:contain!important;
      touch-action:pan-x!important;
      scroll-snap-type:x proximity;
    }
    .tabs::-webkit-scrollbar,.story-tabs::-webkit-scrollbar,.nx-studio-nav::-webkit-scrollbar,[role="tablist"]::-webkit-scrollbar,.feed-tabs::-webkit-scrollbar,.reels-tabs::-webkit-scrollbar,.community-tabs::-webkit-scrollbar{display:none!important}
    .tabs>.tab,.story-tabs>*[role="tab"],.story-tabs>button,.nx-studio-nav>*,[role="tablist"]>*{flex:0 0 auto!important;scroll-snap-align:start}
    .deapp-native-dragging{cursor:grabbing!important;scroll-behavior:auto!important;user-select:none!important}

    .page-home .feed-tabs{border-left:0!important;border-right:0!important;border-top:0!important;border-radius:0!important;box-shadow:none!important;background:var(--surface)!important}
    .page-home .feed{border:0!important;border-radius:0!important;box-shadow:none!important;background:var(--surface)!important}
    .page-home .feed>.post-card,.page-home .feed>.qa-card{border-radius:0!important;box-shadow:none!important}
    .post-card{box-shadow:none!important}
    .post-card .post-actions{border-top-color:transparent!important}

    .modal-overlay{align-items:flex-end!important;justify-content:center!important;padding:0!important;overflow:hidden!important}
    .modal-overlay .modal-box{
      width:100%!important;max-width:100%!important;margin:0!important;
      border-left:0!important;border-right:0!important;border-bottom:0!important;
      border-radius:26px 26px 0 0!important;max-height:94dvh!important;overflow:auto!important;
      padding-bottom:max(20px,env(safe-area-inset-bottom))!important;
      box-shadow:0 -14px 42px rgba(0,0,0,.18)!important;
      animation:deappNativeSheetIn .22s cubic-bezier(.2,.8,.2,1)!important;
      will-change:transform;
    }
    .modal-overlay .modal-box:before,.deapp-cookie-modal .cookie-modal-card:before,dialog.c-modal[open] .c-modal-box:before,
    .post-card .menu-wrap.open>.dropdown:before{
      content:"";display:block;width:38px;height:4px;border-radius:99px;
      background:color-mix(in srgb,var(--text-muted) 44%,transparent);margin:8px auto 11px;flex:none;
    }
    .modal-overlay.photo-studio-modal .modal-box,.modal-overlay.reel-create-modal .modal-box{max-height:97dvh!important}
    .deapp-cookie-modal{align-items:flex-end!important;padding:0!important}
    .deapp-cookie-modal .cookie-modal-card{width:100%!important;max-width:100%!important;margin:0!important;border-radius:26px 26px 0 0!important;max-height:94dvh!important;overflow:auto!important;will-change:transform}
    dialog.c-modal[open]{position:fixed!important;inset:auto 0 0 0!important;width:100%!important;max-width:none!important;margin:0!important;border-radius:26px 26px 0 0!important;max-height:94dvh!important}
    dialog.c-modal .c-modal-box{border-radius:26px 26px 0 0!important;will-change:transform}
    @keyframes deappNativeSheetIn{from{transform:translateY(100%);opacity:.72}to{transform:translateY(0);opacity:1}}

    .deapp-post-sheet-backdrop{position:fixed;inset:0;background:rgba(0,0,0,.46);z-index:2890;animation:deappFadeIn .16s ease}
    @keyframes deappFadeIn{from{opacity:0}to{opacity:1}}
    .post-card .menu-wrap.open>.dropdown,
    .post-card .menu-wrap.open>.dropdown.nx-floating{
      position:fixed!important;left:0!important;right:0!important;top:auto!important;bottom:0!important;
      width:100%!important;min-width:0!important;max-width:none!important;max-height:80dvh!important;
      margin:0!important;padding:8px 14px max(18px,env(safe-area-inset-bottom))!important;
      border-left:0!important;border-right:0!important;border-bottom:0!important;
      border-radius:26px 26px 0 0!important;background:var(--surface)!important;
      box-shadow:0 -14px 42px rgba(0,0,0,.2)!important;
      opacity:1!important;visibility:visible!important;transform:none;transform-origin:bottom center!important;
      z-index:3000!important;overflow-x:hidden!important;overflow-y:auto!important;overscroll-behavior:contain!important;
      animation:deappNativeSheetIn .2s cubic-bezier(.2,.8,.2,1)!important;
      will-change:transform!important;
    }
    .post-card .menu-wrap.open>.dropdown .dropdown-item{min-height:48px!important;border-radius:14px!important;padding:12px 13px!important;font-size:14.5px!important}
    .post-card .menu-wrap.open>.dropdown .dropdown-sep{margin:7px 2px!important}

    .deapp-native-profile .profile-topbar{display:none!important}
    .deapp-native-profile .layout{display:block!important;width:100%!important;max-width:none!important;margin:0!important;padding-left:0!important;padding-right:0!important;gap:0!important}
    .deapp-native-profile .main-col{width:100%!important;max-width:none!important;margin:0!important;padding:0!important}
    .deapp-native-profile .side-col,.deapp-native-profile .siderail{display:none!important}
    .deapp-native-profile .profile-card{margin:0!important;border:0!important;border-radius:0!important;box-shadow:none!important;overflow:hidden!important}
    .deapp-native-profile .profile-cover{height:172px!important;border-radius:0!important}
    .deapp-native-profile .profile-body{padding:0 16px 18px!important}
    .deapp-native-profile .profile-avatar-wrap{margin-top:-47px!important;margin-bottom:10px!important}
    .deapp-native-profile .profile-avatar{width:96px!important;height:96px!important;border-width:4px!important}
    .deapp-native-profile .profile-name{font-size:22px!important;line-height:1.22!important}
    .deapp-native-profile .profile-bio{max-width:none!important;font-size:14.5px!important;line-height:1.55!important}
    .deapp-native-profile .profile-stats{gap:4px!important;justify-content:space-between!important;flex-wrap:nowrap!important;overflow-x:auto!important;scrollbar-width:none!important}
    .deapp-native-profile .profile-stats::-webkit-scrollbar{display:none!important}
    .deapp-native-profile .profile-stats>a{min-width:70px!important;flex:1 0 auto!important;border-radius:12px!important}
    .deapp-native-profile .profile-actions{width:100%!important;display:grid!important;grid-template-columns:repeat(2,minmax(0,1fr))!important;gap:8px!important}
    .deapp-native-profile .profile-actions>.btn,.deapp-native-profile .profile-actions>.menu-wrap{width:100%!important;min-width:0!important}
    .deapp-native-profile .profile-actions .btn{width:100%!important;justify-content:center!important}
    .deapp-native-profile .tabs.sticky-tabs{top:0!important;margin:0!important;border-left:0!important;border-right:0!important;border-radius:0!important;background:var(--surface)!important}
    .deapp-native-profile .card:not(.profile-card){margin-left:0!important;margin-right:0!important;border-left:0!important;border-right:0!important;border-radius:0!important;box-shadow:none!important}
    .deapp-native-profile .post-card{border-left:0!important;border-right:0!important;border-radius:0!important}
    .deapp-native-profile .profile-reels-grid{gap:2px!important}
    .deapp-native-profile .profile-reel-tile{border-radius:2px!important}
  `;
  document.head.appendChild(style);

  function nativeTap() {
    try { if (API && API.tap) API.tap(); } catch (_) {}
  }

  function actionable(node) {
    return node && node.closest ? node.closest('a,button,[role="button"],.btn,.icon-btn,.tab,.dropdown-item,.nav-link,.post-card[data-url],.post-card[onclick],.qa-card[data-url],.qa-card[onclick],.post-action,.comment-action') : null;
  }

  let pressed = null;
  document.addEventListener('pointerdown', function (e) {
    const el = actionable(e.target);
    if (!el || el.matches('input,textarea,select,[contenteditable="true"]')) return;
    pressed = el;
    el.classList.add('deapp-native-pressed');
  }, true);
  ['pointerup','pointercancel','pointerleave'].forEach(function (name) {
    document.addEventListener(name, function () {
      if (pressed) pressed.classList.remove('deapp-native-pressed');
      pressed = null;
    }, true);
  });
  document.addEventListener('click', function (e) {
    if (actionable(e.target)) nativeTap();
  }, true);
  document.addEventListener('contextmenu', function (e) {
    if (actionable(e.target)) e.preventDefault();
  }, true);

  function wireHorizontalTabs(el) {
    if (!el || el.dataset.deappNativeDragTabs === '1') return;
    el.dataset.deappNativeDragTabs = '1';
    let down = false, startX = 0, startLeft = 0, moved = false, pointerId = null;
    el.addEventListener('pointerdown', function (e) {
      if (e.pointerType !== 'mouse') return;
      down = true; moved = false; pointerId = e.pointerId;
      startX = e.clientX; startLeft = el.scrollLeft;
      try { el.setPointerCapture(pointerId); } catch (_) {}
    });
    el.addEventListener('pointermove', function (e) {
      if (!down || e.pointerType !== 'mouse') return;
      const dx = e.clientX - startX;
      if (Math.abs(dx) > 4) {
        moved = true;
        el.classList.add('deapp-native-dragging');
        el.scrollLeft = startLeft - dx;
        e.preventDefault();
      }
    });
    function finish(e) {
      if (!down) return;
      down = false;
      el.classList.remove('deapp-native-dragging');
      try { if (pointerId !== null) el.releasePointerCapture(pointerId); } catch (_) {}
      pointerId = null;
      if (moved) {
        el.dataset.deappNativeSuppressClick = '1';
        setTimeout(function(){ delete el.dataset.deappNativeSuppressClick; }, 80);
      }
    }
    el.addEventListener('pointerup', finish);
    el.addEventListener('pointercancel', finish);
    el.addEventListener('click', function (e) {
      if (el.dataset.deappNativeSuppressClick === '1') {
        e.preventDefault(); e.stopPropagation();
      }
    }, true);
  }

  function closePostMenu(box) {
    const wrap = box ? box.closest('.menu-wrap') : null;
    if (wrap) wrap.classList.remove('open');
    try { if (box && box.matches(':popover-open') && box.hidePopover) box.hidePopover(); } catch (_) {}
    removePostBackdrop();
  }

  function removePostBackdrop() {
    document.querySelectorAll('.deapp-post-sheet-backdrop').forEach(function (x) { x.remove(); });
  }

  function ensurePostBackdrop() {
    const open = document.querySelector('.post-card .menu-wrap.open>.dropdown');
    if (!open) { removePostBackdrop(); return; }
    if (document.querySelector('.deapp-post-sheet-backdrop')) return;
    const b = document.createElement('div');
    b.className = 'deapp-post-sheet-backdrop';
    b.addEventListener('click', function () { closePostMenu(open); });
    document.body.appendChild(b);
  }

  function closeSheet(box) {
    if (!box) return;
    if (box.closest('.post-card .menu-wrap')) { closePostMenu(box); return; }
    const ov = box.closest('.modal-overlay');
    const dlg = box.closest('dialog');
    const btn = box.querySelector('.modal-close,[data-close-modal],[aria-label="Tutup"]');
    if (btn) { btn.click(); return; }
    if (dlg && dlg.close) { dlg.close(); return; }
    if (ov) ov.click();
  }

  function wireSheet(box) {
    if (!box || box.dataset.deappSwipeSheet === '1') return;
    box.dataset.deappSwipeSheet = '1';
    let sy = 0, last = 0, drag = false;
    box.addEventListener('touchstart', function (e) {
      if (e.touches.length !== 1) return;
      sy = e.touches[0].clientY; last = sy; drag = false;
      box.style.transition = 'none';
    }, {passive:true});
    box.addEventListener('touchmove', function (e) {
      if (!e.touches.length) return;
      const y = e.touches[0].clientY, dy = y - sy;
      last = y;
      if (dy > 5 && box.scrollTop <= 0) {
        drag = true;
        box.style.transform = 'translateY(' + Math.min(dy, innerHeight * .78) + 'px)';
        if (dy > 10) e.preventDefault();
      }
    }, {passive:false});
    box.addEventListener('touchend', function () {
      if (!drag) { box.style.transition = ''; return; }
      const dy = last - sy;
      box.style.transition = 'transform .2s cubic-bezier(.2,.8,.2,1)';
      if (dy > 90) {
        box.style.transform = 'translateY(110%)';
        setTimeout(function () {
          closeSheet(box);
          box.style.transform = '';
          box.style.transition = '';
        }, 155);
      } else {
        box.style.transform = 'translateY(0)';
        setTimeout(function () {
          box.style.transform = '';
          box.style.transition = '';
        }, 210);
      }
      drag = false;
    }, {passive:true});
  }

  function syncSession() {
    try {
      const d = window.DEAPP || {};
      const logged = !!d.LOGGED_IN;
      const me = d.ME || null;
      let avatar = me && me.avatar ? String(me.avatar) : '';
      let profile = '';
      const profileLink = document.querySelector('.bottom-nav a.bnav:last-child,#user-dropdown .dropdown-user');
      if (profileLink && profileLink.href) profile = profileLink.href;
      if (!profile && me && me.username && d.BASE_URL) profile = String(d.BASE_URL).replace(/\/$/,'') + '/profile.php?u=' + encodeURIComponent(me.username);
      if (API && API.syncSessionState) API.syncSessionState(logged, avatar, profile, location.href);
    } catch (_) {}
  }

  function scan() {
    document.querySelectorAll('.modal-overlay .modal-box,.deapp-cookie-modal .cookie-modal-card,dialog.c-modal[open] .c-modal-box,.post-card .menu-wrap.open>.dropdown').forEach(wireSheet);
    document.querySelectorAll('.tabs,.story-tabs,.nx-studio-nav,[role="tablist"],.feed-tabs,.reels-tabs,.community-tabs').forEach(wireHorizontalTabs);
    ensurePostBackdrop();
    syncSession();
  }

  scan();
  const observer = new MutationObserver(scan);
  observer.observe(document.body, {subtree:true, childList:true, attributes:true, attributeFilter:['class','open','hidden','aria-expanded']});

  window.__DEAPP_NATIVE_V14__ = { refresh: scan };
  return true;
})();
