import fs from "node:fs";
import path from "node:path";
import { fileURLToPath } from "node:url";
import { PNG } from "pngjs";

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "..");
const iconDir = path.join(root, "src", "static", "icons");
const tabDir = path.join(root, "src", "static", "tabbar");

fs.mkdirSync(iconDir, { recursive: true });
fs.mkdirSync(tabDir, { recursive: true });

const colors = {
  ink: "#111827",
  muted: "#64748B",
  light: "#FFFFFF",
  primary: "#0A4BFE",
  danger: "#EF4444",
  warning: "#F59E0B",
  success: "#10B981",
};

function svg(paths, color = colors.muted, options = {}) {
  const { fill = "none", strokeWidth = 1.9, extra = "" } = options;
  const body = Array.isArray(paths) ? paths.join("") : paths;
  return `<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="${fill}" stroke="${color}" stroke-width="${strokeWidth}" stroke-linecap="round" stroke-linejoin="round"${extra}>${body}</svg>`;
}

const p = {
  home: '<path d="M4 10.4 12 4l8 6.4"/><path d="M6.6 10.2v8.2h10.8v-8.2"/><path d="M10 18.4v-4.5h4v4.5"/>',
  category: '<rect x="4.5" y="4.5" width="6" height="6" rx="1.5"/><rect x="13.5" y="4.5" width="6" height="6" rx="1.5"/><rect x="4.5" y="13.5" width="6" height="6" rx="1.5"/><rect x="13.5" y="13.5" width="6" height="6" rx="1.5"/>',
  community: '<path d="M5 6.8h14v8.4H9.4L5 18.4V6.8Z"/>',
  profile: '<circle cx="12" cy="8.2" r="3.2"/><path d="M5.5 19.2c.9-3.2 3.2-4.8 6.5-4.8s5.6 1.6 6.5 4.8"/>',
  search: '<circle cx="10.8" cy="10.8" r="5.6"/><path d="m15 15 4.3 4.3"/>',
  back: '<path d="M14.8 5.2 8 12l6.8 6.8"/>',
  chevronRight: '<path d="m9.2 5.2 6.8 6.8-6.8 6.8"/>',
  chevronDown: '<path d="m6 9.2 6 6 6-6"/>',
  more: '<circle cx="6" cy="12" r="1.2" fill="currentColor" stroke="none"/><circle cx="12" cy="12" r="1.2" fill="currentColor" stroke="none"/><circle cx="18" cy="12" r="1.2" fill="currentColor" stroke="none"/>',
  circle: '<circle cx="12" cy="12" r="5.8"/>',
  close: '<path d="M7 7l10 10M17 7 7 17"/>',
  trash: '<path d="M8 7.2h8"/><path d="M10 7.2V5.4h4v1.8"/><path d="M9 9.5v8.8h6V9.5"/>',
  eye: '<path d="M3.8 12s3-5 8.2-5 8.2 5 8.2 5-3 5-8.2 5-8.2-5-8.2-5Z"/><circle cx="12" cy="12" r="2.2"/>',
  eyeOff: '<path d="M4.8 4.8 19.2 19.2"/><path d="M9.1 6.3A9.4 9.4 0 0 1 12 5.9c5.2 0 8.2 5.1 8.2 5.1a13 13 0 0 1-2.6 3.2"/><path d="M14.3 14.3A3.2 3.2 0 0 1 9.7 9.7"/><path d="M6.2 8.4A13.3 13.3 0 0 0 3.8 11s3 5.1 8.2 5.1c1 0 2-.2 2.8-.5"/>',
  heart: '<path d="M12 19.2S5 15.1 5 9.5a3.7 3.7 0 0 1 6.4-2.5l.6.7.6-.7A3.7 3.7 0 0 1 19 9.5c0 5.6-7 9.7-7 9.7Z"/>',
  comment: '<path d="M5 6.5h14v9H9.6L5 18.5v-12Z"/>',
  star: '<path d="m12 4.8 2.2 4.4 4.9.7-3.5 3.4.8 4.8-4.4-2.3-4.4 2.3.8-4.8-3.5-3.4 4.9-.7L12 4.8Z"/>',
  share: '<circle cx="7" cy="12" r="2.2"/><circle cx="17" cy="7" r="2.2"/><circle cx="17" cy="17" r="2.2"/><path d="m9 11 6-3M9 13l6 3"/>',
  plus: '<path d="M12 5.5v13M5.5 12h13"/>',
  check: '<path d="m6 12.5 3.7 3.7L18.5 7.4"/>',
  pin: '<path d="M12 20s6-5.5 6-10.2A6 6 0 0 0 6 9.8C6 14.5 12 20 12 20Z"/><circle cx="12" cy="9.8" r="2.1"/>',
  service: '<rect x="5" y="5" width="14" height="14" rx="3"/><path d="M8.5 9.2h7M8.5 13h5"/>',
  grid: '<rect x="5" y="5" width="5.5" height="5.5" rx="1.2"/><rect x="13.5" y="5" width="5.5" height="5.5" rx="1.2"/><rect x="5" y="13.5" width="5.5" height="5.5" rx="1.2"/><rect x="13.5" y="13.5" width="5.5" height="5.5" rx="1.2"/>',
  gear: '<circle cx="12" cy="12" r="3"/><path d="M12 4.8v2M12 17.2v2M4.8 12h2M17.2 12h2M6.9 6.9l1.4 1.4M15.7 15.7l1.4 1.4M17.1 6.9l-1.4 1.4M8.3 15.7l-1.4 1.4"/>',
  bell: '<path d="M7.5 10.5a4.5 4.5 0 0 1 9 0v3.8l1.5 2H6l1.5-2v-3.8Z"/><path d="M10 18.3a2.2 2.2 0 0 0 4 0"/>',
  wallet: '<path d="M5 7.5h13.5v10H5a2 2 0 0 1-2-2v-8a2 2 0 0 1 2-2h12"/><path d="M15 11h4v3h-4a1.5 1.5 0 0 1 0-3Z"/>',
  order: '<path d="M7 4.8h10v14.4H7z"/><path d="M9.5 8.5h5M9.5 12h5M9.5 15.5h3"/>',
  member: '<path d="m12 4.8 2.7 4 4.6 1.3-2.9 3.7.2 4.8-4.6-1.7-4.6 1.7.2-4.8-2.9-3.7 4.6-1.3L12 4.8Z"/>',
  asset: '<path d="M5 8.5 12 5l7 3.5-7 3.5-7-3.5Z"/><path d="M5 12.2 12 15.7l7-3.5"/><path d="M5 15.9 12 19.4l7-3.5"/>',
  income: '<path d="M12 5v14"/><path d="M8 8.2h5.7a2.3 2.3 0 0 1 0 4.6h-3.4a2.3 2.3 0 0 0 0 4.6H16"/>',
  repair: '<path d="m14.5 5.5 4 4-8.8 8.8H5.7v-4z"/><path d="m12.7 7.3 4 4"/>',
  message: '<path d="M5 6.5h14v9H9.6L5 18.5v-12Z"/><path d="M8.5 9.7h7M8.5 12.6h5"/>',
  cart: '<path d="M6.5 6.5h1.8l1.2 8.2h7.2l1.2-5.3H9.1"/><circle cx="10.2" cy="18" r="1.2"/><circle cx="16.5" cy="18" r="1.2"/>',
  play: '<path d="M9 6.5 18 12l-9 5.5V6.5Z" fill="currentColor" stroke="none"/>',
  success: '<circle cx="12" cy="12" r="8"/><path d="m8.4 12.2 2.4 2.4 4.9-5.2"/>',
  fail: '<circle cx="12" cy="12" r="8"/><path d="m9 9 6 6M15 9l-6 6"/>',
  pending: '<circle cx="12" cy="12" r="8"/><path d="M12 7.5v5l3 2"/>',
  empty: '<path d="M6.2 8.4 12 5l5.8 3.4v6.8L12 18.6l-5.8-3.4V8.4Z"/><path d="M6.3 8.5 12 11.9l5.7-3.4"/><path d="M12 11.9v6.5"/>',
  signal: '<path d="M5 17h2.4v-3H5v3Zm5.8 0h2.4v-6h-2.4v6Zm5.8 0H19V7h-2.4v10Z" fill="currentColor" stroke="none"/>',
  wifi: '<path d="M5.5 10.4a10 10 0 0 1 13 0"/><path d="M8.2 13.1a5.8 5.8 0 0 1 7.6 0"/><path d="M11.9 16.4h.2"/>',
  battery: '<rect x="4.5" y="8.2" width="13.2" height="7.6" rx="1.7"/><path d="M19.5 10.5v3"/><path d="M7 10.5h7.7v3H7z" fill="currentColor" stroke="none"/>',
};

