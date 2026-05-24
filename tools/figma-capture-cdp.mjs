const [targetUrl, captureId, endpoint] = process.argv.slice(2);

if (!targetUrl || !captureId || !endpoint) {
  throw new Error("Usage: node tools/figma-capture-cdp.mjs <url> <captureId> <endpoint>");
}

const sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms));

async function getJson(url) {
  const response = await fetch(url);
  if (!response.ok) throw new Error(`${url} -> ${response.status}`);
  return response.json();
}

async function waitForPage() {
  for (let i = 0; i < 80; i += 1) {
    try {
      const pages = await getJson("http://127.0.0.1:9222/json");
      const page = pages.find((entry) => entry.type === "page" && entry.url.includes("figma-import.html"));
      if (page) return page;
    } catch {}
    await sleep(250);
  }
  throw new Error("Timed out waiting for Chrome DevTools page");
}

function makeCdp(wsUrl) {
  const ws = new WebSocket(wsUrl);
  let nextId = 1;
  const pending = new Map();

  ws.addEventListener("message", (event) => {
    const msg = JSON.parse(event.data);
    if (msg.id && pending.has(msg.id)) {
      const { resolve, reject } = pending.get(msg.id);
      pending.delete(msg.id);
      if (msg.error) reject(new Error(JSON.stringify(msg.error)));
      else resolve(msg.result);
      return;
    }
    if (msg.method === "Runtime.consoleAPICalled") {
      const text = msg.params.args.map((arg) => arg.value ?? arg.description ?? "").join(" ");
      console.log(`[console.${msg.params.type}] ${text}`);
    }
    if (msg.method === "Runtime.exceptionThrown") {
      console.log(`[exception] ${msg.params.exceptionDetails.text}`);
    }
    if (msg.method === "Log.entryAdded") {
      console.log(`[log.${msg.params.entry.level}] ${msg.params.entry.text}`);
    }
  });

  const open = new Promise((resolve, reject) => {
    ws.addEventListener("open", resolve, { once: true });
    ws.addEventListener("error", reject, { once: true });
  });

  return {
    open,
    send(method, params = {}) {
      const id = nextId;
      nextId += 1;
      const promise = new Promise((resolve, reject) => pending.set(id, { resolve, reject }));
      ws.send(JSON.stringify({ id, method, params }));
      return promise;
    },
    close() {
      ws.close();
    },
  };
}

const page = await waitForPage();
const cdp = makeCdp(page.webSocketDebuggerUrl);
await cdp.open;
await cdp.send("Runtime.enable");
await cdp.send("Log.enable");
await cdp.send("Page.enable");

await cdp.send("Page.navigate", { url: targetUrl });
await sleep(3000);

const expression = `
  (async () => {
    const sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms));
    await Promise.all([...document.images].map((img) => {
      if (img.complete && img.naturalWidth > 0) return Promise.resolve();
      return new Promise((resolve, reject) => {
        img.addEventListener("load", resolve, { once: true });
        img.addEventListener("error", () => reject(new Error("Image failed: " + img.src)), { once: true });
      });
    }));
    for (let i = 0; i < 80; i += 1) {
      if (window.figma && window.figma.captureForDesign) break;
      await sleep(250);
    }
    if (!window.figma || !window.figma.captureForDesign) {
      throw new Error("window.figma.captureForDesign is unavailable");
    }
    return await window.figma.captureForDesign({
      captureId: ${JSON.stringify(captureId)},
      endpoint: ${JSON.stringify(endpoint)},
      selector: "body",
      verbose: true,
      delayMs: 500
    });
  })()
`;

const result = await cdp.send("Runtime.evaluate", {
  expression,
  awaitPromise: true,
  returnByValue: true,
});

console.log(JSON.stringify(result, null, 2));
cdp.close();
