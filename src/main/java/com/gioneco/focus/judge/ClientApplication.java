package com.gioneco.focus.judge;

import com.gioneco.focus.judge.client.view.LoginView;
import com.gioneco.focus.judge.client.view.MainView;
import de.felixroske.jfxsupport.AbstractJavaFxApplicationSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;

import java.util.Arrays;

/**
 * @author DJ
 * @className ClientApplication
 * @Description
 * @date 2022-09-26 15:36
 */
@SpringBootApplication
public class ClientApplication extends AbstractJavaFxApplicationSupport {
    public static void main(String[] args) {
        launch(ClientApplication.class, LoginView.class, args);
    }
    
}
