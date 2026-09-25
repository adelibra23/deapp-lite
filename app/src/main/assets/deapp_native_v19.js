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
  const root = document.documentElement;
  const path = (location.pathname || '').toLowerCase();
  const isProfilePage = /\/profile\.php$/.test(path);
  const isSettingsPage = /\/settings\.php$/.test(path);
  const isPostDetailPage = /\/post\.php$/.test(path);
  const isAuthPage = /\/(login|register)\.php$/.test(path);

  root.classList.add('deapp-native-shell', 'deapp-native-v19');
  if (isProfilePage) root.classList.add('deapp-native-profile');
  if (isPostDetailPage) root.classList.add('deapp-native-post-detail');

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

    .page-home .feed-tabs{border-left:0!important;border-right:0!important;border-top:0!important;border-radius:0!important;box-shadow:none!important;background:var(--surface)!important}
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
    .post-card:not(.post-embedded) .post-actions{
      justify-content:flex-start!important;gap:1px!important;padding:3px 10px 1px 56px!important;border-top:0!important;min-height:44px!important;overflow:visible!important;
    }
    .post-card:not(.post-embedded) .post-actions .react-wrap{flex:0 0 auto!important}
    .post-card:not(.post-embedded) .act-btn{
      flex:0 0 42px!important;width:42px!important;min-width:42px!important;height:42px!important;min-height:42px!important;
      padding:9px!important;border-radius:50%!important;gap:0!important;color:var(--text)!important;background:transparent!important;
    }
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
    .deapp-native-post-detail .btn-send{width:38px!important;height:38px!important;flex:0 0 38px!important;border-radius:50%!important;margin-bottom:1px!important}
    .deapp-native-post-detail .bottom-nav{display:none!important}
  `;
  document.head.appendChild(style);

  function nativeTap() {
    try { if (API && API.tap) API.tap(); } catch (_) {}
  }

  function actionable(node) {
    return node && node.closest ? node.closest('a,button,[role="button"],.btn,.icon-btn,.tab,.dropdown-item,.nav-link,.post-card[data-url],.post-card[onclick],.qa-card[data-url],.qa-card[onclick],.post-action,.comment-action') : null;
  }

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
    const n = document.querySelector('.profile-name > span,.profile-name');
    return n ? (n.textContent || '').trim().replace(/\s+/g,' ') : 'Profil';
  }

  function postAuthorTitle() {
    const n = document.querySelector('.post-card:not(.post-embedded) .post-name');
    return n ? (n.textContent || '').trim().replace(/\s+/g,' ') : 'Postingan';
  }

  function syncPageChrome() {
    try {
      if (!API || !API.syncPageChrome) return;
      const ownProfile = !!document.querySelector('.profile-actions a[href^="settings.php"],.profile-actions a[href*="settings.php"]');
      const type = isProfilePage ? 'profile' : (isPostDetailPage ? 'post' : (isSettingsPage ? 'settings' : (isAuthPage ? 'auth' : 'default')));
      const title = isProfilePage ? profileTitle() : (isPostDetailPage ? postAuthorTitle() : '');
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
    if (!nav || nav.querySelector('.deapp-native-about-row')) return;
    const group = document.createElement('div');
    group.className = 'snav-group deapp-native-about-group';
    group.textContent = 'Aplikasi';

    const server = document.createElement('button');
    server.type = 'button';
    server.className = 'snav deapp-native-server-row';
    server.innerHTML = '<span style="font-size:20px;line-height:1">⌁</span><span><b>Ganti server</b><small>Hosting, XAMPP atau alamat server Deapp</small></span>';
    server.addEventListener('click', function(){
      nativeTap();
      try { if (API && API.showServerSettings) API.showServerSettings(); } catch (_) {}
    });

    const permissions = document.createElement('button');
    permissions.type = 'button';
    permissions.className = 'snav deapp-native-permission-row';
    permissions.innerHTML = '<span style="font-size:20px;line-height:1">◈</span><span><b>Perizinan aplikasi</b><small>Lokasi, kamera, mikrofon, notifikasi, media dan lainnya</small></span>';
    permissions.addEventListener('click', function(){
      nativeTap();
      try { if (API && API.showPermissions) API.showPermissions(); } catch (_) {}
    });

    const row = document.createElement('button');
    row.type = 'button';
    row.className = 'snav deapp-native-about-row';
    row.innerHTML = '<span style="font-size:20px;line-height:1">ⓘ</span><span><b>Tentang aplikasi</b><small>Deapp Lite untuk Android</small></span><span class="deapp-version-pill">v1.9.4-lite</span>';
    row.addEventListener('click', function(){
      nativeTap();
      try { if (API && API.showAboutApp) API.showAboutApp(); } catch (_) {}
    });
    nav.appendChild(group);
    nav.appendChild(server);
    nav.appendChild(permissions);
    nav.appendChild(row);
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
    return !!el.closest('.modal-overlay:not(#composer-modal) .modal-box,.deapp-cookie-modal .cookie-modal-card,dialog.c-modal[open] .c-modal-box,.post-card .menu-wrap.open>.dropdown,.deapp-native-profile-options-sheet');
  }

  function hasVisibleWebSheet() {
    const selectors = [
      '.modal-overlay:not(#composer-modal) .modal-box',
      '.deapp-cookie-modal .cookie-modal-card',
      'dialog.c-modal[open] .c-modal-box',
      '.post-card .menu-wrap.open>.dropdown',
      '.deapp-native-profile-options-sheet'
    ];
    for (const selector of selectors) {
      const nodes = document.querySelectorAll(selector);
      for (const node of nodes) {
        if (isVisible(node)) return true;
      }
    }
    return false;
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

  function setExternalSheetOpen(open) {
    externalNativeSheetOpen = !!open;
    // Deliberately do not lock the DOM for a native sheet. The Android overlay is enough.
    // When it closes, also self-heal any stale web lock state.
    if (!externalNativeSheetOpen && !hasVisibleWebSheet()) {
      currentWebSheetOpen = false;
      unlockBackgroundScroll(false);
    }
    return true;
  }

  function guardBackgroundGesture(e) {
    if (!backgroundScrollLocked) return;
    if (!hasVisibleWebSheet()) {
      currentWebSheetOpen = false;
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
    document.querySelectorAll('.comment-form').forEach(function(form){
      const input = form.querySelector('.comment-input');
      if (!input || input.dataset.deappThreadComment === '1') return;
      input.dataset.deappThreadComment = '1';

      // Jangan mengganti elemen input asli. Mengganti node dapat menghilangkan
      // event listener milik Deapp dan membuat kirim komentar/mention tidak berfungsi.
      const multiline = input.tagName === 'TEXTAREA';
      const sync = function(){
        const has = (input.value || '').trim().length > 0;
        form.classList.toggle('deapp-comment-typing', has);
        if (multiline) {
          input.style.height = 'auto';
          input.style.height = Math.min(126, Math.max(28, input.scrollHeight)) + 'px';
        }
      };
      input.addEventListener('input', sync, {passive:true});
      input.addEventListener('focus', sync, {passive:true});
      input.addEventListener('blur', sync, {passive:true});
      // Biarkan handler submit/Enter asli Deapp bekerja. Patch hanya menangani visual.
      sync();
    });
  }

  function scan() {
    document.querySelectorAll('.modal-overlay:not(#composer-modal) .modal-box,.deapp-cookie-modal .cookie-modal-card,dialog.c-modal[open] .c-modal-box,.post-card .menu-wrap.open>.dropdown,.deapp-native-profile-options-sheet').forEach(wireSheet);
    document.querySelectorAll('.tabs,.story-tabs,.nx-studio-nav,[role="tablist"],.feed-tabs,.reels-tabs,.community-tabs,.settings-nav').forEach(wireHorizontalTabs);
    ensurePostBackdrop();
    injectAboutSettings();
    enhancePostDetailComments();
    optimizeMediaLoading();
    removePostTranslationUI();
    installPostPublishedHook();
    syncSession();
    syncPageChrome();
    syncComposer();
    syncWebSheetState();
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
  observer.observe(document.body, {subtree:true, childList:true, attributes:true, attributeFilter:['class','open','hidden','aria-expanded']});
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
