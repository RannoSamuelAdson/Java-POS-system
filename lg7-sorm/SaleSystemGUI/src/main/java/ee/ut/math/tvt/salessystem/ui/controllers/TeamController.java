package ee.ut.math.tvt.salessystem.ui.controllers;

import ee.ut.math.tvt.salessystem.dao.SalesSystemDAO;
import ee.ut.math.tvt.salessystem.logic.ShoppingCart;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;
import java.util.ResourceBundle;

public class TeamController implements Initializable {

    private static final Logger log = LogManager.getLogger(TeamController.class);

    @FXML
    private Label teamNameTextLabel;
    @FXML
    private Label teamLeaderTextLabel;
    @FXML
    private Label teamMembersTextLabel;
    @FXML
    private TitledPane teamTabTitle;
    @FXML
    Label teamleaderemailTextLabel;
    @FXML
    private Label teamNameLabel;
    @FXML
    private Label teamLeaderNameLabel;
    @FXML
    private Label teamMember1Label;
    @FXML
    private Label teamMember2Label;
    @FXML
    private Label teamMember3Label;
    @FXML
    private Label teamMember4Label;
    @FXML
    Label teamEmailLabel;
    @FXML
    private ImageView teamLogoImageView;

    public TeamController() {
    }

    public void initialize(URL location, ResourceBundle resources) {
        Properties properties = new Properties();
        try (InputStream input = TeamController.class.getClassLoader().getResourceAsStream("application.properties")) {
            properties.load(input);
        } catch (IOException e) {
            log.error("application.properties file not found!");
            return;
        }

        //Read guide text from properties
        String teamtabTitle = properties.getProperty("team.tabTitle");
        String teamNameText = properties.getProperty("team.nameText");
        String teamLeaderText = properties.getProperty("team.leaderText");
        String teamleaderemailText = properties.getProperty("team.leaderemailText");
        String teamMembersText = properties.getProperty("team.membersText");

        //Read team info from properties
        String teamName = properties.getProperty("team.name");
        String teamLeader = properties.getProperty("team.leader");
        String teamContact = properties.getProperty("team.contact.email");
        String teamMember1 = properties.getProperty("team.member1");
        String teamMember2 = properties.getProperty("team.member2");
        String teamMember3 = properties.getProperty("team.member3");
        String teamMember4 = properties.getProperty("team.member4");
        String teamLogoPath = properties.getProperty("team.logo.path");

        teamTabTitle.setText(teamtabTitle);
        teamNameTextLabel.setText(teamNameText);
        teamLeaderTextLabel.setText(teamLeaderText);
        teamleaderemailTextLabel.setText(teamleaderemailText);
        teamMembersTextLabel.setText(teamMembersText);

        teamNameLabel.setText(teamName);
        teamLeaderNameLabel.setText(teamLeader);
        teamEmailLabel.setText(teamContact);
        teamMember1Label.setText(teamMember1);
        teamMember2Label.setText(teamMember2);
        teamMember3Label.setText(teamMember3);
        teamMember4Label.setText(teamMember4);

        teamLogoImageView.setImage(new Image(getClass().getResourceAsStream(teamLogoPath)));

    }
}
