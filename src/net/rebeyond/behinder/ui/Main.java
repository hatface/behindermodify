// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   Main.java

package net.rebeyond.behinder.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import net.rebeyond.behinder.core.Constants;
import net.rebeyond.behinder.utils.Utils;

import java.io.ByteArrayInputStream;

public class Main extends Application
{

    public Main()
    {
    }

    public void start(Stage primaryStage)
        throws Exception
    {
        Parent root = (Parent)FXMLLoader.load(getClass().getResource("Main.fxml"));
        primaryStage.setTitle(String.format("\u51B0\u874E%s\u52A8\u6001\u4E8C\u8FDB\u5236\u52A0\u5BC6Web\u8FDC\u7A0B\u7BA1\u7406\u5BA2\u6237\u7AEF", new Object[] {
            Constants.VERSION
        }));
        primaryStage.getIcons().add(new Image(new ByteArrayInputStream(Utils.getResourceData("net/rebeyond/behinder/resource/logo.jpg"))));
        primaryStage.setScene(new Scene(root, 1100D, 600D));
        primaryStage.show();
    }

    public static void main(String args[])
    {
        launch(new String[0]);
    }
}
