# MailCheck
This tools helps the userOpen-source phishing awareness tool with transparent analysis
# MailCheck (Arbeitsname)

## Executive Summary
MailCheck ist ein spezialisiertes Awareness-Tool zur Erkennung von Phishing-E-Mails. Der Fokus liegt nicht auf einer rein automatisierten Bewertung, sondern auf der gezielten Sensibilisierung von Nutzern. MailCheck unterstützt Anwender dabei, E-Mails kritisch zu hinterfragen, typische Angriffsmuster zu erkennen und fundierte Entscheidungen zu treffen. Die optionale Ergebnisbewertung dient bewusst nur als Zweitmeinung und soll das eigenständige Denken nicht ersetzen, sondern ergänzen.

---

## 1. Zielsetzung
Phishing-Angriffe zählen weiterhin zu den häufigsten Einfallstoren für Sicherheitsvorfälle. Technische Schutzmechanismen allein reichen nicht aus – der Mensch bleibt ein zentraler Faktor. MailCheck setzt genau hier an.

Das Tool verfolgt einen Awareness-zentrierten Ansatz: Nutzer lernen direkt im relevanten Kontext, nämlich während der Analyse einer konkreten E-Mail. Dadurch entsteht ein nachhaltiger Lerneffekt, der klassische Schulungen oder theoretische Trainings sinnvoll ergänzt.

MailCheck trifft keine endgültigen Entscheidungen für den Nutzer. Stattdessen wird dieser aktiv in den Analyseprozess eingebunden und zur kritischen Reflexion angeregt.

---

## 2. Analyseprinzip & Ergebnisdarstellung
MailCheck analysiert E-Mails anhand mehrerer spezialisierter Scanner. Die Analyse wird bewusst transparent gestaltet:

- Das Gesamtergebnis ist nicht sofort sichtbar.
- Erst durch einen expliziten Klick auf „Ergebnis enthüllen“ wird die Risikobewertung angezeigt.
- Die Bewertung ist als unterstützende Zweitmeinung zu verstehen.

Dieser Ansatz verhindert blindes Vertrauen in automatisierte Systeme und fördert Sicherheitsbewusstsein.

---

## 3. Selbstüberprüfung des Nutzers
Vor der Anzeige des Ergebnisses wird der Nutzer aktiv zur Reflexion aufgefordert. Typische Leitfragen sind:

1. Hatten Sie bereits zuvor Kontakt mit diesem Absender?
2. Wird ungewöhnlicher Zeitdruck aufgebaut?
3. Haben Sie diese E-Mail erwartet?
4. Ist es plausibel, dass der Absender diese Anfrage an Sie richtet?
5. Werden Sie zu Zahlungen oder Überweisungen aufgefordert?
6. Werden Sie aufgefordert, Dateien herunterzuladen oder zu öffnen?
7. Erscheint der Inhalt der E-Mail insgesamt glaubwürdig?

Diese Fragen bilden das Herzstück des Awareness-Konzepts von MailCheck.

---

## 4. Scanner-Übersicht & Funktionsweise
MailCheck setzt auf mehrere spezialisierte Scanner, die einzeln aktivier- oder deaktivierbar sind.

### 4.1 PhishingScanner (Header-Analyse)
Der PhishingScanner analysiert technische Metadaten der E-Mail, insbesondere den Header. Dabei werden u. a. geprüft:

- Abweichungen zwischen Absender-Domain und Reply-To-Adresse
- Inkonsistenzen im Return-Path
- Typische Spoofing-Muster

### 4.2 URL-Scanner (Black- & Whitelist)
Der URL-Scanner untersucht alle in der E-Mail enthaltenen Links:

- Abgleich mit konfigurierbaren Blacklists (bekannte schädliche Domains)
- Berücksichtigung von Whitelists (vertrauenswürdige Domains)
- Erkennung typischer Täuschungsmuster in URLs (z. B. Marken-Imitationen)

### 4.3 Keyword-Scanner
Der Keyword-Scanner analysiert den Textinhalt der E-Mail auf bekannte Phishing-Schlüsselwörter, z. B.:

