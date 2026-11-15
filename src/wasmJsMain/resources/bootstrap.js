// A small deployment manifest chooses both the application build and its API.
async function start() {
  const response = await fetch(new URL("config.json", document.baseURI), {cache: "no-cache"});
  if (!response.ok) throw new Error("Configuration unavailable");
  const config = await response.json();
  const entry = new URL(config.appEntry, document.baseURI);
  if (!config.appEntry || !["https:", "http:"].includes(entry.protocol)) throw new Error("Invalid entry");
  window.watajuConfig = JSON.stringify(config);
  const script = document.createElement("script");
  script.src = entry.href;
  script.onerror = showFailure;
  document.body.append(script);
}

function showFailure() {
  const en = navigator.language.toLowerCase().startsWith("en");
  document.documentElement.lang = en ? "en" : "ja";
  const message = document.createElement("p");
  message.textContent = en ? "Could not open the store." : "店舗を開けませんでした。";
  const retry = document.createElement("button");
  retry.textContent = en ? "Retry" : "再読み込み";
  retry.onclick = () => location.reload();
  document.body.replaceChildren(message, retry);
}

start().catch(showFailure);
