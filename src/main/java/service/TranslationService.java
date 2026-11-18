
package service;

import java.io.IOException;

import com.google.cloud.translate.v3.DetectLanguageRequest;
import com.google.cloud.translate.v3.DetectLanguageResponse;
import com.google.cloud.translate.v3.DetectedLanguage;
import com.google.cloud.translate.v3.LocationName;
import com.google.cloud.translate.v3.TranslateTextRequest;
import com.google.cloud.translate.v3.TranslateTextResponse;
import com.google.cloud.translate.v3.TranslationServiceClient;

public class TranslationService {

    // Google Cloud のプロジェクトID
    private static final String PROJECT_ID = "chat2real";

    // 翻訳処理
    public static String translate(String text) throws IOException {

        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("翻訳する文章がありません。");
        }

        String parent = LocationName.of(PROJECT_ID, "global").toString();

        try (TranslationServiceClient client =
                TranslationServiceClient.create()) {

            // ① 元の文章の言語を判定
            DetectLanguageRequest detectRequest =
                DetectLanguageRequest.newBuilder()
                    .setParent(parent)
                    .setMimeType("text/plain")
                    .setContent(text)
                    .build();

            DetectLanguageResponse detectResponse =
                client.detectLanguage(detectRequest);

            if (detectResponse.getLanguagesList().isEmpty()) {
                throw new IllegalArgumentException(
                    "文章の言語を判定できませんでした。");
            }

            DetectedLanguage detected =
                detectResponse.getLanguagesList().get(0);

            String sourceLanguage = detected.getLanguageCode();

            // ② 日本語・英語以外は対象外
            String targetLanguage;

            if (sourceLanguage.equals("ja")) {
                targetLanguage = "en";
            } else if (sourceLanguage.equals("en")) {
                targetLanguage = "ja";
            } else {
                throw new IllegalArgumentException(
                    "日本語と英語の文章だけ翻訳できます。");
            }

            // ③ 翻訳を実行
            TranslateTextRequest translateRequest =
                TranslateTextRequest.newBuilder()
                    .setParent(parent)
                    .setMimeType("text/plain")
                    .setTargetLanguageCode(targetLanguage)
                    .addContents(text)
                    .build();

            TranslateTextResponse translateResponse =
                client.translateText(translateRequest);

            // ④ 翻訳結果を返す
            if (translateResponse.getTranslationsList().isEmpty()) {
                throw new IllegalStateException(
                    "翻訳結果を取得できませんでした。");
            }

            return translateResponse
                    .getTranslations(0)
                    .getTranslatedText();
        }
    }
}
