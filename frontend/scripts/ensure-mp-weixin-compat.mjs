import fs from "node:fs";
import path from "node:path";

const componentDir = path.resolve("dist/build/mp-weixin/components");
const files = {
  "BottomNav.js": "Component({});\n",
  "BottomNav.json": "{\n  \"component\": true,\n  \"usingComponents\": {}\n}\n",
  "BottomNav.wxml": "<view style=\"display:none\"></view>\n",
  "BottomNav.wxss": "\n",
};

fs.mkdirSync(componentDir, { recursive: true });

for (const [name, content] of Object.entries(files)) {
  const filePath = path.join(componentDir, name);
  if (!fs.existsSync(filePath)) {
    fs.writeFileSync(filePath, content, "utf8");
  }
}
