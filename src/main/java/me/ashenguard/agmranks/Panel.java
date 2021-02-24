package me.ashenguard.agmranks;

import me.ashenguard.api.versions.Version;
import me.ashenguard.spigotapplication.SpigotPanel;

public class Panel extends SpigotPanel {
    public Panel() {
        super(75787, new Version(4, 0));
        this.addDependency(83245);  // AGMCore
        this.addDependency(6245);   // Placeholder API
        this.addDependency(34315);  // Vault
        this.setDescription("Installation:\n" +
                "Add the JAR file to your server plugins folder.\n\n" +
                "About this plugin:\n" +
                "This plugin will add a ranking system which give players access to more permissions and etc...\n" +
                "Ranking will be based on 3 systems: EXP, Money, PlayTime");
    }

    public static void main(String[] args) {
        launch();
    }
}
