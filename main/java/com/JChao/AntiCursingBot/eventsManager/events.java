package com.JChao.AntiCursingBot.eventsManager;

import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.sql.*;
import java.util.concurrent.TimeUnit;


public class events extends ListenerAdapter {
    private Dotenv config = Dotenv.configure().load();
    String uname = "root";
    String password = config.get("PASSWORD");
    String url = "jdbc:mysql://localhost:3306/curseWords_schema";

    public void onMessageReceived(MessageReceivedEvent event) {
        String message = event.getMessage().getContentRaw().toLowerCase();

        try {
            String query = "SELECT * FROM cursewords where guildID = '" + event.getGuild().getId() + "'";
            Connection connection = DriverManager.getConnection(url, uname, password);
            Statement statement = connection.createStatement();
            ResultSet resultset = statement.executeQuery(query);

            while(resultset.next()) {
                String curseWord = resultset.getString("word");
                if(message.contains(curseWord)) {
                    try {
                        event.getGuild().timeoutFor(event.getAuthor(), 1, TimeUnit.HOURS).queue();
                        event.getGuild().getOwner().getUser().openPrivateChannel().
                                flatMap(channel -> channel.
                                        sendMessage("Inappropriate language in channel "
                                                + event.getChannel().getName() + " - "
                                                + event.getAuthor().getName() + "\n" + "\""
                                                + message + "\"")).queue();
                    }
                    catch(Exception e) {
                        continue;
                    }

                    query = "INSERT INTO records_table VALUES (?, ?, ?, ?, ?)";
                    PreparedStatement preparedStmt = connection.prepareStatement(query);
                    preparedStmt.setString(1, event.getAuthor().getId());
                    preparedStmt.setString(2, message);
                    preparedStmt.setString(3, event.getMessage().getTimeCreated().toString());
                    preparedStmt.setString(4, event.getChannel().getName());
                    preparedStmt.setString(5, event.getGuild().getId());
                    preparedStmt.execute();

                    event.getMessage().delete().queue();
                    break;
                }
            }
            connection.close();
            statement.close();
        }
        catch(SQLException e) {
            System.err.println(e.getMessage());
            event.getChannel().sendMessage("there was an error").queue();
        }
        catch(Exception ignored) {
        }
    }
}