const icons = {
  "app-logo": svg('<circle cx="12" cy="12" r="5.2"/><path d="M3.7 13.2c3.3-4.4 10-6.2 16.6-3.3"/><path d="M4.4 16.4c4.3 2.6 10.8 1.5 15.2-3.1"/>', colors.primary),
  search: svg(p.search),
  back: svg(p.back, colors.ink),
  "back-light": svg(p.back, colors.light),
  "chevron-right": svg(p.chevronRight),
  "chevron-down": svg(p.chevronDown),
  more: svg(p.more, colors.ink, { extra: ' style="color:#111827"' }),
  "more-light": svg(p.more, colors.light, { extra: ' style="color:#ffffff"' }),
  circle: svg(p.circle, colors.ink),
  "circle-light": svg(p.circle, colors.light),
  close: svg(p.close, colors.light),
  trash: svg(p.trash),
  eye: svg(p.eye),
  "eye-off": svg(p.eyeOff),
  heart: svg(p.heart),
  "heart-active": svg(p.heart, colors.danger, { fill: "#EF4444" }),
  comment: svg(p.comment),
  star: svg(p.star),
  "star-active": svg(p.star, colors.warning, { fill: "#F59E0B" }),
  share: svg(p.share),
  plus: svg(p.plus, colors.light),
  check: svg(p.check, colors.light),
  "check-blue": svg(p.check, colors.primary),
  pin: svg(p.pin, colors.primary),
  service: svg(p.service),
  grid: svg(p.grid),
  gear: svg(p.gear, colors.light),
  bell: svg(p.bell, colors.light),
  wallet: svg(p.wallet, colors.primary),
  order: svg(p.order, colors.primary),
  member: svg(p.member, colors.primary),
  asset: svg(p.asset, colors.primary),
  income: svg(p.income, colors.primary),
  distribution: svg(p.share, colors.primary),
  repair: svg(p.repair, colors.primary),
  message: svg(p.message, colors.primary),
  cart: svg(p.cart, colors.primary),
  play: svg(p.play, colors.light, { extra: ' style="color:#ffffff"' }),
  success: svg(p.success, colors.success),
  fail: svg(p.fail, colors.danger),
  pending: svg(p.pending, colors.warning),
  empty: svg(p.empty, "#CBD5E1"),
  "status-signal": svg(p.signal, colors.ink, { extra: ' style="color:#111827"' }),
  "status-signal-light": svg(p.signal, colors.light, { extra: ' style="color:#ffffff"' }),
  "status-wifi": svg(p.wifi, colors.ink),
  "status-wifi-light": svg(p.wifi, colors.light),
  "status-battery": svg(p.battery, colors.ink, { extra: ' style="color:#111827"' }),
  "status-battery-light": svg(p.battery, colors.light, { extra: ' style="color:#ffffff"' }),
};

