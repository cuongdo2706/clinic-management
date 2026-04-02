package cd.beapi.utility;

import org.springframework.util.StringUtils;

import java.text.Normalizer;

public class StringUtil {
    private StringUtil(){}
    public static String normalizeKeyword(String keyword){
        if (!StringUtils.hasText(keyword)) return "";

        String normalized = Normalizer.normalize(keyword.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");

        return normalized.toLowerCase().replace("đ", "d");    }
}
