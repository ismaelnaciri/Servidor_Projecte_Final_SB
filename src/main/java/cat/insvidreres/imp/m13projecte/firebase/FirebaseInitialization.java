package cat.insvidreres.imp.m13projecte.firebase;


import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

@Service
public class FirebaseInitialization {

    @PostConstruct
    public void initialization() throws IOException {
        FileInputStream serviceAccount = null;

        try {
            serviceAccount = new FileInputStream(
                    "C:\\IdeaProjects\\2nDAM\\m13projecte\\serviceAccountKey.json");

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();


            FirebaseApp.initializeApp(options);

        } catch (Exception e) {
            System.out.println("ERROR | " + e.getMessage());
            e.printStackTrace();
        }
    }
}