for (const [name, content] of Object.entries(icons)) {
  fs.writeFileSync(path.join(iconDir, `${name}.svg`), content);
}

const tabPaths = {
  home: p.home,
  category: p.category,
  community: p.community,
  profile: p.profile,
};

function setPixel(png, x, y, rgba) {
  if (x < 0 || y < 0 || x >= png.width || y >= png.height) return;
  const idx = (png.width * y + x) << 2;
  png.data[idx] = rgba[0];
  png.data[idx + 1] = rgba[1];
  png.data[idx + 2] = rgba[2];
  png.data[idx + 3] = rgba[3];
}

function parseColor(hex) {
  return [parseInt(hex.slice(1, 3), 16), parseInt(hex.slice(3, 5), 16), parseInt(hex.slice(5, 7), 16), 255];
}

function drawLine(png, x1, y1, x2, y2, rgba, width = 4) {
  const steps = Math.max(Math.abs(x2 - x1), Math.abs(y2 - y1)) * 2;
  for (let i = 0; i <= steps; i++) {
    const t = steps === 0 ? 0 : i / steps;
    const x = x1 + (x2 - x1) * t;
    const y = y1 + (y2 - y1) * t;
    fillCircle(png, Math.round(x), Math.round(y), width / 2, rgba);
  }
}

