package io.github.weechang.generator;

import io.github.weechang.Connector;
import io.github.weechang.config.Profile;
import io.github.weechang.util.FileUtils;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 配置文件生成器
 */
public class ProfileGenerator {
    public static HashMap<String, String> pwdAndName = new HashMap();
    private String ssid = null;
    private String passwordPath = null;
    private ExecutorService threadPool = Executors.newFixedThreadPool(4);

    public ProfileGenerator(String ssid, String passwrodPath) {
        this.ssid = ssid;
        this.passwordPath = passwrodPath;
    }

    /**
     * 生成配置文件
     */
    public void genProfile() {
        List<String> passwordList = null;
        int counter = 0;
        outer:
        while (true) {
            int start = counter * Connector.BATH_SIZE;
            int end = (counter + 1) * Connector.BATH_SIZE - 1;
            passwordList = FileUtils.readLine(passwordPath, start, end);
            if (passwordList != null && passwordList.size() > 0) {
                // 生成配置文件
                for (String password : passwordList) {
                    GenThread genThread = new GenThread(ssid, password);
                    threadPool.execute(genThread);
                }
            } else {
                break outer;
            }
            counter++;
        }
    }
}

class GenThread implements Runnable {

    private String ssid = null;
    private String password = null;

    GenThread(String ssid, String password) {
        this.ssid = ssid;
        this.password = password;
    }

    @Override
    public void run() {
        String uuid = UUID.randomUUID().toString();
        Random random = new Random();
        String seed = new String();
        for (int i = 0; i < 9; i++) {
            seed += String.valueOf(random.nextInt(9));
        }
        ssid = ssid.replace("&","&amp;");
        String profileContent = Profile.PROFILE.replace(Profile.WIFI_NAME, ssid);
        profileContent = profileContent.replace(Profile.WIFI_PASSWORD, password);
        profileContent = profileContent.replace("144996837", seed);
        ProfileGenerator.pwdAndName.put(password, uuid);
        FileUtils.writeToFile(Connector.PROFILE_TEMP_PATH + "\\" + uuid + ".xml", profileContent);
    }
}