- Zahlungsaufforderungen
- Drohungen oder Dringlichkeit
- Konto- oder Sicherheitswarnungen

Die Keyword-Listen sind vollständig konfigurierbar und anpassbar an individuelle Bedrohungsszenarien.

### 4.4 Readability-Scanner (Coleman-Liau-Index)
Der Readability-Scanner bewertet die sprachliche Qualität der E-Mail mithilfe des Coleman-Liau-Index. Auffälligkeiten können sein:

- Ungewöhnlich einfache Sprache
- Unpassendes Sprachniveau für angebliche Unternehmenskommunikation

### 4.5 Attachment-Scanner
Der Attachment-Scanner analysiert Dateianhänge nach potenziellen Risiken:

- Identifikation gefährlicher Dateitypen (z. B. .exe, .bat, .js)


### 4.6 IBAN-Scanner
Der IBAN-Scanner prüft:

- Formale Gültigkeit der IBAN
- Warnt vor einer IBAN die aus einem Land kommt das nicht aus als sihcer eingestellt wurde

---

## 5. Benutzeroberfläche & Bedienkonzept
Der Startbildschirm ist bewusst klar und übersichtlich gestaltet und in drei Bereiche unterteilt:

### Bereich 1 – E-Mail-Übersicht
- Anzeige aller `.eml`-Dateien im gewählten Ordner (standardmäßig Downloads)
- Drag-and-Drop von `.eml`-Dateien direkt aus dem Explorer

### Bereich 2 – Analyse-Details
- Anzeige der detaillierten Scanner-Ergebnisse nach Durchführung der Analyse

### Bereich 3 – Risk Panel
- Freischaltung erst nach Klick auf „Ergebnis enthüllen“
- Ampellogik:
  - Rot: Viele Phishing-Muster erkannt
  - Gelb: Einige Phishing-Muster erkannt
  - Grün: Wenige oder keine typischen Phishing-Muster erkannt

---

## 6. Handlungsempfehlungen
MailCheck unterstützt Nutzer mit klaren Handlungsempfehlungen:

- Links nicht direkt anklicken, sondern manuell aufrufen
- Anhänge nur öffnen, wenn sie erwartet wurden
- Keine Zahlungen ohne eindeutige Verifikation durchführen
- Zahlungsdaten unabhängig prüfen

---

## 7. Fazit
MailCheck ist kein klassischer Phishing-Scanner, sondern ein strategisches Awareness-Tool. Es stärkt den Nutzer als letzte Verteidigungslinie, fördert kritisches Denken und reduziert langfristig das Risiko erfolgreicher Phishing-Angriffe.

Durch die Kombination aus transparenter Analyse, optionaler Bewertung und gezielter Nutzerinteraktion eignet sich MailCheck ideal für Unternehmen, die Sicherheitsbewusstsein nachhaltig verankern möchten – ohne auf starre oder belehrende Schulungskonzepte zu setzen.

---

## Hinweis zur Bezeichnung
Bitte beachten Sie, dass der Begriff „MailCheck“ in diesem Zusammenhang ausschließlich als Arbeitsbezeichnung verwendet wird, um nicht wiederholt auf das Tool verweisen zu müssen. Dabei handelt es sich weder um den rechtsgültigen Namen noch um die endgültige Produktbezeichnung.

---

## Quellen & Orientierung
Die in MailCheck umgesetzten Awareness-Mechanismen basieren auf etablierten Sicherheitsrichtlinien und öffentlich zugänglichen Empfehlungen, wie sie unter anderem vom Bundesamt für Sicherheit in der Informationstechnik (BSI) veröffentlicht werden.

---

## Third-Party / Lizenzinformationen
Die Lizenzinformationen sind im Programm unter **Einstellungen → Lizenzen** einsehbar.

- FlatLaf (Apache License 2.0)
- WiX Toolset (nur für Packaging)
- AI-Assistenz: OpenAI ChatGPT + Google Gemini (nur zur Unterstützung)

