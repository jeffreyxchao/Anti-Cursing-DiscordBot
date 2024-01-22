package com.JChao.AntiCursingBot.commandsManager;

import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class commands extends ListenerAdapter {
    private Dotenv config = Dotenv.configure().load();
    String uname = "root";
    String password = config.get("PASSWORD");
    String url = "jdbc:mysql://localhost:3306/curseWords_schema";

    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        String command = event.getName();
        if(command.equals("records")) {
            recordsCommand(event);
        }
        else if(command.equals("addword")) {
            addwordCommand(event);
        }
        else if(command.equals("removeword")) {
            removewordCommand(event);
        }
        else if(command.equals("showbannedwords")) {
            showBannedWordsCommand(event);
        }
        else {
            event.reply("not a valid command").setEphemeral(true).queue();
        }
    }

    public void recordsCommand(SlashCommandInteraction event) {
        OptionMapping user = event.getOption("username");
        assert user != null;
        String query = "SELECT * FROM records_table WHERE discordID = '" + user.getAsUser().getId() +
                "' AND guildID = '" + event.getGuild().getId() + "'" ;
        StringBuilder output = new StringBuilder();

        try {
            Connection connection = DriverManager.getConnection(url, uname, password);
            Statement statement = connection.createStatement();
            ResultSet resultset = statement.executeQuery(query);

            while(resultset.next()) {
                String message = resultset.getString("message");
                String date = resultset.getString("date");
                String channel = resultset.getString("channel");
                output.append("channel: " + channel + "\n" +
                        "date: " + date + "\n" + "\"" +
                        message + "\"" + "\n\n");
            }

            event.reply(output.toString()).setEphemeral(true).queue();
        }
        catch (SQLException e) {
            System.err.print(e.getMessage());
            event.reply("there was an error").setEphemeral(true).queue();
        }
    }

    public void addwordCommand(SlashCommandInteraction event) {
        String word = event.getOption("word").getAsString();
        String query = "INSERT INTO curseWords (word, guildID) VALUES(?, ?)";

        try {
            Connection connection = DriverManager.getConnection(url, uname, password);
            PreparedStatement preparedStmt = connection.prepareStatement(query);
            preparedStmt.setString(1, word);
            preparedStmt.setString(2, event.getGuild().getId());
            preparedStmt.execute();

            event.reply(word + " has been banned").setEphemeral(true).queue();
            connection.close();
        }
        catch(SQLException e) {
            System.err.println(e.getMessage());
            event.reply("there was an error").setEphemeral(true).queue();
        }
    }

    public void removewordCommand(SlashCommandInteraction event) {
        String word = event.getOption("word").getAsString();
        String query = "DELETE FROM curseWords WHERE word = ? AND guildID = ?";

        try {
            Connection connection = DriverManager.getConnection(url, uname, password);
            PreparedStatement preparedStmt = connection.prepareStatement(query);
            preparedStmt.setString(1, word);
            preparedStmt.setString(2, event.getGuild().getId());
            preparedStmt.execute();

            event.reply(word + " has been unbanned").setEphemeral(true).queue();
            connection.close();
        }
        catch(SQLException e) {
            event.reply("there was an error removing the word").setEphemeral(true).queue();
        }
    }

    public void showBannedWordsCommand(SlashCommandInteraction event) {
        String query = "SELECT * from curseWords WHERE guildID = ?";
        StringBuilder output = new StringBuilder();
        try {
            Connection connection = DriverManager.getConnection(url, uname, password);
            PreparedStatement preparedStmt = connection.prepareStatement(query);
            preparedStmt.setString(1, event.getGuild().getId());

            ResultSet rs = preparedStmt.executeQuery();
            while(rs.next()) {
                output.append(rs.getString("word") + '\n');
            }
            if(output.length() != 0) {
                event.reply(output.toString()).setEphemeral(true).queue();
            }
            else {
                event.reply("no words are banned").setEphemeral(true).queue();
            }
        }
        catch(SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void onGuildReady(GuildReadyEvent event) {
        List<CommandData> commands = new ArrayList<>();
        OptionData user = new OptionData(OptionType.USER, "username", "enter username", true);
        commands.add(Commands.slash("records", "see inappropriate chat log of a user").addOptions(user));

        OptionData word = new OptionData(OptionType.STRING, "word", "enter word/phrase", true);
        commands.add(Commands.slash("addword", "add a word/phrase you would like to ban from server").addOptions(word));

        commands.add(Commands.slash("removeword", "unban a word/phrase from the server").addOptions(word));

        commands.add(Commands.slash("showbannedwords", "show list of words that are banned from the server"));

        event.getGuild().updateCommands().addCommands(commands).queue();
    }
}