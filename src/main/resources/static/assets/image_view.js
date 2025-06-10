function showImageOverlay(imageUrl, caption = '') {
	const overlay = document.createElement('div');
	overlay.style.position = 'fixed';
	overlay.style.top = 0;
	overlay.style.left = 0;
	overlay.style.width = '100vw';
	overlay.style.height = '100vh';
	overlay.style.background = 'rgba(0,0,0,0.85)';
	overlay.style.zIndex = 9999;
	overlay.style.display = 'flex';
	overlay.style.flexDirection = 'column';
	overlay.style.justifyContent = 'center';
	overlay.style.alignItems = 'center';
	overlay.style.overflow = 'auto';
	overlay.style.padding = '0vw';
	const centerWrap = document.createElement('div');
	centerWrap.style.display = 'flex';
	centerWrap.style.flexDirection = 'column';
	centerWrap.style.alignItems = 'center';
	centerWrap.style.justifyContent = 'center';
	centerWrap.style.width = '100%';
	centerWrap.style.height = '100%';
	const img = document.createElement('img');
	img.src = imageUrl;
	img.alt = caption;
	img.style.maxWidth = '96vw';
	img.style.maxHeight = '60vh';
	img.style.width = 'auto';
	img.style.height = 'auto';
	img.style.borderRadius = '10px';
	img.style.boxShadow = '0 4px 18px 2px rgba(0,0,0,0.65)';
	img.style.objectFit = 'contain';
	img.style.display = 'block';
	img.style.margin = '0 auto';
	let captionEl;
	if (caption) {
		captionEl = document.createElement('div');
		captionEl.textContent = caption;
		captionEl.style.marginTop = '20px';
		captionEl.style.color = 'white';
		captionEl.style.fontSize = '1.15em';
		captionEl.style.fontWeight = '500';
		captionEl.style.textAlign = 'center';
		captionEl.style.textShadow = '0 2px 8px rgba(0,0,0,0.8)';
		captionEl.style.maxWidth = '96vw';
		captionEl.style.wordBreak = 'break-word';
	}

	// Большая кнопка закрытия
	const closeBtn = document.createElement('button');
	closeBtn.textContent = '×';
	closeBtn.setAttribute('aria-label', 'Закрыть');
	closeBtn.style.position = 'fixed';
	closeBtn.style.top = '16px';
	closeBtn.style.right = '16px';
	closeBtn.style.width = '56px';
	closeBtn.style.height = '56px';
	closeBtn.style.fontSize = '42px';
	closeBtn.style.borderRadius = '50%';
	closeBtn.style.background = 'rgba(0,0,0,0.85)';
	closeBtn.style.border = '3px solid white';
	closeBtn.style.color = '#fff';
	closeBtn.style.boxShadow = '0 4px 12px rgba(0,0,0,0.5)';
	closeBtn.style.cursor = 'pointer';
	closeBtn.style.display = 'flex';
	closeBtn.style.alignItems = 'center';
	closeBtn.style.justifyContent = 'center';
	closeBtn.style.zIndex = 10000;
	closeBtn.style.transition = 'background 0.2s';
	closeBtn.onmouseenter = () => closeBtn.style.background = 'rgba(255,0,0,0.85)';
	closeBtn.onmouseleave = () => closeBtn.style.background = 'rgba(0,0,0,0.85)';
	closeBtn.onclick = () => {
		if (document.body.contains(overlay)) document.body.removeChild(overlay);
		if (document.head.contains(styleTag)) document.head.removeChild(styleTag);
	};
	centerWrap.appendChild(img);
	if (captionEl) centerWrap.appendChild(captionEl);
	overlay.appendChild(closeBtn);
	overlay.appendChild(centerWrap);
	overlay.addEventListener('click', (e) => {
		if (e.target === overlay) {
			if (document.body.contains(overlay)) document.body.removeChild(overlay);
			if (document.head.contains(styleTag)) document.head.removeChild(styleTag);
		}
	});
	const styleTag = document.createElement('style');
	styleTag.textContent = `
    @media (max-width: 600px) {
      .image-overlay-img {
        max-width: 98vw !important;
        max-height: 48vh !important;
      }
      .image-overlay-caption {
        font-size: 1em !important;
      }
      .image-overlay-close {
        width: 48px !important;

		        height: 48px !important;
		        font-size: 34px !important;
		        border-width: 2px !important;
		        top: 9px !important;
		        right: 9px !important;
		      }
		    }
		  `;
	document.head.appendChild(styleTag);
	img.className = 'image-overlay-img';
	if (captionEl) captionEl.className = 'image-overlay-caption';
	closeBtn.className = 'image-overlay-close';
	document.body.appendChild(overlay);
}