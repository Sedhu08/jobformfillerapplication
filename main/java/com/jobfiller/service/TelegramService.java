package com.jobfiller.service;

import com.jobfiller.model.Settings;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class TelegramService {

    @Autowired
    private SettingsService settingsService;

    private static final String TELEGRAM_API =
            "https://api.telegram.org/bot";

    // ================================
    // SEND SUCCESS NOTIFICATION
    // ================================
    public void sendSuccess(String companyName,
                            String jobTitle,
                            String url) {
        String message =
                         "🤖 Job Form Filler\n\n" +
                        "✅ Application Successful!\n\n" +
                        "🏢 Company   : " + companyName + "\n" +
                        "💼 Job Title : " + jobTitle + "\n" +
                        "📅 Date      : " + getDate() + "\n" +
                        "⏰ Time      : " + getTime() + "\n" +
                        "🔗 URL       : " + url + "\n\n" +
                        "Good luck! 🍀";

        sendMessage(message);
    }

    // ================================
    // SEND FAILURE NOTIFICATION
    // ================================
    public void sendFailure(String companyName,
                            String error) {
        String message =
                "🤖 Job Form Filler\n\n" +
                        "❌ Application Failed!\n\n" +
                        "🏢 Company : " + companyName + "\n" +
                        "❌ Error   : " + error + "\n" +
                        "📅 Date    : " + getDate() + "\n" +
                        "⏰ Time    : " + getTime() + "\n\n" +
                        "Please check error log! 📝";

        sendMessage(message);
    }

    // ================================
    // SEND DUPLICATE NOTIFICATION
    // ================================
    public void sendDuplicate(String companyName,
                              String appliedDate,
                              String status) {
        String message =
                "🤖 Job Form Filler\n\n" +
                        "⚠️ Duplicate Application!\n\n" +
                        "🏢 Company  : " + companyName + "\n" +
                        "📅 Applied  : " + appliedDate + "\n" +
                        "📊 Status   : " + status + "\n" +
                        "📅 Today    : " + getDate() + "\n\n" +
                        "Cancelled automatically! ✋";

        sendMessage(message);
    }

    // ================================
    // SEND TEST NOTIFICATION
    // ================================
    public boolean sendTestMessage(String token,
                                   String chatId) {
        String message =
                         "🤖 Job Form Filler\n\n" +
                        "✅ Bot connected successfully!\n\n" +
                        "Your notifications are set up. \n" +
                        "You will receive updates here! 🎉";

        return sendMessageWithCredentials(
                token, chatId, message);
    }

    // ================================
    // CORE SEND MESSAGE
    // ================================
    private void sendMessage(String text) {
        try {
            Settings settings = settingsService.getSettings();

            // Check if telegram enabled
            if (!settings.getTelegramEnabled()) {
                System.out.println(
                        "Telegram disabled, skipping notification");
                return;
            }

            String token  = settings.getTelegramToken();
            String chatId = settings.getTelegramChatId();

            // Check if token and chatId are set
            if (token == null  || token.isEmpty() ||
                    chatId == null || chatId.isEmpty()) {
                System.out.println(
                        "Telegram token or chatId not set!");
                return;
            }

            sendMessageWithCredentials(token, chatId, text);

        } catch (Exception e) {
            System.out.println(
                    "Telegram notification failed: "
                            + e.getMessage());
        }
    }

    // ================================
    // SEND MESSAGE WITH CREDENTIALS
    // ================================
    private boolean sendMessageWithCredentials(String token,
                                               String chatId,
                                               String text) {
        try {
            // Encode message for URL
            String encodedText = URLEncoder.encode(
                    text, StandardCharsets.UTF_8);

            // Build Telegram API URL
            String url = TELEGRAM_API
                    + token
                    + "/sendMessage"
                    + "?chat_id=" + chatId
                    + "&text="    + encodedText
                    + "&parse_mode=HTML";

            // Make HTTP GET request
            CloseableHttpClient httpClient =
                    HttpClients.createDefault();
            HttpGet request = new HttpGet(url);
            CloseableHttpResponse response =
                    httpClient.execute(request);

            // Check response
            String responseBody = EntityUtils.toString(
                    response.getEntity());
            int statusCode = response.getStatusLine()
                    .getStatusCode();

            httpClient.close();

            if (statusCode == 200) {
                System.out.println(
                        "✅ Telegram notification sent!");
                return true;
            } else {
                System.out.println(
                        "❌ Telegram failed: " + responseBody);
                return false;
            }

        } catch (Exception e) {
            System.out.println(
                    "❌ Telegram error: " + e.getMessage());
            return false;
        }
    }

    // ================================
    // HELPER METHODS
    // ================================
    private String getDate() {
        return LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    private String getTime() {
        return LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("HH:mm:ss"));
    }
}
