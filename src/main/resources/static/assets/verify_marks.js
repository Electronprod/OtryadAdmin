/**
 * verify_marks.js
 *
 * После успешной отправки отметок запрашивает сервер для проверки,
 * что записи действительно сохранились в нужном количестве.
 *
 * При обнаружении расхождения:
 *   1. Собирает полный снимок состояния JS и DOM
 *   2. Отправляет отчёт на /api/report_mark_error
 *   3. Показывает пользователю предупреждение через Swal
 *
 * Зависимости: SweetAlert2 (Swal), должны быть уже загружены к моменту вызова.
 *
 * Серверный контракт
 * ------------------
 * GET /api/verify_marks?event_id={id}
 *   200 → { event_id, saved_count, status: "ok" }
 *   Любой другой код → считается ошибкой верификации
 *
 * POST /api/report_mark_error
 *   Body: JSON (см. buildReport)
 *   Ответ игнорируется — репорт «выстрелить и забыть»
 */

// ─── Публичный API ────────────────────────────────────────────────────────────

/**
 * Верифицирует только что выставленные отметки.
 *
 * @param {number} eventId      - event_id, возвращённый сервером после mark
 * @param {Object} sentData     - объект, который был отправлен в mark (MarkDTO)
 * @param {number} expectedTotal - ожидаемое кол-во записей (present + unpresent)
 */
async function verifyMarks(eventId, sentData, expectedTotal) {
	// Небольшая задержка: даём БД завершить транзакцию перед проверкой
	await _sleep(600);

	let verifyResult = null;
	let fetchError = null;

	try {
		const resp = await fetch(`/api/verify_marks?event_id=${encodeURIComponent(eventId)}`);
		if (!resp.ok) {
			fetchError = `HTTP ${resp.status} ${resp.statusText}`;
		} else {
			verifyResult = await resp.json();
		}
	} catch (err) {
		fetchError = String(err);
	}

	// ── Анализ результата ────────────────────────────────────────────────────
	const problems = [];

	if (fetchError) {
		problems.push(`Не удалось выполнить запрос верификации: ${fetchError}`);
	} else {
		// status — trim + toLowerCase, чтобы не ловить " OK", "Ok" и т.п.
		const status = String(verifyResult.status ?? "").trim().toLowerCase();
		if (status !== "ok") {
			problems.push(`Сервер вернул статус верификации: "${verifyResult.status}"`);
		}

		// Явное приведение к числу с обеих сторон — защита от типа string vs number.
		// Jackson может сериализовать long как строку при WRITE_NUMBERS_AS_STRINGS.
		const savedCount = Number(verifyResult.saved_count);
		const expectedNum = Number(expectedTotal);

		if (Number.isNaN(savedCount)) {
			problems.push(
				`Поле saved_count отсутствует или не является числом` +
				` (получено: ${JSON.stringify(verifyResult.saved_count)},` +
				` тип: ${typeof verifyResult.saved_count})`
			);
		} else if (savedCount !== expectedNum) {
			// В скобках — сырые типы, чтобы сразу было видно если проблема типизации
			problems.push(
				`Ожидалось записей: ${expectedNum} (${typeof expectedTotal}),` +
				` сохранено на сервере: ${savedCount} (${typeof verifyResult.saved_count})`
			);
		}
	}

	if (problems.length === 0) {
		// Всё хорошо — тихо выходим
		console.info(`[verify_marks] OK: event_id=${eventId}, count=${verifyResult.saved_count}`);
		showNotification("Подтверждено", `<b>Отметки успешно выставлены!</b><br> OK: event_id=${eventId}, count=${verifyResult.saved_count}`, "success");
		return;
	}

	// ── Есть расхождение — собираем отчёт и уведомляем ─────────────────────
	console.error("[verify_marks] Обнаружены расхождения:", problems);

	const report = _buildReport({
		eventId,
		sentData,
		expectedTotal,
		verifyResult,
		fetchError,
		problems,
	});

	// Отправляем отчёт и показываем сообщение параллельно
	await Promise.all([
		_sendErrorReport(report),
		_showVerifyError(problems, report.meta.reportId),
	]);
}

