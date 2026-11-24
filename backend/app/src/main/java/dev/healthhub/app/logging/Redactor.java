package dev.healthhub.app.logging;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Masks sensitive values in any string before it is logged. Rule: any {@code key=value},
 * {@code "key":"value"} or {@code key: value} pair whose key looks secret-ish (secret, token,
 * password, api/personal key, authorization, credentials, oauth code/state, client/user id)
 * has its value replaced with *** (keeping surrounding quotes). Applied to request URIs,
 * exception messages and any free text that might embed credentials. Over-redaction is safe.
 */
public final class Redactor {

    private static final Pattern SENSITIVE_KEY = Pattern.compile(
            "(?i)(secret|token|password|passwd|api[_-]?key|personal[_-]?key|authorization|credentials|code|state|client[_-]?id|user[_-]?id|userid)");

    // "clientSecret":"abc" / 'clientSecret': 'abc' / clientSecret=abc / clientSecret: abc / Authorization: Bearer x
    private static final Pattern PAIR = Pattern.compile(
            "(\"[^\"]*\"|'[^']*'|[A-Za-z0-9_.-]+)(\\s*[:=]\\s*)(\"[^\"]*\"|'[^']*'|[^\\s,;&]+)");

    // Authorization: Bearer <jwt> / Token <value>
    private static final Pattern BEARER = Pattern.compile("(?i)((?:bearer|token)\\s+)[^\\s,;&]+");

    private Redactor() {
    }

    public static String apply(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        input = BEARER.matcher(input).replaceAll("$1***");
        var matcher = PAIR.matcher(input);
        var out = new StringBuilder();
        while (matcher.find()) {
            var key = matcher.group(1).replace("\"", "").replace("'", "");
            if (SENSITIVE_KEY.matcher(key).find()) {
                var value = matcher.group(3);
                var masked = value.startsWith("\"") || value.startsWith("'")
                        ? value.charAt(0) + "***" + value.charAt(0)
                        : "***";
                matcher.appendReplacement(out, Matcher.quoteReplacement(matcher.group(1) + matcher.group(2) + masked));
            } else {
                matcher.appendReplacement(out, Matcher.quoteReplacement(matcher.group()));
            }
        }
        matcher.appendTail(out);
        return out.toString();
    }
}
