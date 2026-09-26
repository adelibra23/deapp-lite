(function () {
  if (window.__DEAPP_NATIVE_V19__) {
    if (window.__DEAPP_NATIVE_V19__.refresh) window.__DEAPP_NATIVE_V19__.refresh();
    return true;
  }

  const API = window.DeappNative || null;
  let lastWebSheetOpen = null;
  let currentWebSheetOpen = false;
  let externalNativeSheetOpen = false;
  let backgroundScrollLocked = false;
  let lockedScrollY = 0;
  let savedBodyTop = '';
  let composerSubmitting = false;
  let lastSessionKey = '';
  let lastChromeKey = '';
  let lastComposerState = null;
  let lastThemeDark = null;
  let scrollUiTimer = 0;
  let scrollUiActive = false;
  const root = document.documentElement;
  const path = (location.pathname || '').toLowerCase();
  const isProfilePage = /\/profile\.php$/.test(path);
  const isSettingsPage = /\/settings\.php$/.test(path);
  const isPostDetailPage = /\/post\.php$/.test(path);
  const isReelsPage = /\/reels\.php$/.test(path);
  const isMessagesPage = /\/messages\.php$/.test(path);
  const isNotificationsPage = /\/notifications\.php$/.test(path);
  const isLivePage = /\/live\.php$/.test(path);
  const isAiPage = /\/ai\.php$/.test(path);
  const isShopPage = /\/shop\.php$/.test(path);
  const isExplorePage = /\/explore\.php$/.test(path);
  const isSettingsPageFamily = /\/(settings|security|security-password|security-email|security-wallet-pin|security-2fa|security-sessions|help|privacy|policy|cookies|profile-edit)\.php$/.test(path);
  const isAuthPage = /\/(login|register)\.php$/.test(path);

  root.classList.add('deapp-native-shell', 'deapp-native-v19');
  if (isProfilePage) root.classList.add('deapp-native-profile');
  if (isPostDetailPage) root.classList.add('deapp-native-post-detail');
  if (isReelsPage) root.classList.add('deapp-native-reels');
  if (isMessagesPage) root.classList.add('deapp-native-messages');
  if (isNotificationsPage) root.classList.add('deapp-native-notifications');
  if (isLivePage) root.classList.add('deapp-native-live');
  if (isAiPage) root.classList.add('deapp-native-ai');
  if (isShopPage) root.classList.add('deapp-native-shop');
  if (isExplorePage) root.classList.add('deapp-native-explore');
  if (isSettingsPageFamily) root.classList.add('deapp-native-settings-family');

  const style = document.createElement('style');
  style.id = 'deapp-native-v19-style';
  style.textContent = `
    :root{--topbar-h:0px!important;--bnav-h:0px!important}
    .topbar,.bottom-nav{display:none!important}
    html,body{scrollbar-width:none!important;background:var(--surface)!important;overscroll-behavior-y:auto!important;touch-action:auto!important}
    html::-webkit-scrollbar,body::-webkit-scrollbar,*::-webkit-scrollbar{display:none!important;width:0!important;height:0!important;background:transparent!important}
    body{padding-top:0!important;padding-bottom:0!important;-webkit-tap-highlight-color:transparent!important}
    html:not(.deapp-native-sheet-lock),body:not(.deapp-native-sheet-lock-body){overflow-y:auto!important}
    html.deapp-native-sheet-lock{overflow:hidden!important;overscroll-behavior:none!important}
    body.deapp-native-sheet-lock-body{overflow:hidden!important;overscroll-behavior:none!important}
    .modal-overlay:not(#composer-modal) .modal-box,.deapp-cookie-modal .cookie-modal-card,dialog.c-modal[open] .c-modal-box,.post-card .menu-wrap.open>.dropdown,.deapp-native-profile-options-sheet{touch-action:pan-y!important;overscroll-behavior:contain!important}
    .layout,.layout-guest{padding-top:0!important;padding-bottom:14px!important}
    .page-home .composer-trigger{display:none!important}

    a,button,[role="button"],.btn,.icon-btn,.tab,.dropdown-item,.bnav,.nav-link,.post-card[data-url],.post-card[onclick],.qa-card[data-url],.qa-card[onclick]{
      -webkit-tap-highlight-color:transparent!important;
      touch-action:auto;
    }
    a,button,[role="button"],.btn,.icon-btn,.tab,.dropdown-item,.nav-link{
      -webkit-touch-callout:none;
      user-select:none;
    }
    /* v1.9.3 — interaksi dibuat konservatif: patch tidak boleh memblokir gesture/klik asli halaman. */
    a:active,button:active,[role="button"]:active,.btn:active,.icon-btn:active,.tab:active,.dropdown-item:active,.nav-link:active,.post-action:active,.comment-action:active{
      opacity:1!important;transform:none!important;filter:none!important;-webkit-filter:none!important;box-shadow:none!important;
    }
    *:focus:not(:focus-visible){outline:none!important;}

    .tabs,.story-tabs,.nx-studio-nav,[role="tablist"],.feed-tabs,.reels-tabs,.community-tabs{
      overflow-x:auto!important;
      overflow-y:hidden!important;
      flex-wrap:nowrap!important;
      scrollbar-width:none!important;
      -webkit-overflow-scrolling:touch!important;
      overscroll-behavior-x:contain!important;
      touch-action:pan-x pan-y!important;
      scroll-snap-type:x proximity;
    }
    .tabs::-webkit-scrollbar,.story-tabs::-webkit-scrollbar,.nx-studio-nav::-webkit-scrollbar,[role="tablist"]::-webkit-scrollbar,.feed-tabs::-webkit-scrollbar,.reels-tabs::-webkit-scrollbar,.community-tabs::-webkit-scrollbar{display:none!important}
    .tabs>.tab,.story-tabs>*[role="tab"],.story-tabs>button,.nx-studio-nav>*,[role="tablist"]>*{flex:0 0 auto!important;scroll-snap-align:start}
    .deapp-native-dragging{cursor:grabbing!important;scroll-behavior:auto!important;user-select:none!important}

    /* v1.9.10 — Beranda Lite lebih fokus: ringkasan harian disembunyikan, Story langsung tampil. */
    .page-home .daily-strip{display:none!important}
    .page-home .story-tray{margin-top:2px!important}

    .page-home .feed-tabs{
      border:0!important;border-radius:0!important;box-shadow:none!important;background:var(--surface)!important;
      gap:7px!important;padding:8px 10px 9px!important;min-height:58px!important;align-items:center!important;
    }
    .page-home .feed-tabs>.tab{
      min-height:40px!important;padding:9px 13px!important;border-radius:15px!important;border:1px solid transparent!important;
      font-size:13.5px!important;font-weight:780!important;gap:6px!important;background:var(--surface-2)!important;
      transition:background .16s ease,border-color .16s ease,color .16s ease!important;
    }
    .page-home .feed-tabs>.tab svg{width:18px!important;height:18px!important;stroke-width:2.05!important}
    .page-home .feed-tabs>.tab:nth-child(1){color:#536DFE!important;background:color-mix(in srgb,#536DFE 10%,var(--surface))!important}
    .page-home .feed-tabs>.tab:nth-child(2){color:#00A86B!important;background:color-mix(in srgb,#00A86B 10%,var(--surface))!important}
    .page-home .feed-tabs>.tab:nth-child(3){color:#8B5CF6!important;background:color-mix(in srgb,#8B5CF6 10%,var(--surface))!important}
    .page-home .feed-tabs>.tab:nth-child(4){color:#FF7A00!important;background:color-mix(in srgb,#FF7A00 11%,var(--surface))!important}
    .page-home .feed-tabs>.tab:nth-child(5){color:#E54B8C!important;background:color-mix(in srgb,#E54B8C 10%,var(--surface))!important}
    .page-home .feed-tabs>.tab:nth-child(6){color:#00A6C8!important;background:color-mix(in srgb,#00A6C8 10%,var(--surface))!important}
    .page-home .feed-tabs>.tab.active{
      border-color:currentColor!important;box-shadow:0 3px 12px color-mix(in srgb,currentColor 13%,transparent)!important;
      font-weight:850!important;
    }
    .page-home .feed-tabs>.tab .pill-count{transform:translateY(-1px)!important}
    .page-home .feed{border:0!important;border-radius:0!important;box-shadow:none!important;background:var(--surface)!important}
    .page-home .feed>.post-card,.page-home .feed>.qa-card{border-radius:0!important;box-shadow:none!important}

    /* v1.8 — feed bergaya thread: avatar rail, konten ringan, action icon-first. */
    .post-card:not(.post-embedded){
      position:relative!important;margin:0!important;padding:0 0 8px!important;overflow:visible!important;
      border:0!important;border-bottom:1px solid color-mix(in srgb,var(--border) 78%,transparent)!important;
      border-radius:0!important;box-shadow:none!important;background:var(--surface)!important;animation:none!important;
    }
    .post-card:not(.post-embedded):hover{box-shadow:none!important;background:var(--surface)!important}
    .post-card:not(.post-embedded):before{
      content:"";position:absolute;left:34px;top:58px;bottom:58px;width:2px;border-radius:99px;
      background:color-mix(in srgb,var(--border) 82%,transparent);pointer-events:none;z-index:0;
    }
    .post-card:not(.post-embedded) .post-head{padding:13px 13px 5px 14px!important;gap:10px!important;position:relative;z-index:1}
    .post-card:not(.post-embedded) .post-avatar{width:40px!important;height:40px!important}
    .post-card:not(.post-embedded) .post-head-meta{padding-top:1px!important;min-width:0!important}
    .post-card:not(.post-embedded) .post-author{gap:4px!important;flex-wrap:nowrap!important;min-width:0!important}
    .post-card:not(.post-embedded) .post-name{font-size:14.5px!important;font-weight:800!important;overflow:hidden!important;text-overflow:ellipsis!important;white-space:nowrap!important}
    .post-card:not(.post-embedded) .post-sub{font-size:11.5px!important;line-height:1.35!important;gap:4px!important;margin-top:2px!important;color:var(--text-muted)!important}
    .post-card:not(.post-embedded) .menu-trigger{width:38px!important;height:38px!important;padding:8px!important;border-radius:50%!important;flex:none!important}
    .post-card:not(.post-embedded) .post-content{padding:1px 14px 8px 64px!important;font-size:15px!important;line-height:1.48!important;color:var(--text)!important}
    .post-card:not(.post-embedded) .post-bg{margin:4px 14px 10px 64px!important;border-radius:12px!important;overflow:hidden!important;min-height:210px!important}
    .post-card:not(.post-embedded)>.media-grid,.post-card:not(.post-embedded) .media-grid{
      margin:4px 14px 10px 64px!important;border-radius:12px!important;overflow:hidden!important;background:transparent!important;gap:2px!important;
    }
    .post-card:not(.post-embedded) .media-cell{border-radius:0!important}
    .post-card:not(.post-embedded) .poll,
    .post-card:not(.post-embedded) .shared-wrap,
    .post-card:not(.post-embedded) .link-card,
    .post-card:not(.post-embedded) .pred-card,
    .post-card:not(.post-embedded) .post-intent-card,
    .post-card:not(.post-embedded) .branch-origin,
    .post-card:not(.post-embedded) .source-passport,
    .post-card:not(.post-embedded) .memory-note-banner,
    .post-card:not(.post-embedded) .crowd-shield-banner,
    .post-card:not(.post-embedded) .solved-conversation{margin-left:64px!important;margin-right:14px!important}
    .post-card:not(.post-embedded) .shared-wrap{margin-top:4px!important;margin-bottom:10px!important}
    .post-card:not(.post-embedded) .post-embedded{border:1px solid var(--border)!important;border-radius:13px!important;overflow:hidden!important;background:var(--surface-2)!important}
    .post-card:not(.post-embedded) .post-stats{
      padding:3px 14px 2px 64px!important;min-height:24px!important;font-size:12px!important;justify-content:flex-start!important;gap:10px!important;
    }
    .post-card:not(.post-embedded) .post-stats .stat-right{margin-left:0!important;gap:10px!important;flex-wrap:wrap!important}
    .post-card:not(.post-embedded) .stat-reactions,.post-card:not(.post-embedded) .stat-link{font-size:12px!important}
    .post-card:not(.post-embedded) .post-stats .deapp-stat-relocated{display:none!important}
    .post-card:not(.post-embedded) .post-stats.deapp-stats-empty{display:none!important}
    .post-card:not(.post-embedded) .post-actions{
      justify-content:flex-start!important;gap:2px!important;padding:3px 10px 3px 56px!important;border-top:0!important;min-height:44px!important;overflow:visible!important;
    }
    .post-card:not(.post-embedded) .post-actions .react-wrap{flex:0 0 auto!important}
    .post-card:not(.post-embedded) .act-btn{
      flex:0 0 42px!important;width:42px!important;min-width:42px!important;height:42px!important;min-height:42px!important;
      padding:9px!important;border-radius:50%!important;gap:0!important;color:var(--text)!important;background:transparent!important;
    }
    .post-card:not(.post-embedded) .act-btn.deapp-counted-action{
      flex:0 0 auto!important;width:auto!important;min-width:48px!important;padding:8px 9px!important;border-radius:22px!important;gap:5px!important;
    }
    .post-card:not(.post-embedded) .deapp-action-count{
      display:inline-flex!important;align-items:center!important;justify-content:flex-start!important;min-width:8px!important;
      font-size:12.5px!important;line-height:1!important;font-weight:750!important;color:var(--text-muted)!important;font-variant-numeric:tabular-nums!important;
    }
    .post-card:not(.post-embedded) .js-react-btn.reacted .deapp-action-count{color:currentColor!important}
    .post-card:not(.post-embedded) .act-btn:hover{background:transparent!important}
    .post-card:not(.post-embedded) .act-btn .act-label{display:none!important}
    .post-card:not(.post-embedded) .act-btn svg{width:21px!important;height:21px!important}
    .post-card:not(.post-embedded) .act-emoji{font-size:20px!important}
    .post-card:not(.post-embedded) .js-bookmark{margin-left:0!important}
    .post-card:not(.post-embedded) .post-gifts{margin-left:64px!important;margin-right:14px!important}
    .post-card:not(.post-embedded) .comments-section{margin-left:51px!important;padding:8px 14px 12px 13px!important;border-top:0!important}
    .post-card:not(.post-embedded) .react-picker{left:-4px!important;bottom:calc(100% + 4px)!important}
    .deapp-native-profile .post-card:not(.post-embedded){margin:0!important}

    @media (max-width:380px){
      .post-card:not(.post-embedded):before{left:31px}
      .post-card:not(.post-embedded) .post-head{padding-left:11px!important;padding-right:10px!important}
      .post-card:not(.post-embedded) .post-avatar{width:39px!important;height:39px!important}
      .post-card:not(.post-embedded) .post-content{padding-left:59px!important;padding-right:11px!important}
      .post-card:not(.post-embedded)>.media-grid,.post-card:not(.post-embedded) .media-grid,.post-card:not(.post-embedded) .post-bg{margin-left:59px!important;margin-right:11px!important}
      .post-card:not(.post-embedded) .post-stats{padding-left:59px!important;padding-right:11px!important}
      .post-card:not(.post-embedded) .post-actions{padding-left:51px!important}
    }

    .modal-overlay:not(#composer-modal){align-items:flex-end!important;justify-content:center!important;padding:0!important;overflow:hidden!important}
    .modal-overlay:not(#composer-modal) .modal-box{
      width:100%!important;max-width:100%!important;margin:0!important;
      border-left:0!important;border-right:0!important;border-bottom:0!important;
      border-radius:26px 26px 0 0!important;max-height:94dvh!important;overflow:auto!important;
      padding-bottom:max(20px,env(safe-area-inset-bottom))!important;
      box-shadow:0 -14px 42px rgba(0,0,0,.18)!important;
      animation:deappNativeSheetIn .22s cubic-bezier(.2,.8,.2,1)!important;
      will-change:transform;
    }
    .modal-overlay:not(#composer-modal) .modal-box:before,.deapp-cookie-modal .cookie-modal-card:before,dialog.c-modal[open] .c-modal-box:before,
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
    .deapp-post-sheet-backdrop[hidden],.deapp-native-profile-options-backdrop[hidden],.modal-overlay[hidden]{pointer-events:none!important;display:none!important}
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

    /* v1.5 — composer menjadi halaman penuh di dalam shell Android. Header terbit ada di native toolbar. */
    #composer-modal{position:fixed!important;inset:0!important;display:none;align-items:stretch!important;justify-content:stretch!important;padding:0!important;background:var(--surface)!important;z-index:4200!important;overflow:hidden!important}
    #composer-modal.open{display:flex!important}
    #composer-modal .modal-box{width:100%!important;max-width:none!important;height:100%!important;max-height:none!important;margin:0!important;border:0!important;border-radius:0!important;box-shadow:none!important;background:var(--surface)!important;overflow-y:auto!important;padding:12px 16px max(22px,env(safe-area-inset-bottom))!important;animation:deappComposerIn .18s cubic-bezier(.2,.8,.2,1)!important}
    #composer-modal .modal-box:before,#composer-modal .modal-header,#composer-modal #composer-submit{display:none!important}
    #composer-modal .composer{max-width:680px!important;margin:0 auto!important;padding-top:3px!important}
    #composer-modal .composer-text-wrap textarea{min-height:150px!important;font-size:17px!important;line-height:1.5!important}
    #composer-modal .composer-tools{position:sticky!important;bottom:0!important;background:var(--surface)!important;padding:10px 0 max(4px,env(safe-area-inset-bottom))!important;z-index:3!important}
    @keyframes deappComposerIn{from{opacity:.35;transform:translateY(12px)}to{opacity:1;transform:none}}

    .deapp-native-profile-options-backdrop{position:fixed;inset:0;background:rgba(0,0,0,.48);z-index:4980;animation:deappFadeIn .16s ease}
    .deapp-native-profile-options-sheet{position:fixed;left:0;right:0;bottom:0;z-index:4990;background:var(--surface);border-radius:26px 26px 0 0;padding:9px 14px max(20px,env(safe-area-inset-bottom));max-height:82dvh;overflow-y:auto;box-shadow:0 -14px 42px rgba(0,0,0,.22);animation:deappNativeSheetIn .2s cubic-bezier(.2,.8,.2,1);will-change:transform}
    .deapp-native-profile-options-sheet:before{content:"";display:block;width:38px;height:4px;border-radius:99px;background:color-mix(in srgb,var(--text-muted) 44%,transparent);margin:2px auto 13px}
    .deapp-native-profile-options-title{font-weight:800;font-size:18px;margin:0 4px 11px}
    .deapp-native-profile-options-list{display:grid;gap:7px}
    .deapp-native-profile-sheet-item{width:100%!important;min-height:50px!important;display:flex!important;align-items:center!important;justify-content:flex-start!important;gap:10px!important;padding:12px 14px!important;border:0!important;border-radius:14px!important;background:var(--surface-2,var(--surface-soft,#f5f5f5))!important;color:var(--text)!important;text-decoration:none!important;font:inherit!important;text-align:left!important}
    .deapp-native-profile-sheet-item.danger{color:var(--danger,#e5484d)!important}

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
    .deapp-native-about-row{appearance:none;border:0;width:100%;text-align:left;cursor:pointer}
    .deapp-native-about-row .deapp-version-pill{margin-left:auto;font-size:11px;font-weight:800;color:var(--text-muted);white-space:nowrap}

    /* v1.9 — mobile width, story tray, post detail dan composer komentar ala Threads. */
    html,body{max-width:100%!important;overflow-x:hidden!important}
    *,*:before,*:after{box-sizing:border-box!important}
    .layout,.layout-guest,.main-col,.feed,.card,.post-card,.modal-box,.composer,.settings-wrap,.wide-layout,.wide-main{max-width:100%!important;min-width:0!important}
    img,video,canvas,iframe{max-width:100%!important}
    /* v1.9.4 — Story kembali tampil pada Beranda dan tetap nyaman digeser horizontal. */
    .page-home .story-tray{display:flex!important;max-width:100%!important;overflow-x:auto!important;overflow-y:hidden!important;scrollbar-width:none!important;-webkit-overflow-scrolling:touch!important;overscroll-behavior-x:contain!important;touch-action:pan-x pan-y!important}
    .page-home .story-tray::-webkit-scrollbar{display:none!important}
    .page-home .story-tray>*{flex:0 0 auto!important}

    /* Lite tidak menampilkan fitur terjemahan pada postingan. Konten asli tetap utuh. */
    .post-card .post-translation,.post-card .post-translate,.post-card .translation-box,.post-card .translation-result,
    .post-card .translate-post,.post-card .post-translate-btn,.post-card [data-action="translate"],.post-card [data-action="translation"],
    .post-card [data-translate-post],.post-card [data-post-translate]{display:none!important}
    .deapp-native-post-detail .post-detail-nav,.deapp-native-post-detail .post-meta-card,.deapp-native-post-detail .wide-side,.deapp-native-post-detail .post-detail-more{display:none!important}
    .deapp-native-post-detail .wide-layout,.deapp-native-post-detail .wide-main{display:block!important;width:100%!important;max-width:100%!important;margin:0!important;padding:0!important}
    .deapp-native-post-detail .feed{width:100%!important;max-width:100%!important;border:0!important;border-radius:0!important}
    .deapp-native-post-detail .post-card:not(.post-embedded){padding-bottom:92px!important}
    .deapp-native-post-detail .comments-section{display:block!important;margin:0!important;padding:8px 14px 96px!important;border-top:1px solid var(--border)!important}
    .deapp-native-post-detail .comment-form{position:fixed!important;left:0!important;right:0!important;bottom:0!important;z-index:4300!important;display:flex!important;align-items:flex-end!important;gap:8px!important;width:100%!important;max-width:100%!important;padding:9px 10px max(9px,env(safe-area-inset-bottom))!important;background:color-mix(in srgb,var(--surface) 96%,transparent)!important;border-top:1px solid var(--border)!important;backdrop-filter:blur(18px)!important}
    .deapp-native-post-detail .comment-avatar{width:34px!important;height:34px!important;flex:0 0 34px!important;margin-bottom:3px!important}
    .deapp-native-post-detail .comment-input-wrap{flex:1 1 auto!important;min-width:0!important;display:flex!important;align-items:flex-end!important;border:1px solid var(--border)!important;border-radius:22px!important;background:var(--surface-2,var(--surface-soft,#f5f5f5))!important;padding:5px 7px 5px 12px!important}
    .deapp-native-post-detail .comment-input{display:block!important;width:100%!important;min-width:0!important;min-height:28px!important;max-height:126px!important;resize:none!important;overflow-y:auto!important;border:0!important;outline:0!important;background:transparent!important;padding:5px 0!important;font:inherit!important;line-height:1.35!important;color:var(--text)!important}
    .deapp-native-post-detail .comment-type-select{width:38px!important;max-width:38px!important;height:38px!important;border-radius:19px!important;overflow:hidden!important;color:transparent!important;padding:0!important;flex:0 0 38px!important}
    .deapp-native-post-detail .comment-form.deapp-comment-typing .comment-type-select,.deapp-native-post-detail .comment-form.deapp-comment-typing .js-emoji-trigger{display:none!important}
    .deapp-native-post-detail .btn-send{width:40px!important;height:40px!important;flex:0 0 40px!important;border-radius:50%!important;margin-bottom:1px!important;background:var(--text)!important;color:var(--surface)!important;box-shadow:none!important}
    .deapp-native-post-detail .bottom-nav{display:none!important}

    /* v1.9.5 — posting panjang maksimal 250 karakter di feed. */
    .deapp-post-preview{position:relative!important}
    .deapp-post-preview .deapp-post-more{
      display:inline!important;margin:0 0 0 4px!important;padding:0!important;border:0!important;background:transparent!important;
      color:var(--text-muted)!important;font:inherit!important;font-weight:700!important;line-height:inherit!important;cursor:pointer!important;
    }
    .deapp-post-preview .deapp-post-more:active{opacity:.68!important}

    /* v1.9.5 — komentar detail postingan lebih dekat dengan pola Threads: tanpa bubble berat,
       avatar rail, metadata ringan, dan composer yang dapat tumbuh beberapa baris. */
    .deapp-native-post-detail .comments-section{background:var(--surface)!important}
    .deapp-native-post-detail .comment-list{gap:0!important;margin:0!important}
    .deapp-native-post-detail .comment{
      position:relative!important;display:flex!important;gap:11px!important;padding:14px 0 13px!important;margin:0!important;
      border-bottom:1px solid color-mix(in srgb,var(--border) 76%,transparent)!important;animation:none!important;
    }
    .deapp-native-post-detail .comment.is-reply{margin-left:45px!important}
    .deapp-native-post-detail .comment-avatar{width:38px!important;height:38px!important;flex:0 0 38px!important;border:0!important}
    .deapp-native-post-detail .comment.is-reply .comment-avatar{width:32px!important;height:32px!important;flex-basis:32px!important}
    .deapp-native-post-detail .comment:not(:last-child):not(.is-reply):before{
      content:"";position:absolute;left:18px;top:54px;bottom:-1px;width:2px;border-radius:99px;
      background:color-mix(in srgb,var(--border) 88%,transparent);pointer-events:none;
    }
    .deapp-native-post-detail .comment-body{min-width:0!important;flex:1!important;padding-top:1px!important}
    .deapp-native-post-detail .comment-bubble{display:block!important;width:100%!important;max-width:none!important;padding:0!important;border:0!important;border-radius:0!important;background:transparent!important;box-shadow:none!important;font-size:14.5px!important;line-height:1.48!important}
    .deapp-native-post-detail .comment-author-row{display:flex!important;align-items:center!important;gap:6px!important;min-height:20px!important;flex-wrap:wrap!important}
    .deapp-native-post-detail .comment-author{font-size:14px!important;font-weight:800!important}
    .deapp-native-post-detail .comment-text{margin-top:3px!important;white-space:pre-wrap!important;overflow-wrap:anywhere!important;color:var(--text)!important}
    .deapp-native-post-detail .comment-replyto{margin:0 0 3px!important;font-size:11.5px!important;color:var(--text-muted)!important}
    .deapp-native-post-detail .comment-type-chip,.deapp-native-post-detail .comment-solution-chip{font-size:9px!important;padding:2px 6px!important}
    .deapp-native-post-detail .comment-meta{gap:13px!important;padding:6px 0 0!important;font-size:11.5px!important;flex-wrap:wrap!important}
    .deapp-native-post-detail .comment-meta button{font-size:11.5px!important;font-weight:700!important}
    .deapp-native-post-detail .reply-target{
      position:fixed!important;left:10px!important;right:10px!important;bottom:calc(58px + env(safe-area-inset-bottom))!important;z-index:4302!important;
      margin:0!important;border-radius:14px!important;padding:7px 11px!important;background:var(--surface-2)!important;border:1px solid var(--border)!important;
    }
    .deapp-native-post-detail .comment-form{min-height:58px!important}
    .deapp-native-post-detail .comment-input-wrap{min-height:44px!important;border-radius:22px!important;background:var(--surface)!important;padding:4px 5px 4px 12px!important;box-shadow:none!important}
    .deapp-native-post-detail .comment-input-wrap:focus-within{border-color:color-mix(in srgb,var(--text) 36%,var(--border))!important;box-shadow:none!important}
    .deapp-native-post-detail .deapp-comment-source-input{display:none!important}
    .deapp-native-post-detail textarea.deapp-comment-textarea{
      display:block!important;width:100%!important;min-width:0!important;min-height:32px!important;max-height:128px!important;
      resize:none!important;overflow-y:auto!important;border:0!important;outline:0!important;background:transparent!important;
      padding:6px 2px!important;font:inherit!important;font-size:14.5px!important;line-height:1.4!important;color:var(--text)!important;box-shadow:none!important;
    }
    .deapp-native-post-detail .comment-type-select{
      appearance:none!important;-webkit-appearance:none!important;width:auto!important;max-width:102px!important;height:34px!important;min-height:34px!important;
      padding:0 9px!important;border-radius:17px!important;color:var(--text-muted)!important;background:var(--surface-2)!important;border:1px solid var(--border)!important;
      font-size:10px!important;font-weight:750!important;flex:0 0 auto!important;
    }
    .deapp-native-post-detail .comment-form.deapp-comment-typing .comment-type-select,
    .deapp-native-post-detail .comment-form.deapp-comment-typing .js-emoji-trigger{display:none!important}
    .deapp-native-post-detail .comment-form:not(.deapp-comment-typing) .btn-send{opacity:.42!important}
    .deapp-native-post-detail .comment-form.deapp-comment-typing .btn-send{opacity:1!important}

    /* v1.9.5 — Video Pendek memakai chrome sendiri, terpisah dari header/footer utama Android. */
    .deapp-native-reels body{background:#000!important;padding:0!important;overflow:hidden!important}
    .deapp-native-reels .layout,.deapp-native-reels .layout-guest,.deapp-native-reels .wide-layout,.deapp-native-reels .wide-main,.deapp-native-reels .main-col{
      width:100%!important;max-width:100%!important;min-width:0!important;margin:0!important;padding:0!important;background:#000!important;
    }
    .deapp-native-reels .siderail,.deapp-native-reels .side-col,.deapp-native-reels .site-footer,.deapp-native-reels .reels-toolbar{display:none!important}
    .deapp-native-reels .reels-workspace{display:block!important;min-height:100dvh!important;gap:0!important;background:#000!important}
    .deapp-native-reels .reels-feed{height:100dvh!important;min-height:100dvh!important;margin:0!important;border-radius:0!important;background:#000!important;overscroll-behavior-y:contain!important}
    .deapp-native-reels .reel-card{height:100dvh!important;min-height:100dvh!important;background:#000!important}
    .deapp-native-reels .reel-media{width:100%!important;max-width:560px!important;height:100%!important;border-radius:0!important}
    .deapp-native-reels .reel-volume{top:calc(67px + env(safe-area-inset-top))!important;right:12px!important}
    .deapp-native-reels .reel-meta{left:14px!important;right:76px!important;bottom:calc(82px + env(safe-area-inset-bottom))!important}
    .deapp-native-reels .reel-actions{right:8px!important;bottom:calc(80px + env(safe-area-inset-bottom))!important}
    .deapp-native-reels .reel-progress{bottom:calc(67px + env(safe-area-inset-bottom))!important}
    .deapp-native-reels .reels-audio-banner,.deapp-native-reels .reel-creator-dashboard{margin-top:calc(64px + env(safe-area-inset-top))!important;border-radius:0!important}
    .deapp-reels-header{
      position:fixed;left:0;right:0;top:0;z-index:1180;display:grid;grid-template-columns:48px 1fr 48px;align-items:end;
      min-height:calc(58px + env(safe-area-inset-top));padding:env(safe-area-inset-top) 8px 7px;
      color:#fff;background:linear-gradient(to bottom,rgba(0,0,0,.72),rgba(0,0,0,.18),transparent);pointer-events:none;
    }
    .deapp-reels-header>*{pointer-events:auto}
    .deapp-reels-header button{width:42px;height:42px;border:0;border-radius:50%;display:grid;place-items:center;background:rgba(20,20,20,.42);color:#fff;backdrop-filter:blur(12px);-webkit-backdrop-filter:blur(12px)}
    .deapp-reels-title{text-align:center;align-self:center;min-width:0;text-shadow:0 1px 12px rgba(0,0,0,.6)}
    .deapp-reels-title b{display:block;font-size:16px;line-height:1.15;font-weight:850;letter-spacing:-.015em}
    .deapp-reels-title small{display:block;margin-top:2px;font-size:10.5px;color:rgba(255,255,255,.72)}
    .deapp-reels-footer{
      position:fixed;left:0;right:0;bottom:0;z-index:1180;display:grid;grid-template-columns:1fr 1fr 1fr 1fr 1fr;align-items:start;
      min-height:calc(66px + env(safe-area-inset-bottom));padding:5px 4px env(safe-area-inset-bottom);
      background:linear-gradient(to top,rgba(0,0,0,.94),rgba(0,0,0,.72),rgba(0,0,0,.20));color:#fff;
    }
    .deapp-reels-nav-item,.deapp-reels-create{
      min-width:0;height:58px;border:0;background:transparent;color:rgba(255,255,255,.70);display:flex;flex-direction:column;align-items:center;justify-content:center;gap:3px;
      font-size:9.5px;font-weight:720;white-space:nowrap;overflow:hidden;text-overflow:ellipsis;
    }
    .deapp-reels-nav-item svg,.deapp-reels-create svg{width:22px;height:22px;display:block}
    .deapp-reels-nav-item.is-active{color:#fff}
    .deapp-reels-nav-item.is-active:after{content:"";width:4px;height:4px;border-radius:50%;background:#fff;margin-top:-1px}
    .deapp-reels-create span{width:42px;height:34px;border-radius:11px;display:grid;place-items:center;background:#fff;color:#050505;box-shadow:0 7px 20px rgba(0,0,0,.26)}
    .deapp-native-reels.deapp-reels-overlay-open .deapp-reels-header,.deapp-native-reels.deapp-reels-overlay-open .deapp-reels-footer{opacity:0!important;visibility:hidden!important;pointer-events:none!important}
    .deapp-native-reels .reel-sheet{z-index:4600!important}
    .deapp-native-reels .reel-sheet-panel{height:min(78dvh,680px)!important;border-radius:24px 24px 0 0!important;background:var(--surface)!important}
    .deapp-native-reels .reel-sheet-panel>header{padding:10px 14px!important}
    .deapp-native-reels .reel-sheet-body{padding:0 14px!important}
    .deapp-native-reels .reel-comment{grid-template-columns:38px minmax(0,1fr) auto!important;gap:10px!important;padding:14px 0!important;border-bottom:1px solid color-mix(in srgb,var(--border) 76%,transparent)!important}
    .deapp-native-reels .reel-comment img{width:38px!important;height:38px!important}
    .deapp-native-reels .reel-comment b{font-size:13.5px!important}
    .deapp-native-reels .reel-comment small{font-size:10.5px!important}
    .deapp-native-reels .reel-comment p{font-size:14px!important;line-height:1.45!important;margin-top:4px!important}
    .deapp-native-reels .reel-comment-form{
      display:grid!important;grid-template-columns:minmax(0,1fr) 42px!important;gap:8px!important;padding:9px 10px calc(9px + env(safe-area-inset-bottom))!important;
      border-top:1px solid var(--border)!important;background:color-mix(in srgb,var(--surface) 96%,transparent)!important;backdrop-filter:blur(18px)!important;
    }
    .deapp-native-reels #reel-comment-input{height:42px!important;border:1px solid var(--border)!important;border-radius:21px!important;background:var(--surface-2)!important;padding:0 14px!important;box-shadow:none!important}
    .deapp-native-reels .reel-comment-form button[type="submit"]{width:42px!important;height:42px!important;border-radius:50%!important;padding:0!important;display:grid!important;place-items:center!important}

    /* v1.9.8 — setiap bottom sheet/modal yang benar-benar terlihat menyingkirkan navigasi bawah.
       Header tetap terlihat tetapi hanya diredupkan ringan, meniru lapisan halaman di belakang sheet Threads. */
    html.deapp-bottom-sheet-active .deapp-section-footer,
    html.deapp-bottom-sheet-active .deapp-reels-footer,
    html.deapp-bottom-sheet-active .bottom-nav{
      opacity:0!important;visibility:hidden!important;pointer-events:none!important;transform:translateY(112%)!important;
      transition:opacity .14s ease,transform .19s cubic-bezier(.2,.8,.2,1)!important
    }
    html.deapp-bottom-sheet-active .deapp-section-header,
    html.deapp-bottom-sheet-active .deapp-reels-header,
    html.deapp-bottom-sheet-active .story-topbar,
    html.deapp-bottom-sheet-active .chat-head{
      filter:brightness(.88) saturate(.96)!important;-webkit-filter:brightness(.88) saturate(.96)!important;
      pointer-events:none!important;transition:filter .15s ease!important
    }

    /* v1.9.6 — chrome khusus per bagian: chat, notifikasi, live, Deapp AI, toko/dompet, pengaturan, dan Story. */
    html.deapp-has-section-chrome body{padding-top:calc(62px + env(safe-area-inset-top))!important;padding-bottom:calc(68px + env(safe-area-inset-bottom))!important}
    .deapp-section-header{
      position:fixed;left:0;right:0;top:0;z-index:4450;min-height:calc(58px + env(safe-area-inset-top));
      display:grid;grid-template-columns:48px minmax(0,1fr) 48px;align-items:end;gap:4px;padding:env(safe-area-inset-top) 8px 7px;
      background:color-mix(in srgb,var(--surface) 94%,transparent);border-bottom:1px solid color-mix(in srgb,var(--border) 86%,transparent);
      backdrop-filter:blur(18px);-webkit-backdrop-filter:blur(18px);box-shadow:0 5px 20px rgba(15,23,42,.035);color:var(--text)
    }
    .deapp-section-header .deapp-section-left,.deapp-section-header .deapp-section-action{
      width:42px;height:42px;border:0;border-radius:50%;display:grid;place-items:center;background:transparent;color:inherit;padding:0
    }
    .deapp-section-header .deapp-section-left:active,.deapp-section-header .deapp-section-action:active{background:var(--surface-2)!important}
    .deapp-section-title{min-width:0;text-align:center;align-self:center;line-height:1.12}
    .deapp-section-title b{display:block;font-size:16px;font-weight:850;letter-spacing:-.018em;white-space:nowrap;overflow:hidden;text-overflow:ellipsis}
    .deapp-section-title small{display:block;margin-top:3px;font-size:10px;color:var(--text-muted);white-space:nowrap;overflow:hidden;text-overflow:ellipsis}
    .deapp-section-footer{
      position:fixed;left:0;right:0;bottom:0;z-index:4440;min-height:calc(64px + env(safe-area-inset-bottom));
      display:grid;grid-template-columns:repeat(var(--deapp-section-cols,5),minmax(0,1fr));align-items:start;padding:4px 3px env(safe-area-inset-bottom);
      background:color-mix(in srgb,var(--surface) 96%,transparent);border-top:1px solid color-mix(in srgb,var(--border) 86%,transparent);
      backdrop-filter:blur(20px);-webkit-backdrop-filter:blur(20px);box-shadow:0 -6px 22px rgba(15,23,42,.04);color:var(--text)
    }
    .deapp-section-nav{
      height:58px;min-width:0;border:0;background:transparent;color:var(--text-muted);display:flex;flex-direction:column;align-items:center;justify-content:center;gap:3px;
      padding:2px;font:inherit;font-size:9.4px;font-weight:760;white-space:nowrap;overflow:hidden;text-overflow:ellipsis
    }
    .deapp-section-nav svg{width:21px;height:21px;display:block;flex:none}
    .deapp-section-nav.is-active{color:var(--acc,var(--text))}
    .deapp-section-nav.is-active:after{content:"";width:4px;height:4px;border-radius:50%;background:currentColor;margin-top:-1px}
    .deapp-section-nav.deapp-section-primary span{width:42px;height:34px;border-radius:11px;display:grid;place-items:center;background:linear-gradient(135deg,var(--acc-from),var(--acc-to));color:#fff;box-shadow:0 7px 18px color-mix(in srgb,var(--acc) 22%,transparent)}
    .deapp-section-nav.deapp-section-primary svg{width:20px;height:20px}
    /* v1.9.13 — Notifikasi memakai bottom navigation umum Android. Filter kategori
       dipindahkan ke tab horizontal di atas konten seperti tab sosial modern. */
    html.deapp-native-notifications.deapp-has-section-chrome body{padding-bottom:14px!important}
    html.deapp-native-notifications .page-head{display:none!important}
    html.deapp-native-notifications .tabs.sticky-tabs,html.deapp-native-notifications .deapp-notification-tabs{
      display:flex!important;position:sticky!important;top:calc(58px + env(safe-area-inset-top))!important;z-index:4430!important;
      width:100%!important;max-width:none!important;margin:0!important;padding:7px 9px 8px!important;gap:6px!important;
      overflow-x:auto!important;overflow-y:hidden!important;flex-wrap:nowrap!important;scrollbar-width:none!important;overscroll-behavior-x:contain!important;
      background:color-mix(in srgb,var(--surface) 96%,transparent)!important;border:0!important;border-bottom:1px solid color-mix(in srgb,var(--border) 82%,transparent)!important;
      border-radius:0!important;box-shadow:none!important;backdrop-filter:blur(18px)!important;-webkit-backdrop-filter:blur(18px)!important;
      scroll-padding-inline:10px!important;-webkit-overflow-scrolling:touch!important;touch-action:pan-x!important
    }
    html.deapp-native-notifications .tabs.sticky-tabs::-webkit-scrollbar,html.deapp-native-notifications .deapp-notification-tabs::-webkit-scrollbar{display:none!important}
    html.deapp-native-notifications .deapp-notification-tabs .tab{
      flex:0 0 auto!important;min-width:max-content!important;min-height:38px!important;padding:8px 13px!important;border-radius:19px!important;
      border:1px solid color-mix(in srgb,var(--border) 88%,transparent)!important;background:var(--surface)!important;color:var(--text-muted)!important;
      font-size:12.5px!important;font-weight:760!important;gap:6px!important;white-space:nowrap!important;box-shadow:none!important
    }
    html.deapp-native-notifications .deapp-notification-tabs .tab svg{width:16px!important;height:16px!important}
    html.deapp-native-notifications .deapp-notification-tabs .tab.active{
      color:var(--text)!important;border-color:color-mix(in srgb,var(--acc) 42%,var(--border))!important;
      background:color-mix(in srgb,var(--acc) 11%,var(--surface))!important
    }
    html.deapp-native-notifications .nx-filter{margin-top:8px!important}
    html.deapp-native-live .live-discover-head,html.deapp-native-live .live-tabs{display:none!important}
    html.deapp-native-live .live-page-shell{padding-top:8px!important;padding-bottom:92px!important}
    html.deapp-native-ai .deapp-ai-hero{margin-top:0!important}
    html.deapp-native-ai .deapp-ai-nav{display:none!important}
    html.deapp-native-ai body{overflow-x:hidden!important}
    html.deapp-native-ai .deapp-ai-shell{height:calc(100dvh - 154px - env(safe-area-inset-top) - env(safe-area-inset-bottom))!important;min-height:520px!important}

    /* v1.9.9 — Deapp AI memakai header + burger, tanpa navigation bar bawah.
       Composer Chat AI menjadi area tetap di bawah, sedangkan tombol Chat Baru menjadi FAB AI. */
    html.deapp-native-ai.deapp-has-section-chrome body{padding-bottom:0!important}
    html.deapp-native-ai .deapp-section-footer{display:none!important}
    html.deapp-native-ai .deapp-section-header{grid-template-columns:44px minmax(0,1fr) 44px!important}
    html.deapp-native-ai .deapp-section-header .deapp-section-action svg{width:23px;height:23px}
    html.deapp-native-ai .deapp-ai-shell{height:calc(100dvh - 62px - env(safe-area-inset-top))!important;min-height:0!important}
    html.deapp-native-ai .deapp-ai-chat-panel{position:relative!important;min-height:0!important}
    html.deapp-native-ai[data-deapp-ai-view="chat"] .deapp-ai-messages{padding-bottom:132px!important;scroll-padding-bottom:132px!important}
    html.deapp-native-ai[data-deapp-ai-view="chat"] .deapp-ai-followups{margin-bottom:116px!important}
    html.deapp-native-ai[data-deapp-ai-view="chat"] .deapp-ai-composer-wrap{
      position:fixed!important;left:0!important;right:0!important;bottom:0!important;z-index:4435!important;margin:0!important;
      padding:8px 10px max(8px,env(safe-area-inset-bottom))!important;background:color-mix(in srgb,var(--surface) 96%,transparent)!important;
      border-top:1px solid color-mix(in srgb,var(--border) 78%,transparent)!important;backdrop-filter:blur(20px)!important;-webkit-backdrop-filter:blur(20px)!important;
      box-shadow:0 -6px 24px rgba(15,23,42,.045)!important
    }
    html.deapp-native-ai[data-deapp-ai-view="chat"] .deapp-ai-composer{
      width:min(100%,760px)!important;margin:0 auto!important;border:1px solid color-mix(in srgb,var(--border) 90%,transparent)!important;
      border-radius:22px!important;background:var(--surface)!important;box-shadow:none!important
    }
    html.deapp-native-ai[data-deapp-ai-view="chat"] .deapp-ai-disclaimer{display:none!important}
    html.deapp-native-ai[data-deapp-ai-view="chat"] .deapp-ai-attachment-chip{width:min(100%,760px)!important;margin:6px auto 0!important}
    .deapp-ai-native-menu-backdrop{position:fixed;inset:0;z-index:4448;background:rgba(0,0,0,.055);opacity:0;pointer-events:none;transition:opacity .14s ease}
    .deapp-ai-native-menu-backdrop.open{opacity:1;pointer-events:auto}
    .deapp-ai-native-menu{
      position:fixed;right:10px;top:calc(58px + env(safe-area-inset-top));z-index:4460;width:min(286px,calc(100vw - 20px));
      padding:8px;border:1px solid color-mix(in srgb,var(--border) 88%,transparent);border-radius:20px;background:color-mix(in srgb,var(--surface) 98%,transparent);
      backdrop-filter:blur(22px);-webkit-backdrop-filter:blur(22px);box-shadow:0 18px 50px rgba(0,0,0,.18);opacity:0;visibility:hidden;
      transform:translateY(-7px) scale(.98);transform-origin:top right;pointer-events:none;transition:opacity .14s ease,transform .16s cubic-bezier(.2,.8,.2,1),visibility .14s ease
    }
    .deapp-ai-native-menu.open{opacity:1;visibility:visible;transform:none;pointer-events:auto}
    .deapp-ai-native-menu button{
      width:100%;min-height:50px;border:0;border-radius:14px;background:transparent;color:var(--text);display:grid;grid-template-columns:38px minmax(0,1fr) 18px;
      align-items:center;gap:8px;padding:7px 10px;text-align:left;font:inherit
    }
    .deapp-ai-native-menu button:active,.deapp-ai-native-menu button.is-active{background:var(--surface-2)}
    .deapp-ai-native-menu .deapp-ai-menu-icon{width:34px;height:34px;border-radius:11px;display:grid;place-items:center;background:var(--surface-2);color:var(--text)}
    .deapp-ai-native-menu button.is-active .deapp-ai-menu-icon{background:color-mix(in srgb,var(--acc) 14%,var(--surface));color:var(--acc,var(--text))}
    .deapp-ai-native-menu .deapp-ai-menu-copy{min-width:0}
    .deapp-ai-native-menu .deapp-ai-menu-copy b{display:block;font-size:14px;font-weight:780}
    .deapp-ai-native-menu .deapp-ai-menu-copy small{display:block;margin-top:2px;color:var(--text-muted);font-size:10.5px}
    .deapp-ai-native-menu .deapp-ai-menu-check{opacity:0;color:var(--acc,var(--text));font-size:17px;text-align:center}
    .deapp-ai-native-menu button.is-active .deapp-ai-menu-check{opacity:1}
    .deapp-ai-fab{
      position:fixed;right:16px;bottom:18px;z-index:4438;width:58px;height:58px;border:0;border-radius:50%;padding:0;display:grid;place-items:center;color:#fff;
      background:linear-gradient(145deg,var(--acc-from,#5b67ff),var(--acc-to,#8a55ff));box-shadow:0 12px 30px color-mix(in srgb,var(--acc,#6366f1) 30%,rgba(0,0,0,.2));
      transition:transform .15s ease,opacity .15s ease,bottom .18s ease
    }
    .deapp-ai-fab:active{transform:scale(.94)}
    .deapp-ai-fab .deapp-ai-fab-mark{font-size:25px;line-height:1;transform:translateY(-1px)}
    .deapp-ai-fab .deapp-ai-fab-plus{position:absolute;right:-1px;bottom:-1px;width:22px;height:22px;border-radius:50%;display:grid;place-items:center;background:var(--text);color:var(--surface);border:2px solid var(--surface);font-size:17px;font-weight:700;line-height:1}
    html.deapp-native-ai[data-deapp-ai-view="chat"] .deapp-ai-fab{bottom:calc(102px + env(safe-area-inset-bottom))}
    html.deapp-bottom-sheet-active .deapp-ai-fab{opacity:0!important;visibility:hidden!important;pointer-events:none!important;transform:translateY(18px)!important}
    html.deapp-native-shop .page-head{display:none!important}
    html.deapp-native-settings-family .page-head{display:none!important}
    html.deapp-native-settings-family .security-page-head{margin-top:0!important}
    html.deapp-native-settings-family .settings-wrap{margin-top:0!important}

    /* Chat: daftar percakapan memakai chrome khusus; percakapan aktif memakai head + composer asli sebagai header/footer. */
    html.deapp-native-messages.deapp-chat-list body{padding-top:calc(62px + env(safe-area-inset-top))!important;padding-bottom:calc(68px + env(safe-area-inset-bottom))!important}
    html.deapp-native-messages.deapp-chat-list .msg-list-head{display:none!important}
    html.deapp-native-messages.deapp-chat-open body{padding:0!important;overflow:hidden!important}
    html.deapp-native-messages.deapp-chat-open .layout,html.deapp-native-messages.deapp-chat-open .wide-layout,html.deapp-native-messages.deapp-chat-open .main-col{padding:0!important;margin:0!important;max-width:none!important;width:100%!important}
    html.deapp-native-messages.deapp-chat-open .messenger{height:100dvh!important;min-height:100dvh!important;margin:0!important;gap:0!important}
    html.deapp-native-messages.deapp-chat-open .msg-chat{height:100dvh!important;min-height:0!important;border:0!important;border-radius:0!important;box-shadow:none!important}
    html.deapp-native-messages.deapp-chat-open .chat-head{
      min-height:calc(60px + env(safe-area-inset-top))!important;padding:env(safe-area-inset-top) 10px 7px!important;
      background:color-mix(in srgb,var(--surface) 95%,transparent)!important;border-bottom:1px solid var(--border)!important;backdrop-filter:blur(18px)!important;-webkit-backdrop-filter:blur(18px)!important;z-index:12!important
    }
    html.deapp-native-messages.deapp-chat-open .chat-scroll{padding-bottom:8px!important}
    html.deapp-native-messages.deapp-chat-open .chat-compose{
      padding:7px 9px calc(7px + env(safe-area-inset-bottom))!important;background:color-mix(in srgb,var(--surface) 96%,transparent)!important;
      backdrop-filter:blur(18px)!important;-webkit-backdrop-filter:blur(18px)!important;border-top:1px solid var(--border)!important;z-index:12!important
    }
    html.deapp-native-messages.deapp-chat-open .chat-form{align-items:flex-end!important}
    html.deapp-native-messages.deapp-chat-open #chat-input{max-height:132px!important;line-height:1.38!important}

    /* Story: viewer tetap menggunakan kontrol asli Deapp, tetapi menjadi chrome penuh yang lebih rapi. */
    html.deapp-native-story-open body{overflow:hidden!important;padding:0!important}
    html.deapp-native-story-open #story-viewer{z-index:6000!important;background:#050505!important}
    html.deapp-native-story-open .story-progress{padding:calc(8px + env(safe-area-inset-top)) 10px 0!important;gap:3px!important}
    html.deapp-native-story-open .story-progress i{height:2.5px!important;background:rgba(255,255,255,.26)!important}
    html.deapp-native-story-open .story-topbar{padding:9px 10px 8px!important;background:linear-gradient(to bottom,rgba(0,0,0,.55),transparent)!important;position:relative;z-index:4!important}
    html.deapp-native-story-open .story-user img{width:36px!important;height:36px!important;border-color:rgba(255,255,255,.72)!important}
    html.deapp-native-story-open .story-close{width:40px!important;height:40px!important;border-radius:50%!important;background:rgba(20,20,20,.40)!important;backdrop-filter:blur(12px)!important}
    html.deapp-native-story-open .story-bottom{padding:0 10px calc(8px + env(safe-area-inset-bottom))!important;background:linear-gradient(to top,rgba(0,0,0,.70),rgba(0,0,0,.08),transparent)!important}
    html.deapp-native-story-open .story-bar{gap:7px!important}
    html.deapp-native-story-open .story-reply input{height:44px!important;border-radius:22px!important;background:rgba(20,20,20,.38)!important;border:1px solid rgba(255,255,255,.38)!important;padding:0 44px 0 15px!important;backdrop-filter:blur(12px)!important}
    html.deapp-native-story-open .story-send{right:5px!important;width:34px!important;height:34px!important;border-radius:50%!important;background:#fff!important;color:#111!important}
    html.deapp-native-story-open .story-act{width:42px!important;height:42px!important;border-radius:50%!important;background:rgba(20,20,20,.34)!important;justify-content:center!important;backdrop-filter:blur(10px)!important}
    html.deapp-native-story-open .story-sheet{left:8px!important;right:8px!important;bottom:calc(64px + env(safe-area-inset-bottom))!important;border-radius:24px!important;background:rgba(22,22,22,.94)!important;backdrop-filter:blur(22px)!important;-webkit-backdrop-filter:blur(22px)!important}

    /* v1.9.8 — percakapan lebih minimal seperti Threads: badan chat bersih, bubble tanpa ekor,
       ritme pesan rapat, composer lembut, serta header partner yang tidak terasa berat. */
    html.deapp-native-messages.deapp-chat-open .chat-head{
      min-height:calc(56px + env(safe-area-inset-top))!important;padding:env(safe-area-inset-top) 8px 5px!important;
      gap:6px!important;background:color-mix(in srgb,var(--surface) 97%,transparent)!important;
      border-bottom:1px solid color-mix(in srgb,var(--border) 70%,transparent)!important;box-shadow:none!important
    }
    html.deapp-native-messages.deapp-chat-open .chat-head>.only-mobile.icon-btn{
      width:40px!important;height:40px!important;border-radius:50%!important;background:transparent!important
    }
    html.deapp-native-messages.deapp-chat-open .chat-peer{gap:8px!important}
    html.deapp-native-messages.deapp-chat-open .chat-peer .thread-avatar img{width:36px!important;height:36px!important}
    html.deapp-native-messages.deapp-chat-open .chat-peer b{font-size:14.5px!important;font-weight:780!important;line-height:1.18!important}
    html.deapp-native-messages.deapp-chat-open .chat-peer small{font-size:10.5px!important;margin-top:2px!important}
    html.deapp-native-messages.deapp-chat-open .chat-scroll{
      background:var(--surface)!important;background-image:none!important;padding:14px 10px 18px!important;gap:2px!important;
      scroll-padding-bottom:18px!important;overscroll-behavior-y:contain!important
    }
    html.deapp-native-messages.deapp-chat-open .chat-day{
      margin:12px auto 8px!important;padding:3px 9px!important;background:transparent!important;color:var(--text-faint)!important;
      font-size:10px!important;font-weight:650!important;border-radius:0!important
    }
    html.deapp-native-messages.deapp-chat-open .chat-mood{
      margin:0!important;padding:5px 10px!important;text-align:center!important;background:var(--surface)!important;border-bottom:0!important;
      color:var(--text-muted)!important;font-size:10.5px!important
    }
    html.deapp-native-messages.deapp-chat-open .bubble{
      max-width:min(82%,520px)!important;padding:8px 12px!important;border-radius:18px!important;font-size:14.5px!important;
      line-height:1.42!important;box-shadow:none!important;animation:none!important;border:0!important
    }
    html.deapp-native-messages.deapp-chat-open .bubble.them:not(.is-gift):not(.is-sticker){
      background:#f1f1f1!important;color:#111!important;border:0!important;border-radius:18px!important
    }
    html.deapp-native-messages.deapp-chat-open .bubble.me:not(.is-gift):not(.is-sticker){
      background:#101010!important;color:#fff!important;border:0!important;border-radius:18px!important
    }
    html.is-dark.deapp-native-messages.deapp-chat-open .bubble.them:not(.is-gift):not(.is-sticker){background:#242424!important;color:#f5f5f5!important}
    html.is-dark.deapp-native-messages.deapp-chat-open .bubble.me:not(.is-gift):not(.is-sticker){background:#393939!important;color:#fff!important}
    html.deapp-native-messages.deapp-chat-open .bubble.me+.bubble.me{border-top-right-radius:7px!important;margin-top:0!important}
    html.deapp-native-messages.deapp-chat-open .bubble.them+.bubble.them{border-top-left-radius:7px!important;margin-top:0!important}
    html.deapp-native-messages.deapp-chat-open .bubble-time{margin-top:3px!important;font-size:9.5px!important;opacity:.58!important}
    html.deapp-native-messages.deapp-chat-open .bubble-img{border-radius:14px!important;margin:-4px -8px 5px!important}
    html.deapp-native-messages.deapp-chat-open .chat-compose{
      padding:5px 8px calc(6px + env(safe-area-inset-bottom))!important;background:color-mix(in srgb,var(--surface) 98%,transparent)!important;
      border-top:1px solid color-mix(in srgb,var(--border) 62%,transparent)!important;box-shadow:none!important
    }
    html.deapp-native-messages.deapp-chat-open .chat-quick{padding:4px 2px 5px!important;gap:3px!important}
    html.deapp-native-messages.deapp-chat-open .quick-react{background:transparent!important;border:0!important}
    html.deapp-native-messages.deapp-chat-open .chat-form{gap:5px!important;padding:0!important;border:0!important}
    html.deapp-native-messages.deapp-chat-open .chat-form>.icon-btn,
    html.deapp-native-messages.deapp-chat-open .chat-form>label.icon-btn{width:36px!important;height:36px!important;border-radius:50%!important}
    html.deapp-native-messages.deapp-chat-open .chat-input-wrap{
      border:1px solid color-mix(in srgb,var(--border) 90%,transparent)!important;border-radius:22px!important;background:var(--surface-2)!important;
      box-shadow:none!important;transition:border-color .15s ease,background .15s ease!important
    }
    html.deapp-native-messages.deapp-chat-open .chat-input-wrap:focus-within{border-color:color-mix(in srgb,var(--text) 28%,var(--border))!important;background:var(--surface)!important;box-shadow:none!important}
    html.deapp-native-messages.deapp-chat-open #chat-input{min-height:40px!important;height:40px;max-height:126px!important;padding:9px 12px!important;font-size:15px!important;line-height:1.4!important}
    html.deapp-native-messages.deapp-chat-open .chat-form .btn-send.lg{width:40px!important;height:40px!important;border-radius:50%!important}

    /* Pengaturan ala Threads: back sederhana, tidak berupa tombol kotak, judul bersih tanpa subtitle kedua. */
    html.deapp-native-settings-family .deapp-section-header{
      min-height:calc(56px + env(safe-area-inset-top));padding:env(safe-area-inset-top) 8px 6px;
      background:color-mix(in srgb,var(--surface) 98%,transparent);border-bottom:1px solid color-mix(in srgb,var(--border) 68%,transparent);box-shadow:none
    }
    html.deapp-native-settings-family .deapp-section-header .deapp-section-left{
      justify-self:start;width:40px;height:40px;border-radius:50%;background:transparent!important
    }
    html.deapp-native-settings-family .deapp-section-header .deapp-section-left svg{width:25px;height:25px;stroke-width:1.8}
    html.deapp-native-settings-family .deapp-section-header .deapp-section-title b{font-size:16.5px;font-weight:800;letter-spacing:-.015em}
    html.deapp-native-settings-family .deapp-section-header .deapp-section-title small{display:none!important}
    html.deapp-native-settings-family .deapp-section-header .deapp-section-action{width:40px;height:40px;background:transparent!important}
    html.deapp-native-settings-family .deapp-section-header .deapp-section-left:active,
    html.deapp-native-settings-family .deapp-section-header .deapp-section-action:active{background:var(--surface-2)!important}


    /* v1.9.11 — Story bergaya Status WhatsApp: media edge-to-edge, progress/user overlay,
       area tap kiri/kanan transparan, serta reply bar menempel di bawah. */
    .page-home .story-tray{gap:10px!important;padding:8px 10px 14px!important;background:var(--surface)!important}
    .page-home .story-cell{width:68px!important;gap:5px!important}
    .page-home .story-ring,.page-home .story-add-img{width:60px!important;height:60px!important;padding:2.5px!important}
    .page-home .story-ring.unseen{background:#25D366!important}
    .page-home .story-ring.seen{background:color-mix(in srgb,var(--text-muted) 42%,var(--surface))!important}
    .page-home .story-ring img,.page-home .story-add-img img{border:2.5px solid var(--surface)!important}
    .page-home .story-add-img{background:color-mix(in srgb,#25D366 20%,var(--surface))!important}
    .page-home .story-add-plus{width:22px!important;height:22px!important;right:-1px!important;bottom:-1px!important;background:#25D366!important;border-color:var(--surface)!important}
    .page-home .story-cell-name{font-size:10.8px!important;font-weight:650!important;color:var(--text)!important}

    html.deapp-native-story-open #story-viewer{position:fixed!important;inset:0!important;display:block!important;background:#000!important;color:#fff!important;overflow:hidden!important}
    html.deapp-native-story-open .story-progress{
      position:absolute!important;left:0!important;right:0!important;top:0!important;z-index:30!important;
      padding:calc(env(safe-area-inset-top) + 7px) 7px 0!important;gap:3px!important;background:transparent!important
    }
    html.deapp-native-story-open .story-progress i{height:2.5px!important;background:rgba(255,255,255,.34)!important;border-radius:99px!important}
    html.deapp-native-story-open .story-progress i b{background:#fff!important}
    html.deapp-native-story-open .story-topbar{
      position:absolute!important;left:0!important;right:0!important;top:calc(env(safe-area-inset-top) + 12px)!important;z-index:29!important;
      padding:11px 10px 16px!important;background:linear-gradient(to bottom,rgba(0,0,0,.68),rgba(0,0,0,.22),transparent)!important;
      gap:9px!important;align-items:center!important
    }
    html.deapp-native-story-open .story-user{gap:9px!important;min-width:0!important;text-shadow:0 1px 4px rgba(0,0,0,.65)!important}
    html.deapp-native-story-open .story-user img{width:38px!important;height:38px!important;border:1.5px solid rgba(255,255,255,.78)!important}
    html.deapp-native-story-open .story-user b{font-size:14px!important;font-weight:750!important;color:#fff!important}
    html.deapp-native-story-open .story-user small{font-size:11px!important;color:rgba(255,255,255,.78)!important;opacity:1!important}
    html.deapp-native-story-open .story-close{width:40px!important;height:40px!important;background:transparent!important;color:#fff!important;backdrop-filter:none!important}
    html.deapp-native-story-open .story-stage{
      position:absolute!important;inset:0!important;margin:0!important;border-radius:0!important;padding:76px 22px 116px!important;
      width:100%!important;height:100%!important;min-height:100%!important;background:#000!important;overflow:hidden!important
    }
    html.deapp-native-story-open .story-stage img{object-fit:contain!important;background:#000!important}
    html.deapp-native-story-open .story-stage video{width:100%!important;height:100%!important;object-fit:contain!important;background:#000!important}
    html.deapp-native-story-open .story-stage .story-cap-overlay{padding:70px 18px 116px!important;background:linear-gradient(transparent 44%,rgba(0,0,0,.72))!important;font-size:15px!important;font-weight:650!important}
    html.deapp-native-story-open .story-nav{
      top:64px!important;bottom:86px!important;height:auto!important;width:38%!important;transform:none!important;border-radius:0!important;background:transparent!important;
      backdrop-filter:none!important;-webkit-backdrop-filter:none!important;opacity:1!important;z-index:18!important
    }
    html.deapp-native-story-open .story-nav svg{opacity:0!important}
    html.deapp-native-story-open .story-prev{left:0!important}
    html.deapp-native-story-open .story-next{right:0!important}
    html.deapp-native-story-open .story-bottom{
      position:absolute!important;left:0!important;right:0!important;bottom:0!important;z-index:31!important;max-width:none!important;margin:0!important;
      padding:18px 9px calc(8px + env(safe-area-inset-bottom))!important;background:linear-gradient(to top,rgba(0,0,0,.72),rgba(0,0,0,.20),transparent)!important
    }
    html.deapp-native-story-open .story-bar{gap:3px!important;max-width:720px!important;margin:0 auto!important}
    html.deapp-native-story-open .story-reply input{
      height:44px!important;border-radius:23px!important;background:rgba(0,0,0,.24)!important;border:1.2px solid rgba(255,255,255,.66)!important;
      padding:0 46px 0 16px!important;color:#fff!important;box-shadow:none!important;backdrop-filter:blur(7px)!important
    }
    html.deapp-native-story-open .story-reply input::placeholder{color:rgba(255,255,255,.78)!important}
    html.deapp-native-story-open .story-send{width:34px!important;height:34px!important;right:5px!important;background:#25D366!important;color:#fff!important;box-shadow:none!important}
    html.deapp-native-story-open .story-act{height:44px!important;min-width:42px!important;width:auto!important;padding:0 8px!important;background:transparent!important;border-radius:22px!important;backdrop-filter:none!important}
    html.deapp-native-story-open .story-act-ico svg{width:24px!important;height:24px!important}
    html.deapp-native-story-open .story-views{bottom:82px!important;background:rgba(0,0,0,.40)!important}
    html.deapp-native-story-open .story-sheet{
      position:absolute!important;left:0!important;right:0!important;bottom:0!important;margin:0!important;z-index:50!important;
      max-height:min(66dvh,560px)!important;border-radius:22px 22px 0 0!important;border:0!important;background:#171717!important;
      box-shadow:0 -12px 42px rgba(0,0,0,.42)!important;padding-bottom:env(safe-area-inset-bottom)!important
    }
    html.deapp-native-story-open .story-sheet:before{content:"";display:block;width:38px;height:4px;border-radius:99px;background:#777;margin:8px auto 2px}
    html.deapp-native-story-open .story-quick{left:10px!important;right:10px!important;bottom:calc(60px + env(safe-area-inset-bottom))!important;background:rgba(30,30,30,.96)!important;border-color:rgba(255,255,255,.13)!important}

    /* v1.9.11 — Pengaturan lebih sederhana: tanpa footer khusus, header hanya kembali + judul tengah. */
    html.deapp-native-settings-family.deapp-has-section-chrome body{padding-bottom:18px!important}
    html.deapp-native-settings-family .deapp-section-footer{display:none!important}
    html.deapp-native-settings-family .deapp-section-header{grid-template-columns:44px minmax(0,1fr) 44px!important}
    html.deapp-native-settings-family .deapp-section-header .deapp-section-action{visibility:hidden!important;pointer-events:none!important}
    html.deapp-native-settings-family .settings-wrap{display:block!important;width:min(100%,760px)!important;margin:0 auto!important;padding:0 10px 24px!important}
    html.deapp-native-settings-family .settings-nav{
      position:relative!important;top:auto!important;display:flex!important;gap:7px!important;width:100%!important;margin:8px 0 12px!important;padding:7px 2px!important;
      overflow-x:auto!important;border:0!important;border-radius:0!important;box-shadow:none!important;background:transparent!important;backdrop-filter:none!important
    }
    html.deapp-native-settings-family .settings-nav .snav{
      flex:0 0 auto!important;min-height:38px!important;padding:8px 12px!important;border-radius:999px!important;border:1px solid var(--border)!important;
      background:var(--surface-2)!important;font-size:12.5px!important
    }
    html.deapp-native-settings-family .settings-nav .snav svg{width:17px!important;height:17px!important}
    html.deapp-native-settings-family .settings-nav .snav span{display:block!important}
    html.deapp-native-settings-family .settings-nav .snav b{font-size:12.5px!important;font-weight:720!important}
    html.deapp-native-settings-family .settings-nav .snav small,html.deapp-native-settings-family .settings-nav .snav-group{display:none!important}
    html.deapp-native-settings-family .settings-nav .snav.active{background:var(--text)!important;color:var(--surface)!important;border-color:var(--text)!important}
    html.deapp-native-settings-family .settings-body{min-width:0!important}
    html.deapp-native-settings-family .settings-section,
    html.deapp-native-settings-family .security-card,
    html.deapp-native-settings-family .card.settings-section{
      margin:0 0 10px!important;padding:15px!important;border:1px solid color-mix(in srgb,var(--border) 82%,transparent)!important;
      border-radius:18px!important;box-shadow:none!important;background:var(--surface)!important
    }
    html.deapp-native-settings-family .settings-section h3{font-size:16px!important;font-weight:800!important;margin-bottom:10px!important}
    html.deapp-native-settings-family .toggle-row{border-bottom:1px solid color-mix(in srgb,var(--border) 72%,transparent)!important}
    html.deapp-native-settings-family .toggle-row:last-child{border-bottom:0!important}

    /* Mode gelap harus konsisten sampai chrome paling luar, bukan hanya isi halaman web. */
    html.is-dark,html.is-dark body{background:#000!important;color:#f5f5f5!important;color-scheme:dark!important}
    html.is-dark .deapp-section-header,html.is-dark .deapp-section-footer,
    html.is-dark .deapp-reels-header,html.is-dark .deapp-reels-footer,
    html.is-dark .deapp-ai-composer-wrap,html.is-dark .chat-head,html.is-dark .chat-compose{
      background:rgba(0,0,0,.94)!important;border-color:#252525!important;color:#f5f5f5!important
    }
    html.is-dark .post-card:not(.post-embedded),html.is-dark .page-home .feed,html.is-dark .page-home .feed-tabs,
    html.is-dark .settings-section,html.is-dark .card.settings-section{background:#000!important;border-color:#252525!important}
    html.is-dark .settings-nav .snav{background:#171717!important;border-color:#2a2a2a!important;color:#f3f3f3!important}
    html.is-dark .settings-nav .snav.active{background:#f5f5f5!important;color:#080808!important;border-color:#f5f5f5!important}

    /* Saat halaman sedang bergerak, aksi sekunder/floating menyingkir; muncul lagi setelah scroll berhenti. */
    html.deapp-native-scrolling .deapp-ai-fab,
    html.deapp-native-scrolling .deapp-reels-header-create,
    html.deapp-native-scrolling .deapp-section-action,
    html.deapp-native-scrolling .deapp-native-scroll-hide{
      opacity:0!important;visibility:hidden!important;pointer-events:none!important;transform:translateY(10px) scale(.96)!important;
      transition:opacity .12s ease,transform .14s ease!important
    }

    /* Postingan: tipografi, media, statistik dan action dirapikan tanpa mengubah handler. */
    .post-card:not(.post-embedded) .post-content{letter-spacing:-.006em!important;line-height:1.52!important}
    .post-card:not(.post-embedded)>.media-grid,.post-card:not(.post-embedded) .media-grid,
    .post-card:not(.post-embedded) .post-bg{border-radius:15px!important;box-shadow:inset 0 0 0 1px color-mix(in srgb,var(--border) 55%,transparent)!important}
    .post-card:not(.post-embedded) .post-stats{min-height:28px!important;padding-top:5px!important;padding-bottom:4px!important;color:var(--text-muted)!important}
    .post-card:not(.post-embedded) .post-actions{min-height:46px!important;gap:3px!important;padding-top:2px!important;padding-bottom:4px!important}
    .post-card:not(.post-embedded) .act-btn{width:38px!important;height:38px!important;border-radius:50%!important;display:grid!important;place-items:center!important;padding:0!important}
    .post-card:not(.post-embedded) .act-btn:active{background:var(--surface-2)!important;transform:scale(.94)!important}
    .post-card:not(.post-embedded) .act-btn.on,.post-card:not(.post-embedded) .js-react-btn.on{color:#e83e66!important}
    .post-card:not(.post-embedded) .post-head{padding-top:14px!important}
    .post-card:not(.post-embedded) .post-name{letter-spacing:-.012em!important}


    /* v1.9.12 — ikon/navigasi lebih profesional, Toko & Chat tanpa footer khusus,
       daftar chat dan badan percakapan bergaya WhatsApp. */
    .deapp-section-header svg,.deapp-section-footer svg,.dropdown-item svg,.settings-nav svg,
    .shop-menu-grid svg,.menu-grid svg,.profile-menu svg,.nx-menu svg{stroke-width:1.8!important;shape-rendering:geometricPrecision}
    .dropdown-item svg,.settings-nav .snav svg{flex:0 0 auto!important}

    html.deapp-native-shop.deapp-has-section-chrome body{padding-bottom:18px!important}
    html.deapp-native-shop .deapp-section-footer{display:none!important}
    html.deapp-native-shop .shop-menu-grid a,html.deapp-native-shop .shop-menu-grid button{
      border-radius:17px!important;border:1px solid color-mix(in srgb,var(--border) 78%,transparent)!important;
      box-shadow:none!important;transition:transform .13s ease,background .13s ease!important
    }
    html.deapp-native-shop .shop-menu-grid a:active,html.deapp-native-shop .shop-menu-grid button:active{transform:scale(.97)!important;background:var(--surface-2)!important}

    html.deapp-native-messages.deapp-chat-list.deapp-has-section-chrome body{padding-bottom:12px!important;background:var(--surface)!important}
    html.deapp-native-messages.deapp-chat-list .deapp-section-footer{display:none!important}
    html.deapp-native-messages.deapp-chat-list .messenger{display:block!important;height:auto!important;min-height:calc(100dvh - 62px - env(safe-area-inset-top))!important;margin:0!important}
    html.deapp-native-messages.deapp-chat-list .msg-chat{display:none!important}
    html.deapp-native-messages.deapp-chat-list .msg-list{
      width:100%!important;max-width:none!important;height:auto!important;min-height:calc(100dvh - 62px - env(safe-area-inset-top))!important;
      margin:0!important;padding:0 0 18px!important;border:0!important;border-radius:0!important;box-shadow:none!important;background:var(--surface)!important
    }
    html.deapp-native-messages.deapp-chat-list .msg-search{
      margin:9px 12px 7px!important;min-height:42px!important;border:0!important;border-radius:13px!important;
      background:color-mix(in srgb,var(--surface-2) 94%,transparent)!important;padding:0 13px!important;color:var(--text-muted)!important
    }
    html.deapp-native-messages.deapp-chat-list .msg-search input{font-size:14.5px!important;background:transparent!important;border:0!important;box-shadow:none!important}
    html.deapp-native-messages.deapp-chat-list .msg-filter{
      display:flex!important;gap:7px!important;margin:0!important;padding:3px 12px 8px!important;overflow-x:auto!important;scrollbar-width:none!important
    }
    html.deapp-native-messages.deapp-chat-list .msg-filter::-webkit-scrollbar{display:none!important}
    html.deapp-native-messages.deapp-chat-list .mfilter{
      flex:0 0 auto!important;min-height:33px!important;padding:6px 12px!important;border-radius:999px!important;
      border:1px solid color-mix(in srgb,var(--border) 88%,transparent)!important;background:var(--surface)!important;color:var(--text-muted)!important;
      font-size:12.2px!important;font-weight:680!important
    }
    html.deapp-native-messages.deapp-chat-list .mfilter.active{background:#e7fce8!important;border-color:#b9edbe!important;color:#128c35!important}
    html.is-dark.deapp-native-messages.deapp-chat-list .mfilter.active{background:#113a22!important;border-color:#245d38!important;color:#62d985!important}
    html.deapp-native-messages.deapp-chat-list .thread-list{display:block!important;padding:0!important}
    html.deapp-native-messages.deapp-chat-list .thread{
      position:relative!important;display:grid!important;grid-template-columns:58px minmax(0,1fr) auto!important;align-items:center!important;
      gap:11px!important;min-height:76px!important;margin:0!important;padding:8px 12px!important;border:0!important;border-radius:0!important;
      background:var(--surface)!important;color:var(--text)!important;box-shadow:none!important
    }
    html.deapp-native-messages.deapp-chat-list .thread:after{
      content:"";position:absolute;left:81px;right:0;bottom:0;height:1px;background:color-mix(in srgb,var(--border) 68%,transparent)
    }
    html.deapp-native-messages.deapp-chat-list .thread:last-child:after{display:none}
    html.deapp-native-messages.deapp-chat-list .thread:active{background:var(--surface-2)!important}
    html.deapp-native-messages.deapp-chat-list .thread-avatar{width:54px!important;height:54px!important;flex:0 0 54px!important}
    html.deapp-native-messages.deapp-chat-list .thread-avatar img{width:54px!important;height:54px!important;border-radius:50%!important;object-fit:cover!important}
    html.deapp-native-messages.deapp-chat-list .thread-avatar .online-dot{width:12px!important;height:12px!important;right:1px!important;bottom:1px!important;background:#25D366!important;border:2px solid var(--surface)!important}
    html.deapp-native-messages.deapp-chat-list .thread-body{display:block!important;min-width:0!important}
    html.deapp-native-messages.deapp-chat-list .thread-top{display:flex!important;align-items:center!important;gap:4px!important;min-width:0!important}
    html.deapp-native-messages.deapp-chat-list .thread-top b{font-size:15.5px!important;font-weight:760!important;letter-spacing:-.012em!important}
    html.deapp-native-messages.deapp-chat-list .thread-time{margin-left:auto!important;font-size:10.8px!important;color:var(--text-faint)!important;white-space:nowrap!important}
    html.deapp-native-messages.deapp-chat-list .thread-preview{display:block!important;margin-top:3px!important;font-size:13px!important;color:var(--text-muted)!important;line-height:1.3!important}
    html.deapp-native-messages.deapp-chat-list .thread-preview.unread{font-weight:720!important;color:var(--text)!important}
    html.deapp-native-messages.deapp-chat-list .thread[data-unread="1"] .thread-time{color:#25a244!important;font-weight:720!important}
    html.deapp-native-messages.deapp-chat-list .thread-mood{display:none!important}
    html.deapp-native-messages.deapp-chat-list .thread>.pill-count{
      min-width:20px!important;height:20px!important;padding:0 5px!important;border-radius:999px!important;background:#25D366!important;color:#fff!important;
      display:grid!important;place-items:center!important;font-size:10px!important;font-weight:800!important
    }

    html.deapp-native-messages.deapp-chat-open .chat-scroll{
      background-color:#efeae2!important;
      background-image:radial-gradient(circle at 15% 20%,rgba(120,110,92,.055) 0 1px,transparent 1.4px),radial-gradient(circle at 78% 65%,rgba(120,110,92,.045) 0 1px,transparent 1.4px)!important;
      background-size:28px 28px,34px 34px!important;padding:13px 8px 16px!important;gap:2px!important
    }
    html.is-dark.deapp-native-messages.deapp-chat-open .chat-scroll{
      background-color:#0b141a!important;
      background-image:radial-gradient(circle at 15% 20%,rgba(255,255,255,.028) 0 1px,transparent 1.4px),radial-gradient(circle at 78% 65%,rgba(255,255,255,.022) 0 1px,transparent 1.4px)!important
    }
    html.deapp-native-messages.deapp-chat-open .chat-mood{background:#f0f2f5!important;color:#667781!important;border:0!important}
    html.is-dark.deapp-native-messages.deapp-chat-open .chat-mood{background:#111b21!important;color:#8696a0!important}
    html.deapp-native-messages.deapp-chat-open .bubble{
      max-width:min(84%,520px)!important;padding:7px 9px 5px!important;border-radius:8px!important;font-size:14.2px!important;line-height:1.4!important;
      box-shadow:0 1px 1px rgba(0,0,0,.08)!important
    }
    html.deapp-native-messages.deapp-chat-open .bubble.them:not(.is-gift):not(.is-sticker){background:#fff!important;color:#111b21!important;border-radius:8px!important}
    html.deapp-native-messages.deapp-chat-open .bubble.me:not(.is-gift):not(.is-sticker){background:#d9fdd3!important;color:#111b21!important;border-radius:8px!important}
    html.is-dark.deapp-native-messages.deapp-chat-open .bubble.them:not(.is-gift):not(.is-sticker){background:#202c33!important;color:#e9edef!important}
    html.is-dark.deapp-native-messages.deapp-chat-open .bubble.me:not(.is-gift):not(.is-sticker){background:#005c4b!important;color:#e9edef!important}
    html.deapp-native-messages.deapp-chat-open .bubble.me+.bubble.me{border-top-right-radius:4px!important}
    html.deapp-native-messages.deapp-chat-open .bubble.them+.bubble.them{border-top-left-radius:4px!important}
    html.deapp-native-messages.deapp-chat-open .bubble-time{font-size:9.2px!important;color:#667781!important;opacity:.92!important;margin-top:2px!important}
    html.is-dark.deapp-native-messages.deapp-chat-open .bubble-time{color:#8696a0!important}
    html.deapp-native-messages.deapp-chat-open .chat-day{
      width:max-content!important;max-width:85%!important;margin:10px auto!important;padding:5px 10px!important;border-radius:7px!important;
      background:#fff!important;color:#667781!important;box-shadow:0 1px 1px rgba(0,0,0,.06)!important;font-size:10.4px!important
    }
    html.is-dark.deapp-native-messages.deapp-chat-open .chat-day{background:#182229!important;color:#8696a0!important}
    html.deapp-native-messages.deapp-chat-open .chat-head{background:#f0f2f5!important;border-bottom:0!important}
    html.is-dark.deapp-native-messages.deapp-chat-open .chat-head{background:#202c33!important;border-bottom:0!important}
    html.deapp-native-messages.deapp-chat-open .chat-compose{background:#f0f2f5!important;border-top:0!important;padding:6px 7px calc(6px + env(safe-area-inset-bottom))!important}
    html.is-dark.deapp-native-messages.deapp-chat-open .chat-compose{background:#202c33!important;border-top:0!important}
    html.deapp-native-messages.deapp-chat-open .chat-input-wrap{background:#fff!important;border:0!important;border-radius:22px!important}
    html.is-dark.deapp-native-messages.deapp-chat-open .chat-input-wrap{background:#2a3942!important;border:0!important}
    html.deapp-native-messages.deapp-chat-open .chat-quick{padding:3px 2px 4px!important}
    html.deapp-native-messages.deapp-chat-open .quick-react{font-size:19px!important}

    /* v1.9.14 — Threads-style compact counters + flat notification list. */
    .post-card:not(.post-embedded) .act-btn.deapp-counted-action{
      min-width:40px!important;padding:7px 6px!important;gap:3px!important;border-radius:20px!important
    }
    .post-card:not(.post-embedded) .deapp-action-count{
      min-width:0!important;font-size:11px!important;line-height:1!important;font-weight:650!important;letter-spacing:-.01em!important;color:var(--text-muted)!important
    }
    .post-card:not(.post-embedded) .act-btn svg{width:20px!important;height:20px!important}
    .post-card:not(.post-embedded) .act-emoji{font-size:19px!important}

    html.deapp-native-notifications .notif-layout{display:block!important;margin:0!important;padding:0!important}
    html.deapp-native-notifications .notif-main{width:100%!important;max-width:none!important;margin:0!important;padding:0!important}
    html.deapp-native-notifications .notif-side{display:none!important}
    html.deapp-native-notifications .notif-card{
      padding:0!important;margin:0!important;border:0!important;border-radius:0!important;background:transparent!important;box-shadow:none!important;overflow:visible!important
    }
    html.deapp-native-notifications .notif-item{
      position:relative!important;display:flex!important;align-items:center!important;gap:12px!important;min-height:70px!important;
      margin:0!important;padding:11px 14px!important;border-radius:0!important;background:transparent!important;color:var(--text)!important;box-shadow:none!important
    }
    html.deapp-native-notifications .notif-item:after{
      content:""!important;position:absolute!important;left:72px!important;right:0!important;bottom:0!important;height:1px!important;background:color-mix(in srgb,var(--border) 72%,transparent)!important
    }
    html.deapp-native-notifications .notif-item:last-child:after{display:none!important}
    html.deapp-native-notifications .notif-item.unread{background:color-mix(in srgb,var(--acc) 6%,var(--surface))!important}
    html.deapp-native-notifications .notif-item:active{background:var(--surface-2)!important}
    html.deapp-native-notifications .notif-avatar>img,html.deapp-native-notifications .notif-sysav{width:46px!important;height:46px!important}
    html.deapp-native-notifications .notif-text{font-size:14px!important;line-height:1.38!important;font-weight:560!important}
    html.deapp-native-notifications .notif-item.unread .notif-text{font-weight:700!important}
    html.deapp-native-notifications .notif-time{font-size:11px!important;color:var(--text-faint)!important;margin-top:3px!important}
    html.deapp-native-notifications .unread-dot{width:7px!important;height:7px!important;flex:0 0 7px!important;margin-left:auto!important}
    html.deapp-native-notifications .mention-list{margin:0!important;padding:0!important}
    html.deapp-native-notifications .mention-card{
      position:relative!important;margin:0!important;padding:12px 14px!important;border:0!important;border-radius:0!important;background:transparent!important;box-shadow:none!important
    }
    html.deapp-native-notifications .mention-card:after{
      content:"";position:absolute;left:72px;right:0;bottom:0;height:1px;background:color-mix(in srgb,var(--border) 72%,transparent)
    }
    html.deapp-native-notifications .nx-filter{
      margin:0!important;padding:8px 10px!important;border:0!important;border-radius:0!important;background:var(--surface)!important;box-shadow:none!important;
      border-bottom:1px solid color-mix(in srgb,var(--border) 72%,transparent)!important
    }
    html.deapp-native-notifications .deapp-notification-tabs{top:calc(58px + env(safe-area-inset-top))!important;padding-top:6px!important;padding-bottom:6px!important}

    /* v1.9.16 — Pengaturan menjadi daftar menu utama + subhalaman mandiri. */
    html.deapp-native-settings-family .page-head{display:none!important}
    html.deapp-native-settings-family.deapp-settings-index .settings-wrap{width:min(100%,760px)!important;margin:0 auto!important;padding:6px 0 28px!important}
    html.deapp-native-settings-family.deapp-settings-index .settings-body{display:none!important}
    html.deapp-native-settings-family.deapp-settings-index .settings-nav{display:block!important;width:100%!important;margin:0!important;padding:0 12px 22px!important;overflow:visible!important;border:0!important;border-radius:0!important;background:var(--surface)!important;box-shadow:none!important}
    html.deapp-native-settings-family.deapp-settings-index .settings-nav .snav-group{display:block!important;padding:19px 7px 7px!important;margin:0!important;color:var(--text-muted)!important;font-size:11.5px!important;font-weight:760!important;letter-spacing:.035em!important;text-transform:uppercase!important}
    html.deapp-native-settings-family.deapp-settings-index .settings-nav .snav{position:relative!important;display:grid!important;grid-template-columns:38px minmax(0,1fr) 22px!important;align-items:center!important;gap:11px!important;width:100%!important;min-height:62px!important;margin:0!important;padding:9px!important;border:0!important;border-radius:0!important;background:transparent!important;color:var(--text)!important;text-align:left!important;box-shadow:none!important;text-decoration:none!important}
    html.deapp-native-settings-family.deapp-settings-index .settings-nav .snav:after{content:"›"!important;grid-column:3!important;grid-row:1!important;justify-self:end!important;font-size:25px!important;line-height:1!important;font-weight:300!important;color:var(--text-faint)!important}
    html.deapp-native-settings-family.deapp-settings-index .settings-nav .snav:before{content:""!important;position:absolute!important;left:58px!important;right:0!important;bottom:0!important;height:1px!important;background:color-mix(in srgb,var(--border) 70%,transparent)!important}
    html.deapp-native-settings-family.deapp-settings-index .settings-nav .snav:last-child:before{display:none!important}
    html.deapp-native-settings-family.deapp-settings-index .settings-nav .snav>svg,html.deapp-native-settings-family.deapp-settings-index .settings-nav .snav>.deapp-settings-native-icon,html.deapp-native-settings-family.deapp-settings-index .settings-nav .snav>.deapp-settings-logout-icon{grid-column:1!important;grid-row:1!important;width:36px!important;height:36px!important;border-radius:11px!important;display:grid!important;place-items:center!important;background:var(--surface-2)!important;color:var(--text)!important;font-size:18px!important}
    html.deapp-native-settings-family.deapp-settings-index .settings-nav .snav>svg{padding:8px!important}
    html.deapp-native-settings-family.deapp-settings-index .settings-nav .snav>.deapp-settings-logout-icon svg{width:19px!important;height:19px!important}
    html.deapp-native-settings-family.deapp-settings-index .settings-nav .snav>span:not(.deapp-version-pill):not(.deapp-settings-native-icon):not(.deapp-settings-logout-icon){grid-column:2!important;grid-row:1!important;display:block!important;min-width:0!important}
    html.deapp-native-settings-family.deapp-settings-index .settings-nav .snav b{display:block!important;font-size:14.5px!important;font-weight:760!important;line-height:1.2!important;color:inherit!important}
    html.deapp-native-settings-family.deapp-settings-index .settings-nav .snav small{display:block!important;margin-top:3px!important;font-size:11.5px!important;line-height:1.25!important;color:var(--text-muted)!important;white-space:normal!important}
    html.deapp-native-settings-family.deapp-settings-index .settings-nav .snav.active{background:transparent!important;color:var(--text)!important;border:0!important}
    html.deapp-native-settings-family.deapp-settings-index .settings-nav .deapp-version-pill{position:absolute!important;right:28px!important;top:50%!important;transform:translateY(-50%)!important;font-size:10px!important;color:var(--text-muted)!important;background:var(--surface-2)!important;border-radius:999px!important;padding:4px 7px!important}
    html.deapp-native-settings-family.deapp-settings-index .deapp-native-logout-group{margin-top:14px!important}
    html.deapp-native-settings-family.deapp-settings-index .deapp-native-logout-row{color:#d92d20!important}
    html.deapp-native-settings-family.deapp-settings-index .deapp-native-logout-row:after{color:#d92d20!important}
    html.deapp-native-settings-family.deapp-settings-index .deapp-native-logout-row>.deapp-settings-logout-icon{background:color-mix(in srgb,#d92d20 10%,var(--surface))!important;color:#d92d20!important}
    html.deapp-native-settings-family.deapp-settings-subpage .settings-wrap{display:block!important;width:min(100%,760px)!important;margin:0 auto!important;padding:8px 10px 30px!important}
    html.deapp-native-settings-family.deapp-settings-subpage .settings-nav{display:none!important}
    html.deapp-native-settings-family.deapp-settings-subpage .settings-body{display:block!important;width:100%!important;min-width:0!important;margin:0!important}
    html.deapp-native-settings-family.deapp-settings-subpage .settings-section,html.deapp-native-settings-family.deapp-settings-subpage .security-card,html.deapp-native-settings-family.deapp-settings-subpage .card.settings-section{border-radius:16px!important;padding:15px 14px!important;margin-bottom:10px!important}

    /* v1.9.16 — Deapp AI Lite: fokus pesan + composer, tanpa rail/menu/action bar. */
    html.deapp-native-ai .deapp-section-title small,html.deapp-native-ai .deapp-ai-hero,html.deapp-native-ai .deapp-ai-nav,html.deapp-native-ai .deapp-ai-thread-rail,html.deapp-native-ai .deapp-ai-chat-head,html.deapp-native-ai .deapp-ai-control-strip,html.deapp-native-ai .deapp-ai-fab,html.deapp-native-ai .deapp-ai-native-menu,html.deapp-native-ai .deapp-ai-native-menu-backdrop{display:none!important}
    html.deapp-native-ai .deapp-section-header .deapp-section-action{visibility:hidden!important;pointer-events:none!important}
    html.deapp-native-ai .deapp-ai-view:not(#deapp-ai-view-chat){display:none!important}
    html.deapp-native-ai #deapp-ai-view-chat{display:block!important}
    html.deapp-native-ai .deapp-ai-shell{display:block!important;width:100%!important;max-width:none!important;height:calc(100dvh - 58px - env(safe-area-inset-top))!important;min-height:0!important;border:0!important;border-radius:0!important;background:var(--surface)!important}
    html.deapp-native-ai .deapp-ai-chat-panel{width:100%!important;height:100%!important;min-height:0!important;background:var(--surface)!important}
    html.deapp-native-ai .deapp-ai-messages{width:min(100%,820px)!important;margin:0 auto!important;padding:18px 12px 150px!important;scroll-padding-bottom:150px!important}
    html.deapp-native-ai .deapp-ai-followups{width:min(100%,820px)!important;margin:0 auto 126px!important;padding:0 12px!important}
    html.deapp-native-ai .deapp-ai-message{gap:10px!important;margin:0 0 17px!important;padding:0!important}
    html.deapp-native-ai .deapp-ai-message-avatar{width:30px!important;height:30px!important;flex:0 0 30px!important;border-radius:50%!important}
    html.deapp-native-ai .deapp-ai-message-body{min-width:0!important;max-width:calc(100% - 40px)!important}
    html.deapp-native-ai .deapp-ai-message-meta{margin-bottom:5px!important;font-size:11px!important}
    html.deapp-native-ai .deapp-ai-message-text{font-size:14.5px!important;line-height:1.55!important}
    html.deapp-native-ai .deapp-ai-message.user .deapp-ai-message-body{margin-left:auto!important;max-width:min(86%,660px)!important;padding:10px 13px!important;border-radius:18px 18px 5px 18px!important;background:var(--surface-2)!important}
    html.deapp-native-ai .deapp-ai-message.user .deapp-ai-message-avatar,html.deapp-native-ai .deapp-ai-message.user .deapp-ai-message-meta{display:none!important}
    html.deapp-native-ai[data-deapp-ai-view="chat"] .deapp-ai-composer-wrap,html.deapp-native-ai .deapp-ai-composer-wrap{position:fixed!important;left:0!important;right:0!important;bottom:0!important;z-index:4435!important;padding:8px 10px max(10px,env(safe-area-inset-bottom))!important;border-top:0!important;background:linear-gradient(to top,var(--surface) 74%,color-mix(in srgb,var(--surface) 90%,transparent))!important;backdrop-filter:blur(18px)!important;-webkit-backdrop-filter:blur(18px)!important;box-shadow:none!important}
    html.deapp-native-ai .deapp-ai-composer{display:block!important;width:min(100%,800px)!important;margin:0 auto!important;padding:7px!important;border:1px solid color-mix(in srgb,var(--border) 82%,transparent)!important;border-radius:25px!important;background:var(--surface)!important;box-shadow:0 5px 24px rgba(0,0,0,.07)!important}
    html.deapp-native-ai #ai-chat-input{width:100%!important;min-height:42px!important;max-height:160px!important;padding:10px 11px 7px!important;border:0!important;outline:0!important;background:transparent!important;color:var(--text)!important;font:inherit!important;font-size:15px!important;line-height:1.45!important;resize:none!important}
    html.deapp-native-ai #ai-chat-input::placeholder{color:var(--text-muted)!important}
    html.deapp-native-ai .deapp-ai-composer-bottom{display:flex!important;align-items:center!important;justify-content:space-between!important;gap:8px!important;padding:2px 2px 1px!important}
    html.deapp-native-ai .deapp-ai-composer-left,html.deapp-native-ai .deapp-ai-composer-right{display:flex!important;align-items:center!important;gap:4px!important}
    html.deapp-native-ai #ai-chat-count{display:none!important}
    html.deapp-native-ai #ai-chat-regenerate{min-width:36px!important;width:36px!important;height:36px!important;padding:0!important;border-radius:50%!important;font-size:0!important}
    html.deapp-native-ai #ai-chat-regenerate svg{width:17px!important;height:17px!important;margin:0!important}
    html.deapp-native-ai #ai-chat-attach,html.deapp-native-ai #ai-chat-mic{width:36px!important;height:36px!important;border-radius:50%!important;background:transparent!important;border:0!important;color:var(--text-muted)!important}
    html.deapp-native-ai #ai-chat-send{width:38px!important;height:38px!important;border-radius:50%!important;display:grid!important;place-items:center!important;background:var(--text)!important;color:var(--surface)!important;border:0!important;box-shadow:none!important}
    html.deapp-native-ai .deapp-ai-attachment-chip{width:min(100%,800px)!important;margin:6px auto 0!important;padding:7px 10px!important;border-radius:12px!important}
    html.deapp-native-ai .deapp-ai-disclaimer{display:none!important}
    html.is-dark.deapp-native-ai .deapp-ai-composer{background:#111!important;border-color:#2a2a2a!important;box-shadow:0 5px 24px rgba(0,0,0,.28)!important}



    /* v1.9.17 — Search ala Threads, popup tengah, action row compact, Story hold-to-pause. */
    html.deapp-native-explore .explore-head{
      margin:0!important;padding:9px 12px 7px!important;border:0!important;border-radius:0!important;
      background:var(--surface)!important;box-shadow:none!important
    }
    html.deapp-native-explore .explore-head .page-title{display:none!important}
    html.deapp-native-explore .explore-search{
      display:flex!important;align-items:center!important;gap:9px!important;width:100%!important;min-height:46px!important;
      padding:0 13px!important;border:1px solid color-mix(in srgb,var(--border) 76%,transparent)!important;border-radius:17px!important;
      background:var(--surface-2)!important;box-shadow:none!important
    }
    html.deapp-native-explore .explore-search>svg{width:19px!important;height:19px!important;color:var(--text-muted)!important;flex:0 0 auto!important}
    html.deapp-native-explore .explore-search input{
      flex:1 1 auto!important;width:auto!important;min-width:0!important;height:44px!important;padding:0!important;border:0!important;outline:0!important;
      background:transparent!important;color:var(--text)!important;font-size:15px!important;box-shadow:none!important
    }
    html.deapp-native-explore .explore-search .btn{display:none!important}
    html.deapp-native-explore .tabs.sticky-tabs{
      position:sticky!important;top:0!important;z-index:32!important;display:flex!important;gap:4px!important;margin:0!important;padding:4px 10px 8px!important;
      border:0!important;border-bottom:1px solid color-mix(in srgb,var(--border) 72%,transparent)!important;border-radius:0!important;background:var(--surface)!important;box-shadow:none!important
    }
    html.deapp-native-explore .tabs.sticky-tabs .tab{
      min-height:38px!important;padding:8px 12px!important;border:0!important;border-radius:0!important;background:transparent!important;
      color:var(--text-muted)!important;font-size:13px!important;font-weight:700!important;white-space:nowrap!important
    }
    html.deapp-native-explore .tabs.sticky-tabs .tab.active{color:var(--text)!important;border-bottom:2px solid var(--text)!important}
    html.deapp-native-explore .people-grid{display:block!important;margin:0!important;padding:0!important}
    html.deapp-native-explore .people-card{
      position:relative!important;display:grid!important;grid-template-columns:48px minmax(0,1fr) auto!important;align-items:center!important;gap:11px!important;
      min-height:74px!important;margin:0!important;padding:10px 12px!important;border:0!important;border-bottom:1px solid color-mix(in srgb,var(--border) 68%,transparent)!important;
      border-radius:0!important;background:var(--surface)!important;box-shadow:none!important;text-align:left!important
    }
    html.deapp-native-explore .people-card .people-cover{display:none!important}
    html.deapp-native-explore .people-avatar-link{grid-column:1!important;grid-row:1/5!important;align-self:start!important}
    html.deapp-native-explore .people-avatar{width:46px!important;height:46px!important;border-radius:50%!important}
    html.deapp-native-explore .people-name,html.deapp-native-explore .people-uname,html.deapp-native-explore .people-bio,html.deapp-native-explore .people-stats{grid-column:2!important;margin:0!important;text-align:left!important}
    html.deapp-native-explore .people-name{font-size:14px!important;font-weight:800!important}
    html.deapp-native-explore .people-uname,html.deapp-native-explore .people-stats{font-size:11.5px!important;color:var(--text-muted)!important}
    html.deapp-native-explore .people-bio{font-size:12.5px!important;line-height:1.35!important}
    html.deapp-native-explore .people-card>.btn{grid-column:3!important;grid-row:1/5!important;align-self:center!important;border-radius:999px!important}

    /* Popup ringan dipusatkan agar tidak bertabrakan dengan bottom navigation. */
    .nx-pop-toasts{
      left:50%!important;right:auto!important;top:50%!important;bottom:auto!important;transform:translate(-50%,-50%)!important;
      width:min(370px,calc(100vw - 32px))!important;max-height:min(70dvh,520px)!important;align-content:center!important;gap:9px!important
    }
    .nx-pop-toast{
      grid-template-columns:36px minmax(0,1fr) 28px!important;padding:12px 12px!important;border-radius:18px!important;
      background:color-mix(in srgb,var(--surface) 96%,transparent)!important;box-shadow:0 18px 60px rgba(0,0,0,.22)!important;
      backdrop-filter:blur(20px) saturate(135%)!important;-webkit-backdrop-filter:blur(20px) saturate(135%)!important;
      transform:translateY(8px) scale(.965)!important
    }
    .nx-pop-toast.show{transform:none!important}
    .nx-pop-toast.out{transform:translateY(6px) scale(.975)!important}
    #toast.toast{
      left:50%!important;right:auto!important;top:50%!important;bottom:auto!important;z-index:5200!important;
      min-width:min(280px,calc(100vw - 40px))!important;max-width:min(390px,calc(100vw - 32px))!important;
      padding:13px 16px!important;border:1px solid rgba(255,255,255,.12)!important;border-radius:18px!important;
      background:rgba(22,22,22,.94)!important;color:#fff!important;box-shadow:0 18px 60px rgba(0,0,0,.28)!important;
      backdrop-filter:blur(20px)!important;-webkit-backdrop-filter:blur(20px)!important;
      transform:translate(-50%,calc(-50% + 12px)) scale(.97)!important;text-align:center!important;justify-content:center!important
    }
    #toast.toast.show{transform:translate(-50%,-50%) scale(1)!important;opacity:1!important}
    #toast.toast.success{background:rgba(21,89,64,.96)!important}
    #toast.toast.error{background:rgba(139,35,35,.96)!important}

    /* Action postingan seperti Threads: ikon dan angka kecil sejajar, tanpa label. */
    .post-card:not(.post-embedded) .post-actions{
      display:flex!important;align-items:center!important;justify-content:flex-start!important;gap:10px!important;min-height:40px!important;
      padding-top:1px!important;padding-bottom:3px!important
    }
    .post-card:not(.post-embedded) .post-actions .react-wrap{display:flex!important;align-items:center!important;flex:0 0 auto!important}
    .post-card:not(.post-embedded) .act-btn{
      width:34px!important;min-width:34px!important;height:34px!important;min-height:34px!important;padding:6px!important;border-radius:18px!important
    }
    .post-card:not(.post-embedded) .act-btn.deapp-counted-action{
      display:inline-flex!important;align-items:center!important;justify-content:flex-start!important;flex:0 0 auto!important;
      width:auto!important;min-width:34px!important;height:34px!important;padding:5px 4px!important;gap:3px!important;border-radius:18px!important
    }
    .post-card:not(.post-embedded) .act-btn svg{width:18px!important;height:18px!important;stroke-width:1.8!important}
    .post-card:not(.post-embedded) .act-emoji{font-size:18px!important;line-height:1!important}
    .post-card:not(.post-embedded) .deapp-action-count{
      min-width:0!important;margin:0!important;padding:0!important;font-size:10.5px!important;line-height:1!important;font-weight:620!important;
      letter-spacing:-.015em!important;color:var(--text-muted)!important;font-variant-numeric:tabular-nums!important
    }
    .post-card:not(.post-embedded) .js-bookmark{margin-left:0!important}
    .post-card:not(.post-embedded) .post-gifts{margin-top:0!important;padding-top:0!important}

    /* Story: area tap lebar + composer bawah ala Instagram Stories. */
    html.deapp-native-story-open .story-nav{width:43%!important;z-index:24!important}
    html.deapp-native-story-open .story-bottom{
      padding:20px 10px calc(9px + env(safe-area-inset-bottom))!important;
      background:linear-gradient(to top,rgba(0,0,0,.78),rgba(0,0,0,.24),transparent)!important
    }
    html.deapp-native-story-open .story-bar{display:flex!important;align-items:center!important;gap:6px!important;max-width:720px!important}
    html.deapp-native-story-open .story-reply{flex:1 1 auto!important;min-width:0!important;position:relative!important}
    html.deapp-native-story-open .story-reply input{
      height:46px!important;border-radius:24px!important;background:rgba(18,18,18,.28)!important;border:1px solid rgba(255,255,255,.72)!important;
      padding:0 48px 0 16px!important;font-size:14px!important;color:#fff!important;box-shadow:none!important;
      backdrop-filter:blur(10px)!important;-webkit-backdrop-filter:blur(10px)!important
    }
    html.deapp-native-story-open .story-reply input:focus{border-color:#fff!important;background:rgba(18,18,18,.42)!important}
    html.deapp-native-story-open .story-send{
      width:36px!important;height:36px!important;right:5px!important;border-radius:50%!important;background:transparent!important;color:#fff!important;border:0!important
    }
    html.deapp-native-story-open .story-send svg{width:20px!important;height:20px!important}
    html.deapp-native-story-open .story-act{
      display:inline-flex!important;align-items:center!important;justify-content:center!important;gap:2px!important;height:44px!important;min-width:38px!important;
      padding:0 4px!important;color:#fff!important;background:transparent!important;border:0!important
    }
    html.deapp-native-story-open .story-act-n{font-size:10px!important;color:rgba(255,255,255,.82)!important}
    html.deapp-native-story-open .story-quick{
      left:12px!important;right:12px!important;bottom:calc(66px + env(safe-area-inset-bottom))!important;border-radius:22px!important;
      background:rgba(24,24,24,.94)!important;box-shadow:0 12px 36px rgba(0,0,0,.34)!important
    }
    html.deapp-native-story-open.deapp-story-press-paused .story-stage:after{
      content:""!important;position:absolute!important;inset:0!important;pointer-events:none!important;background:rgba(0,0,0,.035)!important
    }



    /* v1.9.18 — Telegram-style conversation body: tanpa body card, full-width canvas, composer ringan. */
    html.deapp-native-messages.deapp-chat-open,
    html.deapp-native-messages.deapp-chat-open body{
      background:#dce6ee!important
    }
    html.is-dark.deapp-native-messages.deapp-chat-open,
    html.is-dark.deapp-native-messages.deapp-chat-open body{
      background:#0f1b24!important
    }
    html.deapp-native-messages.deapp-chat-open .layout,
    html.deapp-native-messages.deapp-chat-open .wide-layout,
    html.deapp-native-messages.deapp-chat-open .main-col,
    html.deapp-native-messages.deapp-chat-open .messenger,
    html.deapp-native-messages.deapp-chat-open .msg-chat{
      width:100%!important;max-width:none!important;margin:0!important;padding:0!important;border:0!important;border-radius:0!important;
      background:transparent!important;box-shadow:none!important;outline:0!important
    }
    html.deapp-native-messages.deapp-chat-open .messenger{
      display:block!important;height:100dvh!important;min-height:100dvh!important;overflow:hidden!important
    }
    html.deapp-native-messages.deapp-chat-open .msg-chat{
      display:flex!important;flex-direction:column!important;height:100%!important;min-height:0!important;overflow:hidden!important
    }
    html.deapp-native-messages.deapp-chat-open .chat-scroll{
      flex:1 1 auto!important;min-height:0!important;width:100%!important;margin:0!important;
      padding:12px 8px 14px!important;gap:3px!important;background-color:#dce6ee!important;
      background-image:
        radial-gradient(circle at 20% 24%,rgba(72,116,145,.055) 0 1.1px,transparent 1.5px),
        radial-gradient(circle at 76% 66%,rgba(72,116,145,.045) 0 1.1px,transparent 1.5px)!important;
      background-size:34px 34px,42px 42px!important;box-shadow:none!important;border:0!important;overscroll-behavior-y:contain!important
    }
    html.is-dark.deapp-native-messages.deapp-chat-open .chat-scroll{
      background-color:#0f1b24!important;
      background-image:
        radial-gradient(circle at 20% 24%,rgba(255,255,255,.025) 0 1px,transparent 1.4px),
        radial-gradient(circle at 76% 66%,rgba(255,255,255,.02) 0 1px,transparent 1.4px)!important
    }
    html.deapp-native-messages.deapp-chat-open .bubble{
      max-width:min(80%,540px)!important;margin-top:1px!important;padding:7px 10px 6px!important;border:0!important;border-radius:16px!important;
      font-size:14.3px!important;line-height:1.42!important;box-shadow:0 1px 1px rgba(41,72,91,.12)!important;animation:none!important
    }
    html.deapp-native-messages.deapp-chat-open .bubble.them:not(.is-gift):not(.is-sticker){
      background:#fff!important;color:#182533!important;border-radius:16px 16px 16px 6px!important
    }
    html.deapp-native-messages.deapp-chat-open .bubble.me:not(.is-gift):not(.is-sticker){
      background:#d9efff!important;color:#182533!important;border-radius:16px 16px 6px 16px!important
    }
    html.is-dark.deapp-native-messages.deapp-chat-open .bubble.them:not(.is-gift):not(.is-sticker){
      background:#182533!important;color:#f2f5f7!important
    }
    html.is-dark.deapp-native-messages.deapp-chat-open .bubble.me:not(.is-gift):not(.is-sticker){
      background:#2b5278!important;color:#fff!important
    }
    html.deapp-native-messages.deapp-chat-open .bubble.me+.bubble.me{border-top-right-radius:7px!important}
    html.deapp-native-messages.deapp-chat-open .bubble.them+.bubble.them{border-top-left-radius:7px!important}
    html.deapp-native-messages.deapp-chat-open .bubble-time{
      margin:2px 0 0 6px!important;font-size:9.3px!important;line-height:1!important;color:#7d909d!important;opacity:.9!important
    }
    html.is-dark.deapp-native-messages.deapp-chat-open .bubble-time{color:#9ab0bf!important}
    html.deapp-native-messages.deapp-chat-open .bubble-img{border-radius:12px!important;margin:-3px -6px 5px!important}
    html.deapp-native-messages.deapp-chat-open .chat-day{
      margin:10px auto!important;padding:5px 10px!important;border:0!important;border-radius:12px!important;
      background:rgba(255,255,255,.76)!important;color:#607d8b!important;box-shadow:0 1px 2px rgba(41,72,91,.08)!important;
      font-size:10px!important;font-weight:700!important;backdrop-filter:blur(8px)!important;-webkit-backdrop-filter:blur(8px)!important
    }
    html.is-dark.deapp-native-messages.deapp-chat-open .chat-day{background:rgba(24,37,51,.84)!important;color:#9ab0bf!important}
    html.deapp-native-messages.deapp-chat-open .chat-mood{
      margin:0!important;padding:5px 10px!important;border:0!important;background:transparent!important;color:#607d8b!important;font-size:10.5px!important
    }
    html.is-dark.deapp-native-messages.deapp-chat-open .chat-mood{background:transparent!important;color:#9ab0bf!important}
    html.deapp-native-messages.deapp-chat-open .chat-compose{
      flex:0 0 auto!important;margin:0!important;padding:6px 7px calc(7px + env(safe-area-inset-bottom))!important;border:0!important;
      background:transparent!important;box-shadow:none!important;backdrop-filter:none!important;-webkit-backdrop-filter:none!important
    }
    html.deapp-native-messages.deapp-chat-open .chat-quick{display:none!important}
    html.deapp-native-messages.deapp-chat-open .chat-form{
      display:flex!important;align-items:flex-end!important;gap:6px!important;width:100%!important;margin:0!important;padding:0!important;border:0!important;background:transparent!important
    }
    html.deapp-native-messages.deapp-chat-open .chat-input-wrap{
      flex:1 1 auto!important;min-width:0!important;min-height:46px!important;border:0!important;border-radius:24px!important;
      background:#fff!important;box-shadow:0 1px 3px rgba(41,72,91,.14)!important;overflow:hidden!important
    }
    html.is-dark.deapp-native-messages.deapp-chat-open .chat-input-wrap{background:#182533!important;box-shadow:0 1px 3px rgba(0,0,0,.24)!important}
    html.deapp-native-messages.deapp-chat-open .chat-input-wrap:focus-within{border:0!important;background:#fff!important;box-shadow:0 1px 4px rgba(51,144,236,.18)!important}
    html.is-dark.deapp-native-messages.deapp-chat-open .chat-input-wrap:focus-within{background:#182533!important;box-shadow:0 1px 4px rgba(51,144,236,.18)!important}
    html.deapp-native-messages.deapp-chat-open #chat-input{
      min-height:46px!important;height:46px;max-height:132px!important;padding:11px 12px!important;border:0!important;background:transparent!important;
      font-size:15px!important;line-height:1.42!important;box-shadow:none!important;resize:none!important
    }
    html.deapp-native-messages.deapp-chat-open .chat-form>.icon-btn,
    html.deapp-native-messages.deapp-chat-open .chat-form>label.icon-btn{
      flex:0 0 42px!important;width:42px!important;height:42px!important;margin:2px 0!important;border:0!important;border-radius:50%!important;
      background:transparent!important;color:#6f8797!important;box-shadow:none!important
    }
    html.is-dark.deapp-native-messages.deapp-chat-open .chat-form>.icon-btn,
    html.is-dark.deapp-native-messages.deapp-chat-open .chat-form>label.icon-btn{color:#9ab0bf!important}
    html.deapp-native-messages.deapp-chat-open .chat-form>.icon-btn:active,
    html.deapp-native-messages.deapp-chat-open .chat-form>label.icon-btn:active{background:rgba(51,144,236,.10)!important;color:#3390ec!important}
    html.deapp-native-messages.deapp-chat-open .chat-form .btn-send.lg{
      flex:0 0 44px!important;width:44px!important;height:44px!important;margin:1px 0!important;border:0!important;border-radius:50%!important;
      background:#3390ec!important;color:#fff!important;box-shadow:0 2px 7px rgba(51,144,236,.28)!important
    }
    html.deapp-native-messages.deapp-chat-open .chat-form .btn-send.lg:active{transform:scale(.94)!important;background:#2c83d8!important}
    html.deapp-native-messages.deapp-chat-open .chat-form .btn-send.lg svg{width:20px!important;height:20px!important}

    @media(max-width:560px){
      .deapp-section-header{grid-template-columns:44px minmax(0,1fr) 44px;padding-left:6px;padding-right:6px}
      .deapp-section-header .deapp-section-left,.deapp-section-header .deapp-section-action{width:40px;height:40px}
      html.deapp-native-settings-family .settings-nav{border-radius:0!important;border-left:0!important;border-right:0!important}
      html.deapp-native-ai .deapp-ai-hero{display:none!important}
      html.deapp-native-ai .deapp-ai-shell{height:calc(100dvh - 62px - env(safe-area-inset-top))!important;min-height:0!important;border:0!important;border-radius:0!important}
      html.deapp-native-shop .shop-menu-grid{margin-top:4px!important}
    }
  `;
  document.head.appendChild(style);

  function nativeTap() {
    try { if (API && API.tap) API.tap(); } catch (_) {}
  }

  function actionable(node) {
    return node && node.closest ? node.closest('a,button,[role="button"],.btn,.icon-btn,.tab,.dropdown-item,.nav-link,.post-card[data-url],.post-card[onclick],.qa-card[data-url],.qa-card[onclick],.post-action,.comment-action') : null;
  }

  document.addEventListener('click', function (e) {
    if (e.isTrusted && actionable(e.target)) nativeTap();
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

  function visibleOpenPostMenu() {
    const menus = document.querySelectorAll('.post-card .menu-wrap.open>.dropdown');
    for (const menu of menus) {
      if (!menu || !menu.isConnected) continue;
      let node = menu, hidden = false;
      while (node && node.nodeType === 1) {
        const cs = getComputedStyle(node);
        if (cs.display === 'none' || cs.visibility === 'hidden') { hidden = true; break; }
        node = node.parentElement;
      }
      if (hidden) continue;
      const r = menu.getBoundingClientRect();
      if (r.width > 0 && r.height > 0 && r.bottom > 0 && r.top < innerHeight) return menu;
    }
    return null;
  }

  function ensurePostBackdrop() {
    const open = visibleOpenPostMenu();
    if (!open) {
      removePostBackdrop();
      return;
    }
    const existing = document.querySelector('.deapp-post-sheet-backdrop');
    if (existing) {
      existing.dataset.deappForVisibleMenu = '1';
      return;
    }
    const b = document.createElement('div');
    b.className = 'deapp-post-sheet-backdrop';
    b.dataset.deappForVisibleMenu = '1';
    b.addEventListener('click', function () {
      const active = visibleOpenPostMenu();
      if (active) closePostMenu(active);
      else removePostBackdrop();
    });
    document.body.appendChild(b);
  }

  function closeSheet(box) {
    if (!box) return;
    if (box.classList && box.classList.contains('deapp-native-profile-options-sheet')) { closeProfileOptions(); return; }
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

  function composerIsOpen() {
    const modal = document.getElementById('composer-modal');
    return !!(modal && modal.classList.contains('open'));
  }

  function openComposer() {
    const modal = document.getElementById('composer-modal');
    if (!modal) return false;
    const trigger = document.querySelector('[data-open-modal="composer-modal"]');
    if (trigger) trigger.click();
    else {
      modal.classList.add('open');
      document.body.classList.add('no-scroll');
    }
    setTimeout(function(){
      const ta = document.getElementById('composer-content');
      if (ta) ta.focus({preventScroll:true});
      syncComposer();
    }, 80);
    return true;
  }

  function closeComposer() {
    const modal = document.getElementById('composer-modal');
    if (!modal) return false;
    const close = modal.querySelector('.modal-close');
    if (close) close.click();
    else {
      modal.classList.remove('open');
      document.body.classList.remove('no-scroll');
    }
    syncComposer();
    return true;
  }

  function submitComposer() {
    const modal = document.getElementById('composer-modal');
    const form = document.getElementById('composer-form') || (modal ? modal.querySelector('form') : null);
    if (!form || composerSubmitting) return !!form;

    const submit = document.getElementById('composer-submit') || form.querySelector('.composer-submit,[data-action="publish"],[data-action="submit-post"],button[type="submit"],input[type="submit"]');
    if (submit && submit.disabled) return false;
    try {
      if (typeof form.checkValidity === 'function' && !form.checkValidity()) {
        if (typeof form.reportValidity === 'function') form.reportValidity();
        return false;
      }
    } catch (_) {}

    composerSubmitting = true;
    let released = false;
    const release = function(){
      if (released) return;
      released = true;
      composerSubmitting = false;
    };
    setTimeout(release, 2200);
    form.addEventListener('submit', function(){ setTimeout(release, 900); }, {once:true,capture:true});

    try {
      if (typeof form.requestSubmit === 'function') {
        if (submit && submit.form === form) form.requestSubmit(submit);
        else form.requestSubmit();
        return true;
      }
    } catch (_) {}

    try {
      if (submit) {
        submit.click();
        return true;
      }
    } catch (_) {}

    try {
      const ev = new Event('submit', {bubbles:true,cancelable:true});
      const allowed = form.dispatchEvent(ev);
      if (allowed && typeof form.submit === 'function') form.submit();
      return true;
    } catch (_) {
      release();
      return false;
    }
  }

  function syncComposer() {
    try {
      const open = composerIsOpen();
      if (open === lastComposerState) return;
      lastComposerState = open;
      if (API && API.syncComposerState) API.syncComposerState(open);
    } catch (_) {}
  }

  function profileTitle() {
    // Header profil memakai username/handle seperti aplikasi sosial modern.
    const u = document.querySelector('.profile-username');
    if (u) {
      const text = (u.textContent || '').trim().replace(/\s+/g,' ');
      const m = text.match(/@[A-Za-z0-9_.-]+/);
      if (m) return m[0];
    }
    const n = document.querySelector('.profile-name > span,.profile-name');
    return n ? (n.textContent || '').trim().replace(/\s+/g,' ') : 'Profil';
  }

  function postAuthorTitle() {
    const n = document.querySelector('.post-card:not(.post-embedded) .post-name');
    return n ? (n.textContent || '').trim().replace(/\s+/g,' ') : 'Postingan';
  }

  function storyViewerOpen() {
    const v = document.getElementById('story-viewer');
    if (!v || v.hidden) return false;
    try { return getComputedStyle(v).display !== 'none' && getComputedStyle(v).visibility !== 'hidden'; }
    catch (_) { return true; }
  }

  function currentPageType() {
    if (storyViewerOpen()) return 'story';
    if (isProfilePage) return 'profile';
    if (isPostDetailPage) return 'post';
    if (isReelsPage) return 'reels';
    if (isMessagesPage) return 'messages';
    if (isNotificationsPage) return 'notifications';
    if (isLivePage) return 'live';
    if (isAiPage) return 'ai';
    if (isShopPage) return 'shop';
    if (isSettingsPageFamily || isSettingsPage) return 'settings';
    if (isAuthPage) return 'auth';
    return 'default';
  }

  function syncPageChrome() {
    try {
      if (!API || !API.syncPageChrome) return;
      const ownProfile = !!document.querySelector('.profile-actions a[href^="settings.php"],.profile-actions a[href*="settings.php"]');
      const type = currentPageType();
      let title = '';
      if (isProfilePage) title = profileTitle();
      else if (isPostDetailPage) title = postAuthorTitle();
      else if (type === 'story') {
        const n = document.querySelector('#story-user b');
        title = n ? (n.textContent || '').trim().replace(/\s+/g,' ') : 'Cerita';
      }
      const key = [type, title, ownProfile ? '1' : '0'].join('|');
      if (key === lastChromeKey) return;
      lastChromeKey = key;
      API.syncPageChrome(type, title, ownProfile);
    } catch (_) {}
  }

  function closeProfileOptions() {
    document.querySelectorAll('.deapp-native-profile-options-backdrop,.deapp-native-profile-options-sheet').forEach(function(n){ n.remove(); });
  }

  function profileActionSources() {
    const actions = document.querySelector('.profile-actions');
    if (!actions) return [];
    const direct = Array.from(actions.querySelectorAll(':scope > a.btn,:scope > button.btn'));
    const more = Array.from(actions.querySelectorAll('.menu-wrap .dropdown .dropdown-item'));
    return direct.concat(more).filter(function(el){ return !el.hidden && getComputedStyle(el).display !== 'none'; });
  }

  function openProfileOptions() {
    if (!isProfilePage) return false;
    closeProfileOptions();
    const sources = profileActionSources();
    if (!sources.length) return false;

    const backdrop = document.createElement('div');
    backdrop.className = 'deapp-native-profile-options-backdrop';
    backdrop.addEventListener('click', closeProfileOptions);

    const sheet = document.createElement('section');
    sheet.className = 'deapp-native-profile-options-sheet';
    sheet.setAttribute('role','dialog');
    sheet.setAttribute('aria-modal','true');
    sheet.setAttribute('aria-label','Opsi profil');
    const title = document.createElement('div');
    title.className = 'deapp-native-profile-options-title';
    title.textContent = 'Opsi profil';
    sheet.appendChild(title);
    const list = document.createElement('div');
    list.className = 'deapp-native-profile-options-list';
    sheet.appendChild(list);

    sources.forEach(function(src){
      const row = document.createElement('button');
      row.type = 'button';
      row.className = 'deapp-native-profile-sheet-item' + (src.classList.contains('danger') ? ' danger' : '');
      row.innerHTML = src.innerHTML;
      row.addEventListener('click', function(){
        nativeTap();
        closeProfileOptions();
        setTimeout(function(){ src.click(); }, 90);
      });
      list.appendChild(row);
    });

    document.body.appendChild(backdrop);
    document.body.appendChild(sheet);
    wireSheet(sheet);
    return true;
  }

  function injectAboutSettings() {
    if (!isSettingsPage) return;
    const nav = document.querySelector('.settings-nav');
    if (!nav) return;

    if (!nav.querySelector('.deapp-native-about-row')) {
      const group = document.createElement('div');
      group.className = 'snav-group deapp-native-about-group';
      group.textContent = 'Aplikasi';

      const server = document.createElement('button');
      server.type = 'button';
      server.className = 'snav deapp-native-server-row';
      server.innerHTML = '<span class="deapp-settings-native-icon">⌁</span><span><b>Ganti server</b><small>Hosting, XAMPP atau alamat server Deapp</small></span>';
      server.addEventListener('click', function(){
        nativeTap();
        try { if (API && API.showServerSettings) API.showServerSettings(); } catch (_) {}
      });

      const permissions = document.createElement('button');
      permissions.type = 'button';
      permissions.className = 'snav deapp-native-permission-row';
      permissions.innerHTML = '<span class="deapp-settings-native-icon">◈</span><span><b>Perizinan aplikasi</b><small>Lokasi, kamera, mikrofon, notifikasi, media dan lainnya</small></span>';
      permissions.addEventListener('click', function(){
        nativeTap();
        try { if (API && API.showPermissions) API.showPermissions(); } catch (_) {}
      });

      const row = document.createElement('button');
      row.type = 'button';
      row.className = 'snav deapp-native-about-row';
      row.innerHTML = '<span class="deapp-settings-native-icon">ⓘ</span><span><b>Tentang aplikasi</b><small>Deapp Lite untuk Android</small></span><span class="deapp-version-pill">v1.9.18-lite</span>';
      row.addEventListener('click', function(){
        nativeTap();
        try { if (API && API.showAboutApp) API.showAboutApp(); } catch (_) {}
      });
      nav.appendChild(group); nav.appendChild(server); nav.appendChild(permissions); nav.appendChild(row);
    }

    if (!nav.querySelector('.deapp-native-logout-row')) {
      const logoutGroup = document.createElement('div');
      logoutGroup.className = 'snav-group deapp-native-logout-group';
      logoutGroup.textContent = 'Akun';
      const logout = document.createElement('a');
      logout.className = 'snav deapp-native-logout-row';
      logout.href = 'api/logout.php';
      logout.innerHTML = '<span class="deapp-settings-logout-icon">'+sectionIcon('logout')+'</span><span><b>Logout akun</b><small>Keluar dari akun Deapp di perangkat ini</small></span>';
      logout.addEventListener('click', function(){ nativeTap(); });
      nav.appendChild(logoutGroup); nav.appendChild(logout);
    }
  }

  function installPostPublishedHook() {
    if (window.__deappNativePostPublishedHook || typeof window.fetch !== 'function') return;
    window.__deappNativePostPublishedHook = true;
    const originalFetch = window.fetch.bind(window);
    window.fetch = function(input, init) {
      const rawUrl = typeof input === 'string' ? input : (input && input.url ? input.url : '');
      return originalFetch(input, init).then(function(response) {
        if (/\/api\/post_create\.php(?:[?#]|$)/i.test(String(rawUrl || ''))) {
          try {
            response.clone().json().then(function(data) {
              if (data && data.success === true && data.post_id) {
                try { if (API && API.postPublished) API.postPublished(); } catch (_) {}
              }
            }).catch(function(){});
          } catch (_) {}
        }
        return response;
      });
    };
  }


  function isVisible(el) {
    if (!el || !el.isConnected) return false;
    let node = el;
    while (node && node.nodeType === 1) {
      const cs = getComputedStyle(node);
      if (cs.display === 'none' || cs.visibility === 'hidden' || Number(cs.opacity || 1) === 0) return false;
      node = node.parentElement;
    }
    const r = el.getBoundingClientRect();
    return r.width > 0 && r.height > 0 && r.bottom > 0 && r.top < innerHeight;
  }

  function sheetTarget(node) {
    const el = node && node.closest ? node : (node && node.parentElement ? node.parentElement : null);
    if (!el || !el.closest) return false;
    return !!el.closest('.modal-overlay:not(#composer-modal) .modal-box,.deapp-cookie-modal .cookie-modal-card,dialog.c-modal[open] .c-modal-box,.post-card .menu-wrap.open>.dropdown,.deapp-native-profile-options-sheet,#story-sheet:not([hidden]),.reel-sheet:not([hidden]) .reel-sheet-panel,.reel-sheet:not([hidden]) .reel-more-panel');
  }

  const webSheetCandidates = [
    // Seluruh modal Deapp (gift, report, share, edit, AI, photo studio, Live viewer, shop, dst.)
    '.modal-overlay:not(#composer-modal)',
    // Cookie preferences di mobile juga tampil sebagai bottom sheet.
    '.deapp-cookie-modal',
    // Dialog komunitas dan dialog native browser-style yang memang sedang open.
    'dialog[open]',
    // Bottom sheet/panel khusus yang tidak memakai .modal-overlay.
    '.deapp-native-profile-options-sheet',
    '#story-sheet:not([hidden])',
    '.reel-sheet:not([hidden])',
    // Menu postingan ditransformasikan menjadi bottom sheet oleh skin Android.
    '.post-card .menu-wrap.open>.dropdown'
  ];

  function visibleWebSheets() {
    const found = [];
    webSheetCandidates.forEach(function(selector){
      document.querySelectorAll(selector).forEach(function(node){
        if (!node || found.indexOf(node) !== -1) return;
        if (node.getAttribute && node.getAttribute('aria-hidden') === 'true') return;
        if (node.closest && node.closest('#composer-modal')) return;
        if (isVisible(node)) found.push(node);
      });
    });
    return found;
  }

  function hasVisibleWebSheet() {
    return visibleWebSheets().length > 0;
  }

  function unlockBackgroundScroll(restorePosition) {
    if (!document.body) return;
    backgroundScrollLocked = false;
    root.classList.remove('deapp-native-sheet-lock');
    document.body.classList.remove('deapp-native-sheet-lock-body');
    document.body.style.top = savedBodyTop;
    if (restorePosition) {
      const y = lockedScrollY;
      requestAnimationFrame(function(){
        if (!hasVisibleWebSheet()) window.scrollTo(0, y);
      });
    }
  }

  function updateBackgroundScrollLock() {
    if (!document.body) return;
    // Native Android sheet already sits above the WebView and intercepts touches itself.
    // Only web-based sheets need a DOM scroll lock. This prevents a stale native-sheet
    // flag from freezing the whole page after the sheet is dismissed.
    const shouldLock = !!currentWebSheetOpen;
    if (shouldLock === backgroundScrollLocked) {
      if (!shouldLock) {
        root.classList.remove('deapp-native-sheet-lock');
        document.body.classList.remove('deapp-native-sheet-lock-body');
      }
      return;
    }
    if (shouldLock) {
      lockedScrollY = window.scrollY || document.documentElement.scrollTop || 0;
      savedBodyTop = document.body.style.top || '';
      backgroundScrollLocked = true;
      root.classList.add('deapp-native-sheet-lock');
      document.body.classList.add('deapp-native-sheet-lock-body');
    } else {
      unlockBackgroundScroll(true);
    }
  }

  function syncSheetVisualState() {
    const open = !!(currentWebSheetOpen || externalNativeSheetOpen);
    root.classList.toggle('deapp-bottom-sheet-active', open);
    if (document.body) document.body.classList.toggle('deapp-bottom-sheet-active', open);
  }

  function setExternalSheetOpen(open) {
    externalNativeSheetOpen = !!open;
    // Deliberately do not lock the DOM for a native sheet. The Android overlay is enough.
    // When it closes, also self-heal any stale web lock state.
    if (!externalNativeSheetOpen && !hasVisibleWebSheet()) {
      currentWebSheetOpen = false;
      unlockBackgroundScroll(false);
    }
    syncSheetVisualState();
    return true;
  }

  function guardBackgroundGesture(e) {
    if (!backgroundScrollLocked) return;
    if (!hasVisibleWebSheet()) {
      currentWebSheetOpen = false;
      syncSheetVisualState();
      unlockBackgroundScroll(false);
      return;
    }
    if (!sheetTarget(e.target)) e.preventDefault();
  }
  document.addEventListener('touchmove', guardBackgroundGesture, {passive:false,capture:true});
  document.addEventListener('wheel', guardBackgroundGesture, {passive:false,capture:true});
  document.addEventListener('touchstart', function(){
    if (backgroundScrollLocked && !hasVisibleWebSheet()) {
      currentWebSheetOpen = false;
      unlockBackgroundScroll(false);
    }
  }, {passive:true,capture:true});

  function recoverPageScroll() {
    try {
      const open = hasVisibleWebSheet();
      currentWebSheetOpen = open;
      syncSheetVisualState();
      if (!open) unlockBackgroundScroll(false);
      else updateBackgroundScrollLock();
      return true;
    } catch (_) {
      return false;
    }
  }

  function recoverPageInteraction() {
    try {
      // A post-menu backdrop must never survive without a genuinely visible menu.
      // A stale full-screen backdrop is enough to absorb every tap in the WebView.
      if (!visibleOpenPostMenu()) removePostBackdrop();

      // Likewise, remove only an orphan profile backdrop. The live sheet remains untouched.
      const profileSheet = document.querySelector('.deapp-native-profile-options-sheet');
      if (!profileSheet || !isVisible(profileSheet)) {
        document.querySelectorAll('.deapp-native-profile-options-backdrop').forEach(function(n){ n.remove(); });
      }

      recoverPageScroll();
      return true;
    } catch (_) {
      return false;
    }
  }

  function syncWebSheetState() {
    try {
      const open = hasVisibleWebSheet();
      currentWebSheetOpen = open;
      syncSheetVisualState();
      updateBackgroundScrollLock();
      if (open !== lastWebSheetOpen) {
        lastWebSheetOpen = open;
        if (API && API.syncWebSheetState) API.syncWebSheetState(open);
      }
    } catch (_) {}
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
      const key = [logged ? '1' : '0', avatar, profile, location.href].join('|');
      if (key === lastSessionKey) return;
      lastSessionKey = key;
      if (API && API.syncSessionState) API.syncSessionState(logged, avatar, profile, location.href);
    } catch (_) {}
  }


  function syncTheme() {
    try {
      const on = root.classList.contains('is-dark');
      if (lastThemeDark === on) return;
      lastThemeDark = on;
      if (API && API.syncTheme) API.syncTheme(on);
    } catch (_) {}
  }

  function setScrollUiState(on) {
    if (scrollUiActive === on) return;
    scrollUiActive = on;
    root.classList.toggle('deapp-native-scrolling', on);
    try { if (API && API.syncScrollState) API.syncScrollState(on); } catch (_) {}
  }

  function onAnyScroll() {
    setScrollUiState(true);
    clearTimeout(scrollUiTimer);
    scrollUiTimer = setTimeout(function(){ setScrollUiState(false); }, 190);
  }


  function charArray(value) {
    return Array.from(String(value || ''));
  }

  function cloneRichPrefix(source, limit) {
    const frag = document.createDocumentFragment();
    let remaining = Math.max(0, limit | 0);
    let stopped = false;

    function walk(node, parent) {
      if (!node || stopped || remaining <= 0) { stopped = true; return; }
      if (node.nodeType === Node.TEXT_NODE) {
        const chars = charArray(node.nodeValue || '');
        if (!chars.length) return;
        const takeChars = chars.slice(0, remaining);
        parent.appendChild(document.createTextNode(takeChars.join('')));
        remaining -= takeChars.length;
        if (takeChars.length < chars.length || remaining <= 0) stopped = true;
        return;
      }
      if (node.nodeType !== Node.ELEMENT_NODE) return;
      if (/^(SCRIPT|STYLE|NOSCRIPT)$/i.test(node.tagName)) return;
      const clone = node.cloneNode(false);
      clone.removeAttribute('id');
      parent.appendChild(clone);
      for (const child of Array.from(node.childNodes)) {
        walk(child, clone);
        if (stopped) break;
      }
    }

    for (const child of Array.from(source.childNodes)) {
      walk(child, frag);
      if (stopped) break;
    }
    return frag;
  }

  function collapseLongPosts() {
    if (isPostDetailPage || isReelsPage) return;
    document.querySelectorAll('.post-card:not(.post-embedded) .post-content:not(.deapp-post-preview)').forEach(function(content){
      if (!content || content.dataset.deappLongPost === '1') return;
      const raw = (content.textContent || '').trim();
      if (charArray(raw).length <= 250) {
        content.dataset.deappLongPost = '1';
        return;
      }
      content.dataset.deappLongPost = '1';

      const preview = document.createElement('div');
      preview.className = 'post-content deapp-post-preview';
      preview.dataset.deappLongPost = '1';
      preview.appendChild(cloneRichPrefix(content, 250));
      preview.appendChild(document.createTextNode('…'));

      const more = document.createElement('button');
      more.type = 'button';
      more.className = 'deapp-post-more';
      more.textContent = 'Lihat selengkapnya';
      more.setAttribute('aria-expanded', 'false');
      preview.appendChild(more);

      content.style.display = 'none';
      content.parentNode.insertBefore(preview, content);

      more.addEventListener('click', function(e){
        e.preventDefault();
        e.stopPropagation();
        preview.style.display = 'none';
        content.style.display = '';
        content.classList.add('deapp-post-original-expanded');
        more.setAttribute('aria-expanded', 'true');
      });
    });
  }


  function sectionIcon(name) {
    const c = 'viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="1.9" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"';
    const m = {
      back:'<path d="m15 18-6-6 6-6"/>', home:'<path d="M3 11.5 12 4l9 7.5V21h-6v-6H9v6H3z"/>',
      chat:'<path d="M21 15a4 4 0 0 1-4 4H8l-5 3V7a4 4 0 0 1 4-4h10a4 4 0 0 1 4 4z"/>',
      users:'<path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M22 21v-2a4 4 0 0 0-3-3.87"/>',
      bell:'<path d="M18 8a6 6 0 0 0-12 0c0 7-3 7-3 9h18c0-2-3-2-3-9"/><path d="M13.7 21a2 2 0 0 1-3.4 0"/>',
      at:'<circle cx="12" cy="12" r="4"/><path d="M16 8v5a3 3 0 0 0 6 0v-1a10 10 0 1 0-4 8"/>',
      heart:'<path d="M20.8 4.6a5.5 5.5 0 0 0-7.8 0L12 5.7l-1.1-1.1a5.5 5.5 0 0 0-7.8 7.8L12 21l8.9-8.6a5.5 5.5 0 0 0-.1-7.8z"/>',
      shield:'<path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10"/>', plus:'<path d="M12 5v14M5 12h14"/>',
      live:'<rect x="3" y="5" width="14" height="14" rx="3"/><path d="m17 10 4-2v8l-4-2z"/>',
      spark:'<path d="m12 3 1.6 4.4L18 9l-4.4 1.6L12 15l-1.6-4.4L6 9l4.4-1.6z"/><path d="m19 15 .8 2.2L22 18l-2.2.8L19 21l-.8-2.2L16 18l2.2-.8z"/>',
      wand:'<path d="m15 4 5 5L8 21H3v-5z"/><path d="m6 6 1-3 1 3 3 1-3 1-1 3-1-3-3-1z"/>',
      bookmark:'<path d="M6 4.5A1.5 1.5 0 0 1 7.5 3h9A1.5 1.5 0 0 1 18 4.5V21l-6-4-6 4z"/>',
      wallet:'<path d="M4 6h14a2 2 0 0 1 2 2v10a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V6a3 3 0 0 1 3-3h12v3"/><path d="M16 12h4"/>',
      shop:'<path d="M3 9h18l-1.5-5h-15z"/><path d="M5 9v11h14V9M9 20v-6h6v6"/>',
      crown:'<path d="m3 7 4 4 5-7 5 7 4-4-2 11H5z"/>', gift:'<path d="M20 12v9H4v-9M2 7h20v5H2zM12 21V7"/><path d="M12 7H7.5A2.5 2.5 0 1 1 10 4.5zM12 7h4.5A2.5 2.5 0 1 0 14 4.5z"/>',
      grid:'<rect x="3" y="3" width="7" height="7" rx="1"/><rect x="14" y="3" width="7" height="7" rx="1"/><rect x="3" y="14" width="7" height="7" rx="1"/><rect x="14" y="14" width="7" height="7" rx="1"/>',
      user:'<circle cx="12" cy="8" r="4"/><path d="M4.5 21a7.5 7.5 0 0 1 15 0"/>', settings:'<circle cx="12" cy="12" r="3"/><path d="M19.4 15a1.7 1.7 0 0 0 .34 1.88l.06.06-2.83 2.83-.06-.06A1.7 1.7 0 0 0 15 19.4a1.7 1.7 0 0 0-1 .6 1.7 1.7 0 0 0-.4 1.1V21h-4v-.1A1.7 1.7 0 0 0 8 19.4a1.7 1.7 0 0 0-1.88.34l-.06.06-2.83-2.83.06-.06A1.7 1.7 0 0 0 3.6 15a1.7 1.7 0 0 0-1.5-1H2v-4h.1A1.7 1.7 0 0 0 3.6 8a1.7 1.7 0 0 0-.34-1.88l-.06-.06 2.83-2.83.06.06A1.7 1.7 0 0 0 8 3.6a1.7 1.7 0 0 0 1-1.5V2h4v.1a1.7 1.7 0 0 0 1 1.5 1.7 1.7 0 0 0 1.88-.34l.06-.06 2.83 2.83-.06.06A1.7 1.7 0 0 0 19.4 8a1.7 1.7 0 0 0 1.5 1H21v4h-.1a1.7 1.7 0 0 0-1.5 1z"/>',
      help:'<circle cx="12" cy="12" r="10"/><path d="M9.5 9a2.5 2.5 0 1 1 4.2 1.8c-.9.7-1.7 1.2-1.7 2.7M12 17h.01"/>',
      lock:'<rect x="4" y="10" width="16" height="11" rx="2"/><path d="M8 10V7a4 4 0 0 1 8 0v3"/>',
      sun:'<circle cx="12" cy="12" r="4"/><path d="M12 2v2M12 20v2M4.93 4.93l1.41 1.41M17.66 17.66l1.41 1.41M2 12h2M20 12h2M4.93 19.07l1.41-1.41M17.66 6.34l1.41-1.41"/>',
      menu:'<path d="M4 7h16M4 12h16M4 17h16"/>',
      logout:'<path d="M10 17l5-5-5-5"/><path d="M15 12H3"/><path d="M13 3h6a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2h-6"/>',
      more:'<circle cx="5" cy="12" r="1" fill="currentColor" stroke="none"/><circle cx="12" cy="12" r="1" fill="currentColor" stroke="none"/><circle cx="19" cy="12" r="1" fill="currentColor" stroke="none"/>'
    };
    return '<svg '+c+'>'+(m[name]||m.grid)+'</svg>';
  }

  function goSection(href) {
    if (!href) return;
    try { location.href = new URL(href, location.href).href; } catch (_) { location.href = href; }
  }

  function sectionBack() {
    if (history.length > 1) history.back(); else goSection('index.php');
  }

  function removeSectionChrome() {
    document.querySelectorAll('.deapp-section-header,.deapp-section-footer').forEach(function(n){ n.remove(); });
    root.classList.remove('deapp-has-section-chrome');
    if (document.body) document.body.classList.remove('deapp-section-page');
  }

  function sectionHeader(kind, title, subtitle, action) {
    let h = document.querySelector('.deapp-section-header');
    if (!h) {
      h = document.createElement('header');
      h.className = 'deapp-section-header';
      document.body.appendChild(h);
    }
    h.dataset.section = kind;
    h.innerHTML = '<button type="button" class="deapp-section-left" aria-label="Kembali">'+sectionIcon('back')+'</button>'+
      '<div class="deapp-section-title"><b></b><small></small></div>'+
      '<button type="button" class="deapp-section-action" aria-label="Aksi"></button>';
    h.querySelector('.deapp-section-left').addEventListener('click', kind === 'settings' ? settingsBack : sectionBack);
    h.querySelector('.deapp-section-title b').textContent = title || 'Deapp';
    const sub = h.querySelector('.deapp-section-title small');
    sub.textContent = subtitle || 'Deapp';
    const a = h.querySelector('.deapp-section-action');
    if (!action) { a.style.visibility='hidden'; return h; }
    a.style.visibility='visible';
    a.innerHTML = sectionIcon(action.icon || 'more');
    a.setAttribute('aria-label', action.label || 'Aksi');
    a.addEventListener('click', function(e){ e.preventDefault(); if (action.run) action.run(); else if (action.href) goSection(action.href); });
    return h;
  }

  function sectionFooter(kind, items) {
    let f = document.querySelector('.deapp-section-footer');
    if (!f) {
      f = document.createElement('nav');
      f.className = 'deapp-section-footer';
      document.body.appendChild(f);
    }
    f.dataset.section = kind;
    f.style.setProperty('--deapp-section-cols', String(Math.max(1, items.length)));
    f.innerHTML = '';
    items.forEach(function(item){
      const b=document.createElement('button');
      b.type='button'; b.className='deapp-section-nav'+(item.active?' is-active':'')+(item.primary?' deapp-section-primary':'');
      b.innerHTML=(item.primary?'<span>'+sectionIcon(item.icon)+'</span>':sectionIcon(item.icon))+'<em style="font-style:normal">'+item.label+'</em>';
      if (item.active) b.setAttribute('aria-current','page');
      b.addEventListener('click', function(){ if (item.run) item.run(); else goSection(item.href); });
      f.appendChild(b);
    });
    return f;
  }

  function sectionOn(kind) {
    root.classList.add('deapp-has-section-chrome');
    if (document.body) document.body.classList.add('deapp-section-page');
    root.dataset.deappSection = kind;
  }

  function wireStoryChrome() {
    const open = storyViewerOpen();
    root.classList.toggle('deapp-native-story-open', open);
    if (document.body) document.body.classList.toggle('deapp-story-open', open);
    if (open) {
      const reply = document.getElementById('story-reply-input');
      if (reply && reply.placeholder !== 'Balas…') reply.placeholder = 'Balas…';
    }
  }

  function wireMessagesChrome() {
    if (!isMessagesPage || !document.body) return;
    const active = !!document.querySelector('.messenger.has-chat,.page-messages.chat-open,.chat-head');
    root.classList.toggle('deapp-chat-open', active);
    root.classList.toggle('deapp-chat-list', !active);
    if (active) {
      removeSectionChrome();
      return;
    }
    sectionOn('messages');
    sectionHeader('messages','Chat','',{icon:'plus',label:'Mulai chat',href:'explore.php?tab=people'});
    const oldFooter=document.querySelector('.deapp-section-footer[data-section="messages"]'); if(oldFooter) oldFooter.remove();
  }

  const notifLabels={all:'Semua',mentions:'Sebutan',comments:'Balasan',reactions:'Reaksi',gifts:'Hadiah & koin',follows:'Pengikut',predict:'Prediksi',live:'Live',system:'Sistem'};
  function wireNotificationsChrome() {
    if (!isNotificationsPage || !document.body) return;
    const q=new URL(location.href).searchParams, f=q.get('f')||'all';
    sectionOn('notifications');
    sectionHeader('notifications','Notifikasi','',{icon:'settings',label:'Preferensi notifikasi',href:'settings.php?tab=notifications'});

    // v1.9.13: footer kategori lama dibuang. Gunakan tab asli Deapp agar semua filter
    // (termasuk hadiah, pengikut, prediksi dan Live) tetap mengikuti backend asli.
    const oldFooter=document.querySelector('.deapp-section-footer[data-section="notifications"]');
    if (oldFooter) oldFooter.remove();
    const tabs=document.querySelector('.tabs.sticky-tabs');
    const filter=document.querySelector('.nx-filter');
    if (tabs) {
      tabs.classList.add('deapp-notification-tabs');
      if (filter && tabs.nextElementSibling !== filter && filter.parentNode) filter.parentNode.insertBefore(tabs, filter);
      const active=tabs.querySelector('.tab.active');
      if (active && tabs.dataset.deappCentered !== f) {
        tabs.dataset.deappCentered=f;
        setTimeout(function(){ try { active.scrollIntoView({behavior:'auto',block:'nearest',inline:'center'}); } catch (_) {} }, 40);
      }
    }
  }

  function wireLiveChrome() {
    if (!isLivePage || !document.body) return;
    const q=new URL(location.href).searchParams, tab=q.get('tab')||'discover', studio=q.has('studio'), room=q.has('id');
    let title=studio?'Mulai Live':(room?'Live':'Live');
    let sub=studio?'Creator Studio':(tab==='following'?'Mengikuti':(tab==='mine'?'Live Saya':'Siaran langsung'));
    if (room) {
      const t=document.querySelector('.live-title-row h1,.live-scheduled-content h1'); if(t) title=(t.textContent||'Live').trim();
      sub='Sedang menonton';
    }
    sectionOn('live');
    sectionHeader('live',title,sub,{icon:'plus',label:'Mulai Live',href:'live.php?studio=1'});
    sectionFooter('live',[
      {label:'Untukmu',icon:'live',href:'live.php?tab=discover',active:!studio&&!room&&tab==='discover'},
      {label:'Mengikuti',icon:'users',href:'live.php?tab=following',active:!studio&&!room&&tab==='following'},
      {label:'Mulai',icon:'plus',href:'live.php?studio=1',active:studio,primary:true},
      {label:'Live Saya',icon:'user',href:'live.php?tab=mine',active:!studio&&!room&&tab==='mine'}
    ]);
  }

  const aiViewNames={chat:'Chat AI',tools:'AI Tools',creator:'Kreator',library:'Pustaka'};
  const aiViewMeta={
    chat:{icon:'chat',sub:'Percakapan dengan Deapp AI'},
    tools:{icon:'wand',sub:'Perkakas AI'},
    creator:{icon:'spark',sub:'Studio kreator'},
    library:{icon:'bookmark',sub:'Hasil yang disimpan'}
  };
  function activeAiView(){ const b=document.querySelector('.deapp-ai-nav button.active'); return b&&b.dataset.aiView?b.dataset.aiView:'chat'; }
  function closeAiNativeMenu(){
    const menu=document.querySelector('.deapp-ai-native-menu'), back=document.querySelector('.deapp-ai-native-menu-backdrop');
    if(menu) menu.classList.remove('open');
    if(back) back.classList.remove('open');
    const trigger=document.querySelector('.deapp-section-header[data-section="ai"] .deapp-section-action');
    if(trigger) trigger.setAttribute('aria-expanded','false');
  }
  function selectAiView(view){
    const b=document.querySelector('.deapp-ai-nav button[data-ai-view="'+view+'"]');
    if(b){ b.click(); closeAiNativeMenu(); setTimeout(scheduleScan,32); return true; }
    return false;
  }
  function ensureAiNativeMenu(view){
    let back=document.querySelector('.deapp-ai-native-menu-backdrop');
    if(!back){
      back=document.createElement('div'); back.className='deapp-ai-native-menu-backdrop'; back.addEventListener('click',closeAiNativeMenu); document.body.appendChild(back);
    }
    let menu=document.querySelector('.deapp-ai-native-menu');
    if(!menu){
      menu=document.createElement('div'); menu.className='deapp-ai-native-menu'; menu.setAttribute('role','menu'); menu.setAttribute('aria-label','Menu Deapp AI'); document.body.appendChild(menu);
    }
    const defs=[['chat','Chat AI','chat'],['tools','AI Tools','wand'],['creator','Kreator','spark'],['library','Pustaka','bookmark']];
    if(menu.dataset.deappReady!=='1'){
      menu.dataset.deappReady='1';
      defs.forEach(function(d){
        const meta=aiViewMeta[d[0]]||{};
        const b=document.createElement('button'); b.type='button'; b.setAttribute('role','menuitem'); b.dataset.aiView=d[0];
        b.innerHTML='<span class="deapp-ai-menu-icon">'+sectionIcon(d[2])+'</span><span class="deapp-ai-menu-copy"><b>'+d[1]+'</b><small>'+meta.sub+'</small></span><span class="deapp-ai-menu-check">✓</span>';
        b.addEventListener('click',function(){selectAiView(d[0]);}); menu.appendChild(b);
      });
    }
    menu.querySelectorAll('button[data-ai-view]').forEach(function(b){
      const active=b.dataset.aiView===view; b.classList.toggle('is-active',active);
      if(active){ if(b.getAttribute('aria-current')!=='page') b.setAttribute('aria-current','page'); } else if(b.hasAttribute('aria-current')) b.removeAttribute('aria-current');
    });
    return menu;
  }
  function toggleAiNativeMenu(){
    const view=activeAiView(), menu=ensureAiNativeMenu(view), back=document.querySelector('.deapp-ai-native-menu-backdrop');
    const open=!menu.classList.contains('open');
    menu.classList.toggle('open',open); if(back) back.classList.toggle('open',open);
    const trigger=document.querySelector('.deapp-section-header[data-section="ai"] .deapp-section-action');
    if(trigger) trigger.setAttribute('aria-expanded',open?'true':'false');
  }
  function ensureAiFab(){
    let fab=document.querySelector('.deapp-ai-fab');
    const newChat=document.querySelector('.js-ai-new-chat');
    if(!newChat){ if(fab) fab.remove(); return null; }
    if(!fab){
      fab=document.createElement('button'); fab.type='button'; fab.className='deapp-ai-fab'; fab.setAttribute('aria-label','Chat baru dengan Deapp AI');
      fab.innerHTML='<span class="deapp-ai-fab-mark">✦</span><span class="deapp-ai-fab-plus">+</span>';
      fab.addEventListener('click',function(){
        closeAiNativeMenu();
        const b=document.querySelector('.deapp-ai-nav button[data-ai-view="chat"]'); if(b && !b.classList.contains('active')) b.click();
        const add=document.querySelector('.js-ai-new-chat'); if(add) add.click();
        setTimeout(function(){ const input=document.getElementById('ai-chat-input'); if(input) input.focus({preventScroll:true}); scheduleScan(); },90);
      });
      document.body.appendChild(fab);
    }
    return fab;
  }
  function wireAiChrome() {
    if (!isAiPage || !document.body) return;
    const oldFooter=document.querySelector('.deapp-section-footer[data-section="ai"]'); if(oldFooter) oldFooter.remove();
    sectionOn('ai');
    root.dataset.deappAiView='chat';
    closeAiNativeMenu();
    document.querySelectorAll('.deapp-ai-native-menu,.deapp-ai-native-menu-backdrop,.deapp-ai-fab').forEach(function(n){n.remove();});
    const header=sectionHeader('ai','Deapp AI','',null);
    const sub=header&&header.querySelector('.deapp-section-title small');
    if(sub){ sub.textContent=''; sub.style.display='none'; }
    const chatTab=document.querySelector('.deapp-ai-nav button[data-ai-view="chat"]');
    if(chatTab && !chatTab.classList.contains('active')) chatTab.click();
  }

  const shopTitles={wallet:'Dompet',kirim:'Kirim koin',promo:'Kode promo',etalase:'Etalase',wishlist:'Keinginan',pet:'Pet',virtual:'Item Virtual',vip:'VIP',gifts:'Hadiah',theme:'Tema',frame:'Bingkai',bubble:'Gelembung',sticker:'Stiker',effect:'Efek nama',ticket:'Tiket',bag:'Tas barang',koleksi:'Koleksiku',level:'Level'};
  function wireShopChrome() {
    if (!isShopPage || !document.body) return;
    const q=new URL(location.href).searchParams, tab=q.get('tab')||'wallet';
    sectionOn('shop');
    sectionHeader('shop',shopTitles[tab]||'Toko & Dompet','',{icon:'plus',label:'Top Up',href:'settings.php?tab=topup'});
    const oldFooter=document.querySelector('.deapp-section-footer[data-section="shop"]'); if(oldFooter) oldFooter.remove();
  }

  const settingNames={profile:'Profil',appearance:'Tampilan',experience:'Pengalaman aplikasi',language:'Bahasa',ai:'Deapp AI',characters:'Karakter AI',access:'Aksesibilitas',premium:'Premium & mood',topup:'Top Up Koin',focus:'Ruang Fokus',notifications:'Notifikasi',privacy:'Privasi',words:'Kata dibisukan',blocked:'Diblokir & dibisukan',notes:'Catatan pribadi',away:'Arsip & mode rehat',security:'Keamanan',sessions:'Perangkat & sesi',verify:'Verifikasi',data:'Data saya',account:'Akun'};
  function settingsBack() {
    const file=(path.split('/').pop()||'').toLowerCase();
    const params=new URL(location.href).searchParams;
    const childSecurity=['security-password.php','security-email.php','security-wallet-pin.php','security-2fa.php','security-sessions.php'];
    if (childSecurity.includes(file)) return goSection('security.php');
    if (file==='security.php') return goSection('settings.php');
    if (file==='profile-edit.php') return goSection('settings.php?tab=profile');
    if (file==='privacy.php' || file==='policy.php' || file==='cookies.php') return goSection('settings.php?tab=privacy');
    if (file==='help.php') return goSection('settings.php');
    if (file==='settings.php' && params.has('tab')) return goSection('settings.php');
    if (history.length > 1) history.back(); else goSection('index.php');
  }

  function settingsContext() {
    const file=(path.split('/').pop()||'').toLowerCase();
    const params=new URL(location.href).searchParams;
    const hasTab=params.has('tab');
    const tab=params.get('tab')||'profile';
    if(file==='settings.php' && !hasTab) return {title:'Pengaturan',group:'settings',index:true};
    if(file==='settings.php') return {title:settingNames[tab]||'Pengaturan',group:tab==='privacy'||tab==='words'||tab==='blocked'?'privacy':(tab==='security'||tab==='sessions'?'security':'settings'),index:false};
    const map={
      'security.php':['Keamanan akun','security'],'security-password.php':['Kata sandi','security'],'security-email.php':['Alamat email','security'],
      'security-wallet-pin.php':['PIN dompet','security'],'security-2fa.php':['Verifikasi 2 langkah','security'],'security-sessions.php':['Perangkat & sesi','security'],
      'help.php':['Pusat Bantuan','help'],'privacy.php':['Kebijakan Privasi','privacy'],'policy.php':['Policy & Pedoman','privacy'],'cookies.php':['Kebijakan Cookie','privacy'],
      'profile-edit.php':['Custom Profile Studio','settings']
    };
    const v=map[file]||['Pengaturan','settings']; return {title:v[0],group:v[1]};
  }
  function wireSettingsChrome() {
    if (!(isSettingsPageFamily||isSettingsPage) || !document.body) return;
    const c=settingsContext();
    const isIndex=!!(isSettingsPage && c.index);
    root.classList.toggle('deapp-settings-index',isIndex);
    root.classList.toggle('deapp-settings-subpage',!isIndex);
    sectionOn('settings');
    const oldFooter=document.querySelector('.deapp-section-footer[data-section="settings"]'); if(oldFooter) oldFooter.remove();
    if (isSettingsPage) {
      const nav=document.querySelector('.settings-nav');
      if (nav) {
        nav.classList.toggle('deapp-settings-menu-index',isIndex);
        if (isIndex) nav.querySelectorAll('.snav.active').forEach(function(n){ n.classList.remove('active'); });
      }
    }
    sectionHeader('settings',isIndex?'Pengaturan':c.title,'',null);
  }

  function wireSectionChrome() {
    wireStoryChrome();
    if (storyViewerOpen() || isReelsPage || isPostDetailPage || isProfilePage || isAuthPage) {
      if (!isMessagesPage && !isNotificationsPage && !isLivePage && !isAiPage && !isShopPage && !isSettingsPageFamily && !isSettingsPage) removeSectionChrome();
      return;
    }
    if (isMessagesPage) return wireMessagesChrome();
    if (isNotificationsPage) return wireNotificationsChrome();
    if (isLivePage) return wireLiveChrome();
    if (isAiPage) return wireAiChrome();
    if (isShopPage) return wireShopChrome();
    if (isSettingsPageFamily || isSettingsPage) return wireSettingsChrome();
    removeSectionChrome();
  }

  function reelIcon(name) {
    const common = 'viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="1.9" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"';
    if (name === 'back') return '<svg '+common+'><path d="m15 18-6-6 6-6"/></svg>';
    if (name === 'plus') return '<svg '+common+'><path d="M12 5v14M5 12h14"/></svg>';
    if (name === 'play') return '<svg '+common+'><path d="M8 5v14l11-7z"/></svg>';
    if (name === 'users') return '<svg '+common+'><path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M22 21v-2a4 4 0 0 0-3-3.87M16 3.13a4 4 0 0 1 0 7.75"/></svg>';
    if (name === 'bookmark') return '<svg '+common+'><path d="M6 4.5A1.5 1.5 0 0 1 7.5 3h9A1.5 1.5 0 0 1 18 4.5V21l-6-4-6 4z"/></svg>';
    return '<svg '+common+'><circle cx="12" cy="8" r="4"/><path d="M4.5 21a7.5 7.5 0 0 1 15 0"/></svg>';
  }

  function reelsCreate() {
    const b = document.getElementById('reel-create-open') || document.getElementById('reel-empty-create');
    if (b) {
      b.click();
      return true;
    }
    return false;
  }

  function syncReelsOverlayState() {
    if (!isReelsPage) return;
    const comments = document.getElementById('reel-comments-sheet');
    const create = document.getElementById('reel-create-modal');
    const more = document.getElementById('reel-more-sheet');
    const open = !!((comments && !comments.hidden) || (create && create.classList.contains('open')) || (more && !more.hidden));
    root.classList.toggle('deapp-reels-overlay-open', open);
    currentWebSheetOpen = open || hasVisibleWebSheet();
    syncSheetVisualState();
    if (API && API.syncWebSheetState && currentWebSheetOpen !== lastWebSheetOpen) { lastWebSheetOpen = currentWebSheetOpen; API.syncWebSheetState(currentWebSheetOpen); }
  }

  function wireReelsChrome() {
    if (!isReelsPage || !document.body) return;
    document.body.classList.add('deapp-reels-page');

    let header = document.querySelector('.deapp-reels-header');
    if (!header) {
      header = document.createElement('header');
      header.className = 'deapp-reels-header';
      header.innerHTML =
        '<button type="button" class="deapp-reels-back" aria-label="Kembali">'+reelIcon('back')+'</button>'+
        '<div class="deapp-reels-title"><b>Video Pendek</b><small>DeApp</small></div>'+
        '<button type="button" class="deapp-reels-header-create" aria-label="Buat video">'+reelIcon('plus')+'</button>';
      header.querySelector('.deapp-reels-back').addEventListener('click', function(){
        if (history.length > 1) history.back();
        else location.href = new URL('index.php', location.href).href;
      });
      header.querySelector('.deapp-reels-header-create').addEventListener('click', reelsCreate);
      document.body.appendChild(header);
    }

    let footer = document.querySelector('.deapp-reels-footer');
    const availableTabs = Array.from(document.querySelectorAll('.reels-tabs a'));
    if (!footer && availableTabs.length >= 4) {
      footer = document.createElement('nav');
      footer.className = 'deapp-reels-footer';
      footer.setAttribute('aria-label', 'Navigasi Video Pendek');

      const sourceTabs = availableTabs;
      const addTab = function(def) {
        const src = sourceTabs.find(function(a){ return (new URL(a.href, location.href)).searchParams.get('tab') === def.tab; });
        if (!src) return;
        const b = document.createElement('button');
        b.type = 'button';
        b.className = 'deapp-reels-nav-item';
        b.dataset.tab = def.tab;
        b.innerHTML = reelIcon(def.icon) + '<span>'+def.label+'</span>';
        b.addEventListener('click', function(){ location.href = src.href; });
        footer.appendChild(b);
      };

      addTab({tab:'foryou', label:'Untukmu', icon:'play'});
      addTab({tab:'following', label:'Mengikuti', icon:'users'});

      const create = document.createElement('button');
      create.type = 'button';
      create.className = 'deapp-reels-create';
      create.setAttribute('aria-label','Buat video');
      create.innerHTML = '<span>'+reelIcon('plus')+'</span><em style="font-style:normal">Buat</em>';
      create.addEventListener('click', reelsCreate);
      footer.appendChild(create);

      addTab({tab:'saved', label:'Tersimpan', icon:'bookmark'});
      addTab({tab:'mine', label:'Saya', icon:'user'});
      document.body.appendChild(footer);
    }

    if (footer) {
      const currentTab = new URL(location.href).searchParams.get('tab') || 'foryou';
      footer.querySelectorAll('.deapp-reels-nav-item').forEach(function(b){
        const active = b.dataset.tab === currentTab;
        b.classList.toggle('is-active', active);
        if (active) b.setAttribute('aria-current','page'); else b.removeAttribute('aria-current');
      });
    }
    syncReelsOverlayState();
  }

  let postDetailCommentDelegatesInstalled = false;
  function installPostDetailCommentDelegates() {
    if (!isPostDetailPage || postDetailCommentDelegatesInstalled) return;
    postDetailCommentDelegatesInstalled = true;
    document.addEventListener('click', function(e){
      const reply = e.target.closest('.js-creply,.reply-target button,.js-emoji-trigger,.js-toggle-comments');
      if (!reply) return;
      setTimeout(function(){
        const section = reply.closest('.comments-section') || document.querySelector('.comments-section');
        const form = section && section.querySelector('.comment-form');
        if (!form) return;
        const source = form.querySelector('input[name="comment_text"]');
        const proxy = form.querySelector('textarea.deapp-comment-textarea');
        if (!source || !proxy) return;
        proxy.placeholder = source.placeholder || 'Tulis balasan…';
        if (reply.classList.contains('js-emoji-trigger')) proxy.value = source.value || proxy.value;
        if (reply.classList.contains('js-creply') || reply.classList.contains('js-toggle-comments')) proxy.focus({preventScroll:true});
        proxy.dispatchEvent(new Event('input', {bubbles:true}));
      }, 80);
    });
  }


  // v1.9.17 — reaction postingan memberi haptic + suara ringan native.
  let lastReactionHapticAt = 0;
  function installReactionHaptics() {
    if (document.documentElement.dataset.deappReactionHaptic === '1') return;
    document.documentElement.dataset.deappReactionHaptic = '1';
    document.addEventListener('click', function(e){
      const target = e.target && e.target.closest ? e.target.closest('.post-card .js-react-btn,.post-card .react-opt,.qa-card .js-react-btn,.qa-card .react-opt') : null;
      if (!target) return;
      const now = Date.now();
      if (now - lastReactionHapticAt < 90) return;
      lastReactionHapticAt = now;
      try {
        if (API && typeof API.reactionFeedback === 'function') API.reactionFeedback();
        else if (API && typeof API.tap === 'function') API.tap();
      } catch (_) {}
    }, true);
  }

  // v1.9.13 — angka reaction, komentar dan bagikan berada tepat di samping ikon aksi.
  function wirePostActionCounts() {
    document.querySelectorAll('.post-card:not(.post-embedded)').forEach(function(card){
      const stats=card.querySelector('.post-stats');
      const actions=card.querySelector('.post-actions');
      if (!actions) return;

      function valueOf(el) {
        if (!el) return '';
        const t=(el.textContent || '').trim().replace(/\s+/g,' ');
        const m=t.match(/[0-9][0-9.,]*\s*(?:rb|jt|k|m)?/i);
        return m ? m[0].replace(/\s+/g,'') : '';
      }
      function attach(btn, src, kind, explicit) {
        if (!btn || !src) return;
        src.classList.add('deapp-stat-relocated');
        const val=explicit || valueOf(src);
        let count=btn.querySelector('.deapp-action-count[data-count-kind="'+kind+'"]');
        if (!val) {
          if (count) count.remove();
          btn.classList.toggle('deapp-counted-action', !!btn.querySelector('.deapp-action-count'));
          return;
        }
        if (!count) {
          count=document.createElement('span');
          count.className='deapp-action-count';
          count.dataset.countKind=kind;
          count.setAttribute('aria-hidden','true');
          btn.appendChild(count);
        }
        if (count.textContent !== val) count.textContent=val;
        btn.classList.add('deapp-counted-action');
      }

      const reactSrc=stats ? stats.querySelector('.stat-reactions') : null;
      const reactCount=stats ? stats.querySelector('.js-react-count') : null;
      attach(actions.querySelector('.js-react-btn'), reactSrc, 'reactions', valueOf(reactCount));

      const commentSrc=stats ? stats.querySelector('.stat-link.js-toggle-comments') : null;
      attach(actions.querySelector('.js-toggle-comments'), commentSrc, 'comments', valueOf(commentSrc));

      const shareSrc=stats ? stats.querySelector('a.stat-link[href*="tab=shares"]') : null;
      const shareBtn=actions.querySelector('.js-share');
      attach(shareBtn, shareSrc, 'shares', valueOf(shareSrc));

      const giftBtn=actions.querySelector('.js-gift-open');
      const giftRow=card.querySelector('.post-gifts');
      const giftCount=giftRow ? giftRow.querySelectorAll('.post-gift').length : 0;
      if (giftBtn && giftRow && giftCount > 0) attach(giftBtn, giftRow, 'gifts', String(giftCount));
      // Urutan Lite: reaction · komentar · gift · bagikan · simpan.
      if (giftBtn && shareBtn && giftBtn.parentNode === actions && shareBtn.parentNode === actions && giftBtn.nextElementSibling !== shareBtn) {
        actions.insertBefore(giftBtn, shareBtn);
      }

      if (stats) {
        const remaining=stats.querySelector('.stat-reactions:not(.deapp-stat-relocated),.stat-link:not(.deapp-stat-relocated)');
        stats.classList.toggle('deapp-stats-empty', !remaining);
      }
    });
  }

  function optimizeMediaLoading() {
    try {
      const eagerLimit = Math.max(innerHeight * 1.35, 900);
      document.querySelectorAll('img').forEach(function(img){
        try {
          if (img.dataset.deappMediaOptimized === '1') return;
          img.dataset.deappMediaOptimized = '1';
          img.decoding = 'async';
          const r = img.getBoundingClientRect();
          // Story/header/viewport awal tetap eager; media feed di bawah layar dibuat lazy.
          if (!img.closest('.story-tray,.topbar,.profile-cover,.profile-avatar-wrap') && r.top > eagerLimit && !img.hasAttribute('loading')) {
            img.loading = 'lazy';
          }
        } catch (_) {}
      });
      document.querySelectorAll('video').forEach(function(video){
        try {
          if (video.dataset.deappMediaOptimized === '1') return;
          video.dataset.deappMediaOptimized = '1';
          if (!video.autoplay && (!video.preload || video.preload === 'auto')) video.preload = 'metadata';
        } catch (_) {}
      });
    } catch (_) {}
  }

  function removePostTranslationUI() {
    try {
      const direct = [
        '.post-card .post-translation', '.post-card .post-translate', '.post-card .translation-box',
        '.post-card .translation-result', '.post-card .translate-post', '.post-card .post-translate-btn',
        '.post-card [data-action="translate"]', '.post-card [data-action="translation"]',
        '.post-card [data-translate-post]', '.post-card [data-post-translate]'
      ];
      document.querySelectorAll(direct.join(',')).forEach(function(el){ el.remove(); });

      document.querySelectorAll('.post-card button,.post-card a,.post-card .dropdown-item').forEach(function(el){
        const marker = [el.id || '', typeof el.className === 'string' ? el.className : '', el.getAttribute('data-action') || '', el.getAttribute('data-task') || ''].join(' ').toLowerCase();
        const label = (el.textContent || '').trim().replace(/\s+/g,' ').toLowerCase();
        if (marker.indexOf('translat') !== -1 || marker.indexOf('terjemah') !== -1 ||
            /^(terjemahkan|translate|lihat postingan asli|lihat kiriman asli|see original)(\b|$)/i.test(label)) {
          el.remove();
        }
      });
    } catch (_) {}
  }

  function enhancePostDetailComments() {
    if (!isPostDetailPage) return;
    installPostDetailCommentDelegates();

    document.querySelectorAll('.comment-form').forEach(function(form){
      const source = form.querySelector('input[name="comment_text"],textarea[name="comment_text"]');
      if (!source) return;

      let input = source;
      if (source.tagName !== 'TEXTAREA') {
        source.classList.add('deapp-comment-source-input');
        source.setAttribute('aria-hidden','true');
        source.tabIndex = -1;

        let proxy = form.querySelector('textarea.deapp-comment-textarea');
        if (!proxy) {
          proxy = document.createElement('textarea');
          proxy.className = 'comment-input deapp-comment-textarea';
          proxy.rows = 1;
          proxy.maxLength = source.maxLength > 0 ? source.maxLength : 1000;
          proxy.placeholder = source.placeholder || 'Tulis balasan…';
          proxy.autocomplete = 'off';
          proxy.dataset.mention = '1';
          proxy.setAttribute('aria-label', source.getAttribute('aria-label') || 'Tulis komentar');
          source.parentNode.insertBefore(proxy, source);

          proxy.addEventListener('input', function(){
            source.value = proxy.value;
            source.dispatchEvent(new Event('input', {bubbles:true}));
          });
          proxy.addEventListener('change', function(){
            source.value = proxy.value;
            source.dispatchEvent(new Event('change', {bubbles:true}));
          });
          proxy.addEventListener('keydown', function(e){
            if ((e.ctrlKey || e.metaKey) && e.key === 'Enter') {
              e.preventDefault();
              if (typeof form.requestSubmit === 'function') form.requestSubmit();
              else form.dispatchEvent(new Event('submit', {bubbles:true,cancelable:true}));
            }
          });
          form.addEventListener('submit', function(){
            source.value = proxy.value;
            let tries = 0;
            const waitClear = function(){
              tries++;
              if (!source.value) {
                proxy.value = '';
                proxy.dispatchEvent(new Event('input', {bubbles:true}));
                return;
              }
              if (tries < 12) setTimeout(waitClear, 180);
            };
            setTimeout(waitClear, 220);
          }, true);
        }
        input = proxy;
        input.placeholder = source.placeholder || input.placeholder || 'Tulis balasan…';
        if (document.activeElement === source) input.focus({preventScroll:true});
      }

      if (input.dataset.deappThreadComment !== '1') {
        input.dataset.deappThreadComment = '1';
        const multiline = input.tagName === 'TEXTAREA';
        const sync = function(){
          if (input !== source) source.value = input.value;
          const has = (input.value || '').trim().length > 0;
          form.classList.toggle('deapp-comment-typing', has);
          if (multiline) {
            input.style.height = 'auto';
            input.style.height = Math.min(128, Math.max(32, input.scrollHeight)) + 'px';
          }
        };
        input.addEventListener('input', sync, {passive:true});
        input.addEventListener('focus', sync, {passive:true});
        input.addEventListener('blur', sync, {passive:true});
        sync();
      } else {
        const has = (input.value || '').trim().length > 0;
        form.classList.toggle('deapp-comment-typing', has);
      }
    });
  }


  // v1.9.17 — tahan Story untuk jeda, tap singkat tetap menjalankan navigasi asli.
  const storyTimerBridge = {installed:false, records:new Map(), nativeSet:null, nativeClear:null};
  function installStoryTimerBridge() {
    if (storyTimerBridge.installed) return;
    storyTimerBridge.installed = true;
    storyTimerBridge.nativeSet = window.setTimeout.bind(window);
    storyTimerBridge.nativeClear = window.clearTimeout.bind(window);
    const nativeSet = storyTimerBridge.nativeSet;
    const nativeClear = storyTimerBridge.nativeClear;
    window.setTimeout = function(fn, delay) {
      const args = Array.prototype.slice.call(arguments, 2);
      const d = Number(delay) || 0;
      const isStoryNext = typeof fn === 'function' && fn.name === 'nextStory' && d >= 1000 && d <= 20000;
      if (!isStoryNext) return nativeSet.apply(window, [fn, delay].concat(args));
      let virtualId = 0;
      const rec = {fn:fn,args:args,remaining:d,startedAt:Date.now(),paused:false,underlying:0};
      const fire = function(){
        storyTimerBridge.records.delete(virtualId);
        if (!rec.paused) fn.apply(window,args);
      };
      rec.underlying = nativeSet(fire,d);
      virtualId = rec.underlying;
      storyTimerBridge.records.set(virtualId,rec);
      return virtualId;
    };
    window.clearTimeout = function(id) {
      const rec = storyTimerBridge.records.get(id);
      if (rec) {
        nativeClear(rec.underlying);
        storyTimerBridge.records.delete(id);
        return;
      }
      nativeClear(id);
    };
  }
  function currentStoryProgressBar() {
    const bars = Array.from(document.querySelectorAll('#story-progress b'));
    return bars.find(function(b){ return String(b.style.transition || '').indexOf('width') >= 0; }) || null;
  }
  function pauseStoryTimer(on) {
    installStoryTimerBridge();
    const nativeSet=storyTimerBridge.nativeSet, nativeClear=storyTimerBridge.nativeClear;
    storyTimerBridge.records.forEach(function(rec,key){
      if (on && !rec.paused) {
        rec.remaining = Math.max(120, rec.remaining - Math.max(0, Date.now() - rec.startedAt));
        nativeClear(rec.underlying);
        rec.paused = true;
        const b=currentStoryProgressBar();
        if (b && b.parentElement) {
          const tw=b.parentElement.getBoundingClientRect().width || 1;
          const bw=b.getBoundingClientRect().width;
          b.style.transition='none';
          b.style.width=Math.max(0,Math.min(100,bw/tw*100))+'%';
        }
      } else if (!on && rec.paused) {
        rec.paused=false;
        rec.startedAt=Date.now();
        const fire=function(){ storyTimerBridge.records.delete(key); rec.fn.apply(window,rec.args); };
        rec.underlying=nativeSet(fire,rec.remaining);
        const b=currentStoryProgressBar();
        if (b) {
          void b.offsetWidth;
          b.style.transition='width '+rec.remaining+'ms linear';
          b.style.width='100%';
        }
      }
    });
  }
  function installStoryPressPause() {
    const viewer=document.getElementById('story-viewer');
    if (!viewer || viewer.dataset.deappHoldPause === '1') return;
    viewer.dataset.deappHoldPause='1';
    installStoryTimerBridge();
    let pressed=false, downAt=0, suppressUntil=0;
    function interactiveBottom(target){ return !!(target && target.closest && target.closest('.story-bottom,.story-topbar,.story-sheet,.story-quick,input,textarea')); }
    viewer.addEventListener('pointerdown',function(e){
      if (viewer.hidden || interactiveBottom(e.target)) return;
      pressed=true; downAt=Date.now();
      pauseStoryTimer(true);
      root.classList.add('deapp-story-press-paused');
    },true);
    function release(){
      if (!pressed) return;
      const heldFor=Date.now()-downAt;
      pressed=false;
      pauseStoryTimer(false);
      root.classList.remove('deapp-story-press-paused');
      if (heldFor >= 220) suppressUntil=Date.now()+480;
    }
    document.addEventListener('pointerup',release,true);
    document.addEventListener('pointercancel',release,true);
    viewer.addEventListener('click',function(e){
      if (Date.now() < suppressUntil && e.target && e.target.closest && e.target.closest('.story-stage,.story-nav')) {
        e.preventDefault(); e.stopImmediatePropagation();
      }
    },true);
  }

  function scan() {
    document.querySelectorAll('.modal-overlay:not(#composer-modal) .modal-box,.deapp-cookie-modal .cookie-modal-card,dialog[open] .c-modal-box,.post-card .menu-wrap.open>.dropdown,.deapp-native-profile-options-sheet,#story-sheet:not([hidden]),.reel-sheet:not([hidden]) .reel-sheet-panel,.reel-sheet:not([hidden]) .reel-more-panel').forEach(wireSheet);
    document.querySelectorAll('.tabs,.story-tabs,.nx-studio-nav,[role="tablist"],.feed-tabs,.reels-tabs,.community-tabs,.settings-nav').forEach(wireHorizontalTabs);
    ensurePostBackdrop();
    injectAboutSettings();
    collapseLongPosts();
    wireReelsChrome();
    enhancePostDetailComments();
    installReactionHaptics();
    installStoryPressPause();
    wirePostActionCounts();
    optimizeMediaLoading();
    removePostTranslationUI();
    installPostPublishedHook();
    wireSectionChrome();
    syncSession();
    syncTheme();
    syncPageChrome();
    syncComposer();
    syncWebSheetState();
    syncReelsOverlayState();
    recoverPageInteraction();
  }

  scan();
  recoverPageInteraction();

  // DOM Deapp cukup dinamis. Scan langsung pada setiap mutasi dapat menumpuk ratusan
  // callback dan mengganggu klik/ketikan. Debounce singkat menjaga UI responsif tanpa scan per-frame.
  let scanQueued = false;
  let scanTimer = 0;
  function scheduleScan() {
    if (scanQueued) return;
    scanQueued = true;
    clearTimeout(scanTimer);
    scanTimer = setTimeout(function(){
      scanQueued = false;
      if (!document.hidden) scan();
    }, 64);
  }
  const observer = new MutationObserver(scheduleScan);
  observer.observe(document.body, {subtree:true, childList:true, attributes:true, attributeFilter:['class','open','hidden','aria-expanded','aria-hidden','style']});
  observer.observe(root, {attributes:true, attributeFilter:['class','data-theme']});
  // Capture juga scroll pada panel internal (chat, AI, modal), bukan hanya window.
  document.addEventListener('scroll', onAnyScroll, {passive:true,capture:true});
  window.addEventListener('scroll', onAnyScroll, {passive:true});
  window.addEventListener('pageshow', recoverPageInteraction, {passive:true});
  window.addEventListener('focus', recoverPageInteraction, {passive:true});
  document.addEventListener('visibilitychange', function(){
    if (!document.hidden) recoverPageInteraction();
  }, {passive:true});

  window.__DEAPP_NATIVE_V19__ = {
    refresh: scan,
    recoverScroll: recoverPageScroll,
    recoverInteraction: recoverPageInteraction,
    openComposer: openComposer,
    closeComposer: closeComposer,
    submitComposer: submitComposer,
    openProfileOptions: openProfileOptions,
    setExternalSheetOpen: setExternalSheetOpen
  };
  return true;
})();
