package com.mover;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

public class App extends Application {

    public static void main(String[] args) throws IOException {
        test();
        launch();
    }

    public static void test() throws IOException {
        String directoryPath = "C:\\Users\\AFU\\Desktop\\test";

        String[] movieTitles = {
            //"Küçük Askerler", "Liar Liar", "Lilo And Stitch 2 Stitch Has A Glitch", "Lilo And Stitch",
            "Lord of War", "Luck", "Luis Ve Uzaylı Dostları", "Malcolm X", "Marslı", "Mary and Max",
            //"Maskeli Beşler Irak", "Megamind The Doom Syndicate", "Megamind", "Midway", "Migration",
            "Missing Link", "Moana", "Monsters vs Aliens", "Napoleon", "Oblivion", "Oppenheimer",
            "Otel Transilvanya 3", "Over the Moon", "Oyuncak Hikayesi 2", "Oyuncak Hikayesi 3",
            "Oyuncak Hikayesi 4", "Paranorman", "Peter Rabbit 2", "Police Academy 2", "Police Academy 3",
            "Police Academy 5", "Police Academy 6 City Under Siege", "Princess Mononoke", "Problem Child"
            //"Prometheus", "Rat Race", "Robot Ron Sorun Var", "Rumble", "San Andreas Fayı", "San Andreas Fayı",
            //"Saving Private Ryan", "Se7en", "Shark Tale", "Shrek The Halls", "Son of Bigfoot",
            //"Sonic Hedgehog 2", "Source Code", "Spirited Away", "Stop Or My Mom Will Shoot",
            //"Storks", "Surfs Up", "Şirinler 2", "Tentenin Maceraları", "Terminator 2", "Terminator 3",
            //"Terminator Kurtulus", "Ters Yüz", "The Addams Family 2", "The Bad Guys", "The Boss Baby Family Business",
            //"The Cat in the Hat", "The Flintstones", "The Forbidden Kingdom", "The Founder", "The Good the Bad and the Ugly",
            //"The Intouchables", "The Iron Giant", "The Little Prince", "The Lorax", "The Message", "The Number 23",
            //"The Polar Express", "The Truman Show", "Trolls", "Turbo", "Vincentten Sevgilerle", "WallE", "Warm Bodies",
            //"Who Am I Jackie Chan", "Who Am I", "Wish", "Wonka", "Woody Woodpecker Goes Camp", "Wreck It Ralph Internet",
            //"Yenilmezler Ultron Çağı", "Yenilmezler", "Yeti Efsanesi", "Zamana Karsi", "Zathura A Space Adventure",
            //"Zootropolis", "Fri", "The Bing Bang Th", "Arabalar 2", "Asfaltın Kralları", "Astro Boy", "Babys Day Out",
            //"Bir Tat Bir Doku", "Bizim Aile", "Blackberry", "Cars", "Grave of the Fireflies",
            //"Happy Feet", "Her Seyin Teorisi", "Hitlere Suikast", "Kung Fu Panda 4"
        };

        for (String title : movieTitles) {
            createEmptyMp4File(directoryPath, title);
        }
    }

    private static void createEmptyMp4File(String directoryPath, String title) throws IOException {
        String fileName = title.replaceAll("[^\\p{L}0-9\\s]", "") + ".mp4";
        File file = new File(directoryPath, fileName);
        file.createNewFile();
    }

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("/app.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("File AutoRun File Creator");
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();
    }
}