package engine;

import java.net.IDN;
import java.net.URI;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UrlScanner implements Scanner {

    private final AppConfig cfg;
    private final Set<String> legitDomains = new HashSet<>();
    private final Set<String> threatIntelDomains = new HashSet<>();

    public UrlScanner(AppConfig cfg) {
        this.cfg = cfg;

        ConfigLoader loader = new ConfigLoader();

        // ✅ Whitelist laden
        for (String w : loader.getList("whitelist")) {
            legitDomains.add(normalizeDomain(w));
        }

        // 🔥 TI-Blacklist laden
        for (String b : loader.getList("ti-blacklist")) {
            threatIntelDomains.add(normalizeDomain(b));
        }
    }

    // ------------------------------------
    // Robuste URL-Erkennung
    // ------------------------------------
    private static final Pattern URL_PATTERN =
            Pattern.compile("(https?://[^\\s\"'>]+)", Pattern.CASE_INSENSITIVE);

    private static final Set<String> SHORTENER = new HashSet<>(Arrays.asList(
            "bit.ly", "tinyurl.com", "t.co", "goo.gl",
            "is.gd", "rebrand.ly", "cutt.ly", "ow.ly"
    ));

    @Override
    public ScanResult scan(ParsedEmail email) {

        ScanResult result = new ScanResult(
                "UrlScanner",
                ScanCategory.RISK,
                cfg.getInt("score.url.max", 15)
        );

        if (email == null || email.body == null || email.body.isEmpty()) {
            return result;
        }

        Matcher matcher = URL_PATTERN.matcher(email.body);
        Set<String> seenDomains = new HashSet<>();
        List<String> urls = new ArrayList<>();

        while (matcher.find()) {
            urls.add(matcher.group(1));
        }

        
       // 🟡 Viele Links
if (urls.size() >= 3) {
    result.addFinding(new Finding(
            "url.many",
            ScanCategory.INDICATOR,
            cfg.getInt("score.url.many", 4),
            FindingType.URL,
            null,
            "url.many",
            null
    ));
}


        for (String url : urls) {
            analyzeUrl(url, result, seenDomains);
        }

        return result;
    }

    // ------------------------------------
    // Analyse einzelner URL
    // ------------------------------------
    private void analyzeUrl(String url, ScanResult result, Set<String> seenDomains) {

        String domain = extractDomain(url);
        if (domain.isEmpty()) return;

        // Duplikate vermeiden
        if (!seenDomains.add(domain)) return;

        // ✅ Whitelist
        if (legitDomains.contains(domain)) return;

        // 🔴 TI-Blacklist (exakt + Subdomain)
        for (String bad : threatIntelDomains) {
            if (domain.equals(bad) || domain.endsWith("." + bad)) {

                // 🔴 Threat Intelligence
result.addFinding(new Finding(
        "url.threatintel",
        ScanCategory.RISK,
        cfg.getInt("score.url.threatintel", 10),
        FindingType.URL,
        domain,
        "url.threatintel",
        null
));

                return; // 🔥 sofort eskalieren
            }
        }

        // 🟡 Shortener
        if (SHORTENER.contains(domain)) {
            // 🟡 Shortener
result.addFinding(new Finding(
        "url.shortener",
        ScanCategory.INDICATOR,
        cfg.getInt("score.url.shortener", 6),
        FindingType.URL,
        domain,
        "url.shortener",
        null
));

        }

        // 🔴 IP-Adresse
        if (domain.matches("\\d+\\.\\d+\\.\\d+\\.\\d+")) {
            // 🔴 IP-Adresse
result.addFinding(new Finding(
        "url.ip",
        ScanCategory.RISK,
        cfg.getInt("score.url.ip", 8),
        FindingType.URL,
        domain,
        "url.ip",
        null
));

        }

        // 🟡 Unsicheres HTTP
        if (url.toLowerCase().startsWith("http://")) {
            result.addFinding(new Finding(
        "url.http",
        ScanCategory.INDICATOR,
        cfg.getInt("score.url.http", 4),
        FindingType.URL,
        domain,
        "url.http",
        null
));

        }

        // 🟡 Encoding
        if (url.matches(".*%[0-9a-fA-F]{2}.*")) {
            result.addFinding(new Finding(
        "url.encoded",
        ScanCategory.INDICATOR,
        cfg.getInt("score.url.encoded", 3),
        FindingType.URL,
        domain,
        "url.encoded",
        null
));

        }

        // 🔴 Homoglyphen
        if (hasHomoglyphs(domain)) {
            // 🔴 Homoglyphen
result.addFinding(new Finding(
        "url.homoglyph",
        ScanCategory.RISK,
        cfg.getInt("score.url.homoglyph", 8),
        FindingType.URL,
        domain,
        "url.homoglyph",
        null
));

        }

        // 🔴 Riskante TLDs
        String[] riskyTlds = { ".ru", ".su", ".zip", ".click", ".tk", ".ml", ".ga" };
        for (String tld : riskyTlds) {
            if (domain.endsWith(tld)) {
                result.addFinding(new Finding(
        "url.risky_tld",
        ScanCategory.RISK,
        cfg.getInt("score.url.risky_tld", 10),
        FindingType.URL,
        domain,
        "url.risky_tld",
        null
));

            }
        }

        // 🟡 Viele Subdomains
        if (domain.split("\\.").length > 4) {
            // 🟡 Viele Subdomains
result.addFinding(new Finding(
        "url.many_subdomains",
        ScanCategory.INDICATOR,
        cfg.getInt("score.url.subdomains", 3),
        FindingType.URL,
        domain,
        "url.many_subdomains",
        null
));

        }
        
    
    }

    // ------------------------------------
    // Helpers
    // ------------------------------------
    private String extractDomain(String url) {
        try {
            URI uri = new URI(url);
            String host = uri.getHost();
            return host != null ? normalizeDomain(host) : "";
        } catch (Exception e) {
            return "";
        }
    }

    private String normalizeDomain(String domain) {
        try {
            return IDN.toASCII(domain.toLowerCase().trim());
        } catch (Exception e) {
            return domain.toLowerCase().trim();
        }
    }

    private boolean hasHomoglyphs(String s) {
        return s.matches(".*[а-яΑ-Ωα-ωІ].*");
    }
}
