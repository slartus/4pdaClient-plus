var regex = /([^\n]*?)\n([^\n]*?)\n([\s\S]*?)$/;
var forPostUpdate =
    "[color=\"#0277bd\"][size=6][center]Неофициальный клиент 4pda.ru [INSERT_VERSION][/center][/size][/color]\n" +
    "\n" +
    "[b]Скачать[/b]: [url=\"INSERT_APK\"]ForPDA_INSERT_VERSION.apk[/url]\n" +
    "\n" +
    "[spoiler=Изменения]\n" +
    "[font=monospace][!] Важное замечание\n" +
    "[+] Нововведение\n" +
    "[-] Исправлена ошибка\n" +
    "[*] Изменения\n" +
    "\n" +
    "INSERT_TEXT[/font][/spoiler]";
var forUpdate =
    "  \"update_beta\":{\n" +
    "    \"ver\":\"INSERT_VERSION\",\n" +
    "    \"apk\":\"https://raw.githubusercontent.com/slartus/4pdaClient-plus/master/ForPDA_INSERT_VERSION.apk\",\n" +
    "    \"info\":\"INSERT_TEXT\"\n" +
    "  }";
var forUpdateOld =
    "[json_info]{\"org.softeg.slartus.forpda\":{\"release\":{\"ver\":\"1.48\",\"apk\":\"http://4pda.ru/forum/dl/post/8015156/4pda_v1.48_c442.apk\",info:\"1.48\\n[-] Клиент игнорирует topics[]=хххххх из поисковых запросов\\n[-] нет результатов поиска в виде тем.\"}},\"org.softeg.slartus.forpdaplus\":{\"beta\":{\"ver\":\"3.1.4b4\",\"apk\":\"http://4pda.ru/forum/dl/post/7073314/ForPDA_3.1.4b4.apk\",info: \"Исправлены новости\\nТЁМНЫЕ СТИЛИ НЕ РАБОТАЮТ, ПЕРЕД ОБНОВЛЕНИЕМ ОБЯЗАТЕЛЬНО ВКЛЮЧИТЕ СВЕТЛЫЙ СТИЛЬ\"},\"release\":{\"ver\": \"INSERT_VERSION\",\"apk\":\"INSERT_APK\",info:\"[!] Важное замечание\\n[+] Нововведение\\n[-] Исправлена ошибка\\n[*] Изменения\\n\\nINSERT_TEXT\"}}}[/json_info]";

function run_trans() {
    var initString = document.getElementById("input").value;
    var matches = initString.match(regex);
    var version, apk, text;
    version = matches[1];
    apk = matches[2];
    text = matches[3];

    var resultForPost = forPostUpdate.replace(/INSERT_VERSIOn/gi, version).replace(/INSERT_APK/gi, apk).replace(/INSERT_TEXT/, text);

    var resultForUpdate = forUpdate.replace(/INSERT_VERSIOn/gi, version).replace(/INSERT_TEXT/, text.replace(/\n/gi, ""));

    var resultForUpdateOld = forUpdateOld.replace(/INSERT_VERSIOn/gi, version).replace(/INSERT_APK/gi, apk).replace(/INSERT_TEXT/, text.replace(/\n/gi, ""));
    
    document.getElementById("post_upd").value = resultForPost;
    document.getElementById("upd").value = resultForUpdate;
    document.getElementById("upd_old").value = resultForUpdateOld;
}