// ─── Сборка отчёта ────────────────────────────────────────────────────────────

function _buildReport({ eventId, sentData, expectedTotal, verifyResult, fetchError, problems }) {
	return {
		meta: {
			reportId: _generateId(),
			timestamp: new Date().toISOString(),
			url: window.location.href,
			userAgent: navigator.userAgent,
			language: navigator.language,
			screenSize: `${screen.width}x${screen.height}`,
			viewportSize: `${window.innerWidth}x${window.innerHeight}`,
			connectionType: navigator.connection?.effectiveType ?? "unknown",
			onLine: navigator.onLine,
		},
		summary: {
			problems,
			eventId,
			expectedTotal,
			verifyResult,
			fetchError,
		},
		sentPayload: _sanitizePayload(sentData),
		domSnapshot: _captureDomSnapshot(),
		jsGlobals: _captureJsGlobals(),
		performanceTiming: _capturePerformanceTiming(),
		consoleErrors: _captureConsoleErrors(),
	};
}

/** Обрезает потенциально большой payload, чтобы не отправлять мегабайты */
function _sanitizePayload(data) {
	if (!data) return null;
	try {
		const clone = JSON.parse(JSON.stringify(data));
		const MAX_ITEMS = 200;
		if (Array.isArray(clone.presentPeople) && clone.presentPeople.length > MAX_ITEMS) {
			clone.presentPeople = [
				...clone.presentPeople.slice(0, MAX_ITEMS),
				`...(truncated, total ${clone.presentPeople.length})`,
			];
		}
		if (Array.isArray(clone.unpresentPeople) && clone.unpresentPeople.length > MAX_ITEMS) {
			clone.unpresentPeople = [
				...clone.unpresentPeople.slice(0, MAX_ITEMS),
				`...(truncated, total ${clone.unpresentPeople.length})`,
			];
		}
		return clone;
	} catch {
		return { serializationError: true, raw: String(data) };
	}
}

/** Снимок состояния таблицы отметок */
function _captureDomSnapshot() {
	const snapshot = {
		isAnyColumnHidden: typeof isAnyColumnHidden !== "undefined" ? isAnyColumnHidden : "undefined",
		eventType: null,
		dateField: null,
		searchInput: null,
		rows: [],
	};

	try {
		const eventEl = document.getElementById("event_type");
		snapshot.eventType = eventEl ? eventEl.value : "not found";

		const dateEl = document.getElementById("dateField");
		snapshot.dateField = dateEl ? dateEl.value : "not found";

		const searchEl = document.getElementById("searchInput");
		snapshot.searchInput = searchEl
			? { value: searchEl.value, display: searchEl.style.display }
			: "not found";

		const rows = document.querySelectorAll("#markTable tbody tr");
		rows.forEach((row) => {
			const checkbox = row.querySelector(".custom-checkbox");
			const reasonSelect = row.querySelector(".details-input");
			snapshot.rows.push({
				name: row.getAttribute("name"),
				display: row.style.display || "default",
				computedDisplay: getComputedStyle(row).display,
				checked: checkbox ? checkbox.checked : null,
				checkboxValue: checkbox ? checkbox.value : null,
				reason: reasonSelect ? reasonSelect.value : null,
				reasonDisabled: reasonSelect ? reasonSelect.disabled : null,
			});
		});
	} catch (err) {
		snapshot.captureError = String(err);
	}

	return snapshot;
}

/** Глобальные переменные модуля отметок */
function _captureJsGlobals() {
	const globals = {};
	const names = [
		"isAnyColumnHidden",
		"groupid",
		"people",
		"eventTypesWithReasons",
		"showEvents",
	];
	names.forEach((name) => {
		try {
			// eslint-disable-next-line no-eval
			globals[name] = typeof eval(name) !== "undefined" ? eval(name) : "undefined"; // jshint ignore:line
		} catch {
			globals[name] = "access error";
		}
	});
	return globals;
}

