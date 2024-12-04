package com.creator;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class App extends Application {
    public static void main(String[] args) {
        testFolder();
        launch();
    }

    public static void testFolder() {
        String basePath = System.getenv("USERPROFILE") + "\\Desktop\\Test Folder";
        String[] extensions = {".iso", ".zip", ".rar", ".7z", ".exe"};

        List<String> names = List.of(
                "Cars 2", "Despicable Me 4", "Top Gun Maverick",
                "Frozen 2", "1917", "Oblivion",
                "Recep Ivedik", "Liar Liar", "Harry Potter Order Phoenix",
                "Inception", "It", "Mr Poppins Penguin",
                "Forrest Gump", "Pianist", "Office",
                "The Number 23", "Need For Speed", "Outside",
                "The Dark Knight", "Avengers: Endgame", "The Matrix",
                "The Godfather", "Titanic", "Jurassic Park",
                "The Lord of the Rings", "Star Wars A New Hope", "Interstellar",
                "Gladiator", "The Lion King", "The Shawshank Redemption",
                "Pulp Fiction", "The Silence of the Lambs", "Fight Club",
                "The Terminator", "Terminator 2 Judgment Day", "Spider Man No Way Home",
                "The Incredibles", "Toy Story", "Shrek", "The Wolf of Wall Street",
                "The Prestige", "The Avengers", "Guardians of the Galaxy", "Deadpool",
                "Mad Max: Fury Road", "The Revenant", "A Beautiful Mind", "The Imitation Game",
                "The Social Network", "12 Angry Men", "Goodfellas", "American Beauty",
                "Back to the Future", "The Departed", "Saving Private Ryan", "The Hunger Games",
                "Avatar", "The Grand Budapest Hotel", "The Fighter", "Silver Linings Playbook",
                "Whiplash", "La La Land", "The Exorcist", "Jaws", "Fight Club", "The Big Lebowski",
                "Blade Runner 2049", "The Dark Knight Rises", "Casino Royale", "Skyfall", "Guardians of the Galaxy Vol 2",
                "The Matrix Reloaded", "The Matrix Revolutions", "Jurassic World", "Pacific Rim", "Frozen",
                "Shutter Island", "The Great Gatsby", "The Wolf of Wall Street", "The Chronicles of Narnia",
                "The Princess Bride", "The Giver", "The Maze Runner", "Wonder Woman", "Deadpool 2",
                "Toy Story 2", "Toy Story 3", "Toy Story 4", "Shrek 2", "Shrek the Third", "Shrek Forever After",
                "Finding Nemo", "Finding Dory", "The Lion King 2", "The Incredibles 2", "Zootopia", "Ratatouille",
                "Monsters Inc.", "Monsters University", "Kung Fu Panda", "Kung Fu Panda 2", "Kung Fu Panda 3",
                "Madagascar", "Madagascar 2", "Madagascar 3", "Ice Age", "Ice Age 2", "Ice Age 3", "Ice Age 4", "Ice Age 5",
                "The Secret Life of Pets", "The Secret Life of Pets 2", "How to Train Your Dragon", "How to Train Your Dragon 2",
                "How to Train Your Dragon The Hidden World", "The Croods", "The Croods A New Age", "Trolls", "Trolls World Tour",
                "Minions", "Minions: The Rise of Gru", "Despicable Me 2", "Despicable Me", "Cars", "Cars 3",
                "The Lego Movie", "The Lego Movie 2", "Wreck It Ralph", "Ralph Breaks the Internet", "Big Hero 6",
                "Frozen", "Frozen Fever", "The Peanuts Movie", "Moana", "Puss in Boots", "Puss in Boots The Last Wish",
                "Cloudy with a Chance of Meatballs", "Cloudy with a Chance of Meatballs 2", "Lego Batman Movie", "The Boss Baby",
                "The Angry Birds Movie", "The Angry Birds Movie 2", "SpiderMan Into the Spider Verse", "The Little Mermaid",
                "Aladdin", "Beauty and the Beast", "Coco", "Soul", "Luca", "Onward", "Raya and the Last Dragon"
        );
        List<String> gameNames = List.of(
                "The Witcher 3 Wild Hunt", "Red Dead Redemption 2", "Cyberpunk 2077",
                "Grand Theft Auto V", "Minecraft", "Fortnite", "The Last of Us Part II",
                "Apex Legends", "Call of Duty Warzone", "Elden Ring",
                "Horizon Zero Dawn", "Overwatch", "Assassin's Creed Valhalla",
                "Dark Souls III", "Battlefield 5", "The Elder Scrolls V Skyrim",
                "Among Us", "League of Legends", "Dota 2", "Valorant", "Far Cry 5",
                "Super Mario Odyssey", "God of War", "Uncharted 4 A Thiefs End",
                "Minecraft Dungeons", "Watch Dogs Legion", "Fall Guys",
                "Resident Evil Village", "Ghost of Tsushima", "Dead by Daylight",
                "FIFA 21", "NBA 2K21", "Stardew Valley", "Rocket League",
                "World of Warcraft", "Sekiro Shadows Die Twice", "Tomb Raider",
                "The Legend of Zelda Breath of the Wild", "Animal Crossing New Horizons",
                "Borderlands 3", "Gears 5", "Naruto Shippuden Ultimate Ninja Storm",
                "Fallout 4", "BioShock Infinite", "Unravel", "The Sims 4",
                "Dragon Age: Inquisition", "Mass Effect Legendary Edition", "Monster Hunter World",
                "Final Fantasy VII Remake", "Bloodborne", "Mortal Kombat 11", "Forza Horizon 4",
                "Street Fighter V", "Kingdom Hearts III", "The Division 2", "Destiny 2",
                "Civilization VI", "Total War Three Kingdoms", "SimCity 4", "Dead Space",
                "Overcooked 2", "Portal 2", "Cuphead", "Spelunky 2", "Subnautica",
                "Slay the Spire", "The Forest", "No Mans Sky", "The Crew 2", "Battlefield 1",
                "Rainbow Six Siege", "Hitman 3", "Call of Duty Cold War", "Dying Light 2",
                "Watch Dogs 2", "The Outer Worlds", "Life is Strange", "Detroit: Become Human",
                "Sea of Thieves", "Planet Coaster", "Euro Truck Simulator 2", "Oxygen Not Included",
                "Farming Simulator 19", "Monster Hunter Rise", "Nioh 2", "Lords Mobile"
        );

        File baseFolder = new File(basePath);
        if (!baseFolder.exists() && baseFolder.mkdirs()) {
            System.out.println("Ana klasör oluşturuldu: " + basePath);
        }

        Random random = new Random();

        for (String name : names) {
            boolean isFolder = random.nextBoolean();

            if (isFolder) {
                File folder = new File(basePath, name);
                if (folder.mkdir()) {
                    System.out.println("Klasör oluşturuldu: " + folder.getAbsolutePath());
                } else {
                    System.out.println("Klasör oluşturulamadı: " + folder.getAbsolutePath());
                }
            } else {
                File file = new File(basePath, name + ".mp4");
                try {
                    if (file.createNewFile()) {
                        System.out.println("Dosya oluşturuldu: " + file.getAbsolutePath());
                    } else {
                        System.out.println("Dosya oluşturulamadı: " + file.getAbsolutePath());
                    }
                } catch (IOException e) {
                    System.out.println("Hata oluştu: " + e.getMessage());
                }
            }
        }

        for (String name : gameNames) {
            boolean isFolder = random.nextBoolean();

            if (isFolder) {
                File folder = new File(basePath, name);
                if (folder.mkdir()) {
                    System.out.println("Klasör oluşturuldu: " + folder.getAbsolutePath());
                } else {
                    System.out.println("Klasör oluşturulamadı: " + folder.getAbsolutePath());
                }
            } else {
                String randomExtension = extensions[random.nextInt(extensions.length)];
                String fileName = name + randomExtension;

                File file = new File(basePath, fileName);
                try {
                    if (file.createNewFile()) {
                        System.out.println(randomExtension + " dosyası oluşturuldu: " + file.getAbsolutePath());
                    } else {
                        System.out.println(randomExtension + " dosyası oluşturulamadı: " + file.getAbsolutePath());
                    }
                } catch (IOException e) {
                    System.out.println(randomExtension + " dosyası oluşturulamadı: " + e.getMessage());
                }
            }
        }
    }

    @Override
    public void start(Stage stage) throws IOException {
        stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResource("/archive.png")).toString()));
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("/app.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("File AutoRun File Creator");
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();
    }
}