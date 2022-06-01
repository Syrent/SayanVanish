package ir.sayandevelopment.command;

import com.google.common.base.Strings;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import ir.sayandevelopment.VelocityMain;
import ir.sayandevelopment.utils.CommonUtils;
import ir.sayandevelopment.utils.ProgressBar;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class GListCommand implements SimpleCommand {
    @Override
    public void execute(Invocation invocation) {
        Player player = (Player) invocation.source();
        MiniMessage miniMessage = MiniMessage.miniMessage();
        boolean canSeeVanishedPlayers = player.hasPermission("sayanvanish.command.glist.showvanished");

        player.sendMessage(miniMessage.deserialize(String.format("<bold><gradient:#F09D00:#F8BD04><st>                </st></gradient></bold>" +
                " <gradient:#F2E205:#F2A30F>Network Global List</gradient> <dark_gray>(<gradient:#00E4FF:#0097FF>%s</gradient><dark_gray>) " +
                "<bold><gradient:#F8BD04:#F09D00><st>                </st></gradient></bold>",
                canSeeVanishedPlayers ? VelocityMain.INSTANCE.getServer().getAllPlayers().size() : CommonUtils.getNonVanishedPlayers().size())));

        HashMap<String, Collection<Player>> serverPlayerMap = new HashMap<>();

        for (RegisteredServer registeredServer : VelocityMain.INSTANCE.getServer().getAllServers()) {
            if (registeredServer.getPlayersConnected().size() > 0)
                serverPlayerMap.put(registeredServer.getServerInfo().getName(), registeredServer.getPlayersConnected());
        }

        for (Map.Entry<String, Collection<Player>> server : sortByValue(serverPlayerMap).entrySet()) {
            String gameModeName = server.getKey();
            int playerCount = server.getValue().size();
            int nonVanishedPlayerCount = CommonUtils.getNonVanishedPlayers(server.getValue()).size();

            String progress = ProgressBar.progressBar(nonVanishedPlayerCount,
                    VelocityMain.INSTANCE.getServer().getPlayerCount(),
                    45, "|", "<gray>|");

            List<Player> players = new ArrayList<>(serverPlayerMap.get(server.getKey()));
            boolean hasVanishedPlayer = server.getValue().stream().anyMatch(CommonUtils::isPlayerVanished);

            String message = String.format(
                    "<reset><hover:show_text:'<color:#C1D6F1>▣ Players:<color:#929CCB>%s</color>'>" +
                            "<gray>[<gradient:#17FF00:#F3FF00:#FF1F00>%s</gradient><gray>] " +
                            "<dark_gray>(<gradient:#00E4FF:#0097FF>%s</gradient><dark_gray>) <white>« <aqua>%s",
                    canSeeVanishedPlayers ? playerCount == 0 ? " No one is playing on this server!" : formatPlayerList(players, true) : nonVanishedPlayerCount == 0 ?
                            " No one is playing on this server!" : formatPlayerList(players, false), progress, canSeeVanishedPlayers ? playerCount : nonVanishedPlayerCount,
                    hasVanishedPlayer && canSeeVanishedPlayers ? "<yellow>" + gameModeName + "</yellow>" : gameModeName);

            player.sendMessage(miniMessage.deserialize(message));
        }
    }

    private String formatPlayerList(List<Player> players, boolean showVanishedPlayers) {
        if (!showVanishedPlayers)
            players = new ArrayList<>(CommonUtils.getNonVanishedPlayers(players));

        StringBuilder formatted = new StringBuilder();

        formatted.append("\n");
        for (Player player : players) {
            formatted.append(CommonUtils.isPlayerVanished(player) ? "<red>" + player.getUsername() + "</red>" : player.getUsername()).append(", ");
        }

        return formatted.substring(0, formatted.length() - 2);
    }

    public HashMap<String, Collection<Player>> sortByValue(HashMap<String, Collection<Player>> hashMap) {
        return hashMap.entrySet().stream()
                .sorted(Comparator.comparingInt(e -> ((Map.Entry<String, Collection<Player>>) e).getValue().size()).reversed())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (a, b) -> { throw new AssertionError(); }, LinkedHashMap::new));
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        return SimpleCommand.super.suggest(invocation);
    }

    @Override
    public CompletableFuture<List<String>> suggestAsync(Invocation invocation) {
        return SimpleCommand.super.suggestAsync(invocation);
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return SimpleCommand.super.hasPermission(invocation);
    }
}
