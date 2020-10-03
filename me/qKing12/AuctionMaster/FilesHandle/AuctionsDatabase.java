package me.qKing12.AuctionMaster.FilesHandle;

import me.qKing12.AuctionMaster.AuctionObjects.Auction;
import me.qKing12.AuctionMaster.AuctionObjects.AuctionBIN;
import me.qKing12.AuctionMaster.AuctionObjects.AuctionClassic;
import me.qKing12.AuctionMaster.AuctionMaster;
import me.qKing12.AuctionMaster.Utils.utils;
import org.bukkit.Bukkit;

import java.sql.*;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AuctionsDatabase {

    private String url;

    private void loadAuctionsFile() {

        try(
                Connection Auctions = DriverManager.getConnection(url);
                PreparedStatement stmt = Auctions.prepareStatement(
                        "CREATE TABLE IF NOT EXISTS Auctions " +
                                "(id VARCHAR(36) not NULL, " +
                                " coins DOUBLE(25, 0), " +
                                " ending BIGINT(15), " +
                                " sellerDisplayName VARCHAR(50), " +
                                " sellerName VARCHAR(16), " +
                                " sellerUUID VARCHAR(36), " +
                                " item MEDIUMTEXT, " +
                                " displayName VARCHAR(40), " +
                                " bids MEDIUMTEXT, " +
                                " sellerClaimed BOOL, "+
                                " PRIMARY KEY ( id ))"
                )
                ) {

            stmt.execute();
            AuctionMaster.plugin.getLogger().info("Succesfully connected to the Auctions Database SQL.");
        }catch(Exception x){
            AuctionMaster.plugin.getLogger().info("Failed to connect to Auctions Database SQL.");
            x.printStackTrace();
        }

        try(
                Connection Auctions = DriverManager.getConnection(url);
                PreparedStatement stmt = Auctions.prepareStatement(
                        "CREATE TABLE IF NOT EXISTS AuctionLists " +
                        "(id VARCHAR(36) not NULL, " +
                        " ownAuctions MEDIUMTEXT, " +
                        " ownBids MEDIUMTEXT, "+
                        " PRIMARY KEY ( id ))");
        ){
            stmt.execute();
            AuctionMaster.plugin.getLogger().info("Succesfully connected to the Auctions Player List SQL.");
        }catch(Exception x){
            AuctionMaster.plugin.getLogger().info("Failed to connect to Auctions Player List SQL.");
            x.printStackTrace();
        }

        try(Connection Auctions = DriverManager.getConnection(url);
        PreparedStatement stmt = Auctions.prepareStatement(
                "CREATE TABLE IF NOT EXISTS PreviewData " +
                        "(id VARCHAR(36) not NULL, " +
                        " item MEDIUMTEXT, "+
                        " PRIMARY KEY ( id ))");
        ){

            stmt.execute();
            AuctionMaster.plugin.getLogger().info("Succesfully connected to the Preview Data SQL.");
        }catch(Exception x){
            AuctionMaster.plugin.getLogger().info("Failed to connect to Preview Data SQL.");
            x.printStackTrace();
        }
    }

    public void loadPreviewItems(){
        try(
                Connection Auctions = DriverManager.getConnection(url);
                ResultSet resultSet = Auctions.prepareStatement("SELECT * FROM PreviewData").executeQuery();
        ){
            while(resultSet.next()) {
                String uuid = resultSet.getString(1);
                if(!uuid.equalsIgnoreCase("serverCloseDate")){
                    try{
                        AuctionMaster.auctionsHandler.previewItems.put(uuid, utils.itemFromBase64(resultSet.getString(2)));
                    }catch (Exception x){
                        AuctionMaster.plugin.getLogger().info("Tried to load preview item for player with UUID="+uuid+" but failed!");
                    }
                }
            }
        }catch(Exception x){
            x.printStackTrace();
        }
    }

    public void registerPreviewItem(String player, String item) {
        try (
                Connection Auctions = DriverManager.getConnection(url);
                PreparedStatement stmt1 = Auctions.prepareStatement("UPDATE PreviewData SET item = ? WHERE id = ?");
                PreparedStatement stmt2 = Auctions.prepareStatement("INSERT INTO PreviewData VALUES(?, ?)");
        ) {
            stmt1.setString(1, item);
            stmt1.setString(2, player);
            int updated = stmt1.executeUpdate();
            if (updated == 0) {
                stmt2.setString(1, player);
                stmt2.setString(2, item);
                stmt2.executeUpdate();
            }
        } catch (Exception x) {
            if (x.getMessage().startsWith("[SQLITE_BUSY]")) {
                Bukkit.getScheduler().runTaskLaterAsynchronously(AuctionMaster.plugin, () -> registerPreviewItem(player, item), 7);
            } else
                x.printStackTrace();
        }
    }

    public void removePreviewItem(String player){
        Bukkit.getScheduler().runTaskAsynchronously(AuctionMaster.plugin, () -> {
            try (
                    Connection Auctions = DriverManager.getConnection(url);
                    PreparedStatement stmt = Auctions.prepareStatement("DELETE FROM PreviewData WHERE id = ?");
            ) {
                stmt.setString(1, player);
                stmt.executeUpdate();
            } catch (Exception x) {
                if (x.getMessage().startsWith("[SQLITE_BUSY]")) {
                    Bukkit.getScheduler().runTaskLaterAsynchronously(AuctionMaster.plugin, () -> removePreviewItem(player), 7);
                } else
                    x.printStackTrace();
            }
        });
    }

    public AuctionsDatabase() {
        try{
            Class.forName("org.sqlite.JDBC");
        }catch(Exception x){
            x.printStackTrace();
        }
        url = "jdbc:sqlite:" + AuctionMaster.plugin.getDataFolder() + "/database/auctionsData.db";
        loadAuctionsFile();
        loadAuctionsDataFromFile();
    }

    public void insertAuction(Auction auction){
        Bukkit.getScheduler().runTaskAsynchronously(AuctionMaster.plugin, () -> {
            try(
                    Connection Auctions = DriverManager.getConnection(url);
                    PreparedStatement stmt = Auctions.prepareStatement("INSERT INTO Auctions VALUES (?, ?, ?, ?, ?, ?, ?, ?, '"+(auction.isBIN()?"BIN":"")+" 0,,, ', 0)");
                    ){
                stmt.setString(1, auction.getId());
                stmt.setDouble(2, auction.getCoins());
                stmt.setLong(3, auction.getEndingDate());
                stmt.setString(4, auction.getSellerDisplayName());
                stmt.setString(5, auction.getSellerName());
                stmt.setString(6, auction.getSellerUUID());
                stmt.setString(7, utils.itemToBase64(auction.getItemStack()));
                stmt.setString(8, auction.getDisplayName());
                stmt.executeUpdate();
            }catch(Exception x){
                if(x.getMessage().startsWith("[SQLITE_BUSY]")) {
                    Bukkit.getScheduler().runTaskLaterAsynchronously(AuctionMaster.plugin, () -> insertAuction(auction), 7);
                }
                else
                    x.printStackTrace();
            }
        });
    }

    public void updateAuctionField(String id, HashMap<String, String> toUpdate){
        Bukkit.getScheduler().runTaskAsynchronously(AuctionMaster.plugin, () -> {
            String toSet = "";
            for(Map.Entry<String, String> entry : toUpdate.entrySet()){
                toSet=toSet.concat(","+entry.getKey()+"="+entry.getValue());
            }
            toSet=toSet.substring(1);
            try(
            Connection Auctions = DriverManager.getConnection(url);
            PreparedStatement stmt = Auctions.prepareStatement("UPDATE Auctions SET "+toSet+" WHERE id = ?");
            ){
                stmt.setString(1, id);
                stmt.executeUpdate();
            }catch(Exception x){
                if(x.getMessage().startsWith("[SQLITE_BUSY]")) {
                    Bukkit.getScheduler().runTaskLaterAsynchronously(AuctionMaster.plugin, () -> updateAuctionField(id, toUpdate), 7);
                }
                else
                    x.printStackTrace();
                AuctionMaster.plugin.getLogger().info(toSet);
            }
        });
    }

    public void deleteAuction(String id){
        Bukkit.getScheduler().runTaskAsynchronously(AuctionMaster.plugin, () -> {
            try (
                    Connection Auctions = DriverManager.getConnection(url);
                    PreparedStatement stmt = Auctions.prepareStatement("DELETE FROM Auctions WHERE id = ?");
            ) {
                stmt.setString(1, id);
                stmt.executeUpdate();
            } catch (Exception x) {
                if (x.getMessage().startsWith("[SQLITE_BUSY]")) {
                    Bukkit.getScheduler().runTaskLaterAsynchronously(AuctionMaster.plugin, () -> deleteAuction(id), 7);
                } else
                    x.printStackTrace();
            }
        });
    }

    public void addToOwnBids(String player, String toAdd){
        Bukkit.getScheduler().runTaskAsynchronously(AuctionMaster.plugin, () -> {
            try (
                    Connection Auctions = DriverManager.getConnection(url);
                    PreparedStatement stmt2 = Auctions.prepareStatement("INSERT INTO AuctionLists VALUES(?, '', ?)");
                    PreparedStatement stmt1 = Auctions.prepareStatement("UPDATE AuctionLists SET ownBids = ownBids || (CASE WHEN ownBids = '' THEN '" + toAdd + "' ELSE '." + toAdd + "' END) WHERE id = ?");

            ) {
                stmt1.setString(1, player);
                int updated = stmt1.executeUpdate();
                if (updated == 0) {
                    stmt2.setString(1, player);
                    stmt2.setString(2, toAdd);
                    stmt2.executeUpdate();
                }
            } catch (Exception x) {
                if (x.getMessage().startsWith("[SQLITE_BUSY]")) {
                    Bukkit.getScheduler().runTaskLaterAsynchronously(AuctionMaster.plugin, () -> addToOwnBids(player, toAdd), 7);
                } else
                    x.printStackTrace();
            }
        });
    }

    public void removeFromOwnBids(String player, String toRemove){
        Bukkit.getScheduler().runTaskAsynchronously(AuctionMaster.plugin, () -> {
            try (
                    Connection Auctions = DriverManager.getConnection(url);
                    PreparedStatement stmt = Auctions.prepareStatement("UPDATE AuctionLists SET ownBids = REPLACE(REPLACE(REPLACE(ownBids, '" + toRemove + ".', ''), '." + toRemove + "', ''), '" + toRemove + "', '') WHERE id = ?");
            ) {
                stmt.setString(1, player);
                stmt.executeUpdate();
            } catch (Exception x) {
                if (x.getMessage().startsWith("[SQLITE_BUSY]")) {
                    Bukkit.getScheduler().runTaskLaterAsynchronously(AuctionMaster.plugin, () -> removeFromOwnBids(player, toRemove), 7);
                } else
                    x.printStackTrace();
            }
        });
    }

    public void resetOwnBids(String player){
        Bukkit.getScheduler().runTaskAsynchronously(AuctionMaster.plugin, () -> {
            try (
                    Connection Auctions = DriverManager.getConnection(url);
                    PreparedStatement stmt = Auctions.prepareStatement("UPDATE AuctionLists SET ownBids = '' WHERE id = ?");
            ) {
                stmt.setString(1, player);
                stmt.executeUpdate();
            } catch (Exception x) {
                if (x.getMessage().startsWith("[SQLITE_BUSY]")) {
                    Bukkit.getScheduler().runTaskLaterAsynchronously(AuctionMaster.plugin, () -> resetOwnBids(player), 7);
                } else
                    x.printStackTrace();
            }
        });
    }

    public void removeFromOwnAuctions(String player, String toRemove){
        Bukkit.getScheduler().runTaskAsynchronously(AuctionMaster.plugin, () -> {
            try (
                    Connection Auctions = DriverManager.getConnection(url);
                    PreparedStatement stmt = Auctions.prepareStatement("UPDATE AuctionLists SET ownAuctions = REPLACE(REPLACE(REPLACE(ownAuctions, '" + toRemove + ".', ''), '." + toRemove + "', ''), '" + toRemove + "', '') WHERE id = ?");
            ) {
                stmt.setString(1, player);
                stmt.executeUpdate();
            } catch (Exception x) {
                if (x.getMessage().startsWith("[SQLITE_BUSY]")) {
                    Bukkit.getScheduler().runTaskLaterAsynchronously(AuctionMaster.plugin, () -> removeFromOwnAuctions(player, toRemove), 7);
                } else
                    x.printStackTrace();
            }
        });
    }

    public void resetOwnAuctions(String player){
        Bukkit.getScheduler().runTaskAsynchronously(AuctionMaster.plugin, () -> {
            try (
                    Connection Auctions = DriverManager.getConnection(url);
                    PreparedStatement stmt = Auctions.prepareStatement("UPDATE AuctionLists SET ownAuctions = '' WHERE id = ?");
            ) {
                stmt.setString(1, player);
                stmt.executeUpdate();
            } catch (Exception x) {
                if (x.getMessage().startsWith("[SQLITE_BUSY]")) {
                    Bukkit.getScheduler().runTaskLaterAsynchronously(AuctionMaster.plugin, () -> resetOwnAuctions(player), 7);
                } else
                    x.printStackTrace();
            }
        });
    }

    public void addToOwnAuctions(String player, String toAdd){
        Bukkit.getScheduler().runTaskAsynchronously(AuctionMaster.plugin, () -> {
            try (
                    Connection Auctions = DriverManager.getConnection(url);
                    PreparedStatement stmt2 = Auctions.prepareStatement("INSERT INTO AuctionLists VALUES(?, ?, '')");
                    PreparedStatement stmt1 = Auctions.prepareStatement("UPDATE AuctionLists SET ownAuctions = ownAuctions || (CASE WHEN ownAuctions = '' THEN '" + toAdd + "' ELSE '." + toAdd + "' END) WHERE id = ?");

            ) {
                stmt1.setString(1, player);
                int updated = stmt1.executeUpdate();
                if (updated == 0) {
                    stmt2.setString(1, player);
                    stmt2.setString(2, toAdd);
                    stmt2.executeUpdate();
                }
            } catch (Exception x) {
                if (x.getMessage().startsWith("[SQLITE_BUSY]")) {
                    Bukkit.getScheduler().runTaskLaterAsynchronously(AuctionMaster.plugin, () -> addToOwnAuctions(player, toAdd), 7);
                } else
                    x.printStackTrace();
            }
        });
    }

    private void adjustAuctionTimers(long toAdd) {
        try (
                Connection Auctions = DriverManager.getConnection(url);
                PreparedStatement stmt = Auctions.prepareStatement("UPDATE Auctions SET ending=ending+?");
        ) {
            stmt.setLong(1, toAdd);
            stmt.executeUpdate();
        } catch (Exception x) {
            if (x.getMessage().startsWith("[SQLITE_BUSY]")) {
                Bukkit.getScheduler().runTaskLaterAsynchronously(AuctionMaster.plugin, () -> adjustAuctionTimers(toAdd), 7);
            } else
                x.printStackTrace();
        }
    }

    private void addAllToBrowse(){
        for(Auction auction : AuctionMaster.auctionsHandler.auctions.values())
            if(!auction.isEnded()){
                AuctionMaster.auctionsHandler.addToBrowse(auction);
            }
    }

    public void loadAuctionsDataFromFile(){
        long toAdd=0;
        try(
                Connection Auctions = DriverManager.getConnection(url);
                ResultSet resultSet = Auctions.prepareStatement("SELECT item FROM PreviewData WHERE id = 'serverCloseDate'").executeQuery();
                ){
            if(resultSet.next()){
                toAdd=Long.parseLong(resultSet.getString(1));
            }
        }catch(Exception x){
            x.printStackTrace();
        }

        if(toAdd!=0) {
            adjustAuctionTimers(ZonedDateTime.now().toInstant().toEpochMilli()-toAdd);
        }

        try(
                Connection Auctions = DriverManager.getConnection(url);
                ResultSet resultSet = Auctions.prepareStatement("SELECT * FROM Auctions").executeQuery();
                ){
            while(resultSet.next()) {
                if(resultSet.getString(9).startsWith("BIN"))
                    AuctionMaster.auctionsHandler.auctions.put(resultSet.getString(1), new AuctionBIN(resultSet.getString(1), resultSet.getDouble(2), resultSet.getLong(3), resultSet.getString(4), resultSet.getString(5), resultSet.getString(6), resultSet.getString(7), resultSet.getString(8), resultSet.getString(9), resultSet.getBoolean(10)));
                else
                    AuctionMaster.auctionsHandler.auctions.put(resultSet.getString(1), new AuctionClassic(resultSet.getString(1), resultSet.getDouble(2), resultSet.getLong(3), resultSet.getString(4), resultSet.getString(5), resultSet.getString(6), resultSet.getString(7), resultSet.getString(8), resultSet.getString(9), resultSet.getBoolean(10)));
            }
        }catch(Exception x){
            x.printStackTrace();
        }

        try(
                Connection Auctions = DriverManager.getConnection(url);
                PreparedStatement stmt = Auctions.prepareStatement("DELETE FROM AuctionLists WHERE ownAuctions='' AND ownBids=''");
                ){
            stmt.executeUpdate();
        }catch(Exception x){
            x.printStackTrace();
        }

        try(
                Connection Auctions = DriverManager.getConnection(url);
                ResultSet resultSet = Auctions.prepareStatement("SELECT * FROM AuctionLists").executeQuery();
                ){
            while(resultSet.next()) {
                String id = resultSet.getString(1);
                String ownAuctions = resultSet.getString(2);
                String ownBids = resultSet.getString(3);
                ArrayList<Auction> AuctionsArray = new ArrayList<>();
                if(!ownAuctions.equals("")) {
                    for (String idToAdd : ownAuctions.split("\\.")) {
                        try {
                            if(idToAdd.equals("")){
                                AuctionMaster.plugin.getLogger().warning("Tried to add an null auction to the own auctions list");
                                continue;
                            }
                            Auction auction = AuctionMaster.auctionsHandler.auctions.get(idToAdd);
                            if(auction!=null)
                                AuctionsArray.add(auction);
                            else
                                AuctionMaster.plugin.getLogger().warning("Tried to add an auction that is not in the auction list to own auctions. ID=" + idToAdd);
                        } catch (Exception x) {
                            AuctionMaster.plugin.getLogger().warning("Tried to add an auction that is not in the auction list to own auctions. ID=" + idToAdd);
                        }
                    }
                    if (!AuctionsArray.isEmpty())
                        AuctionMaster.auctionsHandler.ownAuctions.put(id, (ArrayList<Auction>)AuctionsArray.clone());
                    AuctionsArray.clear();
                }
                if(!ownBids.equals("")) {
                    for (String idToAdd : ownBids.split("\\."))
                        try {
                            if(idToAdd.equals("")){
                                AuctionMaster.plugin.getLogger().warning("Tried to add an null auction to the bid list");
                                continue;
                            }

                            Auction auction = AuctionMaster.auctionsHandler.auctions.get(idToAdd);
                            if(auction!=null)
                                AuctionsArray.add(auction);
                            else{
                                AuctionMaster.plugin.getLogger().warning("Tried to add an auction that is not in the auction list to own bids. ID=" + idToAdd);
                            }
                        } catch (Exception x) {
                            AuctionMaster.plugin.getLogger().warning("Tried to add an auction that is not in the auction list to own bids. ID=" + idToAdd);
                        }
                    if (!AuctionsArray.isEmpty())
                        AuctionMaster.auctionsHandler.bidAuctions.put(id, AuctionsArray);
                }
            }
        }catch(Exception x){
            x.printStackTrace();
        }
        addAllToBrowse();
        loadPreviewItems();
    }
}