/** Базовые метрики performance */
function _capturePerformanceTiming() {
	try {
		const nav = performance.getEntriesByType("navigation")[0];
		if (!nav) return null;
		return {
			domContentLoaded: Math.round(nav.domContentLoadedEventEnd),
			loadEvent: Math.round(nav.loadEventEnd),
			responseTime: Math.round(nav.responseEnd - nav.requestStart),
		};
	} catch {
		return null;
	}
}

// Перехватчик ошибок — устанавливается один раз при загрузке модуля
const _capturedErrors = [];
(function _installErrorCapture() {
	const _origOnError = window.onerror;
	window.onerror = function(message, source, lineno, colno, error) {
		_capturedErrors.push({
			type: "onerror",
			message,
			source,
			lineno,
			colno,
			stack: error?.stack ?? null,
			time: new Date().toISOString(),
		});
		if (_capturedErrors.length > 50) _capturedErrors.shift();
		if (typeof _origOnError === "function") return _origOnError(...arguments);
		return false;
	};

	window.addEventListener("unhandledrejection", (ev) => {
		_capturedErrors.push({
			type: "unhandledrejection",
			reason: String(ev.reason),
			stack: ev.reason?.stack ?? null,
			time: new Date().toISOString(),
		});
		if (_capturedErrors.length > 50) _capturedErrors.shift();
	});
})();

function _captureConsoleErrors() {
	// Возвращаем копию накопленных ошибок
	return [..._capturedErrors];
}

// ─── Отправка отчёта ─────────────────────────────────────────────────────────

async function _sendErrorReport(report) {
	try {
		const csrfInput = document.querySelector('input[name="_csrf"]');
		const csrfToken = csrfInput ? csrfInput.value : "";

		const resp = await fetch("/api/report_mark_error", {
			method: "POST",
			headers: {
				"Content-Type": "application/json",
				...(csrfToken ? { "X-CSRF-TOKEN": csrfToken } : {}),
			},
			body: JSON.stringify(report),
			// keepalive: true — чтобы запрос дошёл даже при уходе со страницы
			keepalive: true,
		});
		if (!resp.ok) {
			console.warn(`[verify_marks] report_mark_error вернул ${resp.status}`);
		} else {
			console.info(`[verify_marks] Отчёт отправлен. reportId=${report.meta.reportId}`);
		}
	} catch (err) {
		// Ошибка репортинга не должна мешать UI
		console.error("[verify_marks] Не удалось отправить отчёт об ошибке:", err);
	}
}

// ─── UI ──────────────────────────────────────────────────────────────────────

async function _showVerifyError(problems, reportId) {
	const problemList = problems.map((p) => `<li>${_escapeHtml(p)}</li>`).join("");
	await Swal.fire({
		icon: "warning",
		title: "⚠️ Возможная ошибка выставления отметок",
		html: `
            <p>Отметки были отправлены, однако проверка обнаружила расхождения:</p>
            <ul style="text-align:left; margin: 8px 0 12px 0; padding-left: 20px;">
                ${problemList}
            </ul>
            <p style="margin-bottom:4px">
                Пожалуйста, <strong>сообщите администратору</strong> об этом.
            </p>
            <p style="font-size:12px; color:#999; margin-top:8px">
                Код отчёта: <code>${reportId}</code>
            </p>
        `,
		confirmButtonText: "Понятно",
		confirmButtonColor: "#e67e22",
		allowOutsideClick: false,
	});
}

// ─── Утилиты ─────────────────────────────────────────────────────────────────

function _sleep(ms) {
	return new Promise((resolve) => setTimeout(resolve, ms));
}

function _generateId() {
	return `vm-${Date.now().toString(36)}-${Math.random().toString(36).slice(2, 7)}`;
}

function _escapeHtml(str) {
	return String(str)
		.replace(/&/g, "&amp;")
		.replace(/</g, "&lt;")
		.replace(/>/g, "&gt;")
		.replace(/"/g, "&quot;");
}
function showNotification(title, text, icon) {
	setTimeout(() => {
		Swal.fire({
			title: title,
			html: text,
			icon: icon,
		});
	}, 3000);
}

console.debug("[verify_marks] loaded");