function fillCircle(png, cx, cy, r, rgba) {
  for (let y = Math.floor(cy - r); y <= Math.ceil(cy + r); y++) {
    for (let x = Math.floor(cx - r); x <= Math.ceil(cx + r); x++) {
      if ((x - cx) ** 2 + (y - cy) ** 2 <= r ** 2) setPixel(png, x, y, rgba);
    }
  }
}

function strokeRect(png, x, y, w, h, rgba) {
  drawLine(png, x, y, x + w, y, rgba);
  drawLine(png, x + w, y, x + w, y + h, rgba);
  drawLine(png, x + w, y + h, x, y + h, rgba);
  drawLine(png, x, y + h, x, y, rgba);
}

function strokeCircle(png, cx, cy, r, rgba) {
  let prev;
  for (let a = 0; a <= 360; a += 4) {
    const rad = (a * Math.PI) / 180;
    const point = [cx + Math.cos(rad) * r, cy + Math.sin(rad) * r];
    if (prev) drawLine(png, prev[0], prev[1], point[0], point[1], rgba);
    prev = point;
  }
}

function drawTabIcon(name, color) {
  const png = new PNG({ width: 96, height: 96 });
  const c = parseColor(color);
  if (name === "home") {
    drawLine(png, 24, 43, 48, 23, c);
    drawLine(png, 48, 23, 72, 43, c);
    strokeRect(png, 30, 43, 36, 30, c);
    drawLine(png, 43, 73, 43, 58, c);
    drawLine(png, 53, 58, 53, 73, c);
    drawLine(png, 43, 58, 53, 58, c);
  }
  if (name === "category") {
    for (const [x, y] of [[25, 25], [53, 25], [25, 53], [53, 53]]) strokeRect(png, x, y, 18, 18, c);
  }
  if (name === "community") {
    strokeRect(png, 22, 28, 52, 34, c);
    drawLine(png, 38, 62, 26, 74, c);
    drawLine(png, 38, 62, 45, 62, c);
  }
  if (name === "profile") {
    strokeCircle(png, 48, 34, 12, c);
    drawLine(png, 26, 73, 31, 62, c);
    drawLine(png, 31, 62, 48, 56, c);
    drawLine(png, 48, 56, 65, 62, c);
    drawLine(png, 65, 62, 70, 73, c);
  }
  return png;
}

for (const name of Object.keys(tabPaths)) {
  for (const [suffix, color] of [["", "#94A3B8"], ["-active", colors.primary]]) {
    const png = drawTabIcon(name, color);
    fs.writeFileSync(path.join(tabDir, `${name}${suffix}.png`), PNG.sync.write(png));
  }
}
